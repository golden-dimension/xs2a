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

package de.adorsys.psd2.xs2a.service.validator.pis.authorisation.cancellation;

import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationServiceType;
import de.adorsys.psd2.xs2a.service.validator.PisEndpointAccessCheckerService;
import de.adorsys.psd2.xs2a.service.validator.PisPsuDataUpdateAuthorisationCheckerValidator;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationStageCheckValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.AbstractUpdatePisPsuDataValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.PisAuthorisationStatusValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.PisAuthorisationValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.UpdatePisPsuDataPO;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.initiation.UpdatePaymentPsuDataPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationServiceType.PIS_CANCELLATION;

/**
 * Validator to be used for validating create PIS cancellation authorisation request according to some business rules
 */
@Slf4j
@Component
public class UpdatePisCancellationPsuDataValidator extends AbstractUpdatePisPsuDataValidator<UpdatePaymentPsuDataPO> {

    public UpdatePisCancellationPsuDataValidator(PisEndpointAccessCheckerService pisEndpointAccessCheckerService,
                                                 PisAuthorisationValidator pisAuthorisationValidator,
                                                 PisAuthorisationStatusValidator pisAuthorisationStatusValidator,
                                                 PisPsuDataUpdateAuthorisationCheckerValidator pisPsuDataUpdateAuthorisationCheckerValidator,
                                                 AuthorisationStageCheckValidator authorisationStageCheckValidator) {
        super(pisEndpointAccessCheckerService, pisAuthorisationValidator,
              pisAuthorisationStatusValidator, pisPsuDataUpdateAuthorisationCheckerValidator,
              authorisationStageCheckValidator);
    }

    @Override
    protected ValidationResult validateTransactionStatus(UpdatePisPsuDataPO paymentObject) {
        return ValidationResult.valid();
    }

    @Override
    protected AuthorisationServiceType getAuthorisationServiceType() {
        return PIS_CANCELLATION;
    }
}
