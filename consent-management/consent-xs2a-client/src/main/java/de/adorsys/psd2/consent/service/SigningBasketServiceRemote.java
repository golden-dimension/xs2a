package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasket;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketConsentsAndPaymentsResponse;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketCreationResponse;
import de.adorsys.psd2.consent.api.service.SigningBasketServiceEncrypted;
import de.adorsys.psd2.consent.config.CmsRestException;
import de.adorsys.psd2.consent.config.SigningBasketRemoteUrls;
import de.adorsys.psd2.xs2a.core.sb.SigningBasketTransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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
            ResponseEntity<CmsSigningBasketConsentsAndPaymentsResponse> restResponse = consentRestTemplate.getForEntity(
                UriComponentsBuilder.fromHttpUrl(signingBasketRemoteUrls.getConsentsAndPayments())
                    .queryParam("consents", consents)
                    .queryParam("payments", payments)
                    .build()
                    .toString(),
                CmsSigningBasketConsentsAndPaymentsResponse.class);
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

    @Override
    public CmsResponse<Boolean> updateTransactionStatusById(String basketId, SigningBasketTransactionStatus transactionStatus) {
        try {
            consentRestTemplate.put(signingBasketRemoteUrls.updateTransactionStatus(), null, basketId, transactionStatus.getTransactionStatus());
            return CmsResponse.<Boolean>builder()
                       .payload(true)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't update transaction status by basket ID {}, HTTP response status: {}",
                     basketId, cmsRestException.getHttpStatus());
        }

        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> updateMultilevelScaRequired(String encryptedBasketId, boolean multilevelScaRequired) {
        try {
            Boolean updateResponse = consentRestTemplate.exchange(signingBasketRemoteUrls.updateMultilevelScaRequired(),
                                                                  HttpMethod.PUT, null, Boolean.class, encryptedBasketId, multilevelScaRequired)
                                         .getBody();
            return CmsResponse.<Boolean>builder()
                       .payload(updateResponse)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't update multilevel SCA required by basket ID {}, HTTP response status: {}",
                     encryptedBasketId, cmsRestException.getHttpStatus());
        }

        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }
}
