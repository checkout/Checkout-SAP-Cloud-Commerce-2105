package com.checkout.hybris.facades.accelerator.impl;

import de.hybris.platform.acceleratorfacades.flow.CheckoutFlowFacade;
import de.hybris.platform.acceleratorservices.enums.CheckoutPciOptionEnum;
import de.hybris.platform.commercefacades.order.data.*;
import de.hybris.platform.commercefacades.order.impl.DefaultCheckoutFacade;
import de.hybris.platform.commercefacades.storelocator.data.PointOfServiceData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.commerceservices.enums.CountryType;
import de.hybris.platform.commerceservices.order.CommerceCartModificationException;
import de.hybris.platform.order.InvalidCartException;

import java.util.List;

/**
 * Abstract Checkout flow decorator
 */
public abstract class CheckoutComAbstractCheckoutFlowFacadeDecorator extends DefaultCheckoutFacade implements CheckoutFlowFacade {

    protected CheckoutFlowFacade checkoutFlowFacade;

    public CheckoutComAbstractCheckoutFlowFacadeDecorator(final CheckoutFlowFacade checkoutFlowFacade) {
        this.checkoutFlowFacade = checkoutFlowFacade;
    }

    @Override
    public CheckoutPciOptionEnum getSubscriptionPciOption() {
        return checkoutFlowFacade.getSubscriptionPciOption();
    }

    @Override
    public String getCheckoutFlowGroupForCheckout() {
        return checkoutFlowFacade.getCheckoutFlowGroupForCheckout();
    }

    @Override
    public List<PointOfServiceData> getConsolidatedPickupOptions() {
        return checkoutFlowFacade.getConsolidatedPickupOptions();
    }

    @Override
    public List<CartModificationData> consolidateCheckoutCart(final String pickupPointOfServiceName) throws CommerceCartModificationException {
        return checkoutFlowFacade.consolidateCheckoutCart(pickupPointOfServiceName);
    }

    @Override
    public boolean isExpressCheckoutAllowedForCart() {
        return checkoutFlowFacade.isExpressCheckoutAllowedForCart();
    }

    @Override
    public boolean isExpressCheckoutEnabledForStore() {
        return checkoutFlowFacade.isExpressCheckoutAllowedForCart();
    }

    @Override
    public boolean isTaxEstimationEnabledForCart() {
        return checkoutFlowFacade.isTaxEstimationEnabledForCart();
    }

    @Override
    public boolean isNewAddressEnabledForCart() {
        return checkoutFlowFacade.isNewAddressEnabledForCart();
    }

    @Override
    public boolean isRemoveAddressEnabledForCart() {
        return checkoutFlowFacade.isRemoveAddressEnabledForCart();
    }

    @Override
    public ExpressCheckoutResult performExpressCheckout() {
        return checkoutFlowFacade.performExpressCheckout();
    }

    @Override
    public boolean hasValidCart() {
        return checkoutFlowFacade.hasValidCart();
    }

    @Override
    public boolean hasNoDeliveryAddress() {
        return checkoutFlowFacade.hasNoDeliveryAddress();
    }

    @Override
    public boolean hasNoDeliveryMode() {
        return checkoutFlowFacade.hasNoDeliveryMode();
    }

    @Override
    public boolean hasNoPaymentInfo() {
        return checkoutFlowFacade.hasNoPaymentInfo();
    }

    @Override
    public boolean hasCheckoutCart() {
        return checkoutFlowFacade.hasCheckoutCart();
    }

    @Override
    public CartData getCheckoutCart() {
        return checkoutFlowFacade.getCheckoutCart();
    }

    @Override
    public List<AddressData> getSupportedDeliveryAddresses(final boolean visibleAddressesOnly) {
        return (List<AddressData>) checkoutFlowFacade.getSupportedDeliveryAddresses(visibleAddressesOnly);
    }

    @Override
    public AddressData getDeliveryAddressForCode(final String code) {
        return checkoutFlowFacade.getDeliveryAddressForCode(code);
    }

    @Override
    public boolean setDeliveryAddress(final AddressData address) {
        return checkoutFlowFacade.setDeliveryAddress(address);
    }

    @Override
    public boolean removeDeliveryAddress() {
        return checkoutFlowFacade.removeDeliveryAddress();
    }

    @Override
    public List<? extends DeliveryModeData> getSupportedDeliveryModes() {
        return checkoutFlowFacade.getSupportedDeliveryModes();
    }

    @Override
    public boolean setDeliveryAddressIfAvailable() {
        return checkoutFlowFacade.setDeliveryAddressIfAvailable();
    }

    @Override
    public boolean setDeliveryModeIfAvailable() {
        return checkoutFlowFacade.setDeliveryModeIfAvailable();
    }

    @Override
    public boolean setPaymentInfoIfAvailable() {
        return checkoutFlowFacade.setPaymentInfoIfAvailable();
    }

    @Override
    public boolean setDeliveryMode(final String deliveryModeCode) {
        return checkoutFlowFacade.setDeliveryMode(deliveryModeCode);
    }

    @Override
    public boolean removeDeliveryMode() {
        return checkoutFlowFacade.removeDeliveryMode();
    }

    @Override
    public List<CountryData> getDeliveryCountries() {
        return checkoutFlowFacade.getDeliveryCountries();
    }

    @Override
    public List<CountryData> getBillingCountries() {
        return checkoutFlowFacade.getBillingCountries();
    }

    @Override
    public List<CountryData> getCountries(final CountryType countryType) {
        return checkoutFlowFacade.getCountries(countryType);
    }

    @Override
    public boolean setPaymentDetails(final String paymentInfoId) {
        return checkoutFlowFacade.setPaymentDetails(paymentInfoId);
    }

    @Override
    public List<CardTypeData> getSupportedCardTypes() {
        return checkoutFlowFacade.getSupportedCardTypes();
    }

    @Override
    public CCPaymentInfoData createPaymentSubscription(final CCPaymentInfoData paymentInfoData) {
        return checkoutFlowFacade.createPaymentSubscription(paymentInfoData);
    }

    @Override
    public boolean authorizePayment(final String securityCode) {
        return checkoutFlowFacade.authorizePayment(securityCode);
    }

    @Override
    public OrderData placeOrder() throws InvalidCartException {
        return checkoutFlowFacade.placeOrder();
    }

    @Override
    public boolean containsTaxValues() {
        return checkoutFlowFacade.containsTaxValues();
    }

    @Override
    public AddressData getAddressDataForId(final String addressId, final boolean visibleAddressesOnly) {
        return checkoutFlowFacade.getAddressDataForId(addressId, visibleAddressesOnly);
    }

    @Override
    public void prepareCartForCheckout() {

    }

    @Override
    public boolean setDefaultPaymentInfoForCheckout() {
        return checkoutFlowFacade.setDefaultPaymentInfoForCheckout();
    }

    @Override
    public boolean setDefaultDeliveryAddressForCheckout() {
        return checkoutFlowFacade.setDefaultDeliveryAddressForCheckout();
    }

    @Override
    public boolean setCheapestDeliveryModeForCheckout() {
        return checkoutFlowFacade.setCheapestDeliveryModeForCheckout();
    }

    @Override
    public boolean hasShippingItems() {
        return checkoutFlowFacade.hasShippingItems();
    }

    @Override
    public boolean hasPickUpItems() {
        return checkoutFlowFacade.hasPickUpItems();
    }
}
