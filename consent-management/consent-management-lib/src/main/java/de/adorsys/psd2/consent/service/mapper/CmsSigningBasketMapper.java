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

import de.adorsys.psd2.consent.api.signingBasket.*;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.signingBaskets.SigningBasket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class CmsSigningBasketMapper {
    private CmsConsentMapper cmsConsentMapper;
    private PisCommonPaymentMapper pisCommonPaymentMapper;
    private AuthorisationTemplateMapper authorisationTemplateMapper;
    private PsuDataMapper psuDataMapper;

    public CmsSigningBasket mapToCmsSigningBasket(SigningBasket entity, List<AuthorisationEntity> authorisations, Map<String, Integer> usages) {
        CmsSigningBasket cmsSigningBasket = new CmsSigningBasket();
        cmsSigningBasket.setId(entity.getExternalId());
        cmsSigningBasket.setConsents(entity.getConsents().stream().map(consentEntity -> cmsConsentMapper.mapToCmsConsent(consentEntity, authorisations, usages)).collect(Collectors.toList()));
        cmsSigningBasket.setPayments(entity.getPayments().stream().map(paymentEntity -> pisCommonPaymentMapper.mapToPisCommonPaymentResponse(paymentEntity, authorisations).get()).collect(Collectors.toList()));
        cmsSigningBasket.setAuthorisationTemplate(authorisationTemplateMapper.mapToAuthorisationTemplate(entity.getAuthorisationTemplate()));
        cmsSigningBasket.setTransactionStatus(entity.getTransactionStatus());
        cmsSigningBasket.setInternalRequestId(entity.getInternalRequestId());
        cmsSigningBasket.setPsuIdDatas(psuDataMapper.mapToPsuIdDataList(entity.getPsuDataList()));
        return cmsSigningBasket;
    }

//    public ConsentEntity mapToNewConsentEntity(CmsConsent cmsConsent) {
//        ConsentEntity entity = new ConsentEntity();
//        entity.setData(cmsConsent.getConsentData());
//        entity.setChecksum(cmsConsent.getChecksum());
//        entity.setExternalId(UUID.randomUUID().toString());
//        entity.setConsentStatus(cmsConsent.getConsentStatus());
//        entity.setConsentType(cmsConsent.getConsentType().getName());
//        entity.setFrequencyPerDay(cmsConsent.getFrequencyPerDay());
//        entity.setMultilevelScaRequired(cmsConsent.isMultilevelScaRequired());
//        entity.setRequestDateTime(OffsetDateTime.now());
//        entity.setValidUntil(cmsConsent.getValidUntil());
//        entity.setExpireDate(cmsConsent.getExpireDate());
//        entity.setPsuDataList(psuDataMapper.mapToPsuDataList(cmsConsent.getPsuIdDataList(), cmsConsent.getInstanceId()));
//        entity.getPsuDataList().forEach(p -> p.setInstanceId(cmsConsent.getInstanceId()));
//        entity.setAuthorisationTemplate(authorisationTemplateMapper.mapToAuthorisationTemplateEntity(cmsConsent.getAuthorisationTemplate()));
//        entity.setRecurringIndicator(cmsConsent.isRecurringIndicator());
//        entity.setLastActionDate(LocalDate.now());
//        entity.setInternalRequestId(cmsConsent.getInternalRequestId());
//        entity.setTppInformation(consentTppInformationMapper.mapToConsentTppInformationEntity(cmsConsent.getTppInformation()));
//        AccountAccess tppAccountAccesses = cmsConsent.getTppAccountAccesses();
//        entity.setTppAccountAccesses(accessMapper.mapToTppAccountAccess(tppAccountAccesses));
//        entity.setAspspAccountAccesses(accessMapper.mapToAspspAccountAccess(cmsConsent.getAspspAccountAccesses()));
//        entity.setInstanceId(cmsConsent.getInstanceId());
//
//        AdditionalInformationAccess additionalInformationAccess = tppAccountAccesses.getAdditionalInformationAccess();
//        if (additionalInformationAccess != null) {
//            entity.setOwnerNameType(AdditionalAccountInformationType.findTypeByList(additionalInformationAccess.getOwnerName()));
//            entity.setTrustedBeneficiariesType(AdditionalAccountInformationType.findTypeByList(additionalInformationAccess.getTrustedBeneficiaries()));
//        }
//        return entity;
//    }
}
