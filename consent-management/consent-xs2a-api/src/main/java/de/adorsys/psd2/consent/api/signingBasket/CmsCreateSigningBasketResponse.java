package de.adorsys.psd2.consent.api.signingBasket;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CmsCreateSigningBasketResponse {
    private String basketId;
    private CmsSigningBasket signingBasket;
}
