package com.checkout.hybris.commerceservices.customer.impl;

import com.checkout.hybris.commerceservices.customer.CheckoutComCustomerAccountServiceAdapter;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentInstrumentsService;
import de.hybris.platform.basecommerce.enums.ReturnStatus;
import de.hybris.platform.commerceservices.customer.CustomerAccountService;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.customer.PasswordMismatchException;
import de.hybris.platform.commerceservices.customer.TokenInvalidatedException;
import de.hybris.platform.commerceservices.search.pagedata.PageableData;
import de.hybris.platform.commerceservices.search.pagedata.SearchPageData;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.CreditCardPaymentInfoModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.TitleModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.payment.dto.BillingInfo;
import de.hybris.platform.payment.dto.CardInfo;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.store.BaseStoreModel;

import java.util.Collection;
import java.util.List;

import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;

public class DefaultCheckoutComCustomerAccountServiceAdapter implements CheckoutComCustomerAccountServiceAdapter {


	protected final CustomerAccountService customerAccountService;
	protected final CheckoutComPaymentInstrumentsService checkoutComPaymentInstrumentsService;


	public DefaultCheckoutComCustomerAccountServiceAdapter(final CustomerAccountService customerAccountService,
														   final CheckoutComPaymentInstrumentsService checkoutComPaymentInstrumentsService) {
		this.customerAccountService = customerAccountService;
		this.checkoutComPaymentInstrumentsService = checkoutComPaymentInstrumentsService;
	}

	@Override
	public CreditCardPaymentInfoModel createPaymentSubscription(final CustomerModel customerModel,
																final CardInfo cardInfo,
																final BillingInfo billingInfo, final String titleCode,
																final String paymentProvider,
																final boolean saveInAccount) {
		return customerAccountService.createPaymentSubscription(customerModel, cardInfo, billingInfo, titleCode,
																paymentProvider, saveInAccount);
	}

	@Override
	public void setDefaultPaymentInfo(final CustomerModel customerModel, final PaymentInfoModel paymentInfoModel) {
		customerAccountService.setDefaultPaymentInfo(customerModel, paymentInfoModel);
	}

	@Override
	public List<CreditCardPaymentInfoModel> getCreditCardPaymentInfos(final CustomerModel customerModel,
																	  final boolean saved) {
		return customerAccountService.getCreditCardPaymentInfos(customerModel, saved);
	}

	@Override
	public CreditCardPaymentInfoModel getCreditCardPaymentInfoForCode(final CustomerModel customerModel,
																	  final String code) {
		return customerAccountService.getCreditCardPaymentInfoForCode(customerModel, code);
	}

	@Override
	public void deleteCCPaymentInfo(final CustomerModel customerModel,
									final CreditCardPaymentInfoModel creditCardPaymentInfo) {
		checkoutComPaymentInstrumentsService.removeInstrumentByCreditCard(creditCardPaymentInfo);
		customerAccountService.deleteCCPaymentInfo(customerModel, creditCardPaymentInfo);
	}

	@Override
	public void unlinkCCPaymentInfo(final CustomerModel customerModel,
									final CreditCardPaymentInfoModel creditCardPaymentInfo) {
		checkoutComPaymentInstrumentsService.removeInstrumentByCreditCard(creditCardPaymentInfo);
		customerAccountService.unlinkCCPaymentInfo(customerModel, creditCardPaymentInfo);
	}

	@Override
	public Collection<TitleModel> getTitles() {
		return customerAccountService.getTitles();
	}

	@Override
	public List<AddressModel> getAllAddressEntries(final CustomerModel customerModel) {
		return customerAccountService.getAllAddressEntries(customerModel);
	}

	@Override
	public List<AddressModel> getAddressBookEntries(final CustomerModel customerModel) {
		return customerAccountService.getAddressBookEntries(customerModel);
	}

	@Override
	public List<AddressModel> getAddressBookDeliveryEntries(final CustomerModel customerModel) {
		return customerAccountService.getAddressBookDeliveryEntries(customerModel);
	}

	@Override
	public AddressModel getAddressForCode(final CustomerModel customerModel, final String code) {
		return customerAccountService.getAddressForCode(customerModel, code);
	}

	@Override
	public AddressModel getDefaultAddress(final CustomerModel customerModel) {
		return customerAccountService.getDefaultAddress(customerModel);
	}

