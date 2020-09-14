package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.domain.sb.SigningBasketTppInformationEntity;
import de.adorsys.psd2.xs2a.core.sb.SigningBasketTppInformation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;


@Mapper(componentModel = "spring", uses = {TppInfoMapper.class})
public interface SigningBasketTppInformationMapper {

    @Mapping(target = "tppNotificationSupportedModes", source = "tppNotificationContentPreferred")
    SigningBasketTppInformation mapToSigningBasketTppInformation(SigningBasketTppInformationEntity signingBasketTppInformationEntity);

    @Mapping(target = "tppNotificationContentPreferred", source = "tppNotificationSupportedModes")
    SigningBasketTppInformationEntity mapToSigningBasketTppInformationEntity(SigningBasketTppInformation signingBasketTppInformation);
}
