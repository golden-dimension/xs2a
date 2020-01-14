/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.web.aspect;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.pis.CancelPaymentResponse;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.PaymentCancellationAuthorisationNeededDecider;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.link.PaymentCancellationLinks;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_400;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCancellationAspectTest {
    private static final String HTTP_URL = "http://base.url";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "1111111111111";
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType");
    private static final String AUTHORISATION_ID = "463318a0-1e33-45d8-8209-e16444b18dda";
    private CancelPaymentResponse response;

    @InjectMocks
    private PaymentCancellationAspect aspect;

    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    private AuthorisationMethodDecider authorisationMethodDecider;
    @Mock
    private PaymentCancellationAuthorisationNeededDecider cancellationScaNeededDecider;
    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private RedirectLinkBuilder redirectLinkBuilder;
    @Mock
    private RedirectIdService redirectIdService;

    private AspspSettings aspspSettings;
    private ResponseObject<CancelPaymentResponse> responseObject;
    private PisPaymentCancellationRequest paymentCancellationRequest;

    @BeforeEach
    void setUp() {
        JsonReader jsonReader = new JsonReader();
        aspspSettings = jsonReader.getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);

        response = new CancelPaymentResponse();
        response.setStartAuthorisationRequired(true);
        response.setAuthorizationId(AUTHORISATION_ID);
        response.setPaymentId(PAYMENT_ID);
        response.setPaymentProduct(PAYMENT_PRODUCT);
        response.setPaymentType(PaymentType.SINGLE);
        response.setPsuData(PSU_DATA);
        response.setTransactionStatus(TransactionStatus.ACCP);

        paymentCancellationRequest = new PisPaymentCancellationRequest(PaymentType.SINGLE, PAYMENT_PRODUCT, PAYMENT_ID,
                                                                       false, new TppRedirectUri("ok_url", "nok_url"));
    }

    @Test
    void cancelPayment_status_RJCT() {
        // Given
        when(cancellationScaNeededDecider.isScaRequired(true)).thenReturn(true);

        response.setTransactionStatus(TransactionStatus.RJCT);

        responseObject = ResponseObject.<CancelPaymentResponse>builder()
                             .body(response)
                             .build();
        // When
        ResponseObject<CancelPaymentResponse> actualResponse = aspect.cancelPayment(responseObject, paymentCancellationRequest);

        // Then
        assertFalse(actualResponse.hasError());
        assertEquals(actualResponse.getBody(), response);
    }

    @Test
    void cancelPayment_success() {
        // Given
        when(cancellationScaNeededDecider.isScaRequired(true)).thenReturn(true);

        when(authorisationMethodDecider.isExplicitMethod(false, false)).thenReturn(false);
        when(scaApproachResolver.resolveScaApproach()).thenReturn(ScaApproach.EMBEDDED);

        when(aspspProfileService.getAspspSettings()).thenReturn(aspspSettings);
        PaymentCancellationLinks links = new PaymentCancellationLinks(HTTP_URL, scaApproachResolver, redirectLinkBuilder, redirectIdService, response, false);

        responseObject = ResponseObject.<CancelPaymentResponse>builder()
                             .body(response)
                             .build();

        // When
        ResponseObject<CancelPaymentResponse> actualResponse = aspect.cancelPayment(responseObject, paymentCancellationRequest);

        // Then
        verify(aspspProfileService, times(1)).getAspspSettings();

        assertFalse(actualResponse.hasError());
        assertEquals(actualResponse.getBody().getLinks(), links);
    }

    @Test
    void createPisAuthorizationAspect_withError_shouldAddTextErrorMessage() {
        // When
        responseObject = ResponseObject.<CancelPaymentResponse>builder()
                             .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                             .build();
        ResponseObject<CancelPaymentResponse> actualResponse = aspect.cancelPayment(responseObject, paymentCancellationRequest);

        // Then
        assertTrue(actualResponse.hasError());
    }
}
