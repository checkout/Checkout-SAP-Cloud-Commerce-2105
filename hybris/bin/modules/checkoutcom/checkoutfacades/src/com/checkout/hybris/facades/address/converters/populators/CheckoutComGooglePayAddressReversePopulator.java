package com.checkout.hybris.facades.address.converters.populators;

import com.checkout.hybris.facades.address.CheckoutComAddressFacade;
import com.checkout.hybris.facades.beans.GooglePayPaymentContact;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.lang.StringUtils;

import java.util.Optional;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * Populates the AddressData from the GooglePayPaymentContact
 */
public class CheckoutComGooglePayAddressReversePopulator implements Populator<GooglePayPaymentContact, AddressData> {

    protected final CheckoutComAddressFacade checkoutComAddressFacade;

    public CheckoutComGooglePayAddressReversePopulator(final CheckoutComAddressFacade checkoutComAddressFacade) {
        this.checkoutComAddressFacade = checkoutComAddressFacade;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final GooglePayPaymentContact source, final AddressData target) throws ConversionException {
        validateParameterNotNull(source, "GooglePayPaymentContact cannot be null.");
        validateParameterNotNull(target, "AddressData cannot be null.");

        if (StringUtils.isNotBlank(source.getName())) {
            String[] nameSplit = StringUtils.split(source.getName(), " ", 2);
            Optional.ofNullable(nameSplit[0]).ifPresent(target::setFirstName);
            Optional.ofNullable(nameSplit[1]).ifPresent(target::setLastName);
        } else {
            target.setFirstName("");
            target.setLastName("");
        }
        Optional.ofNullable(source.getAddress1()).ifPresent(target::setLine1);
        setLine2(source, target);
        Optional.ofNullable(source.getLocality()).ifPresent(target::setTown);
        Optional.ofNullable(source.getPostalCode()).ifPresent(target::setPostalCode);
        Optional.ofNullable(source.getEmail()).ifPresent(target::setEmail);
        target.setBillingAddress(true);

        String countryCode = source.getCountryCode();
        checkoutComAddressFacade.setAddressDataCountry(countryCode,target);
        checkoutComAddressFacade.setAddressDataRegion(source.getAdministrativeArea(),countryCode,target);
    }

    private void setLine2(GooglePayPaymentContact source, AddressData target) {
        final String line2 = join(new String[]{source.getAddress2(), source.getAddress3()}, " ").trim();
        Optional.of(line2).ifPresent(target::setLine2);
    }
}
