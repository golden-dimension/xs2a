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

package de.adorsys.psd2.xs2a.service.validator.signing_basket;

import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.signingbasket.CmsSigningBasketConsentsAndPaymentsResponse;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.sb.CreateSigningBasketRequest;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.BusinessValidator;
import de.adorsys.psd2.xs2a.service.validator.PsuDataInInitialRequestValidator;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.signing_basket.dto.CreateSigningBasketRequestObject;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;

/**
 * Validator to be used for validating create signing basket request according to some business rules
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class CreateSigningBasketRequestValidator implements BusinessValidator<CreateSigningBasketRequestObject> {
    private final AspspProfileServiceWrapper aspspProfileService;
    private final PsuDataInInitialRequestValidator psuDataInInitialRequestValidator;
    private final Xs2aAisConsentMapper aisConsentMapper;

    @NotNull
    @Override
    public ValidationResult validate(@NotNull CreateSigningBasketRequestObject requestObject) {
        ValidationResult signingBasketSupportedValidationResult = validateSigningBasketSupported();
        if (signingBasketSupportedValidationResult.isNotValid()) {
            return signingBasketSupportedValidationResult;
        }

        ValidationResult psuDataValidationResult = psuDataInInitialRequestValidator.validate(requestObject.getPsuIdData());
        if (psuDataValidationResult.isNotValid()) {
            return psuDataValidationResult;
        }

        CreateSigningBasketRequest createSigningBasketRequest = requestObject.getCreateSigningBasketRequest();

        ValidationResult emptyRequestValidationResult = validateSigningBasketOnEmptyRequest(createSigningBasketRequest);
        if (emptyRequestValidationResult.isNotValid()) {
            return emptyRequestValidationResult;
        }

        ValidationResult signingBasketMaxEntriesValidationResult = validateSigningBasketMaxEntries(createSigningBasketRequest);
        if (signingBasketMaxEntriesValidationResult.isNotValid()) {
            return signingBasketMaxEntriesValidationResult;
        }

        CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse = requestObject.getCmsSigningBasketConsentsAndPaymentsResponse();

        ValidationResult cmsSBResponseValidationResult = validateWithCmsSBResponse(createSigningBasketRequest, cmsSigningBasketConsentsAndPaymentsResponse);
        if (cmsSBResponseValidationResult.isNotValid()) {
            return cmsSBResponseValidationResult;
        }

        return ValidationResult.valid();
    }

    private ValidationResult validateWithCmsSBResponse(CreateSigningBasketRequest createSigningBasketRequest, CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse) {
        ValidationResult wrongIdsValidationResult = validateSigningBasketOnWrongIds(createSigningBasketRequest, cmsSigningBasketConsentsAndPaymentsResponse);
        if (wrongIdsValidationResult.isNotValid()) {
            return wrongIdsValidationResult;
        }

        ValidationResult bankOfferedConsentValidationResult = validateSigningBasketOnBankOfferedConsent(cmsSigningBasketConsentsAndPaymentsResponse);
        if (bankOfferedConsentValidationResult.isNotValid()) {
            return bankOfferedConsentValidationResult;
        }

        ValidationResult multilevelVariationsValidationResult = validateSigningBasketOnMultilevelVariations(cmsSigningBasketConsentsAndPaymentsResponse);
        if (multilevelVariationsValidationResult.isNotValid()) {
            return multilevelVariationsValidationResult;
        }

        ValidationResult signingBasketObjectsBlockedValidationResult = validateSigningBasketOnSigningBasketObjectsBlocked(cmsSigningBasketConsentsAndPaymentsResponse);
        if (signingBasketObjectsBlockedValidationResult.isNotValid()) {
            return signingBasketObjectsBlockedValidationResult;
        }

        ValidationResult partlyAuthorisedValidationResult = validateSigningBasketOnSigningBasketObjectsPartlyAuthorised(cmsSigningBasketConsentsAndPaymentsResponse);
        if (partlyAuthorisedValidationResult.isNotValid()) {
            return partlyAuthorisedValidationResult;
        }


        return ValidationResult.valid();
    }

    private ValidationResult validateSigningBasketOnSigningBasketObjectsBlocked(CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse) {
        Map<Boolean, List<SigningBasketObject>> partitionedSigningBasketObjects = getSigningBasketObjectStream(cmsSigningBasketConsentsAndPaymentsResponse)
                                                                                      .collect(Collectors.partitioningBy(SigningBasketObject::isBlocked));

        if (!partitionedSigningBasketObjects.get(true).isEmpty()) {
            log.error("SB has blocked objects: ");
            partitionedSigningBasketObjects.get(true).forEach(sbo -> log.error(sbo.toString()));
            return ValidationResult.invalid(ErrorType.SB_400, REFERENCE_MIX_INVALID);
        }

        return ValidationResult.valid();
    }

    private ValidationResult validateSigningBasketOnSigningBasketObjectsPartlyAuthorised(CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse) {
        Map<Boolean, List<SigningBasketObject>> partitionedSigningBasketObjects = getSigningBasketObjectStream(cmsSigningBasketConsentsAndPaymentsResponse)
                                                                                      .collect(Collectors.partitioningBy(SigningBasketObject::isPartiallyAuthorised));

        if (!partitionedSigningBasketObjects.get(true).isEmpty()) {
            log.error("SB has partially authorised objects: ");
            partitionedSigningBasketObjects.get(true).forEach(sbo -> log.error(sbo.toString()));
            return ValidationResult.invalid(ErrorType.SB_409, REFERENCE_STATUS_INVALID);
        }

        return ValidationResult.valid();
    }

    private ValidationResult validateSigningBasketOnEmptyRequest(CreateSigningBasketRequest createSigningBasketRequest) {
        if (getEntriesSize(createSigningBasketRequest) == 0) {
            log.error("SB is empty");
            return ValidationResult.invalid(ErrorType.SB_400, REFERENCE_MIX_INVALID);
        }

        return ValidationResult.valid();
    }

    private ValidationResult validateSigningBasketOnWrongIds(CreateSigningBasketRequest createSigningBasketRequest, CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse) {
        if (getEntriesSize(createSigningBasketRequest) != getEntriesSize(cmsSigningBasketConsentsAndPaymentsResponse)) {
            log.error("SB has wrong ids");
            return ValidationResult.invalid(ErrorType.SB_400, REFERENCE_MIX_INVALID);
        }

        return ValidationResult.valid();
    }

    private ValidationResult validateSigningBasketOnBankOfferedConsent(CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse) {
        boolean isBankOfferedConsentIncluded = Stream.ofNullable(cmsSigningBasketConsentsAndPaymentsResponse.getConsents())
                                                   .flatMap(Collection::stream)
                                                   .filter(cmsConsent -> cmsConsent.getConsentType() == ConsentType.AIS)
                                                   .map(aisConsentMapper::mapToAisConsent)
                                                   .anyMatch(AisConsent::isBankOfferedConsent);

        if (isBankOfferedConsentIncluded) {
            log.error("SB has bank-offered consent");
            return ValidationResult.invalid(ErrorType.SB_400, REFERENCE_MIX_INVALID);
        }

        return ValidationResult.valid();
    }

    private ValidationResult validateSigningBasketOnMultilevelVariations(CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse) {
        Map<Boolean, List<SigningBasketObject>> partitionedSigningBasketObjects = getSigningBasketObjectStream(cmsSigningBasketConsentsAndPaymentsResponse)
                                                                                      .collect(Collectors.partitioningBy(SigningBasketObject::isMultilevelScaRequired));

        if (!partitionedSigningBasketObjects.get(true).isEmpty() && !partitionedSigningBasketObjects.get(false).isEmpty()) {

            log.error("SB objects have multilevel inconsistency");
            log.error("SB objects with multilevel: ");
            partitionedSigningBasketObjects.get(true).forEach(sbo -> log.error(sbo.toString()));
            log.error("SB objects without multilevel: ");
            partitionedSigningBasketObjects.get(false).forEach(sbo -> log.error(sbo.toString()));

            return ValidationResult.invalid(ErrorType.SB_400, REFERENCE_MIX_INVALID);
        }

        return ValidationResult.valid();
    }

    private Stream<SigningBasketObject> getSigningBasketObjectStream(CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse) {
        return Stream.concat(
            Stream.of(cmsSigningBasketConsentsAndPaymentsResponse.getConsents())
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .map(this::mapToSigningBasketObject),
            Stream.of(cmsSigningBasketConsentsAndPaymentsResponse.getPayments())
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .map(this::mapToSigningBasketObject)
        );
    }

    private ValidationResult validateSigningBasketSupported() {
        if (!aspspProfileService.isSigningBasketSupported()) {
            return ValidationResult.invalid(ErrorType.SB_405, TppMessageInformation.buildWithCustomError(SERVICE_INVALID_405, "Signing basket is not supported by ASPSP"));
        }

        return ValidationResult.valid();
    }

    private ValidationResult validateSigningBasketMaxEntries(CreateSigningBasketRequest createSigningBasketRequest) {
        int max = aspspProfileService.getSigningBasketMaxEntries();
        int size = getEntriesSize(createSigningBasketRequest);

        if (size > max) {
            return ValidationResult.invalid(ErrorType.SB_400, TppMessageInformation.buildWithCustomError(FORMAT_ERROR, "Number of entries in Signing Basket should not exceed more than " + max));
        }

        return ValidationResult.valid();
    }

    private int getEntriesSize(CreateSigningBasketRequest request) {
        return getEntriesSize(request.getConsentIds(), request.getPaymentIds());
    }

    private int getEntriesSize(CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse) {
        return getEntriesSize(cmsSigningBasketConsentsAndPaymentsResponse.getConsents(), cmsSigningBasketConsentsAndPaymentsResponse.getPayments());
    }

    private int getEntriesSize(List... objects) {
        return Stream.of(objects)
                   .filter(Objects::nonNull)
                   .map(List::size)
                   .mapToInt(e -> e)
                   .sum();
    }

    @Data
    private class SigningBasketObject {
        private final SBObjectType SBObjectType;
        private final String id;
        private final boolean multilevelScaRequired;
        private final boolean isPartiallyAuthorised;
        private final boolean isBlocked;
    }

    private enum SBObjectType {
        CONSENT, PAYMENT;
    }

    private SigningBasketObject mapToSigningBasketObject(CmsConsent consent) {
        return new SigningBasketObject(SBObjectType.CONSENT, consent.getId(), consent.isMultilevelScaRequired(), isAuthorised(consent), consent.isSigningBasketBlocked());
    }

    private boolean isAuthorised(CmsConsent consent) {
        if (consent.getConsentStatus().isFinalisedStatus()) {
            return true;
        }

        return isFinalisedAuthorisationPresent(consent.getAuthorisations());
    }

    private SigningBasketObject mapToSigningBasketObject(PisCommonPaymentResponse payment) {
        return new SigningBasketObject(SBObjectType.PAYMENT, payment.getExternalId(), payment.isMultilevelScaRequired(), isAuthorised(payment), payment.isSigningBasketBlocked());
    }

    private boolean isAuthorised(PisCommonPaymentResponse payment) {
        if (payment.getTransactionStatus().isFinalisedStatus()) {
            return true;
        }

        return isFinalisedAuthorisationPresent(payment.getAuthorisations());
    }

    private boolean isFinalisedAuthorisationPresent(List<Authorisation> authorisations) {
        return Stream.ofNullable(authorisations)
                   .flatMap(Collection::stream)
                   .map(Authorisation::getScaStatus)
                   .anyMatch(status -> status == ScaStatus.FINALISED);
    }
}
