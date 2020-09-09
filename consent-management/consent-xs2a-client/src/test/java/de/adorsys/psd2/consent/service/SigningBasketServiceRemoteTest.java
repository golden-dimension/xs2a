package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasket;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketConsentsAndPaymentsResponse;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketCreationResponse;
import de.adorsys.psd2.consent.config.CmsRestException;
import de.adorsys.psd2.consent.config.SigningBasketRemoteUrls;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SigningBasketServiceRemoteTest {
    private static final String URL = "http://some.url";

    private static final String BASKET_ID = "basket id";
    private static final String CONSENT_ID = "consent id";
    private static final String PAYMENT_ID = "payment id";

    @InjectMocks
    private SigningBasketServiceRemote signingBasketServiceRemote;
    @Mock
    private RestTemplate consentRestTemplate;
    @Mock
    private SigningBasketRemoteUrls signingBasketRemoteUrls;

    @Test
    public void createSigningBasket_successful() {
        CmsSigningBasket basketToCreate = new CmsSigningBasket();
        CmsSigningBasket createdBasket = new CmsSigningBasket();
        CmsSigningBasketCreationResponse createConsentResponse = new CmsSigningBasketCreationResponse(BASKET_ID, createdBasket);
        when(signingBasketRemoteUrls.createSigningBasket()).thenReturn(URL);
        when(consentRestTemplate.postForEntity(URL, basketToCreate, CmsSigningBasketCreationResponse.class))
            .thenReturn(new ResponseEntity<>(createConsentResponse, HttpStatus.CREATED));

        CmsResponse<CmsSigningBasketCreationResponse> actualResponse = signingBasketServiceRemote.createSigningBasket(basketToCreate);

        assertTrue(actualResponse.isSuccessful());
        assertEquals(createConsentResponse, actualResponse.getPayload());
    }

    @Test
    void createSigningBasket_cmsRestException() {
        CmsSigningBasket basketToCreate = new CmsSigningBasket();
        when(signingBasketRemoteUrls.createSigningBasket()).thenReturn(URL);
        when(consentRestTemplate.postForEntity(URL, basketToCreate, CmsSigningBasketCreationResponse.class))
            .thenThrow(CmsRestException.class);

        CmsResponse<CmsSigningBasketCreationResponse> actualResponse = signingBasketServiceRemote.createSigningBasket(basketToCreate);

        assertTrue(actualResponse.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actualResponse.getError());
    }

    @Test
    void getConsentsAndPayments_successful() {
        List<String> consentIds = List.of(CONSENT_ID);
        List<String> paymentIds = List.of(PAYMENT_ID);

        when(signingBasketRemoteUrls.getConsentsAndPayments())
            .thenReturn(URL);
        CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse = new CmsSigningBasketConsentsAndPaymentsResponse(Collections.emptyList(), Collections.emptyList());
        when(consentRestTemplate.getForEntity(UriComponentsBuilder.fromHttpUrl(signingBasketRemoteUrls.getConsentsAndPayments())
                                                  .queryParam("consents", consentIds)
                                                  .queryParam("payments", paymentIds)
                                                  .build()
                                                  .toString(), CmsSigningBasketConsentsAndPaymentsResponse.class))
            .thenReturn(ResponseEntity.ok(cmsSigningBasketConsentsAndPaymentsResponse));

        CmsResponse<CmsSigningBasketConsentsAndPaymentsResponse> actualResponse = signingBasketServiceRemote.getConsentsAndPayments(consentIds, paymentIds);

        assertTrue(actualResponse.isSuccessful());
        assertEquals(cmsSigningBasketConsentsAndPaymentsResponse, actualResponse.getPayload());
    }
}
