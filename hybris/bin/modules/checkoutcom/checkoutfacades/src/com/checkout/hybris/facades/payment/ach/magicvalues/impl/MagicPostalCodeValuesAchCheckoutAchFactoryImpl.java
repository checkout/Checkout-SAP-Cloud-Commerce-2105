package com.checkout.hybris.facades.payment.ach.magicvalues.impl;

import com.checkout.hybris.facades.payment.ach.magicvalues.MagicPostalCodeValueAchCheckoutStrategy;
import com.checkout.hybris.facades.payment.ach.magicvalues.MagicPostalCodeValuesAchCheckoutStrategyFactory;

import java.util.List;
import java.util.Optional;

public class MagicPostalCodeValuesAchCheckoutAchFactoryImpl implements MagicPostalCodeValuesAchCheckoutStrategyFactory {
	private final List<MagicPostalCodeValueAchCheckoutStrategy> strategies;

	public MagicPostalCodeValuesAchCheckoutAchFactoryImpl(final List<MagicPostalCodeValueAchCheckoutStrategy> strategies) {
		this.strategies = strategies;
	}

	@Override
	public Optional<MagicPostalCodeValueAchCheckoutStrategy> findStrategy(final String postalCode) {
		return strategies.stream().filter(
								 magicPostalCodeValueAchCheckoutStrategy -> magicPostalCodeValueAchCheckoutStrategy.isApplicable(
										 postalCode))
						 .findFirst();
	}
}
