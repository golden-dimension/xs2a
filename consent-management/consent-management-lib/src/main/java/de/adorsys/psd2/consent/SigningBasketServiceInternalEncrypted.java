package de.adorsys.psd2.consent;

import de.adorsys.psd2.consent.api.signingBasket.*;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.service.SigningBasketService;
import de.adorsys.psd2.consent.api.service.SigningBasketServiceEncrypted;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public CmsResponse<CmsCreateSigningBasketResponse> createSigningBasket(CmsSigningBasket request) {
        CmsResponse<CmsCreateSigningBasketResponse> serviceResponse = signingBasketService.createSigningBasket(request);

        if(serviceResponse.hasError()){
            return serviceResponse;
        }

        CmsCreateSigningBasketResponse cmsCreateSigningBasketResponse = serviceResponse.getPayload();
        Optional<String> encryptIdOptional = securityDataService.encryptId(cmsCreateSigningBasketResponse.getBasketId());

        if (encryptIdOptional.isEmpty()) {
            log.info("Basket ID: [{}]. Create signing basket failed, couldn't encrypt basket id", cmsCreateSigningBasketResponse.getBasketId());
            return CmsResponse.<CmsCreateSigningBasketResponse>builder()
                       .error(TECHNICAL_ERROR)
                       .build();
        }

        return CmsResponse.<CmsCreateSigningBasketResponse>builder()
                   .payload(new CmsCreateSigningBasketResponse(encryptIdOptional.get(), cmsCreateSigningBasketResponse.getSigningBasket()))
                   .build();
    }
}
