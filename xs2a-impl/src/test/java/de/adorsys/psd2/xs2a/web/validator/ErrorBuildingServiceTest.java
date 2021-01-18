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

package de.adorsys.psd2.xs2a.web.validator;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorMapperContainer;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ErrorBuildingServiceTest {

    private static final String RESPONSE_TEXT = "some response text";
    private static final int STATUS_CODE_400 = 400;
    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;
    private static final ServiceType SERVICE_TYPE_PIS = ServiceType.PIS;
    private static final ErrorType ERROR_PIS_400 = ErrorType.PIS_400;

    @InjectMocks
    private ErrorBuildingService errorBuildingService;

    @Mock
    private ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    @Mock
    private ServiceTypeToErrorTypeMapper errorTypeMapper;
    @Mock
    private ErrorMapperContainer errorMapperContainer;
    @Mock
    private Xs2aObjectMapper xs2aObjectMapper;
    private MockHttpServletResponse response;

    @Captor
    private ArgumentCaptor<MessageError> messageErrorCaptor;

    @BeforeEach
    void setUp() throws Exception {
        response = new MockHttpServletResponse();

        when(serviceTypeDiscoveryService.getServiceType()).thenReturn(SERVICE_TYPE_PIS);
        when(errorTypeMapper.mapToErrorType(any(), any(Integer.class))).thenReturn(ERROR_PIS_400);
        when(xs2aObjectMapper.writeValueAsString(any())).thenReturn(RESPONSE_TEXT);
        when(errorMapperContainer.getErrorBody(messageErrorCaptor.capture())).thenReturn(null);
    }

    @Test
    void buildErrorResponse_success() throws IOException {
        // Given
        ValidationResult validationResult = buildValidationResult(MessageErrorCode.FORMAT_ERROR);

        // When
        errorBuildingService.buildFormatErrorResponse(response, validationResult.getMessageError());

        // Then
        String outputMessage = response.getContentAsString();
        assertNotNull(outputMessage);
        assertEquals(RESPONSE_TEXT, outputMessage);
        assertEquals(STATUS_CODE_400, response.getStatus());
        assertEquals(JSON, response.getContentType());

        MessageError actualMessageError = messageErrorCaptor.getValue();
        assertEquals(ERROR_PIS_400, actualMessageError.getErrorType());
        assertEquals(1, actualMessageError.getTppMessages().size());
        assertEquals(MessageErrorCode.FORMAT_ERROR, actualMessageError.getTppMessages().iterator().next().getMessageErrorCode());
    }

    @Test
    void buildErrorResponse_executionDateInvalid() throws IOException {
        // Given
        ValidationResult validationResult = buildValidationResult(MessageErrorCode.EXECUTION_DATE_INVALID);

        // When
        errorBuildingService.buildFormatErrorResponse(response, validationResult.getMessageError());

        // Then
        String outputMessage = response.getContentAsString();
        assertNotNull(outputMessage);
        assertEquals(RESPONSE_TEXT, outputMessage);
        assertEquals(STATUS_CODE_400, response.getStatus());
        assertEquals(JSON, response.getContentType());
        assertEquals(JSON, response.getContentType());

        MessageError actualMessageError = messageErrorCaptor.getValue();
        assertEquals(ERROR_PIS_400, actualMessageError.getErrorType());
        assertEquals(1, actualMessageError.getTppMessages().size());
        assertEquals(MessageErrorCode.EXECUTION_DATE_INVALID, actualMessageError.getTppMessages().iterator().next().getMessageErrorCode());
    }

    private ValidationResult buildValidationResult(MessageErrorCode messageErrorCode) {
        return ValidationResult.invalid(ERROR_PIS_400, messageErrorCode);
    }
}
