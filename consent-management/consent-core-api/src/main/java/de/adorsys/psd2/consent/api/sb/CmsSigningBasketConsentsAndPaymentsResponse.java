package de.adorsys.psd2.consent.api.sb;

import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import lombok.Value;

import java.util.List;

@Value
public class CmsSigningBasketConsentsAndPaymentsResponse {
    private final List<CmsConsent> consents;
    private final List<PisCommonPaymentResponse> payments;
}
