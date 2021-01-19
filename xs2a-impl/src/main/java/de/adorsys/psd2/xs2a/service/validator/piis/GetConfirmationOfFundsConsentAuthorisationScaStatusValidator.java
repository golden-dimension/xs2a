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

package de.adorsys.psd2.xs2a.service.validator.piis;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.OauthPiisConsentValidator;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

/**
 * Validator to be used for validating get consent authorisation sca status request according to some business rules
 */
@Component
@RequiredArgsConstructor
public class GetConfirmationOfFundsConsentAuthorisationScaStatusValidator extends AbstractConfirmationOfFundsConsentTppValidator<GetConfirmationOfFundsConsentAuthorisationScaStatusPO> {
    private final ConfirmationOfFundsAuthorisationValidator confirmationOfFundsAuthorisationValidator;
    private final OauthPiisConsentValidator oauthPiisConsentValidator;

    /**
     * Validates get consent authorisation sca status request
     *
     * @param consentObject consent information object
     * @return valid result if the consent is valid, invalid result with appropriate error otherwise
     */
    @NotNull
    @Override
    protected ValidationResult executeBusinessValidation(GetConfirmationOfFundsConsentAuthorisationScaStatusPO consentObject) {
        PiisConsent piisConsent = consentObject.getPiisConsent();
        String authorisationId = consentObject.getAuthorisationId();

        ValidationResult authorisationValidationResult = confirmationOfFundsAuthorisationValidator.validate(authorisationId, piisConsent);
        if (authorisationValidationResult.isNotValid()) {
            return authorisationValidationResult;
        }

        ValidationResult authAuthorisationValidationResult = oauthPiisConsentValidator.validate(piisConsent);
        if (authAuthorisationValidationResult.isNotValid()) {
            return authAuthorisationValidationResult;
        }

        return ValidationResult.valid();
    }
}
