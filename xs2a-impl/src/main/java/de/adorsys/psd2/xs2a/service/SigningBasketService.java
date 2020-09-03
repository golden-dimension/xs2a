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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.consent.api.signingbasket.CmsSigningBasketConsentsAndPaymentsResponse;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.sb.CreateSigningBasketRequest;
import de.adorsys.psd2.xs2a.domain.signing_basket.CreateSigningBasketResponse;
import de.adorsys.psd2.xs2a.domain.signing_basket.SigningBasketReq;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class SigningBasketService {
    private final SigningBasketValidationServiceImpl signingBasketValidationService;

    public ResponseObject<CreateSigningBasketResponse> createSigningBasket(SigningBasketReq signingBasketReq, PsuIdData psuIdData) {

        CreateSigningBasketRequest createSigningBasketRequest = new CreateSigningBasketRequest(signingBasketReq.getPaymentIds(), signingBasketReq.getConsentIds(), null, null, null);
        CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse = new CmsSigningBasketConsentsAndPaymentsResponse(Collections.emptyList(), Collections.emptyList());
        ValidationResult validationResult = signingBasketValidationService.validateSigningBasketOnCreate(createSigningBasketRequest, psuIdData, cmsSigningBasketConsentsAndPaymentsResponse);

        if (validationResult.isNotValid()) {
            log.info("Create signing basket - validation failed: {}", validationResult.getMessageError());
            return ResponseObject.<CreateSigningBasketResponse>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }


        return ResponseObject.<CreateSigningBasketResponse>builder().build();
    }
}
