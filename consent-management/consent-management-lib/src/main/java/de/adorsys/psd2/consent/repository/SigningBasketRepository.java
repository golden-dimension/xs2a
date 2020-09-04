package de.adorsys.psd2.consent.repository;

import de.adorsys.psd2.consent.domain.sb.SigningBasket;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface SigningBasketRepository extends CrudRepository<SigningBasket, Long> {
    Optional<SigningBasket> findByExternalId(String externalId);
}
