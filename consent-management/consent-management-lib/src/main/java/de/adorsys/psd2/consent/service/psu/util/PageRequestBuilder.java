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

import de.adorsys.psd2.xs2a.core.pagination.data.PageRequestParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PageRequestBuilder {
    @Value("${cms.defaultPageIndex:0}")
    private int defaultPageIndex;
    @Value("${cms.defaultItemsPerPage:20}")
    private int defaultItemsPerPage;

    public Pageable getPageable(Integer pageIndex, Integer itemsPerPage) {
        if (pageIndex == null && itemsPerPage == null) {
            return Pageable.unpaged();
        }
        return PageRequest.of(getValueOrDefault(pageIndex, defaultPageIndex),
                              getValueOrDefault(itemsPerPage, defaultItemsPerPage));
    }

    public Pageable getPageable(PageRequestParameters pageRequestParameters) {
        if (pageRequestParameters == null) {
            return Pageable.unpaged();
        }
        else if (pageRequestParameters.getPageIndex() == null && pageRequestParameters.getItemsPerPage() == null) {
            return Pageable.unpaged();
        }
        return PageRequest.of(getValueOrDefault(pageRequestParameters.getPageIndex(), defaultPageIndex),
            getValueOrDefault(pageRequestParameters.getItemsPerPage(), defaultItemsPerPage));
    }

    private int getValueOrDefault(Integer value, int defaultValue) {
        if (value == null || value < 0) {
            return defaultValue;
        }
        return value;
    }
}
