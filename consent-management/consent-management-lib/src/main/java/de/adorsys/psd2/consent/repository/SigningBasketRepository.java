package de.adorsys.psd2.consent.repository;

import de.adorsys.psd2.consent.domain.sb.SigningBasketEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SigningBasketRepository extends CrudRepository<SigningBasketEntity, Long> {
    Optional<SigningBasketEntity> findByExternalId(String externalId);
}
