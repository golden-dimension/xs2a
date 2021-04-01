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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.pis.CmsRemittance;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.core.payment.model.*;
import de.adorsys.psd2.xs2a.core.profile.Xs2aAccountReference;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CmsCorePaymentMapper {
    private final CmsAddressMapper cmsAddressMapper;

    public Xs2aPisPaymentInitiationJson mapToPaymentInitiationJson(PisPayment pisPayment) {
        return Optional.ofNullable(pisPayment)
                   .map(ref -> {
                       Xs2aPisPaymentInitiationJson payment = new Xs2aPisPaymentInitiationJson();
                       payment.setDebtorAccount(mapToAccountReference(pisPayment.getDebtorAccount()));
                       payment.setRequestedExecutionDate(pisPayment.getRequestedExecutionDate());
                       setCommonFields(payment, pisPayment);
                       return payment;
                   }).orElse(null);
    }

    public Xs2aPisPaymentInitiationJson mapToPaymentInitiationJson(List<PisPayment> payments) {
        if (CollectionUtils.isEmpty(payments)) {
            return null;
        }
        return mapToPaymentInitiationJson(payments.get(0));
    }

    public Xs2aPisBulkPaymentInitiationJson mapToBulkPaymentInitiationJson(List<PisPayment> payments) {
        if (CollectionUtils.isEmpty(payments)) {
            return null;
        }

        PisPayment pisPayment = payments.get(0);
        Xs2aPisBulkPaymentInitiationJson payment = new Xs2aPisBulkPaymentInitiationJson();
        payment.setPayments(payments.stream().map(this::mapToPaymentInitiationBulkElementJson).collect(Collectors.toList()));

        //Bulk
        payment.setBatchBookingPreferred(pisPayment.getBatchBookingPreferred());
        payment.setDebtorAccount(mapToAccountReference(pisPayment.getDebtorAccount()));
        payment.setRequestedExecutionDate(pisPayment.getRequestedExecutionDate());
        payment.setRequestedExecutionTime(pisPayment.getRequestedExecutionTime());

        return payment;
    }

    public Xs2aPisPeriodicPaymentInitiationJson mapToPeriodicPaymentInitiationJson(List<PisPayment> payments) {
        if (CollectionUtils.isEmpty(payments)) {
            return null;
        }

        PisPayment pisPayment = payments.get(0);
        Xs2aPisPeriodicPaymentInitiationJson payment = new Xs2aPisPeriodicPaymentInitiationJson();

        payment.setDebtorAccount(mapToAccountReference(pisPayment.getDebtorAccount()));
        setCommonFields(payment, pisPayment);

        //Periodic
        payment.setStartDate(pisPayment.getStartDate());
        payment.setEndDate(pisPayment.getEndDate());
        payment.setExecutionRule(Xs2aPisExecutionRule.fromValue(pisPayment.getExecutionRule().getValue()));
        payment.setFrequency(FrequencyCode.valueOf(pisPayment.getFrequency()));
        payment.setDayOfExecution(Xs2aPisDayOfExecution.fromValue(pisPayment.getDayOfExecution().getValue()));

        return payment;
    }

    private Xs2aPisPaymentInitiationJson mapToPaymentInitiationBulkElementJson(PisPayment pisPayment) {
        Xs2aPisPaymentInitiationJson payment = new Xs2aPisPaymentInitiationJson();
        setCommonFields(payment, pisPayment);

        return payment;
    }

    private void setCommonFields(Xs2aPisPaymentInitiationJson payment, PisPayment pisPayment) {
        payment.setCreditorAddress(cmsAddressMapper.mapToAddress(pisPayment.getCreditorAddress()));
        payment.setRemittanceInformationStructured(mapToRemittanceInformationStructured(pisPayment.getRemittanceInformationStructured()));
        payment.setCreditorAgent(pisPayment.getCreditorAgent());
        payment.setCreditorName(pisPayment.getCreditorName());
        payment.setCreditorAccount(mapToAccountReference(pisPayment.getCreditorAccount()));
        payment.setEndToEndIdentification(pisPayment.getEndToEndIdentification());
        payment.setInstructionIdentification(pisPayment.getInstructionIdentification());
        Xs2aAmount amount = new Xs2aAmount();
        amount.setAmount(pisPayment.getAmount().toPlainString());
        amount.setCurrency(mapToCurrency(pisPayment.getCurrency()));
        payment.setInstructedAmount(amount);
        payment.setPurposeCode(Xs2aPisPurposeCode.fromValue(pisPayment.getPurposeCode()));
        payment.setRemittanceInformationUnstructured(pisPayment.getRemittanceInformationUnstructured());
        payment.setUltimateCreditor(pisPayment.getUltimateCreditor());
        payment.setUltimateDebtor(pisPayment.getUltimateDebtor());
    }

    private RemittanceInformationStructured mapToRemittanceInformationStructured(CmsRemittance remittanceInformationStructured) {
        return Optional.ofNullable(remittanceInformationStructured)
                   .map(ref -> {
                       RemittanceInformationStructured informationStructured = new RemittanceInformationStructured();
                       informationStructured.setReference(ref.getReference());
                       informationStructured.setReferenceIssuer(ref.getReferenceIssuer());
                       informationStructured.setReferenceType(ref.getReferenceType());

                       return informationStructured;
                   }).orElse(null);
    }

    private Xs2aPisAccountReference mapToAccountReference(Xs2aAccountReference reference) {
        return Optional.ofNullable(reference)
                   .map(ref -> {
                       Xs2aPisAccountReference accountReference = new Xs2aPisAccountReference();
                       accountReference.setIban(ref.getIban());
                       accountReference.setBban(ref.getBban());
                       accountReference.setMaskedPan(ref.getMaskedPan());
                       accountReference.setMsisdn(ref.getMsisdn());
                       accountReference.setPan(ref.getPan());
                       accountReference.setCurrency(mapToCurrency(ref.getCurrency()));

                       return accountReference;
                   }).orElse(null);
    }

    private String mapToCurrency(Currency currency) {
        return Optional.ofNullable(currency).map(Currency::getCurrencyCode).orElse(null);
    }
}

