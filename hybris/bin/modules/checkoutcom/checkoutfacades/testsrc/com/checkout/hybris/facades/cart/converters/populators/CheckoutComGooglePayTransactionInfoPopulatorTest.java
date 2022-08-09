package com.checkout.hybris.facades.cart.converters.populators;

import com.checkout.hybris.facades.beans.GooglePayTransactionInfoData;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.product.data.PriceData;
import org.junit.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
public class CheckoutComGooglePayTransactionInfoPopulatorTest {

    private static final String CURRENCY_ISO_CODE = "EUR";
    private static final String TOTAL_PRICE_STATUS = "ESTIMATED";
    private static final BigDecimal TOTAL_PRICE = BigDecimal.valueOf(123.23);

    private final CheckoutComGooglePayTransactionInfoPopulator testObj = new CheckoutComGooglePayTransactionInfoPopulator();

    private final CartData source = new CartData();
    private final GooglePayTransactionInfoData target = new GooglePayTransactionInfoData();

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenSourceIsNull_ShouldThrowException() {
        testObj.populate(null, target);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WhenTargetIsNull_ShouldThrowException() {
        testObj.populate(source, null);
    }

    @Test
    public void populate_WhenSourceAndTargetAreNotNull_ShouldPopulateTheTarget() {
        final PriceData priceData = new PriceData();
        priceData.setCurrencyIso(CURRENCY_ISO_CODE);
        priceData.setValue(TOTAL_PRICE);
        source.setTotalPrice(priceData);

        testObj.populate(source, target);

        assertThat(target.getCurrencyCode()).isEqualTo(CURRENCY_ISO_CODE);
        assertThat(target.getTotalPrice()).isEqualTo(TOTAL_PRICE.toString());
        assertThat(target.getTotalPriceStatus()).isEqualTo(TOTAL_PRICE_STATUS);
    }
}
