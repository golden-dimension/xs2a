/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.authorization.ais;

import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthenticationObject;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationDecoupledScaResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommonDecoupledAisService {
    private final AisConsentSpi aisConsentSpi;
    private final RequestProviderService requestProviderService;
    private final SpiErrorMapper spiErrorMapper;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final SpiContextDataProvider spiContextDataProvider;
    private final Xs2aAisConsentService aisConsentService;

    public UpdateConsentPsuDataResponse proceedDecoupledApproach(String consentId, String authorisationId, SpiAccountConsent spiAccountConsent, PsuIdData psuData) {
        return proceedDecoupledApproach(consentId, authorisationId, spiAccountConsent, null, psuData);
    }

    public UpdateConsentPsuDataResponse proceedDecoupledApproach(String consentId, String authorisationId, SpiAccountConsent spiAccountConsent,
                                                                 String authenticationMethodId, PsuIdData psuData) {
        SpiResponse<SpiAuthorisationDecoupledScaResponse> spiResponse = aisConsentSpi.startScaDecoupled(spiContextDataProvider.provideWithPsuIdData(psuData), authorisationId, authenticationMethodId, spiAccountConsent, aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS);
            log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID [{}], Authorisation-ID [{}], PSU-ID [{}], Authentication-Method-ID [{}]. Notifies a decoupled app about starting SCA when proceed decoupled approach has failed. Error msg: {}.",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consentId, authorisationId, psuData.getPsuId(), authenticationMethodId, errorHolder);

            if (errorHolder.getTppMessageInformationList().get(0).getMessageErrorCode() == MessageErrorCode.PSU_CREDENTIALS_INVALID) {
                aisConsentService.updateConsentAuthorizationStatus(authorisationId, ScaStatus.FAILED);
            }
            return new UpdateConsentPsuDataResponse(errorHolder, consentId, authorisationId);
        }

        UpdateConsentPsuDataResponse response = new UpdateConsentPsuDataResponse(ScaStatus.SCAMETHODSELECTED, consentId, authorisationId);
        response.setPsuMessage(spiResponse.getPayload().getPsuMessage());
        response.setChosenScaMethod(buildXs2aAuthenticationObjectForDecoupledApproach(authenticationMethodId));
        return response;
    }

    // Should ONLY be used for switching from Embedded to Decoupled approach during SCA method selection
    private Xs2aAuthenticationObject buildXs2aAuthenticationObjectForDecoupledApproach(String authenticationMethodId) {
        Xs2aAuthenticationObject xs2aAuthenticationObject = new Xs2aAuthenticationObject();
        xs2aAuthenticationObject.setAuthenticationMethodId(authenticationMethodId);
        return xs2aAuthenticationObject;
    }
}
