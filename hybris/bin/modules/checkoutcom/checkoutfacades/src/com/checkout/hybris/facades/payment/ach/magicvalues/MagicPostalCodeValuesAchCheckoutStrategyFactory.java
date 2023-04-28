package com.checkout.hybris.facades.payment.ach.magicvalues;

import java.util.Optional;

public interface MagicPostalCodeValuesAchCheckoutStrategyFactory {
	Optional<MagicPostalCodeValueAchCheckoutStrategy> findStrategy(String postalCode);
}
