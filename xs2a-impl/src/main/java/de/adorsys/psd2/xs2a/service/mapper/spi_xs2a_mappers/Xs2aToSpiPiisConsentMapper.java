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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.AdditionalPsuIdData;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.piis.SpiPiisConsent;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring")
public abstract class Xs2aToSpiPiisConsentMapper {

    @Mapping(target = "account", expression = "java(toSpiAccountReference(piisConsent.getAccountReference()))")
    @Mapping(target = "cardExpiryDate", source = "consentData.cardExpiryDate")
    @Mapping(target = "cardInformation", source = "consentData.cardInformation")
    @Mapping(target = "cardNumber", source = "consentData.cardNumber")
    @Mapping(target = "psuData", expression = "java(toSpiPsuDataList(piisConsent.getPsuIdDataList()))")
    @Mapping(target = "registrationInformation", source = "consentData.registrationInformation")
    @Mapping(target = "requestDateTime", source = "creationTimestamp")
    @Mapping(target = "tppAuthorisationNumber", source = "consentTppInformation.tppInfo.authorisationNumber")
    public abstract SpiPiisConsent mapToSpiPiisConsent(PiisConsent piisConsent);

    public SpiScaConfirmation toSpiScaConfirmation(UpdateAuthorisationRequest request, PsuIdData psuData) {
        SpiScaConfirmation accountConfirmation = new SpiScaConfirmation();
        accountConfirmation.setConsentId(request.getBusinessObjectId());
        accountConfirmation.setPsuId(Optional.ofNullable(psuData).map(PsuIdData::getPsuId).orElse(null));
        accountConfirmation.setTanNumber(request.getScaAuthenticationData());
        return accountConfirmation;
    }

    SpiAccountReference toSpiAccountReference(AccountReference account) {
        return new SpiAccountReference(
            account.getAspspAccountId(),
            account.getResourceId(),
            account.getIban(),
            account.getBban(),
            account.getPan(),
            account.getMaskedPan(),
            account.getMsisdn(),
            account.getCurrency());
    }

    abstract List<SpiPsuData> toSpiPsuDataList(List<PsuIdData> psuIdData);

    SpiPsuData toSpiPsuData(PsuIdData psuIdData) {
        return Optional.ofNullable(psuIdData)
                   .map(this::builderWithPsuData)
                   .orElseGet(SpiPsuData::builder)
                   .build();
    }

    private SpiPsuData.SpiPsuDataBuilder builderWithPsuData(PsuIdData psuIdData) {
        SpiPsuData.SpiPsuDataBuilder builder = SpiPsuData.builder()
                                                   .psuId(psuIdData.getPsuId())
                                                   .psuIdType(psuIdData.getPsuIdType())
                                                   .psuCorporateId(psuIdData.getPsuCorporateId())
                                                   .psuCorporateIdType(psuIdData.getPsuCorporateIdType())
                                                   .psuIpAddress(psuIdData.getPsuIpAddress());

        return Optional.ofNullable(psuIdData.getAdditionalPsuIdData())
                   .map(dta -> addAdditionalPsuIdData(builder, dta))
                   .orElse(builder);
    }

    private SpiPsuData.SpiPsuDataBuilder addAdditionalPsuIdData(SpiPsuData.SpiPsuDataBuilder builder, AdditionalPsuIdData data) {
        return builder.psuIpPort(data.getPsuIpPort())
                   .psuUserAgent(data.getPsuUserAgent())
                   .psuGeoLocation(data.getPsuGeoLocation())
                   .psuAccept(data.getPsuAccept())
                   .psuAcceptCharset(data.getPsuAcceptCharset())
                   .psuAcceptEncoding(data.getPsuAcceptEncoding())
                   .psuAcceptLanguage(data.getPsuAcceptLanguage())
                   .psuHttpMethod(data.getPsuHttpMethod())
                   .psuDeviceId(data.getPsuDeviceId());
    }
}
