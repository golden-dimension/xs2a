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

package de.adorsys.psd2.xs2a.web.validator.header;

import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ContentType;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ConsentIdHeaderValidatorImplTest {
    private static final String[] CONSENT_ID_HEADER_NAME = {"consent-id"};

    private ConsentIdHeaderValidatorImpl validator;
    private MessageError messageError;
    private Map<String, String> headers;

    @BeforeEach
    void setUp() {
        validator = new ConsentIdHeaderValidatorImpl(new ErrorBuildingServiceMock(ErrorType.AIS_400));
        messageError = new MessageError();
        headers = new HashMap<>();
    }

    @Test
    void validate_success() {
        headers.put(validator.getHeaderName(), ContentType.JSON.getType());
        validator.validate(headers, messageError);
        assertTrue(messageError.getTppMessages().isEmpty());
    }

    @Test
    void validate_absentHeaderError() {
        validator.validate(headers, messageError);

        assertEquals(MessageErrorCode.FORMAT_ERROR_ABSENT_HEADER, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(CONSENT_ID_HEADER_NAME, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void validate_nullHeaderError() {
        headers.put(validator.getHeaderName(), null);
        validator.validate(headers, messageError);

        assertEquals(MessageErrorCode.FORMAT_ERROR_NULL_HEADER, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(CONSENT_ID_HEADER_NAME, messageError.getTppMessage().getTextParameters());
    }

    @Test
    void validate_blankHeaderError() {
        headers.put(validator.getHeaderName(), "");
        validator.validate(headers, messageError);

        assertEquals(MessageErrorCode.FORMAT_ERROR_BLANK_HEADER, messageError.getTppMessage().getMessageErrorCode());
        assertArrayEquals(CONSENT_ID_HEADER_NAME, messageError.getTppMessage().getTextParameters());
    }

}
