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

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.aspsp.profile.domain.MulticurrencyAccountLevel;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetailsHolder;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountListHolder;
import de.adorsys.psd2.xs2a.domain.account.Xs2aBalancesReport;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.web.mapper.*;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {AccountModelMapperImpl.class, TestMapperConfiguration.class, BalanceMapperImpl.class,
    ReportExchangeMapperImpl.class, DayOfExecutionMapper.class, OffsetDateTimeMapper.class, HrefLinkMapper.class,
    Xs2aObjectMapper.class, PurposeCodeMapperImpl.class, AmountModelMapper.class
})
class AccountModelMapperTest {
    private static final OffsetDateTime OFFSET_DATE_TIME = OffsetDateTime.now();

    @Autowired
    private AccountModelMapper mapper;

    @MockBean
    private AspspProfileServiceWrapper aspspProfileService;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void mapToAccountList() {
        // Given
        Xs2aAccountListHolder xs2aAccountListHolder = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-account-list-holder.json", Xs2aAccountListHolder.class);

        // When
        AccountList actualAccountList = mapper.mapToAccountList(xs2aAccountListHolder);


        actualAccountList.getAccounts().get(0).getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        AccountList expectedAccountList = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-account-list-expected.json", AccountList.class);
        expectedAccountList.getAccounts().get(0).getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        // Then
        assertLinks(expectedAccountList.getAccounts().get(0).getLinks(), actualAccountList.getAccounts().get(0).getLinks());

        expectedAccountList.getAccounts().get(0).setLinks(actualAccountList.getAccounts().get(0).getLinks());
        assertThat(actualAccountList).isEqualTo(expectedAccountList);
    }

    @Test
    void mapToAccountDetails() {
        Xs2aAccountDetailsHolder xs2aAccountDetailsHolder = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-account-details-holder.json", Xs2aAccountDetailsHolder.class);
        InlineResponse200 actualInlineResponse200 = mapper.mapToInlineResponse200(xs2aAccountDetailsHolder);

        AccountDetails expectedAccountDetails = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-account-details-expected.json", AccountDetails.class);

        assertLinks(expectedAccountDetails.getLinks(), actualInlineResponse200.getAccount().getLinks());

        expectedAccountDetails.setLinks(actualInlineResponse200.getAccount().getLinks());
        assertThat(expectedAccountDetails).isEqualTo(actualInlineResponse200.getAccount());
    }

    @Test
    void mapToAccountDetails_null() {
        AccountDetails actual = mapper.mapToAccountDetails(null);
        assertThat(actual).isNull();
    }

