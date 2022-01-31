package com.checkout.hybris.occ.controllers;

import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.resolvers.CheckoutComPaymentTypeResolver;
import com.checkout.hybris.facades.address.CheckoutComAddressFacade;
import com.checkout.hybris.facades.payment.CheckoutComPaymentInfoFacade;
import com.checkout.hybris.occ.converters.CheckoutComPaymentDetailsDTOReverseConverter;
import com.checkout.hybris.occ.exceptions.NoCheckoutCartException;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.CartFacade;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.strategies.CheckoutCustomerStrategy;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import de.hybris.platform.commercewebservicescommons.dto.user.AddressWsDTO;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.CartAddressException;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.mapping.FieldSetLevelHelper;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.validation.Validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComCartsControllerTest {

    private static final String DEFAULT_FIELD_SET = FieldSetLevelHelper.DEFAULT_LEVEL;
    private static final String CUSTOMER_EMAIL = "customerEmail@email.com";
    private static final String CARD_TYPE = "CARD";
    private static final String ADDRESS_ID = "addressId";

    @InjectMocks
    private CheckoutComCartsController testObj;

    @Mock
    private CheckoutCustomerStrategy checkoutCustomerStrategyMock;
    @Mock
    private CheckoutComAddressFacade checkoutComAddressFacadeMock;
    @Mock
    private UserFacade userFacadeMock;
    @Mock
    private DataMapper dataMapperMock;
    @Mock
    private CheckoutComPaymentTypeResolver checkoutComPaymentTypeResolverMock;
    @Mock
    private CheckoutComPaymentDetailsDTOReverseConverter checkoutComPaymentDetailsDTOReverseConverterMock;
    @Mock
    private CheckoutComPaymentInfoFacade checkoutComPaymentInfoFacadeMock;
    @Mock
    private CheckoutFacade checkoutFacadeMock;
    @Mock
    private CartFacade cartFacadeMock;
    @Mock
    private Validator deliveryAddressValidator;
    @Mock
    private Validator addressDTOValidator;
    @Mock
    private Validator checkoutComPaymentDetailsWsDTOValidValidatorMock;

    @Mock
    private AddressData addressDataMock;
    @Mock
    private AddressWsDTO addressWsDTOMock;
    @Mock
    private CustomerModel customerModelMock;
    @Mock
    private PaymentDetailsWsDTO paymentDetailsWsDtoMock;
    @Mock
    private Object paymentInfoMock;
    @Mock
    private CartData cartDataMock;
    @Mock
    private CCPaymentInfoData paymentInfoDataMock;
    @Mock
    private PaymentDetailsWsDTO paymentDetailsWsDTOMock;

    @Captor
    private ArgumentCaptor<AddressData> addressDataArgumentCaptor;

    @Test
    public void createCartPaymentDetails_shouldCreatePaymentDetails() throws NoCheckoutCartException {
        when(checkoutFacadeMock.hasCheckoutCart()).thenReturn(Boolean.TRUE);
        when(paymentDetailsWsDtoMock.getType()).thenReturn(CARD_TYPE);
        when(checkoutComPaymentTypeResolverMock.resolvePaymentMethod(CARD_TYPE)).thenReturn(CheckoutComPaymentType.CARD);
        when(checkoutComPaymentDetailsDTOReverseConverterMock.convertPaymentDetailsWsDTO(paymentDetailsWsDtoMock, CheckoutComPaymentType.CARD)).thenReturn(paymentInfoMock);
        when(checkoutFacadeMock.getCheckoutCart()).thenReturn(cartDataMock);
        when(cartDataMock.getPaymentInfo()).thenReturn(paymentInfoDataMock);
        when(dataMapperMock.map(paymentInfoDataMock, PaymentDetailsWsDTO.class, StringUtils.EMPTY)).thenReturn(paymentDetailsWsDTOMock);

        final PaymentDetailsWsDTO result = testObj.createCartPaymentDetails(paymentDetailsWsDtoMock, StringUtils.EMPTY);

        assertThat(result).isEqualTo(paymentDetailsWsDTOMock);
        verify(checkoutComPaymentInfoFacadeMock).addPaymentInfoToCart(paymentInfoMock);
    }

    @Test(expected = NoCheckoutCartException.class)
    public void createCartPaymentDetails_WhenHasNoCheckoutCart_ShouldThrowNoCheckoutCartException() throws NoCheckoutCartException {
        when(checkoutFacadeMock.hasCheckoutCart()).thenReturn(Boolean.FALSE);

        testObj.createCartPaymentDetails(paymentDetailsWsDtoMock, StringUtils.EMPTY);
    }

    @Test
    public void createAPMCartPaymentDetails_shouldCreatePaymentDetails() throws NoCheckoutCartException {
        when(checkoutFacadeMock.hasCheckoutCart()).thenReturn(Boolean.TRUE);
        when(paymentDetailsWsDtoMock.getType()).thenReturn(CARD_TYPE);
        when(checkoutComPaymentTypeResolverMock.resolvePaymentMethod(CARD_TYPE)).thenReturn(CheckoutComPaymentType.CARD);
        when(checkoutComPaymentDetailsDTOReverseConverterMock.convertPaymentDetailsWsDTO(paymentDetailsWsDtoMock, CheckoutComPaymentType.CARD)).thenReturn(paymentInfoMock);

        testObj.createCartAPMPaymentDetails(paymentDetailsWsDtoMock);

        verify(checkoutComPaymentInfoFacadeMock).addPaymentInfoToCart(paymentInfoMock);
    }

    @Test(expected = NoCheckoutCartException.class)
    public void createAPMCartPaymentDetails_WhenHasNoCheckoutCart_ShouldThrowNoCheckoutCartException() throws NoCheckoutCartException {
        when(checkoutFacadeMock.hasCheckoutCart()).thenReturn(Boolean.FALSE);

        testObj.createCartAPMPaymentDetails(paymentDetailsWsDtoMock);
    }

    @Test
    public void addBillingAddressToCart_ShouldPopulateFieldsWithDataMapperAddingAddressWithUserFacadeAndSetBillingDetails() {
        when(dataMapperMock.map(addressWsDTOMock, AddressData.class)).thenReturn(addressDataMock);
        when(checkoutCustomerStrategyMock.getCurrentUserForCheckout()).thenReturn(customerModelMock);
        when(customerModelMock.getContactEmail()).thenReturn(CUSTOMER_EMAIL);

        testObj.addBillingAddressToCart(addressWsDTOMock);

        final InOrder inOrder = Mockito.inOrder(addressWsDTOMock, addressDataMock, dataMapperMock, userFacadeMock, checkoutComAddressFacadeMock);
        inOrder.verify(addressWsDTOMock).setEmail(CUSTOMER_EMAIL);
        inOrder.verify(addressWsDTOMock).setVisibleInAddressBook(Boolean.FALSE);
        inOrder.verify(dataMapperMock).map(addressWsDTOMock, AddressData.class);
        inOrder.verify(userFacadeMock).addAddress(addressDataMock);
        inOrder.verify(checkoutComAddressFacadeMock).setCartBillingDetails(addressDataMock);
    }

    @Test
    public void replaceCartDeliveryAndBillingAddress_ShouldReplaceCartDeliveryAndBillingAddress() {
        when(checkoutFacadeMock.setDeliveryAddress(addressDataArgumentCaptor.capture())).thenReturn(Boolean.TRUE);

        testObj.replaceCartDeliveryAndBillingAddress(ADDRESS_ID);

        verify(checkoutComAddressFacadeMock).setCartBillingDetailsByAddressId(ADDRESS_ID);

        final AddressData addressData = addressDataArgumentCaptor.getValue();

        assertThat(addressData.getId()).isEqualTo(ADDRESS_ID);
    }

    @Test(expected = CartAddressException.class)
    public void replaceCartDeliveryAndBillingAddress_WhenDeliveryAddressCanNotBeSet_ShouldThrowException() {
        when(checkoutFacadeMock.setDeliveryAddress(addressDataArgumentCaptor.capture())).thenReturn(Boolean.FALSE);

        testObj.replaceCartDeliveryAndBillingAddress(ADDRESS_ID);
    }

    @Test
    public void createCartDeliveryAndBillingAddress_WhenAddressIsDefaultAddress_ShouldCreateCartDeliveryAndBillingAddressAndSetDeliveryAddressAsDefault() {
        when(checkoutFacadeMock.setDeliveryAddress(addressDataArgumentCaptor.capture())).thenReturn(Boolean.TRUE);
        when(dataMapperMock.map(addressWsDTOMock, AddressData.class, DEFAULT_FIELD_SET)).thenReturn(addressDataMock);
        when(addressDataMock.isDefaultAddress()).thenReturn(Boolean.TRUE);
        when(dataMapperMock.map(addressDataMock, AddressWsDTO.class, DEFAULT_FIELD_SET)).thenReturn(addressWsDTOMock);

        final AddressWsDTO result = testObj.createCartDeliveryAndBillingAddress(addressWsDTOMock, DEFAULT_FIELD_SET);

        verify(addressDataMock).setShippingAddress(Boolean.TRUE);
        verify(addressDataMock).setVisibleInAddressBook(Boolean.TRUE);
        verify(userFacadeMock).addAddress(addressDataMock);
        verify(userFacadeMock).setDefaultAddress(addressDataMock);
        verify(checkoutComAddressFacadeMock).setCartBillingDetails(addressDataMock);

        assertThat(result).isEqualTo(addressWsDTOMock);
    }

    @Test
    public void createCartDeliveryAndBillingAddress_WhenAddressIsNotDefaultAddress_ShouldCreateCartDeliveryAndBillingAddress() {
        when(checkoutFacadeMock.setDeliveryAddress(addressDataArgumentCaptor.capture())).thenReturn(Boolean.TRUE);
        when(dataMapperMock.map(addressWsDTOMock, AddressData.class, DEFAULT_FIELD_SET)).thenReturn(addressDataMock);
        when(addressDataMock.isDefaultAddress()).thenReturn(Boolean.FALSE);
        when(dataMapperMock.map(addressDataMock, AddressWsDTO.class, DEFAULT_FIELD_SET)).thenReturn(addressWsDTOMock);

        final AddressWsDTO result = testObj.createCartDeliveryAndBillingAddress(addressWsDTOMock, DEFAULT_FIELD_SET);

        verify(addressDataMock).setShippingAddress(Boolean.TRUE);
        verify(addressDataMock).setVisibleInAddressBook(Boolean.TRUE);
        verify(userFacadeMock).addAddress(addressDataMock);
        verify(userFacadeMock, never()).setDefaultAddress(addressDataMock);
        verify(checkoutComAddressFacadeMock).setCartBillingDetails(addressDataMock);

        assertThat(result).isEqualTo(addressWsDTOMock);
    }
}
