package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.facades.beans.ApplePayShippingMethod;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComDeliveryModeDataToApplePayShippingMethodPopulatorTest {

    private static final String DELIVERY_MODE_CODE = "deliveryModeCode";
    private static final String DELIVERY_MODE_DESCRIPTION = "deliveryModeDescription";
    private static final String DELIVERY_MODE_NAME = "deliveryModeName";

    @InjectMocks
    private CheckoutComDeliveryModeDataToApplePayShippingMethodPopulator testObj;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DeliveryModeData deliveryModeDataMock;

    private ApplePayShippingMethod applePayShippingMethod;


    @Before
    public void setUp() {
        when(deliveryModeDataMock.getCode()).thenReturn(DELIVERY_MODE_CODE);
        when(deliveryModeDataMock.getDeliveryCost().getValue()).thenReturn(BigDecimal.TEN);
        when(deliveryModeDataMock.getDescription()).thenReturn(DELIVERY_MODE_DESCRIPTION);
        when(deliveryModeDataMock.getName()).thenReturn(DELIVERY_MODE_NAME);
    }

    @Test
    public void populate_shouldFillAmountLabelAndDetail_whenDeliveryModeContainsName() {
        applePayShippingMethod = new ApplePayShippingMethod();

        testObj.populate(deliveryModeDataMock, applePayShippingMethod);

        assertThat(applePayShippingMethod.getAmount()).isEqualTo("10");
        assertThat(applePayShippingMethod.getDetail()).isEqualTo(DELIVERY_MODE_DESCRIPTION);
        assertThat(applePayShippingMethod.getLabel()).isEqualTo(DELIVERY_MODE_NAME);
        assertThat(applePayShippingMethod.getIdentifier()).isEqualTo(DELIVERY_MODE_CODE);
    }

    @Test
    public void populate_shouldFillLabelWithCode_whenDeliveryModeNotContainsName() {
        applePayShippingMethod = new ApplePayShippingMethod();
        when(deliveryModeDataMock.getName()).thenReturn(null);

        testObj.populate(deliveryModeDataMock, applePayShippingMethod);

        assertThat(applePayShippingMethod.getAmount()).isEqualTo("10");
        assertThat(applePayShippingMethod.getDetail()).isEqualTo(DELIVERY_MODE_DESCRIPTION);
        assertThat(applePayShippingMethod.getLabel()).isEqualTo(DELIVERY_MODE_CODE);
        assertThat(applePayShippingMethod.getIdentifier()).isEqualTo(DELIVERY_MODE_CODE);
    }
}
