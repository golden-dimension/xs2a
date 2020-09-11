package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasket;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketConsentsAndPaymentsResponse;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketCreationResponse;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.AuthorisationTemplateEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.sb.SigningBasketEntity;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.SigningBasketRepository;
import de.adorsys.psd2.consent.service.mapper.CmsConsentMapper;
import de.adorsys.psd2.consent.service.mapper.CmsSigningBasketMapper;
import de.adorsys.psd2.consent.service.mapper.PisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.sb.SigningBasketTransactionStatus;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SigningBasketServiceInternalTest {
    private JsonReader jsonReader = new JsonReader();

    /**
     * BASKET
     **/
    private static final String EXTERNAL_ID = "9e22280f-1ee9-4ce1-a19b-d31978b64da6";
    private static final PsuData PSU_DATA = new PsuData("marion.muller", null, null, null, null, null);
    private static final String REDIRECT_URI = "http://test.com";
    private static final String NOK_REDIRECT_URI = "http://test.nok.com";

    /**
     * CONSENT
     **/
    private static final String CONSENT_ID_AIS_1 = "cb03f832-c1a0-45fd-a260-c476fd3931c1";
    private static final String CONSENT_ID_AIS_2 = "8eb5f69f-4f76-446d-a365-5fb96ecf1807";
    private static final String CONSENT_ID_PIIS = "0caf8353-45be-4c4c-957e-e315f1832500";

    /**
     * PAYMENTS
     */
    private static final String PAYMENT_ID_1 = "1_v885CCThYhfwGtCKuVrw";
    private static final String PAYMENT_ID_2 = "FRY14fWpR1ot-mkFVR77x0";

    /**
     * AUTHORISATIONS
     */
    private static final String AUTHORISATION_ID_AIS_1_1 = "b2028c1e-253d-414a-89f1-4ba34a889b44";
    private static final String AUTHORISATION_ID_AIS_1_2 = "9175536a-ea6a-4f1c-8e30-5c7833c4dab1";

    private static final String AUTHORISATION_ID_AIS_2 = "cf1de47a-41d0-4b7e-b7ed-c1341bdf1721";

    private static final String AUTHORISATION_ID_PIIS = "843af89f-e9ef-4e7b-b1ed-80d5d9809f57";

    private static final String AUTHORISATION_ID_PAYMENT_1 = "f4443e9f-24ab-401d-87c3-c05fefebb428";
    private static final String AUTHORISATION_ID_PAYMENT_2 = "767dee3c-b63f-4af4-b466-d5a49bee9367";


    @InjectMocks
    private SigningBasketServiceInternal signingBasketService;
    @Mock
    private CmsSigningBasketMapper cmsSigningBasketMapper;
    @Mock
    private SigningBasketRepository signingBasketRepository;
    @Mock
    private ConsentJpaRepository consentRepository;
    @Mock
    private PisCommonPaymentDataRepository paymentRepository;
    @Mock
    private AuthorisationRepository authorisationRepository;
    @Mock
    private AisConsentUsageService aisConsentUsageService;
    @Mock
    private CmsConsentMapper cmsConsentMapper;
    @Mock
    private PisCommonPaymentMapper pisCommonPaymentMapper;

    private Map<String, List<AuthorisationEntity>> authorisations = new HashMap<>();

    @BeforeEach
    public void setup() {
        authorisations.put(CONSENT_ID_AIS_1, List.of(buildAuthorisationEntity(AUTHORISATION_ID_AIS_1_1, CONSENT_ID_AIS_1),
                                                     buildAuthorisationEntity(AUTHORISATION_ID_AIS_1_2, CONSENT_ID_AIS_1)));
        authorisations.put(CONSENT_ID_AIS_2, List.of(buildAuthorisationEntity(AUTHORISATION_ID_AIS_2, CONSENT_ID_AIS_2)));
        authorisations.put(CONSENT_ID_PIIS, List.of(buildAuthorisationEntity(AUTHORISATION_ID_PIIS, CONSENT_ID_PIIS)));
        authorisations.put(PAYMENT_ID_1, List.of(buildAuthorisationEntity(AUTHORISATION_ID_PAYMENT_1, PAYMENT_ID_1)));
        authorisations.put(PAYMENT_ID_2, List.of(buildAuthorisationEntity(AUTHORISATION_ID_PAYMENT_2, PAYMENT_ID_2)));
    }

    @Test
    public void createSigningBasket_withoutPaymentsAndConsents_successful() {
        when(consentRepository.findAllByExternalIdIn(any())).thenReturn(Collections.emptyList());
        when(paymentRepository.findAllByPaymentIdIn(any())).thenReturn(Collections.emptyList());

        CmsSigningBasket cmsSigningBasket = buildEmptyCmsSigningBasket();
        SigningBasketEntity expected = buildSigningBasket(true);
        when(cmsSigningBasketMapper.mapToNewSigningBasket(cmsSigningBasket, Collections.emptyList(), Collections.emptyList()))
            .thenReturn(expected);
        when(signingBasketRepository.save(expected)).thenReturn(expected);
        when(cmsSigningBasketMapper.mapToCmsSigningBasket(expected, Collections.emptyMap(), Collections.emptyMap())).thenReturn(cmsSigningBasket);

        CmsResponse<CmsSigningBasketCreationResponse> cmsSigningBasketCreationResponse = signingBasketService.createSigningBasket(cmsSigningBasket);

        assertTrue(cmsSigningBasketCreationResponse.isSuccessful());
        CmsSigningBasketCreationResponse payload = cmsSigningBasketCreationResponse.getPayload();
        assertNotNull(payload);
        CmsSigningBasket payloadBasket = payload.getCmsSigningBasket();
        assertNotNull(payloadBasket);
        verify(signingBasketRepository).save(expected);
        assertEquals(payloadBasket.getConsents().size(), 0);
        assertEquals(payloadBasket.getPayments().size(), 0);
    }

    @Test
    public void createSigningBasket_multiplePaymentsAndConsents_succesful() {
        List<ConsentEntity> consents = buildConsents();
        List<PisCommonPaymentData> payments = buildPayments();
        when(consentRepository.findAllByExternalIdIn(consents.stream()
                                                         .map(ConsentEntity::getExternalId)
                                                         .collect(Collectors.toList()))).thenReturn(consents);
        when(paymentRepository.findAllByPaymentIdIn(payments.stream()
                                                        .map(PisCommonPaymentData::getPaymentId)
                                                        .collect(Collectors.toList()))).thenReturn(payments);

        Stream.concat(consents.stream(), payments.stream()).forEach(authorisable -> {
            when(authorisationRepository.findAllByParentExternalIdAndTypeIn(eq(authorisable.getExternalId()), any()))
                .thenReturn(this.authorisations.getOrDefault(authorisable.getExternalId(), Collections.emptyList()));
        });

        when(aisConsentUsageService.getUsageCounterMap(any())).thenReturn(Collections.emptyMap());


        SigningBasketEntity expected = buildSigningBasket(false);
        CmsSigningBasket cmsSigningBasket = buildCmsSigningBasket();
        when(cmsSigningBasketMapper.mapToNewSigningBasket(cmsSigningBasket, consents, payments))
            .thenReturn(expected);
        when(signingBasketRepository.save(expected)).thenReturn(expected);
        when(cmsSigningBasketMapper.mapToCmsSigningBasket(eq(expected), eq(this.authorisations), eq(Collections.emptyMap())))
            .thenReturn(cmsSigningBasket);

        CmsResponse<CmsSigningBasketCreationResponse> cmsSigningBasketCreationResponse = signingBasketService.createSigningBasket(cmsSigningBasket);

        assertTrue(cmsSigningBasketCreationResponse.isSuccessful());
        CmsSigningBasketCreationResponse payload = cmsSigningBasketCreationResponse.getPayload();
        assertNotNull(payload);
        CmsSigningBasket payloadBasket = payload.getCmsSigningBasket();
        assertNotNull(payloadBasket);
        verify(signingBasketRepository).save(expected);
        assertEquals(payloadBasket.getConsents().size(), 3);
        assertEquals(payloadBasket.getPayments().size(), 2);
    }

    @Test
    public void consentsAndPayments_successful() {
        List<ConsentEntity> consents = buildConsents();
        List<PisCommonPaymentData> payments = buildPayments();
        List<String> consentIds = consents.stream()
                                      .map(ConsentEntity::getExternalId)
                                      .collect(Collectors.toList());
        List<String> paymentids = payments.stream()
                                      .map(PisCommonPaymentData::getPaymentId)
                                      .collect(Collectors.toList());

        CmsSigningBasket basket = buildCmsSigningBasket();

        when(consentRepository.findAllByExternalIdIn(consentIds)).thenReturn(consents);
        when(paymentRepository.findAllByPaymentIdIn(paymentids)).thenReturn(payments);

        Stream.concat(consents.stream(), payments.stream()).forEach(authorisable -> {
            when(authorisationRepository.findAllByParentExternalIdAndTypeIn(eq(authorisable.getExternalId()), any()))
                .thenReturn(this.authorisations.getOrDefault(authorisable.getExternalId(), Collections.emptyList()));
        });

        when(aisConsentUsageService.getUsageCounterMap(any())).thenReturn(Collections.emptyMap());

        when(cmsConsentMapper.mapToCmsConsents(eq(consents), eq(this.authorisations), eq(Collections.emptyMap()))).thenReturn(basket.getConsents());

        when(pisCommonPaymentMapper.mapToPisCommonPaymentResponses(eq(payments), eq(this.authorisations))).thenReturn(basket.getPayments()).thenReturn(basket.getPayments());

        CmsResponse<CmsSigningBasketConsentsAndPaymentsResponse> cmsResponse = signingBasketService.getConsentsAndPayments(consentIds, paymentids);
        assertTrue(cmsResponse.isSuccessful());
        CmsSigningBasketConsentsAndPaymentsResponse payload = cmsResponse.getPayload();

        assertNotNull(payload);
        assertEquals(payload.getConsents(), basket.getConsents());
        assertEquals(payload.getPayments(), basket.getPayments());
    }


    @Test
    public void updateTransactionStatusById_successful() {
        SigningBasketEntity signingBasket = buildSigningBasket(false);
        when(signingBasketRepository.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(signingBasket));

        CmsResponse<Boolean> cmsResponse = signingBasketService.updateTransactionStatusById(EXTERNAL_ID, SigningBasketTransactionStatus.ACTC);

        assertTrue(cmsResponse.isSuccessful());
        assertTrue(cmsResponse.getPayload());
        assertEquals(signingBasket.getTransactionStatus(), SigningBasketTransactionStatus.ACTC.toString());
    }

    @Test
    public void updateTransactionStatusById_failure() {
        when(signingBasketRepository.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.empty());

        CmsResponse<Boolean> cmsResponse = signingBasketService.updateTransactionStatusById(EXTERNAL_ID, SigningBasketTransactionStatus.ACTC);

        assertFalse(cmsResponse.isSuccessful());
        assertNull(cmsResponse.getPayload());
        assertTrue(cmsResponse.hasError());
        assertEquals(cmsResponse.getError(), CmsError.LOGICAL_ERROR);
    }

    @Test
    public void updateMultilevelScaRequired_success() {
        SigningBasketEntity signingBasket = buildSigningBasket(false);
        when(signingBasketRepository.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.of(signingBasket));

        CmsResponse<Boolean> cmsResponse = signingBasketService.updateMultilevelScaRequired(EXTERNAL_ID, true);

        assertTrue(cmsResponse.isSuccessful());
        assertTrue(cmsResponse.getPayload());
        assertTrue(signingBasket.isMultilevelScaRequired());
    }

    @Test
    public void updateMultilevelScaRequired_failure() {
        when(signingBasketRepository.findByExternalId(EXTERNAL_ID)).thenReturn(Optional.empty());

        CmsResponse<Boolean> cmsResponse = signingBasketService.updateMultilevelScaRequired(EXTERNAL_ID, true);

        assertTrue(cmsResponse.isSuccessful());
        assertFalse(cmsResponse.getPayload());
    }

    private AuthorisationEntity buildAuthorisationEntity(String externalId, String parentId) {
        AuthorisationEntity authorisationEntity = new AuthorisationEntity();
        authorisationEntity.setExternalId(externalId);
        authorisationEntity.setParentExternalId(parentId);
        authorisationEntity.setScaStatus(ScaStatus.RECEIVED);
        return authorisationEntity;
    }

    private List<ConsentEntity> buildConsents() {
        return List.of(buildConsentEntity(CONSENT_ID_AIS_1, ConsentType.AIS),
                       buildConsentEntity(CONSENT_ID_AIS_2, ConsentType.AIS),
                       buildConsentEntity(CONSENT_ID_PIIS, ConsentType.PIIS_TPP));
    }

    private List<PisCommonPaymentData> buildPayments() {
        return List.of(buildPisCommonPaymentData(PAYMENT_ID_1, TransactionStatus.RCVD),
                       buildPisCommonPaymentData(PAYMENT_ID_2, TransactionStatus.RCVD));
    }

    private SigningBasketEntity buildSigningBasket(boolean empty) {
        SigningBasketEntity basket = new SigningBasketEntity();
        basket.setId(1L);
        basket.setExternalId(EXTERNAL_ID);
        if (!empty) {
            basket.setConsents(buildConsents());
            basket.setPayments(buildPayments());
        }
        basket.setPsuDataList(List.of(PSU_DATA));
        AuthorisationTemplateEntity authorisationTemplateEntity = new AuthorisationTemplateEntity();
        authorisationTemplateEntity.setRedirectUri(REDIRECT_URI);
        authorisationTemplateEntity.setNokRedirectUri(NOK_REDIRECT_URI);
        basket.setAuthorisationTemplate(authorisationTemplateEntity);
        return basket;
    }

    private CmsSigningBasket buildEmptyCmsSigningBasket() {
        return jsonReader.getObjectFromFile("json/service/cms-signing-basket-empty.json", CmsSigningBasket.class);
    }

    private CmsSigningBasket buildCmsSigningBasket() {
        return jsonReader.getObjectFromFile("json/service/cms-signing-basket.json", CmsSigningBasket.class);
    }

    private PisCommonPaymentData buildPisCommonPaymentData(String id, TransactionStatus transactionStatus) {
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        pisCommonPaymentData.setPaymentId(id);
        pisCommonPaymentData.setTransactionStatus(transactionStatus);
        return pisCommonPaymentData;
    }

    private ConsentEntity buildConsentEntity(String externalId, ConsentType consentType) {
        ConsentEntity consentEntity = new ConsentEntity();
        consentEntity.setExternalId(externalId);
        consentEntity.setConsentType(consentType.toString());
        consentEntity.setConsentStatus(ConsentStatus.VALID);
        return consentEntity;
    }
}
