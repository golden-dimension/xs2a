package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.service.SigningBasketService;
import de.adorsys.psd2.consent.api.signingbasket.*;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.signingbaskets.SigningBasket;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.SigningBasketRepository;
import de.adorsys.psd2.consent.service.mapper.CmsSigningBasketMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SigningBasketServiceInternal implements SigningBasketService {
    private final CmsSigningBasketMapper cmsSigningBasketMapper;
    private final ConsentJpaRepository consentRepository;
    private final PisCommonPaymentDataRepository paymentRepository;
    private final SigningBasketRepository signingBasketRepository;

    @Override
    public CmsResponse<CmsSigningBasketCreationResponse> createSigningBasket(CmsSigningBasket cmsSigningBasket) {
        Pair<List<ConsentEntity>, List<PisCommonPaymentData>> consentsAndPayments = retrieveConsentsAndPayments(cmsSigningBasket);

        SigningBasket signingBasket = cmsSigningBasketMapper.mapToNewSigningBasket(cmsSigningBasket, consentsAndPayments.getFirst(), consentsAndPayments.getSecond());
        SigningBasket savedEntity = signingBasketRepository.save(signingBasket);

        return CmsResponse.<CmsSigningBasketCreationResponse>builder()
                   .payload(new CmsSigningBasketCreationResponse(savedEntity.getExternalId(), cmsSigningBasketMapper.mapToCmsSigningBasket(savedEntity)))
                   .build();
    }

    @Override
    public CmsResponse<CmsSigningBasketConsentsAndPaymentsResponse> getConsentsAndPayments(List<String> consents, List<String> payments) {
        Pair<List<ConsentEntity>, List<PisCommonPaymentData>> consentsAndPayments = retrieveConsentsAndPayments(consents, payments);

        return CmsResponse.<CmsSigningBasketConsentsAndPaymentsResponse>builder()
                   .payload(new CmsSigningBasketConsentsAndPaymentsResponse(cmsSigningBasketMapper.mapToCmsSigningBasketConsents(consentsAndPayments.getFirst()),
                                                                            cmsSigningBasketMapper.mapToCmsSigningBasketPayments(consentsAndPayments.getSecond())))
                   .build();
    }

    private Pair<List<ConsentEntity>, List<PisCommonPaymentData>> retrieveConsentsAndPayments(List<String> consents, List<String> payments) {
        return Pair.of(consentRepository.findAllByExternalIdIn(consents), paymentRepository.findAllByPaymentIdIn(payments));
    }

    private Pair<List<ConsentEntity>, List<PisCommonPaymentData>> retrieveConsentsAndPayments(CmsSigningBasket cmsSigningBasket) {
        List<String> consents = cmsSigningBasket.getConsents().stream()
                                    .map(CmsSigningBasketConsent::getId)
                                    .collect(Collectors.toList());
        List<String> payments = cmsSigningBasket.getPayments().stream()
                                    .map(CmsSigningBasketPayment::getId)
                                    .collect(Collectors.toList());

        return retrieveConsentsAndPayments(consents, payments);
    }

}
