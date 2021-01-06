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

package de.adorsys.psd2.xs2a.spi.domain.account;

import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class SpiEntryDetails {
    private final String endToEndId;
    private final String mandateId;
    private final String checkId;
    private final String creditorId;
    private final SpiAmount transactionAmount;
    private final List<SpiExchangeRate> currencyExchange;
    private final String creditorName;
    private final SpiAccountReference creditorAccount;
    private final String creditorAgent;
    private final String ultimateCreditor;
    private final String debtorName;
    private final SpiAccountReference debtorAccount;
    private final String debtorAgent;
    private final String ultimateDebtor;
    private final String remittanceInformationUnstructured;
    private final List<String> remittanceInformationUnstructuredArray;
    private final String remittanceInformationStructured;
    private final List<String> remittanceInformationStructuredArray;
    private final String purposeCode;
}
