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
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.web.mapper.PurposeCodeMapperImpl;
import de.adorsys.psd2.xs2a.web.mapper.Xs2aAddressMapperImpl;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PaymentModelMapperImpl.class, Xs2aAddressMapperImpl.class, PurposeCodeMapperImpl.class})
class PaymentModelMapperImplTest {

    @Autowired
    private PaymentModelMapper paymentModelMapper;

    private final JsonReader jsonReader = new JsonReader();

    @ParameterizedTest
    @EnumSource(FrequencyCode.class)
    void frequencyCode_test(FrequencyCode frequencyCode) {

        PeriodicPaymentInitiationJson periodicPaymentInitiationJson = jsonReader.getObjectFromFile("json/service/mapper/periodic-payment-initiation.json", PeriodicPaymentInitiationJson.class);

        periodicPaymentInitiationJson.setFrequency(frequencyCode);

        PeriodicPayment actual = paymentModelMapper.mapToXs2aPayment(periodicPaymentInitiationJson);

        assertThat(actual.getFrequency().name()).isEqualTo(frequencyCode.name());
    }

    @Test
    void mapToXs2aPayment_null() {
        PeriodicPayment actual = paymentModelMapper.mapToXs2aPayment((PeriodicPaymentInitiationJson) null);

        assertThat(actual).isNull();
    }

    @Test
    void mapToXs2aPayment_singlePayment_null() {
        SinglePayment actual = paymentModelMapper.mapToXs2aPayment((PaymentInitiationJson) null);

        assertThat(actual).isNull();
    }

    @Test
    void mapToXs2aPayment_singlePayment_ok() {
        PaymentInitiationJson input = jsonReader.getObjectFromFile("json/service/mapper/single-payment-initiation.json", PaymentInitiationJson.class);

        SinglePayment actual = paymentModelMapper.mapToXs2aPayment(input);

        SinglePayment expected = jsonReader.getObjectFromFile("json/service/mapper/single-payment-initiation-expected.json", SinglePayment.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToXs2aPayment_bulkPayment_null() {
        BulkPayment actual = paymentModelMapper.mapToXs2aPayment((BulkPaymentInitiationJson) null);

        assertThat(actual).isNull();
    }

    @Test
    void mapToXs2aPayment_bulkPayment_ok() {
        BulkPaymentInitiationJson input = jsonReader.getObjectFromFile("json/service/mapper/bulk-payment-initiation.json", BulkPaymentInitiationJson.class);

        BulkPayment actual = paymentModelMapper.mapToXs2aPayment(input);

        BulkPayment expected = jsonReader.getObjectFromFile("json/service/mapper/bulk-payment-initiation-expected.json", BulkPayment.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToXs2aAmount_null() {
        Xs2aAmount actual = paymentModelMapper.mapToXs2aAmount(null);

        assertThat(actual).isNull();
    }

    @Test
    void mapToXs2aAmount_ok() {
        Amount input = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-amount.json", Amount.class);

        Xs2aAmount actual = paymentModelMapper.mapToXs2aAmount(input);

        Xs2aAmount expected = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-amount.json", Xs2aAmount.class);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToAccountReference_null() {
        AccountReference actual = paymentModelMapper.mapToAccountReference(null);

        assertThat(actual).isNull();
    }

    @Test
    void mapToAccountReference_ok() {
        de.adorsys.psd2.model.AccountReference input = jsonReader.getObjectFromFile("json/service/mapper/account-reference.json", de.adorsys.psd2.model.AccountReference.class);

        de.adorsys.psd2.xs2a.core.profile.AccountReference actual = paymentModelMapper.mapToAccountReference(input);

        de.adorsys.psd2.xs2a.core.profile.AccountReference expected = jsonReader.getObjectFromFile("json/service/mapper/account-reference.json", de.adorsys.psd2.xs2a.core.profile.AccountReference.class);

        assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> params() {
        String periodicPayment = "json/service/mapper/periodic-payment-initiation.json";
        String periodicPaymentExpected = "json/service/mapper/periodic-payment-initiation-expected.json";
        String periodicPaymentExecutionRuleFollowing = "json/service/mapper/periodic-payment-initiation-executionRule-following.json";
        String periodicPaymentExecutionRuleFollowingExpected = "json/service/mapper/periodic-payment-initiation-expected-executionRule-following.json";
        String periodicPaymentExecutionRuleIsNull = "json/service/mapper/periodic-payment-initiation-variousInfo-isNull.json";
        String periodicPaymentExecutionRuleIsNullExpected = "json/service/mapper/periodic-payment-initiation-expected-variousInfo-isNull.json";

        return Stream.of(
            Arguments.arguments(periodicPayment, periodicPaymentExpected),
            Arguments.arguments(periodicPaymentExecutionRuleFollowing, periodicPaymentExecutionRuleFollowingExpected),
            Arguments.arguments(periodicPaymentExecutionRuleIsNull, periodicPaymentExecutionRuleIsNullExpected)
        );
    }

    @ParameterizedTest
    @MethodSource("params")
    void mapToXs2aPayment(String inputPath, String expectedPath) {
        PeriodicPaymentInitiationJson input = jsonReader.getObjectFromFile(inputPath, PeriodicPaymentInitiationJson.class);
        PeriodicPayment expected = jsonReader.getObjectFromFile(expectedPath, PeriodicPayment.class);

        PeriodicPayment actual = paymentModelMapper.mapToXs2aPayment(input);

        assertThat(actual).isEqualTo(expected);
    }
}
