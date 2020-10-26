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

package de.adorsys.psd2.consent.service.psu.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static de.adorsys.psd2.consent.service.psu.constant.PaginationConstants.DEFAULT_ITEMS_PER_PAGE;
import static de.adorsys.psd2.consent.service.psu.constant.PaginationConstants.DEFAULT_PAGE_INDEX;

@Service
public class PageRequestBuilder {
    public PageRequest getPageParams(Integer pageIndex, Integer itemsPerPage) {
        return PageRequest.of(Optional.ofNullable(pageIndex).orElse(DEFAULT_PAGE_INDEX),
                              Optional.ofNullable(itemsPerPage).orElse(DEFAULT_ITEMS_PER_PAGE));
    }
}
