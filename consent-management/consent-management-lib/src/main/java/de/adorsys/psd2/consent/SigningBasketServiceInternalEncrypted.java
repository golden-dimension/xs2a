package de.adorsys.psd2.consent;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasket;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketConsentsAndPaymentsResponse;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketCreationResponse;
import de.adorsys.psd2.consent.api.service.SigningBasketService;
import de.adorsys.psd2.consent.api.service.SigningBasketServiceEncrypted;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.sb.SigningBasketTransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.consent.api.CmsError.TECHNICAL_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SigningBasketServiceInternalEncrypted implements SigningBasketServiceEncrypted {
    private final SecurityDataService securityDataService;
    private final SigningBasketService signingBasketService;

    @Override
    @Transactional
    public CmsResponse<CmsSigningBasketCreationResponse> createSigningBasket(CmsSigningBasket request) {
        CmsResponse<CmsSigningBasketCreationResponse> serviceResponse = signingBasketService.createSigningBasket(request);

        if (serviceResponse.hasError()) {
            return serviceResponse;
        }

        CmsSigningBasketCreationResponse cmsSigningBasketCreationResponse = serviceResponse.getPayload();

        Optional<String> encryptIdOptional = securityDataService.encryptId(cmsSigningBasketCreationResponse.getBasketId());

        if (encryptIdOptional.isEmpty()) {
            log.info("Basket ID: [{}]. Create signing basket failed, couldn't encrypt basket id", cmsSigningBasketCreationResponse.getBasketId());
            return CmsResponse.<CmsSigningBasketCreationResponse>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return CmsResponse.<CmsSigningBasketCreationResponse>builder()
                   .payload(new CmsSigningBasketCreationResponse(encryptIdOptional.get(), cmsSigningBasketCreationResponse.getCmsSigningBasket()))
                   .build();
    }

    @Override
    public CmsResponse<CmsSigningBasketConsentsAndPaymentsResponse> getConsentsAndPayments(List<String> consents, List<String> payments) {
        Optional<List<String>> decryptedConsents = decrypt(consents);
        Optional<List<String>> decryptedPayments = decrypt(payments);

        if (decryptedPayments.isEmpty() || decryptedConsents.isEmpty()) {
            log.info("Couldn't decrypt an ID of consents/payments");
            return CmsResponse.<CmsSigningBasketConsentsAndPaymentsResponse>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        CmsResponse<CmsSigningBasketConsentsAndPaymentsResponse> serviceResponse = signingBasketService.getConsentsAndPayments(decryptedConsents.get(), decryptedPayments.get());

        if (serviceResponse.hasError()) {
            return serviceResponse;
        }

        CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketPaymentAndConsents = serviceResponse.getPayload();

        return CmsResponse.<CmsSigningBasketConsentsAndPaymentsResponse>builder()
                   .payload(cmsSigningBasketPaymentAndConsents)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> blockBasket(String encryptedBasketId) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedBasketId);

        if (decryptIdOptional.isEmpty()) {
            log.info("Encrypted Basket ID: [{}]. Update transaction status of basket by id failed, couldn't decrypt basket id", encryptedBasketId);
            return CmsResponse.<Boolean>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return signingBasketService.blockBasket(decryptIdOptional.get());
    }
    
    @Override
    @Transactional
    public CmsResponse<Boolean> updateTransactionStatusById(String encryptedBasketId, SigningBasketTransactionStatus transactionStatus) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedBasketId);

        if (decryptIdOptional.isEmpty()) {
            log.info("Encrypted Basket ID: [{}]. Update transaction status of basket by id failed, couldn't decrypt basket id", encryptedBasketId);
            return CmsResponse.<Boolean>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return signingBasketService.updateTransactionStatusById(decryptIdOptional.get(), transactionStatus);
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> updateMultilevelScaRequired(String encryptedBasketId, boolean multilevelScaRequired) {
        Optional<String> decryptIdOptional = securityDataService.decryptId(encryptedBasketId);

        if (decryptIdOptional.isEmpty()) {
            log.info("Encrypted Basket ID: [{}]. Update multilevelScaRequired failed, couldn't decrypt basket id",
                     encryptedBasketId);
            return CmsResponse.<Boolean>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return signingBasketService.updateMultilevelScaRequired(decryptIdOptional.get(), multilevelScaRequired);
    }

    private Optional<List<String>> decrypt(List<String> content) {
        List<String> modifiedIds = new ArrayList<>();
        for (String id : content) {
            Optional<String> decryptId = securityDataService.decryptId(id);

            if (decryptId.isEmpty()) {
                return Optional.empty();
            }

            modifiedIds.add(decryptId.get());
        }
        return Optional.of(modifiedIds);
    }
}
