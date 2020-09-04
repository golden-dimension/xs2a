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

package de.adorsys.psd2.xs2a.domain.sb;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.domain.Links;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class CreateSigningBasketResponse {
    private final String transactionStatus;
    private final String basketId;
    private final List<AuthenticationObject> scaMethods;
    private final AuthenticationObject chosenScaMethod;
    private final ChallengeData challengeData;
    private final boolean isMultilevelScaRequired;

    @JsonProperty("_links")
    private Links links = new Links();

    private final String psuMessage;
    private Set<TppMessageInformation> tppMessageInformation;

    private final List<NotificationSupportedMode> tppNotificationContentPreferred;
}
