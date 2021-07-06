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

package de.adorsys.psd2.consent.api.authorisation;

import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Create authorisation request", value = "CreateAuthorisationRequest")
public class CreateAuthorisationRequest {

    @ApiModelProperty(value = "Authorisation id")
    private String authorisationId;

    @ApiModelProperty(value = "Corresponding PSU", required = true)
    private PsuIdData psuData;

    @ApiModelProperty(value = "SCA approach")
    private ScaApproach scaApproach;

    @ApiModelProperty(value = "SCA status")
    private ScaStatus scaStatus;

    @ApiModelProperty(value = "TPP redirect URIs")
    private TppRedirectUri tppRedirectURIs;
}
