package com.checkout.hybris.facades.payment.impl;

import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComAchPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComBenefitPayPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.payment.details.mappers.CheckoutComUpdatePaymentInfoStrategyMapper;
import com.checkout.hybris.core.payment.details.strategies.CheckoutComUpdatePaymentInfoStrategy;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.resolvers.CheckoutComPaymentTypeResolver;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentInfoService;
import com.checkout.hybris.facades.beans.*;
import com.checkout.hybris.facades.payment.converters.CheckoutComApmMappedPaymentInfoReverseConverter;
import com.checkout.payments.GetPaymentResponse;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.util.ReflectionTestUtils;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.*;
import static de.hybris.platform.constants.GeneratedCoreConstants.Enumerations.CreditCardType.VISA;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComPaymentInfoFacadeTest {

    @Spy
    @InjectMocks
    private DefaultCheckoutComPaymentInfoFacade testObj;

    @Mock
    private CheckoutComPaymentInfoService paymentInfoServiceMock;
    @Mock
    private Converter<CCPaymentInfoData, CheckoutComCreditCardPaymentInfoModel> checkoutComCCPaymentInfoReverseConverterMock;
    @Mock
    private Converter<AchPaymentInfoData, CheckoutComAchPaymentInfoModel> checkoutComAchPaymentInfoReverseConverterMock;
    @Mock
    private CartModel cartModelMock;
    @Mock
    private CCPaymentInfoData ccPaymentInfoDataMock;
    @Mock
    private CheckoutComCreditCardPaymentInfoModel checkoutComCreditCardPaymentInfoModelMock;
    @Mock
    private CheckoutComAPMPaymentInfoModel apmPaymentInfoMock;
    @Mock
    private CheckoutComAchPaymentInfoModel checkoutComAchPaymentInfoModelMock;
    @Mock
    private CartData cartDataMock;
    @Mock
    private APMPaymentInfoData apmPaymentInfoDataMock;
    @Mock
    private AchPaymentInfoData achPaymentInfoDataMock;
    @Mock
    private CheckoutComPaymentTypeResolver checkoutComPaymentTypeResolverMock;
    @Mock
    private CartService cartServiceMock;
    @Mock
    private CheckoutComApmMappedPaymentInfoReverseConverter checkoutComApmMappedPaymentInfoReverseConverterMock;
    @Mock
    private CheckoutComBenefitPayPaymentInfoModel benefitPayPaymentInfoMock;
    @Mock
    private GetPaymentResponse getPaymentResponseMock;
    @Mock
    private CheckoutComUpdatePaymentInfoStrategyMapper checkoutComUpdatePaymentInfoStrategyMapperMock;
    @Mock
    private CheckoutComUpdatePaymentInfoStrategy benefitPayPaymentInfoStrategyMock;

    @Before
    public void setUp() {
        ReflectionTestUtils.setField(testObj, "checkoutComCCPaymentInfoReverseConverter", checkoutComCCPaymentInfoReverseConverterMock);
        when(cartServiceMock.getSessionCart()).thenReturn(cartModelMock);
        when(cartServiceMock.hasSessionCart()).thenReturn(true);
        when(cartDataMock.getPaymentInfo()).thenReturn(ccPaymentInfoDataMock);
        when(cartDataMock.getCheckoutComPaymentInfo()).thenReturn(null);
        when(ccPaymentInfoDataMock.getPaymentToken()).thenReturn("token");
        when(apmPaymentInfoDataMock.getType()).thenReturn(CheckoutComPaymentType.BENEFITPAY.name());
        when(checkoutComPaymentTypeResolverMock.resolvePaymentMethod(CheckoutComPaymentType.BENEFITPAY.name())).thenReturn(CheckoutComPaymentType.BENEFITPAY);
        when(checkoutComApmMappedPaymentInfoReverseConverterMock.convertAPMPaymentInfoData(apmPaymentInfoDataMock, CheckoutComPaymentType.BENEFITPAY)).thenReturn(benefitPayPaymentInfoMock);
        when(checkoutComCCPaymentInfoReverseConverterMock.convert(ccPaymentInfoDataMock)).thenReturn(checkoutComCreditCardPaymentInfoModelMock);
        when(checkoutComAchPaymentInfoReverseConverterMock.convert(achPaymentInfoDataMock)).thenReturn(checkoutComAchPaymentInfoModelMock);
    }

    @Test
    public void addPaymentInfoToCart_WhenNoSessionCart_ShouldDoNothing() {
        when(cartServiceMock.hasSessionCart()).thenReturn(false);

        testObj.addPaymentInfoToCart(ccPaymentInfoDataMock);

        verify(cartServiceMock, never()).getSessionCart();
        verify(paymentInfoServiceMock, never()).createPaymentInfo(checkoutComCreditCardPaymentInfoModelMock, cartModelMock);
        verifyZeroInteractions(checkoutComCCPaymentInfoReverseConverterMock);
        verifyZeroInteractions(checkoutComCCPaymentInfoReverseConverterMock);
    }

    @Test
    public void addPaymentInfoToCart_WhenCCPaymentInfoDataAndCardTypeNull_ShouldDoNothing() {
        doNothing().when(paymentInfoServiceMock).createPaymentInfo(checkoutComCreditCardPaymentInfoModelMock, cartModelMock);

        testObj.addPaymentInfoToCart(ccPaymentInfoDataMock);

        verify(cartServiceMock).getSessionCart();
        verify(checkoutComCCPaymentInfoReverseConverterMock, never()).convert(ccPaymentInfoDataMock);
        verify(paymentInfoServiceMock, never()).createPaymentInfo(checkoutComCreditCardPaymentInfoModelMock, cartModelMock);
    }

    @Test
    public void addPaymentInfoToCart_WhenCCPaymentInfoDataAndCardTypeNotNull_ShouldSetThePaymentToSessionCart() {
        doNothing().when(paymentInfoServiceMock).createPaymentInfo(checkoutComCreditCardPaymentInfoModelMock, cartModelMock);
        when(ccPaymentInfoDataMock.getCardType()).thenReturn(VISA);

        testObj.addPaymentInfoToCart(ccPaymentInfoDataMock);

        final InOrder inOrder = inOrder(cartServiceMock, checkoutComCCPaymentInfoReverseConverterMock, paymentInfoServiceMock, checkoutComCreditCardPaymentInfoModelMock);
        inOrder.verify(cartServiceMock).getSessionCart();
        inOrder.verify(checkoutComCCPaymentInfoReverseConverterMock).convert(ccPaymentInfoDataMock);
        inOrder.verify(paymentInfoServiceMock).createPaymentInfo(checkoutComCreditCardPaymentInfoModelMock, cartModelMock);
    }

    @Test
    public void addPaymentInfoToCart_WhenAPMPaymentInfoData_ShouldSetThePaymentToSessionCart() {
        doNothing().when(paymentInfoServiceMock).createPaymentInfo(benefitPayPaymentInfoMock, cartModelMock);

        testObj.addPaymentInfoToCart(apmPaymentInfoDataMock);

        final InOrder inOrder = inOrder(cartServiceMock, checkoutComApmMappedPaymentInfoReverseConverterMock, paymentInfoServiceMock, benefitPayPaymentInfoMock);
        inOrder.verify(cartServiceMock).hasSessionCart();
        inOrder.verify(cartServiceMock).getSessionCart();
        inOrder.verify(checkoutComApmMappedPaymentInfoReverseConverterMock).convertAPMPaymentInfoData(apmPaymentInfoDataMock, CheckoutComPaymentType.BENEFITPAY);
        inOrder.verify(paymentInfoServiceMock).createPaymentInfo(benefitPayPaymentInfoMock, cartModelMock);
    }

    @Test
    public void addPaymentInfoToCart_WhenAPMPaymentInfoDataAndCartAlreadyHAsPaymentInfo_ShouldSetThePaymentToSessionCart() {
        doNothing().when(paymentInfoServiceMock).createPaymentInfo(benefitPayPaymentInfoMock, cartModelMock);
        when(cartModelMock.getPaymentInfo()).thenReturn(checkoutComCreditCardPaymentInfoModelMock);

        testObj.addPaymentInfoToCart(apmPaymentInfoDataMock);

        final InOrder inOrder = inOrder(cartServiceMock, apmPaymentInfoMock, paymentInfoServiceMock, benefitPayPaymentInfoMock);
        inOrder.verify(cartServiceMock).hasSessionCart();
        inOrder.verify(cartServiceMock).getSessionCart();
        inOrder.verify(paymentInfoServiceMock).removePaymentInfo(cartModelMock);
        inOrder.verify(paymentInfoServiceMock).createPaymentInfo(benefitPayPaymentInfoMock, cartModelMock);
    }

    @Test
    public void addPaymentInfoToCart_WhenACHPaymentInfoDataAndCartAlreadyHAsPaymentInfo_ShouldSetThePaymentToSessionCart() {
        ReflectionTestUtils.setField(testObj, "checkoutComAchPaymentInfoReverseConverter", checkoutComAchPaymentInfoReverseConverterMock);
        testObj.addPaymentInfoToCart(achPaymentInfoDataMock);

        verify(paymentInfoServiceMock).createPaymentInfo(checkoutComAchPaymentInfoModelMock, cartModelMock);
    }


    @Test(expected = IllegalArgumentException.class)
    public void isTokenMissingOnCardPaymentInfo_WhenCartNull_ShouldThrowException() {
        testObj.isTokenMissingOnCardPaymentInfo(null);
    }

    @Test
    public void isTokenMissingOnCardPaymentInfo_WhenCartDoesNotHavePaymentInfo_ShouldReturnFalse() {
        when(cartDataMock.getPaymentInfo()).thenReturn(null);

        assertFalse(testObj.isTokenMissingOnCardPaymentInfo(cartDataMock));
    }

    @Test
    public void isTokenMissingOnCardPaymentInfo_WhenCartDoesNotHavePaymentToken_ShouldReturnTrue() {
        when(ccPaymentInfoDataMock.getPaymentToken()).thenReturn("");

        assertTrue(testObj.isTokenMissingOnCardPaymentInfo(cartDataMock));
    }

    @Test
    public void isTokenMissingOnCardPaymentInfo_WhenCartHasPaymentToken_ShouldReturnFalse() {
        assertFalse(testObj.isTokenMissingOnCardPaymentInfo(cartDataMock));
    }

    @Test(expected = IllegalArgumentException.class)
    public void updatePaymentInfoFromResponse_WhenPaymentInfoIsNull_ShouldThrowException() {
        testObj.updatePaymentInfoFromResponse(getPaymentResponseMock, null);
    }

    @Test
    public void updatePaymentInfoFromResponse_WhenPaymentStrategyFound_ShouldCallStrategy() {
        when(checkoutComPaymentTypeResolverMock.resolvePaymentType(apmPaymentInfoMock)).thenReturn(BENEFITPAY);
        when(checkoutComUpdatePaymentInfoStrategyMapperMock.findStrategy(BENEFITPAY)).thenReturn(benefitPayPaymentInfoStrategyMock);
        doNothing().when(benefitPayPaymentInfoStrategyMock).processPaymentResponse(getPaymentResponseMock);

        testObj.updatePaymentInfoFromResponse(getPaymentResponseMock, apmPaymentInfoMock);

        verify(checkoutComPaymentTypeResolverMock).resolvePaymentType(apmPaymentInfoMock);
        verify(checkoutComUpdatePaymentInfoStrategyMapperMock).findStrategy(BENEFITPAY);
        verify(benefitPayPaymentInfoStrategyMock).processPaymentResponse(getPaymentResponseMock);
    }

    @Test
    public void processPaymentDetails_WhenResponseIsValid_ShouldDelegateLogicToCorrectStrategy() {
        doNothing().when(testObj).updatePaymentInfoFromResponse(getPaymentResponseMock, apmPaymentInfoMock);
        when(cartModelMock.getPaymentInfo()).thenReturn(apmPaymentInfoMock);

        testObj.processPaymentDetails(getPaymentResponseMock);

        verify(cartServiceMock).hasSessionCart();
        verify(cartServiceMock).getSessionCart();
        verify(testObj).updatePaymentInfoFromResponse(getPaymentResponseMock, apmPaymentInfoMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void processPaymentDetails_WhenNoSessionCart_ShouldThrowException() {
        when(cartServiceMock.hasSessionCart()).thenReturn(false);

        testObj.processPaymentDetails(getPaymentResponseMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void processPaymentDetails_WhenNoCartPaymentInfo_ShouldThrowException() {
        when(cartModelMock.getPaymentInfo()).thenReturn(null);

        testObj.processPaymentDetails(getPaymentResponseMock);
    }

    @Test
    public void createPaymentInfoData_WhenCardPaymentMethod_ShouldReturnCCPaymentInfoData() {
        final Object result = testObj.createPaymentInfoData(CARD.name());

        assertTrue(result instanceof CCPaymentInfoData);
    }

    @Test
    public void createPaymentInfoData_WhenFawryPaymentMethod_ShouldReturnFawryPaymentInfoData() {
        final Object result = testObj.createPaymentInfoData(FAWRY.name());

        assertTrue(result instanceof FawryPaymentInfoData);
        assertEquals(FAWRY.name(), ((FawryPaymentInfoData) result).getType());
    }

    @Test
    public void createPaymentInfoData_WhenAchPaymentMethod_ShouldReturnAchPaymentInfoData() {
        final Object result = testObj.createPaymentInfoData(ACH.name());

        assertTrue(result instanceof AchPaymentInfoData);
    }

    @Test
    public void createPaymentInfoData_WhenSepaPaymentMethod_ShouldReturnSepaPaymentInfoData() {
        final Object result = testObj.createPaymentInfoData(SEPA.name());

        assertTrue(result instanceof SepaPaymentInfoData);
        assertEquals(SEPA.name(), ((SepaPaymentInfoData) result).getType());
    }

    @Test
    public void createPaymentInfoData_WhenKlarnaPaymentMethod_ShouldReturnKlarnaPaymentInfoData() {
        final Object result = testObj.createPaymentInfoData(KLARNA.name());

        assertTrue(result instanceof KlarnaPaymentInfoData);
        assertEquals(KLARNA.name(), ((KlarnaPaymentInfoData) result).getType());
    }

    @Test
    public void createPaymentInfoData_WhenOxxoPaymentMethod_ShouldReturnOxxoPaymentInfoData() {
        final Object result = testObj.createPaymentInfoData(OXXO.name());

        assertTrue(result instanceof OxxoPaymentInfoData);
        assertEquals(OXXO.name(), ((OxxoPaymentInfoData) result).getType());
    }

    @Test
    public void createPaymentInfoData_WhenOtherApmPaymentMethod_ShouldReturnApmPaymentInfoData() {
        final Object result = testObj.createPaymentInfoData(MULTIBANCO.name());

        assertTrue(result instanceof APMPaymentInfoData);
        assertEquals(MULTIBANCO.name(), ((APMPaymentInfoData) result).getType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPaymentInfoData_WhenInvalidPaymentMethod_ShouldReturnApmPaymentInfoData() {

        testObj.createPaymentInfoData("notSupportedPayment");
    }
}
