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

package de.adorsys.psd2.xs2a.service.sb;

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasket;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketConsentsAndPaymentsResponse;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketCreationResponse;
import de.adorsys.psd2.consent.api.service.SigningBasketServiceEncrypted;
import de.adorsys.psd2.core.data.CoreSigningBasket;
import de.adorsys.psd2.xs2a.core.psu.AdditionalPsuIdData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sb.SigningBasketTransactionStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.sb.CreateSigningBasketRequest;
import de.adorsys.psd2.xs2a.domain.sb.Xs2aCreateSigningBasketResponse;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.CmsXs2aSigningBasketMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Xs2aSigningBasketServiceTest {
    private static final String PSU_ID = "anton.brueckner";
    private static final String TPP_ID = "Test TppId";
    private static final String BASKET_ID = "12345";
    private static final String INSTANCE_ID = "bank1";
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";

    @InjectMocks
    private Xs2aSigningBasketService xs2aSigningBasketService;
    @Mock
    private CmsXs2aSigningBasketMapper signingBasketMapper;
    @Mock
    private SigningBasketServiceEncrypted signingBasketService;

    private final JsonReader jsonReader = new JsonReader();
    private PsuIdData psuIdData;
    private TppInfo tppInfo;

    @BeforeEach
    void setUp() {
        psuIdData = buildPsuIdData();
        tppInfo = buildTppInfo();
    }

    @Test
    void createSigningBasket_failure() {
        // Given
        CreateSigningBasketRequest request = new CreateSigningBasketRequest(Collections.emptyList(), Collections.emptyList(), null, null, null);
        CmsSigningBasketConsentsAndPaymentsResponse cmsPisAndAisResponse = new CmsSigningBasketConsentsAndPaymentsResponse(Collections.emptyList(), Collections.emptyList());

        CmsSigningBasket cmsSigningBasket = new CmsSigningBasket();
        when(signingBasketMapper.mapToCmsSigningBasket(request, cmsPisAndAisResponse, psuIdData, tppInfo, SigningBasketTransactionStatus.RCVD)).thenReturn(cmsSigningBasket);

        CmsResponse<CmsSigningBasketCreationResponse> response = CmsResponse.<CmsSigningBasketCreationResponse>builder()
                                                                     .error(CmsError.LOGICAL_ERROR)
                                                                     .build();
        when(signingBasketService.createSigningBasket(cmsSigningBasket)).thenReturn(response);


        // When
        Optional<Xs2aCreateSigningBasketResponse> actual = xs2aSigningBasketService.createSigningBasket(request, cmsPisAndAisResponse, psuIdData, tppInfo);

        // Then
        assertThat(actual.isEmpty()).isTrue();
    }

    @Test
    void createSigningBasket_success() {
        // Given
        CreateSigningBasketRequest request = new CreateSigningBasketRequest(Collections.emptyList(), Collections.emptyList(), null, null, null);
        CmsSigningBasketConsentsAndPaymentsResponse cmsPisAndAisResponse = new CmsSigningBasketConsentsAndPaymentsResponse(Collections.emptyList(), Collections.emptyList());

        CmsSigningBasket cmsSigningBasket = new CmsSigningBasket();
        when(signingBasketMapper.mapToCmsSigningBasket(request, cmsPisAndAisResponse, psuIdData, tppInfo, SigningBasketTransactionStatus.RCVD)).thenReturn(cmsSigningBasket);

        CmsSigningBasketCreationResponse cmsSigningBasketCreationResponse = new CmsSigningBasketCreationResponse(BASKET_ID, cmsSigningBasket);
        CmsResponse<CmsSigningBasketCreationResponse> response = CmsResponse.<CmsSigningBasketCreationResponse>builder()
                                                                     .payload(cmsSigningBasketCreationResponse)
                                                                     .build();
        when(signingBasketService.createSigningBasket(cmsSigningBasket)).thenReturn(response);
        CoreSigningBasket coreSigningBasket = new CoreSigningBasket();
        when(signingBasketMapper.mapToCoreSigningBasket(cmsSigningBasket)).thenReturn(coreSigningBasket);

        // When
        Optional<Xs2aCreateSigningBasketResponse> actual = xs2aSigningBasketService.createSigningBasket(request, cmsPisAndAisResponse, psuIdData, tppInfo);
        Xs2aCreateSigningBasketResponse expected = new Xs2aCreateSigningBasketResponse(BASKET_ID, coreSigningBasket, null);

        // Then
        assertThat(actual.isEmpty()).isFalse();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    void getConsentsAndPayments_failure() {
        // Given
        List<String> consentIds = Collections.singletonList("consentId");
        List<String> paymentIds = Collections.singletonList("paymentId");

        when(signingBasketService.getConsentsAndPayments(consentIds, paymentIds))
            .thenReturn(CmsResponse.<CmsSigningBasketConsentsAndPaymentsResponse>builder()
                            .error(CmsError.LOGICAL_ERROR)
                            .build());

        // When
        Optional<CmsSigningBasketConsentsAndPaymentsResponse> actual = xs2aSigningBasketService.getConsentsAndPayments(consentIds, paymentIds);

        // Then
        assertThat(actual.isEmpty()).isTrue();
    }

    @Test
    void getConsentsAndPayments_success() {
        // Given
        List<String> consentIds = Collections.singletonList("consentId");
        List<String> paymentIds = Collections.singletonList("paymentId");

        CmsConsent cmsConsent = new CmsConsent();
        cmsConsent.setId("consentId");
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        pisCommonPaymentResponse.setExternalId("paymentId");
        CmsSigningBasketConsentsAndPaymentsResponse consentsAndPaymentsResponse =
            new CmsSigningBasketConsentsAndPaymentsResponse(Collections.singletonList(cmsConsent), Collections.singletonList(pisCommonPaymentResponse));
        when(signingBasketService.getConsentsAndPayments(consentIds, paymentIds))
            .thenReturn(CmsResponse.<CmsSigningBasketConsentsAndPaymentsResponse>builder()
                            .payload(consentsAndPaymentsResponse)
                            .build());

        // When
        Optional<CmsSigningBasketConsentsAndPaymentsResponse> actual = xs2aSigningBasketService.getConsentsAndPayments(consentIds, paymentIds);

        // Then
        assertThat(actual.isEmpty()).isFalse();
        assertThat(actual.get()).isEqualTo(consentsAndPaymentsResponse);
    }

    private PsuIdData buildPsuIdData() {
        return new PsuIdData(PSU_ID, null, null, null, null,
                             new AdditionalPsuIdData(null, null, null, null, null, null, null, null, null));
    }

    private TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(TPP_ID);
        return tppInfo;
    }
}
