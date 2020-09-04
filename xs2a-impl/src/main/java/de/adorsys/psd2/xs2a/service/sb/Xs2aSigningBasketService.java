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

package de.adorsys.psd2.xs2a.service.sb;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasket;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketConsentsAndPaymentsResponse;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketCreationResponse;
import de.adorsys.psd2.consent.api.service.SigningBasketServiceEncrypted;
import de.adorsys.psd2.xs2a.core.consent.SigningBasketTppInformation;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.signingbasket.SigningBasketTransactionStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.sb.CreateSigningBasketRequest;
import de.adorsys.psd2.xs2a.domain.sb.Xs2aCreateSigningBasketResponse;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.CmsXs2aSigningBasketMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class Xs2aSigningBasketService {
    private final CmsXs2aSigningBasketMapper signingBasketMapper;
    private final SigningBasketServiceEncrypted signingBasketService;

    public Optional<Xs2aCreateSigningBasketResponse> createSigningBasket(CreateSigningBasketRequest request, CmsSigningBasketConsentsAndPaymentsResponse consentsAndPaymentsResponse, PsuIdData psuData, TppInfo tppInfo) {
        CmsSigningBasket cmsSigningBasket = signingBasketMapper.mapToCmsSigningBasket(request, consentsAndPaymentsResponse, psuData, tppInfo, SigningBasketTransactionStatus.RCVD);
        CmsResponse<CmsSigningBasketCreationResponse> response = signingBasketService.createSigningBasket(cmsSigningBasket);

        if (response.hasError()) {
            log.info("Signing basket cannot be created, because can't save to cms DB");
            return Optional.empty();
        }

        CmsSigningBasketCreationResponse createSigningBasketResponse = response.getPayload();
        return Optional.ofNullable(createSigningBasketResponse)
                   .map(c -> new Xs2aCreateSigningBasketResponse(c.getBasketId(),
                                                                 signingBasketMapper.mapToCoreSigningBasket(c.getCmsSigningBasket()),
                                                                 getNotificationSupportedMode(c.getCmsSigningBasket())));
    }

    public Optional<CmsSigningBasketConsentsAndPaymentsResponse> getConsentsAndPayments(List<String> consentIds, List<String> paymentIds) {
        CmsResponse<CmsSigningBasketConsentsAndPaymentsResponse> response = signingBasketService.getConsentsAndPayments(consentIds, paymentIds);

        if (response.hasError()) {
            log.info("Payments and consents couldn't be found, because can't get from cms DB");
            return Optional.empty();
        }

        return Optional.ofNullable(response.getPayload());
    }

    public void updateSigningBasketStatus(String basketId, SigningBasketTransactionStatus transactionStatus) {
        signingBasketService.updateTransactionStatusById(basketId, transactionStatus);
    }

    public void updateMultilevelScaRequired(String basketId, boolean multilevelScaRequired) {
        signingBasketService.updateMultilevelScaRequired(basketId, multilevelScaRequired);
    }

    private List<NotificationSupportedMode> getNotificationSupportedMode(CmsSigningBasket cmsSigningBasket) {
        return Optional.ofNullable(cmsSigningBasket)
                   .map(CmsSigningBasket::getTppInformation)
                   .map(SigningBasketTppInformation::getTppNotificationSupportedModes)
                   .orElse(null);
    }
}