    @Test
    void mapToAccountReference_success() {
        AccountReference accountReference = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-account-reference.json", AccountReference.class);
        de.adorsys.psd2.model.AccountReference actualAccountReference = mapper.mapToAccountReference(accountReference);

        de.adorsys.psd2.model.AccountReference expectedAccountReference = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-account-reference-expected.json",
                                                                                                       de.adorsys.psd2.model.AccountReference.class);
        assertThat(actualAccountReference).isEqualTo(expectedAccountReference);
    }

    @Test
    void mapToAccountReference_nullValue() {
        de.adorsys.psd2.model.AccountReference accountReference = mapper.mapToAccountReference(null);
        assertThat(accountReference).isNull();
    }

    @Test
    void mapToAccountReferences() {
        AccountReference accountReference = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-account-reference.json", AccountReference.class);
        List<de.adorsys.psd2.model.AccountReference> actualAccountReferences = mapper.mapToAccountReferences(Collections.singletonList(accountReference));

        de.adorsys.psd2.model.AccountReference expectedAccountReference = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-account-reference-expected.json",
                                                                                                       de.adorsys.psd2.model.AccountReference.class);

        assertThat(actualAccountReferences).asList().hasSize(1).contains(expectedAccountReference);
    }

    @Test
    void mapToBalance_null() {
        ReadAccountBalanceResponse200 actual = mapper.mapToBalance(null);
        assertThat(actual).isNull();
    }

    @Test
    void mapToAccountReferences_null() {
        List<de.adorsys.psd2.model.AccountReference> actual = mapper.mapToAccountReferences(null);
        assertThat(actual).isNull();
    }

    @ParameterizedTest
    @MethodSource("params")
    void accountStatusToAccountStatus_status_deleted(String input, String expected) {
        Xs2aAccountDetailsHolder accountDetails = jsonReader.getObjectFromFile(input, Xs2aAccountDetailsHolder.class);

        AccountDetails actual = mapper.mapToAccountDetails(accountDetails.getAccountDetails());

        AccountDetails accountDetailsExpected = jsonReader.getObjectFromFile(expected, AccountDetails.class);

        assertLinks(accountDetailsExpected.getLinks(), actual.getLinks());
        accountDetailsExpected.setLinks(null);
        actual.setLinks(null);

        accountDetailsExpected.setLinks(actual.getLinks());
        assertThat(actual).isEqualTo(accountDetailsExpected);
    }

    @Test
    void mapToBalance_ReadAccountBalanceResponse200() {
        // Given
        Xs2aBalancesReport xs2aBalancesReport = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-balances-report.json", Xs2aBalancesReport.class);

        LocalDateTime lastChangeDateTime = LocalDateTime.parse("2018-03-31T15:16:16.374");
        ZoneOffset zoneOffset = ZoneId.systemDefault().getRules().getOffset(lastChangeDateTime);
        OffsetDateTime expectedLastChangeDateTime = lastChangeDateTime.atOffset(zoneOffset);

        // When
        ReadAccountBalanceResponse200 actualReadAccountBalanceResponse200 = mapper.mapToBalance(xs2aBalancesReport);

        ReadAccountBalanceResponse200 expectedReadAccountBalanceResponse200 = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-read-account-balance-expected.json", ReadAccountBalanceResponse200.class);

        // Then
        Balance actualBalance = actualReadAccountBalanceResponse200.getBalances().get(0);
        assertEquals(expectedLastChangeDateTime, actualBalance.getLastChangeDateTime());

        actualBalance.setLastChangeDateTime(OFFSET_DATE_TIME);
        expectedReadAccountBalanceResponse200.getBalances().get(0).setLastChangeDateTime(OFFSET_DATE_TIME);

        assertThat(actualReadAccountBalanceResponse200).isEqualTo(expectedReadAccountBalanceResponse200);
    }

    @Test
    void mapToAccountDetailsCurrency_currencyPresent() {
        //Given
        Currency currency = Currency.getInstance("EUR");
        //When
        String currencyRepresentation = mapper.mapToAccountDetailsCurrency(currency);
        //Then
        assertThat(currency.getCurrencyCode()).isEqualTo(currencyRepresentation);
    }

    @Test
    void mapToAccountDetailsCurrency_currencyNull() {
        //Given
        //When
        String currencyRepresentation = mapper.mapToAccountDetailsCurrency(null);
        //Then
        assertThat(currencyRepresentation).isNull();
    }

    @Test
    void mapToAccountDetailsCurrency_multicurrencySubaccount() {
        //Given
        when(aspspProfileService.getMulticurrencyAccountLevel()).thenReturn(MulticurrencyAccountLevel.SUBACCOUNT);
        //When
        String currencyRepresentation = mapper.mapToAccountDetailsCurrency(null);
        //Then
        assertThat(currencyRepresentation).isNull();
    }

    @Test
    void mapToAccountDetailsCurrency_multicurrencyAggregations() {
        Arrays.asList(MulticurrencyAccountLevel.AGGREGATION, MulticurrencyAccountLevel.AGGREGATION_AND_SUBACCOUNT).forEach(multicurrencyAccountLevel -> {
            //Given
            when(aspspProfileService.getMulticurrencyAccountLevel()).thenReturn(multicurrencyAccountLevel);
            //When
            String currencyRepresentation = mapper.mapToAccountDetailsCurrency(null);
            //Then
            assertThat(currencyRepresentation).isEqualTo("XXX");
        });
    }

    @Test
    void mapToAccountList_currencyPresent_multicurrencyLevelSubaccount() {
        //Given
        when(aspspProfileService.getMulticurrencyAccountLevel()).thenReturn(MulticurrencyAccountLevel.SUBACCOUNT);
        Currency currency = Currency.getInstance("EUR");
        Xs2aAccountDetails xs2aAccountDetails = buildXs2aAccountDetails(currency);
        Xs2aAccountListHolder xs2aAccountListHolder = new Xs2aAccountListHolder(Collections.singletonList(xs2aAccountDetails), null);
        //When
        AccountList actualAccountList = mapper.mapToAccountList(xs2aAccountListHolder);
        //Then
        AccountDetails accountDetails = actualAccountList.getAccounts().get(0);
        assertThat(accountDetails.getCurrency()).isEqualTo(currency.getCurrencyCode());
    }

    @Test
    void mapToAccountList_currencyNull_multicurrencyLevelSubaccount() {
        //Given
        when(aspspProfileService.getMulticurrencyAccountLevel()).thenReturn(MulticurrencyAccountLevel.SUBACCOUNT);
        Currency currency = null;
        Xs2aAccountDetails xs2aAccountDetails = buildXs2aAccountDetails(currency);
        Xs2aAccountListHolder xs2aAccountListHolder = new Xs2aAccountListHolder(Collections.singletonList(xs2aAccountDetails), null);
        //When
        AccountList actualAccountList = mapper.mapToAccountList(xs2aAccountListHolder);
        //Then
        AccountDetails accountDetails = actualAccountList.getAccounts().get(0);
        assertThat(accountDetails.getCurrency()).isNull();
    }

    @Test
    void mapToAccountList_currencyPresent_multicurrencyLevelAggregation() {
        //Given
        Arrays.asList(MulticurrencyAccountLevel.AGGREGATION, MulticurrencyAccountLevel.AGGREGATION_AND_SUBACCOUNT).forEach(multicurrencyAccountLevel -> {
            //Given
            when(aspspProfileService.getMulticurrencyAccountLevel()).thenReturn(multicurrencyAccountLevel);
            Currency currency = Currency.getInstance("EUR");
            Xs2aAccountDetails xs2aAccountDetails = buildXs2aAccountDetails(currency);
            Xs2aAccountListHolder xs2aAccountListHolder = new Xs2aAccountListHolder(Collections.singletonList(xs2aAccountDetails), null);
            //When
            AccountList actualAccountList = mapper.mapToAccountList(xs2aAccountListHolder);
            //Then
            AccountDetails accountDetails = actualAccountList.getAccounts().get(0);
            assertThat(accountDetails.getCurrency()).isEqualTo(currency.getCurrencyCode());
        });
    }

    @Test
    void mapToAccountList_currencyNull_multicurrencyLevelAggregation() {
        //Given
        Arrays.asList(MulticurrencyAccountLevel.AGGREGATION, MulticurrencyAccountLevel.AGGREGATION_AND_SUBACCOUNT).forEach(multicurrencyAccountLevel -> {
            //Given
            when(aspspProfileService.getMulticurrencyAccountLevel()).thenReturn(multicurrencyAccountLevel);
            Currency currency = null;
            Xs2aAccountDetails xs2aAccountDetails = buildXs2aAccountDetails(currency);
            Xs2aAccountListHolder xs2aAccountListHolder = new Xs2aAccountListHolder(Collections.singletonList(xs2aAccountDetails), null);
            //When
            AccountList actualAccountList = mapper.mapToAccountList(xs2aAccountListHolder);
            //Then
            AccountDetails accountDetails = actualAccountList.getAccounts().get(0);
            assertThat(accountDetails.getCurrency()).isEqualTo("XXX");
        });
    }

    private static Stream<Arguments> params() {
        String accountsDetailsStatusDeleted = "json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-account-details-holder-accountStatusDeleted.json";
        String accountDetailsStatusDeletedExpected = "json/service/mapper/account-model-mapper/AccountModelMapper-account-details-expected-statusDeleted.json";
        String accountDetailsStatusBlocked = "json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-account-details-holder-accountStatusBlocked.json";
        String accountDetailsStatusBlockedExpected = "json/service/mapper/account-model-mapper/AccountModelMapper-account-details-expected-statusBlocked.json";
        String accountDetailsAccountUsageOrga = "json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-account-details-holder-accountUsage-orga.json";
        String accountDetailsAccountUsageOrgaExpected = "json/service/mapper/account-model-mapper/AccountModelMapper-account-details-expected-accountUsage-orga.json";

        return Stream.of(Arguments.arguments(accountsDetailsStatusDeleted, accountDetailsStatusDeletedExpected),
                         Arguments.arguments(accountDetailsStatusBlocked, accountDetailsStatusBlockedExpected),
                         Arguments.arguments(accountDetailsAccountUsageOrga, accountDetailsAccountUsageOrgaExpected)
        );
    }

    private Xs2aAccountDetails buildXs2aAccountDetails(Currency currency) {
        return new Xs2aAccountDetails(null, null, null, null,
                                      null, null, null, currency,
                                      null, null, null, null,
                                      null, null, null, null,
                                      null, null, null, null);
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
