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

package de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers;

import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.web.mapper.TppRedirectUriMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class Xs2aConsentAuthorisationMapper {
    private final TppRedirectUriMapper tppRedirectUriMapper;

    public CreateAuthorisationRequest mapToAuthorisationRequest(ScaStatus scaStatus, PsuIdData psuData, ScaApproach scaApproach, String tppRedirectURI, String tppNOKRedirectURI) {
        return Optional.ofNullable(scaStatus)
                   .map(st -> {
                       CreateAuthorisationRequest consentAuthorization = new CreateAuthorisationRequest();
                       consentAuthorization.setPsuData(psuData);
                       consentAuthorization.setScaApproach(scaApproach);
                       consentAuthorization.setTppRedirectURIs(tppRedirectUriMapper.mapToTppRedirectUri(tppRedirectURI,tppNOKRedirectURI));
                       return consentAuthorization;
                   })
                   .orElse(null);
    }

    public UpdateAuthorisationRequest mapToAuthorisationRequest(UpdateConsentPsuDataReq updatePsuData) {
        return Optional.ofNullable(updatePsuData)
                   .map(data -> {
                       UpdateAuthorisationRequest consentAuthorization = new UpdateAuthorisationRequest();
                       consentAuthorization.setPsuData(data.getPsuData());
                       consentAuthorization.setScaStatus(data.getScaStatus());
                       consentAuthorization.setAuthenticationMethodId(data.getAuthenticationMethodId());
                       consentAuthorization.setPassword(data.getPassword());
                       consentAuthorization.setScaAuthenticationData(data.getScaAuthenticationData());
                       consentAuthorization.setAuthorisationType(AuthorisationType.CONSENT);

                       return consentAuthorization;
                   })
                   .orElse(null);
    }

}
