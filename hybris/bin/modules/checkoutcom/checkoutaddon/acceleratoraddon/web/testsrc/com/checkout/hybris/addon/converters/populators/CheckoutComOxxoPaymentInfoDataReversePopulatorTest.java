package com.checkout.hybris.addon.converters.populators;


import com.checkout.hybris.addon.forms.PaymentDataForm;
import com.checkout.hybris.facades.beans.OxxoPaymentInfoData;
import de.hybris.bootstrap.annotations.UnitTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComOxxoPaymentInfoDataReversePopulatorTest {

    private static final String DOCUMENT_VALUE = "asafasfasfafasf";
    private static final String DOCUMENT = "document";

    @InjectMocks
    private CheckoutComOxxoPaymentInfoDataReversePopulator testObj;

    private OxxoPaymentInfoData target = new OxxoPaymentInfoData();
    private PaymentDataForm source = new PaymentDataForm();
    private Map<String, Object> formAttributes = new HashMap<>();

    @Test
    public void populate_ShouldPopulateDocument() {
        formAttributes.put(DOCUMENT, DOCUMENT_VALUE);
        source.setFormAttributes(formAttributes);

        testObj.populate(source, target);

        Assert.assertEquals(target.getDocument(), DOCUMENT_VALUE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_ShouldThrowException_WhenSourceIsNull() {
        testObj.populate(null, target);
    }

    @Test(expected = IllegalArgumentException.class)
    public void populate_ShouldThrowException_WhenTargetIsNull() {
        testObj.populate(source, null);
    }
}
