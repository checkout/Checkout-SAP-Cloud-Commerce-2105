package com.checkout.hybris.commerceservices.customer.impl;

import com.checkout.hybris.core.payment.services.CheckoutComPaymentInstrumentsService;
import de.hybris.bootstrap.annotations.UnitTest;
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
import de.hybris.platform.payment.dto.BillingInfo;
import de.hybris.platform.payment.dto.CardInfo;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.store.BaseStoreModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComCustomerAccountServiceAdapterTest {

	@InjectMocks
	private DefaultCheckoutComCustomerAccountServiceAdapter testObj;
	@Mock
	private CustomerAccountService customerAccountServiceMock;
	@Mock
	private CheckoutComPaymentInstrumentsService checkoutComPaymentInstrumentsServiceMock;
	private final CardInfo cardInfo = new CardInfo();
	private final CustomerModel customerModel = new CustomerModel();
	private final BillingInfo billingInfo = new BillingInfo();
	private final String titleCode = "titleCode";
	private final String paymentProvider = "paymentProvider";
	private final boolean saveInAccount = true;
	private final PaymentInfoModel paymentInfoModel = new PaymentInfoModel();


	private final CreditCardPaymentInfoModel creditCardPaymentInfoModel = new CreditCardPaymentInfoModel();

	private final List<CreditCardPaymentInfoModel> creditCardPaymentInfoModels = List.of(creditCardPaymentInfoModel);
	private final String code = "code";
	private final AddressModel addressModel = new AddressModel();

	private final List<AddressModel> addressModels = List.of(addressModel);
	private final String password = "password";
	private final String name = "name";
	private final String login = "login";
	private final String newPasword = "newPasword";
	private final String token = "token";
	private final BaseStoreModel baseStore = new BaseStoreModel();
	private final OrderModel order = new OrderModel();
	private final OrderStatus[] orderStatuses = new OrderStatus[]{OrderStatus.COMPLETED};
	private final List<OrderModel> orderModels = List.of();
	private final PageableData pageableData = new PageableData();

	final SearchPageData<OrderModel> orderModelSearchPageData = new SearchPageData<>();
	private final SearchPageData<ReturnRequestModel> returnRequestSearchPageData = new SearchPageData<>();
	private final String newUid = "newUid";
	private final String guid = "guid";

	@Test
	public void createPaymentSubscription_shouldCallCustomerAccountServiceMethod() {
		final CreditCardPaymentInfoModel creditCardPaymentInfo = new CreditCardPaymentInfoModel();
		when(customerAccountServiceMock.createPaymentSubscription(customerModel, cardInfo,
																  billingInfo, titleCode,
																  paymentProvider,
																  saveInAccount)).thenReturn(creditCardPaymentInfo);

		final CreditCardPaymentInfoModel result = testObj.createPaymentSubscription(customerModel, cardInfo,
																					billingInfo, titleCode,
																					paymentProvider,
																					saveInAccount);

		assertThat(result).isSameAs(creditCardPaymentInfo);
		verify(customerAccountServiceMock).createPaymentSubscription(customerModel, cardInfo,
																	 billingInfo, titleCode,
																	 paymentProvider,
																	 saveInAccount);
	}

	@Test
	public void setDefaultPaymentInfo_shouldCallCustomerAccountServiceMethod() {
		testObj.setDefaultPaymentInfo(customerModel, paymentInfoModel);

		verify(customerAccountServiceMock).setDefaultPaymentInfo(customerModel, paymentInfoModel);
	}

	@Test
	public void getCreditCardPaymentInfos_shouldCallCustomerAccountServiceMethod() {
		when(customerAccountServiceMock.getCreditCardPaymentInfos(customerModel, saveInAccount)).thenReturn(
				creditCardPaymentInfoModels);

		final List<CreditCardPaymentInfoModel> result = testObj.getCreditCardPaymentInfos(customerModel,
																						  saveInAccount);

		assertThat(result).isEqualTo(creditCardPaymentInfoModels);
		verify(customerAccountServiceMock).getCreditCardPaymentInfos(customerModel, saveInAccount);
	}

	@Test
	public void getCreditCardPaymentInfoForCode_shouldCallCustomerAccountServiceMethod() {
		when(customerAccountServiceMock.getCreditCardPaymentInfoForCode(customerModel, code)).thenReturn(
				creditCardPaymentInfoModel);

		final CreditCardPaymentInfoModel result = testObj.getCreditCardPaymentInfoForCode(customerModel, code);

		assertThat(result).isSameAs(creditCardPaymentInfoModel);
		verify(customerAccountServiceMock).getCreditCardPaymentInfoForCode(customerModel, code);
	}

	@Test
	public void deleteCCPaymentInfo_shouldCallCustomerAccountServiceMethodAndCheckoutComPaymentInstrumentsService() {
		testObj.deleteCCPaymentInfo(customerModel, creditCardPaymentInfoModel);

		verify(checkoutComPaymentInstrumentsServiceMock).removeInstrumentByCreditCard(creditCardPaymentInfoModel);
		verify(customerAccountServiceMock).deleteCCPaymentInfo(customerModel, creditCardPaymentInfoModel);
	}

	@Test
	public void unlinkCCPaymentInfo_shouldCallCustomerAccountServiceMethodAndCheckoutComPaymentInstrumentsService() {
		testObj.unlinkCCPaymentInfo(customerModel, creditCardPaymentInfoModel);

		verify(checkoutComPaymentInstrumentsServiceMock).removeInstrumentByCreditCard(creditCardPaymentInfoModel);
		verify(customerAccountServiceMock).unlinkCCPaymentInfo(customerModel, creditCardPaymentInfoModel);
	}

	@Test
	public void getTitles_shouldCallCustomerAccountServiceMethod() {
		final List<TitleModel> titleModels = List.of();
		when(customerAccountServiceMock.getTitles()).thenReturn(titleModels);

		final Collection<TitleModel> result = testObj.getTitles();

		verify(customerAccountServiceMock).getTitles();
		assertThat(result).isSameAs(titleModels);
	}

	@Test
	public void getAllAddressEntries_shouldCallCustomerAccountServiceMethod() {
		when(customerAccountServiceMock.getAllAddressEntries(customerModel)).thenReturn(addressModels);

		final List<AddressModel> result = testObj.getAllAddressEntries(customerModel);

		assertThat(result).isSameAs(addressModels);
		verify(customerAccountServiceMock).getAllAddressEntries(customerModel);
	}

	@Test
	public void getAddressBookEntries_shouldCallCustomerAccountServiceMethod() {
		when(customerAccountServiceMock.getAddressBookEntries(customerModel)).thenReturn(addressModels);

		final List<AddressModel> result = testObj.getAddressBookEntries(customerModel);

		assertThat(result).isSameAs(addressModels);
		verify(customerAccountServiceMock).getAddressBookEntries(customerModel);
	}

	@Test
	public void getAddressBookDeliveryEntries_shouldCallCustomerAccountServiceMethod() {
		when(customerAccountServiceMock.getAddressBookDeliveryEntries(customerModel)).thenReturn(addressModels);

		final List<AddressModel> result = testObj.getAddressBookDeliveryEntries(customerModel);

		assertThat(result).isSameAs(addressModels);
		verify(customerAccountServiceMock).getAddressBookDeliveryEntries(customerModel);
	}

	@Test
	public void getAddressForCode_shouldCallCustomerAccountServiceMethod() {
		when(customerAccountServiceMock.getAddressForCode(customerModel, code)).thenReturn(addressModel);

		final AddressModel result = testObj.getAddressForCode(customerModel, code);

		assertThat(result).isSameAs(addressModel);
		verify(customerAccountServiceMock).getAddressForCode(customerModel, code);
	}

	@Test
	public void getDefaultAddress_shouldCallCustomerAccountServiceMethod() {
		when(customerAccountServiceMock.getDefaultAddress(customerModel)).thenReturn(addressModel);

		final AddressModel result = testObj.getDefaultAddress(customerModel);

		assertThat(result).isSameAs(addressModel);
		verify(customerAccountServiceMock).getDefaultAddress(customerModel);
	}

	@Test
	public void saveAddressEntry_shouldCallCustomerAccountServiceMethod() {
		testObj.saveAddressEntry(customerModel, addressModel);

		verify(customerAccountServiceMock).saveAddressEntry(customerModel, addressModel);
	}

	@Test
	public void deleteAddressEntry_shouldCallCustomerAccountServiceMethod() {
		testObj.deleteAddressEntry(customerModel, addressModel);

		verify(customerAccountServiceMock).deleteAddressEntry(customerModel, addressModel);
	}

	@Test
	public void setDefaultAddressEntry_shouldCallCustomerAccountServiceMethod() {
		testObj.setDefaultAddressEntry(customerModel, addressModel);

		verify(customerAccountServiceMock).setDefaultAddressEntry(customerModel, addressModel);
	}

	@Test
	public void clearDefaultAddressEntry_shouldCallCustomerAccountServiceMethod() {
		testObj.clearDefaultAddressEntry(customerModel);

		verify(customerAccountServiceMock).clearDefaultAddressEntry(customerModel);
	}

	@Test
	public void register_shouldCallCustomerAccountServiceMethod() throws DuplicateUidException {
		testObj.register(customerModel, password);

		verify(customerAccountServiceMock).register(customerModel, password);
	}

	@Test
	public void updateProfile_shouldCallCustomerAccountServiceMethod() throws DuplicateUidException {
		testObj.updateProfile(customerModel, titleCode, name, login);

		verify(customerAccountServiceMock).updateProfile(customerModel, titleCode, name, login);
	}

	@Test
	public void changePassword_shouldCallCustomerAccountServiceMethod() throws PasswordMismatchException {
		testObj.changePassword(customerModel, password, newPasword);

		verify(customerAccountServiceMock).changePassword(customerModel, password, newPasword);
	}

	@Test
	public void forgottenPassword_shouldCallCustomerAccountServiceMethod() {
		testObj.forgottenPassword(customerModel);

		verify(customerAccountServiceMock).forgottenPassword(customerModel);
	}

	@Test
	public void updatePassword_shouldCallCustomerAccountServiceMethod() throws TokenInvalidatedException {
		testObj.updatePassword(token, newPasword);

		verify(customerAccountServiceMock).updatePassword(token, newPasword);
	}

	@Test
	public void getOrderForCode_shouldCallCustomerAccountServiceMethod() {
		when(customerAccountServiceMock.getOrderForCode(customerModel, code, baseStore)).thenReturn(order);

		final OrderModel result = testObj.getOrderForCode(customerModel, code, baseStore);

		assertThat(result).isSameAs(order);
	}

	@Test
	public void getOrderList_shouldCallCustomerAccountServiceMethod() {
		when(customerAccountServiceMock.getOrderList(customerModel, baseStore, orderStatuses)).thenReturn(orderModels);

		final List<OrderModel> result = testObj.getOrderList(customerModel, baseStore, orderStatuses);

		assertThat(result).isEqualTo(orderModels);
		verify(customerAccountServiceMock).getOrderList(customerModel, baseStore, orderStatuses);
	}

	@Test
	public void getOrderListPageable_shouldCallCustomerAccountServiceMethod() {
		when(customerAccountServiceMock.getOrderList(customerModel, baseStore, orderStatuses, pageableData)).thenReturn(
				orderModelSearchPageData);


		final SearchPageData<OrderModel> result = testObj.getOrderList(customerModel, baseStore, orderStatuses,
																	   pageableData);

		assertThat(result).isEqualTo(orderModelSearchPageData);
		verify(customerAccountServiceMock).getOrderList(customerModel, baseStore, orderStatuses, pageableData);
	}

	@Test
	public void getReturnRequestsByCustomerAndStore_shouldCallCustomerAccountServiceMethod() {
		when(customerAccountServiceMock.getReturnRequestsByCustomerAndStore(
				customerModel, baseStore, new ReturnStatus[]{ReturnStatus.APPROVING}, pageableData)).thenReturn(
				returnRequestSearchPageData);

		final SearchPageData<ReturnRequestModel> result =
				testObj.getReturnRequestsByCustomerAndStore(
						customerModel, baseStore, new ReturnStatus[]{ReturnStatus.APPROVING}, pageableData);

		assertThat(result).isSameAs(returnRequestSearchPageData);
		verify(customerAccountServiceMock).getReturnRequestsByCustomerAndStore(
				customerModel, baseStore, new ReturnStatus[]{ReturnStatus.APPROVING}, pageableData);
	}

	@Test
	public void changeUid_shouldCallCustomerAccountServiceMethod() throws PasswordMismatchException,
			DuplicateUidException {
		testObj.changeUid(newUid, password);

		verify(customerAccountServiceMock).changeUid(newUid, password);
	}

	@Test
	public void getGuestOrderForGUID_shouldCallCustomerAccountServiceMethod() {
		when(customerAccountServiceMock.getGuestOrderForGUID(guid, baseStore)).thenReturn(order);

		final OrderModel result = testObj.getGuestOrderForGUID(guid, baseStore);

		assertThat(result).isSameAs(order);
		verify(customerAccountServiceMock).getGuestOrderForGUID(guid, baseStore);
	}

	@Test
	public void registerGuestForAnonymousCheckout_shouldCallCustomerAccountServiceMethod() throws DuplicateUidException {
		testObj.registerGuestForAnonymousCheckout(customerModel, password);

		verify(customerAccountServiceMock).registerGuestForAnonymousCheckout(customerModel, password);
	}

	@Test
	public void getOrderDetailsForGUID_shouldCallCustomerAccountServiceMethod() {
		when(customerAccountServiceMock.getGuestOrderForGUID(guid, baseStore)).thenReturn(order);

		final OrderModel result = testObj.getOrderDetailsForGUID(guid, baseStore);

		assertThat(result).isSameAs(order);
		verify(customerAccountServiceMock).getGuestOrderForGUID(guid, baseStore);
	}

	@Test
	public void convertGuestToCustomer_shouldCallCustomerAccountServiceMethod() throws DuplicateUidException {
		testObj.convertGuestToCustomer(password, guid);

		verify(customerAccountServiceMock).convertGuestToCustomer(password, guid);
	}

	@Test
	public void getOrderForCodeAndStore_shouldCallCustomerAccountServiceMethod() {
		when(customerAccountServiceMock.getOrderForCode(code, baseStore)).thenReturn(order);

		final OrderModel result = testObj.getOrderForCode(code, baseStore);

		assertThat(result).isSameAs(order);
		verify(customerAccountServiceMock).getOrderForCode(code, baseStore);
	}

	@Test
	public void closeAccount_shouldCallCustomerAccountServiceMethod() {
		when(testObj.closeAccount(customerModel)).thenReturn(customerModel);

		final CustomerModel result = testObj.closeAccount(customerModel);

		assertThat(result).isSameAs(customerModel);
		verify(customerAccountServiceMock).closeAccount(customerModel);
	}

	@Test
	public void updateCreditCardDetails_shouldCheckoutComPaymentsInstrumentsServiceMethod() {
		testObj.updateCreditCardDetails(customerModel, creditCardPaymentInfoModel);

		verify(checkoutComPaymentInstrumentsServiceMock).updateInstrumentByCreditCard(creditCardPaymentInfoModel);
	}
}
