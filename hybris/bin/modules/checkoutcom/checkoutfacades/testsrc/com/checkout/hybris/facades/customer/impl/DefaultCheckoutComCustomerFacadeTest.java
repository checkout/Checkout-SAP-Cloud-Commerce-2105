package com.checkout.hybris.facades.customer.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.storesession.StoreSessionFacade;
import de.hybris.platform.commercefacades.storesession.data.CurrencyData;
import de.hybris.platform.commercefacades.storesession.data.LanguageData;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.enums.CustomerType;
import de.hybris.platform.commerceservices.order.CommerceCartCalculationStrategy;
import de.hybris.platform.commerceservices.order.CommerceCartService;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComCustomerFacadeTest {

    private static final String GUEST_CHECKOUT_EMAIL = "guestGooglePayExpressUser@checkout.com";
    private static final String GUEST_APPLE_PAY_EXPRESS_CHECKOUT_EMAIL = "guestApplePayExpressUser@checkout.com";

    @Spy
    @InjectMocks
    private DefaultCheckoutComCustomerFacade testObj;

    @Mock
    private Converter<UserModel, CustomerData> customerConverterMock;
    @Mock
    private ModelService modelServiceMock;
    @Mock
    private CustomerAccountService customerAccountServiceMock;
    @Mock
    private StoreSessionFacade storeSessionFacadeMock;
    @Mock
    private UserFacade userFacadeMock;
    @Mock
    private CommerceCartService commerceCartServiceMock;
    @Mock
    private UserService userServiceMock;
    @Mock
    private CurrencyData currencyDataMock;
    @Mock
    private LanguageData languageDataMock;
    @Mock
    private CommonI18NService commonI18NServiceMock;
    @Mock
    private CommerceCartCalculationStrategy commerceCartCalculationStrategyMock;

    @Mock
    private CustomerModel customerModelMock;
    @Mock
    private CustomerData customerDataMock;
    @Mock
    private LanguageModel languageModelMock;
    @Mock
    private CurrencyModel currencyModelMock;
    @Mock
    private CartService cartServiceMock;
    @Mock
    private CartModel cartModelMock;
    @Mock
    private CommerceCartParameter commerceCartParameterMock;


    @Captor
    private ArgumentCaptor<CommerceCartParameter> commerceCartParameterArgumentCaptor;

    @Before
    public void setUp() {
        testObj = new DefaultCheckoutComCustomerFacade(commerceCartCalculationStrategyMock);

        testObj.setCartService(cartServiceMock);
        testObj.setModelService(modelServiceMock);
        testObj.setCustomerAccountService(customerAccountServiceMock);
        testObj.setStoreSessionFacade(storeSessionFacadeMock);
        testObj.setUserFacade(userFacadeMock);
        testObj.setCommerceCartService(commerceCartServiceMock);
        testObj.setUserService(userServiceMock);
        testObj.setCommonI18NService(commonI18NServiceMock);
        testObj.setCustomerConverter(customerConverterMock);


        when(customerModelMock.getName()).thenReturn(GUEST_CHECKOUT_EMAIL);
        when(customerModelMock.getUid()).thenReturn(GUEST_CHECKOUT_EMAIL);
        when(customerModelMock.getSessionLanguage()).thenReturn(languageModelMock);
        when(customerModelMock.getSessionCurrency()).thenReturn(currencyModelMock);
        when(modelServiceMock.create(CustomerModel.class)).thenReturn(customerModelMock);

        when(customerModelMock.getName()).thenReturn(GUEST_CHECKOUT_EMAIL);
        when(customerModelMock.getUid()).thenReturn(GUEST_CHECKOUT_EMAIL);
        when(customerModelMock.getType()).thenReturn(CustomerType.GUEST);
        when(customerModelMock.getSessionLanguage()).thenReturn(languageModelMock);
        when(customerModelMock.getSessionCurrency()).thenReturn(currencyModelMock);
        when(modelServiceMock.create(CustomerModel.class)).thenReturn(customerModelMock);

        when(currencyModelMock.getIsocode()).thenReturn("USD");
        when(currencyModelMock.getName()).thenReturn("USD");

        when(customerDataMock.getUid()).thenReturn(GUEST_CHECKOUT_EMAIL);
        when(customerDataMock.getName()).thenReturn(GUEST_CHECKOUT_EMAIL);
        when(customerDataMock.getFirstName()).thenReturn(GUEST_CHECKOUT_EMAIL);
        when(customerDataMock.getLastName()).thenReturn(GUEST_CHECKOUT_EMAIL);

        when(currencyDataMock.getIsocode()).thenReturn("USD");
        when(currencyDataMock.getName()).thenReturn("USD");
        when(customerDataMock.getCurrency()).thenReturn(currencyDataMock);
        when(storeSessionFacadeMock.getDefaultCurrency()).thenReturn(currencyDataMock);

        when(languageDataMock.getIsocode()).thenReturn("US");
        when(languageDataMock.getName()).thenReturn("United States");
        when(customerDataMock.getLanguage()).thenReturn(languageDataMock);
        when(storeSessionFacadeMock.getDefaultLanguage()).thenReturn(languageDataMock);

        when(customerConverterMock.convert(customerModelMock)).thenReturn(customerDataMock);

        when(commonI18NServiceMock.getCurrentLanguage()).thenReturn(languageModelMock);
        when(commonI18NServiceMock.getCurrentCurrency()).thenReturn(currencyModelMock);

        when(cartModelMock.getPaymentCost()).thenReturn(10.0d);
        when(cartModelMock.getTotalPrice()).thenReturn(150.0d);
        when(cartModelMock.getUser()).thenReturn(customerModelMock);

        when(cartServiceMock.getSessionCart()).thenReturn(cartModelMock);
        when(cartServiceMock.getSessionCart().getUser()).thenReturn(customerModelMock);
        when(cartServiceMock.hasSessionCart()).thenReturn(Boolean.TRUE);

    }

    @Test
    public void createExpressCheckoutGuestUserForAnonymousCheckout_ShouldReturnExpressCheckoutGuestUserCreated() throws DuplicateUidException {
        testObj.createGooglePayExpressCheckoutGuestUserForAnonymousCheckout();

        assertThat(customerModelMock.getName()).isEqualTo(GUEST_CHECKOUT_EMAIL);
    }

    @Test
    public void isGooglePayExpressGuestCustomer_WhenGoogleGuestCustomer_ShouldReturnTrue() {
        final boolean result = testObj.isGooglePayExpressGuestCustomer();

        assertThat(result).isTrue();
    }

    @Test
    public void isGooglePayExpressGuestCustomer_WhenRegisteredCustomerType_ShouldReturnFalse() {
        when(customerModelMock.getType()).thenReturn(CustomerType.REGISTERED);

        final boolean result = testObj.isGooglePayExpressGuestCustomer();

        assertThat(result).isFalse();
    }

    @Test
    public void isApplePayExpressGuestCustomer_WhenApplePayGuestCustomer_ShouldReturnTrue() {
        when(customerModelMock.getName()).thenReturn(GUEST_APPLE_PAY_EXPRESS_CHECKOUT_EMAIL);

        final boolean result = testObj.isApplePayExpressGuestCustomer();

        assertThat(result).isTrue();
    }

    @Test
    public void isApplePayExpressGuestCustomer_WhenRegisteredCustomer_ShouldReturnFalse() {
        when(customerModelMock.getName()).thenReturn(GUEST_APPLE_PAY_EXPRESS_CHECKOUT_EMAIL);
        when(customerModelMock.getType()).thenReturn(CustomerType.REGISTERED);

        final boolean result = testObj.isApplePayExpressGuestCustomer();

        assertThat(result).isFalse();
    }

    @Test
    public void createApplePayExpressCheckoutGuestUserForAnonymousCheckout() throws DuplicateUidException {
        testObj.createApplePayExpressCheckoutGuestUserForAnonymousCheckout();

        assertThat(cartServiceMock.getSessionCart()).isNotNull();
        assertThat(cartServiceMock.getSessionCart().getUser()).isNotNull();
        assertThat(cartServiceMock.getSessionCart().getUser().getName()).isEqualTo(GUEST_CHECKOUT_EMAIL);
    }

    @Test
    public void createGuestUserForAnonymousCheckout_ShouldReturnExpressCheckoutGuestUserCreated() throws DuplicateUidException {
        testObj.createGuestUserForAnonymousCheckout(GUEST_CHECKOUT_EMAIL, GUEST_CHECKOUT_EMAIL);

        assertThat(cartServiceMock.getSessionCart()).isNotNull();
        assertThat(cartServiceMock.getSessionCart().getUser()).isNotNull();
        assertThat(cartServiceMock.getSessionCart().getUser().getName()).isEqualTo(GUEST_CHECKOUT_EMAIL);
    }

    @Test
    public void updateCartWithGuestForAnonymousCheckout_ShouldReturnCartUpdated() {
        testObj.updateCartWithGuestForAnonymousCheckout(customerDataMock);

        assertThat(cartServiceMock.getSessionCart()).isNotNull();
        assertThat(cartModelMock.getUser()).isNotNull();
        assertThat(cartModelMock.getUser().getName()).isEqualTo(GUEST_CHECKOUT_EMAIL);
    }

    @Test
    public void updateExpressCheckoutUserEmail_ShouldUpdateUserEmail() {

        testObj.updateExpressCheckoutUserEmail("email", GUEST_CHECKOUT_EMAIL);
        verify(customerModelMock).setUid(anyString());
        verify(customerModelMock).setName(anyString());
        verify(modelServiceMock).save(customerModelMock);
    }

    @Test
    public void recalculateExpressCheckoutCart_ShouldRecalculateCart() {
        when(commerceCartParameterMock.getCart()).thenReturn(cartModelMock);

        testObj.recalculateExpressCheckoutCart();
        verify(commerceCartCalculationStrategyMock).calculateCart(commerceCartParameterArgumentCaptor.capture());

        final CommerceCartParameter commerceCartParameter = commerceCartParameterArgumentCaptor.getValue();
        assertThat(commerceCartParameter.isEnableHooks()).isTrue();
        assertThat(commerceCartParameter.getCart()).isEqualTo(cartModelMock);

    }
}
