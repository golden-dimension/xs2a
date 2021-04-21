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

package de.adorsys.psd2.xs2a.web.controller;

import de.adorsys.psd2.api.PaymentApi;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentCancellationRequest;
import de.adorsys.psd2.model.PaymentInitationRequestResponse201;
import de.adorsys.psd2.model.PaymentInitiationCancelResponse202;
import de.adorsys.psd2.model.PeriodicPaymentInitiationXmlPart2StandingorderTypeJson;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.AdditionalPsuIdData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppAttributes;
import de.adorsys.psd2.xs2a.core.tpp.TppNotificationData;
import de.adorsys.psd2.xs2a.domain.HrefType;
import de.adorsys.psd2.xs2a.domain.NotificationModeResponseHeaders;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.authorisation.CancellationAuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAuthorisationSubResources;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aCreatePisAuthorisationRequest;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aPaymentCancellationAuthorisationSubResource;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aScaStatusResponse;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.domain.pis.*;
import de.adorsys.psd2.xs2a.exception.WrongPaymentTypeException;
import de.adorsys.psd2.xs2a.service.*;
import de.adorsys.psd2.xs2a.service.mapper.ResponseMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ResponseErrorMapper;
import de.adorsys.psd2.xs2a.web.header.PaymentCancellationHeadersBuilder;
import de.adorsys.psd2.xs2a.web.header.PaymentInitiationHeadersBuilder;
import de.adorsys.psd2.xs2a.web.header.ResponseHeaders;
import de.adorsys.psd2.xs2a.web.mapper.*;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("unchecked") // This class implements autogenerated interface without proper return values generated
@RestController
@AllArgsConstructor
public class PaymentController implements PaymentApi {
    private final PaymentService xs2aPaymentService;
    private final ResponseMapper responseMapper;
    private final ResponseErrorMapper responseErrorMapper;
    private final PaymentModelMapperPsd2 paymentModelMapperPsd2;
    private final PaymentModelMapperXs2a paymentModelMapperXs2a;
    private final ConsentModelMapper consentModelMapper;
    private final PaymentAuthorisationService paymentAuthorisationService;
    private final PaymentCancellationAuthorisationService paymentCancellationAuthorisationService;
    private final AuthorisationMapper authorisationMapper;
    private final PaymentInitiationHeadersBuilder paymentInitiationHeadersBuilder;
    private final PaymentCancellationHeadersBuilder paymentCancellationHeadersBuilder;
    private final AuthorisationModelMapper authorisationModelMapper;
    private final NotificationSupportedModeService notificationSupportedModeService;
    private final PaymentServiceForAuthorisationImpl paymentServiceForAuthorisation;
    private final PaymentCancellationServiceForAuthorisationImpl paymentCancellationServiceForAuthorisation;
    private final RequestProviderService requestProviderService;

    @Override
    public ResponseEntity getPaymentInitiationStatus(String paymentService, String paymentProduct,
                                                     String paymentId, UUID xRequestId, String digest,
                                                     String signature, byte[] tppSignatureCertificate,
                                                     String psuIpAddress, String psuIpPort,
                                                     String psuAccept, String psuAcceptCharset, String psuAcceptEncoding,
                                                     String psuAcceptLanguage, String psuUserAgent, String psuHttpMethod,
                                                     UUID psuDeviceId, String psuGeoLocation) {

        ResponseObject<GetPaymentStatusResponse> serviceResponse = xs2aPaymentService.getPaymentStatusById(getPaymentType(paymentService), paymentProduct, paymentId);

        if (serviceResponse.hasError()) {
            return responseErrorMapper.generateErrorResponse(serviceResponse.getError());
        }

        if (serviceResponse.getBody().isResponseContentTypeJson()) {
            return responseMapper.ok(serviceResponse, paymentModelMapperPsd2::mapToStatusResponseJson);
        } else {
            return responseMapper.ok(serviceResponse, paymentModelMapperPsd2::mapToStatusResponseRaw);
        }
    }


