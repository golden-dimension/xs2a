package de.adorsys.psd2.consent.repository;

import de.adorsys.psd2.consent.domain.signingbaskets.SigningBasket;
import org.springframework.data.repository.CrudRepository;

public interface SigningBasketRepository extends CrudRepository<SigningBasket, Long> {
}
