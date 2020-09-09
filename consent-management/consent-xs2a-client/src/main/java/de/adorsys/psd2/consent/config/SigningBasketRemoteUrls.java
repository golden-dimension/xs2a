package de.adorsys.psd2.consent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SigningBasketRemoteUrls {
    @Value("${xs2a.cms.consent-service.baseurl:http://localhost:38080/api/v1}")
    private String consentServiceBaseUrl;

    public String createSigningBasket(){ return consentServiceBaseUrl + "/signing-baskets/"; };

    public String getConsentsAndPayments(){ return consentServiceBaseUrl + "/signing-baskets/"; }

    public String updateTransactionStatus(){ return consentServiceBaseUrl + "/signing-baskets/{encrypted-basket-id}/status/{status}"; }

    public String updateMultilevelScaRequired(){ return consentServiceBaseUrl + "/signing-baskets/{encrypted-basket-id}/multilevel-sca?multilevel-sca={multilevel-sca}"; }
}
