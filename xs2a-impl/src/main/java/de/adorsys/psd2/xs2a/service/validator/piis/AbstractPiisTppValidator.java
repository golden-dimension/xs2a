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

import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.validator.BusinessValidator;
import de.adorsys.psd2.xs2a.service.validator.TppInfoProvider;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppInfoValidator;
import org.jetbrains.annotations.NotNull;

/**
 * Common validator for validating TPP in consents and executing request-specific business validation afterwards.
 * Should be used for all PIIS-related requests after consent creation.
 *
 * @param <T> type of object to be checked
 */
public abstract class AbstractPiisTppValidator<T extends TppInfoProvider> implements BusinessValidator<T> {
    @NotNull
    @Override
    public ValidationResult validate(@NotNull T object) {
        TppInfo tppInfoInConsent = object.getTppInfo();
        ValidationResult tppValidationResult = getTppInfoValidator().validateTpp(tppInfoInConsent);
        if (tppValidationResult.isNotValid()) {
            return tppValidationResult;
        }

        return executeBusinessValidation(object);
    }

    /**
     * Executes request-specific business validation
     *
     * @param consentObject consent object to be validated
     * @return valid result if the object is valid, invalid result with appropriate error otherwise
     */
    @NotNull
    protected abstract ValidationResult executeBusinessValidation(T consentObject);

    /**
     * Returns appropriate TPP info validator for current request
     *
     * @return TPP info validator
     */
    @NotNull
    protected abstract TppInfoValidator getTppInfoValidator();
}
