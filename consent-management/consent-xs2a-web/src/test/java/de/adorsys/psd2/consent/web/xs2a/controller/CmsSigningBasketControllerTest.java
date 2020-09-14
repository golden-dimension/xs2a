package de.adorsys.psd2.consent.web.xs2a.controller;

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasket;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketConsentsAndPaymentsResponse;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketCreationResponse;
import de.adorsys.psd2.consent.api.service.SigningBasketServiceEncrypted;
import de.adorsys.psd2.xs2a.core.sb.SigningBasketTransactionStatus;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CmsSigningBasketControllerTest {
    private static final String EXTERNAL_ID = "external id";
    private static final String CONSENT_ID = "consent id";
    private static final String PAYMENT_ID = "payment id";

    private JsonReader jsonReader;

    @Mock
    private SigningBasketServiceEncrypted signingBasketServiceEncrypted;

    @InjectMocks
    private CmsSigningBasketController cmsSigningBasketController;

    @BeforeEach
    private void setup() {
        jsonReader = new JsonReader();
    }

    @Test
    void createSigningBasket_successful() {
        CmsSigningBasket request = buildCmsSigningBasket();
        CmsSigningBasketCreationResponse response = buildCmsSigningBasketCreationResponse();
        when(signingBasketServiceEncrypted.createSigningBasket(request)).thenReturn(CmsResponse.<CmsSigningBasketCreationResponse>builder()
                                                                                        .payload(response)
                                                                                        .build());

        ResponseEntity<Object> responseEntity = cmsSigningBasketController.createSigningBasket(request);
        assertEquals(responseEntity.getBody(), response);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.CREATED);
    }

    @Test
    void createSigningBasket_failure() {
        CmsSigningBasket request = buildCmsSigningBasket();
        when(signingBasketServiceEncrypted.createSigningBasket(request)).thenReturn(CmsResponse.<CmsSigningBasketCreationResponse>builder()
                                                                                        .error(CmsError.TECHNICAL_ERROR)
                                                                                        .build());
        ResponseEntity<Object> responseEntity = cmsSigningBasketController.createSigningBasket(request);
        assertNull(responseEntity.getBody());
        assertEquals(responseEntity.getStatusCode(), HttpStatus.NO_CONTENT);
    }

    @Test
    void getConsentsAndPayments_successful() {
        List<String> consentIds = List.of(CONSENT_ID);
        List<String> paymentIds = List.of(PAYMENT_ID);
        CmsSigningBasketConsentsAndPaymentsResponse response = buildCmsSigningBasketConsentsAndPaymentsResponse();

        when(signingBasketServiceEncrypted.getConsentsAndPayments(consentIds, paymentIds)).thenReturn(CmsResponse.<CmsSigningBasketConsentsAndPaymentsResponse>builder()
                                                                                                          .payload(response)
                                                                                                          .build());

        ResponseEntity<Object> responseEntity = cmsSigningBasketController.getConsentsAndPayments(consentIds, paymentIds);
        assertEquals(responseEntity.getBody(), response);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
    }

    @Test
    void getConsentsAndPayments_failure() {
        List<String> consentIds = List.of(CONSENT_ID);
        List<String> paymentIds = List.of(PAYMENT_ID);

        when(signingBasketServiceEncrypted.getConsentsAndPayments(consentIds, paymentIds))
            .thenReturn(CmsResponse.<CmsSigningBasketConsentsAndPaymentsResponse>builder()
                            .error(CmsError.TECHNICAL_ERROR)
                            .build());

        ResponseEntity<Object> responseEntity = cmsSigningBasketController.getConsentsAndPayments(consentIds, paymentIds);
        assertNull(responseEntity.getBody());
        assertEquals(responseEntity.getStatusCode(), HttpStatus.NO_CONTENT);
    }

    @Test
    void updateTransactionStatus_succesful() {
        when(signingBasketServiceEncrypted.updateTransactionStatusById(EXTERNAL_ID, SigningBasketTransactionStatus.ACTC))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());

        ResponseEntity<Object> responseEntity = cmsSigningBasketController.updateTransactionStatus(EXTERNAL_ID, SigningBasketTransactionStatus.ACTC.name());
        assertEquals(responseEntity.getBody(), true);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
    }

    @Test
    void updateTransactionStatus_failure(){
        when(signingBasketServiceEncrypted.updateTransactionStatusById(EXTERNAL_ID, SigningBasketTransactionStatus.ACTC))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(false)
                            .build());

        ResponseEntity<Object> responseEntity = cmsSigningBasketController.updateTransactionStatus(EXTERNAL_ID, SigningBasketTransactionStatus.ACTC.name());
        assertNull(responseEntity.getBody());
        assertEquals(responseEntity.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    void updateTransactionStatus_failure_cms_error() {
        when(signingBasketServiceEncrypted.updateTransactionStatusById(EXTERNAL_ID, SigningBasketTransactionStatus.ACTC))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .error(CmsError.TECHNICAL_ERROR)
                            .build());

        ResponseEntity<Object> responseEntity = cmsSigningBasketController.updateTransactionStatus(EXTERNAL_ID, SigningBasketTransactionStatus.ACTC.name());
        assertNull(responseEntity.getBody());
        assertEquals(responseEntity.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    void updateMultilevelScaRequired_succesful() {
        when(signingBasketServiceEncrypted.updateMultilevelScaRequired(EXTERNAL_ID, true))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(true)
                            .build());

        ResponseEntity<Object> responseEntity = cmsSigningBasketController.updateMultilevelScaRequired(EXTERNAL_ID, true);
        assertEquals(responseEntity.getBody(), true);
        assertEquals(responseEntity.getStatusCode(), HttpStatus.OK);
    }

    @Test
    void updateMultilevelScaRequired_failure_cms_error(){
        when(signingBasketServiceEncrypted.updateMultilevelScaRequired(EXTERNAL_ID, true))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .payload(false)
                            .build());

        ResponseEntity<Object> responseEntity = cmsSigningBasketController.updateMultilevelScaRequired(EXTERNAL_ID, true);
        assertNull(responseEntity.getBody());
        assertEquals(responseEntity.getStatusCode(), HttpStatus.NOT_FOUND);
    }

    @Test
    void updateMultilevelScaRequired_failure() {
        when(signingBasketServiceEncrypted.updateMultilevelScaRequired(EXTERNAL_ID, true))
            .thenReturn(CmsResponse.<Boolean>builder()
                            .error(CmsError.TECHNICAL_ERROR)
                            .build());

        ResponseEntity<Object> responseEntity = cmsSigningBasketController.updateMultilevelScaRequired(EXTERNAL_ID, true);
        assertNull(responseEntity.getBody());
        assertEquals(responseEntity.getStatusCode(), HttpStatus.NOT_FOUND);
    }


    private CmsSigningBasketCreationResponse buildCmsSigningBasketCreationResponse() {
        return new CmsSigningBasketCreationResponse(EXTERNAL_ID, buildCmsSigningBasket());
    }

    private CmsSigningBasketConsentsAndPaymentsResponse buildCmsSigningBasketConsentsAndPaymentsResponse() {
        CmsSigningBasket cmsSigningBasket = buildCmsSigningBasket();
        return new CmsSigningBasketConsentsAndPaymentsResponse(cmsSigningBasket.getConsents(), cmsSigningBasket.getPayments());
    }

    private CmsSigningBasket buildCmsSigningBasket() {
        return jsonReader.getObjectFromFile("json/controller/cms-signing-basket.json", CmsSigningBasket.class);
    }


}
