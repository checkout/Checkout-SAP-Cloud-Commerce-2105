package com.checkout.hybris.addon.forms;

import java.util.HashMap;
import java.util.Map;

/**
 * The generic payment data form to collect the specific payment method data
 */
public class PaymentDataForm {

    private Map<String, Object> formAttributes = new HashMap<>();

    public Map<String, Object> getFormAttributes() {
        return formAttributes;
    }

    public void setFormAttributes(final Map<String, Object> formAttributes) {
        this.formAttributes = formAttributes;
    }
}
