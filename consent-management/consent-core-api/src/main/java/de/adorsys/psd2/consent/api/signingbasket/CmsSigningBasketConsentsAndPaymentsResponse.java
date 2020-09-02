package de.adorsys.psd2.consent.api.signingbasket;

import lombok.Value;

import java.util.List;

@Value
public class CmsSigningBasketConsentsAndPaymentsResponse {
    private final List<CmsSigningBasketConsent> consents;
    private final List<CmsSigningBasketPayment> payments;
}
