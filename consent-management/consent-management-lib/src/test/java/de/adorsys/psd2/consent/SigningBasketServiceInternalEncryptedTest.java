package de.adorsys.psd2.consent;

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasket;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketConsentsAndPaymentsResponse;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketCreationResponse;
import de.adorsys.psd2.consent.api.service.SigningBasketService;
import de.adorsys.psd2.consent.service.security.SecurityDataService;
import de.adorsys.psd2.xs2a.core.sb.SigningBasketTransactionStatus;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SigningBasketServiceInternalEncryptedTest {
    private JsonReader jsonReader = new JsonReader();

    /**
     * BASKET
     */
    private static final String ENCRYPTED_EXTERNAL_ID = "encrypted c5fa2f82-a81c-4c28-979d-563694f1c3ad";
    private static final String EXTERNAL_ID = "c5fa2f82-a81c-4c28-979d-563694f1c3ad";

    /**
     * CONSENT
     */
    private static final String ENCRYPTED_CONSENT_ID = "encrypted consent id";
    private static final String CONSENT_ID = "consent id";

    /**
     * PAYMENT
     */
    private static final String ENCRYPTED_PAYMENT_ID = "encrypted payment id";
    private static final String PAYMENT_ID = "payment id";

    @InjectMocks
    private SigningBasketServiceInternalEncrypted signingBasketServiceInternalEncrypted;
    @Mock
    private SecurityDataService securityDataService;
    @Mock
    private SigningBasketService signingBasketService;

    @Test
    public void createSigningBasket_success() {
        CmsSigningBasket cmsSigningBasket = buildCmsSigningBasket();
        CmsSigningBasketCreationResponse cmsSigningBasketCreationResponse = buildCmsSigningBasketCreationResponse();
        when(signingBasketService.createSigningBasket(cmsSigningBasket)).thenReturn(CmsResponse.<CmsSigningBasketCreationResponse>builder()
                                                                                        .payload(cmsSigningBasketCreationResponse)
                                                                                        .build());
        when(securityDataService.encryptId(cmsSigningBasketCreationResponse.getBasketId())).thenReturn(Optional.of(ENCRYPTED_EXTERNAL_ID));


        CmsResponse<CmsSigningBasketCreationResponse> cmsResponse = signingBasketServiceInternalEncrypted.createSigningBasket(cmsSigningBasket);

        assertTrue(cmsResponse.isSuccessful());
        CmsSigningBasketCreationResponse payload = cmsResponse.getPayload();
        assertNotNull(payload);
        assertEquals(payload.getBasketId(), ENCRYPTED_EXTERNAL_ID);
        assertEquals(payload.getCmsSigningBasket(), cmsSigningBasketCreationResponse.getCmsSigningBasket());
    }

    @Test
    public void createSigningBasket_errorful_cms_response() {
        CmsSigningBasket cmsSigningBasket = buildCmsSigningBasket();
        when(signingBasketService.createSigningBasket(cmsSigningBasket)).thenReturn(CmsResponse.<CmsSigningBasketCreationResponse>builder()
                                                                                        .error(CmsError.TECHNICAL_ERROR)
                                                                                        .build());

        CmsResponse<CmsSigningBasketCreationResponse> cmsResponse = signingBasketServiceInternalEncrypted.createSigningBasket(cmsSigningBasket);

        assertFalse(cmsResponse.isSuccessful());
        assertNull(cmsResponse.getPayload());
        assertTrue(cmsResponse.hasError());
        assertEquals(cmsResponse.getError(), CmsError.TECHNICAL_ERROR);
    }

    @Test
    public void createSigningBasket_encryption_error() {
        CmsSigningBasket cmsSigningBasket = buildCmsSigningBasket();
        CmsSigningBasketCreationResponse cmsSigningBasketCreationResponse = buildCmsSigningBasketCreationResponse();
        when(signingBasketService.createSigningBasket(cmsSigningBasket)).thenReturn(CmsResponse.<CmsSigningBasketCreationResponse>builder()
                                                                                        .payload(cmsSigningBasketCreationResponse)
                                                                                        .build());
        when(securityDataService.encryptId(cmsSigningBasketCreationResponse.getBasketId())).thenReturn(Optional.empty());


        CmsResponse<CmsSigningBasketCreationResponse> cmsResponse = signingBasketServiceInternalEncrypted.createSigningBasket(cmsSigningBasket);

        assertFalse(cmsResponse.isSuccessful());
        assertNull(cmsResponse.getPayload());
        assertTrue(cmsResponse.hasError());
        assertEquals(cmsResponse.getError(), CmsError.TECHNICAL_ERROR);
    }

    @Test
    public void getConsentsAndPayments_success() {
        CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse = buildCmsSigningBasketConsentsAndPaymentsResponse();
        List<String> consentIds = Collections.singletonList(ENCRYPTED_CONSENT_ID);
        List<String> paymentIds = Collections.singletonList(ENCRYPTED_PAYMENT_ID);

        Map<String, String> encryptedToDecrypted = new HashMap<>();
        encryptedToDecrypted.put(ENCRYPTED_CONSENT_ID, CONSENT_ID);
        encryptedToDecrypted.put(ENCRYPTED_PAYMENT_ID, PAYMENT_ID);
        Stream.concat(consentIds.stream(), paymentIds.stream()).forEach(encryptedId -> {
            when(securityDataService.decryptId(encryptedId)).thenReturn(Optional.of(encryptedToDecrypted.get(encryptedId)));
        });

        when(signingBasketService.getConsentsAndPayments(Collections.singletonList(CONSENT_ID), Collections.singletonList(PAYMENT_ID)))
            .thenReturn(CmsResponse.<CmsSigningBasketConsentsAndPaymentsResponse>builder().payload(cmsSigningBasketConsentsAndPaymentsResponse).build());


        CmsResponse<CmsSigningBasketConsentsAndPaymentsResponse> cmsResponse = signingBasketServiceInternalEncrypted.getConsentsAndPayments(consentIds, paymentIds);

        assertTrue(cmsResponse.isSuccessful());
        CmsSigningBasketConsentsAndPaymentsResponse payload = cmsResponse.getPayload();
        assertNotNull(payload);
        assertEquals(payload.getConsents(), cmsSigningBasketConsentsAndPaymentsResponse.getConsents());
        assertEquals(payload.getPayments(), cmsSigningBasketConsentsAndPaymentsResponse.getPayments());
    }

    @Test
    public void getConsentsAndPayments_decryption_error() {
        CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse = buildCmsSigningBasketConsentsAndPaymentsResponse();
        List<String> consentIds = Collections.singletonList(ENCRYPTED_CONSENT_ID);
        List<String> paymentIds = Collections.singletonList(ENCRYPTED_PAYMENT_ID);

        when(securityDataService.decryptId(any())).thenReturn(Optional.empty());

        CmsResponse<CmsSigningBasketConsentsAndPaymentsResponse> cmsResponse = signingBasketServiceInternalEncrypted.getConsentsAndPayments(consentIds, paymentIds);

        assertFalse(cmsResponse.isSuccessful());
        assertTrue(cmsResponse.hasError());
        CmsSigningBasketConsentsAndPaymentsResponse payload = cmsResponse.getPayload();
        assertNull(payload);
        assertEquals(cmsResponse.getError(), CmsError.TECHNICAL_ERROR);
    }

    @Test
    public void getConsentsAndPayments_errorful_cms_response() {
        CmsSigningBasketConsentsAndPaymentsResponse cmsSigningBasketConsentsAndPaymentsResponse = buildCmsSigningBasketConsentsAndPaymentsResponse();
        List<String> consentIds = Collections.singletonList(ENCRYPTED_CONSENT_ID);
        List<String> paymentIds = Collections.singletonList(ENCRYPTED_PAYMENT_ID);

        Map<String, String> encryptedToDecrypted = new HashMap<>();
        encryptedToDecrypted.put(ENCRYPTED_CONSENT_ID, CONSENT_ID);
        encryptedToDecrypted.put(ENCRYPTED_PAYMENT_ID, PAYMENT_ID);
        Stream.concat(consentIds.stream(), paymentIds.stream()).forEach(encryptedId -> {
            when(securityDataService.decryptId(encryptedId)).thenReturn(Optional.of(encryptedToDecrypted.get(encryptedId)));
        });

        when(signingBasketService.getConsentsAndPayments(Collections.singletonList(CONSENT_ID), Collections.singletonList(PAYMENT_ID)))
            .thenReturn(CmsResponse.<CmsSigningBasketConsentsAndPaymentsResponse>builder().error(CmsError.TECHNICAL_ERROR).build());


        CmsResponse<CmsSigningBasketConsentsAndPaymentsResponse> cmsResponse = signingBasketServiceInternalEncrypted.getConsentsAndPayments(consentIds, paymentIds);

        assertFalse(cmsResponse.isSuccessful());
        assertTrue(cmsResponse.hasError());
        CmsSigningBasketConsentsAndPaymentsResponse payload = cmsResponse.getPayload();
        assertNull(payload);
        assertEquals(cmsResponse.getError(), CmsError.TECHNICAL_ERROR);
    }

    @Test
    public void updateTransactionStatusById_success() {
        when(securityDataService.decryptId(ENCRYPTED_EXTERNAL_ID)).thenReturn(Optional.of(EXTERNAL_ID));
        when(signingBasketService.updateTransactionStatusById(eq(EXTERNAL_ID), any())).thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        CmsResponse<Boolean> cmsResponse = signingBasketServiceInternalEncrypted.updateTransactionStatusById(ENCRYPTED_EXTERNAL_ID, SigningBasketTransactionStatus.ACTC);

        assertTrue(cmsResponse.isSuccessful());
        assertTrue(cmsResponse.getPayload());
    }

    @Test
    public void updateTransactionStatusById_failure() {
        when(securityDataService.decryptId(ENCRYPTED_EXTERNAL_ID)).thenReturn(Optional.empty());

        CmsResponse<Boolean> cmsResponse = signingBasketServiceInternalEncrypted.updateTransactionStatusById(ENCRYPTED_EXTERNAL_ID, SigningBasketTransactionStatus.ACTC);

        assertFalse(cmsResponse.isSuccessful());
        assertNull(cmsResponse.getPayload());
        assertEquals(cmsResponse.getError(), CmsError.TECHNICAL_ERROR);
    }

    @Test
    public void updateMultilevelScaRequired_success() {
        when(securityDataService.decryptId(ENCRYPTED_EXTERNAL_ID)).thenReturn(Optional.of(EXTERNAL_ID));
        when(signingBasketService.updateMultilevelScaRequired(EXTERNAL_ID, true))
            .thenReturn(CmsResponse.<Boolean>builder().payload(true).build());

        CmsResponse<Boolean> cmsResponse = signingBasketServiceInternalEncrypted.updateMultilevelScaRequired(ENCRYPTED_EXTERNAL_ID, true);

        assertTrue(cmsResponse.isSuccessful());
        assertTrue(cmsResponse.getPayload());
    }

    @Test
    public void updateMultilevelScaRequired_failure() {
        when(securityDataService.decryptId(ENCRYPTED_EXTERNAL_ID)).thenReturn(Optional.empty());

        CmsResponse<Boolean> cmsResponse = signingBasketServiceInternalEncrypted.updateMultilevelScaRequired(ENCRYPTED_EXTERNAL_ID, true);

        assertFalse(cmsResponse.isSuccessful());
        assertNull(cmsResponse.getPayload());
        assertEquals(cmsResponse.getError(), CmsError.TECHNICAL_ERROR);
    }

    private CmsSigningBasketConsentsAndPaymentsResponse buildCmsSigningBasketConsentsAndPaymentsResponse() {
        CmsSigningBasket cmsSigningBasket = buildCmsSigningBasket();
        return new CmsSigningBasketConsentsAndPaymentsResponse(cmsSigningBasket.getConsents(), cmsSigningBasket.getPayments());
    }

    private CmsSigningBasketCreationResponse buildCmsSigningBasketCreationResponse() {
        return new CmsSigningBasketCreationResponse(EXTERNAL_ID, buildCmsSigningBasket());
    }

    private CmsSigningBasket buildCmsSigningBasket() {
        return jsonReader.getObjectFromFile("json/service/cms-signing-basket-empty.json", CmsSigningBasket.class);
    }
}
