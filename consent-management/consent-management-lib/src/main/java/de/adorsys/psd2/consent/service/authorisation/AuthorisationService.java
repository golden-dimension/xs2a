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

package de.adorsys.psd2.consent.service.authorisation;

import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.domain.Authorisable;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.CmsPsuData;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.service.mapper.AuthorisationMapper;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class AuthorisationService {
    protected final AuthorisationMapper authorisationMapper;
    protected final AuthorisationRepository authorisationRepository;

    public List<AuthorisationEntity> findAllByParentExternalIdAndType(String parentId, AuthorisationType authorisationType) {
        return authorisationRepository.findAllByParentExternalIdAndType(parentId, authorisationType);
    }

    public Optional<AuthorisationEntity> findByExternalIdAndType(String authorisationId, AuthorisationType authorisationType) {
        return authorisationRepository.findByExternalIdAndType(authorisationId, authorisationType);
    }

    public AuthorisationEntity prepareAuthorisationEntity(Authorisable authorisationParent, CreateAuthorisationRequest request,
                                                          Optional<CmsPsuData> psuDataOptional, AuthorisationType authorisationType,
                                                          long redirectUrlExpirationTimeMs, long authorisationExpirationTimeMs) {
        return authorisationMapper.prepareAuthorisationEntity(authorisationParent, request, psuDataOptional, authorisationType,
                                                              redirectUrlExpirationTimeMs, authorisationExpirationTimeMs);
    }

    public AuthorisationEntity save(AuthorisationEntity entity) {
        return authorisationRepository.save(entity);
    }
}
