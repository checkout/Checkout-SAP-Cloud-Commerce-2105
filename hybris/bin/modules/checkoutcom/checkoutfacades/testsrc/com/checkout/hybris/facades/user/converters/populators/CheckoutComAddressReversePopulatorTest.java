package com.checkout.hybris.facades.user.converters.populators;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.user.AddressModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComAddressReversePopulatorTest {

    private static final String CONTACT_EMAIL_VALUE = "CONTACT_EMAIL";

    @Spy
    @InjectMocks
    private CheckoutComAddressReversePopulator testObj;

    @Mock
    private AddressData sourceMock;
    @Mock
    private AddressModel targetMock;

    @Test
    public void populate_WhenEmailIsPopulatedInData_ShouldBePopulatedInTheModel() {
        doNothing().when(testObj).callSuperPopulate(sourceMock, targetMock);
        when(sourceMock.getEmail()).thenReturn(CONTACT_EMAIL_VALUE);

        testObj.populate(sourceMock, targetMock);

        verify(targetMock).setEmail(CONTACT_EMAIL_VALUE);
    }
}
