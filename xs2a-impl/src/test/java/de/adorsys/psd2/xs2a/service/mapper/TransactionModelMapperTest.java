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

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.Transactions;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAdditionalInformationStructured;
import de.adorsys.psd2.xs2a.domain.account.Xs2aStandingOrderDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReport;
import de.adorsys.psd2.xs2a.web.mapper.*;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TransactionModelMapperImpl.class, TestMapperConfiguration.class,
    BalanceMapperImpl.class, ReportExchangeMapperImpl.class, HrefLinkMapper.class, Xs2aObjectMapper.class,
    DayOfExecutionMapper.class, OffsetDateTimeMapper.class, PurposeCodeMapperImpl.class, AmountModelMapper.class})
class TransactionModelMapperTest {
    private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.now();
    private static final String BYTE_ARRAY_IN_STRING = "000000000000000=";

    @Autowired
    private TransactionModelMapper mapper;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void mapToTransaction_success() {
        Transactions transactions = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-transactions.json", Transactions.class);
        de.adorsys.psd2.model.Transactions actualTransactionDetails = mapper.mapToTransactions(transactions);

        de.adorsys.psd2.model.Transactions expectedReportTransactionDetails = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-transaction-details-expected.json",
                                                                                                           de.adorsys.psd2.model.Transactions.class);
        assertThat(actualTransactionDetails).isEqualTo(expectedReportTransactionDetails);
    }

    @Test
    void mapToTransactionDetails_success() {
        Transactions transactions = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-transactions.json", Transactions.class);

        InlineResponse2001 actualInlineResponse2001 = mapper.mapToTransactionDetails(transactions);

        de.adorsys.psd2.model.Transactions expectedTransactionDetails = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-transaction-details-expected.json",
                                                                                                     de.adorsys.psd2.model.Transactions.class);
        assertThat(actualInlineResponse2001).isNotNull();
        assertThat(actualInlineResponse2001.getTransactionsDetails().getTransactionDetails()).isEqualTo(expectedTransactionDetails);
    }

    @Test
    void mapToTransactionsResponseRaw_success() {
        Xs2aTransactionsReport xs2aTransactionsReport = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-transactions-report.json", Xs2aTransactionsReport.class);

        byte[] actualByteArray = mapper.mapToTransactionsResponseRaw(xs2aTransactionsReport);

        String actual = Base64.getEncoder().encodeToString(actualByteArray);

        assertThat(actual).isEqualTo(BYTE_ARRAY_IN_STRING);
    }

    @Test
    void mapToTransactionsResponse200Json_success() {
        // Given
        Xs2aTransactionsReport xs2aTransactionsReport = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-transactions-report.json", Xs2aTransactionsReport.class);

        // When
        TransactionsResponse200Json actual = mapper.mapToTransactionsResponse200Json(xs2aTransactionsReport);
        actual.getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        TransactionsResponse200Json expected = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-transactionsResponse200.json",
                                                                            TransactionsResponse200Json.class);
        expected.getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        // Then
        assertLinks(expected.getLinks(), actual.getLinks());
        expected.getTransactions().setLinks(null);
        actual.getTransactions().setLinks(null);

        expected.setLinks(actual.getLinks());
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToAccountReport_null() {
        AccountReport actual = mapper.mapToAccountReport(null);
        assertThat(actual).isNull();
    }

    @Test
    void mapToTransactionsResponse200Json_null() {
        TransactionsResponse200Json actual = mapper.mapToTransactionsResponse200Json(null);
        assertThat(actual).isNull();
    }

    @Test
    void mapToTransactions_null() {
        de.adorsys.psd2.model.Transactions actual = mapper.mapToTransactions(null);
        assertThat(actual).isNull();
    }

    @Test
    void mapToEntryDetails_null() {
        EntryDetails actual = mapper.mapToEntryDetails(null);
        assertThat(actual).isNull();
    }

    @Test
    void transactionInfo_isNull() {
        Transactions transactions = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-transaction-info-isNull.json", Transactions.class);
        de.adorsys.psd2.model.Transactions actualTransactionDetails = mapper.mapToTransactions(transactions);

        de.adorsys.psd2.model.Transactions expectedReportTransactionDetails = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-transaction-expected-info-isNull.json",
                                                                                                           de.adorsys.psd2.model.Transactions.class);
        assertThat(actualTransactionDetails).isEqualTo(expectedReportTransactionDetails);
    }

    @Test
    void remittanceInformationStructured_missingVariousInfo() {
        de.adorsys.psd2.xs2a.domain.EntryDetails inputDetails = jsonReader.getObjectFromFile("json/service/mapper/transaction-model-mapper/entryDetails-variousInfo-isNull.json", de.adorsys.psd2.xs2a.domain.EntryDetails.class);

        EntryDetails actual = mapper.mapToEntryDetails(inputDetails);

        EntryDetails expectedDetails = jsonReader.getObjectFromFile("json/service/mapper/transaction-model-mapper/entryDetails-variousInfo-isNull-expected.json", EntryDetails.class);

        assertThat(actual).isEqualTo(expectedDetails);
    }

    @ParameterizedTest
    @EnumSource(de.adorsys.psd2.xs2a.core.pis.FrequencyCode.class)
    void frequencyCodeToFrequencyCode(de.adorsys.psd2.xs2a.core.pis.FrequencyCode frequencyCode) {
        Transactions transactions = new Transactions();
        Xs2aAdditionalInformationStructured a = new Xs2aAdditionalInformationStructured();
        Xs2aStandingOrderDetails s = new Xs2aStandingOrderDetails();
        s.setFrequency(frequencyCode);
        a.setStandingOrderDetails(s);
        transactions.setAdditionalInformationStructured(a);

        de.adorsys.psd2.model.Transactions transactions1 = mapper.mapToTransactions(transactions);

        String actual = transactions1.getAdditionalInformationStructured().getStandingOrderDetails().getFrequency().name();

        assertThat(actual).isEqualTo(frequencyCode.name());
    }

    @ParameterizedTest
    @EnumSource(PisExecutionRule.class)
    void frequencyCodeToFrequencyCode(PisExecutionRule pisExecutionRule) {
        Transactions transactions = new Transactions();
        Xs2aAdditionalInformationStructured a = new Xs2aAdditionalInformationStructured();
        Xs2aStandingOrderDetails s = new Xs2aStandingOrderDetails();
        s.setExecutionRule(pisExecutionRule);
        a.setStandingOrderDetails(s);
        transactions.setAdditionalInformationStructured(a);

        de.adorsys.psd2.model.Transactions transactions1 = mapper.mapToTransactions(transactions);

        String actual = transactions1.getAdditionalInformationStructured().getStandingOrderDetails().getExecutionRule().name();

        assertThat(actual).isEqualTo(pisExecutionRule.name());
    }

    @Test
    void monthOfExecution() {
        List<String> monthsOfExecution = new MonthsOfExecution();
        String month = "month";
        monthsOfExecution.add(month);

        Transactions transactions = new Transactions();
        Xs2aAdditionalInformationStructured a = new Xs2aAdditionalInformationStructured();
        Xs2aStandingOrderDetails s = new Xs2aStandingOrderDetails();
        s.setMonthsOfExecution(monthsOfExecution);
        a.setStandingOrderDetails(s);
        transactions.setAdditionalInformationStructured(a);

        de.adorsys.psd2.model.Transactions transactions1 = mapper.mapToTransactions(transactions);

        MonthsOfExecution actual = transactions1.getAdditionalInformationStructured().getStandingOrderDetails().getMonthsOfExecution();

        assertThat(actual).isEqualTo(monthsOfExecution);
    }

    @Test
    void remittanceInformationUnstructured_isNull() {
        de.adorsys.psd2.xs2a.domain.Transactions input = jsonReader.getObjectFromFile("json/service/mapper/transaction-model-mapper/transactions-remittanceInformationUnstructured-isNull.json", Transactions.class);

        de.adorsys.psd2.model.Transactions actual = mapper.mapToTransactions(input);

        de.adorsys.psd2.model.Transactions expected = jsonReader.getObjectFromFile("json/service/mapper/transaction-model-mapper/transactions-remittanceInformationUnstructured-isNull-expected.json", de.adorsys.psd2.model.Transactions.class);

        assertThat(actual).isEqualTo(expected);
    }

    private void assertLinks(Map<?, ?> expectedLinks, Map<?, ?> actualLinks) {
        assertNotNull(actualLinks);
        assertFalse(actualLinks.isEmpty());
        assertEquals(expectedLinks.size(), actualLinks.size());
        for (Object linkKey : actualLinks.keySet()) {
            HrefType actualHrefType = (HrefType) actualLinks.get(linkKey);
            assertEquals(String.valueOf(((Map) expectedLinks.get(linkKey)).get("href")), actualHrefType.getHref());
        }
    }
}
