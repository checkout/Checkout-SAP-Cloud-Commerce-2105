package com.checkout.hybris.facades.address.impl;

import com.checkout.hybris.facades.address.CheckoutComAddressFacade;
import com.checkout.hybris.facades.address.CheckoutComWalletAddressFacade;
import com.checkout.hybris.facades.beans.*;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
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
    protected final CheckoutFacade checkoutFacade;

    public DefaultCheckoutComWalletAddressFacade(final Converter<GooglePayPaymentContact, AddressData> checkoutComGooglePayAddressReverseConverter,
                                                 final Converter<ApplePayPaymentContact, AddressData> checkoutComApplePayAddressReverseConverter,
                                                 final CheckoutCustomerStrategy checkoutCustomerStrategy,
                                                 final CheckoutComAddressFacade checkoutComAddressFacade,
                                                 final UserFacade userFacade,
                                                 final CheckoutFacade checkoutFacade) {
        this.checkoutComGooglePayAddressReverseConverter = checkoutComGooglePayAddressReverseConverter;
        this.checkoutComApplePayAddressReverseConverter = checkoutComApplePayAddressReverseConverter;
        this.checkoutCustomerStrategy = checkoutCustomerStrategy;
        this.checkoutComAddressFacade = checkoutComAddressFacade;
        this.userFacade = userFacade;
        this.checkoutFacade = checkoutFacade;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleAndSaveBillingAddress(final WalletPaymentContact billingContact) {
        final AddressData addressData = getAddressDataForWallet(billingContact);
        Optional.ofNullable(addressData).ifPresent(addressData1 -> Optional.ofNullable(checkoutCustomerStrategy.getCurrentUserForCheckout())
                .ifPresentOrElse(currentUserForCheckout -> addressData1.setEmail(currentUserForCheckout.getContactEmail()),
                        () -> addressData1.setEmail(null)));

        userFacade.addAddress(addressData);
        checkoutComAddressFacade.setCartBillingDetails(addressData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleAndSaveShippingAddress(final WalletPaymentContact shippingContact) {
        final AddressData addressData = getAddressDataForWallet(shippingContact);
        Optional.of(addressData).ifPresent(addressData1 -> addressData.setShippingAddress(true));
        Optional.of(addressData).ifPresent(addressData1 -> addressData.setVisibleInAddressBook(true));
        userFacade.addAddress(addressData);
        checkoutFacade.setDeliveryAddress(addressData);
    }

    /**
     * Gets the address data based on the type of wallet payment contact
     *
     * @param paymentContact wallet payment contact
     * @return {@link AddressData}
     */
    private AddressData getAddressDataForWallet(final WalletPaymentContact paymentContact) {
        if (paymentContact instanceof GooglePayPaymentContact) {
            return checkoutComGooglePayAddressReverseConverter.convert((GooglePayPaymentContact) paymentContact);
        }
        if (paymentContact instanceof ApplePayPaymentContact) {
            return checkoutComApplePayAddressReverseConverter.convert((ApplePayPaymentContact) paymentContact);
        }
        return null;
    }
}
