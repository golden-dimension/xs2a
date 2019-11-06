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

package de.adorsys.psd2.xs2a.service.validator.pis.payment;

import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TransactionStatusAcceptHeaderValidatorTest {
    private static final String JSON_MEDIA_TYPE = "application/json";
    private static final String ACCEPT_HEADER_JSON_XML = "application/json,application/xml";
    private static final String ACCEPT_HEADER_JSON = "application/json";
    private static final String ACCEPT_HEADER_XML = "application/xml";
    private static final String ACCEPT_HEADER_WILDCARD = "*/*";

    private static final MessageError VALIDATION_ERROR = new MessageError(ErrorType.PIS_406, TppMessageInformation.of(MessageErrorCode.REQUESTED_FORMATS_INVALID));

    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;

    @InjectMocks
    private TransactionStatusAcceptHeaderValidator transactionStatusAcceptHeaderValidator;

    @Test
    public void validate_withSupportedHeader_shouldReturnValid() {
        // Given
        when(aspspProfileServiceWrapper.getSupportedTransactionStatusFormats())
            .thenReturn(Collections.singletonList(JSON_MEDIA_TYPE));

        // When
        ValidationResult validationResult = transactionStatusAcceptHeaderValidator.validate(ACCEPT_HEADER_JSON);

        // Then
        verify(aspspProfileServiceWrapper).getSupportedTransactionStatusFormats();

        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validate_withMixedSupportedHeader_shouldReturnValid() {
        // Given
        when(aspspProfileServiceWrapper.getSupportedTransactionStatusFormats())
            .thenReturn(Collections.singletonList(JSON_MEDIA_TYPE));

        // When
        ValidationResult validationResult = transactionStatusAcceptHeaderValidator.validate(ACCEPT_HEADER_JSON_XML);

        // Then
        verify(aspspProfileServiceWrapper).getSupportedTransactionStatusFormats();

        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validate_withWildcardHeader_shouldReturnValid() {
        // When
        ValidationResult validationResult = transactionStatusAcceptHeaderValidator.validate(ACCEPT_HEADER_WILDCARD);

        // Then
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validate_withEmptyListInProfile_shouldReturnValid() {
        // Given
        when(aspspProfileServiceWrapper.getSupportedTransactionStatusFormats())
            .thenReturn(Collections.emptyList());

        // When
        ValidationResult validationResult = transactionStatusAcceptHeaderValidator.validate(ACCEPT_HEADER_JSON_XML);

        // Then
        verify(aspspProfileServiceWrapper).getSupportedTransactionStatusFormats();

        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    public void validate_withNotSupportedHeader_shouldReturnValidationError() {
        // Given
        when(aspspProfileServiceWrapper.getSupportedTransactionStatusFormats())
            .thenReturn(Collections.singletonList(JSON_MEDIA_TYPE));

        // When
        ValidationResult validationResult = transactionStatusAcceptHeaderValidator.validate(ACCEPT_HEADER_XML);

        // Then
        verify(aspspProfileServiceWrapper).getSupportedTransactionStatusFormats();

        assertTrue(validationResult.isNotValid());
        assertEquals(VALIDATION_ERROR, validationResult.getMessageError());
    }
}
