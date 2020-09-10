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

import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasket;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketConsentsAndPaymentsResponse;
import de.adorsys.psd2.core.data.Consent;
import de.adorsys.psd2.core.data.CoreSigningBasket;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationTemplate;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.sb.SigningBasketTppInformation;
import de.adorsys.psd2.xs2a.core.pis.CoreCommonPayment;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sb.SigningBasketTransactionStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppNotificationData;
import de.adorsys.psd2.xs2a.domain.sb.CreateSigningBasketRequest;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class CmsXs2aSigningBasketMapper {
    private final RequestProviderService requestProviderService;
    private final Xs2aAisConsentMapper aisConsentMapper;
    private final Xs2aPiisConsentMapper xs2aPiisConsentMapper;
    private final CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper;

    public CmsSigningBasket mapToCmsSigningBasket(CreateSigningBasketRequest request,
                                                  CmsSigningBasketConsentsAndPaymentsResponse consentsAndPaymentsResponse,
                                                  PsuIdData psuData, TppInfo tppInfo,
                                                  SigningBasketTransactionStatus transactionStatus) {
        CmsSigningBasket cmsSigningBasket = new CmsSigningBasket();
        cmsSigningBasket.setInstanceId(request.getInstanceId());

        cmsSigningBasket.setConsents(consentsAndPaymentsResponse.getConsents());
        cmsSigningBasket.setPayments(consentsAndPaymentsResponse.getPayments());

        AuthorisationTemplate authorisationTemplate = new AuthorisationTemplate();
        authorisationTemplate.setTppRedirectUri(request.getTppRedirectUri());
        cmsSigningBasket.setAuthorisationTemplate(authorisationTemplate);

        cmsSigningBasket.setTransactionStatus(transactionStatus);

        cmsSigningBasket.setInternalRequestId(requestProviderService.getInternalRequestIdString());
        cmsSigningBasket.setPsuIdDatas(Collections.singletonList(psuData));

        SigningBasketTppInformation tppInformation = new SigningBasketTppInformation();
        tppInformation.setTppInfo(tppInfo);
        tppInformation.setTppNotificationSupportedModes(Optional.ofNullable(request.getTppNotificationData())
                                                            .map(TppNotificationData::getNotificationModes)
                                                            .orElse(Collections.emptyList()));

        cmsSigningBasket.setTppInformation(tppInformation);

        return cmsSigningBasket;
    }

    public CoreSigningBasket mapToCoreSigningBasket(CmsSigningBasket cmsSigningBasket) {
        CoreSigningBasket coreSigningBasket = new CoreSigningBasket();

        coreSigningBasket.setConsents(mapToConsentList(cmsSigningBasket.getConsents()));
        coreSigningBasket.setPayments(mapToCoreCommonPaymentList(cmsSigningBasket.getPayments()));

        coreSigningBasket.setBasketId(cmsSigningBasket.getId());
        coreSigningBasket.setInstanceId(cmsSigningBasket.getInstanceId());
        coreSigningBasket.setAuthorisationTemplate(cmsSigningBasket.getAuthorisationTemplate());
        coreSigningBasket.setTransactionStatus(cmsSigningBasket.getTransactionStatus());
        coreSigningBasket.setInternalRequestId(cmsSigningBasket.getInternalRequestId());
        coreSigningBasket.setPsuIdDatas(cmsSigningBasket.getPsuIdDatas());
        coreSigningBasket.setMultilevelScaRequired(cmsSigningBasket.isMultilevelScaRequired());
        coreSigningBasket.setTppInformation(cmsSigningBasket.getTppInformation());
        return coreSigningBasket;
    }

    private List<Consent> mapToConsentList(List<CmsConsent> cmsConsents) {
        if (cmsConsents == null) {
            return null;
        }
        return cmsConsents.stream().map(this::mapToConsent).collect(Collectors.toList());
    }

    private Consent mapToConsent(CmsConsent cmsConsent) {
        if (cmsConsent.getConsentType().equals(ConsentType.AIS)) {
            return aisConsentMapper.mapToAisConsent(cmsConsent);
        } else {
            return xs2aPiisConsentMapper.mapToPiisConsent(cmsConsent);
        }
    }

    private List<CoreCommonPayment> mapToCoreCommonPaymentList(List<PisCommonPaymentResponse> pisCommonPaymentResponses) {
        if (pisCommonPaymentResponses == null) {
            return null;
        }
        return pisCommonPaymentResponses.stream().map(this::mapToCoreCommonPayment).collect(Collectors.toList());
    }

    private CoreCommonPayment mapToCoreCommonPayment(PisCommonPaymentResponse pisCommonPaymentResponse) {
        return cmsToXs2aPaymentMapper.mapToXs2aCommonPayment(pisCommonPaymentResponse);
    }
}