	@Override
	public void saveAddressEntry(final CustomerModel customerModel, final AddressModel addressModel) {
		customerAccountService.saveAddressEntry(customerModel, addressModel);
	}

	@Override
	public void deleteAddressEntry(final CustomerModel customerModel, final AddressModel addressModel) {
		customerAccountService.deleteAddressEntry(customerModel, addressModel);
	}

	@Override
	public void setDefaultAddressEntry(final CustomerModel customerModel, final AddressModel addressModel) {
		customerAccountService.setDefaultAddressEntry(customerModel, addressModel);
	}

	@Override
	public void clearDefaultAddressEntry(final CustomerModel customerModel) {
		customerAccountService.clearDefaultAddressEntry(customerModel);
	}

	@Override
	public void register(final CustomerModel customerModel, final String password) throws DuplicateUidException {
		customerAccountService.register(customerModel, password);
	}

	@Override
	public void updateProfile(final CustomerModel customerModel, final String titleCode, final String name,
							  final String login) throws DuplicateUidException {
		customerAccountService.updateProfile(customerModel, titleCode, name, login);
	}

	@Override
	public void changePassword(final UserModel userModel, final String oldPassword, final String newPassword) throws PasswordMismatchException {
		customerAccountService.changePassword(userModel, oldPassword, newPassword);
	}

	@Override
	public void forgottenPassword(final CustomerModel customerModel) {
		customerAccountService.forgottenPassword(customerModel);
	}

	@Override
	public void updatePassword(final String token, final String newPassword) throws TokenInvalidatedException {
		customerAccountService.updatePassword(token, newPassword);
	}

	@Override
	public OrderModel getOrderForCode(final CustomerModel customerModel, final String code,
									  final BaseStoreModel store) {
		return customerAccountService.getOrderForCode(customerModel, code, store);
	}

	@Override
	public List<OrderModel> getOrderList(final CustomerModel customerModel, final BaseStoreModel store,
										 final OrderStatus[] status) {
		return customerAccountService.getOrderList(customerModel, store, status);
	}

	@Override
	public SearchPageData<OrderModel> getOrderList(final CustomerModel customerModel, final BaseStoreModel store,
												   final OrderStatus[] status, final PageableData pageableData) {
		return customerAccountService.getOrderList(customerModel, store, status, pageableData);
	}

	@Override
	public SearchPageData<ReturnRequestModel> getReturnRequestsByCustomerAndStore(final CustomerModel customerModel,
																				  final BaseStoreModel store,
																				  final ReturnStatus[] returnStatuses,
																				  final PageableData pageableData) {
		return customerAccountService.getReturnRequestsByCustomerAndStore(customerModel, store, returnStatuses,
																		  pageableData);
	}

	@Override
	public void changeUid(final String newUid, final String currentPassword) throws DuplicateUidException,
			PasswordMismatchException {
		customerAccountService.changeUid(newUid, currentPassword);
	}

	@Override
	public OrderModel getGuestOrderForGUID(final String guid, final BaseStoreModel store) {
		return customerAccountService.getGuestOrderForGUID(guid, store);
	}

	@Override
	public void registerGuestForAnonymousCheckout(final CustomerModel customerModel, final String password) throws DuplicateUidException {
		customerAccountService.registerGuestForAnonymousCheckout(customerModel, password);
	}

	@Override
	public OrderModel getOrderDetailsForGUID(final String guid, final BaseStoreModel store) {
		return customerAccountService.getGuestOrderForGUID(guid, store);
	}

	@Override
	public void convertGuestToCustomer(final String pwd, final String orderGUID) throws DuplicateUidException {
		customerAccountService.convertGuestToCustomer(pwd, orderGUID);
	}

	@Override
	public OrderModel getOrderForCode(final String code, final BaseStoreModel store) {
		return customerAccountService.getOrderForCode(code, store);
	}

	@Override
	public CustomerModel closeAccount(final CustomerModel user) {
		return customerAccountService.closeAccount(user);
	}

	@Override
	public void updateCreditCardDetails(final CustomerModel customerModel,
										final CreditCardPaymentInfoModel creditCardPaymentInfoModel) {
		validateParameterNotNull(customerModel, "Customer model cannot be null");
		validateParameterNotNull(creditCardPaymentInfoModel, "Credit card info model cannot be null");

		checkoutComPaymentInstrumentsService.updateInstrumentByCreditCard(creditCardPaymentInfoModel);
	}
}
