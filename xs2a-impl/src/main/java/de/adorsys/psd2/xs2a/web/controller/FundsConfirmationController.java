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

package de.adorsys.psd2.xs2a.web.controller;

import de.adorsys.psd2.api.FundsConfirmationApi;
import de.adorsys.psd2.model.ConfirmationOfFunds;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.fund.FundsConfirmationResponse;
import de.adorsys.psd2.xs2a.service.FundsConfirmationService;
import de.adorsys.psd2.xs2a.service.mapper.FundsConfirmationModelMapper;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@SuppressWarnings("unchecked") // This class implements autogenerated interface without proper return values generated
@Slf4j
@RestController
@AllArgsConstructor
public class FundsConfirmationController implements FundsConfirmationApi {
    private final ResponseMapper responseMapper;
    private final ResponseErrorMapper responseErrorMapper;
    private final FundsConfirmationService fundsConfirmationService;
    private final FundsConfirmationModelMapper fundsConfirmationModelMapper;

    @Override
    public ResponseEntity checkAvailabilityOfFunds(ConfirmationOfFunds body, UUID xRequestID, String authorization, String consentID, String digest, String signature, byte[] tpPSignatureCertificate) {
        ResponseObject<FundsConfirmationResponse> responseObject = fundsConfirmationService.fundsConfirmation(fundsConfirmationModelMapper.mapToFundsConfirmationRequest(body, consentID));

        return responseObject.hasError()
                   ? responseErrorMapper.generateErrorResponse(responseObject.getError())
                   : responseMapper.ok(responseObject, fundsConfirmationModelMapper::mapToInlineResponse2003);
    }
}
