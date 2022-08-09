package com.checkout.hybris.facades.address.impl;

import com.checkout.hybris.core.address.services.CheckoutComAddressService;
import com.checkout.hybris.facades.accelerator.CheckoutComCheckoutFlowFacade;
import com.checkout.hybris.facades.address.CheckoutComAddressFacade;
import de.hybris.platform.commercefacades.i18n.I18NFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.apache.commons.lang.StringUtils;

import java.util.Optional;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Default implementation of {@link CheckoutComAddressFacade}
 */
public class DefaultCheckoutComAddressFacade implements CheckoutComAddressFacade {

    protected static final String CART_MODEL_NULL = "CartModel cannot be null";

    protected final CheckoutComAddressService addressService;
    protected final Converter<AddressModel, AddressData> addressConverter;
    protected final CartService cartService;
    protected final CheckoutComCheckoutFlowFacade checkoutFlowFacade;
    private final I18NFacade i18NFacade;

    public DefaultCheckoutComAddressFacade(final CheckoutComAddressService addressService,
                                           final Converter<AddressModel, AddressData> addressConverter,
                                           final CartService cartService,
                                           final CheckoutComCheckoutFlowFacade checkoutFlowFacade,
                                           final I18NFacade i18NFacade) {
        this.addressService = addressService;
        this.addressConverter = addressConverter;
        this.cartService = cartService;
        this.checkoutFlowFacade = checkoutFlowFacade;
        this.i18NFacade = i18NFacade;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AddressData getCartBillingAddress() {
        if (cartService.hasSessionCart()) {
            final CartModel sessionCart = cartService.getSessionCart();

            final AddressModel addressModel = sessionCart.getPaymentAddress();
            if (addressModel != null) {
                return addressConverter.convert(addressModel);
            }
            return null;
        } else {
            throw new IllegalArgumentException(CART_MODEL_NULL);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCartBillingDetails(final AddressData addressData) {
        validateParameterNotNull(addressData, "Delivery Address cannot be null");
        setCartBillingDetailsByAddressId(addressData.getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCartBillingDetailsByAddressId(final String addressId) {
        if (cartService.hasSessionCart()) {
            final CartModel sessionCart = cartService.getSessionCart();

            final AddressModel addressModel = checkoutFlowFacade.getDeliveryAddressModelForCode(addressId);
            if (addressModel != null) {
                addressService.setCartPaymentAddress(sessionCart, addressModel);
            }
        } else {
            throw new IllegalArgumentException(CART_MODEL_NULL);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAddressDataCountry(final String countryCode, final AddressData addressData) {
        Optional.ofNullable(countryCode).filter(StringUtils::isNotBlank)
                .map(String::toUpperCase)
                .map(i18NFacade::getCountryForIsocode)
                .ifPresent(addressData::setCountry);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAddressDataRegion(final String region,final String countryCode, final AddressData addressData) {
        Optional.ofNullable(region).filter(StringUtils::isNotBlank)
                .map(String::toUpperCase)
                .filter(administrativeAreaUpperCased -> StringUtils.isNotBlank(countryCode))
                .map(administrativeAreaUpperCased -> i18NFacade.getRegion(countryCode.toUpperCase(),administrativeAreaUpperCased))
                .ifPresent(addressData::setRegion);
    }

}
