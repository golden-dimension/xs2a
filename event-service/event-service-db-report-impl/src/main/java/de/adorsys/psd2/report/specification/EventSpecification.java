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

package de.adorsys.psd2.report.specification;

import de.adorsys.psd2.report.entity.AspspEventEntity;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.Predicate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;



@Service
public class EventSpecification {
    private static final String TIMESTAMP_ATTRIBUTE = "timestamp";
    private static final String INSTANCE_ID_ATTRIBUTE = "instanceId";

    public Specification<AspspEventEntity> byPeriodAndInstanceId(OffsetDateTime start, OffsetDateTime end, String instanceId) {
        return Optional.of(byPeriod(start, end))
                   .map(s -> s.and(byInstanceId(instanceId)))
                   .orElse(null);
    }

    private Specification<AspspEventEntity> byPeriod(@Nullable OffsetDateTime start, @Nullable OffsetDateTime end) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            criteriaQuery.orderBy(criteriaBuilder.asc(root.get(TIMESTAMP_ATTRIBUTE)));

            List<Predicate> predicates = new ArrayList<>();
            if (start != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get(TIMESTAMP_ATTRIBUTE), start));
            }

            if (end != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get(TIMESTAMP_ATTRIBUTE), end));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    protected Specification<AspspEventEntity> byInstanceId(@Nullable String instanceId) {
        return EventEntityAttributeSpecificationProvider.provideSpecificationForEntityAttribute(INSTANCE_ID_ATTRIBUTE, instanceId);
    }
}
