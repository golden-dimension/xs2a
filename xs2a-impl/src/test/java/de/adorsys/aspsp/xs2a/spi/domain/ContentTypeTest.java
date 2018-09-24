/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.aspsp.xs2a.spi.domain;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ContentTypeTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testContentTypeWithoutCharset() throws IOException {
        Container expected = new Container(ContentType.JSON);

        String contentTypeJson = "{ \"contentType\": \"application/json\" }";

        assertEquals(expected, objectMapper.readValue(contentTypeJson, Container.class));
    }

    @Test
    public void testContentTypeWithCharset() throws IOException {
        Container expected = new Container(ContentType.XML);

        String contentTypeJson = "{ \"contentType\": \"application/xml; charset=utf-8\" }";

        assertEquals(expected, objectMapper.readValue(contentTypeJson, Container.class));
    }
}

@Data
@AllArgsConstructor
class Container {
    private ContentType contentType;
}
