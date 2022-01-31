package com.checkout.hybris.facades.payment.attributes.strategies.impl;

import com.checkout.hybris.core.enums.AchAccountType;
import com.google.common.collect.ImmutableList;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.enumeration.EnumerationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.ui.Model;

import java.util.List;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.ACH;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComAchPaymentAttributeStrategyTest {

    private static final String ACH_ACCOUNT_TYPES_MODEL_ATTRIBUTE = "achAccountTypes";

    @InjectMocks
    @Spy
    private CheckoutComAchPaymentAttributeStrategy testObj;

    @Mock
    private Model modelMock;
    @Mock
    private EnumerationService enumerationServiceMock;
    @Mock
    private AchAccountType achAccountType1, achAccountType2;

    @Before
    public void setUp() {
        when(achAccountType1.getCode()).thenReturn("Checking");
        when(achAccountType2.getCode()).thenReturn("Savings");
    }

    @Test
    public void addPaymentAttributeToModel() {
        final ImmutableList<String> achAccountTypes = ImmutableList.of("Saving", "Checking");
        doReturn(achAccountTypes).when(testObj).getAchAccountTypeCodes();

        testObj.addPaymentAttributeToModel(modelMock);

        verify(modelMock).addAttribute(ACH_ACCOUNT_TYPES_MODEL_ATTRIBUTE, achAccountTypes);
    }

    @Test
    public void getStrategyKey_WhenAch_ShouldReturnAchType() {
        assertEquals(ACH, testObj.getStrategyKey());
    }

    @Test
    public void getAchAccountTypeCodes_ShouldReturnTheAccountTypeCodes() {
        when(enumerationServiceMock.getEnumerationValues(AchAccountType.class)).thenReturn(asList(achAccountType1, achAccountType2));

        final List<String> results = testObj.getAchAccountTypeCodes();

        assertFalse(results.isEmpty());
        assertEquals(2, results.size());
        assertTrue(results.contains(achAccountType1.getCode()));
        assertTrue(results.contains(achAccountType2.getCode()));
    }

    @Test
    public void getAchAccountTypeCodes_WhenNoValuesFound_ShouldReturnEmptyList() {
        when(enumerationServiceMock.getEnumerationValues(AchAccountType.class)).thenReturn(emptyList());

        final List<String> results = testObj.getAchAccountTypeCodes();

        assertTrue(results.isEmpty());
    }
}
