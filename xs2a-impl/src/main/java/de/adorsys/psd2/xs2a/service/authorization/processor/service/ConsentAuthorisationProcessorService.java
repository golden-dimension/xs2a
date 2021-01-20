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

package de.adorsys.psd2.xs2a.service.authorization.processor.service;

import de.adorsys.psd2.core.data.Consent;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.AuthorisationProcessorResponse;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAvailableScaMethodsResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiPsuAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiVerifyScaAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Optional;

public abstract class ConsentAuthorisationProcessorService<T extends Consent> extends BaseAuthorisationProcessorService {
    private static final String CONSENT_NOT_FOUND_LOG_MESSAGE = "Apply authorisation when update consent PSU data has failed. Consent not found by id.";

    private final Xs2aAuthorisationService authorisationService;
    private final SpiContextDataProvider spiContextDataProvider;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final SpiErrorMapper spiErrorMapper;
    private final Xs2aToSpiPsuDataMapper psuDataMapper;

    protected ConsentAuthorisationProcessorService(Xs2aAuthorisationService authorisationService,
                                                SpiContextDataProvider spiContextDataProvider,
                                                SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory,
                                                SpiErrorMapper spiErrorMapper, Xs2aToSpiPsuDataMapper psuDataMapper) {
        this.authorisationService = authorisationService;
        this.spiContextDataProvider = spiContextDataProvider;
        this.aspspConsentDataProviderFactory = aspspConsentDataProviderFactory;
        this.spiErrorMapper = spiErrorMapper;
        this.psuDataMapper = psuDataMapper;
    }

    @Override
    public AuthorisationProcessorResponse doScaReceived(AuthorisationProcessorRequest authorisationProcessorRequest) {
        return doScaPsuIdentified(authorisationProcessorRequest);
    }

    @Override
    public AuthorisationProcessorResponse doScaPsuIdentified(AuthorisationProcessorRequest authorisationProcessorRequest) {
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        return request.isUpdatePsuIdentification()
                   ? applyIdentification(authorisationProcessorRequest)
                   : applyAuthorisation(authorisationProcessorRequest);
    }

