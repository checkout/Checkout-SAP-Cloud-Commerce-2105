package com.checkout.hybris.core.address.services.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComAddressServiceTest {

    @InjectMocks
    private DefaultCheckoutComAddressService testObj;

    @Mock
    private ModelService modelServiceMock;
    @Mock
    private CartModel cartModelMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AddressModel addressModelMock;

    @Test
    public void setCartPaymentAddress_ShouldUpdateTheCartCorrectly() {

        testObj.setCartPaymentAddress(cartModelMock, addressModelMock);

        verify(cartModelMock).setPaymentAddress(addressModelMock);
        verify(modelServiceMock).save(cartModelMock);
    }

    @Test
    public void getCustomerFullNameFromAddress_ShouldReturnFullNameWithTitle() {
        when(addressModelMock.getTitle().getName()).thenReturn("Mr.");
        when(addressModelMock.getFirstname()).thenReturn("FirstName");
        when(addressModelMock.getLastname()).thenReturn("LastName");

        final String result = testObj.getCustomerFullNameFromAddress(addressModelMock);

        assertEquals("Mr. FirstName LastName", result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void getCustomerFullNameFromAddress__WhenAddressIsNull_ShouldThrowException() {
        testObj.getCustomerFullNameFromAddress(null);
    }

    @Test
    public void getCustomerFullNameFromAddress_WhenTitleIsMissing_ShouldReturnFullNameWithOutTitle() {
        when(addressModelMock.getFirstname()).thenReturn("FirstName");
        when(addressModelMock.getLastname()).thenReturn("LastName");
        when(addressModelMock.getTitle()).thenReturn(null);

        final String result = testObj.getCustomerFullNameFromAddress(addressModelMock);

        assertEquals("FirstName LastName", result);
    }
}
