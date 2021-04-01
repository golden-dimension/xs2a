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

package de.adorsys.psd2.report.mapper;

import de.adorsys.psd2.consent.domain.PsuDataEmbeddable;
import de.adorsys.psd2.event.persist.model.PsuIdDataPO;
import de.adorsys.psd2.event.persist.model.ReportEvent;
import de.adorsys.psd2.report.entity.AspspEventEntity;
import de.adorsys.psd2.report.entity.EventConsentEntity;
import de.adorsys.psd2.report.entity.EventEntityForReport;
import de.adorsys.psd2.report.entity.EventPaymentEntity;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface EventReportDBMapper {

    ReportEvent mapToReportEvent(EventEntityForReport event);

    @Mapping(target = "consentId", source = "event.consent.externalId")
    @Mapping(target = "paymentId", source = "event.payment.paymentId")
    ReportEvent mapToReportEventFromEventEntity(AspspEventEntity event);

    @AfterMapping
    default void mapToReportEventAfterMappingFromEventEntity(AspspEventEntity event,
                                                             @MappingTarget ReportEvent reportEvent) {
        reportEvent.setPsuIdData(getPsuIdDataPOSetForEventEntity(event));
    }

    @AfterMapping
    default void mapToReportEventAfterMapping(EventEntityForReport event,
                                              @MappingTarget ReportEvent reportEvent) {
        reportEvent.setPsuIdData(getPsuIdDataPOSet(event));
    }

    default Set<PsuIdDataPO> getPsuIdDataPOSet(EventEntityForReport event) {
        Set<PsuIdDataPO> psus = new HashSet<>();
        if (StringUtils.isNotBlank(event.getPsuId())) {
            psus.add(mapToPsuIdDataPO(event.getPsuId(), event.getPsuIdType(), event.getPsuCorporateId(), event.getPsuCorporateIdType()));
        } else if (StringUtils.isNotBlank(event.getPsuExId())) {
            psus.add(mapToPsuIdDataPO(event.getPsuExId(), event.getPsuExIdType(), event.getPsuExCorporateId(), event.getPsuExCorporateIdType()));
        }
        return psus;
    }

    default Set<PsuIdDataPO> getPsuIdDataPOSetForEventEntity(AspspEventEntity event) {
        Set<PsuIdDataPO> psus = new HashSet<>();
        PsuDataEmbeddable psuDataEmbeddable = event.getPsuData();
        if (psuDataEmbeddable != null && psuDataEmbeddable.getPsuId() != null) {
            psus.add(mapToPsuIdDataPO(psuDataEmbeddable.getPsuId(), psuDataEmbeddable.getPsuIdType(),
                                      psuDataEmbeddable.getPsuCorporateId(), psuDataEmbeddable.getPsuCorporateIdType()));
        }
        EventConsentEntity eventConsentEntity = event.getConsent();
        if (eventConsentEntity != null
                && eventConsentEntity.getPsuDataList() != null
                && !eventConsentEntity.getPsuDataList().isEmpty()) {
            psus.addAll(eventConsentEntity.getPsuDataList().stream()
                            .map(p -> mapToPsuIdDataPO(p.getPsuId(), p.getPsuIdType(),
                                                       p.getPsuCorporateId(), p.getPsuCorporateIdType()))
                            .collect(Collectors.toList()));
        }
        EventPaymentEntity eventPaymentEntity = event.getPayment();
        if (eventPaymentEntity != null
                && eventPaymentEntity.getPsuDataList() != null
                && !eventPaymentEntity.getPsuDataList().isEmpty()) {
            psus.addAll(eventPaymentEntity.getPsuDataList().stream()
                            .map(p -> mapToPsuIdDataPO(p.getPsuId(), p.getPsuIdType(),
                                                       p.getPsuCorporateId(), p.getPsuCorporateIdType()))
                            .collect(Collectors.toList()));
        }
        return psus;
    }

    default PsuIdDataPO mapToPsuIdDataPO(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType) {
        PsuIdDataPO psuIdDataPO = new PsuIdDataPO();
        psuIdDataPO.setPsuId(psuId);
        psuIdDataPO.setPsuIdType(psuIdType);
        psuIdDataPO.setPsuCorporateId(psuCorporateId);
        psuIdDataPO.setPsuCorporateIdType(psuCorporateIdType);
        return psuIdDataPO;
    }

    default List<ReportEvent> mapToAspspReportEvents(List<EventEntityForReport> events) {
        Collection<ReportEvent> eventCollection = events.stream()
                                                      .map(this::mapToReportEvent)
                                                      .collect(Collectors.toMap(ReportEvent::getId,
                                                                                Function.identity(),
                                                                                ReportEvent::merge))
                                                      .values();
        return new ArrayList<>(eventCollection);
    }

    default List<ReportEvent> mapToAspspReportEventsFromEventEntities(List<AspspEventEntity> events) {
        Collection<ReportEvent> eventCollection = events.stream()
                                                      .map(this::mapToReportEventFromEventEntity)
                                                      .collect(Collectors.toMap(ReportEvent::getId,
                                                                                Function.identity(),
                                                                                ReportEvent::merge))
                                                      .values();
        return new ArrayList<>(eventCollection);
    }
}
