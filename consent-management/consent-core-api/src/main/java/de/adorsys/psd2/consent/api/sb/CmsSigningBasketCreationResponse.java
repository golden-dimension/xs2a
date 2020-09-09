package de.adorsys.psd2.consent.api.sb;

import lombok.Value;

@Value
public class CmsSigningBasketCreationResponse {
    private String basketId;
    private CmsSigningBasket cmsSigningBasket;
}
