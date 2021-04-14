/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.filter.holder;

import de.adorsys.psd2.consent.api.service.TppService;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.tpp.TppRoleValidationService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;

@AllArgsConstructor
@Service
public class QwacCertificateService {
    private final RequestProviderService requestProviderService;
    private final TppRoleValidationService tppRoleValidationService;
    private final TppService tppService;
    private final AspspProfileServiceWrapper aspspProfileService;

    public String getEncodedTppQwacCert() {
        return requestProviderService.getEncodedTppQwacCert();
    }

    public String getTppRolesAllowedHeader() {
        return requestProviderService.getTppRolesAllowedHeader();
    }

    public boolean isCheckTppRolesFromCertificateSupported() {
        return aspspProfileService.isCheckTppRolesFromCertificateSupported();
    }

    public boolean hasAccess(TppInfo tppInfo, HttpServletRequest request) {
        return tppRoleValidationService.hasAccess(tppInfo, request);
    }

    public void updateTppInfo(TppInfo tppInfo) {
        tppService.updateTppInfo(tppInfo);
    }
}
