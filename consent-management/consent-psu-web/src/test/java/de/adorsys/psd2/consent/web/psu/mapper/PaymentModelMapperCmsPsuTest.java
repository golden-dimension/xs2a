/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.web.psu.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(SpringExtension.class)
class PaymentModelMapperCmsPsuTest {

    private static final String CONTENT = "payment content";

    private PaymentModelMapperCmsPsu paymentModelMapper;

    private MockHttpServletRequest mockHttpServletRequest;

    @BeforeEach
    void setUp() {
        mockHttpServletRequest = new MockHttpServletRequest();
        paymentModelMapper = new PaymentModelMapperCmsPsu(mockHttpServletRequest);
    }

    @Test
    void mapToXs2aPayment() {
        mockHttpServletRequest.setContent(CONTENT.getBytes());

        byte[] actual = paymentModelMapper.mapToXs2aPayment();

        assertEquals(CONTENT, new String(actual));
    }
}
