package com.checkout.hybris.facades.apm.converters.populators;

import com.checkout.data.apm.CheckoutComAPMConfigurationData;
import com.checkout.hybris.core.apm.services.CheckoutComAPMConfigurationService;
import com.checkout.hybris.core.model.CheckoutComAPMConfigurationModel;
import de.hybris.platform.cmsfacades.data.MediaData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

/**
 * Populates the {@link CheckoutComAPMConfigurationData} from {@link CheckoutComAPMConfigurationModel}
 */
public class CheckoutComAPMConfigurationPopulator implements Populator<CheckoutComAPMConfigurationModel, CheckoutComAPMConfigurationData> {

    protected final CheckoutComAPMConfigurationService checkoutComAPMConfigurationService;
    protected final Converter<MediaModel, MediaData> mediaModelConverter;

    public CheckoutComAPMConfigurationPopulator(final CheckoutComAPMConfigurationService checkoutComAPMConfigurationService,
                                                final Converter<MediaModel, MediaData> mediaModelConverter) {
        this.checkoutComAPMConfigurationService = checkoutComAPMConfigurationService;
        this.mediaModelConverter = mediaModelConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void populate(final CheckoutComAPMConfigurationModel source, final CheckoutComAPMConfigurationData target) throws ConversionException {
        final String apmCode = source.getCode();
        target.setCode(apmCode);
        target.setName(source.getName());
        target.setIsRedirect(checkoutComAPMConfigurationService.isApmRedirect(apmCode));
        target.setIsUserDataRequired(checkoutComAPMConfigurationService.isApmUserDataRequired(apmCode));

        checkoutComAPMConfigurationService.getApmConfigurationMedia(source).ifPresent(mediaModel -> target.setMedia(mediaModelConverter.convert(mediaModel)));
    }
}
