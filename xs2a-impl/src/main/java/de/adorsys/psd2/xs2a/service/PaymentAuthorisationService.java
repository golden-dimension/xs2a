/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.PaymentScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthorisationSubResources;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.PaymentAuthorisationParameters;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;

public interface PaymentAuthorisationService {
    ResponseObject<AuthorisationResponse> createPisAuthorisation(Xs2aCreatePisAuthorisationRequest request);

    ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> updatePisCommonPaymentPsuData(PaymentAuthorisationParameters request);

    ResponseObject<Xs2aAuthorisationSubResources> getPaymentInitiationAuthorisations(String paymentId, String paymentProduct, PaymentType paymentType);

    ResponseObject<PaymentScaStatus> getPaymentInitiationAuthorisationScaStatus(String paymentId, String authorisationId,
                                                                                PaymentType paymentType, String paymentProduct);
}
