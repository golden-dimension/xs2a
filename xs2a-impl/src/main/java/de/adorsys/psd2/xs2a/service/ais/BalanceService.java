/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.service.ais;

import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aBalancesReport;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aBalanceReportMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetBalancesReportValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.GetAccountBalanceRequestObject;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountBalance;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AccountSpi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_400;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_401;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_INVALID;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;

@Slf4j
@Service
@AllArgsConstructor
public class BalanceService {
    private final AccountSpi accountSpi;

    private final SpiToXs2aBalanceReportMapper balanceReportMapper;

    private final Xs2aAisConsentService aisConsentService;
    private final Xs2aAisConsentMapper consentMapper;
    private final TppService tppService;
    private final Xs2aEventService xs2aEventService;
    private final SpiErrorMapper spiErrorMapper;

    private final GetBalancesReportValidator getBalancesReportValidator;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final AccountHelperService accountHelperService;

    private final LoggingContextService loggingContextService;

    /**
     * Gets Balances Report based on consentId and accountId
     *
     * @param consentId  String representing an AccountConsent identification
     * @param accountId  String representing a PSU`s Account at ASPSP
     * @param requestUri the URI of incoming request
     * @return Balances Report based on consentId and accountId
     */
    public ResponseObject<Xs2aBalancesReport> getBalancesReport(String consentId, String accountId, String requestUri) {
        xs2aEventService.recordConsentTppRequest(consentId, EventType.READ_BALANCE_REQUEST_RECEIVED);

        Optional<AisConsent> aisConsentOptional = aisConsentService.getAccountConsentById(consentId);

        if (aisConsentOptional.isEmpty()) {
            log.info("Account-ID [{}], Consent-ID [{}]. Get balances report failed. Account consent not found by ID",
                     accountId, consentId);
            return ResponseObject.<Xs2aBalancesReport>builder()
                       .fail(AIS_400, TppMessageInformation.of(CONSENT_UNKNOWN_400))
                       .build();
        }

        AisConsent aisConsent = aisConsentOptional.get();

        ValidationResult validationResult = getValidationResultForCommonAccountBalanceRequest(accountId, requestUri, aisConsent);

        if (validationResult.isNotValid()) {
            log.info("Account-ID [{}], Consent-ID [{}], RequestUri [{}]. Get balances report - validation failed: {}",
                     accountId, consentId, requestUri, validationResult.getMessageError());
            return ResponseObject.<Xs2aBalancesReport>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        SpiResponse<List<SpiAccountBalance>> spiResponse = getSpiResponse(aisConsent, consentId, accountId);

        if (spiResponse.hasError()) {
            return checkSpiResponse(consentId, accountId, spiResponse);
        }

        loggingContextService.storeConsentStatus(aisConsent.getConsentStatus());

        return getXs2aBalancesReportResponseObject(aisConsent, accountId, consentId, requestUri, spiResponse.getPayload());
    }

    private ValidationResult getValidationResultForCommonAccountBalanceRequest(String accountId, String requestUri, AisConsent accountConsent) {
        return getBalancesReportValidator.validate(
            new GetAccountBalanceRequestObject(accountConsent, accountId, requestUri));
    }

    private SpiResponse<List<SpiAccountBalance>> getSpiResponse(AisConsent aisConsent, String consentId, String accountId) {
        AccountAccess access = aisConsent.getAspspAccountAccesses();
        SpiAccountReference requestedAccountReference = accountHelperService.findAccountReference(access.getBalances(), accountId);

        return accountSpi.requestBalancesForAccount(accountHelperService.getSpiContextData(),
                                                    requestedAccountReference,
                                                    consentMapper.mapToSpiAccountConsent(aisConsent),
                                                    aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));
    }

    private ResponseObject<Xs2aBalancesReport> checkSpiResponse(String consentId, String accountId, SpiResponse<List<SpiAccountBalance>> spiResponse) {

        log.info("Account-ID [{}], Consent-ID: [{}]. Get balances report failed: error on SPI level",
                 accountId, consentId);
        return ResponseObject.<Xs2aBalancesReport>builder()
                   .fail(new MessageError(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS)))
                   .build();
    }

    @NotNull
    private ResponseObject<Xs2aBalancesReport> getXs2aBalancesReportResponseObject(AisConsent accountConsent,
                                                                                   String accountId,
                                                                                   String consentId,
                                                                                   String requestUri,
                                                                                   List<SpiAccountBalance> payload) {
        AccountAccess access = accountConsent.getAspspAccountAccesses();
        List<AccountReference> balances = access.getBalances();
        if (hasNoAccessToSource(balances)) {
            return ResponseObject.<Xs2aBalancesReport>builder()
                       .fail(AIS_401, TppMessageInformation.of(CONSENT_INVALID))
                       .build();
        }

        SpiAccountReference requestedAccountReference = accountHelperService.findAccountReference(balances, accountId);

        Xs2aBalancesReport balancesReport = balanceReportMapper.mapToXs2aBalancesReportSpi(requestedAccountReference, payload);

        ResponseObject<Xs2aBalancesReport> response = ResponseObject.<Xs2aBalancesReport>builder()
                                                          .body(balancesReport)
                                                          .build();

        aisConsentService.consentActionLog(tppService.getTppId(), consentId,
                                           accountHelperService.createActionStatus(false, TypeAccess.BALANCE, response),
                                           requestUri, accountHelperService.needsToUpdateUsage(accountConsent), accountId, null);

        return response;
    }

    private boolean hasNoAccessToSource(List<AccountReference> references) {
        return references.stream()
                   .allMatch(AccountReference::isNotIbanAccount);
    }
}
