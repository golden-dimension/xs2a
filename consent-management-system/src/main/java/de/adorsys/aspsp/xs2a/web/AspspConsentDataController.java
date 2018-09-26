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

package de.adorsys.aspsp.xs2a.web;

import de.adorsys.aspsp.xs2a.consent.api.UpdateConsentAspspDataRequest;
import de.adorsys.aspsp.xs2a.consent.api.pis.PisConsentAspspDataResponse;
import de.adorsys.aspsp.xs2a.consent.api.pis.proto.CreatePisConsentResponse;
import de.adorsys.aspsp.xs2a.service.PisConsentService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/pis")
@Api(value = "api/v1/pis/consent", tags = "PIS, Aspsp Consent Data", description = "Provides access to consent management system for AspspDataConsent")
public class AspspConsentDataController {
    private final PisConsentService pisConsentService;

    @GetMapping(path = "/consent/{consent-id}/aspspConsentData")
    @ApiOperation(value = "Get aspsp consent data identified by given consent id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<PisConsentAspspDataResponse> getAspspConsentData(
        @ApiParam(name = "consent-id", value = "The payment consent identification assigned to the created payment consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId) {
        return pisConsentService.getAspspDataByConsentId(consentId)
                   .map(response -> new ResponseEntity<>(response, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PutMapping(path = "/consent/{consent-id}/aspspConsentData")
    @ApiOperation(value = "Update aspsp consent data identified by given consent id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<CreatePisConsentResponse> updateAspspConsentData(
        @ApiParam(name = "consent-id", value = "The payment consent identification assigned to the created payment consent.", example = "bf489af6-a2cb-4b75-b71d-d66d58b934d7")
        @PathVariable("consent-id") String consentId,
        @RequestBody UpdateConsentAspspDataRequest request) {
        return pisConsentService.updateConsentAspspData(consentId, request)
                   .map(consId -> new ResponseEntity<>(new CreatePisConsentResponse(consId), HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @GetMapping(path = "/payment/{payment-id}/aspspConsentData")
    @ApiOperation(value = "Get aspsp consent data identified by given payment id.")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK"),
        @ApiResponse(code = 404, message = "Not Found")})
    public ResponseEntity<PisConsentAspspDataResponse> getAspspConsentDataByPaymentId(
        @ApiParam(name = "payment-id", value = "The payment identification.", example = "32454656712432")
        @PathVariable("payment-id") String paymentId) {
        return pisConsentService.getAspspDataByPaymentId(paymentId)
                   .map(response -> new ResponseEntity<>(response, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }
}
