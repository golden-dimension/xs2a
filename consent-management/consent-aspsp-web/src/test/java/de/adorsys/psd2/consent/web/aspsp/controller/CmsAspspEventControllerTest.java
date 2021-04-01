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

import de.adorsys.psd2.consent.web.aspsp.config.ObjectMapperTestConfig;
import de.adorsys.psd2.event.service.AspspEventService;
import de.adorsys.psd2.event.service.model.AspspEvent;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CmsAspspEventControllerTest {

    private static final String START = "2019-07-11T11:51:00Z";
    private static final String END = "2019-07-11T20:00:00Z";
    private static final String INSTANCE_ID = "UNDEFINED";
    private static final String EVENT_LIST_PATH = "json/list-aspsp-event.json";
    private static final String GET_ASPSP_EVENT_LIST_URL = "/aspsp-api/v1/events/";

    @Mock
    private AspspEventService aspspEventService;

    private final JsonReader jsonReader = new JsonReader();
    private final HttpHeaders httpHeaders = new HttpHeaders();
    private MockMvc mockMvc;
    private List<AspspEvent> events;

    @BeforeEach
    void setUp() {
        ObjectMapperTestConfig objectMapperTestConfig = new ObjectMapperTestConfig();

        AspspEvent event = jsonReader.getObjectFromFile("json/aspsp-event.json", AspspEvent.class);
        events = Collections.singletonList(event);

        httpHeaders.add("start-date", START);
        httpHeaders.add("end-date", END);

        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders
                      .standaloneSetup(new CmsAspspEventController(aspspEventService))
                      .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapperTestConfig.getXs2aObjectMapper()))
                      .build();
    }

    @Test
    void getEventsForDates_success() throws Exception {
        when(aspspEventService.getEventsForPeriod(OffsetDateTime.parse(START), OffsetDateTime.parse(END), INSTANCE_ID, 0, 20))
            .thenReturn(events);

        mockMvc.perform(get(GET_ASPSP_EVENT_LIST_URL)
                            .headers(httpHeaders)
                            .header("instance-id", INSTANCE_ID))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile(EVENT_LIST_PATH)))
            .andReturn();

        verify(aspspEventService, times(1)).getEventsForPeriod(OffsetDateTime.parse(START), OffsetDateTime.parse(END), INSTANCE_ID, 0, 20);
    }

    @Test
    void getEventsForDates_withoutInstanceId() throws Exception {
        when(aspspEventService.getEventsForPeriod(OffsetDateTime.parse(START), OffsetDateTime.parse(END), INSTANCE_ID, 0, 20))
            .thenReturn(events);

        mockMvc.perform(get(GET_ASPSP_EVENT_LIST_URL)
                            .headers(httpHeaders))
            .andExpect(status().is(HttpStatus.OK.value()))
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().json(jsonReader.getStringFromFile(EVENT_LIST_PATH)))
            .andReturn();

        verify(aspspEventService, times(1)).getEventsForPeriod(OffsetDateTime.parse(START), OffsetDateTime.parse(END), INSTANCE_ID, 0, 20);
    }
}
