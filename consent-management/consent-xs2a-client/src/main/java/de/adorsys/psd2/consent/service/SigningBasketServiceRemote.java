package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.service.SigningBasketServiceEncrypted;
import de.adorsys.psd2.consent.api.signingbasket.CmsSigningBasket;
import de.adorsys.psd2.consent.api.signingbasket.CmsSigningBasketConsentsAndPaymentsResponse;
import de.adorsys.psd2.consent.api.signingbasket.CmsSigningBasketCreationResponse;
import de.adorsys.psd2.consent.config.CmsRestException;
import de.adorsys.psd2.consent.config.SigningBasketRemoteUrls;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static de.adorsys.psd2.consent.api.CmsError.TECHNICAL_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class SigningBasketServiceRemote implements SigningBasketServiceEncrypted {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final SigningBasketRemoteUrls signingBasketRemoteUrls;

    @Override
    public CmsResponse<CmsSigningBasketCreationResponse> createSigningBasket(CmsSigningBasket signingBasket) {
        try {
            ResponseEntity<CmsSigningBasketCreationResponse> restResponse = consentRestTemplate.postForEntity(signingBasketRemoteUrls.createSigningBasket(), signingBasket, CmsSigningBasketCreationResponse.class);
            return CmsResponse.<CmsSigningBasketCreationResponse>builder()
                       .payload(restResponse.getBody())
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't create new signing basket, HTTP response status: {}", cmsRestException.getHttpStatus());
        }
        return CmsResponse.<CmsSigningBasketCreationResponse>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<CmsSigningBasketConsentsAndPaymentsResponse> getConsentsAndPayments(List<String> consents, List<String> payments) {
        try {
            ResponseEntity<CmsSigningBasketConsentsAndPaymentsResponse> restResponse = consentRestTemplate.getForEntity(signingBasketRemoteUrls.getConsentsAndPayments(), CmsSigningBasketConsentsAndPaymentsResponse.class, consents, payments);
            return CmsResponse.<CmsSigningBasketConsentsAndPaymentsResponse>builder()
                       .payload(restResponse.getBody())
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't retrieve consents and payments in basket, HTTP response status: {}", cmsRestException.getHttpStatus());
        }
        return CmsResponse.<CmsSigningBasketConsentsAndPaymentsResponse>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }
}
