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

package de.adorsys.psd2.xs2a.domain.consent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.domain.Links;
import lombok.Data;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class CreateConsentResponse {
    private final String consentStatus;

    private final String consentId;

    private final List<AuthenticationObject> scaMethods;

    private final AuthenticationObject chosenScaMethod;

    private final ChallengeData challengeData;

    @JsonProperty("_links")
    private Links links = new Links();

    private final String psuMessage;

    private final boolean multilevelScaRequired;

    //For Embedded approach Implicit case
    @JsonIgnore
    private String authorizationId;

    @JsonIgnore
    private final String internalRequestId;

    private final List<NotificationSupportedMode> tppNotificationContentPreferred;
    private Set<TppMessageInformation> tppMessageInformation = new HashSet<>();
}
