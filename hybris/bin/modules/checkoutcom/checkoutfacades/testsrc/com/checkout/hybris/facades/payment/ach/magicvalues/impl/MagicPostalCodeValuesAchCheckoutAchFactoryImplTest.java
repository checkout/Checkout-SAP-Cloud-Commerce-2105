package com.checkout.hybris.facades.payment.ach.magicvalues.impl;

import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;
import com.checkout.hybris.facades.payment.ach.magicvalues.MagicPostalCodeValueAchCheckoutStrategy;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
public class MagicPostalCodeValuesAchCheckoutAchFactoryImplTest {

	private MagicPostalCodeValuesAchCheckoutAchFactoryImpl testObj;

	private MyPostalCodeStrategyPC1 myPostalCodeStrategyPC1;

	private MyPostalCodeStrategyPC2 myPostalCodeStrategyPC2;

	@Before
	public void setUp() throws Exception {
		myPostalCodeStrategyPC1 = new MyPostalCodeStrategyPC1();
		myPostalCodeStrategyPC2 = new MyPostalCodeStrategyPC2();
		testObj = new MagicPostalCodeValuesAchCheckoutAchFactoryImpl(
				List.of(myPostalCodeStrategyPC1, myPostalCodeStrategyPC2));
	}

	@Test
	public void findStrategy_shouldReturnStrategyIfPostalCode_whenPostalCodeIsWithinOneOfTheStrategies() {
		final Optional<MagicPostalCodeValueAchCheckoutStrategy> result = testObj.findStrategy("PC1");

		assertThat(result).containsSame(myPostalCodeStrategyPC1);
	}

	@Test
	public void findStrategy_shouldReturnEmpty_whenPostalCodeIsNotWithinOneOfTheStrategies() {
		final Optional<MagicPostalCodeValueAchCheckoutStrategy> result = testObj.findStrategy("NON_EXISTENT");

		assertThat(result).isEmpty();
	}

	private static class MyPostalCodeStrategyPC1 implements MagicPostalCodeValueAchCheckoutStrategy {

		@Override
		public AchBankInfoDetailsData createAchBankInfoDetailsData() {
			return null;
		}

		@Override
		public boolean isApplicable(final String postalCode) {
			return postalCode.equals("PC1");
		}
	}

	private static class MyPostalCodeStrategyPC2 implements MagicPostalCodeValueAchCheckoutStrategy {

		@Override
		public AchBankInfoDetailsData createAchBankInfoDetailsData() {
			return null;
		}

		@Override
		public boolean isApplicable(final String postalCode) {
			return postalCode.equals("PC2");
		}
	}
}
