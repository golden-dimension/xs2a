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

package de.adorsys.psd2.stub.impl;

import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorizationCodeResult;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAvailableScaMethodsResponse;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiPsuAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAuthenticationObject;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiChallengeData;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiSigningBasketTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.sb.SpiInitiateSigningBasketResponse;
import de.adorsys.psd2.xs2a.spi.domain.sb.SpiSigningBasket;
import de.adorsys.psd2.xs2a.spi.service.SigningBasketSpi;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class SigningBasketSpiMockImpl implements SigningBasketSpi {
    @Override
    public SpiResponse<SpiInitiateSigningBasketResponse> initiateSigningBasket(@NotNull SpiContextData contextData, SpiSigningBasket signingBasket, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        log.info("SigningBasketSpi#initiateSigningBasket: contextData {}, signingBasket-id {}", contextData, signingBasket.getBasketId());

        SpiInitiateSigningBasketResponse spiInitiateSigningBasketResponse = new SpiInitiateSigningBasketResponse(
            SpiSigningBasketTransactionStatus.RCVD,
            signingBasket.getBasketId(),
            getAuthenticationObjects(),
            null,
            getSpiChallengeData(),
            false,
            "psu message",
            Collections.emptyList()
        );
        return SpiResponse.<SpiInitiateSigningBasketResponse>builder()
                   .payload(spiInitiateSigningBasketResponse)
                   .build();
    }

    @Override
    public SpiResponse<SpiPsuAuthorisationResponse> authorisePsu(@NotNull SpiContextData contextData, @NotNull String authorisationId, @NotNull SpiPsuData psuLoginData, String password, SpiSigningBasket businessObject, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return null;
    }

    @Override
    public SpiResponse<SpiAvailableScaMethodsResponse> requestAvailableScaMethods(@NotNull SpiContextData contextData, SpiSigningBasket businessObject, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {

        return null;
    }

    @Override
    public @NotNull SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(@NotNull SpiContextData contextData, @NotNull String authenticationMethodId, @NotNull SpiSigningBasket businessObject, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return null;
    }

    @Override
    public @NotNull SpiResponse<Boolean> requestTrustedBeneficiaryFlag(@NotNull SpiContextData contextData, @NotNull SpiSigningBasket businessObject, @NotNull String authorisationId, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return null;
    }

    private List<SpiAuthenticationObject> getAuthenticationObjects() {
        List<SpiAuthenticationObject> spiScaMethods = new ArrayList<>();
        SpiAuthenticationObject sms = new SpiAuthenticationObject();
        sms.setAuthenticationType("SMS_OTP");
        sms.setAuthenticationMethodId("sms");
        sms.setName("some-sms-name");
        spiScaMethods.add(sms);
        SpiAuthenticationObject push = new SpiAuthenticationObject();
        push.setAuthenticationType("PUSH_OTP");
        push.setAuthenticationMethodId("push");
        push.setDecoupled(true);
        spiScaMethods.add(push);
        return spiScaMethods;
    }

    private SpiChallengeData getSpiChallengeData() {
        return new SpiChallengeData(null, Collections.singletonList("some data"), "some link", 100, null, "info");
    }
}
