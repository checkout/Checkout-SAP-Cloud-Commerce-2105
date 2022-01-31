package com.checkout.hybris.facades.payment.attributes.strategies.impl;

import com.checkout.hybris.core.enums.SepaPaymentType;
import com.google.common.collect.ImmutableList;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.enumeration.EnumerationMetaTypeModel;
import de.hybris.platform.core.model.enumeration.EnumerationValueModel;
import de.hybris.platform.servicelayer.type.TypeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.Model;

import java.util.HashMap;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.SEPA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComSepaPaymentAttributeStrategyTest {

    private static final String RECURRING_PAYMENT_KEY = "recurring";
    private static final String SINGLE_PAYMENT_KEY = "single";
    public static final String RECURRING_PAYMENT_VALUE = "Recurring payment";
    public static final String SINGLE_PAYMENT_VALUE = "One-off payment";
    protected static final String SEPA_PAYMENT_TYPES_MODEL_ATTRIBUTE = "sepaPaymentTypes";

    @Spy
    @InjectMocks
    private CheckoutComSepaPaymentAttributeStrategy testObj;

    @Mock
    private TypeService typeServiceMock;
    @Mock
    private EnumerationMetaTypeModel enumerationMetaTypeMock;
    @Mock
    private EnumerationValueModel enumerationValue1Mock, enumerationValue2Mock;
    @Mock
    private Model modelMock;

    @Test
    public void addPaymentAttributeToModel_WhenSepaPayment_ShouldAddPaymentTypes() {
        final HashMap sepaPaymentTypes = new HashMap<>();
        sepaPaymentTypes.put(RECURRING_PAYMENT_KEY, RECURRING_PAYMENT_VALUE);
        sepaPaymentTypes.put(SINGLE_PAYMENT_KEY, SINGLE_PAYMENT_VALUE);
        doReturn(sepaPaymentTypes).when(testObj).getSepaPaymentTypes();

        testObj.addPaymentAttributeToModel(modelMock);

        verify(modelMock).addAttribute(SEPA_PAYMENT_TYPES_MODEL_ATTRIBUTE, sepaPaymentTypes);
    }

    @Test
    public void getStrategyKey_WhenSepa_ShouldReturnSepaType() {
        assertEquals(SEPA, testObj.getStrategyKey());
    }

    @Test
    public void getSepaPaymentTypes_ShouldReturnTheMapWithTheValues() {
        when(typeServiceMock.getEnumerationTypeForCode(SepaPaymentType._TYPECODE)).thenReturn(enumerationMetaTypeMock);
        when(enumerationMetaTypeMock.getValues()).thenReturn(ImmutableList.of(enumerationValue1Mock, enumerationValue2Mock));
        when(enumerationValue1Mock.getCode()).thenReturn(RECURRING_PAYMENT_KEY);
        when(enumerationValue1Mock.getName()).thenReturn(RECURRING_PAYMENT_VALUE);
        when(enumerationValue2Mock.getCode()).thenReturn(SINGLE_PAYMENT_KEY);
        when(enumerationValue2Mock.getName()).thenReturn(SINGLE_PAYMENT_VALUE);

        final HashMap<String, String> result = testObj.getSepaPaymentTypes();

        assertEquals(2, result.size());
        assertTrue(result.containsKey(RECURRING_PAYMENT_KEY));
        assertEquals(RECURRING_PAYMENT_VALUE, result.get(RECURRING_PAYMENT_KEY));
        assertTrue(result.containsKey(SINGLE_PAYMENT_KEY));
        assertEquals("One-off payment", result.get(SINGLE_PAYMENT_KEY));
    }
}
