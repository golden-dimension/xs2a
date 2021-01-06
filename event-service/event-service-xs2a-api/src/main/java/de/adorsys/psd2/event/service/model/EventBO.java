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

package de.adorsys.psd2.event.service.model;

import de.adorsys.psd2.event.core.model.EventOrigin;
import de.adorsys.psd2.event.core.model.EventType;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Contains information about the event.
 */
@Setter
@Getter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class EventBO {
    /**
     * Date and time indicating when the event has occurred.
     */
    private OffsetDateTime timestamp;

    /**
     * Id of the consent that can be associated with this event.
     * Can be null if the event isn't connected with the specific consent.
     */
    private String consentId;

    /**
     * Id of the payment that can be associated with this event.
     * Can be null if the event isn't connected with the specific payment.
     */
    private String paymentId;

    /**
     * Indicates the origin of the event.
     */
    private EventOrigin eventOrigin;

    /**
     * Indicates what happened in this event.
     */
    private EventType eventType;

    /**
     * The id of particular service instance.
     */
    private String instanceId;

    /**
     * PSU data
     */
    private PsuIdDataBO psuIdData;

    /**
     * Authorization number of the TPP
     */
    private String tppAuthorisationNumber;

    /**
     * ID of the request, provided by the TPP
     */
    private UUID xRequestId;

    /**
     * Internal ID of the request, generated by the XS2A
     */
    private UUID internalRequestId;

    /**
     * Object that may contain additional information about the event.
     * Can be null if the event doesn't provide any additional information.
     */
    private Object payload;
}
