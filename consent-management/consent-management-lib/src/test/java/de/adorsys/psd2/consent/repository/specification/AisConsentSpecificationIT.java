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

package de.adorsys.psd2.consent.repository.specification;

import de.adorsys.psd2.consent.api.ais.AdditionalTppInfo;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.TppInfoRepository;
import de.adorsys.psd2.integration.test.BaseTest;
import de.adorsys.psd2.integration.test.TestDBConfiguration;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = TestDBConfiguration.class,
    initializers = {AisConsentSpecificationIT.Initializer.class})
class AisConsentSpecificationIT extends BaseTest {

    private static final String INSTANCE_ID = "UNDEFINED";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("987654321", "", "", "", "");
    private static final String ASPSP_ACCOUNT_ID = "123-DEDE89370400440532013000-EUR";

    @Autowired
    private AisConsentSpecification aisConsentSpecification;

    @Autowired
    private ConsentJpaRepository consentJpaRepository;
    @Autowired
    private TppInfoRepository tppInfoRepository;

    private TppInfoEntity tppInfo;
    private ConsentEntity consent;

    @BeforeEach
    void setUp() {
        clearData();

        tppInfo = tppInfoRepository.save(
            jsonReader.getObjectFromFile("json/specification/tpp-info-entity.json", TppInfoEntity.class));

        ConsentEntity consentEntity = jsonReader.getObjectFromFile("json/specification/consent-entity.json", ConsentEntity.class);
        consentEntity.setConsentType(ConsentType.AIS.getName());
        consentEntity.getTppInformation().setTppInfo(tppInfo);
        consent = consentJpaRepository.save(consentEntity);
    }

    @Test
    void byConsentIdAndInstanceId() {
        Optional<ConsentEntity> actual = consentJpaRepository.findOne(
            aisConsentSpecification.byConsentIdAndInstanceId(consent.getExternalId(), INSTANCE_ID));

        assertTrue(actual.isPresent());
        assertEquals(consent.getExternalId(), actual.get().getExternalId());
        assertEquals(INSTANCE_ID, actual.get().getInstanceId());
    }

    @Test
    @Transactional
    void byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId() {
        OffsetDateTime from = consent.getCreationTimestamp().minusMinutes(1);
        OffsetDateTime to = consent.getCreationTimestamp().plusMinutes(1);
        List<ConsentEntity> actual = consentJpaRepository.findAll(
            aisConsentSpecification.byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(
                tppInfo.getAuthorisationNumber(),
                from.toLocalDate(),
                to.toLocalDate(),
                PSU_ID_DATA,
                INSTANCE_ID,
                AdditionalTppInfo.NONE
            )
        );

        assertTrue(CollectionUtils.isNotEmpty(actual));
        assertEquals(1, actual.size());
        ConsentEntity actualConsent = actual.get(0);
        assertEquals(tppInfo.getAuthorisationNumber(), actualConsent.getTppInformation().getTppInfo().getAuthorisationNumber());
        assertEquals(consent.getExternalId(), actualConsent.getExternalId());
        assertTrue(actualConsent.getCreationTimestamp().isAfter(from));
        assertTrue(actualConsent.getCreationTimestamp().isBefore(to));
        assertEquals(PSU_ID_DATA.getPsuId(), actualConsent.getPsuDataList().get(0).getPsuId());
        assertEquals(INSTANCE_ID, actualConsent.getInstanceId());
    }

    @Test
    @Transactional
    void byPsuDataInListAndInstanceId() {
        List<ConsentEntity> actual = consentJpaRepository.findAll(
            aisConsentSpecification.byPsuDataInListAndInstanceId(
                PSU_ID_DATA,
                INSTANCE_ID
            )
        );

        assertTrue(CollectionUtils.isNotEmpty(actual));
        assertEquals(1, actual.size());
        ConsentEntity actualConsent = actual.get(0);
        assertEquals(PSU_ID_DATA.getPsuId(), actualConsent.getPsuDataList().get(0).getPsuId());
        assertEquals(INSTANCE_ID, actual.get(0).getInstanceId());
    }

    @Test
    @Transactional
    void byPsuIdDataAndCreationPeriodAndInstanceId() {
        OffsetDateTime from = consent.getCreationTimestamp().minusMinutes(1);
        OffsetDateTime to = consent.getCreationTimestamp().plusMinutes(1);
        List<ConsentEntity> actual = consentJpaRepository.findAll(
            aisConsentSpecification.byPsuIdDataAndCreationPeriodAndInstanceIdAndAdditionalTppInfo(
                PSU_ID_DATA,
                from.toLocalDate(),
                to.toLocalDate(),
                INSTANCE_ID,
                AdditionalTppInfo.NONE
            )
        );

        assertTrue(CollectionUtils.isNotEmpty(actual));
        assertEquals(1, actual.size());
        ConsentEntity actualConsent = actual.get(0);
        assertEquals(PSU_ID_DATA.getPsuId(), actualConsent.getPsuDataList().get(0).getPsuId());
        assertTrue(actualConsent.getCreationTimestamp().isAfter(from));
        assertTrue(actualConsent.getCreationTimestamp().isBefore(to));
        assertEquals(INSTANCE_ID, actualConsent.getInstanceId());
    }

    @Test
    @Transactional
    void byAspspAccountIdAndCreationPeriodAndInstanceId() {
        OffsetDateTime from = consent.getCreationTimestamp().minusMinutes(1);
        OffsetDateTime to = consent.getCreationTimestamp().plusMinutes(1);
        List<ConsentEntity> actual = consentJpaRepository.findAll(
            aisConsentSpecification.byAspspAccountIdAndCreationPeriodAndInstanceIdAndAdditionalTppInfo(
                ASPSP_ACCOUNT_ID,
                from.toLocalDate(),
                to.toLocalDate(),
                INSTANCE_ID,
                AdditionalTppInfo.NONE
            )
        );

        assertTrue(CollectionUtils.isNotEmpty(actual));
        assertEquals(1, actual.size());
        ConsentEntity actualConsent = actual.get(0);
        assertEquals(ASPSP_ACCOUNT_ID, actualConsent.getAspspAccountAccesses().get(0).getAspspAccountId());
        assertTrue(actualConsent.getCreationTimestamp().isAfter(from));
        assertTrue(actualConsent.getCreationTimestamp().isBefore(to));
        assertEquals(INSTANCE_ID, actualConsent.getInstanceId());
    }
}
