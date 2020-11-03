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

package de.adorsys.psd2.consent.web.aspsp.controller;

import de.adorsys.psd2.consent.api.piis.v1.CmsPiisConsent;
import de.adorsys.psd2.consent.aspsp.api.piis.CmsAspspPiisService;
import de.adorsys.psd2.consent.aspsp.api.piis.CreatePiisConsentRequest;
import de.adorsys.psd2.consent.web.aspsp.config.ObjectMapperTestConfig;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CmsAspspPiisControllerTest {
    private final String CONSENT_ID = "03f0fccb-8462-43e8-9f7e-fce2887bcf38";
    private final String CREATE_PIIS_CONSENT_URL = "/aspsp-api/v1/piis/consents/";
    private final String DELETE_PIIS_CONSENT_URL = "/aspsp-api/v1/piis/consents/03f0fccb-8462-43e8-9f7e-fce2887bcf38";
    private final String PSU_ID = "marion.mueller";
    private final String CREATE_PIIS_CONSENT_REQUEST_PATH = "json/piis/create-piis-consent-request.json";
    private final String CREATE_PIIS_CONSENT_RESPONSE_PATH = "json/piis/create-piis-consent-response.json";
    private final String LIST_OF_PIIS_CONSENTS_PATH = "json/piis/list-piis-consent.json";
    private final String INSTANCE_ID = "UNDEFINED";
    private final String TRUE = "true";

    private MockMvc mockMvc;
    private JsonReader jsonReader = new JsonReader();
    private HttpHeaders httpHeaders = new HttpHeaders();
    private PsuIdData psuIdData;
    private CreatePiisConsentRequest createPiisConsentRequest;
    private CmsPiisConsent cmsPiisConsent;

    @InjectMocks
    private CmsAspspPiisController cmsAspspPiisController;

    @Mock
    private CmsAspspPiisService cmsAspspPiisService;

    @BeforeEach
    void setUp() {
        ObjectMapperTestConfig objectMapperTestConfig = new ObjectMapperTestConfig();

        psuIdData = jsonReader.getObjectFromFile("json/psu-id-data.json", PsuIdData.class);
        createPiisConsentRequest = jsonReader.getObjectFromFile("json/piis/create-piis-consent-request.json", CreatePiisConsentRequest.class);
        cmsPiisConsent = jsonReader.getObjectFromFile("json/piis/cms-piis-consent.json", CmsPiisConsent.class);

        httpHeaders.add("psu-id", PSU_ID);
        httpHeaders.add("Content-Type", "application/json");
        httpHeaders.add("instance-id", INSTANCE_ID);

        mockMvc = MockMvcBuilders
                      .standaloneSetup(cmsAspspPiisController)
                      .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapperTestConfig.getXs2aObjectMapper()))
                      .build();
    }

    @Test
    void createConsent_Success() throws Exception {
        when(cmsAspspPiisService.createConsent(psuIdData, createPiisConsentRequest, INSTANCE_ID))
            .thenReturn(Optional.of(CONSENT_ID));

        mockMvc.perform(post(CREATE_PIIS_CONSENT_URL)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .headers(httpHeaders)
                            .content(jsonReader.getStringFromFile(CREATE_PIIS_CONSENT_REQUEST_PATH)))
            .andExpect(status().is(HttpStatus.CREATED.value()))
            .andExpect(content().json(jsonReader.getStringFromFile(CREATE_PIIS_CONSENT_RESPONSE_PATH)));

        verify(cmsAspspPiisService, times(1)).createConsent(psuIdData, createPiisConsentRequest, INSTANCE_ID);
    }

    @Test
    void createConsent_BadRequest() throws Exception {
        when(cmsAspspPiisService.createConsent(psuIdData, createPiisConsentRequest, INSTANCE_ID))
            .thenReturn(Optional.empty());

        mockMvc.perform(post(CREATE_PIIS_CONSENT_URL)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .headers(httpHeaders)
                            .content(jsonReader.getStringFromFile(CREATE_PIIS_CONSENT_REQUEST_PATH)))
            .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));

        verify(cmsAspspPiisService, times(1)).createConsent(psuIdData, createPiisConsentRequest, INSTANCE_ID);
    }

    @Test
    void getConsentsForPsu_Success() throws Exception {
        List<CmsPiisConsent> consents = Collections.singletonList(cmsPiisConsent);
        when(cmsAspspPiisService.getConsentsForPsu(psuIdData, INSTANCE_ID, null, null))
            .thenReturn(consents);

        mockMvc.perform(get(CREATE_PIIS_CONSENT_URL)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().json(jsonReader.getStringFromFile(LIST_OF_PIIS_CONSENTS_PATH)));

        verify(cmsAspspPiisService, times(1)).getConsentsForPsu(psuIdData, INSTANCE_ID, null, null);
    }

    @Test
    void terminateConsent_ReturnTrue() throws Exception {
        when(cmsAspspPiisService.terminateConsent(CONSENT_ID, INSTANCE_ID))
            .thenReturn(true);

        mockMvc.perform(delete(DELETE_PIIS_CONSENT_URL)
                            .contentType(MediaType.APPLICATION_JSON_VALUE)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().string(TRUE));

        verify(cmsAspspPiisService, times(1)).terminateConsent(CONSENT_ID, INSTANCE_ID);
    }
}