    @Override
    public AuthorisationProcessorResponse doScaPsuAuthenticated(AuthorisationProcessorRequest authorisationProcessorRequest) {
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        String consentId = request.getBusinessObjectId();
        String authorisationId = request.getAuthorisationId();

        Optional<T> consentOptional = getConsentByIdFromCms(consentId);
        if (consentOptional.isEmpty()) {
            ErrorHolder errorHolder = ErrorHolder.builder(getErrorType400())
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400))
                                          .build();
            writeErrorLog(authorisationProcessorRequest, request.getPsuData(), errorHolder, CONSENT_NOT_FOUND_LOG_MESSAGE);
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, request.getPsuData());
        }

        PsuIdData psuData = extractPsuIdData(request, authorisationProcessorRequest.getAuthorisation());
        T consent = consentOptional.get();

        String authenticationMethodId = request.getAuthenticationMethodId();
        if (isDecoupledApproach(request.getAuthorisationId(), authenticationMethodId)) {
            authorisationService.updateScaApproach(request.getAuthorisationId(), ScaApproach.DECOUPLED);
            return proceedDecoupledApproach(request.getBusinessObjectId(), request.getAuthorisationId(), consent, authenticationMethodId, psuData);
        }

        return proceedEmbeddedApproach(authorisationProcessorRequest, authenticationMethodId, consent, psuData);
    }

    @Override
    public AuthorisationProcessorResponse doScaMethodSelected(AuthorisationProcessorRequest authorisationProcessorRequest) {
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        String consentId = request.getBusinessObjectId();
        String authorisationId = request.getAuthorisationId();
        Optional<T> consentOptional = getConsentByIdFromCms(consentId);

        if (consentOptional.isEmpty()) {
            ErrorHolder errorHolder = ErrorHolder.builder(getErrorType400())
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400))
                                          .build();
            writeErrorLog(authorisationProcessorRequest, request.getPsuData(), errorHolder, CONSENT_NOT_FOUND_LOG_MESSAGE);
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, request.getPsuData());
        }
        T consent = consentOptional.get();

        PsuIdData psuData = extractPsuIdData(request, authorisationProcessorRequest.getAuthorisation());

        SpiResponse<SpiVerifyScaAuthorisationResponse> spiResponse = verifyScaAuthorisation(spiContextDataProvider.provideWithPsuIdData(psuData),
                                                                                            request,
                                                                                            psuData,
                                                                                            consent,
                                                                                            aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, getServiceType());
            writeErrorLog(authorisationProcessorRequest, request.getPsuData(), errorHolder, "Verify SCA authorisation failed when update PSU data.");

            SpiVerifyScaAuthorisationResponse spiAuthorisationResponse = spiResponse.getPayload();
            return getSpiErrorResponse(authorisationProcessorRequest, consentId, authorisationId, psuData, errorHolder, spiAuthorisationResponse);
        }

        ConsentStatus responseConsentStatus = spiResponse.getPayload().getConsentStatus();

        if (ConsentStatus.PARTIALLY_AUTHORISED == responseConsentStatus && !consent.isMultilevelScaRequired()) {
            updateMultilevelScaRequired(consentId, true);
        }

        if (consent.getConsentStatus() != responseConsentStatus) {
            updateConsentStatus(consentId, responseConsentStatus);
        }

        findAndTerminateOldConsentsByNewConsentId(consentId);

        return new UpdateConsentPsuDataResponse(ScaStatus.FINALISED, consentId, request.getAuthorisationId(), psuData);
    }

    private AuthorisationProcessorResponse getSpiErrorResponse(AuthorisationProcessorRequest authorisationProcessorRequest, String consentId, String authorisationId, PsuIdData psuData, ErrorHolder errorHolder, SpiVerifyScaAuthorisationResponse spiAuthorisationResponse) {
        if (spiAuthorisationResponse != null && spiAuthorisationResponse.getSpiAuthorisationStatus() == SpiAuthorisationStatus.ATTEMPT_FAILURE) {
            return new UpdateConsentPsuDataResponse(authorisationProcessorRequest.getScaStatus(), errorHolder, consentId, authorisationId, psuData);
        }

        Optional<MessageErrorCode> first = errorHolder.getFirstErrorCode();
        if (first.isPresent() && first.get() == MessageErrorCode.PSU_CREDENTIALS_INVALID) {
            authorisationService.updateAuthorisationStatus(authorisationId, ScaStatus.FAILED);
        }
        return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, psuData);
    }

    @Override
    public AuthorisationProcessorResponse doScaFinalised(AuthorisationProcessorRequest authorisationProcessorRequest) {
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        return new UpdateConsentPsuDataResponse(ScaStatus.FINALISED, request.getBusinessObjectId(), request.getAuthorisationId(), request.getPsuData());
    }

    private UpdateConsentPsuDataResponse proceedEmbeddedApproach(AuthorisationProcessorRequest authorisationProcessorRequest, String authenticationMethodId, T consent, PsuIdData psuData) {
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        SpiResponse<SpiAuthorizationCodeResult> spiResponse = requestAuthorisationCode(spiContextDataProvider.provideWithPsuIdData(psuData),
                                                                                       authenticationMethodId,
                                                                                       consent,
                                                                                       aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(request.getBusinessObjectId()));


        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, getServiceType());
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Proceed embedded approach when performs authorisation depending on selected SCA method has failed.");

            Optional<MessageErrorCode> first = errorHolder.getFirstErrorCode();
            if (first.isPresent() && first.get() == MessageErrorCode.PSU_CREDENTIALS_INVALID) {
                authorisationService.updateAuthorisationStatus(request.getAuthorisationId(), ScaStatus.FAILED);
            }
            return new UpdateConsentPsuDataResponse(errorHolder, request.getBusinessObjectId(), request.getAuthorisationId(), psuData);
        }

        SpiAuthorizationCodeResult authorizationCodeResult = spiResponse.getPayload();

        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse(ScaStatus.SCAMETHODSELECTED, request.getBusinessObjectId(), request.getAuthorisationId(), psuData);
        response.setChosenScaMethod(authorizationCodeResult.getSelectedScaMethod());
        response.setChallengeData(authorizationCodeResult.getChallengeData());
        return response;
    }

    private UpdateConsentPsuDataResponse applyAuthorisation(AuthorisationProcessorRequest authorisationProcessorRequest) {
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        String consentId = request.getBusinessObjectId();
        String authorisationId = request.getAuthorisationId();
        Optional<T> consentOptional = getConsentByIdFromCms(consentId);

        if (consentOptional.isEmpty()) {
            ErrorHolder errorHolder = ErrorHolder.builder(getErrorType400())
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400))
                                          .build();
            writeErrorLog(authorisationProcessorRequest, request.getPsuData(), errorHolder, CONSENT_NOT_FOUND_LOG_MESSAGE);
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, request.getPsuData());
        }

        Authorisation authorisation = authorisationProcessorRequest.getAuthorisation();
        PsuIdData psuData = extractPsuIdData(request, authorisation);
        T consent = consentOptional.get();
        SpiResponse<SpiPsuAuthorisationResponse> authorisationStatusSpiResponse = authorisePsu(spiContextDataProvider.provideWithPsuIdData(psuData),
                                                                                               authorisation.getAuthorisationId(),
                                                                                               psuDataMapper.mapToSpiPsuData(psuData),
                                                                                               request.getPassword(),
                                                                                               consent,
                                                                                               aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));

        if (authorisationStatusSpiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(authorisationStatusSpiResponse, getServiceType());
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Authorise PSU when apply authorisation has failed.");
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, psuData);
        }

        SpiPsuAuthorisationResponse spiAuthorisationResponse = authorisationStatusSpiResponse.getPayload();
        if (spiAuthorisationResponse.getSpiAuthorisationStatus() == SpiAuthorisationStatus.ATTEMPT_FAILURE) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(authorisationStatusSpiResponse, getServiceType());
            return new UpdateConsentPsuDataResponse(authorisationProcessorRequest.getScaStatus(), errorHolder, consentId, authorisationId, psuData);
        }

        if (spiAuthorisationResponse.getSpiAuthorisationStatus() == SpiAuthorisationStatus.FAILURE) {
            ErrorHolder errorHolder = ErrorHolder.builder(getErrorType401())
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.PSU_CREDENTIALS_INVALID))
                                          .build();
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Authorise PSU when apply authorisation has failed. PSU credentials invalid.");
            authorisationService.updateAuthorisationStatus(authorisationId, ScaStatus.FAILED);
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, psuData);
        }

        if (isOneFactorAuthorisation(consent)) {
            updateConsentStatus(consentId, ConsentStatus.VALID);
            return new UpdateConsentPsuDataResponse(ScaStatus.FINALISED, consentId, authorisationId, psuData);
        }

        if (authorisation.getChosenScaApproach() == ScaApproach.DECOUPLED) {
            return proceedDecoupledApproach(consentId, authorisationId, consent, psuData);
        }

        return requestAvailableScaMethods(authorisationProcessorRequest, consentId, authorisationId, psuData, consent);
    }

    private UpdateConsentPsuDataResponse requestAvailableScaMethods(AuthorisationProcessorRequest authorisationProcessorRequest, String consentId, String authorisationId, PsuIdData psuData, T consent) {
        SpiResponse<SpiAvailableScaMethodsResponse> spiResponse = requestAvailableScaMethods(spiContextDataProvider.provideWithPsuIdData(psuData),
                                                                                             consent,
                                                                                             aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, getServiceType());
            writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Request available SCA methods when apply authorisation has failed.");
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, psuData);
        }

        List<AuthenticationObject> availableScaMethods = spiResponse.getPayload().getAvailableScaMethods();
        if (CollectionUtils.isNotEmpty(availableScaMethods)) {
            authorisationService.saveAuthenticationMethods(authorisationId, availableScaMethods);

            return getScaMethodsResponse(authorisationProcessorRequest, consentId, authorisationId, psuData, consent, availableScaMethods);
        }

        ErrorHolder errorHolder = ErrorHolder.builder(getErrorType400())
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.SCA_METHOD_UNKNOWN))
                                      .build();
        writeErrorLog(authorisationProcessorRequest, psuData, errorHolder, "Apply authorisation has failed. Consent was rejected because PSU has no available SCA methods.");
        updateConsentStatus(consentId, ConsentStatus.REJECTED);
        authorisationService.updateAuthorisationStatus(authorisationId, ScaStatus.FAILED);
        return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId, psuData);
    }

    private UpdateConsentPsuDataResponse getScaMethodsResponse(AuthorisationProcessorRequest authorisationProcessorRequest, String consentId, String authorisationId, PsuIdData psuData, T consent, List<AuthenticationObject> availableScaMethods) {
        if (isMultipleScaMethods(availableScaMethods)) {
            return createResponseForMultipleAvailableMethods(availableScaMethods, authorisationId, consentId, psuData);
        } else {
            return createResponseForOneAvailableMethod(authorisationProcessorRequest, consent, availableScaMethods.get(0), psuData);
        }
    }

    private UpdateConsentPsuDataResponse createResponseForMultipleAvailableMethods(List<AuthenticationObject> availableScaMethods,
                                                                                   String authorisationId,
                                                                                   String consentId,
                                                                                   PsuIdData psuIdData) {
        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse(ScaStatus.PSUAUTHENTICATED, consentId, authorisationId, psuIdData);
        response.setAvailableScaMethods(availableScaMethods);
        return response;
    }

    private UpdateConsentPsuDataResponse createResponseForOneAvailableMethod(AuthorisationProcessorRequest authorisationProcessorRequest, T consent, AuthenticationObject scaMethod, PsuIdData psuData) {
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        if (scaMethod.isDecoupled()) {
            authorisationService.updateScaApproach(request.getAuthorisationId(), ScaApproach.DECOUPLED);
            return proceedDecoupledApproach(request.getBusinessObjectId(), request.getAuthorisationId(), consent, scaMethod.getAuthenticationMethodId(), psuData);
        }

        return proceedEmbeddedApproach(authorisationProcessorRequest, scaMethod.getAuthenticationMethodId(), consent, psuData);
    }

    private UpdateConsentPsuDataResponse applyIdentification(AuthorisationProcessorRequest authorisationProcessorRequest) {
        UpdateAuthorisationRequest request = authorisationProcessorRequest.getUpdateAuthorisationRequest();
        if (!isPsuExist(request.getPsuData())) {
            ErrorHolder errorHolder = ErrorHolder.builder(getErrorType400())
                                          .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_NO_PSU))
                                          .build();
            writeErrorLog(authorisationProcessorRequest, request.getPsuData(), errorHolder, "Apply identification when update consent PSU data has failed. No PSU data available in request.");
            return new UpdateConsentPsuDataResponse(errorHolder, request.getBusinessObjectId(), request.getAuthorisationId(), request.getPsuData());
        }

        return new UpdateConsentPsuDataResponse(ScaStatus.PSUIDENTIFIED, request.getBusinessObjectId(), request.getAuthorisationId(), request.getPsuData());
    }

    private boolean isDecoupledApproach(String authorisationId, String authenticationMethodId) {
        return authorisationService.isAuthenticationMethodDecoupled(authorisationId, authenticationMethodId);
    }


    abstract UpdateConsentPsuDataResponse proceedDecoupledApproach(String consentId, String authorisationId, T consent, PsuIdData psuData);

    abstract UpdateConsentPsuDataResponse proceedDecoupledApproach(String consentId, String authorisationId, T consent, String authenticationMethodId, PsuIdData psuData);

    abstract boolean isOneFactorAuthorisation(T consent);

    abstract SpiResponse<SpiAvailableScaMethodsResponse> requestAvailableScaMethods(SpiContextData spiContextData, T consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider);

    abstract SpiResponse<SpiPsuAuthorisationResponse> authorisePsu(SpiContextData spiContextData, String authorisationId, SpiPsuData spiPsuData, String password, T consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider);

    abstract void findAndTerminateOldConsentsByNewConsentId(String consentId);

    abstract void updateConsentStatus(String consentId, ConsentStatus responseConsentStatus);

    abstract void updateMultilevelScaRequired(String consentId, boolean multilevelScaRequired);

    abstract ServiceType getServiceType();

    abstract SpiResponse<SpiVerifyScaAuthorisationResponse> verifyScaAuthorisation(SpiContextData spiContextData, UpdateAuthorisationRequest request, PsuIdData psuData, T consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider);

    abstract Optional<T> getConsentByIdFromCms(String consentId);

    abstract ErrorType getErrorType400();

    abstract ErrorType getErrorType401();

    abstract SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(SpiContextData spiContextData, String authenticationMethodId, T consent, SpiAspspConsentDataProvider spiAspspConsentDataProvider);
}
