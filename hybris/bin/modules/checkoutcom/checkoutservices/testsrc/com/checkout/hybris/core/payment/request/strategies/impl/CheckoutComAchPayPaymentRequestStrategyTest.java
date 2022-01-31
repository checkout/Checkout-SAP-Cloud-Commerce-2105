package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.common.Phone;
import com.checkout.hybris.core.address.strategies.CheckoutComPhoneNumberStrategy;
import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.enums.AchAccountType;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.merchantconfiguration.BillingDescriptor;
import com.checkout.hybris.core.model.CheckoutComAchPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComSepaPaymentInfoModel;
import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentIntegrationService;
import com.checkout.payments.AlternativePaymentSource;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.RequestSource;
import com.checkout.sources.CustomerResponse;
import com.checkout.sources.SourceProcessed;
import com.checkout.sources.SourceRequest;
import com.checkout.sources.SourceResponse;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.ACH;
import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.SEPA;
import static com.checkout.hybris.core.payment.request.strategies.impl.CheckoutComAchPayPaymentRequestStrategy.PAYMENT_SOURCE_ID_KEY;
import static java.util.Optional.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComAchPayPaymentRequestStrategyTest {

    private static final String CURRENCY_ISO_CODE = "BRL";
    private static final Long CHECKOUT_COM_TOTAL_PRICE = 10000L;
    private static final String CART_REFERENCE = "CART_REFERENCE";
    private static final String TOWN = "Town";
    private static final String LINE_1 = "Line 1";
    private static final String LINE_2 = "LINE_2";
    private static final String UK_COUNTRY_CODE = "UK";
    private static final String UK_STATE = "United kingdom";
    private static final String POST_CODE = "POST_CODE";
    private static final String NUMBER = "213423423";
    private static final String ACCOUNT_NAME_VALUE = "Account Name";
    private static final String CORP_SAVINGS_ACCOUNT_TYPE_VALUE = "CorpSavings";
    private static final String ACCOUNT_NUMBER_VALUE = "098765432";
    private static final String ROUTING_NUMBER_VALUE = "098765";
    private static final String COMPANY_NAME_VALUE = "Company Name";
    private static final String BILLING_DESCRIPTOR_NAME = "billingDescriptorName";
    private static final String PAYMENT_ID = "PAYMENT_ID";
    private static final String CUSTOMER_ID = "customerId";
    private static final String GBP = "GBP";
    private static final double TOTAL_PRICE = 100D;

    @Spy
    @InjectMocks
    private CheckoutComAchPayPaymentRequestStrategy testObj;

    @Mock
    private CheckoutComPaymentIntegrationService checkoutComPaymentIntegrationServiceMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CartModel cartMock;
    @Mock
    private SourceResponse sourceResponseMock;
    @Mock
    private CheckoutComSepaPaymentInfoModel sepaPaymentInfoMock;
    @Mock
    private CheckoutComAchPaymentInfoModel achPaymentInfoModelMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AddressModel addressMock;
    @Mock
    private CheckoutComPhoneNumberStrategy checkoutComPhoneNumberStrategyMock;
    @Mock
    private CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationServiceMock;
    @Mock
    private Phone phoneMock;
    @Mock
    private BillingDescriptor billingDescriptorMock;
    @Mock
    private SourceProcessed sourceProcessedMock;
    @Mock
    private CustomerResponse customerResponseMock;
    @Mock
    private CurrencyModel currencyModelMock;
    @Mock
    private CheckoutComCurrencyService checkoutComCurrencyServiceMock;
    @Mock
    private CustomerResponse customerSourceResponseMock;

    @Before
    public void setUp() {
        doNothing().when(testObj).populatePaymentRequest(anyObject(), anyObject());
        when(cartMock.getCurrency().getIsocode()).thenReturn(CURRENCY_ISO_CODE);
        when(cartMock.getPaymentInfo()).thenReturn(achPaymentInfoModelMock);
        when(cartMock.getCheckoutComPaymentReference()).thenReturn(CART_REFERENCE);
        when(cartMock.getCurrency()).thenReturn(currencyModelMock);
        when(currencyModelMock.getIsocode()).thenReturn(GBP);
        when(sepaPaymentInfoMock.getType()).thenReturn(SEPA.name());
        when(checkoutComMerchantConfigurationServiceMock.getBillingDescriptor()).thenReturn(billingDescriptorMock);
        when(billingDescriptorMock.getBillingDescriptorName()).thenReturn(BILLING_DESCRIPTOR_NAME);
        when(checkoutComPaymentIntegrationServiceMock.setUpPaymentSource(any(SourceRequest.class))).thenReturn(sourceResponseMock);
        when(sourceResponseMock.getSource()).thenReturn(sourceProcessedMock);
        when(sourceProcessedMock.getCustomer()).thenReturn(customerResponseMock);
        when(customerResponseMock.getId()).thenReturn(CUSTOMER_ID);
        when(sourceProcessedMock.getId()).thenReturn(PAYMENT_ID);
        when(sourceProcessedMock.getCustomer()).thenReturn(customerSourceResponseMock);
        when(customerSourceResponseMock.getId()).thenReturn(CUSTOMER_ID);
        when(checkoutComPhoneNumberStrategyMock.createPhone(addressMock)).thenReturn(of(phoneMock));
        when(checkoutComCurrencyServiceMock.convertAmountIntoPennies(GBP, TOTAL_PRICE)).thenReturn(CHECKOUT_COM_TOTAL_PRICE);
        setUpAddress();
        setUpPaymentInfo();
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPaymentRequest_WhenCartIsNull_ShouldThrowException() {
        testObj.createPaymentRequest(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPaymentRequest_WhenPaymentInfoIsNotAch_ShouldThrowException() {
        when(cartMock.getPaymentInfo()).thenReturn(sepaPaymentInfoMock);

        testObj.createPaymentRequest(cartMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPaymentRequest_WhenBillingAddressIsNull_ShouldThrowException() {
        when(achPaymentInfoModelMock.getBillingAddress()).thenReturn(null);

        testObj.createPaymentRequest(cartMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPaymentRequest_WhenThereIsAnErrorInSetupPaymentSource_ShouldThrowException() {
        when(checkoutComPaymentIntegrationServiceMock.setUpPaymentSource(any(SourceRequest.class))).thenThrow(new CheckoutComPaymentIntegrationException("Exception"));

        testObj.createPaymentRequest(cartMock);
    }

    @Test
    public void createPaymentRequest_WhenEverythingIsCorrectButCustomerNotPopulated_ShouldReturnThePopulatedRequestWithoutCustomer() {
        when(sourceProcessedMock.getCustomer()).thenReturn(null);

        PaymentRequest<RequestSource> result = testObj.createPaymentRequest(cartMock);

        assertEquals(PAYMENT_SOURCE_ID_KEY, result.getSource().getType());
        assertEquals(PAYMENT_ID, ((AlternativePaymentSource) result.getSource()).get(PAYMENT_SOURCE_ID_KEY));
        assertNull(result.getCustomer());
    }

    @Test
    public void createPaymentRequest_WhenEverythingIsCorrect_ShouldReturnThePopulatedRequest() {
        PaymentRequest<RequestSource> result = testObj.createPaymentRequest(cartMock);

        assertEquals(PAYMENT_SOURCE_ID_KEY, result.getSource().getType());
        assertEquals(PAYMENT_ID, ((AlternativePaymentSource) result.getSource()).get(PAYMENT_SOURCE_ID_KEY));
        assertEquals(CUSTOMER_ID, result.getCustomer().getId());
    }

    @Test
    public void getStrategyKey_WhenAch_ShouldReturnAchType() {
        assertEquals(ACH, testObj.getStrategyKey());
    }

    private void setUpPaymentInfo() {
        when(achPaymentInfoModelMock.getBillingAddress()).thenReturn(addressMock);
        when(achPaymentInfoModelMock.getAccountHolderName()).thenReturn(ACCOUNT_NAME_VALUE);
        when(achPaymentInfoModelMock.getAccountNumber()).thenReturn(ACCOUNT_NUMBER_VALUE);
        when(achPaymentInfoModelMock.getAccountType()).thenReturn(AchAccountType.valueOf(CORP_SAVINGS_ACCOUNT_TYPE_VALUE));
        when(achPaymentInfoModelMock.getRoutingNumber()).thenReturn(ROUTING_NUMBER_VALUE);
        when(achPaymentInfoModelMock.getCompanyName()).thenReturn(COMPANY_NAME_VALUE);
        when(achPaymentInfoModelMock.getType()).thenReturn(PAYMENT_SOURCE_ID_KEY);
    }

    private void setUpAddress() {
        when(addressMock.getLine1()).thenReturn(LINE_1);
        when(addressMock.getLine2()).thenReturn(LINE_2);
        when(addressMock.getCountry().getIsocode()).thenReturn(UK_COUNTRY_CODE);
        when(addressMock.getRegion().getName()).thenReturn(UK_STATE);
        when(addressMock.getTown()).thenReturn(TOWN);
        when(addressMock.getPostalcode()).thenReturn(POST_CODE);
        when(phoneMock.getNumber()).thenReturn(NUMBER);
    }
}
