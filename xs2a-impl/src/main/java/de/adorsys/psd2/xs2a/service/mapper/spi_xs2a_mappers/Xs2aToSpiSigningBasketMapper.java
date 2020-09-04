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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.core.data.Consent;
import de.adorsys.psd2.core.data.CoreSigningBasket;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.pis.CoreCommonPayment;
import de.adorsys.psd2.xs2a.core.sb.SigningBasketTransactionStatus;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiSigningBasketTransactionStatus;
import de.adorsys.psd2.xs2a.spi.domain.piis.SpiPiisConsent;
import de.adorsys.psd2.xs2a.spi.domain.sb.SpiSigningBasket;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class Xs2aToSpiSigningBasketMapper {
    private final Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;
    private final Xs2aAisConsentMapper xs2aAisConsentMapper;
    private final Xs2aToSpiPiisConsentMapper xs2aToSpiPiisConsentMapper;
    private final Xs2aToSpiPaymentInfoMapper xs2aToSpiPaymentInfoMapper;
    private final Predicate<? super Consent> isAisConsentType = consent -> consent.getConsentType().equals(ConsentType.AIS);

    public SpiSigningBasket mapToSpiSigningBasket(CoreSigningBasket signingBasket) {
        return Optional.ofNullable(signingBasket)
                   .map(s -> new SpiSigningBasket(
                            s.getBasketId(),
                            s.getInstanceId(),
                            mapToSpiPaymentList(s.getPayments()),
                            mapToSpiAccountConsent(s.getConsents()),
                            mapToSpiPiisConsent(s.getConsents()),
                            mapToSpiSigningBasketTransactionStatus(s.getTransactionStatus()),
                            s.getInternalRequestId(),
                            xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(s.getPsuIdDatas()),
                            s.isMultilevelScaRequired()
                        )
                   )
                   .orElse(null);
    }

    private List<SpiAccountConsent> mapToSpiAccountConsent(List<Consent> consents) {
        return Stream.ofNullable(consents)
                   .flatMap(Collection::stream)
                   .filter(isAisConsentType)
                   .map(consent -> (AisConsent) consent)
                   .map(xs2aAisConsentMapper::mapToSpiAccountConsent)
                   .collect(Collectors.toList());
    }

    private List<SpiPiisConsent> mapToSpiPiisConsent(List<Consent> consents) {
        return Stream.ofNullable(consents)
                   .flatMap(Collection::stream)
                   .filter(Predicate.not(isAisConsentType))
                   .map(consent -> (PiisConsent) consent)
                   .map(xs2aToSpiPiisConsentMapper::mapToSpiPiisConsent)
                   .collect(Collectors.toList());
    }

    private List<SpiPayment> mapToSpiPaymentList(List<CoreCommonPayment> payments) {
        if (payments == null) {
            return null;
        }
        return payments.stream().map(this::mapToSpiPayment).collect(Collectors.toList());
    }

    private SpiPayment mapToSpiPayment(CoreCommonPayment payment) {
        return xs2aToSpiPaymentInfoMapper.mapToSpiPaymentInfo((CommonPayment) payment);
    }

    private SpiSigningBasketTransactionStatus mapToSpiSigningBasketTransactionStatus(SigningBasketTransactionStatus signingBasketTransactionStatus) {
        return Optional.ofNullable(signingBasketTransactionStatus)
                   .map(Enum::name)
                   .map(SpiSigningBasketTransactionStatus::valueOf)
                   .orElse(null);
    }
}
