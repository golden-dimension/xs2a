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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.specification.PisCommonPaymentDataSpecification;
import de.adorsys.psd2.xs2a.core.pis.InternalPaymentStatus;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommonPaymentDataService {
    private final PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    private final PisCommonPaymentDataSpecification pisCommonPaymentDataSpecification;

    public Optional<PisCommonPaymentData> getPisCommonPaymentData(String paymentId, @Nullable String instanceId) {
        Specification<PisCommonPaymentData> specification = Optional.ofNullable(instanceId)
                                                                .map(i -> pisCommonPaymentDataSpecification.byPaymentIdAndInstanceId(paymentId, i))
                                                                .orElseGet(() -> pisCommonPaymentDataSpecification.byPaymentId(paymentId));

        return pisCommonPaymentDataRepository.findOne(specification);
    }

    @Transactional
    public boolean updateStatusInPaymentData(PisCommonPaymentData paymentData, TransactionStatus status) {
        paymentData.setTransactionStatus(status);
        if (status == TransactionStatus.PATC) {
            paymentData.setMultilevelScaRequired(true);
        }
        PisCommonPaymentData saved = pisCommonPaymentDataRepository.save(paymentData);
        return saved.getPaymentId() != null;
    }

    @Transactional
    public boolean updateInternalStatusInPaymentData(PisCommonPaymentData paymentData, InternalPaymentStatus status) {
        paymentData.setInternalPaymentStatus(status);
        PisCommonPaymentData saved = pisCommonPaymentDataRepository.save(paymentData);
        return saved.getPaymentId() != null;
    }

    @Transactional
    public boolean updateCancelTppRedirectURIs(PisCommonPaymentData paymentData, @NotNull TppRedirectUri tppRedirectUri) {
        paymentData.getAuthorisationTemplate().setCancelRedirectUri(tppRedirectUri.getUri());
        paymentData.getAuthorisationTemplate().setCancelNokRedirectUri(tppRedirectUri.getNokUri());
        PisCommonPaymentData saved = pisCommonPaymentDataRepository.save(paymentData);
        return saved.getPaymentId() != null;
    }

    @Transactional
    public boolean updatePaymentCancellationInternalRequestId(PisCommonPaymentData paymentData, @NotNull String internalRequestId) {
        paymentData.setCancellationInternalRequestId(internalRequestId);
        PisCommonPaymentData saved = pisCommonPaymentDataRepository.save(paymentData);
        return saved.getPaymentId() != null;
    }

    public boolean updatePaymentData(PisCommonPaymentData paymentData, byte[] payment) {
        paymentData.setPayment(payment);
        PisCommonPaymentData saved = pisCommonPaymentDataRepository.save(paymentData);
        return saved.getPaymentId() != null;
    }
}
