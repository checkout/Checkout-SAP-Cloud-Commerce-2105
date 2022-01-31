package com.checkout.hybris.addon.converters.populators;

import de.hybris.platform.acceleratorstorefrontcommons.forms.AddressForm;
import de.hybris.platform.commercefacades.i18n.I18NFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.commercefacades.user.data.RegionData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import org.apache.commons.lang.StringUtils;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Populates the AddressData from the AddressForm
 */
public class CheckoutComAddressDataReversePopulator implements Populator<AddressForm, AddressData> {

    protected final I18NFacade i18NFacade;

    public CheckoutComAddressDataReversePopulator(final I18NFacade i18NFacade) {
        this.i18NFacade = i18NFacade;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final AddressForm source, final AddressData target) throws ConversionException {
        validateParameterNotNull(source, "AddressForm cannot be null.");
        validateParameterNotNull(target, "AddressData cannot be null.");

        target.setId(source.getAddressId());
        target.setTitleCode(source.getTitleCode());
        target.setFirstName(source.getFirstName());
        target.setLastName(source.getLastName());
        target.setLine1(source.getLine1());
        target.setLine2(source.getLine2());
        target.setTown(source.getTownCity());
        target.setPostalCode(source.getPostcode());
        target.setBillingAddress(false);
        target.setShippingAddress(true);
        target.setPhone(source.getPhone());

        if (StringUtils.isNotBlank(source.getCountryIso())) {
            final CountryData countryData = i18NFacade.getCountryForIsocode(source.getCountryIso());
            target.setCountry(countryData);
        }
        if (StringUtils.isNotBlank(source.getRegionIso())) {
            final RegionData regionData = i18NFacade.getRegion(source.getCountryIso(), source.getRegionIso());
            target.setRegion(regionData);
        }
    }
}
