package de.adorsys.psd2.consent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SigningBasketRemoteUrls {
    @Value("${xs2a.cms.consent-service.baseurl:http://localhost:38080/api/v1}")
    private String consentServiceBaseUrl;

    public String createSigningBasket(){ return consentServiceBaseUrl + "/signingBasket/"; };

    public String getConsentsAndPayments(){ return consentServiceBaseUrl + "/signingBasket/"; }
}
