package de.adorsys.psd2.consent.api.signingbasket;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CmsSigningBasketPayment {
    private String id;
}
