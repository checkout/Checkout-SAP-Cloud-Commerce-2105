package com.checkout.hybris.facades.customer.impl;

import com.checkout.hybris.facades.customer.CheckoutComCustomerFacade;
import de.hybris.platform.commercefacades.customer.impl.DefaultCustomerFacade;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.enums.CustomerType;
import de.hybris.platform.commerceservices.order.CommerceCartCalculationStrategy;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.order.exceptions.CalculationException;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

/**
 * {{@inheritDoc}}
 */
public class DefaultCheckoutComCustomerFacade extends DefaultCustomerFacade implements CheckoutComCustomerFacade {

    private static final Logger LOG = Logger.getLogger(DefaultCheckoutComCustomerFacade.class);

    private static final String GUEST_GOOGLE_PAY_EXPRESS_CHECKOUT_EMAIL = "guestGooglePayExpressUser@checkout.com";
    private static final String GUEST_APPLE_PAY_EXPRESS_CHECKOUT_EMAIL = "guestApplePayExpressUser@checkout.com";
    private static final String ANONYMOUS_CHECKOUT = "anonymous_checkout";


    private final CommerceCartCalculationStrategy commerceCartCalculationStrategy;

    public DefaultCheckoutComCustomerFacade(final CommerceCartCalculationStrategy commerceCartCalculationStrategy) {
        super();
        this.commerceCartCalculationStrategy = commerceCartCalculationStrategy;
    }

    /**
     * {{@inheritDoc}}
     */
    @Override
    public void createGooglePayExpressCheckoutGuestUserForAnonymousCheckout() throws DuplicateUidException {
        createGuestUserForAnonymousCheckout(GUEST_GOOGLE_PAY_EXPRESS_CHECKOUT_EMAIL, GUEST_GOOGLE_PAY_EXPRESS_CHECKOUT_EMAIL);
    }

    /**
     * {{@inheritDoc}}
     */
    @Override
    public void createApplePayExpressCheckoutGuestUserForAnonymousCheckout() throws DuplicateUidException {
        createGuestUserForAnonymousCheckout(GUEST_APPLE_PAY_EXPRESS_CHECKOUT_EMAIL, GUEST_APPLE_PAY_EXPRESS_CHECKOUT_EMAIL);
    }

    /**
     * {{@inheritDoc}}
     */
    @Override
    public void createApplePayExpressCheckoutGuestUserForAnonymousCheckoutAndSetItOnSession() throws DuplicateUidException {
        createApplePayExpressCheckoutGuestUserForAnonymousCheckout();
        getSessionService().setAttribute(ANONYMOUS_CHECKOUT, Boolean.TRUE);
    }

    @Override
    public void createGooglePayExpressCheckoutGuestUserForAnonymousCheckoutAndSetItOnSession() throws DuplicateUidException {
        createGooglePayExpressCheckoutGuestUserForAnonymousCheckout();
        getSessionService().setAttribute(ANONYMOUS_CHECKOUT, Boolean.TRUE);
    }

    /**
     * {{@inheritDoc}}
     */
    @Override
    public boolean isGooglePayExpressGuestCustomer() {
        final UserModel currentUser = getCartService().getSessionCart().getUser();
        return currentUser instanceof CustomerModel
                && CustomerType.GUEST.equals(((CustomerModel) currentUser).getType())
                && GUEST_GOOGLE_PAY_EXPRESS_CHECKOUT_EMAIL.equals(currentUser.getName());
    }

    /**
     * {{@inheritDoc}}
     */
    @Override
    public boolean isApplePayExpressGuestCustomer() {
        final UserModel currentUser = getCartService().getSessionCart().getUser();
        return currentUser instanceof CustomerModel
                && CustomerType.GUEST.equals(((CustomerModel) currentUser).getType())
                && GUEST_APPLE_PAY_EXPRESS_CHECKOUT_EMAIL.equals(currentUser.getName());
    }

    @Override
    public void updateExpressCheckoutUserEmail(final String email, final String name) {
        validateParameterNotNullStandardMessage("email", email);
        final UserModel expressGPayUser = getCartService().getSessionCart().getUser();
        final String guid = StringUtils.substringBefore(expressGPayUser.getUid(), "|");
        expressGPayUser.setUid(guid + "|" + email);
        expressGPayUser.setName(name);
        getModelService().save(expressGPayUser);
    }

    @Override
    public void updateCartWithGuestForAnonymousCheckout(final CustomerData guestCustomerData) {
        // First thing to do is to try to change the user on the session cart
        if (getCartService().hasSessionCart()) {
            getCartService().changeCurrentCartUser(getUserService().getUserForUID(guestCustomerData.getUid()));
        }

        // Update the session currency (which might change the cart currency)
        if (!updateSessionCurrency(guestCustomerData.getCurrency(), getStoreSessionFacade().getDefaultCurrency())) {
            // Update the user
            getUserFacade().syncSessionCurrency();
        }

        if (!updateSessionLanguage(guestCustomerData.getLanguage(), getStoreSessionFacade().getDefaultLanguage())) {
            // Update the user
            getUserFacade().syncSessionLanguage();
        }

        // Calculate the cart after setting everything up
        if (getCartService().hasSessionCart()) {
            final CartModel sessionCart = getCartService().getSessionCart();

            // Clear the payment info before starting the guest checkout.
            sessionCart.setPaymentInfo(null);
            getCartService().saveOrder(sessionCart);

            try {
                final CommerceCartParameter parameter = new CommerceCartParameter();
                parameter.setEnableHooks(true);
                parameter.setCart(sessionCart);
                getCommerceCartService().recalculateCart(parameter);
            } catch (final CalculationException ex) {
                LOG.error("Failed to recalculate order [" + sessionCart.getCode() + "]", ex);
            }
        }
    }

    /**
     * {{@inheritDoc}}
     */
    @Override
    public void recalculateExpressCheckoutCart() {
        final CommerceCartParameter calculateCartParameter = new CommerceCartParameter();
        calculateCartParameter.setEnableHooks(true);
        calculateCartParameter.setCart(getCartService().getSessionCart());
        commerceCartCalculationStrategy.calculateCart(calculateCartParameter);
    }
}
