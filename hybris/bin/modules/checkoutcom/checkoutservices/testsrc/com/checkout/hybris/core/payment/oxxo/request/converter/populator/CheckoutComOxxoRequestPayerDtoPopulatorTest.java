package com.checkout.hybris.core.payment.oxxo.request.converter.populator;

import com.checkout.hybris.core.model.CheckoutComOxxoPaymentInfoModel;
import com.checkout.hybris.core.oxxo.session.request.OxxoPayerRequestDto;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComOxxoRequestPayerDtoPopulatorTest {

    private static final String EMAIL = "name@email.com";
    private static final String NAME = "name";
    private static final String DOCUMENT = "document";

    @InjectMocks
    private CheckoutComOxxoRequestPayerDtoPopulator testObj;

    @Mock
    private CartModel sourceMock;
    @Mock
    private OxxoPayerRequestDto targetMock;
    @Mock
    private CustomerModel customerModelMock;
    @Mock
    private CheckoutComOxxoPaymentInfoModel checkoutComOxxoPaymentInfoModelMock;
    @Mock
    private UserModel userModelMock;
    @Mock
    private PaymentInfoModel paymentInfoModelMock;

    @Before
    public void setUp() {
        when(sourceMock.getUser()).thenReturn(customerModelMock);
        when(customerModelMock.getContactEmail()).thenReturn(EMAIL);
        when(customerModelMock.getName()).thenReturn(NAME);
        when(sourceMock.getPaymentInfo()).thenReturn(checkoutComOxxoPaymentInfoModelMock);
        when(checkoutComOxxoPaymentInfoModelMock.getDocument()).thenReturn(DOCUMENT);
    }

    @Test
    public void populate_ShouldPopulateRequestPayerDto() {
        testObj.populate(sourceMock, targetMock);

        verify(targetMock).setName(NAME);
        verify(targetMock).setDocument(DOCUMENT);
        verify(targetMock).setEmail(EMAIL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_ShouldThrowException_WhenSourceIsNull() {
        testObj.populate(null, targetMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_ShouldThrowException_WhenTargetIsNull() {
        testObj.populate(sourceMock, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_ShouldThrowException_WhenSourceUserIsNull() {
        when(sourceMock.getUser()).thenReturn(null);

        testObj.populate(sourceMock, targetMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_ShouldThrowException_WhenSourceUserIsNotACustomer() {
        when(sourceMock.getUser()).thenReturn(userModelMock);

        testObj.populate(sourceMock, targetMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_ShouldThrowException_WhenSourcePaymentInfoIsNotACheckoutComOxxoPaymentInfoModel() {
        when(sourceMock.getPaymentInfo()).thenReturn(paymentInfoModelMock);

        testObj.populate(sourceMock, targetMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_ShouldThrowException_WhenSourcePaymentInfoIsNull() {
        when(sourceMock.getPaymentInfo()).thenReturn(null);

        testObj.populate(sourceMock, targetMock);
    }
}
