package com.checkout.hybris.core.payment.request.strategies.impl;

import com.checkout.payments.PaymentRequest;
import com.checkout.payments.RequestSource;
import com.checkout.payments.ThreeDSRequest;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.cms2.model.site.CMSSiteModel;
import de.hybris.platform.cms2.servicelayer.services.CMSSiteService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Map;
import java.util.Optional;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.MADA;
import static com.checkout.hybris.core.payment.request.strategies.impl.CheckoutComAbstractPaymentRequestStrategy.UDF1_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComMadaPaymentRequestStrategyTest {

    private static final String SITE_ID = "SITE_ID";

    @Spy
    @InjectMocks
    private CheckoutComMadaPaymentRequestStrategy testObj;

    @Mock
    private CMSSiteService cmsSiteServiceMock;
    @Mock
    private CMSSiteModel currentSiteMock;
    @Mock
    private PaymentRequest<RequestSource> paymentRequestMock;
    @Captor
    private ArgumentCaptor<Map<String, Object>> metadataMapCapture;

    @Before
    public void setUp() {
        when(cmsSiteServiceMock.getCurrentSite()).thenReturn(currentSiteMock);
        when(currentSiteMock.getUid()).thenReturn(SITE_ID);
    }

    @Test
    public void isCapture_WhenMada_ThenReturnNull() {
        assertTrue(testObj.isCapture().isEmpty());
    }

    @Test
    public void populateRequestMetadata_WhenMada_ThenUdf1ValueIsPresent() {
        testObj.populateRequestMetadata(paymentRequestMock);

        verify(paymentRequestMock).setMetadata(metadataMapCapture.capture());

        final Map<String, Object> mapCaptureValue = metadataMapCapture.getValue();
        assertTrue(mapCaptureValue.containsKey(UDF1_KEY));
        assertEquals("mada", mapCaptureValue.get(UDF1_KEY));
    }

    @Test
    public void createThreeDSRequest_WhenMada_ShouldCreateThreeDSWithTrueValue() {
        final Optional<ThreeDSRequest> result = testObj.createThreeDSRequest();

        assertTrue(result.isPresent());
        assertTrue(result.get().isEnabled());
    }

    @Test
    public void getStrategyKey_WhenMada_ShouldReturnMadaType() {
        assertEquals(MADA, testObj.getStrategyKey());
    }
}