    @Override
    public ResponseEntity getPaymentInformation(String paymentService, String paymentProduct, String paymentId, UUID xRequestID, String digest,
                                                String signature, byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort,
                                                String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage,
                                                String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {

        ResponseObject<CommonPayment> serviceResponse = xs2aPaymentService.getPaymentById(getPaymentType(paymentService), paymentProduct, paymentId);

        return serviceResponse.hasError()
                   ? responseErrorMapper.generateErrorResponse(serviceResponse.getError())
                   : responseMapper.ok(serviceResponse, paymentModelMapperPsd2::mapToGetPaymentResponse);
    }

    //Method for JSON format payments

    @Override
    public ResponseEntity<PaymentInitationRequestResponse201> initiatePayment(Object body, UUID xRequestID,
                                                                              String psUIPAddress, String paymentService,
                                                                              String paymentProduct, String digest,
                                                                              String signature, byte[] tpPSignatureCertificate,
                                                                              String psuId, String psUIDType, String psUCorporateID,
                                                                              String psUCorporateIDType, String consentID,
                                                                              Boolean tpPRedirectPreferred, String tpPRedirectURI,
                                                                              String tpPNokRedirectURI, Boolean tpPExplicitAuthorisationPreferred,
                                                                              Boolean tpPRejectionNoFundsPreferred, String tppBrandLoggingInformation,
                                                                              String tpPNotificationURI, String tpPNotificationContentPreferred,
                                                                              String psUIPPort, String psUAccept, String psUAcceptCharset,
                                                                              String psUAcceptEncoding, String psUAcceptLanguage,
                                                                              String psUUserAgent, String psUHttpMethod,
                                                                              UUID psUDeviceID, String psUGeoLocation) {
        PsuIdData psuData = new PsuIdData(psuId, psUIDType, psUCorporateID, psUCorporateIDType, psUIPAddress, new AdditionalPsuIdData(psUIPPort, psUUserAgent, psUGeoLocation, psUAccept, psUAcceptCharset, psUAcceptEncoding, psUAcceptLanguage, psUHttpMethod, psUDeviceID));
        TppNotificationData tppNotificationData = notificationSupportedModeService.getTppNotificationData(tpPNotificationContentPreferred, tpPNotificationURI);

        TppAttributes tppAttributes = new TppAttributes(tpPSignatureCertificate, tpPRedirectURI, tpPNokRedirectURI, BooleanUtils.isTrue(tpPExplicitAuthorisationPreferred), tppNotificationData, tppBrandLoggingInformation);
        PaymentInitiationParameters paymentInitiationParameters = paymentModelMapperPsd2.mapToPaymentRequestParameters(paymentProduct,
                                                                                                                       paymentService,
                                                                                                                       tppAttributes,
                                                                                                                       psuData,
                                                                                                                       requestProviderService.getInstanceId());
        ResponseObject<PaymentInitiationResponse> serviceResponse =
            xs2aPaymentService.createPayment(paymentModelMapperXs2a.mapToXs2aPayment(), paymentInitiationParameters);

        if (serviceResponse.hasError()) {
            return responseErrorMapper.generateErrorResponse(serviceResponse.getError());
        }

        PaymentInitiationResponse serviceResponseBody = serviceResponse.getBody();
        ResponseHeaders responseHeaders = buildPaymentInitiationResponseHeaders(serviceResponseBody);

        return responseMapper.created(ResponseObject
                                          .builder()
                                          .body(paymentModelMapperPsd2.mapToPaymentInitiationResponse(serviceResponseBody))
                                          .build(), responseHeaders);
    }

    //Method for pain.001 payment products
    @Override
    public ResponseEntity<PaymentInitationRequestResponse201> initiatePayment(UUID xRequestID, String psUIPAddress,
                                                                              String paymentService, String paymentProduct,
                                                                              Object xmlSct,
                                                                              PeriodicPaymentInitiationXmlPart2StandingorderTypeJson jsonStandingorderType,
                                                                              String digest, String signature,
                                                                              byte[] tpPSignatureCertificate, String psuId,
                                                                              String psUIDType, String psUCorporateID,
                                                                              String psUCorporateIDType, String consentID,
                                                                              Boolean tpPRedirectPreferred, String tpPRedirectURI,
                                                                              String tpPNokRedirectURI, Boolean tpPExplicitAuthorisationPreferred,
                                                                              Boolean tpPRejectionNoFundsPreferred, String tppBrandLoggingInformation,
                                                                              String tpPNotificationURI, String tpPNotificationContentPreferred,
                                                                              String psUIPPort, String psUAccept, String psUAcceptCharset,
                                                                              String psUAcceptEncoding, String psUAcceptLanguage,
                                                                              String psUUserAgent, String psUHttpMethod,
                                                                              UUID psUDeviceID, String psUGeoLocation) {
        PsuIdData psuData = new PsuIdData(psuId, psUIDType, psUCorporateID, psUCorporateIDType, psUIPAddress, new AdditionalPsuIdData(psUIPPort, psUUserAgent, psUGeoLocation, psUAccept, psUAcceptCharset, psUAcceptEncoding, psUAcceptLanguage, psUHttpMethod, psUDeviceID));
        TppNotificationData tppNotificationData = notificationSupportedModeService.getTppNotificationData(tpPNotificationContentPreferred, tpPNotificationURI);
        TppAttributes tppAttributes = new TppAttributes(tpPSignatureCertificate, tpPRedirectURI, tpPNokRedirectURI, BooleanUtils.isTrue(tpPExplicitAuthorisationPreferred), tppNotificationData, tppBrandLoggingInformation);
        PaymentInitiationParameters paymentInitiationParameters = paymentModelMapperPsd2.mapToPaymentRequestParameters(paymentProduct,
                                                                                                                       paymentService,
                                                                                                                       tppAttributes,
                                                                                                                       psuData,
                                                                                                                       requestProviderService.getInstanceId());
        ResponseObject<PaymentInitiationResponse> serviceResponse =
            xs2aPaymentService.createPayment(paymentModelMapperXs2a.mapToXs2aRawPayment(paymentInitiationParameters, xmlSct, jsonStandingorderType), paymentInitiationParameters);

        if (serviceResponse.hasError()) {
            return responseErrorMapper.generateErrorResponse(serviceResponse.getError());
        }

        PaymentInitiationResponse serviceResponseBody = serviceResponse.getBody();
        ResponseHeaders responseHeaders = buildPaymentInitiationResponseHeaders(serviceResponseBody);

        return responseMapper.created(ResponseObject
                                          .builder()
                                          .body(paymentModelMapperPsd2.mapToPaymentInitiationResponse(serviceResponseBody))
                                          .build(), responseHeaders);
    }

    // Method for raw payment products

    @Override
    public ResponseEntity<PaymentInitationRequestResponse201> initiatePayment(String body, UUID xRequestID,
                                                                              String psUIPAddress, String paymentService,
                                                                              String paymentProduct, String digest,
                                                                              String signature, byte[] tpPSignatureCertificate,
                                                                              String psuId, String psUIDType,
                                                                              String psUCorporateID, String psUCorporateIDType,
                                                                              String consentID, Boolean tpPRedirectPreferred,
                                                                              String tpPRedirectURI, String tpPNokRedirectURI,
                                                                              Boolean tpPExplicitAuthorisationPreferred, Boolean tpPRejectionNoFundsPreferred,
                                                                              String tppBrandLoggingInformation, String tpPNotificationURI,
                                                                              String tpPNotificationContentPreferred, String psUIPPort,
                                                                              String psUAccept, String psUAcceptCharset, String psUAcceptEncoding,
                                                                              String psUAcceptLanguage, String psUUserAgent,
                                                                              String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        PsuIdData psuData = new PsuIdData(psuId, psUIDType, psUCorporateID, psUCorporateIDType, psUIPAddress, new AdditionalPsuIdData(psUIPPort, psUUserAgent, psUGeoLocation, psUAccept, psUAcceptCharset, psUAcceptEncoding, psUAcceptLanguage, psUHttpMethod, psUDeviceID));

        TppNotificationData tppNotificationData = notificationSupportedModeService.getTppNotificationData(tpPNotificationContentPreferred, tpPNotificationURI);
        TppAttributes tppAttributes = new TppAttributes(tpPSignatureCertificate, tpPRedirectURI, tpPNokRedirectURI, BooleanUtils.isTrue(tpPExplicitAuthorisationPreferred), tppNotificationData, tppBrandLoggingInformation);
        PaymentInitiationParameters paymentInitiationParameters = paymentModelMapperPsd2.mapToPaymentRequestParameters(paymentProduct,
                                                                                                                       paymentService,
                                                                                                                       tppAttributes,
                                                                                                                       psuData,
                                                                                                                       requestProviderService.getInstanceId());
        ResponseObject<PaymentInitiationResponse> serviceResponse =
            xs2aPaymentService.createPayment(body.getBytes(), paymentInitiationParameters);

        if (serviceResponse.hasError()) {
            return responseErrorMapper.generateErrorResponse(serviceResponse.getError());
        }

        PaymentInitiationResponse serviceResponseBody = serviceResponse.getBody();
        ResponseHeaders responseHeaders = buildPaymentInitiationResponseHeaders(serviceResponseBody);

        return responseMapper.created(ResponseObject
                                          .builder()
                                          .body(paymentModelMapperPsd2.mapToPaymentInitiationResponse(serviceResponseBody))
                                          .build(), responseHeaders);
    }

    @Override
    public ResponseEntity cancelPayment(String paymentService, String paymentProduct, String paymentId,
                                        UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate,
                                        Boolean tpPRedirectPreferred, String tpPNokRedirectURI, String tpPRedirectURI,
                                        Boolean tppExplicitAuthorisationPreferred, String psUIPAddress, String psUIPPort,
                                        String psUAccept, String psUAcceptCharset, String psUAcceptEncoding,
                                        String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod,
                                        UUID psUDeviceID, String psUGeoLocation) {

        PisPaymentCancellationRequest paymentCancellationRequest = paymentModelMapperPsd2.mapToPaymentCancellationRequest(paymentProduct, paymentService, paymentId, tppExplicitAuthorisationPreferred, tpPRedirectURI, tpPNokRedirectURI);
        ResponseObject<CancelPaymentResponse> serviceResponse = xs2aPaymentService.cancelPayment(paymentCancellationRequest);

        if (serviceResponse.hasError()) {
            return responseErrorMapper.generateErrorResponse(serviceResponse.getError());
        }

        CancelPaymentResponse cancelPayment = serviceResponse.getBody();
        PaymentInitiationCancelResponse202 response = paymentModelMapperPsd2.mapToPaymentInitiationCancelResponse(cancelPayment);

        return cancelPayment.isStartAuthorisationRequired()
                   ? responseMapper.accepted(ResponseObject.builder().body(response).build())
                   : responseMapper.delete(serviceResponse);
    }

    @Override
    public ResponseEntity getPaymentCancellationScaStatus(String paymentService, String paymentProduct, String paymentId,
                                                          String authorisationId, UUID xRequestID, String digest, String signature,
                                                          byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort,
                                                          String psUAccept, String psUAcceptCharset, String psUAcceptEncoding,
                                                          String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod,
                                                          UUID psUDeviceID, String psUGeoLocation) {
        ResponseObject<Xs2aScaStatusResponse> serviceResponse =
            paymentCancellationServiceForAuthorisation.getAuthorisationScaStatus(paymentId, authorisationId, getPaymentType(paymentService), paymentProduct);
        return serviceResponse.hasError()
                   ? responseErrorMapper.generateErrorResponse(serviceResponse.getError())
                   : responseMapper.ok(serviceResponse, authorisationMapper::mapToScaStatusResponse);
    }

    @Override
    public ResponseEntity getPaymentInitiationAuthorisation(String paymentService, String paymentProduct, String paymentId, UUID xRequestID, String digest,
                                                            String signature, byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort,
                                                            String psUAccept, String psUAcceptCharset, String psUAcceptEncoding, String psUAcceptLanguage,
                                                            String psUUserAgent, String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        ResponseObject<Xs2aAuthorisationSubResources> serviceResponse = paymentAuthorisationService.getPaymentInitiationAuthorisations(paymentId, paymentProduct, getPaymentType(paymentService));
        return serviceResponse.hasError()
                   ? responseErrorMapper.generateErrorResponse(serviceResponse.getError())
                   : responseMapper.ok(serviceResponse, authorisationMapper::mapToAuthorisations);
    }

    @Override
    public ResponseEntity getPaymentInitiationCancellationAuthorisationInformation(String paymentService, String paymentProduct, String paymentId,
                                                                                   UUID xRequestID, String digest, String signature,
                                                                                   byte[] tpPSignatureCertificate, String psUIPAddress,
                                                                                   String psUIPPort, String psUAccept, String psUAcceptCharset,
                                                                                   String psUAcceptEncoding, String psUAcceptLanguage,
                                                                                   String psUUserAgent, String psUHttpMethod, UUID psUDeviceID,
                                                                                   String psUGeoLocation) {
        ResponseObject<Xs2aPaymentCancellationAuthorisationSubResource> serviceResponse =
            paymentCancellationAuthorisationService.getPaymentInitiationCancellationAuthorisationInformation(paymentId, getPaymentType(paymentService), paymentProduct);
        return serviceResponse.hasError()
                   ? responseErrorMapper.generateErrorResponse(serviceResponse.getError())
                   : responseMapper.ok(serviceResponse, consentModelMapper::mapToAuthorisations);
    }

    @Override
    public ResponseEntity getPaymentInitiationScaStatus(String paymentService, String paymentProduct, String paymentId, String authorisationId,
                                                        UUID xRequestID, String digest, String signature, byte[] tpPSignatureCertificate,
                                                        String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset,
                                                        String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent,
                                                        String psUHttpMethod, UUID psUDeviceID, String psUGeoLocation) {
        ResponseObject<Xs2aScaStatusResponse> serviceResponse =
            paymentServiceForAuthorisation.getAuthorisationScaStatus(paymentId, authorisationId, getPaymentType(paymentService), paymentProduct);
        return serviceResponse.hasError()
                   ? responseErrorMapper.generateErrorResponse(serviceResponse.getError())
                   : responseMapper.ok(serviceResponse, authorisationMapper::mapToScaStatusResponse);
    }

    @Override
    public ResponseEntity startPaymentAuthorisation(UUID xRequestID, String paymentService, String paymentProduct,
                                                    String paymentId, Object body, String psuId, String psUIDType,
                                                    String psUCorporateID, String psUCorporateIDType, Boolean tpPRedirectPreferred,
                                                    String tpPRedirectURI, String tpPNokRedirectURI, String tpPNotificationURI,
                                                    String tpPNotificationContentPreferred, String digest, String signature,
                                                    byte[] tpPSignatureCertificate, String psUIPAddress, String psUIPPort,
                                                    String psUAccept, String psUAcceptCharset, String psUAcceptEncoding,
                                                    String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod,
                                                    UUID psUDeviceID, String psUGeoLocation) {
        PsuIdData psuData = new PsuIdData(psuId, psUIDType, psUCorporateID, psUCorporateIDType, psUIPAddress);

        Xs2aCreatePisAuthorisationRequest createRequest = authorisationMapper.mapToXs2aCreatePisAuthorisationRequest(psuData, paymentId, getPaymentType(paymentService), paymentProduct, (Map) body);

        ResponseObject<AuthorisationResponse> createAuthResponse = paymentAuthorisationService.createPisAuthorisation(createRequest);

        if (createAuthResponse.hasError()) {
            return responseErrorMapper.generateErrorResponse(createAuthResponse.getError());
        }

        AuthorisationResponse authResponse = createAuthResponse.getBody();
        ResponseHeaders responseHeaders = paymentInitiationHeadersBuilder.buildStartAuthorisationHeaders(authResponse.getAuthorisationId());

        return responseMapper.created(ResponseObject.builder()
                                          .body(authorisationMapper.mapToPisCreateOrUpdateAuthorisationResponse(createAuthResponse))
                                          .build(), responseHeaders);
    }

    @Override
    public ResponseEntity startPaymentInitiationCancellationAuthorisation(UUID xRequestID, String paymentService,
                                                                          String paymentProduct, String paymentId,
                                                                          Object body, String digest, String signature,
                                                                          byte[] tpPSignatureCertificate, String psuId,
                                                                          String psUIDType, String psUCorporateID,
                                                                          String psUCorporateIDType, Boolean tpPRedirectPreferred,
                                                                          String tpPRedirectURI, String tpPNokRedirectURI,
                                                                          String tpPNotificationURI, String tpPNotificationContentPreferred,
                                                                          String psUIPAddress, String psUIPPort, String psUAccept,
                                                                          String psUAcceptCharset, String psUAcceptEncoding,
                                                                          String psUAcceptLanguage, String psUUserAgent,
                                                                          String psUHttpMethod, UUID psUDeviceID,
                                                                          String psUGeoLocation) {
        PsuIdData psuData = new PsuIdData(psuId, psUIDType, psUCorporateID, psUCorporateIDType, psUIPAddress);
        Xs2aCreatePisAuthorisationRequest createRequest = authorisationMapper.mapToXs2aCreatePisAuthorisationRequest(psuData, paymentId, getPaymentType(paymentService), paymentProduct, (Map) body);

        ResponseObject<CancellationAuthorisationResponse> serviceResponse = paymentCancellationAuthorisationService.createPisCancellationAuthorisation(createRequest);

        if (serviceResponse.hasError()) {
            return responseErrorMapper.generateErrorResponse(serviceResponse.getError());
        }

        CancellationAuthorisationResponse serviceResponseBody = serviceResponse.getBody();
        ResponseHeaders responseHeaders = paymentCancellationHeadersBuilder.buildStartAuthorisationHeaders(serviceResponseBody.getAuthorisationId());

        return responseMapper.created(serviceResponse, authorisationModelMapper::mapToStartOrUpdateCancellationResponse, responseHeaders);
    }

    @Override
    public ResponseEntity updatePaymentCancellationPsuData(UUID xRequestID, String paymentService, String paymentProduct, String paymentId,
                                                           String authorisationId, Object body, String digest, String signature,
                                                           byte[] tpPSignatureCertificate, String psuId, String psUIDType,
                                                           String psUCorporateID, String psUCorporateIDType, String psUIPAddress,
                                                           String psUIPPort, String psUAccept, String psUAcceptCharset, String psUAcceptEncoding,
                                                           String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod, UUID psUDeviceID,
                                                           String psUGeoLocation) {
        PsuIdData psuData = new PsuIdData(psuId, psUIDType, psUCorporateID, psUCorporateIDType, psUIPAddress);
        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> serviceResponse = paymentCancellationAuthorisationService.updatePisCancellationPsuData(consentModelMapper.mapToPisUpdatePsuData(psuData, paymentId, authorisationId, getPaymentType(paymentService), paymentProduct, (Map) body));

        if (serviceResponse.hasError()) {
            return responseErrorMapper.generateErrorResponse(serviceResponse.getError());
        }

        ResponseHeaders responseHeaders = paymentCancellationHeadersBuilder.buildUpdatePsuDataHeaders(authorisationId);

        return responseMapper.ok(serviceResponse, authorisationMapper::mapToPisUpdatePsuAuthenticationResponse, responseHeaders);
    }

    @Override
    public ResponseEntity updatePaymentPsuData(UUID xRequestID, String paymentService, String paymentProduct, String paymentId,
                                               String authorisationId, Object body, String digest, String signature, byte[] tpPSignatureCertificate,
                                               String psuId, String psUIDType, String psUCorporateID, String psUCorporateIDType,
                                               String psUIPAddress, String psUIPPort, String psUAccept, String psUAcceptCharset,
                                               String psUAcceptEncoding, String psUAcceptLanguage, String psUUserAgent, String psUHttpMethod,
                                               UUID psUDeviceID, String psUGeoLocation) {

        PsuIdData psuData = new PsuIdData(psuId, psUIDType, psUCorporateID, psUCorporateIDType, psUIPAddress);
        return updatePisAuthorisation(psuData, authorisationId, paymentService, paymentProduct, paymentId, body);
    }

    private ResponseEntity<Object> updatePisAuthorisation(PsuIdData psuData, String authorisationId, String paymentService, String paymentProduct, String paymentId, Object body) {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = consentModelMapper.mapToPisUpdatePsuData(psuData, paymentId, authorisationId, getPaymentType(paymentService), paymentProduct, (Map) body);

        ResponseObject<Xs2aUpdatePisCommonPaymentPsuDataResponse> serviceResponse = paymentAuthorisationService.updatePisCommonPaymentPsuData(request);

        if (serviceResponse.hasError()) {
            return responseErrorMapper.generateErrorResponse(serviceResponse.getError());
        }

        ResponseHeaders responseHeaders = paymentInitiationHeadersBuilder.buildUpdatePsuDataHeaders(authorisationId);

        return responseMapper.ok(serviceResponse, authorisationMapper::mapToPisUpdatePsuAuthenticationResponse, responseHeaders);
    }

    private ResponseHeaders buildPaymentInitiationResponseHeaders(PaymentInitiationResponse paymentInitiationResponse) {
        NotificationModeResponseHeaders notificationHeaders = notificationSupportedModeService.resolveNotificationHeaders(paymentInitiationResponse.getTppNotificationContentPreferred());

        String selfLink = Optional.ofNullable(paymentInitiationResponse.getLinks().getSelf())
                              .map(HrefType::getHref)
                              .orElseThrow(() -> new IllegalArgumentException("Wrong href type in self link"));

        return paymentInitiationHeadersBuilder.buildInitiatePaymentHeaders(paymentInitiationResponse.getAuthorizationId(), selfLink, notificationHeaders);
    }

    private PaymentType getPaymentType(String paymentService) {
        return PaymentType.getByValue(paymentService)
                   .orElseThrow(() -> new WrongPaymentTypeException(paymentService));
    }
}
