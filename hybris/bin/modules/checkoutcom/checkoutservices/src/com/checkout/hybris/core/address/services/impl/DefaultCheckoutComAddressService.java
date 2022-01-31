package com.checkout.hybris.core.address.services.impl;

import com.checkout.hybris.core.address.services.CheckoutComAddressService;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.impl.DefaultAddressService;

import java.util.StringJoiner;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

/**
 * Default implementation of the {@link CheckoutComAddressService}
 */
public class DefaultCheckoutComAddressService extends DefaultAddressService implements CheckoutComAddressService {

    protected static final String DELIMITER = " ";

    protected final ModelService modelService;

    public DefaultCheckoutComAddressService(final ModelService modelService) {
        this.modelService = modelService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCartPaymentAddress(final CartModel cartModel, final AddressModel addressModel) {
        cartModel.setPaymentAddress(addressModel);
        modelService.save(cartModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCustomerFullNameFromAddress(final AddressModel addressModel) {
        validateParameterNotNull(addressModel, "Address cannot be null");

        return new StringJoiner(DELIMITER)
                .add(addressModel.getTitle() != null ? addressModel.getTitle().getName() : "")
                .add(addressModel.getFirstname())
                .add(addressModel.getLastname()).toString()
                .stripLeading();
    }
}
