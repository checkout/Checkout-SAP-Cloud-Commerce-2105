package com.checkout.hybris.facades.payment.wallet;

import com.checkout.hybris.facades.beans.PlaceWalletOrderDataResponse;
import com.checkout.hybris.facades.beans.WalletPaymentAdditionalAuthInfo;
import com.checkout.hybris.facades.enums.WalletPaymentType;
import de.hybris.platform.order.InvalidCartException;
import org.springframework.validation.Validator;

/**
 * Facade to place order using wallet payments
 */
public interface CheckoutComWalletOrderFacade {

    /**
     * Places the order for the given wallet payment type
     *
     * @param walletPaymentAdditionalAuthInfo the create token request object
     * @param walletPaymentType               the wallet specific type
     * @return the populated place order response
     */
    PlaceWalletOrderDataResponse placeWalletOrder(WalletPaymentAdditionalAuthInfo walletPaymentAdditionalAuthInfo,
                                                  WalletPaymentType walletPaymentType);

    /**
     * Validates the wallet cart before place order
     *
     * @param checkoutComPlaceOrderCartValidator the custom validator
     * @throws InvalidCartException throws exception in case of validation failure
     */
    void validateCartForPlaceOrder(Validator checkoutComPlaceOrderCartValidator) throws InvalidCartException;
}
