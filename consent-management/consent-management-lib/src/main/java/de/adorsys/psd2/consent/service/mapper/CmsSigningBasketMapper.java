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

import de.adorsys.psd2.consent.api.signingbasket.CmsSigningBasket;
import de.adorsys.psd2.consent.api.signingbasket.CmsSigningBasketConsent;
import de.adorsys.psd2.consent.api.signingbasket.CmsSigningBasketPayment;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.signingbaskets.SigningBasket;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.signingbasket.SigningBasketTransactionStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class CmsSigningBasketMapper {
    private final AuthorisationTemplateMapper authorisationTemplateMapper;
    private final PsuDataMapper psuDataMapper;

    public Optional<CmsSigningBasket> decrypt(CmsSigningBasket cmsSigningBasket, SecurityDataService securityDataService) {
        Optional<List<CmsSigningBasketConsent>> consents = decryptConsents(cmsSigningBasket, securityDataService);
        Optional<List<CmsSigningBasketPayment>> payments = decryptPayments(cmsSigningBasket, securityDataService);

        if (consents.isEmpty() || payments.isEmpty()) {
            return Optional.empty();
        }

        CmsSigningBasket decryptedCmsSigningBasket = new CmsSigningBasket();
        decryptedCmsSigningBasket.setId(cmsSigningBasket.getId());
        decryptedCmsSigningBasket.setConsents(consents.get());
        decryptedCmsSigningBasket.setPayments(payments.get());
        decryptedCmsSigningBasket.setAuthorisationTemplate(cmsSigningBasket.getAuthorisationTemplate());
        decryptedCmsSigningBasket.setTransactionStatus(cmsSigningBasket.getTransactionStatus());
        decryptedCmsSigningBasket.setInternalRequestId(cmsSigningBasket.getInternalRequestId());
        decryptedCmsSigningBasket.setPsuIdDatas(cmsSigningBasket.getPsuIdDatas());
        decryptedCmsSigningBasket.setMultilevelScaRequired(cmsSigningBasket.isMultilevelScaRequired());
        return Optional.of(decryptedCmsSigningBasket);
    }

    private Optional<List<CmsSigningBasketConsent>> decryptConsents(CmsSigningBasket cmsSigningBasket, SecurityDataService securityDataService) {
        List<CmsSigningBasketConsent> consents = new ArrayList<>();
        for (CmsSigningBasketConsent cmsSigningBasketConsent : cmsSigningBasket.getConsents()) {
            Optional<CmsSigningBasketConsent> consent = decryptCmsSigningBasketConsent(cmsSigningBasketConsent, securityDataService);

            if (consent.isEmpty()) {
                return Optional.empty();
            }

            consents.add(consent.get());
        }
        return Optional.of(consents);
    }

    private Optional<List<CmsSigningBasketPayment>> decryptPayments(CmsSigningBasket cmsSigningBasket, SecurityDataService securityDataService) {
        List<CmsSigningBasketPayment> payments = new ArrayList<>();
        for (CmsSigningBasketPayment cmsSigningBasketPayment : cmsSigningBasket.getPayments()) {
            Optional<CmsSigningBasketPayment> payment = decryptCmsSigningBasketPayment(cmsSigningBasketPayment, securityDataService);

            if (payment.isEmpty()) {
                return Optional.empty();
            }

            payments.add(payment.get());
        }
        return Optional.of(payments);
    }

    private Optional<CmsSigningBasketConsent> decryptCmsSigningBasketConsent(CmsSigningBasketConsent consent, SecurityDataService securityDataService) {
        Optional<String> consentId = securityDataService.decryptId(consent.getId());

        if (consentId.isEmpty()) {
            return Optional.empty();
        }

        CmsSigningBasketConsent decryptedConsent = new CmsSigningBasketConsent();
        decryptedConsent.setId(consentId.get());
        return Optional.of(decryptedConsent);
    }

    private Optional<CmsSigningBasketPayment> decryptCmsSigningBasketPayment(CmsSigningBasketPayment payment, SecurityDataService securityDataService) {
        Optional<String> paymentId = securityDataService.decryptId(payment.getId());

        if (paymentId.isEmpty()) {
            return Optional.empty();
        }

        CmsSigningBasketPayment decryptedPayment = new CmsSigningBasketPayment();
        decryptedPayment.setId(paymentId.get());
        return Optional.of(decryptedPayment);
    }

    public CmsSigningBasket mapToCmsSigningBasket(SigningBasket entity) {
        CmsSigningBasket cmsSigningBasket = new CmsSigningBasket();
        cmsSigningBasket.setId(entity.getExternalId());
        cmsSigningBasket.setConsents(mapToCmsSigningBasketConsents(entity.getConsents()));
        cmsSigningBasket.setPayments(mapToCmsSigningBasketPayments(entity.getPayments()));
        cmsSigningBasket.setAuthorisationTemplate(authorisationTemplateMapper.mapToAuthorisationTemplate(entity.getAuthorisationTemplate()));
        cmsSigningBasket.setTransactionStatus(SigningBasketTransactionStatus.getByValue(entity.getTransactionStatus()));
        cmsSigningBasket.setInternalRequestId(entity.getInternalRequestId());
        cmsSigningBasket.setPsuIdDatas(psuDataMapper.mapToPsuIdDataList(entity.getPsuDataList()));
        cmsSigningBasket.setMultilevelScaRequired(entity.isMultilevelScaRequired());
        return cmsSigningBasket;
    }

    public SigningBasket mapToNewSigningBasket(CmsSigningBasket cmsSigningBasket, List<ConsentEntity> consentEntities, List<PisCommonPaymentData> payments) {
        SigningBasket signingBasket = new SigningBasket();
        signingBasket.setExternalId(UUID.randomUUID().toString());
        signingBasket.setConsents(consentEntities);
        signingBasket.setPayments(payments);
        signingBasket.setTransactionStatus(cmsSigningBasket.getTransactionStatus().toString());
        signingBasket.setInternalRequestId(cmsSigningBasket.getInternalRequestId());
        signingBasket.setAuthorisationTemplate(authorisationTemplateMapper.mapToAuthorisationTemplateEntity(cmsSigningBasket.getAuthorisationTemplate()));
        signingBasket.setPsuDataList(psuDataMapper.mapToPsuDataList(cmsSigningBasket.getPsuIdDatas(), cmsSigningBasket.getInstanceId()));
        signingBasket.setMultilevelScaRequired(cmsSigningBasket.isMultilevelScaRequired());
        return signingBasket;
    }

    public List<CmsSigningBasketConsent> mapToCmsSigningBasketConsents(List<ConsentEntity> consentEntities) {
        return consentEntities.stream().map(this::mapToCmsSigningBasketConsent).collect(Collectors.toList());
    }

    public List<CmsSigningBasketPayment> mapToCmsSigningBasketPayments(List<PisCommonPaymentData> payments) {
        return payments.stream().map(this::mapToCmsSigningBasketPayments).collect(Collectors.toList());
    }

    private CmsSigningBasketConsent mapToCmsSigningBasketConsent(ConsentEntity consentEntity) {
        CmsSigningBasketConsent cmsSigningBasketConsent = new CmsSigningBasketConsent();
        cmsSigningBasketConsent.setId(consentEntity.getExternalId());
        return cmsSigningBasketConsent;
    }

    private CmsSigningBasketPayment mapToCmsSigningBasketPayments(PisCommonPaymentData payment) {
        CmsSigningBasketPayment cmsSigningBasketPayment = new CmsSigningBasketPayment();
        cmsSigningBasketPayment.setId(payment.getExternalId());
        return cmsSigningBasketPayment;
    }
}
