/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.spi.domain.account;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Transaction report of SPI layer to be used as a container for account reference, transactions and balances.
 * Also, holds encoded file download identifier.
 */
@Value
public class SpiTransactionReport {
    public static final String RESPONSE_TYPE_JSON = "application/json";
    public static final String RESPONSE_TYPE_XML = "application/xml";
    public static final String RESPONSE_TYPE_TEXT = "text/plain";

    /**
     * This field stores the file download identifier. To be used in further calls, when TPP asks
     * the SPI for the transaction list file.
     */
    private String downloadId;

    private List<SpiTransaction> transactions;
    @Nullable
    private List<SpiAccountBalance> balances;
    @NotNull
    private String responseContentType;

    private byte[] transactionsRaw;

    private SpiTransactionLinks spiTransactionLinks;
    private int totalPages;
}
