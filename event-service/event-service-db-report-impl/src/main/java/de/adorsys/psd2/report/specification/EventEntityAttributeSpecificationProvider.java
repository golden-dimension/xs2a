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

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.jpa.domain.Specification;

import javax.persistence.criteria.Join;
import java.util.List;

/**
 * This is a class for providing Spring Data Jpa Specification for different entities attributes
 */
public class EventEntityAttributeSpecificationProvider {
    private EventEntityAttributeSpecificationProvider() {
    }

    /**
     * Provides specification for the attribute in some entity.
     *
     * @param attribute name of the attribute in entity
     * @param value     optional value of the attribute
     * @param <T>       type of the entity, for which this specification will be created
     * @return resulting specification, or <code>null</code> if the attribute's value was omitted
     */
    public static <T> Specification<T> provideSpecificationForEntityAttribute(String attribute, String value) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (StringUtils.isBlank(value)) {
                return null;
            }
            return criteriaBuilder.and(criteriaBuilder.equal(root.get(attribute), value));
        };
    }

    public static <T> Specification<T> provideSpecificationForEntityAttributeInList(String attribute, List<String> values) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (CollectionUtils.isEmpty(values)) {
                return null;
            }
            return criteriaBuilder.and(criteriaBuilder.in(root.get(attribute)).value(values));
        };
    }

    /**
     * Provides specification for the attribute in a joined entity.
     *
     * @param join      join to an entity
     * @param attribute name of the attribute in joined entity
     * @param value     optional value of the attribute
     * @param <T>       type of the entity, for which this specification will be created
     * @return resulting specification, or <code>null</code> if the attribute's value was omitted
     */
    public static <T> Specification<T> provideSpecificationForJoinedEntityAttribute(@NotNull Join<T, ?> join,
                                                                                    @NotNull String
                                                                                        attribute,
                                                                                    @Nullable String value) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (StringUtils.isBlank(value)) {
                return null;
            }
            return criteriaBuilder.and(criteriaBuilder.equal(join.get(attribute), value));
        };
    }

    /**
     * Provides specification for the attribute in a joined entity.
     *
     * @param join      join to an entity
     * @param attribute name of the attribute in joined entity
     * @param values    optional values of the attribute
     * @param <T>       type of the entity, for which this specification will be created
     * @return resulting specification, or <code>null</code> if the attribute's value was omitted
     */
    public static <T> Specification<T> provideSpecificationForJoinedEntityAttributeIn(@NotNull Join<T, ?> join,
                                                                                      @NotNull String attribute,
                                                                                      @Nullable List<String> values) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            if (CollectionUtils.isEmpty(values)) {
                return null;
            }
            return criteriaBuilder.and(join.get(attribute).in(values));
        };
    }

    /**
     * Provides specification for the attribute in a joined entity.
     *
     * @param join      join to an entity
     * @param attribute name of the attribute in joined entity
     * @param value     optional value of the attribute as Object
     * @param <T>       type of the entity, for which this specification will be created
     * @return resulting specification, or <code>null</code> if the attribute's value was omitted
     */
    public static <T> Specification<T> provideSpecificationForJoinedEntityAttribute(@NotNull Join<T, ?> join,
                                                                                    @NotNull String
                                                                                        attribute,
                                                                                    @Nullable Object value) {
        return (root, criteriaQuery, criteriaBuilder) -> value == null
                                                             ? criteriaBuilder.and(criteriaBuilder.isNull(join.get(attribute)))
                                                             : criteriaBuilder.and(criteriaBuilder.equal(join.get(attribute), value));
    }
}
