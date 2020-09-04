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

import de.adorsys.psd2.xs2a.domain.sb.CreateSigningBasketResponse;

public class CreateSigningBasketLinks extends AbstractLinks {

    public CreateSigningBasketLinks(String httpUrl, CreateSigningBasketResponse response) {
        super(httpUrl);

        String basketId = response.getBasketId();

        setSelf(buildPath(UrlHolder.SIGNING_BASKET_LINK_URL, basketId));
        setStatus(buildPath(UrlHolder.SIGNING_BASKET_STATUS_URL, basketId));
        setStartAuthorisation(buildPath(UrlHolder.CREATE_SIGNING_BASKET_AUTHORISATION_URL, basketId));
    }
}
