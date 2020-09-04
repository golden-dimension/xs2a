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

import de.adorsys.psd2.consent.api.sb.CmsSigningBasketConsentsAndPaymentsResponse;
import de.adorsys.psd2.core.data.CoreSigningBasket;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.psu.AdditionalPsuIdData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.sb.CreateSigningBasketRequest;
import de.adorsys.psd2.xs2a.domain.sb.CreateSigningBasketResponse;
import de.adorsys.psd2.xs2a.domain.sb.Xs2aCreateSigningBasketResponse;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAuthenticationObjectMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aChallengeDataMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiSigningBasketMapper;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiSigningBasketTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.sb.SpiInitiateSigningBasketResponse;
import de.adorsys.psd2.xs2a.spi.domain.sb.SpiSigningBasket;
import de.adorsys.psd2.xs2a.spi.service.SigningBasketSpi;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SigningBasketServiceTest {
    private static final String PSU_ID = "anton.brueckner";
    private static final String TPP_ID = "Test TppId";
    private static final String BASKET_ID = "12345";
    private static final String INSTANCE_ID = "bank1";
    private static final String INTERNAL_REQUEST_ID = "5c2d5564-367f-4e03-a621-6bef76fa4208";

    @InjectMocks
    private SigningBasketService signingBasketService;
    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private SigningBasketValidationService validationService;
    @Mock
    private TppService tppService;
    @Mock
    private Xs2aSigningBasketService xs2aSigningBasketService;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    @Mock
    private SigningBasketSpi signingBasketSpi;
    @Mock
    private Xs2aToSpiSigningBasketMapper spiSigningBasketMapper;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper;
    @Mock
    private SpiToXs2aChallengeDataMapper spiToXs2aChallengeDataMapper;
    @Mock
    private InitialSpiAspspConsentDataProvider aspspConsentDataProvider;

    private final JsonReader jsonReader = new JsonReader();
    private PsuIdData psuIdData;
    private TppInfo tppInfo;
    private SpiContextData contextData;
    private SpiPsuData spiPsuData;

    @BeforeEach
    void setUp() {
        psuIdData = buildPsuIdData();
        tppInfo = buildTppInfo();
        contextData = buildSpiContextData();
        spiPsuData = buildSpiPsuData();
    }

    @Test
    void createSigningBasket_validationFailure() {
        // Given
        MessageError messageError = new MessageError(ErrorType.SB_405, of(MessageErrorCode.SERVICE_INVALID_405_SB));
        ValidationResult invalid = ValidationResult.invalid(messageError);

        CreateSigningBasketRequest request = jsonReader.getObjectFromFile("json/sb/create-signing-basket-request.json", CreateSigningBasketRequest.class);
        CmsSigningBasketConsentsAndPaymentsResponse cmsPisAndAisResponse = new CmsSigningBasketConsentsAndPaymentsResponse(Collections.emptyList(), Collections.emptyList());
        when(xs2aSigningBasketService.getConsentsAndPayments(request.getConsentIds(), request.getPaymentIds())).thenReturn(Optional.of(cmsPisAndAisResponse));

        when(validationService.validateSigningBasketOnCreate(request, psuIdData, cmsPisAndAisResponse, true)).thenReturn(invalid);

        // When
        ResponseObject<CreateSigningBasketResponse> actual = signingBasketService.createSigningBasket(request, psuIdData, true);

        // Then
        assertThat(actual.getError()).isNotNull();
        assertThat(actual.getError()).isEqualTo(messageError);
    }

    @Test
    void createSigningBasket_cmsSavingFailure() {
        // Given
        MessageError messageError = new MessageError(ErrorType.SB_400, of(MessageErrorCode.RESOURCE_UNKNOWN_400));

        CreateSigningBasketRequest request = jsonReader.getObjectFromFile("json/sb/create-signing-basket-request.json", CreateSigningBasketRequest.class);
        CmsSigningBasketConsentsAndPaymentsResponse cmsPisAndAisResponse = new CmsSigningBasketConsentsAndPaymentsResponse(Collections.emptyList(), Collections.emptyList());
        when(xs2aSigningBasketService.getConsentsAndPayments(request.getConsentIds(), request.getPaymentIds())).thenReturn(Optional.of(cmsPisAndAisResponse));

        when(validationService.validateSigningBasketOnCreate(request, buildPsuIdData(), cmsPisAndAisResponse, true)).thenReturn(ValidationResult.valid());
        when(tppService.getTppInfo()).thenReturn(tppInfo);
        when(xs2aSigningBasketService.createSigningBasket(request, cmsPisAndAisResponse, buildPsuIdData(), buildTppInfo())).thenReturn(Optional.empty());

        // When
        ResponseObject<CreateSigningBasketResponse> actual = signingBasketService.createSigningBasket(request, buildPsuIdData(), true);

        // Then
        assertThat(actual.getError()).isNotNull();
        assertThat(actual.getError()).isEqualTo(messageError);
    }

    @Test
    void createSigningBasket_spiFailure() {
        // Given
        MessageError messageError = new MessageError(ErrorType.SB_400, of(MessageErrorCode.PSU_CREDENTIALS_INVALID));

        CreateSigningBasketRequest request = jsonReader.getObjectFromFile("json/sb/create-signing-basket-request.json", CreateSigningBasketRequest.class);
        CmsSigningBasketConsentsAndPaymentsResponse cmsPisAndAisResponse = new CmsSigningBasketConsentsAndPaymentsResponse(Collections.emptyList(), Collections.emptyList());
        when(xs2aSigningBasketService.getConsentsAndPayments(request.getConsentIds(), request.getPaymentIds())).thenReturn(Optional.of(cmsPisAndAisResponse));

        when(validationService.validateSigningBasketOnCreate(request, psuIdData, cmsPisAndAisResponse, true)).thenReturn(ValidationResult.valid());
        when(tppService.getTppInfo()).thenReturn(buildTppInfo());

        CoreSigningBasket coreSigningBasket = new CoreSigningBasket();
        Xs2aCreateSigningBasketResponse xs2aCreateSigningBasketResponse = new Xs2aCreateSigningBasketResponse(BASKET_ID, coreSigningBasket, Collections.emptyList());
        when(xs2aSigningBasketService.createSigningBasket(request, cmsPisAndAisResponse, psuIdData, tppInfo)).thenReturn(Optional.of(xs2aCreateSigningBasketResponse));
        when(spiContextDataProvider.provide(psuIdData, tppInfo)).thenReturn(contextData);
        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider()).thenReturn(aspspConsentDataProvider);

        SpiSigningBasket spiSigningBasket = new SpiSigningBasket(BASKET_ID, INSTANCE_ID, Collections.emptyList(), Collections.emptyList(),
                                                                 SpiSigningBasketTransactionStatus.RCVD, INTERNAL_REQUEST_ID,
                                                                 Collections.singletonList(spiPsuData), false);

        when(spiSigningBasketMapper.mapToSpiSigningBasket(coreSigningBasket)).thenReturn(spiSigningBasket);

        TppMessage tppMessage = new TppMessage(MessageErrorCode.PSU_CREDENTIALS_INVALID);
        SpiResponse<SpiInitiateSigningBasketResponse> spiResponse = SpiResponse.<SpiInitiateSigningBasketResponse>builder()
                                                                        .error(tppMessage)
                                                                        .build();
        when(signingBasketSpi.initiateSigningBasket(contextData, spiSigningBasket, aspspConsentDataProvider))
            .thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.SB))
            .thenReturn(ErrorHolder
                            .builder(ErrorType.SB_400)
                            .tppMessages(TppMessageInformation.of(MessageErrorCode.PSU_CREDENTIALS_INVALID))
                            .build());

        // When
        ResponseObject<CreateSigningBasketResponse> actual = signingBasketService.createSigningBasket(request, buildPsuIdData(), true);

        // Then
        assertThat(actual.getError()).isNotNull();
        assertThat(actual.getError()).isEqualTo(messageError);
    }

    @Test
    void createSigningBasket_success() {
        // Given
        CreateSigningBasketRequest request = jsonReader.getObjectFromFile("json/sb/create-signing-basket-request.json", CreateSigningBasketRequest.class);
        CmsSigningBasketConsentsAndPaymentsResponse cmsPisAndAisResponse = new CmsSigningBasketConsentsAndPaymentsResponse(Collections.emptyList(), Collections.emptyList());
        when(xs2aSigningBasketService.getConsentsAndPayments(request.getConsentIds(), request.getPaymentIds())).thenReturn(Optional.of(cmsPisAndAisResponse));

        when(validationService.validateSigningBasketOnCreate(request, psuIdData, cmsPisAndAisResponse, true)).thenReturn(ValidationResult.valid());
        when(tppService.getTppInfo()).thenReturn(buildTppInfo());

        CoreSigningBasket coreSigningBasket = new CoreSigningBasket();
        coreSigningBasket.setBasketId(BASKET_ID);
        Xs2aCreateSigningBasketResponse xs2aCreateSigningBasketResponse = new Xs2aCreateSigningBasketResponse(BASKET_ID, coreSigningBasket, null);
        when(xs2aSigningBasketService.createSigningBasket(request, cmsPisAndAisResponse, psuIdData, tppInfo)).thenReturn(Optional.of(xs2aCreateSigningBasketResponse));
        when(spiContextDataProvider.provide(psuIdData, tppInfo)).thenReturn(contextData);
        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider()).thenReturn(aspspConsentDataProvider);

        SpiSigningBasket spiSigningBasket = new SpiSigningBasket(BASKET_ID, INSTANCE_ID, Collections.emptyList(), Collections.emptyList(),
                                                                 SpiSigningBasketTransactionStatus.RCVD, INTERNAL_REQUEST_ID,
                                                                 Collections.singletonList(spiPsuData), false);

        when(spiSigningBasketMapper.mapToSpiSigningBasket(coreSigningBasket)).thenReturn(spiSigningBasket);

        SpiInitiateSigningBasketResponse spiInitiateSigningBasketResponse = new SpiInitiateSigningBasketResponse(SpiSigningBasketTransactionStatus.RCVD, BASKET_ID,
                                                                                                                 Collections.emptyList(), null, null,
                                                                                                                 false, "psu mes", null);
        SpiResponse<SpiInitiateSigningBasketResponse> spiResponse = SpiResponse.<SpiInitiateSigningBasketResponse>builder()
                                                                        .payload(spiInitiateSigningBasketResponse)
                                                                        .build();

        when(signingBasketSpi.initiateSigningBasket(contextData, spiSigningBasket, aspspConsentDataProvider))
            .thenReturn(spiResponse);

        CreateSigningBasketResponse expected = jsonReader.getObjectFromFile("json/sb/create-signing-basket-response-without-links.json", CreateSigningBasketResponse.class);

        AuthenticationObject authenticationObject = jsonReader.getObjectFromFile("json/sb/authentication-object.json", AuthenticationObject.class);
        when(spiToXs2aAuthenticationObjectMapper.toAuthenticationObjectList(spiInitiateSigningBasketResponse.getScaMethods())).thenReturn(Collections.singletonList(authenticationObject));

        // When
        ResponseObject<CreateSigningBasketResponse> actual = signingBasketService.createSigningBasket(request, buildPsuIdData(), true);

        // Then
        assertThat(actual.getError()).isNull();
        assertThat(actual.getBody()).isEqualTo(expected);
    }

    @Test
    void createSigningBasket_successMultilevel() {
        // Given
        CreateSigningBasketRequest request = jsonReader.getObjectFromFile("json/sb/create-signing-basket-request.json", CreateSigningBasketRequest.class);
        CmsSigningBasketConsentsAndPaymentsResponse cmsPisAndAisResponse = new CmsSigningBasketConsentsAndPaymentsResponse(Collections.emptyList(), Collections.emptyList());
        when(xs2aSigningBasketService.getConsentsAndPayments(request.getConsentIds(), request.getPaymentIds())).thenReturn(Optional.of(cmsPisAndAisResponse));

        when(validationService.validateSigningBasketOnCreate(request, psuIdData, cmsPisAndAisResponse, true)).thenReturn(ValidationResult.valid());
        when(tppService.getTppInfo()).thenReturn(buildTppInfo());

        CoreSigningBasket coreSigningBasket = new CoreSigningBasket();
        coreSigningBasket.setBasketId(BASKET_ID);
        Xs2aCreateSigningBasketResponse xs2aCreateSigningBasketResponse = new Xs2aCreateSigningBasketResponse(BASKET_ID, coreSigningBasket, null);
        when(xs2aSigningBasketService.createSigningBasket(request, cmsPisAndAisResponse, psuIdData, tppInfo)).thenReturn(Optional.of(xs2aCreateSigningBasketResponse));
        when(spiContextDataProvider.provide(psuIdData, tppInfo)).thenReturn(contextData);
        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider()).thenReturn(aspspConsentDataProvider);

        SpiSigningBasket spiSigningBasket = new SpiSigningBasket(BASKET_ID, INSTANCE_ID, Collections.emptyList(), Collections.emptyList(),
                                                                 SpiSigningBasketTransactionStatus.RCVD, INTERNAL_REQUEST_ID,
                                                                 Collections.singletonList(spiPsuData), true);

        when(spiSigningBasketMapper.mapToSpiSigningBasket(coreSigningBasket)).thenReturn(spiSigningBasket);

        SpiInitiateSigningBasketResponse spiInitiateSigningBasketResponse = new SpiInitiateSigningBasketResponse(SpiSigningBasketTransactionStatus.RCVD, BASKET_ID,
                                                                                                                 Collections.emptyList(), null, null,
                                                                                                                 true, "psu mes", null);
        SpiResponse<SpiInitiateSigningBasketResponse> spiResponse = SpiResponse.<SpiInitiateSigningBasketResponse>builder()
                                                                        .payload(spiInitiateSigningBasketResponse)
                                                                        .build();

        when(signingBasketSpi.initiateSigningBasket(contextData, spiSigningBasket, aspspConsentDataProvider))
            .thenReturn(spiResponse);

        CreateSigningBasketResponse expected = jsonReader.getObjectFromFile("json/sb/create-signing-basket-response-multilevel.json", CreateSigningBasketResponse.class);

        AuthenticationObject authenticationObject = jsonReader.getObjectFromFile("json/sb/authentication-object.json", AuthenticationObject.class);
        when(spiToXs2aAuthenticationObjectMapper.toAuthenticationObjectList(spiInitiateSigningBasketResponse.getScaMethods())).thenReturn(Collections.singletonList(authenticationObject));

        // When
        ResponseObject<CreateSigningBasketResponse> actual = signingBasketService.createSigningBasket(request, buildPsuIdData(), true);

        // Then
        assertThat(actual.getError()).isNull();
        assertThat(actual.getBody()).isEqualTo(expected);
        verify(xs2aSigningBasketService, times(1)).updateMultilevelScaRequired(BASKET_ID, true);
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

    private SpiContextData buildSpiContextData() {
        return new SpiContextData(spiPsuData, tppInfo, null, null, null, null, null);
    }

    private SpiPsuData buildSpiPsuData() {
        return SpiPsuData.builder().psuId(PSU_ID).build();
    }
}
