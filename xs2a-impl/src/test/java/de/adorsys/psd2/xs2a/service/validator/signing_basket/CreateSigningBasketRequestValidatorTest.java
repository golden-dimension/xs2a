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

package de.adorsys.psd2.xs2a.service.validator.signing_basket;

import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketConsentsAndPaymentsResponse;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.sb.CreateSigningBasketRequest;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.PsuDataInInitialRequestValidator;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.signing_basket.dto.CreateSigningBasketRequestObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateSigningBasketRequestValidatorTest {
    private static final PsuIdData EMPTY_PSU_DATA = new PsuIdData(null, null, null, null, null);
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", null, null, null, null);
    private static final MessageError FORMAT_ERROR_VALIDATION_ERROR =
        new MessageError(ErrorType.SB_400, TppMessageInformation.of(FORMAT_ERROR));
    private static final MessageError MAX_ENTRIES_VALIDATION_ERROR =
        new MessageError(ErrorType.SB_400, TppMessageInformation.buildWithCustomError(FORMAT_ERROR, "Number of entries in Signing Basket should not exceed more than " + 5));
    private static final MessageError SIGNING_BASKET_NOT_SUPPORTED_VALIDATION_ERROR =
        new MessageError(ErrorType.SB_405, TppMessageInformation.buildWithCustomError(SERVICE_INVALID_405, "Signing basket is not supported by ASPSP"));
    private static final MessageError REFERENCE_MIX_INVALID_VALIDATION_ERROR =
        new MessageError(ErrorType.SB_400, TppMessageInformation.of(REFERENCE_MIX_INVALID));
    private static final MessageError REFERENCE_STATUS_INVALID_VALIDATION_ERROR =
        new MessageError(ErrorType.SB_409, TppMessageInformation.of(REFERENCE_STATUS_INVALID));

    @InjectMocks
    private CreateSigningBasketRequestValidator createSigningBasketRequestValidator;
    @Mock
    private PsuDataInInitialRequestValidator psuDataInInitialRequestValidator;
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private Xs2aAisConsentMapper aisConsentMapper;

    @Test
    void validate_withInvalidPsuData_shouldReturnErrorFromValidator() {
        //Given
        when(aspspProfileService.isSigningBasketSupported())
            .thenReturn(true);
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.invalid(FORMAT_ERROR_VALIDATION_ERROR));

        CreateSigningBasketRequest createSigningBasketRequest = new CreateSigningBasketRequest(Collections.emptyList(), Collections.emptyList(), null, null, null);
        CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse = new CmsSigningBasketConsentsAndPaymentsResponse(Collections.emptyList(), Collections.emptyList());

        //When
        ValidationResult validationResult = createSigningBasketRequestValidator.validate(new CreateSigningBasketRequestObject(createSigningBasketRequest, cmsSigningBasketConsentsAndPaymentsResponse, EMPTY_PSU_DATA, true));

        //Then
        verify(psuDataInInitialRequestValidator).validate(EMPTY_PSU_DATA);
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(FORMAT_ERROR_VALIDATION_ERROR);
    }

    @Test
    void validate_signingBasketMaxEntries_shouldReturnErrorFromValidator() {
        //Given
        when(aspspProfileService.isSigningBasketSupported())
            .thenReturn(true);
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.getSigningBasketMaxEntries())
            .thenReturn(5);

        CreateSigningBasketRequest createSigningBasketRequest = new CreateSigningBasketRequest(List.of("1", "2", "3"), List.of("4", "5", "6"), null, null, null);
        CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse = new CmsSigningBasketConsentsAndPaymentsResponse(Collections.emptyList(), Collections.emptyList());

        //When
        ValidationResult validationResult = createSigningBasketRequestValidator.validate(new CreateSigningBasketRequestObject(createSigningBasketRequest, cmsSigningBasketConsentsAndPaymentsResponse, PSU_DATA, true));

        //Then
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(MAX_ENTRIES_VALIDATION_ERROR);
    }

    @Test
    void validate_signingBasketIsNotSupported_shouldReturnErrorFromValidator() {
        //Given
        when(aspspProfileService.isSigningBasketSupported())
            .thenReturn(false);

        CreateSigningBasketRequest createSigningBasketRequest = new CreateSigningBasketRequest(Collections.emptyList(), Collections.emptyList(), null, null, null);
        CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse = new CmsSigningBasketConsentsAndPaymentsResponse(Collections.emptyList(), Collections.emptyList());

        //When
        ValidationResult validationResult = createSigningBasketRequestValidator.validate(new CreateSigningBasketRequestObject(createSigningBasketRequest, cmsSigningBasketConsentsAndPaymentsResponse, PSU_DATA, true));

        //Then
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(SIGNING_BASKET_NOT_SUPPORTED_VALIDATION_ERROR);
    }

    @Test
    void validate_explicitAuthorisationPreferredFalse_shouldReturnErrorFromValidator() {
        //Given
        when(aspspProfileService.isSigningBasketSupported())
            .thenReturn(true);


        CreateSigningBasketRequest createSigningBasketRequest = new CreateSigningBasketRequest(Collections.emptyList(), Collections.emptyList(), null, null, null);
        CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse = new CmsSigningBasketConsentsAndPaymentsResponse(Collections.emptyList(), Collections.emptyList());

        //When
        ValidationResult validationResult = createSigningBasketRequestValidator.validate(new CreateSigningBasketRequestObject(createSigningBasketRequest, cmsSigningBasketConsentsAndPaymentsResponse, PSU_DATA, false));

        //Then
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(FORMAT_ERROR_VALIDATION_ERROR);
    }

    @Test
    void validate_signingBasketIsEmpty_shouldReturnErrorFromValidator() {
        //Given
        when(aspspProfileService.isSigningBasketSupported())
            .thenReturn(true);
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.valid());

        CreateSigningBasketRequest createSigningBasketRequest = new CreateSigningBasketRequest(Collections.emptyList(), Collections.emptyList(), null, null, null);
        CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse = new CmsSigningBasketConsentsAndPaymentsResponse(Collections.emptyList(), Collections.emptyList());

        //When
        ValidationResult validationResult = createSigningBasketRequestValidator.validate(new CreateSigningBasketRequestObject(createSigningBasketRequest, cmsSigningBasketConsentsAndPaymentsResponse, PSU_DATA, true));

        //Then
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(REFERENCE_MIX_INVALID_VALIDATION_ERROR);
    }

    @Test
    void validate_signingBasketWrongIds_shouldReturnErrorFromValidator() {
        //Given
        when(aspspProfileService.isSigningBasketSupported())
            .thenReturn(true);
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.getSigningBasketMaxEntries())
            .thenReturn(5);


        CreateSigningBasketRequest createSigningBasketRequest = new CreateSigningBasketRequest(List.of("2"), List.of("1"), null, null, null);
        CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse = new CmsSigningBasketConsentsAndPaymentsResponse(Collections.singletonList(buildCmsConsent("1")), Collections.emptyList());

        //When
        ValidationResult validationResult = createSigningBasketRequestValidator.validate(new CreateSigningBasketRequestObject(createSigningBasketRequest, cmsSigningBasketConsentsAndPaymentsResponse, PSU_DATA, true));

        //Then
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(REFERENCE_MIX_INVALID_VALIDATION_ERROR);
    }

    @Test
    void validate_signingBasketBankOfferedConsent_shouldReturnErrorFromValidator() {
        //Given
        when(aspspProfileService.isSigningBasketSupported())
            .thenReturn(true);
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.getSigningBasketMaxEntries())
            .thenReturn(5);

        CmsConsent cmsConsent = buildCmsConsent("1");

        AisConsent aisConsent = new AisConsent();
        aisConsent.setConsentData(AisConsentData.buildDefaultAisConsentData());
        aisConsent.setConsentStatus(ConsentStatus.RECEIVED);

        when(aisConsentMapper.mapToAisConsent(cmsConsent))
            .thenReturn(aisConsent);


        CreateSigningBasketRequest createSigningBasketRequest = new CreateSigningBasketRequest(Collections.emptyList(), List.of("1"), null, null, null);
        CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse = new CmsSigningBasketConsentsAndPaymentsResponse(Collections.singletonList(cmsConsent), Collections.emptyList());

        //When
        ValidationResult validationResult = createSigningBasketRequestValidator.validate(new CreateSigningBasketRequestObject(createSigningBasketRequest, cmsSigningBasketConsentsAndPaymentsResponse, PSU_DATA, true));

        //Then
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(REFERENCE_MIX_INVALID_VALIDATION_ERROR);
    }

    @Test
    void validate_signingBasketMultilevelVariations_shouldReturnErrorFromValidator() {
        //Given
        when(aspspProfileService.isSigningBasketSupported())
            .thenReturn(true);
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.getSigningBasketMaxEntries())
            .thenReturn(5);

        CmsConsent cmsConsent1 = buildCmsConsent("1");
        cmsConsent1.setMultilevelScaRequired(false);
        CmsConsent cmsConsent2 = buildCmsConsent("2");
        cmsConsent2.setMultilevelScaRequired(true);

        AisConsent aisConsent1 = buildAisConsent();
        AisConsent aisConsent2 = buildAisConsent();

        when(aisConsentMapper.mapToAisConsent(cmsConsent1))
            .thenReturn(aisConsent1);
        when(aisConsentMapper.mapToAisConsent(cmsConsent2))
            .thenReturn(aisConsent2);

        PisCommonPaymentResponse payment = new PisCommonPaymentResponse();
        payment.setExternalId("2");
        payment.setTransactionStatus(TransactionStatus.ACSP);
        payment.setMultilevelScaRequired(true);

        CreateSigningBasketRequest createSigningBasketRequest = new CreateSigningBasketRequest(Collections.singletonList("3"), List.of("1", "2"), null, null, null);
        CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse = new CmsSigningBasketConsentsAndPaymentsResponse(Arrays.asList(cmsConsent1, cmsConsent2), Collections.singletonList(payment));

        //When
        ValidationResult validationResult = createSigningBasketRequestValidator.validate(new CreateSigningBasketRequestObject(createSigningBasketRequest, cmsSigningBasketConsentsAndPaymentsResponse, PSU_DATA, true));

        //Then
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(REFERENCE_MIX_INVALID_VALIDATION_ERROR);
    }

    @Test
    void validate_signingBasketObjectIsBlocked_shouldReturnErrorFromValidator() {
        //Given
        when(aspspProfileService.isSigningBasketSupported())
            .thenReturn(true);
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.getSigningBasketMaxEntries())
            .thenReturn(5);
        CmsConsent cmsConsent = buildCmsConsent("1");
        AisConsent aisConsent = buildAisConsent();
        when(aisConsentMapper.mapToAisConsent(cmsConsent))
            .thenReturn(aisConsent);

        PisCommonPaymentResponse payment = new PisCommonPaymentResponse();
        payment.setExternalId("2");
        payment.setSigningBasketBlocked(true);
        payment.setTransactionStatus(TransactionStatus.ACSP);

        CreateSigningBasketRequest createSigningBasketRequest = new CreateSigningBasketRequest(Collections.singletonList("2"), List.of("1"), null, null, null);
        CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse = new CmsSigningBasketConsentsAndPaymentsResponse(Collections.singletonList(cmsConsent), Collections.singletonList(payment));

        //When
        ValidationResult validationResult = createSigningBasketRequestValidator.validate(new CreateSigningBasketRequestObject(createSigningBasketRequest, cmsSigningBasketConsentsAndPaymentsResponse, PSU_DATA, true));

        //Then
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(REFERENCE_MIX_INVALID_VALIDATION_ERROR);
    }

    @Test
    void validate_signingBasketObjectIsPartiallyAuthorised_shouldReturnErrorFromValidator() {
        //Given
        when(aspspProfileService.isSigningBasketSupported())
            .thenReturn(true);
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.getSigningBasketMaxEntries())
            .thenReturn(5);
        CmsConsent cmsConsent = new CmsConsent();
        cmsConsent.setConsentType(ConsentType.AIS);
        cmsConsent.setConsentStatus(ConsentStatus.VALID);
        cmsConsent.setId("1");
        AisConsent aisConsent = new AisConsent();
        AisConsentData consentData = new AisConsentData(AccountAccessType.ALL_ACCOUNTS, null, null, false);
        aisConsent.setConsentData(consentData);
        aisConsent.setConsentStatus(ConsentStatus.VALID);
        when(aisConsentMapper.mapToAisConsent(cmsConsent))
            .thenReturn(aisConsent);

        PisCommonPaymentResponse payment = new PisCommonPaymentResponse();
        payment.setExternalId("2");
        payment.setTransactionStatus(TransactionStatus.ACCC);

        CreateSigningBasketRequest createSigningBasketRequest = new CreateSigningBasketRequest(Collections.singletonList("2"), List.of("1"), null, null, null);
        CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse = new CmsSigningBasketConsentsAndPaymentsResponse(Collections.singletonList(cmsConsent), Collections.singletonList(payment));

        //When
        ValidationResult validationResult = createSigningBasketRequestValidator.validate(new CreateSigningBasketRequestObject(createSigningBasketRequest, cmsSigningBasketConsentsAndPaymentsResponse, PSU_DATA, true));

        //Then
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(REFERENCE_STATUS_INVALID_VALIDATION_ERROR);
    }

    @Test
    void validate_signingBasketObjectIsPartiallyAuthorisedAuthorisationFinalised_shouldReturnErrorFromValidator() {
        //Given
        when(aspspProfileService.isSigningBasketSupported())
            .thenReturn(true);
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.getSigningBasketMaxEntries())
            .thenReturn(5);
        CmsConsent cmsConsent = new CmsConsent();
        cmsConsent.setConsentType(ConsentType.AIS);
        cmsConsent.setConsentStatus(ConsentStatus.VALID);
        cmsConsent.setId("1");
        AisConsent aisConsent = new AisConsent();
        AisConsentData consentData = new AisConsentData(AccountAccessType.ALL_ACCOUNTS, null, null, false);
        aisConsent.setConsentData(consentData);
        aisConsent.setConsentStatus(ConsentStatus.VALID);
        when(aisConsentMapper.mapToAisConsent(cmsConsent))
            .thenReturn(aisConsent);

        PisCommonPaymentResponse payment = new PisCommonPaymentResponse();
        Authorisation authorisation = new Authorisation();
        authorisation.setScaStatus(ScaStatus.FINALISED);
        payment.setAuthorisations(Collections.singletonList(authorisation));
        payment.setExternalId("2");
        payment.setTransactionStatus(TransactionStatus.ACCP);

        CreateSigningBasketRequest createSigningBasketRequest = new CreateSigningBasketRequest(Collections.singletonList("2"), List.of("1"), null, null, null);
        CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse = new CmsSigningBasketConsentsAndPaymentsResponse(Collections.singletonList(cmsConsent), Collections.singletonList(payment));

        //When
        ValidationResult validationResult = createSigningBasketRequestValidator.validate(new CreateSigningBasketRequestObject(createSigningBasketRequest, cmsSigningBasketConsentsAndPaymentsResponse, PSU_DATA, true));

        //Then
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(REFERENCE_STATUS_INVALID_VALIDATION_ERROR);
    }

    @Test
    void validate_signingBasketValid() {
        //Given
        when(aspspProfileService.isSigningBasketSupported())
            .thenReturn(true);
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.getSigningBasketMaxEntries())
            .thenReturn(5);
        CmsConsent cmsConsent = new CmsConsent();
        cmsConsent.setConsentType(ConsentType.AIS);
        cmsConsent.setConsentStatus(ConsentStatus.VALID);
        cmsConsent.setId("1");
        AisConsent aisConsent = new AisConsent();
        AisConsentData consentData = new AisConsentData(AccountAccessType.ALL_ACCOUNTS, null, null, false);
        aisConsent.setConsentData(consentData);
        aisConsent.setConsentStatus(ConsentStatus.VALID);
        when(aisConsentMapper.mapToAisConsent(cmsConsent))
            .thenReturn(aisConsent);

        PisCommonPaymentResponse payment = new PisCommonPaymentResponse();
        payment.setExternalId("2");
        payment.setTransactionStatus(TransactionStatus.ACCP);

        CreateSigningBasketRequest createSigningBasketRequest = new CreateSigningBasketRequest(Collections.singletonList("2"), List.of("1"), null, null, null);
        CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse = new CmsSigningBasketConsentsAndPaymentsResponse(Collections.singletonList(cmsConsent), Collections.singletonList(payment));

        //When
        ValidationResult validationResult = createSigningBasketRequestValidator.validate(new CreateSigningBasketRequestObject(createSigningBasketRequest, cmsSigningBasketConsentsAndPaymentsResponse, PSU_DATA, true));

        //Then
        assertThat(validationResult.isValid()).isTrue();
    }

    private AisConsent buildAisConsent() {
        AisConsent aisConsent = new AisConsent();
        AisConsentData consentData = new AisConsentData(AccountAccessType.ALL_ACCOUNTS, null, null, false);
        aisConsent.setConsentData(consentData);
        aisConsent.setConsentStatus(ConsentStatus.RECEIVED);
        return aisConsent;
    }

    private CmsConsent buildCmsConsent(String id) {
        CmsConsent cmsConsent = new CmsConsent();
        cmsConsent.setSigningBasketBlocked(true);
        cmsConsent.setConsentStatus(ConsentStatus.RECEIVED);
        cmsConsent.setId(id);
        cmsConsent.setConsentType(ConsentType.AIS);
        return cmsConsent;
    }
}
