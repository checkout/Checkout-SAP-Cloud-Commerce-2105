package com.checkout.hybris.commercefacades.user.impl;

import com.checkout.hybris.commercefacades.user.CheckoutComUserFacade;
import com.checkout.hybris.commerceservices.customer.CheckoutComCustomerAccountServiceAdapter;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.TitleData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.payment.CreditCardPaymentInfoModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.user.UserService;

import java.util.List;
import java.util.Optional;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNullStandardMessage;

public class DefaultCheckoutComUserFacade implements CheckoutComUserFacade {

    protected final UserFacade userFacade;
    protected final UserService userService;
    protected final Populator<CCPaymentInfoData, CreditCardPaymentInfoModel> cardPaymentInfoReversePopulator;

    protected final CheckoutComCustomerAccountServiceAdapter checkoutComCustomerAccountServiceAdapter;

    protected final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService;

    public DefaultCheckoutComUserFacade(final UserFacade userFacade,
                                        final UserService userService,
                                        final Populator<CCPaymentInfoData, CreditCardPaymentInfoModel> cardPaymentInfoReversePopulator, final CheckoutComCustomerAccountServiceAdapter checkoutComCustomerAccountServiceAdapter, final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService) {
        this.userFacade = userFacade;
        this.userService = userService;
        this.cardPaymentInfoReversePopulator = cardPaymentInfoReversePopulator;
        this.checkoutComCustomerAccountServiceAdapter = checkoutComCustomerAccountServiceAdapter;
        this.checkoutComMerchantConfigurationService = checkoutComMerchantConfigurationService;
    }

    @Override
    public void removeCCPaymentInfo(final String id) {
        if (checkoutComMerchantConfigurationService.isNasUsed()) {
            validateParameterNotNullStandardMessage("id", id);
            final CustomerModel currentCustomer = (CustomerModel) userService.getCurrentUser();
            final Optional<CreditCardPaymentInfoModel> creditCard = getCreditCardForCustomer(id, currentCustomer);
            creditCard.ifPresent(
                card -> checkoutComCustomerAccountServiceAdapter.deleteCCPaymentInfo(currentCustomer, card));
        }
        userFacade.removeCCPaymentInfo(id);
    }

    @Override
    public void updateCreditCardDetails(final String code, final CCPaymentInfoData ccPaymentInfoData) {
        if (checkoutComMerchantConfigurationService.isNasUsed()) {
            validateParameterNotNullStandardMessage("code", code);
            final CustomerModel currentCustomer = (CustomerModel) userService.getCurrentUser();
            final Optional<CreditCardPaymentInfoModel> creditCard =
                getCreditCardForCustomer(code, currentCustomer);
            creditCard.ifPresent(
                card -> checkoutComCustomerAccountServiceAdapter.updateCreditCardDetails(currentCustomer, card));
        }
    }

    @Override
    public void updateCCPaymentInfo(final CCPaymentInfoData paymentInfo) {
        if (checkoutComMerchantConfigurationService.isNasUsed()) {
            validateParameterNotNullStandardMessage("paymentInfo", paymentInfo);
            validateParameterNotNullStandardMessage("paymentInfoID", paymentInfo.getId());
            final CustomerModel currentCustomer = (CustomerModel) userService.getCurrentUser();
            final Optional<CreditCardPaymentInfoModel> creditCardForCustomer =
                getCreditCardForCustomer(paymentInfo.getId(),
                                         currentCustomer);
            creditCardForCustomer.ifPresent(card -> {
                cardPaymentInfoReversePopulator.populate(paymentInfo, card);
                checkoutComCustomerAccountServiceAdapter.updateCreditCardDetails(currentCustomer,
                                                                                 card);
            });
        }
        userFacade.updateCCPaymentInfo(paymentInfo);
    }

    @Override
    public void unlinkCCPaymentInfo(final String id) {
        userFacade.unlinkCCPaymentInfo(id);
    }

    @Override
    public void setDefaultPaymentInfo(final CCPaymentInfoData paymentInfo) {
        userFacade.setDefaultPaymentInfo(paymentInfo);
    }

	@Override
	public void syncSessionLanguage() {
		userFacade.syncSessionLanguage();
	}

	@Override
	public void syncSessionCurrency() {
		userFacade.syncSessionCurrency();
	}

	@Override
	public boolean isAnonymousUser() {
		return userFacade.isAnonymousUser();
	}

	@Override
	public boolean isUserExisting(final String id) {
		return userFacade.isUserExisting(id);
	}

	@Override
	public String getUserUID(final String id) {
		return userFacade.getUserUID(id);
	}

	@Override
	public void setCurrentUser(final String id) {
		userFacade.setCurrentUser(id);
	}

	@Override
	public List<TitleData> getTitles() {
		return userFacade.getTitles();
	}

	@Override
	public boolean isAddressBookEmpty() {
		return userFacade.isAddressBookEmpty();
	}

	@Override
	public List<AddressData> getAddressBook() {
		return userFacade.getAddressBook();
	}

	@Override
	public void addAddress(final AddressData addressData) {
		userFacade.addAddress(addressData);
	}

	@Override
	public void removeAddress(final AddressData addressData) {
		userFacade.removeAddress(addressData);
	}

	@Override
	public void editAddress(final AddressData addressData) {
		userFacade.editAddress(addressData);
	}

	@Override
	public AddressData getDefaultAddress() {
		return userFacade.getDefaultAddress();
	}

	@Override
	public void setDefaultAddress(final AddressData addressData) {
		userFacade.setDefaultAddress(addressData);
	}

	@Override
	public AddressData getAddressForCode(final String code) {
		return userFacade.getAddressForCode(code);
	}

	@Override
	public boolean isDefaultAddress(final String addressId) {
		return userFacade.isDefaultAddress(addressId);
	}

	@Override
	public List<CCPaymentInfoData> getCCPaymentInfos(final boolean saved) {
		return userFacade.getCCPaymentInfos(saved);
	}

	@Override
	public CCPaymentInfoData getCCPaymentInfoForCode(final String code) {
		return userFacade.getCCPaymentInfoForCode(code);
	}


	protected Optional<CreditCardPaymentInfoModel> getCreditCardForCustomer(final String code,
																			final CustomerModel currentCustomer) {
		return checkoutComCustomerAccountServiceAdapter.getCreditCardPaymentInfos(currentCustomer, false).stream()
													   .filter(creditCard -> creditCard.getPk().toString()
																					   .equals(code))
													   .findAny();
	}
}
