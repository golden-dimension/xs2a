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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.BulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.CommonPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.domain.pis.SinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiBulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPeriodicPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiSinglePaymentInitiationResponse;
import de.adorsys.psd2.xs2a.web.mapper.ScaMethodsMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT,
uses = ScaMethodsMapper.class)
public abstract class SpiToXs2aPaymentMapper {

    @Autowired
    protected ScaMethodsMapper scaMethodsMapper;

    @Mapping(target = "scaMethods", expression = "java(scaMethodsMapper.mapToAuthenticationObjectList(spi.getScaMethods()))")
    @Mapping(target = "psuMessage", source = "spi.psuMessage")
    @Mapping(target = "tppMessageInformation", source ="spi.tppMessages")
    @Mapping(target = "transactionFeeIndicator", source = "spi.spiTransactionFeeIndicator")
    @Mapping(target = "aspspConsentDataProvider", source = "aspspConsentDataProvider")
    public abstract SinglePaymentInitiationResponse mapToPaymentInitiateResponse(SpiSinglePaymentInitiationResponse spi,
                                                                 InitialSpiAspspConsentDataProvider aspspConsentDataProvider);

    public void main(SpiSinglePaymentInitiationResponse spi, SinglePaymentInitiationResponse s) {
       s.setTppMessageInformation(spi.getTppMessages());
    }

    @Mapping(target = "scaMethods", expression = "java(scaMethodsMapper.mapToAuthenticationObjectList(spi.getScaMethods()))")
    @Mapping(target = "psuMessage", source = "spi.psuMessage")
    @Mapping(target = "tppMessageInformation", source ="spi.tppMessages")
    @Mapping(target = "transactionFeeIndicator", source = "spi.spiTransactionFeeIndicator")
    @Mapping(target = "aspspConsentDataProvider", source = "aspspConsentDataProvider")
    public abstract PeriodicPaymentInitiationResponse mapToPaymentInitiateResponse(SpiPeriodicPaymentInitiationResponse spi,
                                                                   InitialSpiAspspConsentDataProvider aspspConsentDataProvider);

    @Mapping(target = "scaMethods", expression = "java(scaMethodsMapper.mapToAuthenticationObjectList(spi.getScaMethods()))")
    @Mapping(target = "psuMessage", source = "spi.psuMessage")
    @Mapping(target = "tppMessageInformation", source ="spi.tppMessages")
    @Mapping(target = "transactionFeeIndicator", source = "spi.spiTransactionFeeIndicator")
    @Mapping(target = "aspspConsentDataProvider", source = "aspspConsentDataProvider")
    public abstract BulkPaymentInitiationResponse mapToPaymentInitiateResponse(SpiBulkPaymentInitiationResponse spi,
                                                               InitialSpiAspspConsentDataProvider aspspConsentDataProvider);

    @Mapping(target = "scaMethods", expression = "java(scaMethodsMapper.mapToAuthenticationObjectList(spi.getScaMethods()))")
    @Mapping(target = "psuMessage", source = "spi.psuMessage")
    @Mapping(target = "tppMessageInformation", source ="spi.tppMessages")
    @Mapping(target = "paymentType", source = "type")
    @Mapping(target = "transactionFeeIndicator", source = "spi.spiTransactionFeeIndicator")
    @Mapping(target = "aspspConsentDataProvider", source = "aspspConsentDataProvider")
    public abstract CommonPaymentInitiationResponse mapToCommonPaymentInitiateResponse(SpiPaymentInitiationResponse spi,
                                                                       PaymentType type, InitialSpiAspspConsentDataProvider aspspConsentDataProvider);

    @BeanMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_NULL)
    public abstract Xs2aAmount spiAmountToXs2aAmount(SpiAmount spiAmount);
}
