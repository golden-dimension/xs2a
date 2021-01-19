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

package de.adorsys.psd2.xs2a.service.validator.pis.payment;

import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.validator.OauthPaymentValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.AbstractPisValidator;
import org.springframework.stereotype.Component;

/**
 * Validator to be used for validating get payment status by ID request according to some business rules
 */
@Component
public class GetPaymentStatusByIdValidator extends AbstractPisValidator<GetPaymentStatusByIdPO> {
    private final RequestProviderService requestProviderService;
    private final OauthPaymentValidator oauthPaymentValidator;
    private final TransactionStatusAcceptHeaderValidator transactionStatusAcceptHeaderValidator;

    public GetPaymentStatusByIdValidator(RequestProviderService requestProviderService,
                                         OauthPaymentValidator oauthPaymentValidator,
                                         TransactionStatusAcceptHeaderValidator transactionStatusAcceptHeaderValidator) {
        this.requestProviderService = requestProviderService;
        this.oauthPaymentValidator = oauthPaymentValidator;
        this.transactionStatusAcceptHeaderValidator = transactionStatusAcceptHeaderValidator;
    }

    /**
     * Validates get payment status by ID request by checking whether:
     * <ul>
     * <li>accept header in the request is supported by the ASPSP</li>
     * <li>oauth request is valid for given payment</li>
     * </ul>
     *
     * @param paymentObject payment information object
     * @return valid result if the payment is valid, invalid result with appropriate error otherwise
     */
    @Override
    protected ValidationResult executeBusinessValidation(GetPaymentStatusByIdPO paymentObject) {
        ValidationResult transactionStatusFormatValidationResult = transactionStatusAcceptHeaderValidator.validate(requestProviderService.getAcceptHeader());
        if (transactionStatusFormatValidationResult.isNotValid()) {
            return transactionStatusFormatValidationResult;
        }

        ValidationResult oauthPaymentValidationResult = oauthPaymentValidator.validate(paymentObject.getPisCommonPaymentResponse());

        if (oauthPaymentValidationResult.isNotValid()) {
            return oauthPaymentValidationResult;
        }

        return ValidationResult.valid();
    }
}
