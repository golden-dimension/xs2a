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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PsuDataInInitialRequestValidatorTest {
    private static final PsuIdData EMPTY_PSU_DATA = new PsuIdData(null, null, null, null, null);
    private static final PsuIdData PSU_DATA = new PsuIdData("some psu id", null, null, null, null);
    private static final MessageError BLANK_PSU_ID_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(FORMAT_ERROR_PSU_ID_BLANK));
    private static final MessageError NULL_PSU_ID_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(FORMAT_ERROR_NO_PSU_ID));
    private static final ServiceType SERVICE_TYPE = ServiceType.AIS;

    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    @Mock
    private ServiceTypeToErrorTypeMapper errorTypeMapper;

    @InjectMocks
    private PsuDataInInitialRequestValidator psuDataInInitialRequestValidator;

    @Test
    void validate_withPsuDataNotMandated_shouldReturnValid() {
        //When
        ValidationResult validationResult = psuDataInInitialRequestValidator.validate(EMPTY_PSU_DATA);

        //Then
        assertTrue(validationResult.isValid());
    }

    @Test
    void validate_withPsuDataMandatedAndValidPsuData_shouldReturnValid() {
        //Given
        when(aspspProfileService.isPsuInInitialRequestMandated()).thenReturn(true);

        //When
        ValidationResult validationResult = psuDataInInitialRequestValidator.validate(PSU_DATA);

        //Then
        assertTrue(validationResult.isValid());
    }

    @Test
    void validate_withPsuDataMandatedAndBlankPsuId_shouldReturnFormatError() {
        //Given
        when(aspspProfileService.isPsuInInitialRequestMandated()).thenReturn(true);

        PsuIdData psuIdData = new PsuIdData(" ", null, null, null, null);
        when(serviceTypeDiscoveryService.getServiceType()).thenReturn(SERVICE_TYPE);
        when(errorTypeMapper.mapToErrorType(SERVICE_TYPE, FORMAT_ERROR.getCode())).thenReturn(ErrorType.AIS_400);

        //When
        ValidationResult validationResult = psuDataInInitialRequestValidator.validate(psuIdData);

        //Then
        verify(serviceTypeDiscoveryService).getServiceType();
        verify(errorTypeMapper).mapToErrorType(SERVICE_TYPE, FORMAT_ERROR.getCode());

        assertTrue(validationResult.isNotValid());
        assertEquals(BLANK_PSU_ID_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withPsuDataMandatedAndNullPsuId_shouldReturnFormatError() {
        //Given
        when(aspspProfileService.isPsuInInitialRequestMandated()).thenReturn(true);
        PsuIdData psuIdData = new PsuIdData(null, null, null, null, null);

        when(serviceTypeDiscoveryService.getServiceType()).thenReturn(SERVICE_TYPE);
        when(errorTypeMapper.mapToErrorType(SERVICE_TYPE, FORMAT_ERROR.getCode())).thenReturn(ErrorType.AIS_400);

        //When
        ValidationResult validationResult = psuDataInInitialRequestValidator.validate(psuIdData);

        //Then
        verify(serviceTypeDiscoveryService).getServiceType();
        verify(errorTypeMapper).mapToErrorType(SERVICE_TYPE, FORMAT_ERROR.getCode());

        assertTrue(validationResult.isNotValid());
        assertEquals(NULL_PSU_ID_ERROR, validationResult.getMessageError());
    }
}
