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
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sb.SigningBasketTransactionStatus;
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
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.sb.SpiInitiateSigningBasketResponse;
import de.adorsys.psd2.xs2a.spi.domain.sb.SpiSigningBasket;
import de.adorsys.psd2.xs2a.spi.service.SigningBasketSpi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;

@Slf4j
@Service
@AllArgsConstructor
public class SigningBasketService {
    private final Xs2aEventService xs2aEventService;
    private final SigningBasketValidationService validationService;
    private final TppService tppService;
    private final Xs2aSigningBasketService xs2aSigningBasketService;
    private final SpiContextDataProvider spiContextDataProvider;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final SigningBasketSpi signingBasketSpi;
    private final Xs2aToSpiSigningBasketMapper spiSigningBasketMapper;
    private final SpiErrorMapper spiErrorMapper;
    private final SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper;
    private final SpiToXs2aChallengeDataMapper spiToXs2aChallengeDataMapper;

    /**
     * Performs create signing basket operation
     *
     * @param request body of create signing basket request carrying such parameters as paymentIds, consentIds etc.
     * @param psuData PsuIdData container of authorisation data about PSU
     * @return CreateSigningBasketResponse representing the complete response to create signing basket request
     */
    public ResponseObject<CreateSigningBasketResponse> createSigningBasket(CreateSigningBasketRequest request, PsuIdData psuData, boolean explicitPreferred) {
        xs2aEventService.recordTppRequest(EventType.CREATE_SIGNING_BASKET_REQUEST_RECEIVED, request);

        CmsSigningBasketConsentsAndPaymentsResponse consentsAndPaymentsResponse = xs2aSigningBasketService.getConsentsAndPayments(request.getConsentIds(), request.getPaymentIds()).orElse(null);

        ValidationResult validationResult = validationService.validateSigningBasketOnCreate(request, psuData, consentsAndPaymentsResponse, explicitPreferred);

        if (validationResult.isNotValid()) {
            log.info("Create signing basket - validation failed: {}",
                     validationResult.getMessageError());
            return ResponseObject.<CreateSigningBasketResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        TppInfo tppInfo = tppService.getTppInfo();

        Optional<Xs2aCreateSigningBasketResponse> createSigningBasketResponseOptional = xs2aSigningBasketService.createSigningBasket(request, consentsAndPaymentsResponse, psuData, tppInfo);

        if (createSigningBasketResponseOptional.isEmpty()) {
            return ResponseObject.<CreateSigningBasketResponse>builder()
                       .fail(ErrorType.SB_400, of(MessageErrorCode.RESOURCE_UNKNOWN_400))
                       .build();
        }

        Xs2aCreateSigningBasketResponse xs2aCreateSigningBasketResponse = createSigningBasketResponseOptional.get();
        SpiContextData contextData = spiContextDataProvider.provide(psuData, tppInfo);
        InitialSpiAspspConsentDataProvider aspspConsentDataProvider = aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider();
        CoreSigningBasket coreSigningBasket = xs2aCreateSigningBasketResponse.getSigningBasket();
        SpiSigningBasket spiSigningBasket = spiSigningBasketMapper.mapToSpiSigningBasket(coreSigningBasket);

        SpiResponse<SpiInitiateSigningBasketResponse> spiResponse = signingBasketSpi.initiateSigningBasket(contextData, spiSigningBasket, aspspConsentDataProvider);

        String encryptedBasketId = xs2aCreateSigningBasketResponse.getSigningBasketId();
        aspspConsentDataProvider.saveWith(encryptedBasketId);

        if (spiResponse.hasError()) {
            xs2aSigningBasketService.updateSigningBasketStatus(encryptedBasketId, SigningBasketTransactionStatus.RJCT);
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.SB);
            log.info("SigningBasket-ID: [{}]. Create signing basket failed. Signing basket rejected. Couldn't initiate signing basket at SPI level: {}",
                     encryptedBasketId, errorHolder);
            return ResponseObject.<CreateSigningBasketResponse>builder()
                       .fail(new MessageError(errorHolder))
                       .build();
        }

        SpiInitiateSigningBasketResponse spiInitiateSigningBasketResponse = spiResponse.getPayload();
        boolean multilevelScaRequired = spiInitiateSigningBasketResponse.isMultilevelScaRequired();

        if (multilevelScaRequired) {
            xs2aSigningBasketService.updateMultilevelScaRequired(encryptedBasketId, multilevelScaRequired);
        }

        CreateSigningBasketResponse createSigningBasketResponse = new CreateSigningBasketResponse(
            getTransactionStatus(spiInitiateSigningBasketResponse.getTransactionStatus()),
            encryptedBasketId,
            spiToXs2aAuthenticationObjectMapper.toAuthenticationObjectList(spiInitiateSigningBasketResponse.getScaMethods()),
            spiToXs2aAuthenticationObjectMapper.toAuthenticationObject(spiInitiateSigningBasketResponse.getChosenScaMethod()),
            spiToXs2aChallengeDataMapper.toChallengeData(spiInitiateSigningBasketResponse.getChallengeData()),
            multilevelScaRequired,
            spiInitiateSigningBasketResponse.getPsuMessage(),
            xs2aCreateSigningBasketResponse.getTppNotificationContentPreferred()
        );

        return ResponseObject.<CreateSigningBasketResponse>builder()
                   .body(createSigningBasketResponse)
                   .build();
    }

    private String getTransactionStatus(SpiSigningBasketTransactionStatus transactionStatus) {
        return Optional.ofNullable(transactionStatus)
                   .map(Enum::name)
                   .orElse(null);
    }
}
