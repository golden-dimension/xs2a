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

package de.adorsys.psd2.xs2a.service.validator.pis.authorisation.cancellation;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.Xs2aScaStatus;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.service.validator.PisEndpointAccessCheckerService;
import de.adorsys.psd2.xs2a.service.validator.PisPsuDataUpdateAuthorisationCheckerValidator;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationStageCheckValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.PisAuthorisationStatusValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.PisAuthorisationValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.initiation.UpdatePaymentPsuDataPO;
import de.adorsys.psd2.xs2a.service.validator.tpp.PisTppInfoValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.*;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationServiceType.PIS_CANCELLATION;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePisCancellationPsuDataValidatorTest {
    private static final String MESSAGE_ERROR_NO_PSU = "Please provide the PSU identification data";

    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");
    private static final String AUTHORISATION_ID = "authorisation id";
    private static final String INVALID_AUTHORISATION_ID_FOR_ENDPOINT = "invalid authorisation id for endpoint";
    private static final String INVALID_AUTHORISATION_ID = "invalid authorisation id";

    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(ErrorType.PIS_401, TppMessageInformation.of(UNAUTHORIZED));
    private static final MessageError BLOCKED_ENDPOINT_ERROR = new MessageError(PIS_403, of(SERVICE_BLOCKED));
    private static final MessageError INVALID_AUTHORISATION_ERROR = new MessageError(PIS_403, of(RESOURCE_UNKNOWN_403));
    private static final MessageError STATUS_VALIDATION_ERROR = new MessageError(PIS_409, of(STATUS_INVALID));
    private static final MessageError SCA_INVALID_ERROR = new MessageError(AIS_400, of(SCA_INVALID));
    private static final MessageError FORMAT_BOTH_PSUS_ABSENT_ERROR = new MessageError(ErrorType.PIS_400, of(FORMAT_ERROR, MESSAGE_ERROR_NO_PSU));
    private static final MessageError CREDENTIALS_INVALID_ERROR = new MessageError(PIS_401, of(PSU_CREDENTIALS_INVALID));
    private static final MessageError PIS_SERVICE_INVALID = new MessageError(PIS_400, of(SERVICE_INVALID_400));

    private static final MessageError PAYMENT_PRODUCT_VALIDATION_ERROR =
        new MessageError(ErrorType.PIS_403, TppMessageInformation.of(PRODUCT_INVALID_FOR_PAYMENT));

    private static final String CORRECT_PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String WRONG_PAYMENT_PRODUCT = "sepa-credit-transfers111";

    private static final String CONFIRMATION_CODE = "confirmation code";
    private static final boolean CONFIRMATION_CODE_RECEIVED_FALSE = false;
    private static final boolean CONFIRMATION_CODE_RECEIVED_TRUE = true;

    private static final PsuIdData PSU_ID_DATA_1 = new PsuIdData("psu-id", null, null, null, null);
    private static final PsuIdData PSU_ID_DATA_2 = new PsuIdData("psu-id-2", null, null, null, null);

    @Mock
    private PisTppInfoValidator pisTppInfoValidator;
    @Mock
    private PisEndpointAccessCheckerService pisEndpointAccessCheckerService;
    @Mock
    private PisAuthorisationValidator pisAuthorisationValidator;
    @Mock
    private PisAuthorisationStatusValidator pisAuthorisationStatusValidator;
    @Mock
    private PisPsuDataUpdateAuthorisationCheckerValidator pisPsuDataUpdateAuthorisationCheckerValidator;
    @Mock
    private AuthorisationStageCheckValidator authorisationStageCheckValidator;

    @InjectMocks
    private UpdatePisCancellationPsuDataValidator updatePisCancellationPsuDataValidator;

    @BeforeEach
    void setUp() {
        // Inject pisTppInfoValidator via setter
        updatePisCancellationPsuDataValidator.setPisValidators(pisTppInfoValidator);
    }

    @Test
    void validate_withValidPaymentObject_shouldReturnValid() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO, Xs2aScaStatus.RECEIVED);
        when(pisAuthorisationValidator.validate(AUTHORISATION_ID, commonPaymentResponse))
            .thenReturn(ValidationResult.valid());
        when(pisAuthorisationStatusValidator.validate(Xs2aScaStatus.RECEIVED, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(ValidationResult.valid());

        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);
        when(pisPsuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, PSU_ID_DATA_1))
            .thenReturn(ValidationResult.valid());
        when(authorisationStageCheckValidator.validate(any(), any(), eq(PIS_CANCELLATION)))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = updatePisCancellationPsuDataValidator.validate(new UpdatePaymentPsuDataPO(commonPaymentResponse, buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1)));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidPaymentProduct_shouldReturnPaymentProductValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO, Xs2aScaStatus.RECEIVED);
        commonPaymentResponse.setPaymentProduct(WRONG_PAYMENT_PRODUCT);
        // When
        ValidationResult validationResult = updatePisCancellationPsuDataValidator.validate(new UpdatePaymentPsuDataPO(commonPaymentResponse, buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1)));

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(PAYMENT_PRODUCT_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidTppInPayment_shouldReturnTppValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(INVALID_TPP_INFO, Xs2aScaStatus.RECEIVED);
        when(pisTppInfoValidator.validateTpp(INVALID_TPP_INFO)).thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = updatePisCancellationPsuDataValidator.validate(new UpdatePaymentPsuDataPO(commonPaymentResponse, buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1)));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidAuthorisationForEndpoint_shouldReturnValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO, Xs2aScaStatus.RECEIVED);
        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(pisEndpointAccessCheckerService.isEndpointAccessible(INVALID_AUTHORISATION_ID_FOR_ENDPOINT, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(false);

        // When
        ValidationResult validationResult = updatePisCancellationPsuDataValidator.validate(new UpdatePaymentPsuDataPO(commonPaymentResponse, buildUpdateRequest(INVALID_AUTHORISATION_ID_FOR_ENDPOINT, PSU_ID_DATA_1)));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(BLOCKED_ENDPOINT_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidTppAndInvalidAuthorisationForEndpoint_shouldReturnTppValidationErrorFirst() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(INVALID_TPP_INFO, Xs2aScaStatus.RECEIVED);
        when(pisTppInfoValidator.validateTpp(INVALID_TPP_INFO)).thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = updatePisCancellationPsuDataValidator.validate(new UpdatePaymentPsuDataPO(commonPaymentResponse, buildUpdateRequest(INVALID_AUTHORISATION_ID_FOR_ENDPOINT, PSU_ID_DATA_1)));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidAuthorisation_shouldReturnAuthorisationValidationError() {
        // Given
        when(pisEndpointAccessCheckerService.isEndpointAccessible(INVALID_AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO, Xs2aScaStatus.RECEIVED);
        when(pisAuthorisationValidator.validate(INVALID_AUTHORISATION_ID, commonPaymentResponse))
            .thenReturn(ValidationResult.invalid(INVALID_AUTHORISATION_ERROR));

        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = updatePisCancellationPsuDataValidator.validate(new UpdatePaymentPsuDataPO(commonPaymentResponse, buildUpdateRequest(INVALID_AUTHORISATION_ID, PSU_ID_DATA_1)));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(INVALID_AUTHORISATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidScaStatus_shouldReturnStatusValidationError() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO, Xs2aScaStatus.FAILED);
        when(pisAuthorisationValidator.validate(AUTHORISATION_ID, commonPaymentResponse))
            .thenReturn(ValidationResult.valid());
        when(pisAuthorisationStatusValidator.validate(Xs2aScaStatus.FAILED, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(ValidationResult.invalid(STATUS_VALIDATION_ERROR));

        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);
        when(pisPsuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, PSU_ID_DATA_1))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = updatePisCancellationPsuDataValidator.validate(new UpdatePaymentPsuDataPO(commonPaymentResponse, buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1)));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(STATUS_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidScaStatusInConfirmationAuthorisationFlow_shouldReturnScaInvalid() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO, Xs2aScaStatus.FAILED);
        when(pisAuthorisationValidator.validate(AUTHORISATION_ID, commonPaymentResponse))
            .thenReturn(ValidationResult.valid());
        when(pisAuthorisationStatusValidator.validate(Xs2aScaStatus.FAILED, CONFIRMATION_CODE_RECEIVED_TRUE))
            .thenReturn(ValidationResult.invalid(SCA_INVALID_ERROR));

        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_TRUE))
            .thenReturn(true);
        when(pisPsuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, PSU_ID_DATA_1))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = updatePisCancellationPsuDataValidator.validate(new UpdatePaymentPsuDataPO(commonPaymentResponse, buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1, CONFIRMATION_CODE)));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(SCA_INVALID_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withNoAuthorisation_shouldReturnAuthorisationValidationError() {
        //Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO, Xs2aScaStatus.RECEIVED, INVALID_AUTHORISATION_ID, PSU_ID_DATA_1);
        when(pisAuthorisationValidator.validate(AUTHORISATION_ID, commonPaymentResponse))
            .thenReturn(ValidationResult.valid());
        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);

        //When
        ValidationResult validationResult = updatePisCancellationPsuDataValidator.validate(new UpdatePaymentPsuDataPO(commonPaymentResponse, buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1)));

        //Then
        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(INVALID_AUTHORISATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withBothPsusAbsent_shouldReturnFormatError() {
        //Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO, Xs2aScaStatus.RECEIVED, AUTHORISATION_ID, null);
        PsuIdData psuIdData = new PsuIdData(null, null, null, null, null);

        when(pisAuthorisationValidator.validate(AUTHORISATION_ID, commonPaymentResponse))
            .thenReturn(ValidationResult.valid());
        when(pisPsuDataUpdateAuthorisationCheckerValidator.validate(psuIdData, null))
            .thenReturn(ValidationResult.invalid(FORMAT_BOTH_PSUS_ABSENT_ERROR));

        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);

        ValidationResult validationResult = updatePisCancellationPsuDataValidator.validate(new UpdatePaymentPsuDataPO(commonPaymentResponse, buildUpdateRequest(AUTHORISATION_ID, psuIdData)));

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(FORMAT_BOTH_PSUS_ABSENT_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_cantPsuUpdateAuthorisation_shouldReturnCredentialsInvalidError() {
        //Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO, Xs2aScaStatus.RECEIVED, AUTHORISATION_ID, PSU_ID_DATA_2);

        when(pisAuthorisationValidator.validate(AUTHORISATION_ID, commonPaymentResponse))
            .thenReturn(ValidationResult.valid());
        when(pisPsuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, PSU_ID_DATA_2))
            .thenReturn(ValidationResult.invalid(CREDENTIALS_INVALID_ERROR));

        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);

        ValidationResult validationResult = updatePisCancellationPsuDataValidator.validate(new UpdatePaymentPsuDataPO(commonPaymentResponse, buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1)));

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(CREDENTIALS_INVALID_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_validationFailure_wrongAuthorisationStageDataProvided_received() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO, Xs2aScaStatus.RECEIVED);
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1);
        when(pisAuthorisationValidator.validate(AUTHORISATION_ID, commonPaymentResponse))
            .thenReturn(ValidationResult.valid());
        when(pisAuthorisationStatusValidator.validate(Xs2aScaStatus.RECEIVED, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(ValidationResult.valid());
        when(authorisationStageCheckValidator.validate(updateRequest, Xs2aScaStatus.RECEIVED, PIS_CANCELLATION))
            .thenReturn(ValidationResult.invalid(PIS_400, SERVICE_INVALID_400));

        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);
        when(pisPsuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, PSU_ID_DATA_1))
            .thenReturn(ValidationResult.valid());


        // When
        ValidationResult validationResult = updatePisCancellationPsuDataValidator.validate(new UpdatePaymentPsuDataPO(commonPaymentResponse, updateRequest));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(PIS_SERVICE_INVALID, validationResult.getMessageError());
    }

    @Test
    void validate_validationFailure_wrongAuthorisationStageDataProvided_psuidentified() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO, Xs2aScaStatus.PSUIDENTIFIED);
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1);
        when(pisAuthorisationValidator.validate(AUTHORISATION_ID, commonPaymentResponse))
            .thenReturn(ValidationResult.valid());
        when(pisAuthorisationStatusValidator.validate(Xs2aScaStatus.PSUIDENTIFIED, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(ValidationResult.valid());
        when(authorisationStageCheckValidator.validate(updateRequest, Xs2aScaStatus.PSUIDENTIFIED, PIS_CANCELLATION))
            .thenReturn(ValidationResult.invalid(PIS_400, SERVICE_INVALID_400));

        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);
        when(pisPsuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, PSU_ID_DATA_1))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = updatePisCancellationPsuDataValidator.validate(new UpdatePaymentPsuDataPO(commonPaymentResponse, updateRequest));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(PIS_SERVICE_INVALID, validationResult.getMessageError());
    }

    @Test
    void validate_validationFailure_wrongAuthorisationStageDataProvided_psuauthenticated() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO, Xs2aScaStatus.PSUAUTHENTICATED);
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1);
        when(pisAuthorisationValidator.validate(AUTHORISATION_ID, commonPaymentResponse))
            .thenReturn(ValidationResult.valid());
        when(pisAuthorisationStatusValidator.validate(Xs2aScaStatus.PSUAUTHENTICATED, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(ValidationResult.valid());
        when(authorisationStageCheckValidator.validate(updateRequest, Xs2aScaStatus.PSUAUTHENTICATED, PIS_CANCELLATION))
            .thenReturn(ValidationResult.invalid(PIS_400, SERVICE_INVALID_400));

        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);
        when(pisPsuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, PSU_ID_DATA_1))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = updatePisCancellationPsuDataValidator.validate(new UpdatePaymentPsuDataPO(commonPaymentResponse, updateRequest));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(PIS_SERVICE_INVALID, validationResult.getMessageError());
    }

    @Test
    void validate_validationFailure_wrongAuthorisationStageDataProvided_scamethodselected() {
        // Given
        PisCommonPaymentResponse commonPaymentResponse = buildPisCommonPaymentResponse(TPP_INFO, Xs2aScaStatus.SCAMETHODSELECTED);
        Xs2aUpdatePisCommonPaymentPsuDataRequest updateRequest = buildUpdateRequest(AUTHORISATION_ID, PSU_ID_DATA_1);
        when(pisAuthorisationValidator.validate(AUTHORISATION_ID, commonPaymentResponse))
            .thenReturn(ValidationResult.valid());
        when(pisAuthorisationStatusValidator.validate(Xs2aScaStatus.SCAMETHODSELECTED, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(ValidationResult.valid());
        when(authorisationStageCheckValidator.validate(updateRequest, Xs2aScaStatus.SCAMETHODSELECTED, PIS_CANCELLATION))
            .thenReturn(ValidationResult.invalid(PIS_400, SERVICE_INVALID_400));

        when(pisTppInfoValidator.validateTpp(TPP_INFO)).thenReturn(ValidationResult.valid());

        when(pisEndpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONFIRMATION_CODE_RECEIVED_FALSE))
            .thenReturn(true);
        when(pisPsuDataUpdateAuthorisationCheckerValidator.validate(PSU_ID_DATA_1, PSU_ID_DATA_1))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = updatePisCancellationPsuDataValidator.validate(new UpdatePaymentPsuDataPO(commonPaymentResponse, updateRequest));

        // Then
        verify(pisTppInfoValidator).validateTpp(commonPaymentResponse.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(PIS_SERVICE_INVALID, validationResult.getMessageError());
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }

    private PisCommonPaymentResponse buildPisCommonPaymentResponse(TppInfo tppInfo, Xs2aScaStatus scaStatus) {
        return buildPisCommonPaymentResponse(tppInfo, scaStatus, AUTHORISATION_ID, PSU_ID_DATA_1);
    }

    private PisCommonPaymentResponse buildPisCommonPaymentResponse(TppInfo tppInfo, Xs2aScaStatus scaStatus, String authorisationId, PsuIdData psuIdData) {
        PisCommonPaymentResponse pisCommonPaymentResponse = new PisCommonPaymentResponse();
        pisCommonPaymentResponse.setTppInfo(tppInfo);
        pisCommonPaymentResponse.setPaymentProduct(CORRECT_PAYMENT_PRODUCT);
        pisCommonPaymentResponse.setPaymentType(PaymentType.SINGLE);
        pisCommonPaymentResponse.setAuthorisations(buildAuthorisation(scaStatus, authorisationId, psuIdData));
        return pisCommonPaymentResponse;
    }

    private List<Authorisation> buildAuthorisation(Xs2aScaStatus scaStatus, String authorisationId, PsuIdData psuIdData) {
        return Collections.singletonList(new Authorisation(authorisationId, psuIdData, "paymentId", AuthorisationType.PIS_CANCELLATION, scaStatus));
    }

    private Xs2aUpdatePisCommonPaymentPsuDataRequest buildUpdateRequest(String authorisationId, PsuIdData psuIdData) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest result = new Xs2aUpdatePisCommonPaymentPsuDataRequest();
        result.setAuthorisationId(authorisationId);
        result.setPsuData(psuIdData);
        result.setPaymentProduct(CORRECT_PAYMENT_PRODUCT);
        result.setPaymentService(PaymentType.SINGLE);
        return result;
    }

    private Xs2aUpdatePisCommonPaymentPsuDataRequest buildUpdateRequest(String authorisationId, PsuIdData psuIdData, String confirmationCode) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest result = buildUpdateRequest(authorisationId, psuIdData);
        result.setConfirmationCode(CONFIRMATION_CODE);
        return result;
    }
}
