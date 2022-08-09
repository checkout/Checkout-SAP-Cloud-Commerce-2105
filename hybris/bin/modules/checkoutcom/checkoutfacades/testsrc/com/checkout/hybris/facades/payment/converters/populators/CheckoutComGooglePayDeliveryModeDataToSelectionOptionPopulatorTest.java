package com.checkout.hybris.facades.payment.converters.populators;

import com.checkout.hybris.facades.beans.GooglePaySelectionOption;
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
public class CheckoutComGooglePayDeliveryModeDataToSelectionOptionPopulatorTest {

    private static final String DELIVERY_MODE_CODE = "deliveryModeCode";
    private static final String DELIVERY_MODE_DESCRIPTION = "deliveryModeDescription";
    private static final String DELIVERY_MODE_NAME = "deliveryModeName";

    @InjectMocks
    private CheckoutComGooglePayDeliveryModeDataToSelectionOptionPopulator testObj;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DeliveryModeData deliveryModeData;

    private GooglePaySelectionOption googlePaySelectionOption;

    @Before
    public void setUp() {
        when(deliveryModeData.getCode()).thenReturn(DELIVERY_MODE_CODE);
        when(deliveryModeData.getDeliveryCost().getValue()).thenReturn(BigDecimal.TEN);
        when(deliveryModeData.getDescription()).thenReturn(DELIVERY_MODE_DESCRIPTION);
        when(deliveryModeData.getName()).thenReturn(DELIVERY_MODE_NAME);
    }

    @Test
    public void populate_ShouldFillGooglePaySelectionOption() {
        googlePaySelectionOption = new GooglePaySelectionOption();

        testObj.populate(deliveryModeData, googlePaySelectionOption);
        assertThat(googlePaySelectionOption.getId()).isEqualTo(DELIVERY_MODE_CODE);
        assertThat(googlePaySelectionOption.getLabel()).isEqualTo(DELIVERY_MODE_NAME);
        assertThat(googlePaySelectionOption.getDescription()).isEqualTo(DELIVERY_MODE_DESCRIPTION);
    }
}
