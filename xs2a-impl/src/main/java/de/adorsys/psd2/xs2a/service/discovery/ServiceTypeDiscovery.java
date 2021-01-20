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

package de.adorsys.psd2.xs2a.service.discovery;

import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import java.util.HashMap;
import java.util.Map;

import static de.adorsys.psd2.xs2a.config.Xs2aEndpointPathConstant.*;
import static de.adorsys.psd2.xs2a.core.mapper.ServiceType.*;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
class ServiceTypeDiscovery {
    private static final AntPathMatcher matcher = new AntPathMatcher();
    private static final Map<String, ServiceType> pathToServiceType;

    static {
        pathToServiceType = new HashMap<>();
        pathToServiceType.put(ACCOUNTS_PATH, AIS);
        pathToServiceType.put(BENEFICIARIES_PATH, AIS);
        pathToServiceType.put(CARD_ACCOUNTS_PATH, AIS);
        pathToServiceType.put(CONSENTS_PATH, AIS);
        pathToServiceType.put(SINGLE_PAYMENTS_PATH, PIS);
        pathToServiceType.put(PERIODIC_PAYMENTS_PATH, PIS);
        pathToServiceType.put(BULK_PAYMENTS_PATH, PIS);
        pathToServiceType.put(FUNDS_CONFIRMATION_PATH, PIIS);
        pathToServiceType.put(CONSENTS_V2_PATH, PIIS);
        pathToServiceType.put(SIGNING_BASKETS_PATH, SB);
    }

    /**
     * Returns service type by checking incoming path on existing paths patterns matching (each pattern is associated with corresponding service type).
     *
     * @param targetPath target path to be checked on pattern matching
     * @return Service Type value
     */
    static ServiceType getServiceType(String targetPath) {
        for (Map.Entry<String, ServiceType> entry : pathToServiceType.entrySet()) {
            String pattern = entry.getKey();

            if (matcher.match(pattern, targetPath)) {
                return entry.getValue();
            }
        }

        log.warn("Can't get ServiceType because illegal path: [{}]", targetPath);
        throw new IllegalArgumentException("Illegal path: " + targetPath);
    }
}
