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

package de.adorsys.psd2.xs2a.web.link;

import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentAuthorizationResponse;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.link.holder.LinksFieldHolder;

import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.REDIRECT;

public class CreatePiisAuthorisationLinks extends AbstractLinks {
    public CreatePiisAuthorisationLinks(LinksFieldHolder fieldHolder, CreateConsentAuthorizationResponse response,
                                        ScaApproachResolver scaApproachResolver, RedirectLinkBuilder redirectLinkBuilder,
                                        RedirectIdService redirectIdService, ScaRedirectFlow scaRedirectFlow) {
        super(fieldHolder.getHttpUrl());

        String consentId = response.getConsentId();
        String authorisationId = response.getAuthorisationId();

        setScaStatus(buildPath(UrlHolder.PIIS_AUTHORISATION_URL, consentId, authorisationId));

        if (scaApproachResolver.getScaApproach(authorisationId) == REDIRECT) {
            String redirectId = redirectIdService.generateRedirectId(authorisationId);

            String consentOauthLink = scaRedirectFlow == ScaRedirectFlow.OAUTH
                                          ? redirectLinkBuilder.buildConsentScaOauthRedirectLink(consentId, redirectId, response.getInternalRequestId())
                                          : redirectLinkBuilder.buildConsentScaRedirectLink(consentId, redirectId, response.getInternalRequestId(), fieldHolder.getInstanceId(), ConsentType.PIIS_TPP);

            setScaRedirect(new HrefType(consentOauthLink));

            if (fieldHolder.isAuthorisationConfirmationRequestMandated()) {
                setConfirmation(buildPath(redirectLinkBuilder.buildConfirmationLink(consentId, redirectId, ConsentType.PIIS_TPP)));
            }

        } else {
            setUpdatePsuAuthentication(buildPath(UrlHolder.PIIS_AUTHORISATION_URL, consentId, authorisationId));
        }
    }
}
