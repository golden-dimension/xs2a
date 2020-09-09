package de.adorsys.psd2.consent.api.sb;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationTemplate;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sb.SigningBasketTransactionStatus;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CmsSigningBasket {
    private String id;
    private String instanceId;
    private List<CmsConsent> consents;
    private List<PisCommonPaymentResponse> payments;
    private AuthorisationTemplate authorisationTemplate;
    private SigningBasketTransactionStatus transactionStatus;
    private String internalRequestId;
    private List<PsuIdData> psuIdDatas;
    private boolean multilevelScaRequired;
}
