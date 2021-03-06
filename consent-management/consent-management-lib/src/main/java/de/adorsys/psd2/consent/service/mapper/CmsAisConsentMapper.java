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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.xs2a.core.authorisation.ConsentAuthorization;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Mapper(componentModel = "spring")
public abstract class CmsAisConsentMapper {
    @Autowired
    protected ConsentDataMapper consentDataMapper;


    @Mapping(target = "consentTppInformation", source = "tppInformation")
    @Mapping(target = "consentData", expression = "java(consentDataMapper.mapToAisConsentData(cmsConsent.getConsentData()))")
    @Mapping(target = "authorisations", expression = "java(mapToAccountConsentAuthorisation(cmsConsent.getAuthorisations()))")
    public abstract AisConsent mapToAisConsent(CmsConsent cmsConsent);

    List<ConsentAuthorization> mapToAccountConsentAuthorisation(List<Authorisation> authorisations) {
        if (CollectionUtils.isEmpty(authorisations)) {
            return Collections.emptyList();
        }
        return authorisations.stream()
                   .map(this::mapToAccountConsentAuthorisation)
                   .collect(Collectors.toList());
    }

    ConsentAuthorization mapToAccountConsentAuthorisation(Authorisation authorisation) {
        return Optional.ofNullable(authorisation)
                   .map(auth -> {
                       ConsentAuthorization accountConsentAuthorisation = new ConsentAuthorization();
                       accountConsentAuthorisation.setId(auth.getAuthorisationId());
                       accountConsentAuthorisation.setPsuIdData(auth.getPsuIdData());
                       accountConsentAuthorisation.setScaStatus(auth.getScaStatus());
                       return accountConsentAuthorisation;
                   })
                   .orElse(null);
    }
}
