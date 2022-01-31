package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.core.model.CheckoutComOxxoPaymentInfoModel;
import com.checkout.hybris.facades.beans.OxxoPaymentInfoData;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComOxxoPaymentInfoReversePopulatorTest {

    private static final String DOCUMENT_ID = "DocumentID";

    @InjectMocks
    private CheckoutComOxxoPaymentInfoReversePopulator testObj;

    @Mock
    private CheckoutComOxxoPaymentInfoModel targetMock;
    @Mock
    private OxxoPaymentInfoData sourceMock;

    @Test
    public void populate_ShouldPopulateTheDocument_WhenSourceAndTargetAreNotNull() {
        when(sourceMock.getDocument()).thenReturn(DOCUMENT_ID);

        testObj.populate(sourceMock, targetMock);

        verify(targetMock).setDocument(DOCUMENT_ID);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_ShouldThrowException_WhenSourceIsNull() {
        testObj.populate(null, targetMock);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_ShouldThrowException_WhenTargetIsNull() {
        testObj.populate(sourceMock, null);
    }
}
