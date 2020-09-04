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

package de.adorsys.psd2.xs2a.web.mapper;

import de.adorsys.psd2.model.*;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.tpp.TppNotificationData;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.Links;
import de.adorsys.psd2.xs2a.domain.sb.CreateSigningBasketRequest;
import de.adorsys.psd2.xs2a.domain.sb.CreateSigningBasketResponse;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public abstract class SigningBasketModelMapper {
    @Autowired
    protected ScaMethodsMapper scaMethodsMapper;

    public CreateSigningBasketRequest mapToCreateSigningBasketRequest(SigningBasket signingBasket, TppRedirectUri tppRedirectUri, TppNotificationData tppNotificationData, String instanceId) {
        return Optional.ofNullable(signingBasket)
                   .map(s -> new CreateSigningBasketRequest(
                       s.getPaymentIds(),
                       s.getConsentIds(),
                       tppRedirectUri,
                       tppNotificationData,
                       instanceId
                   ))
                   .orElse(null);
    }

    public SigningBasketResponse201 mapToSigningBasketResponse201(CreateSigningBasketResponse createSigningBasketResponse) {
        return Optional.ofNullable(createSigningBasketResponse)
                   .map(s -> new SigningBasketResponse201()
                                 .transactionStatus(mapToTransactionStatusSBS(s.getTransactionStatus()))
                                 .basketId(s.getBasketId())
                                 .scaMethods(scaMethodsMapper.mapToScaMethods(s.getScaMethods()))
                                 ._links(mapToLinksSigningBasket(s.getLinks()))
                                 .psuMessage(s.getPsuMessage())
                                 .tppMessages(mapToTppMessage2XXList(s.getTppMessageInformation()))
                   )
                   .orElse(null);
    }

    private TransactionStatusSBS mapToTransactionStatusSBS(String transactionStatus) {
        return Optional.ofNullable(transactionStatus)
                   .map(TransactionStatusSBS::fromValue)
                   .orElse(null);
    }

    protected abstract LinksSigningBasket mapToLinksSigningBasket(Links links);

    private List<TppMessage2XX> mapToTppMessage2XXList(Set<TppMessageInformation> tppMessages) {
        if (CollectionUtils.isEmpty(tppMessages)) {
            return null;
        }
        return tppMessages.stream()
                   .map(this::mapToTppMessage2XX)
                   .collect(Collectors.toList());
    }

    private TppMessage2XX mapToTppMessage2XX(TppMessageInformation tppMessage) {
        TppMessage2XX tppMessage2XX = new TppMessage2XX();
        tppMessage2XX.setCategory(TppMessageCategory.fromValue(tppMessage.getCategory().name()));
        tppMessage2XX.setCode(MessageCode2XX.WARNING);
        tppMessage2XX.setPath(tppMessage.getPath());
        tppMessage2XX.setText(tppMessage.getText());

        return tppMessage2XX;
    }
}
