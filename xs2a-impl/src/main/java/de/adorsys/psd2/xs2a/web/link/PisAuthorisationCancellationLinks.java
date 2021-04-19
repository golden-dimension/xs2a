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

package de.adorsys.psd2.xs2a.web.link;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.profile.ScaRedirectFlow;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.service.RedirectIdService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.web.RedirectLinkBuilder;
import de.adorsys.psd2.xs2a.web.link.holder.LinksFieldHolder;

import java.util.EnumSet;

import static de.adorsys.psd2.xs2a.core.profile.ScaApproach.*;

public class PisAuthorisationCancellationLinks extends AbstractLinks {//NOSONAR

    private final RedirectLinkBuilder redirectLinkBuilder;
    private final ScaRedirectFlow scaRedirectFlow;
    private final RedirectIdService redirectIdService;
    private final LinksFieldHolder fieldHolder;

    public PisAuthorisationCancellationLinks(LinksFieldHolder fieldHolder,
                                             ScaApproachResolver scaApproachResolver,
                                             RedirectLinkBuilder redirectLinkBuilder,
                                             RedirectIdService redirectIdService,
                                             ScaRedirectFlow scaRedirectFlow) {
        super(fieldHolder.getHttpUrl());
        this.redirectLinkBuilder = redirectLinkBuilder;
        this.scaRedirectFlow = scaRedirectFlow;
        this.redirectIdService = redirectIdService;
        this.fieldHolder = fieldHolder;

        setScaStatus(buildPath(UrlHolder.PIS_CANCELLATION_AUTH_LINK_URL, fieldHolder.getPaymentService(),
            fieldHolder.getPaymentProduct(), fieldHolder.getPaymentId(), fieldHolder.getAuthorisationId()));

        ScaApproach cancellationScaApproach = scaApproachResolver.getScaApproach(fieldHolder.getAuthorisationId());
        if (EnumSet.of(EMBEDDED, DECOUPLED).contains(cancellationScaApproach)) {
            setUpdatePsuAuthentication(buildPath(UrlHolder.PIS_CANCELLATION_AUTH_LINK_URL,
                fieldHolder.getPaymentService(), fieldHolder.getPaymentProduct(),
                fieldHolder.getPaymentId(), fieldHolder.getAuthorisationId()));
        } else if (cancellationScaApproach == REDIRECT) {
            addRedirectRelatedLinks(fieldHolder.getPaymentService(), fieldHolder.getPaymentProduct(),
                fieldHolder.getPaymentId(), fieldHolder.getAuthorisationId(), fieldHolder.getInternalRequestId());
        }
    }

    private void addRedirectRelatedLinks(String paymentService, String paymentProduct, String paymentId, String authorizationId, String internalRequestId) {
        String redirectId = redirectIdService.generateRedirectId(authorizationId);

        String paymentCancellationOauthLink = scaRedirectFlow == ScaRedirectFlow.OAUTH
                                                  ? redirectLinkBuilder.buildPaymentCancellationScaOauthRedirectLink(paymentId, redirectId, internalRequestId)
                                                  : redirectLinkBuilder.buildPaymentCancellationScaRedirectLink(paymentId, redirectId, internalRequestId, fieldHolder.getInstanceId());

        setScaRedirect(new HrefType(paymentCancellationOauthLink));

        setScaStatus(buildPath(UrlHolder.PIS_CANCELLATION_AUTH_LINK_URL, paymentService, paymentProduct, paymentId, authorizationId));
    }
}
