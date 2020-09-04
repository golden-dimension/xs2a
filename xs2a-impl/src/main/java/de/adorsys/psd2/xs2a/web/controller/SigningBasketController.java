/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.web.controller;

import de.adorsys.psd2.api.SigningBasketApi;
import de.adorsys.psd2.model.SigningBasket;
import de.adorsys.psd2.xs2a.core.psu.AdditionalPsuIdData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppNotificationData;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.NotificationModeResponseHeaders;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.sb.CreateSigningBasketRequest;
import de.adorsys.psd2.xs2a.domain.sb.CreateSigningBasketResponse;
import de.adorsys.psd2.xs2a.service.NotificationSupportedModeService;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import de.adorsys.psd2.xs2a.service.sb.SigningBasketService;
import de.adorsys.psd2.xs2a.web.header.ResponseHeaders;
import de.adorsys.psd2.xs2a.web.header.SigningBasketHeadersBuilder;
import de.adorsys.psd2.xs2a.web.mapper.SigningBasketModelMapper;
import de.adorsys.psd2.xs2a.web.mapper.TppRedirectUriMapper;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@AllArgsConstructor
public class SigningBasketController implements SigningBasketApi {
    private final SigningBasketService signingBasketService;
    private final ResponseMapper responseMapper;
    private final ResponseErrorMapper responseErrorMapper;
    private final TppRedirectUriMapper tppRedirectUriMapper;
    private final NotificationSupportedModeService notificationSupportedModeService;
    private final SigningBasketModelMapper signingBasketModelMapper;
    private final RequestProviderService requestProviderService;
    private final SigningBasketHeadersBuilder signingBasketHeadersBuilder;

    @Override
    public ResponseEntity createSigningBasket(UUID xRequestID, String psUIPAddress, SigningBasket body, String digest, String signature, byte[] tpPSignatureCertificate,
                                              String psuId, String psUIDType, String psUCorporateID, String psUCorporateIDType, String consentID, Boolean tpPRedirectPreferred,
                                              String tpPRedirectURI, String tpPNokRedirectURI, Boolean tpPExplicitAuthorisationPreferred, String tpPNotificationURI,
                                              String tpPNotificationContentPreferred, String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding,
                                              String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {

        TppRedirectUri xs2aTppRedirectUri = tppRedirectUriMapper.mapToTppRedirectUri(tpPRedirectURI, tpPNokRedirectURI);
        TppNotificationData tppNotificationData = notificationSupportedModeService.getTppNotificationData(tpPNotificationContentPreferred, tpPNotificationURI);

        CreateSigningBasketRequest createSigningBasketRequest = signingBasketModelMapper.mapToCreateSigningBasketRequest(body, xs2aTppRedirectUri, tppNotificationData, requestProviderService.getInstanceId());

        PsuIdData psuData = new PsuIdData(psuId, psUIDType, psUCorporateID, psUCorporateIDType, psUIPAddress,
                                          new AdditionalPsuIdData(psUIPPort, psUUserAgent, psUGeoLocation, psUAccept, psUAcceptCharset, psUAcceptEncoding, psUAcceptLanguage, psUHttpMethod, psUDeviceID));

        boolean explicitPreferred = tpPExplicitAuthorisationPreferred == null || BooleanUtils.isTrue(tpPExplicitAuthorisationPreferred);
        ResponseObject<CreateSigningBasketResponse> createResponse = signingBasketService.createSigningBasket(createSigningBasketRequest, psuData, explicitPreferred);

        if (createResponse.hasError()) {
            return responseErrorMapper.generateErrorResponse(createResponse.getError());
        }

        CreateSigningBasketResponse createSigningBasketResponse = createResponse.getBody();

        NotificationModeResponseHeaders notificationHeaders = notificationSupportedModeService.resolveNotificationHeaders(createSigningBasketResponse.getTppNotificationContentPreferred());

        ResponseHeaders headers = signingBasketHeadersBuilder.buildCreateSigningBasketHeaders(
            Optional.ofNullable(createSigningBasketResponse.getLinks().getSelf())
                .map(HrefType::getHref)
                .orElseThrow(() -> new IllegalArgumentException("Wrong href type in self link")),
            notificationHeaders);

        return responseMapper.created(createResponse, signingBasketModelMapper::mapToSigningBasketResponse201, headers);
    }
}
