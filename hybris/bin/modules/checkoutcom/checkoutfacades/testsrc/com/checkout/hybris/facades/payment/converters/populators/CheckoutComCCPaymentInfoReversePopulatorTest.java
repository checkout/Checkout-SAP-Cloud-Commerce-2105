package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.payment.resolvers.CheckoutComPaymentTypeResolver;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComCCPaymentInfoReversePopulatorTest {

    private static final String TOKEN = "token";
    private static final String CARD_BIN = "123456";

    @InjectMocks
    private CheckoutComCCPaymentInfoReversePopulator testObj;

    @Mock
    private CheckoutComPaymentTypeResolver checkoutComPaymentTypeResolverMock;
    @Mock
    private CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationServiceMock;

    private CheckoutComCreditCardPaymentInfoModel target = new CheckoutComCreditCardPaymentInfoModel();

    @Before
    public void setUp() {
        when(checkoutComMerchantConfigurationServiceMock.isAutoCapture()).thenReturn(false);
        when(checkoutComPaymentTypeResolverMock.isMadaCard(CARD_BIN)).thenReturn(false);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WithNullSource_ShouldThrowException() {
        testObj.populate(null, target);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_WithNullTarget_ShouldThrowException() {
        final CCPaymentInfoData source = new CCPaymentInfoData();
        source.setPaymentToken(TOKEN);

        testObj.populate(source, null);
    }

    @Test
    public void populate_WhenCardMarkedToSave_ShouldPopulateEverythingCorrectly() {
        final CCPaymentInfoData source = createSource(true);

        testObj.populate(source, target);

        assertEquals(TOKEN, target.getCardToken());
        assertTrue(target.getMarkToSave());
        assertFalse(target.isSaved());
        assertEquals(CARD_BIN, target.getCardBin());
        assertFalse(target.getAutoCapture());
    }

    @Test
    public void populate_WhenMadaCardMarkedToSave_ShouldPopulateEverythingCorrectly() {
        when(checkoutComPaymentTypeResolverMock.isMadaCard(CARD_BIN)).thenReturn(true);
        final CCPaymentInfoData source = createSource(true);

        testObj.populate(source, target);

        assertEquals(TOKEN, target.getCardToken());
        assertTrue(target.getMarkToSave());
        assertEquals(CARD_BIN, target.getCardBin());
        assertTrue(target.getAutoCapture());
    }

    @Test
    public void populate_WhenCardNotMarkedToSave_ShouldPopulateEverythingCorrectly() {
        final CCPaymentInfoData source = createSource(false);

        testObj.populate(source, target);

        assertEquals(TOKEN, target.getCardToken());
        assertFalse(target.getMarkToSave());
        assertEquals(CARD_BIN, target.getCardBin());
    }

    private CCPaymentInfoData createSource(final boolean isSaved) {
        final CCPaymentInfoData source = new CCPaymentInfoData();
        source.setPaymentToken(TOKEN);
        source.setSaved(isSaved);
        source.setCardBin(CARD_BIN);
        return source;
    }
}
