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

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.domain.signing_basket.SigningBasketReq;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.BusinessValidator;
import de.adorsys.psd2.xs2a.service.validator.PsuDataInInitialRequestValidator;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.signing_basket.dto.CreateSigningBasketRequestObject;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR;

/**
 * Validator to be used for validating create signing basket request according to some business rules
 */
@Component
@RequiredArgsConstructor
public class CreateSigningBasketRequestValidator implements BusinessValidator<CreateSigningBasketRequestObject> {
    private final AspspProfileServiceWrapper aspspProfileService;
    private final PsuDataInInitialRequestValidator psuDataInInitialRequestValidator;

    @NotNull
    @Override
    public ValidationResult validate(@NotNull CreateSigningBasketRequestObject requestObject) {
        ValidationResult psuDataValidationResult = psuDataInInitialRequestValidator.validate(requestObject.getPsuIdData());
        if (psuDataValidationResult.isNotValid()) {
            return psuDataValidationResult;
        }

        ValidationResult signingBasketMaxEntriesValidationResult = validateSigningBasketMaxEntries(requestObject.getSigningBasketReq());
        if (signingBasketMaxEntriesValidationResult.isNotValid()) {
            return signingBasketMaxEntriesValidationResult;
        }

        return ValidationResult.valid();
    }

    private ValidationResult validateSigningBasketMaxEntries(SigningBasketReq request) {
        int max = aspspProfileService.getSigningBasketMaxEntries();
        int size = Stream.of(request.getConsentIds(), request.getPaymentIds())
                       .filter(Objects::nonNull)
                       .map(List::size)
                       .mapToInt(e -> e)
                       .sum();

        if (size > max) {
            return ValidationResult.invalid(ErrorType.SB_400, TppMessageInformation.buildWithCustomError(FORMAT_ERROR, "Number of entries in Signing Basket should not exceed more than " + max));
        }

        return ValidationResult.valid();
    }
}
