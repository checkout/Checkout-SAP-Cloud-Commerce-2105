package com.checkout.hybris.core.payment.services.impl;

import com.checkout.hybris.core.address.services.CheckoutComAddressService;
import com.checkout.hybris.core.enums.EnvironmentType;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.*;
import com.checkout.hybris.core.order.daos.CheckoutComOrderDao;
import com.checkout.hybris.core.payment.daos.CheckoutComPaymentInfoDao;
import com.checkout.payments.CardSourceResponse;
import com.google.common.collect.ImmutableList;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.core.GenericSearchConstants;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComPaymentInfoServiceTest {

    private static final String SITE_ID = "electronics";
    private static final String CART_CODE = "CART_CODE";
    private static final String USER_ID = "USER_ID";
    private static final String QR_CODE_DATA_VALUE = "gesbyhnfuerwfluihltgsbdkljnkwb5tfrcduegw";
    private static final String PAYMENT_1_CODE = "payment1Code";
    private static final String PAYMENT_2_CODE = "payment2Code";
    private static final String SUBSCRIPTION_ID = "subscriptionID";
    public static final String RESPONSE = "response";
    public static final String REQUEST = "request";
    public static final String PAYMENT_REFERENCE = "paymentReference";
    public static final String PAYLOAD = "payload";

    @Spy
    @InjectMocks
    private DefaultCheckoutComPaymentInfoService testObj;

    @Mock
    private ModelService modelServiceMock;
    @Mock
    private CheckoutComAddressService addressServiceMock;
    @Mock
    private CheckoutComPaymentInfoDao checkoutComPaymentInfoDaoMock;
    @Mock
    private CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationServiceMock;
    @Mock
    private CheckoutComOrderDao checkoutComOrderDaoMock;

    @Mock
    private CartModel cartModelMock;
    @Mock
    private CheckoutComCreditCardPaymentInfoModel checkoutComCreditCardPaymentInfoModelMock;
    @Mock
    private PaymentInfoModel paymentInfoModelMock;
    @Mock
    private CustomerModel userMock;
    @Mock
    private AddressModel paymentAddressMock;
    @Mock
    private AddressModel clonedAddressMock;
    @Mock
    private CheckoutComAPMPaymentInfoModel redirectApmPaymentInfoMock;
    @Mock
    private CheckoutComBenefitPayPaymentInfoModel benefitPayPaymentInfoMock;
    @Mock
    private CheckoutComCreditCardPaymentInfoModel cardPaymentInfoMock, userPaymentInfo1Mock, userPaymentInfo2Mock;
    @Mock
    private PayloadModel payloadModelMock;
    @Mock
    private CardSourceResponse sourceMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private OrderModel orderMock;
    @Mock
    private CheckoutComFawryPaymentInfoModel fawryPaymentInfoMock;



    @Before
    public void setUp() {
        when(cartModelMock.getUser()).thenReturn(userMock);
        when(cartModelMock.getCode()).thenReturn(CART_CODE);
        when(checkoutComCreditCardPaymentInfoModelMock.getItemtype()).thenReturn(CheckoutComCreditCardPaymentInfoModel._TYPECODE);
        when(redirectApmPaymentInfoMock.getItemtype()).thenReturn(CheckoutComAPMPaymentInfoModel._TYPECODE);
        when(benefitPayPaymentInfoMock.getItemtype()).thenReturn(CheckoutComBenefitPayPaymentInfoModel._TYPECODE);
        when(cartModelMock.getPaymentInfo()).thenReturn(paymentInfoModelMock);
        doReturn(modelServiceMock).when(testObj).callSuperModelService();
        when(paymentInfoModelMock.getItemtype()).thenReturn(PaymentInfoModel._TYPECODE);
        when(userMock.getUid()).thenReturn(USER_ID);
        when(cartModelMock.getPaymentAddress()).thenReturn(paymentAddressMock);
        when(orderMock.getPaymentInfo()).thenReturn(cardPaymentInfoMock);
        when(cardPaymentInfoMock.getCode()).thenReturn(PAYMENT_1_CODE);
        when(cardPaymentInfoMock.getUser()).thenReturn(userMock);
        when(cartModelMock.getPaymentInfo()).thenReturn(cardPaymentInfoMock);
        when(userMock.getPaymentInfos()).thenReturn(ImmutableList.of(userPaymentInfo1Mock, userPaymentInfo2Mock));
        when(sourceMock.getId()).thenReturn(SUBSCRIPTION_ID);
        when(cartModelMock.getUser()).thenReturn(userMock);
        when(userMock.getUid()).thenReturn(USER_ID);
        when(userMock.getPaymentInfos()).thenReturn(ImmutableList.of(userPaymentInfo1Mock, userPaymentInfo2Mock));
        when(userPaymentInfo1Mock.getCode()).thenReturn(PAYMENT_1_CODE);
        when(userPaymentInfo2Mock.getCode()).thenReturn(PAYMENT_2_CODE);
        when(checkoutComPaymentInfoDaoMock.findPaymentInfosByPaymentId(PAYMENT_1_CODE)).thenReturn(ImmutableList.of(paymentInfoModelMock, cardPaymentInfoMock));
        when(paymentInfoModelMock.getOwner()).thenReturn(orderMock);
        when(cardPaymentInfoMock.getOwner()).thenReturn(userMock);
        when(orderMock.getSite().getUid()).thenReturn(SITE_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPaymentInfo_WhenNullCart_ShouldThrowException() {
        testObj.createPaymentInfo(checkoutComCreditCardPaymentInfoModelMock, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPaymentInfo_WhenNullPaymentInfo_ShouldThrowException() {
        testObj.createPaymentInfo(null, cartModelMock);
    }

    @Test
    public void createPaymentInfo_WhenCheckoutComCreditCardInfo_ShouldWorkProperly() {
        doReturn(clonedAddressMock).when(testObj).cloneAndSetBillingAddressFromCart(cartModelMock, checkoutComCreditCardPaymentInfoModelMock);

        testObj.createPaymentInfo(checkoutComCreditCardPaymentInfoModelMock, cartModelMock);

        final InOrder inOrder = inOrder(checkoutComCreditCardPaymentInfoModelMock, modelServiceMock, cartModelMock);
        inOrder.verify(checkoutComCreditCardPaymentInfoModelMock).setUser(userMock);
        inOrder.verify(modelServiceMock).save(checkoutComCreditCardPaymentInfoModelMock);
        inOrder.verify(cartModelMock).setPaymentInfo(checkoutComCreditCardPaymentInfoModelMock);
        inOrder.verify(modelServiceMock).save(cartModelMock);
    }

    @Test
    public void createPaymentInfo_ForApmPaymentInfo_ShouldWorkProperly() {
        doReturn(clonedAddressMock).when(testObj).cloneAndSetBillingAddressFromCart(cartModelMock, redirectApmPaymentInfoMock);

        testObj.createPaymentInfo(redirectApmPaymentInfoMock, cartModelMock);

        final InOrder inOrder = inOrder(redirectApmPaymentInfoMock, modelServiceMock, cartModelMock);
        inOrder.verify(redirectApmPaymentInfoMock).setUser(userMock);
        inOrder.verify(modelServiceMock).save(redirectApmPaymentInfoMock);
        inOrder.verify(cartModelMock).setPaymentInfo(redirectApmPaymentInfoMock);
        inOrder.verify(modelServiceMock).save(cartModelMock);
    }

    @Test
    public void createPaymentInfo_ForBenefitPaymentInfo_ShouldWorkProperly() {
        doReturn(clonedAddressMock).when(testObj).cloneAndSetBillingAddressFromCart(cartModelMock, benefitPayPaymentInfoMock);

        testObj.createPaymentInfo(benefitPayPaymentInfoMock, cartModelMock);

        final InOrder inOrder = inOrder(benefitPayPaymentInfoMock, modelServiceMock, cartModelMock);
        inOrder.verify(benefitPayPaymentInfoMock).setUser(userMock);
        inOrder.verify(modelServiceMock).save(benefitPayPaymentInfoMock);
        inOrder.verify(cartModelMock).setPaymentInfo(benefitPayPaymentInfoMock);
        inOrder.verify(modelServiceMock).save(cartModelMock);
    }

    @Test
    public void createPaymentInfo_ForFawryPaymentInfo_ShouldWorkProperly() {
        doReturn(clonedAddressMock).when(testObj).cloneAndSetBillingAddressFromCart(cartModelMock, fawryPaymentInfoMock);

        testObj.createPaymentInfo(fawryPaymentInfoMock, cartModelMock);

        final InOrder inOrder = inOrder(fawryPaymentInfoMock, modelServiceMock, cartModelMock);
        inOrder.verify(fawryPaymentInfoMock).setUser(userMock);
        inOrder.verify(modelServiceMock).save(fawryPaymentInfoMock);
        inOrder.verify(cartModelMock).setPaymentInfo(fawryPaymentInfoMock);
        inOrder.verify(modelServiceMock).save(cartModelMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPaymentInfo_ForGenericPaymentInfo_ShouldThrowException() {
        doReturn(clonedAddressMock).when(testObj).cloneAndSetBillingAddressFromCart(cartModelMock, paymentInfoModelMock);

        testObj.createPaymentInfo(paymentInfoModelMock, cartModelMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removePaymentInfo_WhenNullCart_ShouldThrowException() {
        testObj.removePaymentInfo(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removePaymentInfo_WhenNullPaymentInfo_ShouldThrowException() {
        when(cartModelMock.getPaymentInfo()).thenReturn(null);

        testObj.removePaymentInfo(cartModelMock);
    }

    @Test
    public void removePaymentInfo_ShouldRemovePaymentInfoFromCart() {
        testObj.removePaymentInfo(cartModelMock);

        verify(modelServiceMock).remove(cardPaymentInfoMock);
        verify(modelServiceMock).save(cartModelMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void removePaymentInfo_WhenPaymentInfoIsNotPresent_ShouldThrowException() {
        when(cartModelMock.getPaymentInfo()).thenReturn(null);

        testObj.removePaymentInfo(cartModelMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cloneAndSetBillingAddressFromCart_WhenCartIsNull_ShouldThrowException() {
        testObj.cloneAndSetBillingAddressFromCart(null, paymentInfoModelMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cloneAndSetBillingAddressFromCart_WhenPaymentAddressNull_ShouldThrowException() {
        when(cartModelMock.getPaymentAddress()).thenReturn(null);

        testObj.cloneAndSetBillingAddressFromCart(cartModelMock, paymentInfoModelMock);
    }

    @Test
    public void cloneAndSetBillingAddressFromCart_WhenEverythingIsCorrect_Should() {
        when(addressServiceMock.cloneAddressForOwner(paymentAddressMock, paymentInfoModelMock)).thenReturn(new AddressModel());

        final AddressModel result = testObj.cloneAndSetBillingAddressFromCart(cartModelMock, paymentInfoModelMock);

        assertTrue(result.getBillingAddress());
        assertFalse(result.getShippingAddress());
        assertEquals(paymentInfoModelMock, result.getOwner());
        verify(paymentInfoModelMock).setBillingAddress(result);
    }

    @Test
    public void isValidCreditCardPaymentInfo_WhenCartPaymentIsInvalid_ShouldReturnFalse() {
        assertFalse(testObj.isValidCreditCardPaymentInfo(cartModelMock));
    }

    @Test
    public void isValidCreditCardPaymentInfo_WhenCardTokenIsNull_ShouldReturnFalse() {
        when(cartModelMock.getPaymentInfo()).thenReturn(checkoutComCreditCardPaymentInfoModelMock);

        assertFalse(testObj.isValidCreditCardPaymentInfo(cartModelMock));
    }

    @Test
    public void isValidCreditCardPaymentInfo_WhenCardPaymentIsValid_ShouldReturnTrue() {
        when(cartModelMock.getPaymentInfo()).thenReturn(checkoutComCreditCardPaymentInfoModelMock);
        when(checkoutComCreditCardPaymentInfoModelMock.getCardToken()).thenReturn("someToken");

        assertTrue(testObj.isValidCreditCardPaymentInfo(cartModelMock));
    }

    @Test
    public void isValidRedirectApmPaymentInfo_WhenApmIsInvalid_ShouldReturnFalse() {
        assertFalse(testObj.isValidRedirectApmPaymentInfo(cartModelMock));
    }

    @Test
    public void isValidRedirectApmPaymentInfo_WhenApmTypeIsNull_ShouldReturnFalse() {
        when(cartModelMock.getPaymentInfo()).thenReturn(redirectApmPaymentInfoMock);

        assertFalse(testObj.isValidRedirectApmPaymentInfo(cartModelMock));
    }

    @Test
    public void isValidRedirectApmPaymentInfo_WhenApmIsValid_ShouldReturnTrue() {
        when(cartModelMock.getPaymentInfo()).thenReturn(redirectApmPaymentInfoMock);
        when(redirectApmPaymentInfoMock.getType()).thenReturn("APM");

        assertTrue(testObj.isValidRedirectApmPaymentInfo(cartModelMock));
    }

    @Test(expected = IllegalArgumentException.class)
    public void isUserDataRequiredApmPaymentMethod_WhenCartIsNull_ShouldThrowException() {
        testObj.isUserDataRequiredApmPaymentMethod(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void isUserDataRequiredApmPaymentMethod_WhenCartDoesNotHavePaymentInfo_ShouldThrowException() {
        when(cartModelMock.getPaymentInfo()).thenReturn(null);

        testObj.isUserDataRequiredApmPaymentMethod(cartModelMock);
    }

    @Test
    public void isUserDataRequiredApmPaymentMethod_WhenCartHasNotApmPaymentInfo_ShouldReturnFalse() {
        assertFalse(testObj.isUserDataRequiredApmPaymentMethod(cartModelMock));
    }

    @Test
    public void isUserDataRequiredApmPaymentMethod_WhenSessionCartHasRedirectApmPaymentInfoWithDataRequiredForm_ShouldReturnTrue
            () {
        when(cartModelMock.getPaymentInfo()).thenReturn(redirectApmPaymentInfoMock);
        when(redirectApmPaymentInfoMock.getUserDataRequired()).thenReturn(true);

        assertTrue(testObj.isUserDataRequiredApmPaymentMethod(cartModelMock));
    }

    @Test
    public void isUserDataRequiredApmPaymentMethod_WhenSessionCartHasRedirectApmPaymentInfoWithoutDataRequiredForm_ShouldReturnFalse
            () {
        when(cartModelMock.getPaymentInfo()).thenReturn(redirectApmPaymentInfoMock);
        when(redirectApmPaymentInfoMock.getUserDataRequired()).thenReturn(false);

        assertFalse(testObj.isUserDataRequiredApmPaymentMethod(cartModelMock));
    }

    @Test
    public void isValidPaymentInfo_WhenPaymentInfoIsValid_ShouldReturnTrue() {
        doReturn(true).when(testObj).isValidCreditCardPaymentInfo(cartModelMock);

        assertTrue(testObj.isValidPaymentInfo(cartModelMock));
    }

    @Test
    public void isValidPaymentInfo_WhenPaymentInfoIsNotValid_ShouldReturnFalse() {
        doReturn(false).when(testObj).isValidCreditCardPaymentInfo(cartModelMock);
        doReturn(false).when(testObj).isValidRedirectApmPaymentInfo(cartModelMock);

        assertFalse(testObj.isValidPaymentInfo(cartModelMock));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addQRCodeDataToBenefitPaymentInfo_WhenPaymentInfoIsNull_ShouldThrowException() {
        testObj.addQRCodeDataToBenefitPaymentInfo(null, QR_CODE_DATA_VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addQRCodeDataToBenefitPaymentInfo_WhenUserDataIsBlank_ShouldThrowException() {
        testObj.addQRCodeDataToBenefitPaymentInfo(benefitPayPaymentInfoMock, "    ");
    }

    @Test
    public void addQRCodeDataToBenefitPaymentInfo_WhenUserDataIsPresent_ShouldUpdatePaymentInfoProperly() {
        testObj.addQRCodeDataToBenefitPaymentInfo(benefitPayPaymentInfoMock, QR_CODE_DATA_VALUE);

        verify(benefitPayPaymentInfoMock).setQrCode(QR_CODE_DATA_VALUE);
        verify(modelServiceMock).save(benefitPayPaymentInfoMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSubscriptionToUserPayment_WhenPaymentInfoIsNull_ShouldThrowException() {
        testObj.addSubscriptionIdToUserPayment(null, sourceMock);
    }

    @Test
    public void addSubscriptionToUserPayment_WhenMarkedToSaveCard_ShouldSetSubscription() {
        when(cardPaymentInfoMock.getMarkToSave()).thenReturn(true);

        testObj.addSubscriptionIdToUserPayment(cardPaymentInfoMock, sourceMock);

        final InOrder inOrder = inOrder(userPaymentInfo1Mock, modelServiceMock);
        inOrder.verify(userPaymentInfo1Mock).setSaved(true);
        inOrder.verify(userPaymentInfo1Mock).setSubscriptionId(SUBSCRIPTION_ID);
        inOrder.verify(modelServiceMock).save(userPaymentInfo1Mock);
    }

    @Test
    public void addSubscriptionToUserPayment_WhenNoMatchUserPayment_ShouldNotSetSubscription() {
        when(cardPaymentInfoMock.getMarkToSave()).thenReturn(true);
        when(userMock.getPaymentInfos()).thenReturn(ImmutableList.of(userPaymentInfo2Mock));

        testObj.addSubscriptionIdToUserPayment(cardPaymentInfoMock, sourceMock);

        verifyZeroInteractions(modelServiceMock);
    }

    @Test
    public void addSubscriptionToUserPayment_WhenNotMarkedToSaveCard_ShouldNotSetSubscription() {
        when(cardPaymentInfoMock.getMarkToSave()).thenReturn(false);

        testObj.addSubscriptionIdToUserPayment(cardPaymentInfoMock, sourceMock);

        verifyZeroInteractions(modelServiceMock);
    }

    @Test
    public void addSubscriptionToUserPayment_WhenNotCardSource_ShouldNotSetSubscription() {
        when(cardPaymentInfoMock.getMarkToSave()).thenReturn(false);

        testObj.addSubscriptionIdToUserPayment(cardPaymentInfoMock, sourceMock);

        verifyZeroInteractions(modelServiceMock);
    }

    @Test
    public void addSubscriptionToUserPayment_WhenSourceIdNull_ShouldNotSetSubscription() {
        when(cardPaymentInfoMock.getMarkToSave()).thenReturn(false);

        testObj.addSubscriptionIdToUserPayment(cardPaymentInfoMock, sourceMock);

        verifyZeroInteractions(modelServiceMock);
    }

    @Test
    public void addPaymentId_ShouldAddThePaymentIdIntoTheCartPaymentInfo() {
        testObj.addPaymentId(PAYMENT_1_CODE, cardPaymentInfoMock);

        final InOrder inOrder = inOrder(cardPaymentInfoMock, modelServiceMock);
        inOrder.verify(cardPaymentInfoMock).setPaymentId(PAYMENT_1_CODE);
        inOrder.verify(modelServiceMock).save(cardPaymentInfoMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPaymentId_WhenPaymentInfoIsNull_ShouldThrowException() {
        when(cartModelMock.getPaymentInfo()).thenReturn(null);

        testObj.addPaymentId(PAYMENT_1_CODE, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addPaymentId_WhenPaymentIdIsBlank_ShouldThrowException() {
        testObj.addPaymentId("    ", paymentInfoModelMock);
    }

    @Test
    public void createCommerceCheckoutParameter_WhenGivenInputsAreCorrect_ShouldCreateCorrectCommerceCheckoutParameter() {
        final CommerceCheckoutParameter result = testObj.createCommerceCheckoutParameter(cartModelMock);

        assertEquals(cartModelMock, result.getCart());
        assertTrue(result.isEnableHooks());
    }

    @Test
    public void getSiteIdFromPaymentId_WhenPaymentIdExistsInAbstractOrder_ShouldReturnSiteId() {
        final String result = testObj.getSiteIdFromPaymentId(PAYMENT_1_CODE);

        assertEquals(SITE_ID, result);
    }

    @Test
    public void getSiteIdFromPaymentId_WhenPaymentIdDoesNotBelongToAbstractOrder_ShouldReturnEmptyString() {
        when(checkoutComPaymentInfoDaoMock.findPaymentInfosByPaymentId(PAYMENT_1_CODE)).thenReturn(Collections.emptyList());

        final String result = testObj.getSiteIdFromPaymentId(PAYMENT_1_CODE);

        assertTrue(isBlank(result));
    }

    @Test
    public void findAbstractOrderByPaymentId_WhenPaymentIdExistsInAbstractOrder_ShouldReturnSiteId() {
        final List<AbstractOrderModel> result = testObj.findAbstractOrderByPaymentId(PAYMENT_1_CODE);

        assertEquals(1, result.size());
        assertEquals(orderMock, result.get(0));
    }

    @Test
    public void findAbstractOrderByPaymentId_WhenPaymentIdDoesNotBelongToAbstractOrder_ShouldReturnEmptyString() {
        when(checkoutComPaymentInfoDaoMock.findPaymentInfosByPaymentId(PAYMENT_1_CODE)).thenReturn(ImmutableList.of(cardPaymentInfoMock));

        final List<AbstractOrderModel> result = testObj.findAbstractOrderByPaymentId(PAYMENT_1_CODE);

        assertTrue(result.isEmpty());
    }

    @Test
    public void getPaymentInfosByPaymentId_ShouldCallDao() {
        testObj.getPaymentInfosByPaymentId(PAYMENT_1_CODE);

        verify(checkoutComPaymentInfoDaoMock).findPaymentInfosByPaymentId(PAYMENT_1_CODE);
    }

    @Test
    public void saveRequestAndResponseInOrder_shouldCallModelService() {
        when(orderMock.getRequestsPayload()).thenReturn(Collections.emptyList());
        when(orderMock.getResponsesPayload()).thenReturn(Collections.emptyList());
        doReturn(payloadModelMock).when(testObj).createPayloadModel(anyString());


        testObj.saveRequestAndResponseInOrder(orderMock, REQUEST, RESPONSE);

        verify(modelServiceMock).save(orderMock);
    }

    @Test
    public void saveResponseInOrderByPaymentReference_shouldCallModelService_whenOrderIsFound() {
        when(orderMock.getRequestsPayload()).thenReturn(Collections.emptyList());
        when(orderMock.getResponsesPayload()).thenReturn(Collections.emptyList());
        doReturn(payloadModelMock).when(testObj).createPayloadModel(anyString());
        doReturn(Optional.of(orderMock))
                .when(checkoutComOrderDaoMock).findAbstractOrderForPaymentReferenceNumber(PAYMENT_REFERENCE);

        testObj.saveResponseInOrderByPaymentReference(PAYMENT_REFERENCE, RESPONSE);

        verify(modelServiceMock).save(orderMock);
    }

    @Test
    public void logInfoOutput_shouldLogInfo_whenEnvironmentEqualsTest() {
        when(checkoutComMerchantConfigurationServiceMock.getEnvironment()).thenReturn(EnvironmentType.TEST);
        testObj.logInfoOut(PAYLOAD);

        verify(checkoutComMerchantConfigurationServiceMock).getEnvironment();
    }

    @Test
    public void logInfoOutput_shouldLogInfo_whenEnvironmentEqualsPROD() {
        when(checkoutComMerchantConfigurationServiceMock.getEnvironment()).thenReturn(EnvironmentType.PRODUCTION);
        testObj.logInfoOut(PAYLOAD);

        verify(checkoutComMerchantConfigurationServiceMock).getEnvironment();
    }

    @Test
    public void createPayloadModel_shouldCreatePayloadModel() {
        when(modelServiceMock.create(PayloadModel.class)).thenReturn(payloadModelMock);

        testObj.createPayloadModel(PAYLOAD);

        verify(payloadModelMock).setPayload(PAYLOAD);
        verify(modelServiceMock).save(payloadModelMock);
    }
}
