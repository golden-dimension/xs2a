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

package de.adorsys.psd2.xs2a.web.validator.body;

import de.adorsys.psd2.validator.payment.config.Occurrence;
import de.adorsys.psd2.validator.payment.config.ValidationObject;
import de.adorsys.psd2.xs2a.core.domain.MessageCategory;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.web.validator.ErrorBuildingService;
import de.adorsys.psd2.xs2a.web.validator.header.ErrorBuildingServiceMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FieldLengthValidatorTest {
    private FieldLengthValidator validator;
    private MessageError messageError;

    @BeforeEach
    void setUp() {
        messageError = new MessageError();
        ErrorBuildingService errorBuildingService = new ErrorBuildingServiceMock(ErrorType.AIS_400);
        validator = new FieldLengthValidator(errorBuildingService);
    }

    @Test
    void checkFieldForMaxLength_extraField() {
        // Given
        String fieldToCheck = "fieldToCheck";
        String fieldName = "fieldName";
        ValidationObject validationObject = new ValidationObject(Occurrence.NONE, 0);

        // When
        validator.checkFieldForMaxLength(fieldToCheck, fieldName, validationObject, messageError);

        // Then
        assertFalse(messageError.getTppMessages().isEmpty());
        assertEquals(MessageCategory.ERROR, messageError.getTppMessage().getCategory());
        assertEquals(MessageErrorCode.FORMAT_ERROR_EXTRA_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[] {fieldName}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void checkFieldForMaxLength_emptyField() {
        // Given
        String fieldToCheck = "";
        String fieldName = "fieldName";
        ValidationObject validationObject = new ValidationObject(Occurrence.REQUIRED, 50);

        // When
        validator.checkFieldForMaxLength(fieldToCheck, fieldName, validationObject, messageError);

        // Then
        assertFalse(messageError.getTppMessages().isEmpty());
        assertEquals(MessageCategory.ERROR, messageError.getTppMessage().getCategory());
        assertEquals(MessageErrorCode.FORMAT_ERROR_EMPTY_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[] {fieldName}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void checkFieldForMaxLength_oversizedField() {
        // Given
        String fieldToCheck = "fieldToCheck";
        String fieldName = "fieldName";
        ValidationObject validationObject = new ValidationObject(Occurrence.REQUIRED, 1);

        // When
        validator.checkFieldForMaxLength(fieldToCheck, fieldName, validationObject, messageError);

        // Then
        assertFalse(messageError.getTppMessages().isEmpty());
        assertEquals(MessageCategory.ERROR, messageError.getTppMessage().getCategory());
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[] {fieldName, 1}, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void checkFieldForMaxLength_requiredOk() {
        // Given
        String fieldToCheck = "fieldToCheck";
        String fieldName = "fieldName";
        ValidationObject validationObject = new ValidationObject(Occurrence.REQUIRED, 19);

        // When
        validator.checkFieldForMaxLength(fieldToCheck, fieldName, validationObject, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());

    }

    @Test
    void checkFieldForMaxLength_optionalOk() {
        // Given
        String fieldToCheck = "fieldToCheck";
        String fieldName = "fieldName";
        ValidationObject validationObject = new ValidationObject(Occurrence.OPTIONAL, 19);

        // When
        validator.checkFieldForMaxLength(fieldToCheck, fieldName, validationObject, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());

    }

    @Test
    void checkFieldForMaxLength_optionalBlank() {
        // Given
        String fieldToCheck = "";
        String fieldName = "fieldName";
        ValidationObject validationObject = new ValidationObject(Occurrence.OPTIONAL, 19);

        // When
        validator.checkFieldForMaxLength(fieldToCheck, fieldName, validationObject, messageError);

        // Then
        assertTrue(messageError.getTppMessages().isEmpty());

    }

    @Test
    void checkFieldForMaxLength_oversizedOptionalField() {
        // Given
        String fieldToCheck = "fieldToCheck";
        String fieldName = "fieldName";
        ValidationObject validationObject = new ValidationObject(Occurrence.OPTIONAL, 1);

        // When
        validator.checkFieldForMaxLength(fieldToCheck, fieldName, validationObject, messageError);

        // Then
        assertFalse(messageError.getTppMessages().isEmpty());
        assertEquals(MessageCategory.ERROR, messageError.getTppMessage().getCategory());
        assertEquals(MessageErrorCode.FORMAT_ERROR_OVERSIZE_FIELD, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(new Object[] {fieldName, 1}, messageError.getTppMessage().getTextParameters());
    }
}
