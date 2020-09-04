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

package de.adorsys.psd2.xs2a.web.aspect;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.sb.CreateSigningBasketRequest;
import de.adorsys.psd2.xs2a.domain.sb.CreateSigningBasketResponse;
import de.adorsys.psd2.xs2a.service.link.SigningBasketAspectService;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class SigningBasketAspect {
    private SigningBasketAspectService signingBasketAspectService;

    public SigningBasketAspect(SigningBasketAspectService signingBasketAspectService) {
        this.signingBasketAspectService = signingBasketAspectService;
    }

    @AfterReturning(pointcut = "execution(* de.adorsys.psd2.xs2a.service.sb.SigningBasketService.createSigningBasket(..)) && args( request, psuData, explicitPreferred)", returning = "result", argNames = "result,request,psuData,explicitPreferred")
    ResponseObject<CreateSigningBasketResponse> invokeCreateSigningBasketAspect(ResponseObject<CreateSigningBasketResponse> result, CreateSigningBasketRequest request, PsuIdData psuData, boolean explicitPreferred) {
        return signingBasketAspectService.invokeCreateSigningBasketAspect(result);
    }
}
