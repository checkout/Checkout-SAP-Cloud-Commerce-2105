package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.common.Phone;
import com.checkout.hybris.core.address.strategies.CheckoutComPhoneNumberStrategy;
import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.enums.MadaBin;
import com.checkout.hybris.core.enums.PaymentActionType;
import com.checkout.hybris.core.enums.PaymentTypes;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.merchantconfiguration.BillingDescriptor;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.request.mappers.CheckoutComPaymentRequestStrategyMapper;
import com.checkout.hybris.core.payment.request.strategies.CheckoutComPaymentRequestStrategy;
import com.checkout.hybris.core.populators.payments.CheckoutComCartModelToPaymentL2AndL3Converter;
import com.checkout.hybris.core.url.services.CheckoutComUrlService;
import com.checkout.payments.*;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.enumeration.EnumerationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.internal.util.reflection.Whitebox;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Optional.of;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComAbstractPaymentRequestStrategyTest {

    private static final String BILLING_DESCRIPTOR_NAME = "billingDescriptorName";
    private static final String BILLING_DESCRIPTOR_CITY = "billingDescriptorCity";
    private static final String DISPLAY_CUSTOMER_NAME = "Customer Name";
    private static final String SITE_ID_KEY = "site_id";
    private static final String LINE_2 = "LINE_2";
    private static final String UK_COUNTRY_CODE = "UK";
    private static final String UK_STATE = "United kingdom";
    private static final String POST_CODE = "POST_CODE";
    private static final double TOTAL_PRICE = 100D;
    private static final String GBP = "GBP";
    private static final Long CHECKOUT_COM_TOTAL_PRICE = 10000L;
    private static final String TOKEN = "TOKEN";
    private static final String CUSTOMER_EMAIL = "email@email.com";
    private static final String CART_REFERENCE = "CART_REFERENCE";
    private static final String NUMBER = "213423423";
    private static final String CHECKOUT_COM_PAYMENT_REDIRECT_PAYMENT_SUCCESS = "/successUrl";
    private static final String CHECKOUT_COM_PAYMENT_REDIRECT_PAYMENT_FAILURE = "/failureUrl";
    private static final String SITE_ID = "SITE_ID";
    private static final String TOWN = "Town";
    private static final String LINE_1 = "Line 1";
    private static final String SUBSCRIPTION_ID = "subscriptionId";

    @Spy
    private CheckoutComAbstractPaymentRequestStrategy testObj;

    @Mock
    private CartModel cartModelMock;
    @Mock
    private CheckoutComCreditCardPaymentInfoModel paymentInfoMock;
    @Mock
    private EnumerationService enumerationServiceMock;
    @Mock
    private CMSSiteService cmsSiteServiceMock;
    @Mock
    private CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationServiceMock;
    @Mock
    private CheckoutComCurrencyService checkoutComCurrencyServiceMock;
    @Mock
    private CheckoutComCartModelToPaymentL2AndL3Converter checkoutComCartModelToPaymentL2AndL3ConverterMock;
    @Mock
    private CustomerModel customerModelMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AddressModel cartAddressModelMock;
    @Mock
    private CurrencyModel currencyModelMock;
    @Mock
    private CheckoutComPhoneNumberStrategy checkoutComPhoneNumberStrategyMock;
    @Mock
    private Phone phoneMock;
    @Mock
    private CheckoutComUrlService checkoutComUrlServiceMock;
    @Mock
    private CMSSiteModel currentSiteMock;
    @Mock
    private PaymentRequest<RequestSource> paymentRequestMock;
    @Mock
    private CustomerRequest customerRequestMock;
    @Mock
    private ShippingDetails shippingDetailsMock;
    @Mock
    private CheckoutComPaymentRequestStrategyMapper checkoutComPaymentRequestStrategyMapperMock;
    @Mock
    private BillingDescriptor billingDescriptorMock;
    @Captor
    private ArgumentCaptor<RiskRequest> riskRequestCaptor;
    @Captor
    private ArgumentCaptor<com.checkout.payments.BillingDescriptor> billingDescriptorCaptor;

    private final Map<String, Object> metadataMap = new HashMap<>();

    @Before
    public void setUp() {
        testObj = Mockito.mock(
                CheckoutComAbstractPaymentRequestStrategy.class,
                Mockito.CALLS_REAL_METHODS);

        Whitebox.setInternalState(testObj, "checkoutComUrlService", checkoutComUrlServiceMock);
        Whitebox.setInternalState(testObj, "checkoutComPhoneNumberStrategy", checkoutComPhoneNumberStrategyMock);
        Whitebox.setInternalState(testObj, "checkoutComCurrencyService", checkoutComCurrencyServiceMock);
        Whitebox.setInternalState(testObj, "checkoutComPaymentRequestStrategyMapper", checkoutComPaymentRequestStrategyMapperMock);
        Whitebox.setInternalState(testObj, "cmsSiteService", cmsSiteServiceMock);
        Whitebox.setInternalState(testObj, "checkoutComMerchantConfigurationService",
                                  checkoutComMerchantConfigurationServiceMock);
        Whitebox.setInternalState(testObj, "checkoutComCartModelToPaymentL2AndL3Converter",
                                  checkoutComCartModelToPaymentL2AndL3ConverterMock);

        setUpCart();
        setUpAddress();
        setUpConfiguration();

        when(paymentRequestMock.getMetadata()).thenReturn(metadataMap);
        when(enumerationServiceMock.getEnumerationValues(MadaBin.class)).thenReturn(emptyList());
        when(paymentInfoMock.getCardToken()).thenReturn(TOKEN);
        when(currencyModelMock.getIsocode()).thenReturn(GBP);
        when(customerModelMock.getContactEmail()).thenReturn(CUSTOMER_EMAIL);
        when(customerModelMock.getDisplayName()).thenReturn(DISPLAY_CUSTOMER_NAME);
        when(cartModelMock.getCurrency()).thenReturn(currencyModelMock);
        when(cartModelMock.getTotalPrice()).thenReturn(TOTAL_PRICE);
        when(checkoutComMerchantConfigurationServiceMock.getBillingDescriptor()).thenReturn(billingDescriptorMock);
        when(billingDescriptorMock.getBillingDescriptorName()).thenReturn(BILLING_DESCRIPTOR_NAME);
        when(billingDescriptorMock.getBillingDescriptorCity()).thenReturn(BILLING_DESCRIPTOR_CITY);
    }

    private void setUpConfiguration() {
        when(checkoutComMerchantConfigurationServiceMock.getPaymentAction()).thenReturn(PaymentActionType.AUTHORIZE);
        when(checkoutComCurrencyServiceMock.convertAmountIntoPennies(GBP, TOTAL_PRICE)).thenReturn(CHECKOUT_COM_TOTAL_PRICE);
        when(checkoutComMerchantConfigurationServiceMock.isAttemptNoThreeDSecure()).thenReturn(true);
        when(checkoutComMerchantConfigurationServiceMock.isThreeDSEnabled()).thenReturn(true);
        when(checkoutComUrlServiceMock.getFullUrl(CHECKOUT_COM_PAYMENT_REDIRECT_PAYMENT_SUCCESS, true)).thenReturn(CHECKOUT_COM_PAYMENT_REDIRECT_PAYMENT_SUCCESS);
        when(checkoutComUrlServiceMock.getFullUrl(CHECKOUT_COM_PAYMENT_REDIRECT_PAYMENT_FAILURE, true)).thenReturn(CHECKOUT_COM_PAYMENT_REDIRECT_PAYMENT_FAILURE);
        when(cmsSiteServiceMock.getCurrentSite()).thenReturn(currentSiteMock);
        when(currentSiteMock.getUid()).thenReturn(SITE_ID);
        when(currentSiteMock.getCheckoutComSuccessRedirectUrl()).thenReturn(CHECKOUT_COM_PAYMENT_REDIRECT_PAYMENT_SUCCESS);
        when(currentSiteMock.getCheckoutComFailureRedirectUrl()).thenReturn(CHECKOUT_COM_PAYMENT_REDIRECT_PAYMENT_FAILURE);
    }

    private void setUpAddress() {
        when(checkoutComPhoneNumberStrategyMock.createPhone(cartAddressModelMock)).thenReturn(of(phoneMock));
        when(cartAddressModelMock.getLine1()).thenReturn(LINE_1);
        when(cartAddressModelMock.getLine2()).thenReturn(LINE_2);
        when(cartAddressModelMock.getCountry().getIsocode()).thenReturn(UK_COUNTRY_CODE);
        when(cartAddressModelMock.getRegion().getName()).thenReturn(UK_STATE);
        when(cartAddressModelMock.getTown()).thenReturn(TOWN);
        when(cartAddressModelMock.getPostalcode()).thenReturn(POST_CODE);
        when(phoneMock.getNumber()).thenReturn(NUMBER);
    }

    private void setUpCart() {
        when(cartModelMock.getTotalPrice()).thenReturn(TOTAL_PRICE);
        when(cartModelMock.getUser()).thenReturn(customerModelMock);
        when(cartModelMock.getCheckoutComPaymentReference()).thenReturn(CART_REFERENCE);
        when(cartModelMock.getDeliveryAddress()).thenReturn(cartAddressModelMock);
        when(cartModelMock.getPaymentAddress()).thenReturn(cartAddressModelMock);
        when(cartModelMock.getCurrency()).thenReturn(currencyModelMock);
        when(cartModelMock.getPaymentInfo()).thenReturn(paymentInfoMock);
    }

    @Test
    public void registerStrategy_ShouldRegisterTheStrategy() {
        testObj.registerStrategy();

        verify(checkoutComPaymentRequestStrategyMapperMock).addStrategy(any(CheckoutComPaymentType.class), any(CheckoutComPaymentRequestStrategy.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPaymentRequest_WhenCartIsNull_ShouldThrowException() {
        testObj.createPaymentRequest(null);
    }

    @Test
    public void createPaymentRequest_WhenNoMadaCard_ShouldCreateTheRequestProperlyWithIdSource() {
        doNothing().when(testObj).populatePaymentRequest(any(CartModel.class), any(PaymentRequest.class));
        doReturn(paymentRequestMock).when(testObj).getRequestSourcePaymentRequest(any(CartModel.class), anyString(), anyLong());
        when(paymentInfoMock.isSaved()).thenReturn(true);
        when(paymentInfoMock.getSubscriptionId()).thenReturn(SUBSCRIPTION_ID);

        final PaymentRequest<RequestSource> result = testObj.createPaymentRequest(cartModelMock);

        verify(testObj).populatePaymentRequest(cartModelMock, paymentRequestMock);
        assertSame(paymentRequestMock, result);
    }

    @Test
    public void populatePaymentRequest_WhenBillingDescriptorConfigurationIncluded_ShouldPopulateAndIncludeBillingDescriptor() {
        doReturn(customerRequestMock).when(testObj).getCustomerRequest(customerModelMock);
        doReturn(shippingDetailsMock).when(testObj).createShippingDetails(cartAddressModelMock);
        when(billingDescriptorMock.getIncludeBillingDescriptor()).thenReturn(true);

        testObj.populatePaymentRequest(cartModelMock, paymentRequestMock);

        verify(paymentRequestMock).setReference(CART_REFERENCE);
        verify(paymentRequestMock).setPaymentType(PaymentTypes.REGULAR.getCode());
        verify(paymentRequestMock).setCustomer(customerRequestMock);
        verify(paymentRequestMock).setShipping(shippingDetailsMock);
        verify(paymentRequestMock, never()).setCapture(anyBoolean());
        verify(paymentRequestMock).setSuccessUrl(CHECKOUT_COM_PAYMENT_REDIRECT_PAYMENT_SUCCESS);
        verify(paymentRequestMock).setFailureUrl(CHECKOUT_COM_PAYMENT_REDIRECT_PAYMENT_FAILURE);

        verify(paymentRequestMock).setBillingDescriptor(billingDescriptorCaptor.capture());
        assertEquals(billingDescriptorCaptor.getValue().getName(), BILLING_DESCRIPTOR_NAME);
        assertEquals(billingDescriptorCaptor.getValue().getCity(), BILLING_DESCRIPTOR_CITY);

        verify(paymentRequestMock).setRisk(riskRequestCaptor.capture());
        assertTrue(riskRequestCaptor.getValue().isEnabled());
    }

    @Test
    public void populatePaymentRequest_WhenBillingDescriptorConfigurationExcluded_ShouldPopulateAndNotIncludeBillingDescriptor() {
        doReturn(customerRequestMock).when(testObj).getCustomerRequest(customerModelMock);
        doReturn(shippingDetailsMock).when(testObj).createShippingDetails(cartAddressModelMock);
        when(billingDescriptorMock.getIncludeBillingDescriptor()).thenReturn(false);

        testObj.populatePaymentRequest(cartModelMock, paymentRequestMock);

        verify(paymentRequestMock).setReference(CART_REFERENCE);
        verify(paymentRequestMock).setPaymentType(PaymentTypes.REGULAR.getCode());
        verify(paymentRequestMock).setCustomer(customerRequestMock);
        verify(paymentRequestMock).setShipping(shippingDetailsMock);
        verify(paymentRequestMock, never()).setCapture(anyBoolean());
        verify(paymentRequestMock).setSuccessUrl(CHECKOUT_COM_PAYMENT_REDIRECT_PAYMENT_SUCCESS);
        verify(paymentRequestMock).setFailureUrl(CHECKOUT_COM_PAYMENT_REDIRECT_PAYMENT_FAILURE);

        verify(paymentRequestMock, never()).setBillingDescriptor(any(com.checkout.payments.BillingDescriptor.class));

        verify(paymentRequestMock).setRisk(riskRequestCaptor.capture());
        assertTrue(riskRequestCaptor.getValue().isEnabled());
    }

    @Test
    public void isCapture_WhenDefaultBehavior_ShouldReturnNull() {
        assertTrue(testObj.isCapture().isEmpty());
    }

    @Test
    public void createGenericMetadata_ShouldPopulateCorrectly() {
        final Map<String, Object> result = testObj.createGenericMetadata();

        assertTrue(result.containsKey(SITE_ID_KEY));
        assertEquals(SITE_ID, result.get(SITE_ID_KEY));
    }

    @Test
    public void createShippingDetails_ShouldCreateAddressCorrectly() {
        final ShippingDetails result = testObj.createShippingDetails(cartAddressModelMock);

        assertEquals(phoneMock, result.getPhone());
        assertEquals(LINE_1, result.getAddress().getAddressLine1());
        assertEquals(LINE_2, result.getAddress().getAddressLine2());
        assertEquals(TOWN, result.getAddress().getCity());
        assertEquals(UK_COUNTRY_CODE, result.getAddress().getCountry());
        assertEquals(UK_STATE, result.getAddress().getState());
        assertEquals(POST_CODE, result.getAddress().getZip());
    }

    @Test
    public void getCustomerRequest_ShouldCreateCustomerCorrectly() {
        final CustomerRequest result = testObj.getCustomerRequest(customerModelMock);

        assertEquals(CUSTOMER_EMAIL, result.getEmail());
        assertEquals(DISPLAY_CUSTOMER_NAME, result.getName());
    }

    @Test
    public void createThreeDSRequest_WhenDefaultBehavior_ShouldReturnEmpty() {
        assertTrue(testObj.createThreeDSRequest().isEmpty());
    }
}
