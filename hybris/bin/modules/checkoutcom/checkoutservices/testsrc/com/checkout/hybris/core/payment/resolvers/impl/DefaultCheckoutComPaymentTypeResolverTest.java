package com.checkout.hybris.core.payment.resolvers.impl;

import com.checkout.hybris.core.enums.MadaBin;
import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.payment.CreditCardPaymentInfoModel;
import de.hybris.platform.enumeration.EnumerationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComPaymentTypeResolverTest {

    private static final String APM_PAYMENT_TYPE = "SEPA";
    private static final String INVALID_PAYMENT_TYPE = "CICCIO";
    private static final String CARD_BIN = "123456";

    @InjectMocks
    private DefaultCheckoutComPaymentTypeResolver testObj;

    @Mock
    private CheckoutComCreditCardPaymentInfoModel paymentInfoMock;
    @Mock
    private CheckoutComAPMPaymentInfoModel apmPaymentInfoMock;
    @Mock
    private CreditCardPaymentInfoModel otherPaymentInfoMock;
    @Mock
    private EnumerationService enumerationServiceMock;
    @Mock
    private MadaBin madaBin1Mock, madaBin2Mock;

    @Before
    public void setUp() {
        when(enumerationServiceMock.getEnumerationValues(MadaBin.class)).thenReturn(emptyList());
        when(otherPaymentInfoMock.getCode()).thenReturn("paymentInfoCode");
        when(otherPaymentInfoMock.getItemtype()).thenReturn("PaymentInfo");
        when(madaBin1Mock.getCode()).thenReturn(CARD_BIN);
        when(madaBin2Mock.getCode()).thenReturn("7891011");
    }

    @Test
    public void resolvePaymentType_WhenCreditCardPaymentInfoAndNormalCard_ThenReturnCardType() {
        final CheckoutComPaymentType result = testObj.resolvePaymentType(paymentInfoMock);

        assertThat(result).isEqualTo(CARD);
    }

    @Test
    public void resolvePaymentType_WhenCreditCardPaymentInfoAndMada_ThenReturnMadaType() {
        when(enumerationServiceMock.getEnumerationValues(MadaBin.class)).thenReturn(asList(madaBin1Mock, madaBin2Mock));
        when(paymentInfoMock.getCardBin()).thenReturn(CARD_BIN);

        final CheckoutComPaymentType result = testObj.resolvePaymentType(paymentInfoMock);

        assertThat(result).isEqualTo(MADA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolvePaymentType_WhenPaymentInfoNotRecognised_ShouldThrowException() {
        testObj.resolvePaymentType(otherPaymentInfoMock);
    }

    @Test
    public void resolvePaymentType_WhenRedirectPaymentInfo_ThenReturnPaymentInfoType() {
        when(apmPaymentInfoMock.getType()).thenReturn(APM_PAYMENT_TYPE);

        final CheckoutComPaymentType result = testObj.resolvePaymentType(apmPaymentInfoMock);

        assertThat(result).isEqualTo(SEPA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolvePaymentType_WhenRedirectPaymentInfoAndUnknownType_ShouldThrowException() {
        when(apmPaymentInfoMock.getType()).thenReturn(INVALID_PAYMENT_TYPE);

        testObj.resolvePaymentType(apmPaymentInfoMock);
    }

    @Test
    public void resolvePaymentMethod_WhenValidType_ShouldReturnPaymentType() {
        final CheckoutComPaymentType result = testObj.resolvePaymentMethod(APM_PAYMENT_TYPE);

        assertThat(result).isEqualTo(SEPA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolvePaymentMethod_WhenPaymentMethodIsBlank_ShouldThrowException() {
        testObj.resolvePaymentMethod("  ");
    }

    @Test(expected = IllegalArgumentException.class)
    public void resolvePaymentMethod_WhenPaymentMethodIsInvalid_ShouldThrowException() {
        testObj.resolvePaymentMethod("TEST");
    }

    @Test
    public void isMadaCard_WhenMadaCard_ShouldReturnTrue() {
        when(enumerationServiceMock.getEnumerationValues(MadaBin.class)).thenReturn(asList(madaBin1Mock, madaBin2Mock));

        final boolean result = testObj.isMadaCard(CARD_BIN);

        assertTrue(result);
    }

    @Test
    public void isMadaCard_WhenNotMadaCard_ShouldReturnFalse() {
        final boolean result = testObj.isMadaCard("222222");

        assertFalse(result);
    }
}
