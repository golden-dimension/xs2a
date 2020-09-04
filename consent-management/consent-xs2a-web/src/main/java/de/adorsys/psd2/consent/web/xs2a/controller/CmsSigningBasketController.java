package de.adorsys.psd2.consent.web.xs2a.controller;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.CmsSigningBasketApi;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasket;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketConsentsAndPaymentsResponse;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketCreationResponse;
import de.adorsys.psd2.consent.api.service.SigningBasketServiceEncrypted;
import de.adorsys.psd2.xs2a.core.sb.SigningBasketTransactionStatus;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
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

        return new ResponseEntity<>(cmsResponse.getPayload(), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Object> getConsentsAndPayments(List<String> consents, List<String> payments) {
        CmsResponse<CmsSigningBasketConsentsAndPaymentsResponse> cmsResponse = signingBasketServiceEncrypted.getConsentsAndPayments(consents, payments);

        if (cmsResponse.hasError()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        return new ResponseEntity<>(cmsResponse.getPayload(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> updateTransactionStatus(String encryptedBasketId, String transactionStatus) {
        CmsResponse<Boolean> cmsResponse = signingBasketServiceEncrypted.updateTransactionStatusById(encryptedBasketId, SigningBasketTransactionStatus.getByName(transactionStatus));

        if(cmsResponse.hasError() || BooleanUtils.isFalse(cmsResponse.getPayload())){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(cmsResponse.getPayload(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Object> updateMultilevelScaRequired(String encryptedBasketId, boolean multilevelSca) {
        CmsResponse<Boolean> cmsResponse = signingBasketServiceEncrypted.updateMultilevelScaRequired(encryptedBasketId, multilevelSca);

        if(cmsResponse.hasError() || BooleanUtils.isFalse(cmsResponse.getPayload())){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(cmsResponse.getPayload(), HttpStatus.OK);
    }
}
