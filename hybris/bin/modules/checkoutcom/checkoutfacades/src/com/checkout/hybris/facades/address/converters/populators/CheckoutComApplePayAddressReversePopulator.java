package com.checkout.hybris.facades.address.converters.populators;

import com.checkout.hybris.facades.address.CheckoutComAddressFacade;
import com.checkout.hybris.facades.beans.ApplePayPaymentContact;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.collections.CollectionUtils;

import java.util.Optional;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Populates the AddressData from the ApplePayPaymentContact
 */
public class CheckoutComApplePayAddressReversePopulator implements Populator<ApplePayPaymentContact, AddressData> {

    protected final CheckoutComAddressFacade checkoutComAddressFacade;

    public CheckoutComApplePayAddressReversePopulator(final CheckoutComAddressFacade checkoutComAddressFacade) {
        this.checkoutComAddressFacade = checkoutComAddressFacade;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final ApplePayPaymentContact source, final AddressData target) throws ConversionException {
        validateParameterNotNull(source, "ApplePayPaymentContact cannot be null.");
        validateParameterNotNull(target, "AddressData cannot be null.");

        Optional.ofNullable(source.getGivenName()).ifPresent(target::setFirstName);
        Optional.ofNullable(source.getFamilyName()).ifPresent(target::setLastName);
        Optional.ofNullable(source.getFamilyName()).ifPresent(target::setLastName);
        Optional.ofNullable(source.getAddressLines()).filter(CollectionUtils::isNotEmpty)
                .ifPresent(addressLines -> target.setLine1(addressLines.get(0)));
        Optional.ofNullable(source.getAddressLines()).filter(CollectionUtils::isNotEmpty)
                .filter(addressLines -> addressLines.size() >= 2)
                .ifPresent(addressLines -> target.setLine2(addressLines.get(1)));
        Optional.ofNullable(source.getLocality()).ifPresent(target::setTown);
        Optional.ofNullable(source.getPostalCode()).ifPresent(target::setPostalCode);
        Optional.ofNullable(source.getPhoneNumber()).ifPresent(target::setPhone);
        Optional.ofNullable(source.getEmailAddress()).ifPresent(target::setEmail);
        String countryCode = source.getCountryCode();
        checkoutComAddressFacade.setAddressDataCountry(countryCode, target);
        checkoutComAddressFacade.setAddressDataRegion(source.getAdministrativeArea(), countryCode, target);
        target.setBillingAddress(true);
    }
}
