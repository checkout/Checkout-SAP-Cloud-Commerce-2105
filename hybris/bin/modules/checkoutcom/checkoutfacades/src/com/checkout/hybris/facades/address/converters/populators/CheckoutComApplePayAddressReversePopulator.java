package com.checkout.hybris.facades.address.converters.populators;

import com.checkout.hybris.facades.beans.ApplePayPaymentContact;
import de.hybris.platform.commercefacades.i18n.I18NFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.commercefacades.user.data.RegionData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Populates the AddressData from the ApplePayPaymentContact
 */
public class CheckoutComApplePayAddressReversePopulator implements Populator<ApplePayPaymentContact, AddressData> {

    protected final I18NFacade i18NFacade;

    public CheckoutComApplePayAddressReversePopulator(final I18NFacade i18NFacade) {
        this.i18NFacade = i18NFacade;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final ApplePayPaymentContact source, final AddressData target) throws ConversionException {
        validateParameterNotNull(source, "ApplePayPaymentContact cannot be null.");
        validateParameterNotNull(target, "AddressData cannot be null.");

        target.setFirstName(source.getGivenName());
        target.setLastName(source.getFamilyName());
        target.setLine1(CollectionUtils.isNotEmpty(source.getAddressLines()) ? source.getAddressLines().get(0) : null);
        target.setLine2(source.getAddressLines().size() == 2 ? source.getAddressLines().get(1) : null);
        target.setTown(source.getLocality());
        target.setPostalCode(source.getPostalCode());
        target.setBillingAddress(true);
        target.setPhone(source.getPhoneNumber());
        target.setEmail(source.getEmailAddress());

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
