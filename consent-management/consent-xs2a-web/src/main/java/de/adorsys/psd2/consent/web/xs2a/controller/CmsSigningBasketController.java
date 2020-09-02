package de.adorsys.psd2.consent.web.xs2a.controller;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.CmsSigningBasketApi;
import de.adorsys.psd2.consent.api.service.SigningBasketServiceEncrypted;
import de.adorsys.psd2.consent.api.signingbasket.CmsSigningBasket;
import de.adorsys.psd2.consent.api.signingbasket.CmsSigningBasketConsentsAndPaymentsResponse;
import de.adorsys.psd2.consent.api.signingbasket.CmsSigningBasketCreationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CmsSigningBasketController implements CmsSigningBasketApi {
    private final SigningBasketServiceEncrypted signingBasketServiceEncrypted;

    @Override
    public ResponseEntity<Object> createSigningBasket(CmsSigningBasket request) {
        CmsResponse<CmsSigningBasketCreationResponse> cmsResponse = signingBasketServiceEncrypted.createSigningBasket(request);

        if (cmsResponse.hasError()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(cmsResponse, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Object> getConsentsAndPayments(List<String> consents, List<String> payments) {
        CmsResponse<CmsSigningBasketConsentsAndPaymentsResponse> cmsResponse = signingBasketServiceEncrypted.getConsentsAndPayments(consents, payments);

        if (cmsResponse.hasError()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(cmsResponse, HttpStatus.OK);
    }
}
