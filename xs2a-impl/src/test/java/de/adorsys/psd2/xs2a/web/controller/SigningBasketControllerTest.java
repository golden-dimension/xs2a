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

package de.adorsys.psd2.xs2a.web.controller;

import de.adorsys.psd2.model.SigningBasket;
import de.adorsys.psd2.model.SigningBasketResponse201;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.AdditionalPsuIdData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppNotificationData;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.NotificationModeResponseHeaders;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.sb.CreateSigningBasketRequest;
import de.adorsys.psd2.xs2a.domain.sb.CreateSigningBasketResponse;
import de.adorsys.psd2.xs2a.service.NotificationSupportedModeService;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import de.adorsys.psd2.xs2a.service.sb.SigningBasketService;
import de.adorsys.psd2.xs2a.web.header.ResponseHeaders;
import de.adorsys.psd2.xs2a.web.header.SigningBasketHeadersBuilder;
import de.adorsys.psd2.xs2a.web.mapper.SigningBasketModelMapper;
import de.adorsys.psd2.xs2a.web.mapper.TppRedirectUriMapper;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.function.Function;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SigningBasketControllerTest {
    private static final String TPP_REDIRECT_URI = "tpp-redirect-uri";
    private static final String TPP_NOK_REDIRECT_URI = "tpp-nok-redirect-uri";
    private static final String TPP_NOTIFICATION_CONTENT_PREFERRED = "sca";
    private static final String TPP_NOTIFICATION_URI = "tpp-notification-uri";
    private static final String INSTANCE_ID = "bank1";
    private static final String PSU_ID = "anton.brueckner";
    private static final ResponseHeaders RESPONSE_HEADERS = ResponseHeaders.builder().aspspScaApproach(ScaApproach.REDIRECT).build();

    @InjectMocks
    private SigningBasketController signingBasketController;
    @Mock
    private SigningBasketService signingBasketService;
    @Mock
    private ResponseMapper responseMapper;
    @Mock
    private ResponseErrorMapper responseErrorMapper;
    @Mock
    private TppRedirectUriMapper tppRedirectUriMapper;
    @Mock
    private NotificationSupportedModeService notificationSupportedModeService;
    @Mock
    private SigningBasketModelMapper signingBasketModelMapper;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private SigningBasketHeadersBuilder signingBasketHeadersBuilder;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void createSigningBasket_creationError() {
        // Given
        TppRedirectUri tppRedirectUri = new TppRedirectUri(TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI);
        when(tppRedirectUriMapper.mapToTppRedirectUri(TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI)).thenReturn(tppRedirectUri);

        TppNotificationData tppNotificationData = new TppNotificationData(Collections.singletonList(NotificationSupportedMode.SCA), TPP_NOTIFICATION_URI);
        when(notificationSupportedModeService.getTppNotificationData(TPP_NOTIFICATION_CONTENT_PREFERRED, TPP_NOTIFICATION_URI)).thenReturn(tppNotificationData);

        when(requestProviderService.getInstanceId()).thenReturn(INSTANCE_ID);

        SigningBasket signingBasket = jsonReader.getObjectFromFile("json/sb/signing-basket.json", SigningBasket.class);

        CreateSigningBasketRequest createSigningBasketRequest = jsonReader.getObjectFromFile("json/sb/create-signing-basket-request.json", CreateSigningBasketRequest.class);

        when(signingBasketModelMapper.mapToCreateSigningBasketRequest(signingBasket, tppRedirectUri, tppNotificationData, INSTANCE_ID)).thenReturn(createSigningBasketRequest);

        MessageError messageError = new MessageError(ErrorType.SB_405, of(MessageErrorCode.SERVICE_INVALID_405_SB));
        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null,
                                            new AdditionalPsuIdData(null, null, null, null, null, null, null, null, null));
        when(signingBasketService.createSigningBasket(createSigningBasketRequest, psuIdData, true))
            .thenReturn(ResponseObject.<CreateSigningBasketResponse>builder()
                            .fail(messageError)
                            .build());

        when(responseErrorMapper.generateErrorResponse(messageError))
            .thenReturn(new ResponseEntity<>(HttpStatus.METHOD_NOT_ALLOWED));

        // When
        ResponseEntity actual = signingBasketController.createSigningBasket(null, null, signingBasket,
                                                                            null, null, null,
                                                                            PSU_ID, null, null,
                                                                            null, null, null,
                                                                            TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI, null,
                                                                            TPP_NOTIFICATION_URI, TPP_NOTIFICATION_CONTENT_PREFERRED, null,
                                                                            null, null, null,
                                                                            null, null, null,
                                                                            null, null);
        // Then
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.METHOD_NOT_ALLOWED);
    }

    @Test
    void createSigningBasket_success() {
        // Given
        TppRedirectUri tppRedirectUri = new TppRedirectUri(TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI);
        when(tppRedirectUriMapper.mapToTppRedirectUri(TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI)).thenReturn(tppRedirectUri);

        TppNotificationData tppNotificationData = new TppNotificationData(Collections.singletonList(NotificationSupportedMode.SCA), TPP_NOTIFICATION_URI);
        when(notificationSupportedModeService.getTppNotificationData(TPP_NOTIFICATION_CONTENT_PREFERRED, TPP_NOTIFICATION_URI)).thenReturn(tppNotificationData);

        when(requestProviderService.getInstanceId()).thenReturn(INSTANCE_ID);

        SigningBasket signingBasket = jsonReader.getObjectFromFile("json/sb/signing-basket.json", SigningBasket.class);

        CreateSigningBasketRequest createSigningBasketRequest = jsonReader.getObjectFromFile("json/sb/create-signing-basket-request.json", CreateSigningBasketRequest.class);

        when(signingBasketModelMapper.mapToCreateSigningBasketRequest(signingBasket, tppRedirectUri, tppNotificationData, INSTANCE_ID)).thenReturn(createSigningBasketRequest);

        PsuIdData psuIdData = new PsuIdData(PSU_ID, null, null, null, null,
                                            new AdditionalPsuIdData(null, null, null, null, null, null, null, null, null));

        CreateSigningBasketResponse createSigningBasketResponse = jsonReader.getObjectFromFile("json/sb/create-signing-basket-response.json", CreateSigningBasketResponse.class);

        when(signingBasketService.createSigningBasket(createSigningBasketRequest, psuIdData, true))
            .thenReturn(ResponseObject.<CreateSigningBasketResponse>builder()
                            .body(createSigningBasketResponse)
                            .build());

        NotificationModeResponseHeaders notificationModeResponseHeaders = new NotificationModeResponseHeaders(true, "content");
        when(notificationSupportedModeService.resolveNotificationHeaders(createSigningBasketResponse.getTppNotificationContentPreferred())).thenReturn(notificationModeResponseHeaders);

        when(signingBasketHeadersBuilder.buildCreateSigningBasketHeaders(any(), any())).thenReturn(RESPONSE_HEADERS);

        SigningBasketResponse201 signingBasketResponse201 = jsonReader.getObjectFromFile("json/sb/create-signing-basket-response.json", SigningBasketResponse201.class);
        ResponseEntity<SigningBasketResponse201> expected = new ResponseEntity<>(signingBasketResponse201, HttpStatus.CREATED);
        when(responseMapper.created(any(), any(Function.class), eq(RESPONSE_HEADERS))).thenReturn(expected);

        // When
        ResponseEntity actual = signingBasketController.createSigningBasket(null, null, signingBasket,
                                                                            null, null, null,
                                                                            PSU_ID, null, null,
                                                                            null, null, null,
                                                                            TPP_REDIRECT_URI, TPP_NOK_REDIRECT_URI, null,
                                                                            TPP_NOTIFICATION_URI, TPP_NOTIFICATION_CONTENT_PREFERRED, null,
                                                                            null, null, null,
                                                                            null, null, null,
                                                                            null, null);
        // Then
        assertThat(actual.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(actual).isEqualTo(expected);
    }
}
