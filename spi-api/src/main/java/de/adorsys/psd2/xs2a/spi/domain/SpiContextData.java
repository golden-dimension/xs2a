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

package de.adorsys.psd2.xs2a.spi.domain;

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * This object represents known Context of call, provided by this or previous requests in scope of one process (e.g. one payment or one AIS consent)
 */
@Value
@NotNull
@RequiredArgsConstructor
public class SpiContextData {
    private SpiPsuData psuData;
    private TppInfo tppInfo;
    private UUID xRequestId;
    private UUID internalRequestId;
    private String oAuth2Token;
    @Nullable
    private String tppBrandLoggingInformation;
    @Nullable
    private Boolean tppRejectionNoFundsPreferred;
    @Nullable
    private Boolean tppRedirectPreferred;
    @Nullable
    private Boolean tppDecoupledPreferred;
}
