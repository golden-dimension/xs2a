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
import de.adorsys.psd2.xs2a.core.sb.SigningBasketTransactionStatus;

import java.util.List;

/**
 * Base version of SigningBasketService that contains all method declarations.
 * Should not be implemented directly, consider using one of the interfaces that extends this one.
 *
 * @see SigningBasketService
 * @see SigningBasketServiceEncrypted
 */
interface SigningBasketServiceBase {

    /**
     * Creates Signing Basket
     *
     * @param signingBasket needed parameters for creating Signing Basket
     * @return create signing basket response, containing basket and its encrypted ID
     */
    CmsResponse<CmsSigningBasketCreationResponse> createSigningBasket(CmsSigningBasket signingBasket);

    /**
     * Get ids of payment and consents.
     *
     * @param consents consents of the to be created signing basket
     * @param payments payments of the to be created signing basket
     * @return signing basket consents and payments response, containing consents and payments of the signing basket
     */
    CmsResponse<CmsSigningBasketConsentsAndPaymentsResponse> getConsentsAndPayments(List<String> consents, List<String> payments);

    /**
     * Updates consent status by id
     *
     * @param basketId          id of signing basket
     * @param transactionStatus new signing basket status
     * @return true if signing basket was found and status was updated, false otherwise.
     */
    CmsResponse<Boolean> updateTransactionStatusById(String basketId, SigningBasketTransactionStatus transactionStatus);

    /**
     * Updates multilevel SCA required field
     *
     * @param basketId              String representation of the signing basket identifier
     * @param multilevelScaRequired multilevel SCA required indicator
     * @return <code>true</code> if authorisation was found and SCA required field updated, <code>false</code> otherwise
     */
    CmsResponse<Boolean> updateMultilevelScaRequired(String basketId, boolean multilevelScaRequired);
}
