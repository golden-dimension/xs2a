package de.adorsys.psd2.consent.api.signingBasket;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.pis.CommonPaymentData;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationTemplate;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.signingBasket.TransactionStatus;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CmsSigningBasket {
    private String id;
    private List<CmsConsent> consents;
    private List<CommonPaymentData> payments;
    private AuthorisationTemplate authorisationTemplate;
    private TransactionStatus transactionStatus;
    private String internalRequestId;
    private List<PsuIdData> psuIdDatas;
}
