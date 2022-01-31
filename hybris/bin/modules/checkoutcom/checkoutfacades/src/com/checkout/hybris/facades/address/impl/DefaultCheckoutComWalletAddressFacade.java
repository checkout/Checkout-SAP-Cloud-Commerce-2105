package com.checkout.hybris.facades.address.impl;

import com.checkout.hybris.facades.address.CheckoutComAddressFacade;
import com.checkout.hybris.facades.address.CheckoutComWalletAddressFacade;
import com.checkout.hybris.facades.beans.ApplePayPaymentContact;
import com.checkout.hybris.facades.beans.GooglePayPaymentContact;
import com.checkout.hybris.facades.beans.WalletPaymentContact;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.strategies.CheckoutCustomerStrategy;
import de.hybris.platform.servicelayer.dto.converter.Converter;

import java.util.Optional;

/**
 * Default implementation of {@link CheckoutComWalletAddressFacade}
 */
public class DefaultCheckoutComWalletAddressFacade implements CheckoutComWalletAddressFacade {

    protected final Converter<GooglePayPaymentContact, AddressData> checkoutComGooglePayAddressReverseConverter;
    protected final Converter<ApplePayPaymentContact, AddressData> checkoutComApplePayAddressReverseConverter;
    protected final CheckoutCustomerStrategy checkoutCustomerStrategy;
    protected final CheckoutComAddressFacade checkoutComAddressFacade;
    protected final UserFacade userFacade;

    public DefaultCheckoutComWalletAddressFacade(final Converter<GooglePayPaymentContact, AddressData> checkoutComGooglePayAddressReverseConverter,
                                                 final Converter<ApplePayPaymentContact, AddressData> checkoutComApplePayAddressReverseConverter,
                                                 final CheckoutCustomerStrategy checkoutCustomerStrategy,
                                                 final CheckoutComAddressFacade checkoutComAddressFacade,
                                                 final UserFacade userFacade) {
        this.checkoutComGooglePayAddressReverseConverter = checkoutComGooglePayAddressReverseConverter;
        this.checkoutComApplePayAddressReverseConverter = checkoutComApplePayAddressReverseConverter;
        this.checkoutCustomerStrategy = checkoutCustomerStrategy;
        this.checkoutComAddressFacade = checkoutComAddressFacade;
        this.userFacade = userFacade;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleAndSaveAddresses(final WalletPaymentContact billingContact) {
        final AddressData addressData = getAddressDataForWallet(billingContact);
        Optional.ofNullable(addressData).ifPresent(addressData1 -> Optional.ofNullable(checkoutCustomerStrategy.getCurrentUserForCheckout())
                .ifPresentOrElse(currentUserForCheckout -> addressData1.setEmail(currentUserForCheckout.getContactEmail()),
                        () -> addressData1.setEmail(null)));

        userFacade.addAddress(addressData);
        checkoutComAddressFacade.setCartBillingDetails(addressData);
    }

    /**
     * Gets the address data based on the type of wallet payment contact
     *
     * @param billingContact wallet payment contact
     * @return {@link AddressData}
     */
    private AddressData getAddressDataForWallet(final WalletPaymentContact billingContact) {
        if (billingContact instanceof GooglePayPaymentContact) {
            return checkoutComGooglePayAddressReverseConverter.convert((GooglePayPaymentContact) billingContact);
        }
        if (billingContact instanceof ApplePayPaymentContact) {
            return checkoutComApplePayAddressReverseConverter.convert((ApplePayPaymentContact) billingContact);
        }
        return null;
    }
}
