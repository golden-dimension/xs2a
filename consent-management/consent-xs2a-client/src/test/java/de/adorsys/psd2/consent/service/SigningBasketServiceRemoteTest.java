package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasket;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketConsentsAndPaymentsResponse;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketCreationResponse;
import de.adorsys.psd2.consent.config.CmsRestException;
import de.adorsys.psd2.consent.config.SigningBasketRemoteUrls;
import de.adorsys.psd2.xs2a.core.sb.SigningBasketTransactionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
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
        when(consentRestTemplate.getForEntity(UriComponentsBuilder.fromHttpUrl(URL)
                                                  .queryParam("consents", consentIds)
                                                  .queryParam("payments", paymentIds)
                                                  .build()
                                                  .toString(), CmsSigningBasketConsentsAndPaymentsResponse.class))
            .thenReturn(ResponseEntity.ok(cmsSigningBasketConsentsAndPaymentsResponse));

        CmsResponse<CmsSigningBasketConsentsAndPaymentsResponse> actualResponse = signingBasketServiceRemote.getConsentsAndPayments(consentIds, paymentIds);

        assertTrue(actualResponse.isSuccessful());
        assertEquals(cmsSigningBasketConsentsAndPaymentsResponse, actualResponse.getPayload());
    }

    @Test
    void getConsentsAndPayments_cmsRestException() {
        List<String> consentIds = List.of(CONSENT_ID);
        List<String> paymentIds = List.of(PAYMENT_ID);

        when(signingBasketRemoteUrls.getConsentsAndPayments()).thenReturn(URL);
        when(consentRestTemplate.getForEntity(UriComponentsBuilder.fromHttpUrl(URL)
                                                  .queryParam("consents", consentIds)
                                                  .queryParam("payments", paymentIds)
                                                  .build()
                                                  .toString(), CmsSigningBasketConsentsAndPaymentsResponse.class))
            .thenThrow(CmsRestException.class);

        CmsResponse<CmsSigningBasketConsentsAndPaymentsResponse> actualResponse = signingBasketServiceRemote.getConsentsAndPayments(consentIds, paymentIds);

        assertTrue(actualResponse.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actualResponse.getError());
    }

    @Test
    void updateTransactionStatusById_success() {
        when(signingBasketRemoteUrls.updateTransactionStatus()).thenReturn(URL);

        CmsResponse<Boolean> cmsResponse = signingBasketServiceRemote.updateTransactionStatusById(BASKET_ID, SigningBasketTransactionStatus.ACTC);

        assertTrue(cmsResponse.isSuccessful());
        assertTrue(cmsResponse.getPayload());
    }

    @Test
    void updateTransactionStatusById_cmsRestException() {
        when(signingBasketRemoteUrls.updateTransactionStatus()).thenReturn(URL);
        doThrow(CmsRestException.class)
            .when(consentRestTemplate)
            .put(URL, null, BASKET_ID, SigningBasketTransactionStatus.ACTC.toString());

        CmsResponse<Boolean> cmsResponse = signingBasketServiceRemote.updateTransactionStatusById(BASKET_ID, SigningBasketTransactionStatus.ACTC);

        assertFalse(cmsResponse.hasError());
        assertFalse(cmsResponse.getPayload());
    }

    @Test
    void updateMultilevelScaRequired_success() {
        when(signingBasketRemoteUrls.updateMultilevelScaRequired()).thenReturn(URL);
        when(consentRestTemplate.exchange(URL, HttpMethod.PUT, null, Boolean.class, BASKET_ID, true))
            .thenReturn(ResponseEntity.of(Optional.of(true)));

        CmsResponse<Boolean> cmsResponse = signingBasketServiceRemote.updateMultilevelScaRequired(BASKET_ID, true);

        assertTrue(cmsResponse.isSuccessful());
        assertTrue(cmsResponse.getPayload());
    }

    @Test
    void updateMultilevelScaRequired_failure() {
        when(signingBasketRemoteUrls.updateMultilevelScaRequired()).thenReturn(URL);
        when(consentRestTemplate.exchange(URL, HttpMethod.PUT, null, Boolean.class, BASKET_ID, true))
            .thenThrow(CmsRestException.class);

        CmsResponse<Boolean> cmsResponse = signingBasketServiceRemote.updateMultilevelScaRequired(BASKET_ID, true);

        assertFalse(cmsResponse.hasError());
        assertFalse(cmsResponse.getPayload());
    }
}
