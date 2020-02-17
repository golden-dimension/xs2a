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

package de.adorsys.psd2.xs2a.service.authorization.pis.stage.initiation;

import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisCommonDecoupledService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAuthenticationObjectMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthenticationObject;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PisScaAuthenticatedStageTest {
    private static final String PAYMENT_PRODUCT = "Test payment product";
    private static final String PAYMENT_ID = "Test payment id";
    private static final String AUTHORISATION_ID = "Test authorisation id";
    private static final String AUTHENTICATION_METHOD_ID = "Test authentication method id";
    private static final String PSU_ID = "Test psuId";
    private static final PaymentType SINGLE_PAYMENT_TYPE = PaymentType.SINGLE;
    private static final TransactionStatus ACCP_TRANSACTION_STATUS = TransactionStatus.ACCP;
    private static final ServiceType PIS_SERVICE_TYPE = ServiceType.PIS;
    private static final MessageErrorCode SCA_METHOD_UNKNOWN = MessageErrorCode.SCA_METHOD_UNKNOWN;
    private static final ErrorType PIS_400_ERROR_TYPE = ErrorType.PIS_400;
    private static final ScaStatus FAILED_SCA_STATUS = ScaStatus.FAILED;
    private static final byte[] PAYMENT_DATA = "Test payment data".getBytes();
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(PSU_ID, null, null, null);
    private static final SpiPsuData SPI_PSU_DATA = new SpiPsuData(PSU_ID, null, null, null, null);
    private static final SpiContextData SPI_CONTEXT_DATA = new SpiContextData(SPI_PSU_DATA, new TppInfo(), UUID.randomUUID(), UUID.randomUUID());
    private static final PisPaymentInfo PAYMENT_INFO = buildPisPaymentInfo();
    private static final SpiPaymentInfo SPI_PAYMENT_INFO = buildSpiPaymentInfo();

    @InjectMocks
    private PisScaAuthenticatedStage pisScaAuthenticatedStage;

    @Mock
    private PaymentAuthorisationSpi paymentAuthorisationSpi;
    @Mock
    private Xs2aPisCommonPaymentService xs2aPisCommonPaymentService;
    @Mock
    private PisCommonDecoupledService pisCommonDecoupledService;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private Xs2aUpdatePisCommonPaymentPsuDataRequest request;
    @Mock
    private GetPisAuthorisationResponse response;
    @Mock
    private Xs2aUpdatePisCommonPaymentPsuDataResponse mockedExpectedResponse;
    @Mock
    private SpiAuthorizationCodeResult spiAuthorizationCodeResult;
    @Mock
    private SpiAuthenticationObject spiAuthenticationObject;
    @Mock
    private ChallengeData challengeData;
    @Mock
    private Xs2aAuthenticationObject xs2aAuthenticationObject;
    @Mock
    private Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    @Mock
    private SpiAspspConsentDataProvider spiAspspConsentDataProvider;

    @BeforeEach
    void setUp() {
        when(response.getPaymentType())
            .thenReturn(SINGLE_PAYMENT_TYPE);

        when(response.getPaymentProduct())
            .thenReturn(PAYMENT_PRODUCT);

        when(response.getPayments())
            .thenReturn(Collections.emptyList());

        when(response.getPaymentInfo())
            .thenReturn(PAYMENT_INFO);

        when(request.getAuthorisationId())
            .thenReturn(AUTHORISATION_ID);

        when(request.getAuthenticationMethodId())
            .thenReturn(AUTHENTICATION_METHOD_ID);

        when(xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(Collections.singletonList(PSU_ID_DATA)))
            .thenReturn(Collections.singletonList(SPI_PSU_DATA));
    }

    @Test
    void apply_Success_decoupledApproach() {
        // Given
        when(xs2aPisCommonPaymentService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID))
            .thenReturn(true);

        when(pisCommonDecoupledService.proceedDecoupledInitiation(request, SPI_PAYMENT_INFO, AUTHENTICATION_METHOD_ID))
            .thenReturn(mockedExpectedResponse);

        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisScaAuthenticatedStage.apply(request, response);

        // Then
        assertThat(actualResponse).isNotNull();
        verify(pisCommonDecoupledService).proceedDecoupledInitiation(request, SPI_PAYMENT_INFO, AUTHENTICATION_METHOD_ID);
    }

    @Test
    void apply_Failure_embeddedApproach_spiResponseHasError() {
        // Given
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(PAYMENT_ID)).thenReturn(spiAspspConsentDataProvider);

        when(xs2aPisCommonPaymentService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID))
            .thenReturn(false);

        when(request.getPsuData())
            .thenReturn(PSU_ID_DATA);

        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA))
            .thenReturn(SPI_CONTEXT_DATA);

        when(request.getPaymentId())
            .thenReturn(PAYMENT_ID);

        SpiResponse<SpiAuthorizationCodeResult> spiResponse = buildErrorSpiResponse();

        when(paymentAuthorisationSpi.requestAuthorisationCode(SPI_CONTEXT_DATA, AUTHENTICATION_METHOD_ID, SPI_PAYMENT_INFO, spiAspspConsentDataProvider))
            .thenReturn(spiResponse);

        when(spiErrorMapper.mapToErrorHolder(spiResponse, PIS_SERVICE_TYPE))
            .thenReturn(ErrorHolder.builder(ErrorType.PIS_400).tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR)).build());

        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisScaAuthenticatedStage.apply(request, response);

        // Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getScaStatus()).isEqualTo(FAILED_SCA_STATUS);
        assertThat(actualResponse.getErrorHolder().getErrorType()).isEqualTo(PIS_400_ERROR_TYPE);
    }

    @Test
    void apply_Failure_embeddedApproach_authorizationCodeResultIsEmpty() {
        // Given
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(PAYMENT_ID)).thenReturn(spiAspspConsentDataProvider);

        when(xs2aPisCommonPaymentService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID))
            .thenReturn(false);

        when(request.getPsuData())
            .thenReturn(PSU_ID_DATA);

        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA))
            .thenReturn(SPI_CONTEXT_DATA);

        when(request.getPaymentId())
            .thenReturn(PAYMENT_ID);

        SpiResponse<SpiAuthorizationCodeResult> spiResponse = buildSuccessSpiResponse();

        when(paymentAuthorisationSpi.requestAuthorisationCode(SPI_CONTEXT_DATA, AUTHENTICATION_METHOD_ID, SPI_PAYMENT_INFO, spiAspspConsentDataProvider))
            .thenReturn(spiResponse);

        when(spiAuthorizationCodeResult.isEmpty())
            .thenReturn(true);

        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisScaAuthenticatedStage.apply(request, response);

        // Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getScaStatus()).isEqualTo(FAILED_SCA_STATUS);
        assertThat(actualResponse.getErrorHolder().getTppMessageInformationList().iterator().next().getMessageErrorCode().getCode()).isEqualTo(SCA_METHOD_UNKNOWN.getCode());
    }

    @Test
    void apply_Success_embeddedApproach() {
        // Given
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(PAYMENT_ID)).thenReturn(spiAspspConsentDataProvider);

        when(xs2aPisCommonPaymentService.isAuthenticationMethodDecoupled(AUTHORISATION_ID, AUTHENTICATION_METHOD_ID))
            .thenReturn(false);

        when(request.getPsuData())
            .thenReturn(PSU_ID_DATA);

        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA))
            .thenReturn(SPI_CONTEXT_DATA);

        when(request.getPaymentId())
            .thenReturn(PAYMENT_ID);

        SpiResponse<SpiAuthorizationCodeResult> spiResponse = buildSuccessSpiResponse();

        when(paymentAuthorisationSpi.requestAuthorisationCode(SPI_CONTEXT_DATA, AUTHENTICATION_METHOD_ID, SPI_PAYMENT_INFO, spiAspspConsentDataProvider))
            .thenReturn(spiResponse);

        when(spiAuthorizationCodeResult.isEmpty())
            .thenReturn(false);

        when(spiAuthorizationCodeResult.getSelectedScaMethod())
            .thenReturn(spiAuthenticationObject);

        when(spiAuthorizationCodeResult.getChallengeData())
            .thenReturn(challengeData);

        when(spiToXs2aAuthenticationObjectMapper.mapToXs2aAuthenticationObject(spiAuthenticationObject))
            .thenReturn(xs2aAuthenticationObject);

        // When
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResponse = pisScaAuthenticatedStage.apply(request, response);

        // Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getChosenScaMethod()).isEqualTo(xs2aAuthenticationObject);
        assertThat(actualResponse.getChallengeData()).isEqualTo(challengeData);
    }

    private static PisPaymentInfo buildPisPaymentInfo() {
        PisPaymentInfo paymentInfo = new PisPaymentInfo();
        paymentInfo.setPaymentData(PAYMENT_DATA);
        paymentInfo.setPaymentId(PAYMENT_ID);
        paymentInfo.setPaymentProduct(PAYMENT_PRODUCT);
        paymentInfo.setPaymentType(SINGLE_PAYMENT_TYPE);
        paymentInfo.setTransactionStatus(ACCP_TRANSACTION_STATUS);
        paymentInfo.setPsuDataList(Collections.singletonList(PSU_ID_DATA));
        return paymentInfo;
    }

    private static SpiPaymentInfo buildSpiPaymentInfo() {
        SpiPaymentInfo paymentInfo = new SpiPaymentInfo(PAYMENT_PRODUCT);
        paymentInfo.setPaymentData(PAYMENT_DATA);
        paymentInfo.setPaymentId(PAYMENT_ID);
        paymentInfo.setPaymentType(SINGLE_PAYMENT_TYPE);
        paymentInfo.setStatus(ACCP_TRANSACTION_STATUS);
        paymentInfo.setPsuDataList(Collections.singletonList(SPI_PSU_DATA));
        return paymentInfo;
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private SpiResponse<SpiAuthorizationCodeResult> buildSuccessSpiResponse() {
        return SpiResponse.<SpiAuthorizationCodeResult>builder()
                   .payload(spiAuthorizationCodeResult)
                   .build();
    }

    // Needed because SpiResponse is final, so it's impossible to mock it
    private <T> SpiResponse<T> buildErrorSpiResponse() {
        return SpiResponse.<T>builder()
                   .error(new TppMessage(MessageErrorCode.FORMAT_ERROR))
                   .build();
    }
}