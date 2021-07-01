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

package de.adorsys.psd2.xs2a.service.profile;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.domain.MulticurrencyAccountLevel;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.profile.*;
import de.adorsys.psd2.xs2a.domain.account.SupportedAccountReferenceField;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AspspProfileServiceWrapperTest {
    private static final String ASPSP_SETTINGS_JSON_PATH = "json/service/profile/AspspSettings.json";
    private static final String INSTANCE_ID = "bank1";

    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    private RequestProviderService requestProviderService;

    @InjectMocks
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;

    @BeforeEach
    void setUp() {
        when(requestProviderService.getInstanceId()).thenReturn(INSTANCE_ID);
    }

    @Test
    void getAvailableBookingStatuses() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));
        BookingStatus bookingStatus = BookingStatus.BOOKED;

        // When
        List<BookingStatus> actualAvailableStatuses = aspspProfileServiceWrapper.getAvailableBookingStatuses();

        // Then
        assertEquals(Collections.singletonList(bookingStatus), actualAvailableStatuses);
    }

    @Test
    void getSupportedTransactionStatusFormats() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));
        List<String> expectedFormats = Collections.singletonList("application/json");

        // When
        List<String> actualFormats = aspspProfileServiceWrapper.getSupportedTransactionStatusFormats();

        // Then
        assertEquals(expectedFormats, actualFormats);
    }

    @Test
    void getSupportedTransactionApplicationTypes() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));
        List<String> expected = Collections.singletonList("supportedTransactionApplicationTypes");

        // When
        List<String> actual = aspspProfileServiceWrapper.getSupportedTransactionApplicationTypes();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getSupportedPaymentTypeAndProductMatrix() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));
        Set<String> products = Collections.singleton("sepa-credit-transfers");
        Map<PaymentType, Set<String>> expected = Collections.singletonMap(PaymentType.SINGLE, products);

        // When
        Map<PaymentType, Set<String>> actual = aspspProfileServiceWrapper.getSupportedPaymentTypeAndProductMatrix();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getScaApproaches() {
        // Given
        List<ScaApproach> expected = Collections.singletonList(ScaApproach.DECOUPLED);
        when(aspspProfileService.getScaApproaches(INSTANCE_ID)).thenReturn(expected);

        // When
        List<ScaApproach> actual = aspspProfileServiceWrapper.getScaApproaches();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void isTppSignatureRequired() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        boolean actual = aspspProfileServiceWrapper.isTppSignatureRequired();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isAvailableAccountsConsentSupported() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        boolean actual = aspspProfileServiceWrapper.isAvailableAccountsConsentSupported();

        // Then
        assertThat(actual).isTrue();
    }

    @Test
    void isScaByOneTimeAvailableAccountsConsentRequired() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        boolean actual = aspspProfileServiceWrapper.isScaByOneTimeAvailableAccountsConsentRequired();

        // Then
        assertThat(actual).isTrue();
    }

    @Test
    void isScaByOneTimeGlobalConsentRequired() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        boolean actual = aspspProfileServiceWrapper.isScaByOneTimeGlobalConsentRequired();

        // Then
        assertThat(actual).isTrue();
    }

    @Test
    void isPsuInInitialRequestMandated() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        boolean actual = aspspProfileServiceWrapper.isPsuInInitialRequestMandated();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isForceXs2aBaseLinksUrl() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        boolean actual = aspspProfileServiceWrapper.isForceXs2aBaseLinksUrl();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isGlobalConsentSupported() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        boolean actual = aspspProfileServiceWrapper.isGlobalConsentSupported();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isBankOfferedConsentSupported() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        boolean actual = aspspProfileServiceWrapper.isBankOfferedConsentSupported();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isTransactionsWithoutBalancesSupported() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        boolean actual = aspspProfileServiceWrapper.isTransactionsWithoutBalancesSupported();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isSigningBasketSupported() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        boolean actual = aspspProfileServiceWrapper.isSigningBasketSupported();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isPaymentCancellationAuthorisationMandated() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        boolean actual = aspspProfileServiceWrapper.isPaymentCancellationAuthorisationMandated();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isDebtorAccountOptionalInInitialRequest() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        boolean actual = aspspProfileServiceWrapper.isDebtorAccountOptionalInInitialRequest();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isIbanValidationDisabled() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        boolean actual = aspspProfileServiceWrapper.isIbanValidationDisabled();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isPsuInInitialRequestIgnored() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        boolean actual = aspspProfileServiceWrapper.isPsuInInitialRequestIgnored();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isCheckUriComplianceToDomainSupported() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        boolean actual = aspspProfileServiceWrapper.isCheckUriComplianceToDomainSupported();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isAuthorisationConfirmationCheckByXs2a() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        boolean actual = aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isAuthorisationConfirmationRequestMandated() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        boolean actual = aspspProfileServiceWrapper.isAuthorisationConfirmationRequestMandated();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isCheckTppRolesFromCertificateSupported() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        boolean actual = aspspProfileServiceWrapper.isCheckTppRolesFromCertificateSupported();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isTrustedBeneficiariesSupported() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        boolean actual = aspspProfileServiceWrapper.isTrustedBeneficiariesSupported();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isAccountOwnerInformationSupported() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        boolean actual = aspspProfileServiceWrapper.isAccountOwnerInformationSupported();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isEntryReferenceFromSupported() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        boolean actual = aspspProfileServiceWrapper.isEntryReferenceFromSupported();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isAisPisSessionsSupported() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        boolean actual = aspspProfileServiceWrapper.isAisPisSessionsSupported();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void isDeltaListSupported() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        boolean actual = aspspProfileServiceWrapper.isDeltaListSupported();

        // Then
        assertThat(actual).isFalse();
    }

    @Test
    void getPiisConsentSupported() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        PiisConsentSupported actual = aspspProfileServiceWrapper.getPiisConsentSupported();

        // Then
        assertThat(actual).isEqualTo(PiisConsentSupported.ASPSP_CONSENT_SUPPORTED);
    }

    @Test
    void getPisRedirectUrlToAspsp() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));
        String expected = "http://localhost:4200/pis/{redirect-id}/{encrypted-payment-id}";

        // When
        String actual = aspspProfileServiceWrapper.getPisRedirectUrlToAspsp();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getSupportedPaymentCountryValidation() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));
        String expected = "DE";

        // When
        String actual = aspspProfileServiceWrapper.getSupportedPaymentCountryValidation();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getXs2aBaseLinksUrl() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));
        String expected = "http://myhost.com/";

        // When
        String actual = aspspProfileServiceWrapper.getXs2aBaseLinksUrl();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getOauthConfigurationUrl() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));
        String expected = "http://oauthConfigurationUrl.com/";

        // When
        String actual = aspspProfileServiceWrapper.getOauthConfigurationUrl();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getStartAuthorisationMode() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));
        StartAuthorisationMode expected = StartAuthorisationMode.AUTO;

        // When
        StartAuthorisationMode actual = aspspProfileServiceWrapper.getStartAuthorisationMode();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getScaRedirectFlow() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));
        ScaRedirectFlow expected = ScaRedirectFlow.REDIRECT;

        // When
        ScaRedirectFlow actual = aspspProfileServiceWrapper.getScaRedirectFlow();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getAisRedirectUrlToAspsp() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));
        String expected = "http://localhost:4200/ais/{redirect-id}/{encrypted-consent-id}";

        // When
        String actual = aspspProfileServiceWrapper.getAisRedirectUrlToAspsp();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getPiisRedirectUrlToAspsp() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));
        String expected = "http://localhost:4200/piis/{redirect-id}/{encrypted-consent-id}";

        // When
        String actual = aspspProfileServiceWrapper.getPiisRedirectUrlToAspsp();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getTppUriComplianceResponse() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));
        TppUriCompliance expected = TppUriCompliance.WARNING;

        // When
        TppUriCompliance actual = aspspProfileServiceWrapper.getTppUriComplianceResponse();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getMulticurrencyAccountLevel() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));
        MulticurrencyAccountLevel expected = MulticurrencyAccountLevel.SUBACCOUNT;

        // When
        MulticurrencyAccountLevel actual = aspspProfileServiceWrapper.getMulticurrencyAccountLevel();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getPisPaymentCancellationRedirectUrlToAspsp() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));
        String expected = "http://localhost:4200/pis/cancellation/{redirect-id}/{encrypted-payment-id}";

        // When
        String actual = aspspProfileServiceWrapper.getPisPaymentCancellationRedirectUrlToAspsp();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getSupportedAccountReferenceFields() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));
        List<SupportedAccountReferenceField> expected = Collections.singletonList(SupportedAccountReferenceField.IBAN);

        // When
        List<SupportedAccountReferenceField> actual = aspspProfileServiceWrapper.getSupportedAccountReferenceFields();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getNotificationSupportedModes() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));
        List<NotificationSupportedMode> expected = Collections.singletonList(NotificationSupportedMode.NONE);

        // When
        List<NotificationSupportedMode> actual = aspspProfileServiceWrapper.getNotificationSupportedModes();

        // Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void getMaxConsentValidityDays() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        int actual = aspspProfileServiceWrapper.getMaxConsentValidityDays();

        // Then
        assertThat(actual).isEqualTo(0);
    }

    @Test
    void getAccountAccessFrequencyPerDay() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        int actual = aspspProfileServiceWrapper.getAccountAccessFrequencyPerDay();

        // Then
        assertThat(actual).isEqualTo(4);
    }

    @Test
    void getRedirectUrlExpirationTimeMs() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        long actual = aspspProfileServiceWrapper.getRedirectUrlExpirationTimeMs();

        // Then
        assertThat(actual).isEqualTo(600000);
    }

    @Test
    void getAuthorisationExpirationTimeMs() {
        // Given
        when(aspspProfileService.getAspspSettings(INSTANCE_ID))
            .thenReturn(new JsonReader().getObjectFromFile(ASPSP_SETTINGS_JSON_PATH, AspspSettings.class));

        // When
        long actual = aspspProfileServiceWrapper.getAuthorisationExpirationTimeMs();

        // Then
        assertThat(actual).isEqualTo(86400000);
    }
}
