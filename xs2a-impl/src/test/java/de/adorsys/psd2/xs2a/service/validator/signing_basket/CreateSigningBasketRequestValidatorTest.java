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

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.signing_basket.SigningBasketReq;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.PsuDataInInitialRequestValidator;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.signing_basket.dto.CreateSigningBasketRequestObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.SERVICE_INVALID_405;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateSigningBasketRequestValidatorTest {
    private static final PsuIdData EMPTY_PSU_DATA = new PsuIdData(null, null, null, null, null);
    private static final MessageError PSU_DATA_VALIDATION_ERROR =
        new MessageError(ErrorType.SB_400, TppMessageInformation.of(FORMAT_ERROR));
    private static final MessageError MAX_ENTRIES_VALIDATION_ERROR =
        new MessageError(ErrorType.SB_400, TppMessageInformation.buildWithCustomError(FORMAT_ERROR, "Number of entries in Signing Basket should not exceed more than " + 5));
    private static final MessageError SIGNING_BASKET_NOT_SUPPORTED_VALIDATION_ERROR =
        new MessageError(ErrorType.SB_405, TppMessageInformation.buildWithCustomError(SERVICE_INVALID_405, "Signing basket is not supported by ASPSP"));

    @InjectMocks
    private CreateSigningBasketRequestValidator createSigningBasketRequestValidator;
    @Mock
    private PsuDataInInitialRequestValidator psuDataInInitialRequestValidator;
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;

    @Test
    void validate_withInvalidPsuData_shouldReturnErrorFromValidator() {
        //Given
        when(aspspProfileService.isSigningBasketSupported())
            .thenReturn(true);
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.invalid(PSU_DATA_VALIDATION_ERROR));
        SigningBasketReq signingBasketReq = new SigningBasketReq();

        //When
        ValidationResult validationResult = createSigningBasketRequestValidator.validate(new CreateSigningBasketRequestObject(signingBasketReq, EMPTY_PSU_DATA));

        //Then
        verify(psuDataInInitialRequestValidator).validate(EMPTY_PSU_DATA);
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(PSU_DATA_VALIDATION_ERROR);
    }

    @Test
    void validate_signingBasketMaxEntries_shouldReturnErrorFromValidator() {
        when(aspspProfileService.isSigningBasketSupported())
            .thenReturn(true);
        when(psuDataInInitialRequestValidator.validate(any(PsuIdData.class)))
            .thenReturn(ValidationResult.valid());
        when(aspspProfileService.getSigningBasketMaxEntries())
            .thenReturn(5);

        SigningBasketReq signingBasketReq = new SigningBasketReq();
        signingBasketReq.setConsentIds(List.of("1", "2", "3"));
        signingBasketReq.setPaymentIds(List.of("4", "5", "6"));

        //When
        ValidationResult validationResult = createSigningBasketRequestValidator.validate(new CreateSigningBasketRequestObject(signingBasketReq, EMPTY_PSU_DATA));

        //Then
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(MAX_ENTRIES_VALIDATION_ERROR);
    }

    @Test
    void validate_signingBasketIsNotSupported_shouldReturnErrorFromValidator() {
        when(aspspProfileService.isSigningBasketSupported())
            .thenReturn(false);

        SigningBasketReq signingBasketReq = new SigningBasketReq();

        //When
        ValidationResult validationResult = createSigningBasketRequestValidator.validate(new CreateSigningBasketRequestObject(signingBasketReq, EMPTY_PSU_DATA));

        //Then
        assertThat(validationResult.isNotValid()).isTrue();
        assertThat(validationResult.getMessageError()).isEqualTo(SIGNING_BASKET_NOT_SUPPORTED_VALIDATION_ERROR);
    }
}
