package com.checkout.hybris.core.currency.services.impl;

import com.checkout.payments.PaymentProcessed;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComCurrencyServiceTest {

    private static final double GBP_AMOUNT = 123.23d;
    private static final double BHD_AMOUNT = 12.323d;
    private static final long CHECKOUTCOM_AMOUNT = 12323;
    private static final String GBP = "GBP";
    private static final String BHD = "BHD";

    @InjectMocks
    private DefaultCheckoutComCurrencyService testObj;

    @Mock
    private CurrencyModel currencyModelMock;
    @Mock
    private CommonI18NService commonI18NServiceMock;
    @Mock
    private PaymentProcessed paymentProcessedMock;

    @Before
    public void setUp() {
        when(currencyModelMock.getIsocode()).thenReturn(GBP);
        when(paymentProcessedMock.getAmount()).thenReturn(CHECKOUTCOM_AMOUNT);
        when(paymentProcessedMock.getCurrency()).thenReturn(GBP);
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertAmountIntoPennies_WhenCurrencyIsNull_ShouldThrowException() {
        testObj.convertAmountIntoPennies(null, GBP_AMOUNT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertAmountIntoPennies_WhenAmountIsNull_ShouldThrowException() {
        testObj.convertAmountIntoPennies(GBP, null);
    }

    @Test
    public void convertAmountIntoPennies_ShouldConvertTheValueProperly_WhenCurrencyHasTwoDecimals() {
        when(commonI18NServiceMock.convertAndRoundCurrency(1, Math.pow(10, 2), 0, GBP_AMOUNT)).thenReturn(12323d);

        final Long amount = testObj.convertAmountIntoPennies(GBP, GBP_AMOUNT);

        assertThat(amount).isEqualTo(CHECKOUTCOM_AMOUNT);
    }

    @Test
    public void convertAmountIntoPennies_ShouldConvertTheValueProperly_WhenCurrencyHas3Decimals() {
        when(commonI18NServiceMock.convertAndRoundCurrency(1, Math.pow(10, 3), 0, BHD_AMOUNT)).thenReturn(12323d);

        final Long amount = testObj.convertAmountIntoPennies(BHD, BHD_AMOUNT);

        assertThat(amount).isEqualTo(CHECKOUTCOM_AMOUNT);
    }

    @Test(expected = IllegalArgumentException.class)
    public void convertAmountFromPennies_WhenTheCurrencyCodeNull_ShouldThrowException() {
        testObj.convertAmountFromPennies(null, CHECKOUTCOM_AMOUNT);
    }


    @Test(expected = IllegalArgumentException.class)
    public void convertAmountFromPennies_WhenTheAmountNull_ShouldThrowException() {
        testObj.convertAmountFromPennies(GBP, null);
    }

    @Test
    public void convertAmountFromPennies_WhenThePaymentResponseValud_ShouldConvertTheValueProperly() {
        final BigDecimal result = testObj.convertAmountFromPennies(GBP, CHECKOUTCOM_AMOUNT);

        assertEquals(result, BigDecimal.valueOf(GBP_AMOUNT));
    }

    @Test
    public void convertAmountFromPennies_WhenThePaymentResponseValueAndCurrancyHasThreeDecimals_ShouldConvertTheValueProperly() {
        final BigDecimal result = testObj.convertAmountFromPennies(BHD, CHECKOUTCOM_AMOUNT);

        assertEquals(result, BigDecimal.valueOf(BHD_AMOUNT));
    }
}
