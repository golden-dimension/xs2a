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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.domain.pis.PisAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.pis.Xs2aTransactionStatus;
import de.adorsys.psd2.xs2a.core.sca.Xs2aScaStatus;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PisCommonPaymentConfirmationExpirationServiceTest {
    private static final String PAYMENT_ID = "some payment id";

    @InjectMocks
    private PisCommonPaymentConfirmationExpirationServiceImpl service;

    @Mock
    private PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    @Mock
    private AuthorisationRepository authorisationRepository;
    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    private AspspSettings aspspSettings;

    @Test
    void checkAndUpdateOnConfirmationExpiration_confirmationIsExpired() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(Xs2aTransactionStatus.RCVD);
        pisCommonPaymentData.setCreationTimestamp(OffsetDateTime.now().minusSeconds(100));
        pisCommonPaymentData.setPaymentId(PAYMENT_ID);

        AuthorisationEntity pisAuthorisation = new AuthorisationEntity();
        pisAuthorisation.setScaStatus(Xs2aScaStatus.RECEIVED);
        when(authorisationRepository.findAllByParentExternalIdAndTypeIn(PAYMENT_ID, EnumSet.of(AuthorisationType.PIS_CREATION, AuthorisationType.PIS_CANCELLATION)))
            .thenReturn(Collections.singletonList(pisAuthorisation));

        when(aspspProfileService.getAspspSettings(pisAuthorisation.getInstanceId())).thenReturn(aspspSettings);
        when(aspspSettings.getPis()).thenReturn(getPisAspspProfileSetting(1000L));

        service.checkAndUpdateOnConfirmationExpiration(pisCommonPaymentData);

        assertEquals(Xs2aTransactionStatus.RJCT, pisCommonPaymentData.getTransactionStatus());
        assertEquals(Xs2aScaStatus.FAILED, pisAuthorisation.getScaStatus());

        verify(pisCommonPaymentDataRepository).save(pisCommonPaymentData);
    }

    @Test
    void checkAndUpdateOnConfirmationExpiration_confirmationIsNotExpired() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(Xs2aTransactionStatus.RCVD);
        pisCommonPaymentData.setCreationTimestamp(OffsetDateTime.now().plusHours(1));

        AuthorisationEntity pisAuthorisation = new AuthorisationEntity();
        pisAuthorisation.setScaStatus(Xs2aScaStatus.RECEIVED);

        when(aspspProfileService.getAspspSettings(pisAuthorisation.getInstanceId())).thenReturn(aspspSettings);
        when(aspspSettings.getPis()).thenReturn(getPisAspspProfileSetting(10L));

        service.checkAndUpdateOnConfirmationExpiration(pisCommonPaymentData);

        assertEquals(Xs2aTransactionStatus.RCVD, pisCommonPaymentData.getTransactionStatus());
        assertEquals(Xs2aScaStatus.RECEIVED, pisAuthorisation.getScaStatus());
    }

    @Test
    void isPaymentDataOnConfirmationExpired() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(Xs2aTransactionStatus.RCVD);
        pisCommonPaymentData.setCreationTimestamp(OffsetDateTime.now().minusHours(1));

        when(aspspProfileService.getAspspSettings(pisCommonPaymentData.getInstanceId())).thenReturn(aspspSettings);
        when(aspspSettings.getPis()).thenReturn(getPisAspspProfileSetting(10L));

        boolean actual = service.isConfirmationExpired(pisCommonPaymentData);

        assertTrue(actual);
    }

    @Test
    void isPaymentDataOnConfirmationNotExpired() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(Xs2aTransactionStatus.RCVD);
        pisCommonPaymentData.setCreationTimestamp(OffsetDateTime.now().plusHours(1));

        when(aspspProfileService.getAspspSettings(pisCommonPaymentData.getInstanceId())).thenReturn(aspspSettings);
        when(aspspSettings.getPis()).thenReturn(getPisAspspProfileSetting(10L));

        boolean actual = service.isConfirmationExpired(pisCommonPaymentData);

        assertFalse(actual);
    }

    @Test
    void isConfirmationExpired_pisCommonPaymentDataIsNull() {
        assertFalse(service.isConfirmationExpired(null));
    }

    @Test
    void updateOnConfirmationExpiration() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(Xs2aTransactionStatus.RCVD);
        pisCommonPaymentData.setPaymentId(PAYMENT_ID);

        AuthorisationEntity pisAuthorization = new AuthorisationEntity();
        pisAuthorization.setScaStatus(Xs2aScaStatus.RECEIVED);

        when(authorisationRepository.findAllByParentExternalIdAndTypeIn(PAYMENT_ID, EnumSet.of(AuthorisationType.PIS_CREATION, AuthorisationType.PIS_CANCELLATION)))
            .thenReturn(Collections.singletonList(pisAuthorization));

        service.updateOnConfirmationExpiration(pisCommonPaymentData);

        assertEquals(Xs2aTransactionStatus.RJCT, pisCommonPaymentData.getTransactionStatus());
        assertEquals(Xs2aScaStatus.FAILED, pisAuthorization.getScaStatus());

        verify(pisCommonPaymentDataRepository).save(pisCommonPaymentData);
    }

    @Test
    void updatePaymentDataListOnConfirmationExpiration() {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setTransactionStatus(Xs2aTransactionStatus.RCVD);
        pisCommonPaymentData.setPaymentId(PAYMENT_ID);

        AuthorisationEntity pisAuthorization = new AuthorisationEntity();
        pisAuthorization.setScaStatus(Xs2aScaStatus.RECEIVED);

        when(authorisationRepository.findAllByParentExternalIdAndTypeIn(PAYMENT_ID, EnumSet.of(AuthorisationType.PIS_CREATION, AuthorisationType.PIS_CANCELLATION)))
            .thenReturn(Collections.singletonList(pisAuthorization));

        service.updatePaymentDataListOnConfirmationExpiration(Collections.singletonList(pisCommonPaymentData));

        assertEquals(Xs2aTransactionStatus.RJCT, pisCommonPaymentData.getTransactionStatus());
        assertEquals(Xs2aScaStatus.FAILED, pisAuthorization.getScaStatus());

        verify(pisCommonPaymentDataRepository).saveAll(Collections.singletonList(pisCommonPaymentData));
    }

    @NotNull
    private PisAspspProfileSetting getPisAspspProfileSetting(long notConfirmedPaymentExpirationTimeMs) {
        return new PisAspspProfileSetting(new HashMap<>(), 0, notConfirmedPaymentExpirationTimeMs,
                                          true, null, "", null);
    }
}
