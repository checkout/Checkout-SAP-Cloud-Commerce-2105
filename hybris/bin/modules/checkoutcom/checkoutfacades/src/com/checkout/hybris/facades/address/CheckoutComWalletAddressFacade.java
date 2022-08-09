package com.checkout.hybris.facades.address;

import com.checkout.hybris.facades.beans.WalletPaymentContact;

/**
 * Facade to handle the address data for wallet payments (GooglePay, ApplePay)
 */
public interface CheckoutComWalletAddressFacade {

    /**
     * Populate the address data based on the walletContact and set the billing address into the cart
     *
     * @param billingContact the billing contact from the form
     */
    void handleAndSaveBillingAddress(WalletPaymentContact billingContact);

    /**
     * Populate the address data based on the walletContact and set the shipping address into the cart
     *
     * @param shippingContact the shipping contact from the form
     */
    void handleAndSaveShippingAddress(WalletPaymentContact shippingContact);
}
