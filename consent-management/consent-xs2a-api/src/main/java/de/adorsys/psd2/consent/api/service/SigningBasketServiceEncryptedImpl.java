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

package de.adorsys.psd2.consent.api.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasket;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketConsentsAndPaymentsResponse;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketCreationResponse;
import de.adorsys.psd2.xs2a.core.signingbasket.SigningBasketTransactionStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SigningBasketServiceEncryptedImpl implements SigningBasketServiceEncrypted {
    @Override
    public CmsResponse<CmsSigningBasketCreationResponse> createSigningBasket(CmsSigningBasket signingBasket) {
        return CmsResponse.<CmsSigningBasketCreationResponse>builder()
                   .payload(new CmsSigningBasketCreationResponse(signingBasket.getId(), signingBasket))
                   .build();
    }

    @Override
    public CmsResponse<CmsSigningBasketConsentsAndPaymentsResponse> getConsentsAndPayments(List<String> consents, List<String> payments) {
        return null;
    }

    @Override
    public CmsResponse<Boolean> updateTransactionStatusById(String basketId, SigningBasketTransactionStatus transactionStatus) {
        return null;
    }

    @Override
    public CmsResponse<Boolean> updateMultilevelScaRequired(String basketId, boolean multilevelScaRequired) {
        return null;
    }
}
