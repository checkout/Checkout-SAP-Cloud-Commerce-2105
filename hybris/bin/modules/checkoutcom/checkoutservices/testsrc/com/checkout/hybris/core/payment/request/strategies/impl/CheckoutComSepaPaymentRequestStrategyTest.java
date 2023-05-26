package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.common.Address;
import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.merchantconfiguration.BillingDescriptor;
import com.checkout.hybris.core.model.CheckoutComSepaPaymentInfoModel;
import com.checkout.hybris.core.payment.exception.CheckoutComPaymentIntegrationException;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentIntegrationService;
import com.checkout.payments.AlternativePaymentSource;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.RequestSource;
import com.checkout.sources.*;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static com.checkout.hybris.core.enums.SepaPaymentType.RECURRING;
import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.SEPA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComSepaPaymentRequestStrategyTest {

    protected static final String PAYMENT_SOURCE_ID_KEY = "id";
    private static final String CURRENCY_ISO_CODE = "BRL";
    private static final Long CHECKOUT_COM_TOTAL_PRICE = 10000L;
    private static final String CART_REFERENCE = "CART_REFERENCE";
    private static final String BILLING_DESCRIPTOR_NAME = "billingDescriptorName";
    private static final String PAYMENT_ID = "PAYMENT_ID";
    private static final String CUSTOMER_ID = "customerId";
    private static final String GBP = "GBP";
    private static final double TOTAL_PRICE = 100D;
    private static final String FIRST_NAME_KEY = "first_name";
    private static final String LAST_NAME_KEY = "last_name";
    private static final String BILLING_DESCRIPTOR__KEY = "billing_descriptor";
    private static final String ACCOUNT_IBAN_KEY = "account_iban";
    private static final String MANDATE_TYPE_KEY = "mandate_type";

    @Spy
    @InjectMocks
    private CheckoutComSepaPaymentRequestStrategy testObj;

    @Mock
    private CheckoutComPaymentIntegrationService checkoutComPaymentIntegrationServiceMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CartModel cartMock;
    @Mock
    private SourceResponse sourceResponseMock;
    @Mock
    private CheckoutComSepaPaymentInfoModel sepaPaymentInfoModelMock;
    @Mock
    private CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationServiceMock;
    @Mock
    private BillingDescriptor billingDescriptorMock;
    @Mock
    private SourceProcessed sourceProcessedMock;
    @Mock
    private CurrencyModel currencyModelMock;
    @Mock
    private CheckoutComCurrencyService checkoutComCurrencyServiceMock;
    @Mock
    private CustomerResponse customerResponseMock;
    @Mock
    private CustomerResponse customerSourceResponseMock;
    @Mock
    private Address addressMock;
    @Mock
    private SourceData sourceDataMock;
    @Mock
    private PaymentInfoModel paymentInfoMock;

    @Before
    public void setUp() {
        doNothing().when(testObj).populatePaymentRequest(anyObject(), anyObject());
        when(cartMock.getCurrency().getIsocode()).thenReturn(CURRENCY_ISO_CODE);
        when(cartMock.getPaymentInfo()).thenReturn(sepaPaymentInfoModelMock);
        when(cartMock.getCheckoutComPaymentReference()).thenReturn(CART_REFERENCE);
        when(cartMock.getCurrency()).thenReturn(currencyModelMock);
        when(currencyModelMock.getIsocode()).thenReturn(GBP);
        when(checkoutComMerchantConfigurationServiceMock.getBillingDescriptor()).thenReturn(billingDescriptorMock);
        when(billingDescriptorMock.getBillingDescriptorName()).thenReturn(BILLING_DESCRIPTOR_NAME);
        when(checkoutComPaymentIntegrationServiceMock.setUpPaymentSource(any(SourceRequest.class))).thenReturn(sourceResponseMock);
        when(sourceResponseMock.getSource()).thenReturn(sourceProcessedMock);
        when(sourceProcessedMock.getId()).thenReturn(PAYMENT_ID);
        when(checkoutComCurrencyServiceMock.convertAmountIntoPennies(GBP, TOTAL_PRICE)).thenReturn(CHECKOUT_COM_TOTAL_PRICE);
        when(sourceProcessedMock.getCustomer()).thenReturn(customerResponseMock);
        when(customerResponseMock.getId()).thenReturn(CUSTOMER_ID);
        when(sourceProcessedMock.getCustomer()).thenReturn(customerSourceResponseMock);
        when(customerSourceResponseMock.getId()).thenReturn(CUSTOMER_ID);

        setUpPaymentInfo();
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPaymentRequest_WhenCartIsNull_ShouldThrowException() {
        testObj.createPaymentRequest(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getRequestSourcePaymentRequest_WhenPaymentInfoIsNotSepa_ShouldThrowException() {
        when(cartMock.getPaymentInfo()).thenReturn(paymentInfoMock);

        testObj.getRequestSourcePaymentRequest(cartMock, CURRENCY_ISO_CODE, CHECKOUT_COM_TOTAL_PRICE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createPaymentRequest_WhenThereIsAnErrorInSetupPaymentSource_ShouldThrowException() {
        when(checkoutComPaymentIntegrationServiceMock.setUpPaymentSource(any(SourceRequest.class))).thenThrow(new CheckoutComPaymentIntegrationException("Exception"));

        testObj.createPaymentRequest(cartMock);
    }

    @Test
    public void createPaymentRequest_WhenEverythingIsCorrect_ShouldReturnThePopulatedRequest() {
        final PaymentRequest<RequestSource> result = testObj.createPaymentRequest(cartMock);

        assertEquals(PAYMENT_SOURCE_ID_KEY, result.getSource().getType());
        assertEquals(PAYMENT_ID, ((AlternativePaymentSource) result.getSource()).get(PAYMENT_SOURCE_ID_KEY));
        assertEquals(CUSTOMER_ID, result.getCustomer().getId());
    }

    @Test
    public void createPaymentRequest_WhenEverythingIsCorrectButCustomerNotPopulated_ShouldReturnThePopulatedRequestWithoutCustomer() {
        when(sourceProcessedMock.getCustomer()).thenReturn(null);

        final PaymentRequest<RequestSource> result = testObj.createPaymentRequest(cartMock);

        assertEquals(PAYMENT_SOURCE_ID_KEY, result.getSource().getType());
        assertEquals(PAYMENT_ID, ((AlternativePaymentSource) result.getSource()).get(PAYMENT_SOURCE_ID_KEY));
        assertNull(result.getCustomer());
    }


    @Test
    public void createSourceRequest_WhenSepa_ShouldCreateCorrectSourceRequest() {
        doReturn(addressMock).when(testObj).createAddress(sepaPaymentInfoModelMock);
        doReturn(sourceDataMock).when(testObj).createSourceData(sepaPaymentInfoModelMock);

        final SourceRequest result = testObj.createSourceRequest(cartMock, sepaPaymentInfoModelMock);

        assertEquals(addressMock, result.getBillingAddress());
        assertEquals(CART_REFERENCE, result.getReference());
        assertEquals(SEPA.name(), result.getType());
        assertEquals(sourceDataMock, result.getSourceData());
    }

    @Test
    public void createAddress_WhenSepa_ShouldCreateCorrectAddress() {
        final Address result = testObj.createAddress(sepaPaymentInfoModelMock);

        assertEquals("line1", result.getAddressLine1());
        assertEquals("city", result.getCity());
        assertEquals("country", result.getCountry());
        assertEquals("20020", result.getZip());
    }

    @Test
    public void createSourceData_WHenSepa_ShouldCreateCorrectSourceData() {
        final SourceData result = testObj.createSourceData(sepaPaymentInfoModelMock);

        assertEquals("first name", result.get(FIRST_NAME_KEY));
        assertEquals("last name", result.get(LAST_NAME_KEY));
        assertEquals("iban code", result.get(ACCOUNT_IBAN_KEY));
        assertEquals("recurring", result.get(MANDATE_TYPE_KEY));
        assertEquals(BILLING_DESCRIPTOR_NAME, result.get(BILLING_DESCRIPTOR__KEY));
    }

    @Test
    public void getStrategyKey_WhenSepa_ShouldReturnSepaType() {
        assertEquals(SEPA, testObj.getStrategyKey());
    }

    private void setUpPaymentInfo() {
        when(sepaPaymentInfoModelMock.getAccountIban()).thenReturn("iban code");
        when(sepaPaymentInfoModelMock.getPaymentType()).thenReturn(RECURRING);
        when(sepaPaymentInfoModelMock.getAddressLine1()).thenReturn("line1");
        when(sepaPaymentInfoModelMock.getCity()).thenReturn("city");
        when(sepaPaymentInfoModelMock.getPostalCode()).thenReturn("20020");
        when(sepaPaymentInfoModelMock.getCountry()).thenReturn("country");
        when(sepaPaymentInfoModelMock.getType()).thenReturn(PAYMENT_SOURCE_ID_KEY);
        when(sepaPaymentInfoModelMock.getFirstName()).thenReturn("first name");
        when(sepaPaymentInfoModelMock.getLastName()).thenReturn("last name");
    }
}
