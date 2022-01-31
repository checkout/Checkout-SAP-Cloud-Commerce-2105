package com.checkout.hybris.addon.forms;

import de.hybris.platform.acceleratorstorefrontcommons.forms.AddressForm;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class PaymentDetailsForm {

    private boolean useDeliveryAddress;
    @NotNull
    private String paymentMethod;
    @NotNull
    @Valid
    private AddressForm billingAddress;
    private boolean redirect;
    private boolean dataRequired;

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(final String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public boolean getUseDeliveryAddress() {
        return useDeliveryAddress;
    }

    public void setUseDeliveryAddress(final boolean useDeliveryAddress) {
        this.useDeliveryAddress = useDeliveryAddress;
    }

    public AddressForm getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(final AddressForm billingAddress) {
        this.billingAddress = billingAddress;
    }

    public boolean isRedirect() {
        return redirect;
    }

    public void setRedirect(final boolean redirect) {
        this.redirect = redirect;
    }

    public boolean isDataRequired() {
        return dataRequired;
    }

    public void setDataRequired(final boolean dataRequired) {
        this.dataRequired = dataRequired;
    }
}
