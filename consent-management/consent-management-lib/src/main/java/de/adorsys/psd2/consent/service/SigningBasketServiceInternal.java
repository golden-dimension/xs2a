package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasket;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketConsentsAndPaymentsResponse;
import de.adorsys.psd2.consent.api.sb.CmsSigningBasketCreationResponse;
import de.adorsys.psd2.consent.api.service.SigningBasketService;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.sb.SigningBasket;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.SigningBasketRepository;
import de.adorsys.psd2.consent.service.mapper.CmsConsentMapper;
import de.adorsys.psd2.consent.service.mapper.CmsSigningBasketMapper;
import de.adorsys.psd2.consent.service.mapper.PisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.sb.SigningBasketTransactionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.adorsys.psd2.consent.api.CmsError.LOGICAL_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SigningBasketServiceInternal implements SigningBasketService {
    private final CmsSigningBasketMapper cmsSigningBasketMapper;
    private final CmsConsentMapper cmsConsentMapper;
    private final PisCommonPaymentMapper pisCommonPaymentMapper;
    private final ConsentJpaRepository consentRepository;
    private final PisCommonPaymentDataRepository paymentRepository;
    private final SigningBasketRepository signingBasketRepository;
    private final AuthorisationRepository authorisationRepository;
    private final AisConsentUsageService aisConsentUsageService;

    @Override
    @Transactional
    public CmsResponse<CmsSigningBasketCreationResponse> createSigningBasket(CmsSigningBasket cmsSigningBasket) {
        Pair<List<ConsentEntity>, List<PisCommonPaymentData>> consentsAndPayments = retrieveConsentsAndPayments(cmsSigningBasket);

        List<ConsentEntity> consents = consentsAndPayments.getFirst();
        consents.forEach(consent -> consent.setSigningBasketBlocked(true));
        List<PisCommonPaymentData> payments = consentsAndPayments.getSecond();
        payments.forEach(payment -> payment.setSigningBasketBlocked(true));

        SigningBasket signingBasket = cmsSigningBasketMapper.mapToNewSigningBasket(cmsSigningBasket, consents, payments);
        SigningBasket savedEntity = signingBasketRepository.save(signingBasket);

        Map<String, List<AuthorisationEntity>> authorisations = retrieveAuthorisations(consents, payments);
        Map<String, Map<String, Integer>> usages = retrieveUsages(consents);

        return CmsResponse.<CmsSigningBasketCreationResponse>builder()
                   .payload(new CmsSigningBasketCreationResponse(savedEntity.getExternalId(), cmsSigningBasketMapper.mapToCmsSigningBasket(savedEntity, authorisations, usages)))
                   .build();
    }

    @Override
    public CmsResponse<CmsSigningBasketConsentsAndPaymentsResponse> getConsentsAndPayments(List<String> consents, List<String> payments) {
        Pair<List<ConsentEntity>, List<PisCommonPaymentData>> consentsAndPayments = retrieveConsentsAndPayments(consents, payments);

        Map<String, List<AuthorisationEntity>> authorisations = retrieveAuthorisations(consentsAndPayments.getFirst(), consentsAndPayments.getSecond());
        Map<String, Map<String, Integer>> usages = retrieveUsages(consentsAndPayments.getFirst());

        return CmsResponse.<CmsSigningBasketConsentsAndPaymentsResponse>builder()
                   .payload(
                       new CmsSigningBasketConsentsAndPaymentsResponse(
                           cmsConsentMapper.mapToCmsConsents(consentsAndPayments.getFirst(), authorisations, usages),
                           pisCommonPaymentMapper.mapToPisCommonPaymentResponses(consentsAndPayments.getSecond(), authorisations)))
                   .build();
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> updateTransactionStatusById(String basketId, SigningBasketTransactionStatus transactionStatus) {
        Optional<SigningBasket> signingBasketOptional = getActualSigningBasket(basketId);

        if (signingBasketOptional.isPresent()) {
            SigningBasket signingBasket = signingBasketOptional.get();
            signingBasket.setTransactionStatus(transactionStatus.toString());

            return CmsResponse.<Boolean>builder()
                       .payload(true)
                       .build();
        }

        log.info("Basket ID [{}]. Update transaction status by ID failed, because basket not found", basketId);
        return CmsResponse.<Boolean>builder()
                   .error(LOGICAL_ERROR)
                   .build();
    }

    @Override
    @Transactional
    public CmsResponse<Boolean> updateMultilevelScaRequired(String basketId, boolean multilevelScaRequired) {
        Optional<SigningBasket> signingBasketOptional = signingBasketRepository.findByExternalId(basketId);
        if (signingBasketOptional.isEmpty()) {
            log.info("Basket ID: [{}]. Get update multilevel SCA required status failed, because basket is not found",
                     basketId);
            return CmsResponse.<Boolean>builder()
                       .payload(false)
                       .build();
        }
        SigningBasket basket = signingBasketOptional.get();
        basket.setMultilevelScaRequired(multilevelScaRequired);

        return CmsResponse.<Boolean>builder()
                   .payload(true)
                   .build();
    }

    private Optional<SigningBasket> getActualSigningBasket(String basketId) {
        return signingBasketRepository.findByExternalId(basketId);
    }

    private Pair<List<ConsentEntity>, List<PisCommonPaymentData>> retrieveConsentsAndPayments(List<String> consents, List<String> payments) {
        return Pair.of(consentRepository.findAllByExternalIdIn(consents), paymentRepository.findAllByPaymentIdIn(payments));
    }

    private Pair<List<ConsentEntity>, List<PisCommonPaymentData>> retrieveConsentsAndPayments(CmsSigningBasket cmsSigningBasket) {
        List<String> consents = cmsSigningBasket.getConsents().stream()
                                    .map(CmsConsent::getId)
                                    .collect(Collectors.toList());
        List<String> payments = cmsSigningBasket.getPayments().stream()
                                    .map(PisCommonPaymentResponse::getExternalId)
                                    .collect(Collectors.toList());

        return retrieveConsentsAndPayments(consents, payments);
    }

    private Map<String, List<AuthorisationEntity>> retrieveAuthorisations(List<ConsentEntity> consents, List<PisCommonPaymentData> payments) {
        return Stream.concat(consents.stream(), payments.stream()).reduce(new HashMap<>(), (map, authorisable) -> {
            map.put(authorisable.getExternalId(), authorisationRepository.findAllByParentExternalIdAndTypeIn(authorisable.getExternalId(), Set.of(AuthorisationType.CONSENT, AuthorisationType.PIS_CREATION, AuthorisationType.PIS_CANCELLATION)));
            return map;
        }, (a, b) -> a);
    }

    private Map<String, Map<String, Integer>> retrieveUsages(List<ConsentEntity> consents) {
        return consents.stream().reduce(new HashMap<>(), (map, entity) -> {
            map.put(entity.getExternalId(), aisConsentUsageService.getUsageCounterMap(entity));
            return map;
        }, (a, b) -> a);
    }
}
