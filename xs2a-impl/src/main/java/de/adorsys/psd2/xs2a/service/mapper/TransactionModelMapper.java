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

import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.domain.Xs2aTransactions;
import de.adorsys.psd2.xs2a.domain.Xs2aEntryDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountReport;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTransactionsReport;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.mapper.*;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.Nullable;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {
    AmountModelMapper.class, PurposeCodeMapper.class,
    Xs2aAddressMapper.class, AspspProfileServiceWrapper.class,
    ReportExchangeMapper.class, BalanceMapper.class,
    DayOfExecutionMapper.class})
public abstract class TransactionModelMapper {
    @Autowired
    protected ReportExchangeMapper reportExchangeMapper;
    @Autowired
    protected HrefLinkMapper hrefLinkMapper;
    @Autowired
    protected BalanceMapper balanceMapper;
    @Autowired
    protected DayOfExecutionMapper dayOfExecutionMapper;

    @Mapping(target = "_links", ignore = true)
    @Mapping(target = "links", expression = "java(hrefLinkMapper.mapToLinksMap(accountReport.getLinks()))")
    public abstract AccountReport mapToAccountReport(Xs2aAccountReport accountReport);

    @Mapping(target = "_links", ignore = true)
    @Mapping(target = "links", expression = "java(hrefLinkMapper.mapToLinksMap(transactionsReport.getLinks()))")
    @Mapping(target = "transactions", source = "accountReport")
    @Mapping(target = "account", source = "accountReference")
    public abstract TransactionsResponse200Json mapToTransactionsResponse200Json(Xs2aTransactionsReport transactionsReport);

    public byte[] mapToTransactionsResponseRaw(Xs2aTransactionsReport transactionsReport) {
        return transactionsReport.getAccountReport().getTransactionsRaw();
    }

    @Mapping(target = "currencyExchange", expression = "java(reportExchangeMapper.mapToReportExchanges(transactions.getExchangeRate()))")
    @Mapping(target = "bankTransactionCode", source = "bankTransactionCodeCode.code")
    @Mapping(target = "transactionAmount", source = "amount")
    @Mapping(target = "additionalInformationStructured.standingOrderDetails.dayOfExecution", expression = "java(dayOfExecutionMapper.mapDayOfExecution(xs2aStandingOrderDetails.getDayOfExecution()))")
    @Mapping(target = "links", ignore = true)
    @Mapping(target = "_links", ignore = true)
    @Mapping(target = "creditorName", source = "transactionInfo.creditorName")
    @Mapping(target = "creditorAccount", source = "transactionInfo.creditorAccount")
    @Mapping(target = "creditorAgent", source = "transactionInfo.creditorAgent")
    @Mapping(target = "ultimateCreditor", source = "transactionInfo.ultimateCreditor")
    @Mapping(target = "debtorName", source = "transactionInfo.debtorName")
    @Mapping(target = "debtorAccount", source = "transactionInfo.debtorAccount")
    @Mapping(target = "debtorAgent", source = "transactionInfo.debtorAgent")
    @Mapping(target = "ultimateDebtor", source = "transactionInfo.ultimateDebtor")
    @Mapping(target = "remittanceInformationUnstructured", source = "transactionInfo.remittanceInformationUnstructured")
    @Mapping(target = "remittanceInformationUnstructuredArray", source = "transactionInfo.remittanceInformationUnstructuredArray")
    @Mapping(target = "remittanceInformationStructured", source = "transactionInfo.remittanceInformationStructured")
    @Mapping(target = "remittanceInformationStructuredArray", source = "transactionInfo.remittanceInformationStructuredArray")
    @Mapping(target = "purposeCode", source = "transactionInfo.purposeCode")
    public abstract de.adorsys.psd2.model.Transactions mapToTransactions(Xs2aTransactions transactions);

    @Mapping(target = "creditorName", source = "transactionInfo.creditorName")
    @Mapping(target = "creditorAccount", source = "transactionInfo.creditorAccount")
    @Mapping(target = "creditorAgent", source = "transactionInfo.creditorAgent")
    @Mapping(target = "ultimateCreditor", source = "transactionInfo.ultimateCreditor")
    @Mapping(target = "debtorName", source = "transactionInfo.debtorName")
    @Mapping(target = "debtorAccount", source = "transactionInfo.debtorAccount")
    @Mapping(target = "debtorAgent", source = "transactionInfo.debtorAgent")
    @Mapping(target = "ultimateDebtor", source = "transactionInfo.ultimateDebtor")
    @Mapping(target = "remittanceInformationUnstructured", source = "transactionInfo.remittanceInformationUnstructured")
    @Mapping(target = "remittanceInformationUnstructuredArray", source = "transactionInfo.remittanceInformationUnstructuredArray")
    @Mapping(target = "remittanceInformationStructured", source = "transactionInfo.remittanceInformationStructured")
    @Mapping(target = "remittanceInformationStructuredArray", source = "transactionInfo.remittanceInformationStructuredArray")
    @Mapping(target = "purposeCode", source = "transactionInfo.purposeCode")
    public abstract de.adorsys.psd2.model.EntryDetails mapToEntryDetails(Xs2aEntryDetails entryDetails);

    public InlineResponse2001 mapToTransactionDetails(Xs2aTransactions transactions) {
        InlineResponse2001 inlineResponse2001 = new InlineResponse2001();
        TransactionDetailsBody transactionDetailsBody = new TransactionDetailsBody();
        transactionDetailsBody.setTransactionDetails(mapToTransactions(transactions));
        inlineResponse2001.setTransactionsDetails(transactionDetailsBody);
        return inlineResponse2001;
    }

    protected @Nullable TransactionList mapToTransactionList(@Nullable List<Xs2aTransactions> transactions) {
        if (CollectionUtils.isEmpty(transactions)) {
            return null;
        }

        List<de.adorsys.psd2.model.Transactions> transactionDetails = transactions.stream()
                                                                          .map(this::mapToTransactions)
                                                                          .collect(Collectors.toList());

        TransactionList transactionList = new TransactionList();
        transactionList.addAll(transactionDetails);
        return transactionList;
    }
}
