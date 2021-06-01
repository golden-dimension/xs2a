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

package de.adorsys.psd2.xs2a.domain.pis;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import de.adorsys.psd2.xs2a.core.authorisation.AuthenticationObject;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.pis.Xs2aAmount;
import de.adorsys.psd2.xs2a.core.profile.NotificationSupportedMode;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
public abstract class PaymentInitiationResponse {
    private ScaStatus scaStatus;
    @JsonUnwrapped
    private TransactionStatus transactionStatus;
    private Xs2aAmount transactionFees;
    private Boolean transactionFeeIndicator;
    private boolean multilevelScaRequired;
    private String paymentId;
    private List<AuthenticationObject> scaMethods;
    private ChallengeData challengeData;
    private String psuMessage;
    @JsonProperty("_links")
    private Links links = new Links();
    private String authorizationId;
    private InitialSpiAspspConsentDataProvider aspspConsentDataProvider;
    private String aspspAccountId;
    private ErrorHolder errorHolder;
    private String internalRequestId;
    private List<NotificationSupportedMode> tppNotificationContentPreferred;
    private final Set<TppMessageInformation> tppMessageInformation = new HashSet<>();
    private Xs2aAmount currencyConversionFee;
    private Xs2aAmount estimatedTotalAmount;
    private Xs2aAmount estimatedInterbankSettlementAmount;

    PaymentInitiationResponse(ErrorHolder errorHolder) {
        this.errorHolder = errorHolder;
    }

    public boolean hasError() {
        return errorHolder != null;
    }
}
