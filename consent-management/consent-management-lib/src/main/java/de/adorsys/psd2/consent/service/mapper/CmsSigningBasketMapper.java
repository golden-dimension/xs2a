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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.sb.CmsSigningBasket;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.sb.SigningBasketEntity;
import de.adorsys.psd2.xs2a.core.sb.SigningBasketTransactionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;


@Component
@RequiredArgsConstructor
public class CmsSigningBasketMapper {
    private final AuthorisationTemplateMapper authorisationTemplateMapper;
    private final PsuDataMapper psuDataMapper;
    private final CmsConsentMapper cmsConsentMapper;
    private final PisCommonPaymentMapper pisCommonPaymentMapper;
    private final SigningBasketTppInformationMapper signingBasketTppInformationMapper;

    public CmsSigningBasket mapToCmsSigningBasket(SigningBasketEntity entity, Map<String, List<AuthorisationEntity>> authorisations, Map<String, Map<String, Integer>> usages) {
        CmsSigningBasket cmsSigningBasket = new CmsSigningBasket();
        cmsSigningBasket.setId(entity.getExternalId());
        cmsSigningBasket.setConsents(cmsConsentMapper.mapToCmsConsents(entity.getConsents(), authorisations, usages));
        cmsSigningBasket.setPayments(pisCommonPaymentMapper.mapToPisCommonPaymentResponses(entity.getPayments(), authorisations));
        cmsSigningBasket.setAuthorisationTemplate(authorisationTemplateMapper.mapToAuthorisationTemplate(entity.getAuthorisationTemplate()));
        cmsSigningBasket.setTransactionStatus(SigningBasketTransactionStatus.getByName(entity.getTransactionStatus()));
        cmsSigningBasket.setInternalRequestId(entity.getInternalRequestId());
        cmsSigningBasket.setPsuIdDatas(psuDataMapper.mapToPsuIdDataList(entity.getPsuDataList()));
        cmsSigningBasket.setMultilevelScaRequired(entity.isMultilevelScaRequired());
        cmsSigningBasket.setTppInformation(signingBasketTppInformationMapper.mapToSigningBasketTppInformation(entity.getTppInformation()));
        cmsSigningBasket.setInstanceId(entity.getInstanceId());
        return cmsSigningBasket;
    }

    public SigningBasketEntity mapToNewSigningBasket(CmsSigningBasket cmsSigningBasket, List<ConsentEntity> consentEntities, List<PisCommonPaymentData> payments) {
        SigningBasketEntity signingBasket = new SigningBasketEntity();
        signingBasket.setExternalId(UUID.randomUUID().toString());
        signingBasket.setConsents(consentEntities);
        signingBasket.setPayments(payments);
        signingBasket.setTransactionStatus(cmsSigningBasket.getTransactionStatus().toString());
        signingBasket.setInternalRequestId(cmsSigningBasket.getInternalRequestId());
        signingBasket.setAuthorisationTemplate(authorisationTemplateMapper.mapToAuthorisationTemplateEntity(cmsSigningBasket.getAuthorisationTemplate()));
        signingBasket.setPsuDataList(psuDataMapper.mapToPsuDataList(cmsSigningBasket.getPsuIdDatas(), cmsSigningBasket.getInstanceId()));
        signingBasket.setMultilevelScaRequired(cmsSigningBasket.isMultilevelScaRequired());
        signingBasket.setInstanceId(cmsSigningBasket.getInstanceId());
        signingBasket.setTppInformation(signingBasketTppInformationMapper.mapToSigningBasketTppInformationEntity(cmsSigningBasket.getTppInformation()));
        return signingBasket;
    }
}
