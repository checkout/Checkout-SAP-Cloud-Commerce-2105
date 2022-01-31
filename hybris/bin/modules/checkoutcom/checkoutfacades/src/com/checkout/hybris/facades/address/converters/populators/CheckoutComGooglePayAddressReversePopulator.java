package com.checkout.hybris.facades.address.converters.populators;

import com.checkout.hybris.facades.beans.GooglePayPaymentContact;
import de.hybris.platform.commercefacades.i18n.I18NFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.commercefacades.user.data.RegionData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.lang.StringUtils;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Populates the AddressData from the GooglePayPaymentContact
 */
public class CheckoutComGooglePayAddressReversePopulator implements Populator<GooglePayPaymentContact, AddressData> {

    protected final I18NFacade i18NFacade;

    public CheckoutComGooglePayAddressReversePopulator(final I18NFacade i18NFacade) {
        this.i18NFacade = i18NFacade;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final GooglePayPaymentContact source, final AddressData target) throws ConversionException {
        validateParameterNotNull(source, "GooglePayPaymentContact cannot be null.");
        validateParameterNotNull(target, "AddressData cannot be null.");

        String[] nameSplit = StringUtils.split(source.getName(), " ", 2);
        target.setFirstName(nameSplit[0]);
        target.setLastName(nameSplit[1]);
        target.setLine1(source.getAddress1());
        target.setLine2(org.apache.commons.lang3.StringUtils.join(new String[]{source.getAddress2(), source.getAddress3()}, " ").trim());
        target.setTown(source.getLocality());
        target.setPostalCode(source.getPostalCode());
        target.setBillingAddress(true);

        if (StringUtils.isNotBlank(source.getCountryCode())) {
            final CountryData countryData = i18NFacade.getCountryForIsocode(source.getCountryCode().toUpperCase());
            target.setCountry(countryData);
        }
        if (StringUtils.isNotBlank(source.getAdministrativeArea())) {
            final RegionData regionData = i18NFacade.getRegion(source.getCountryCode().toUpperCase(), source.getAdministrativeArea());
            target.setRegion(regionData);
        }
    }
}
