package com.checkout.hybris.commercefacades.user.impl;

import com.checkout.hybris.commerceservices.customer.CheckoutComCustomerAccountServiceAdapter;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.TitleData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.order.payment.CreditCardPaymentInfoModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.user.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComUserFacadeTest {

    private static final String PAYMENT_INFO_CODE1 = "809891232";
    private static final String PAYMENT_INFO_CODE2 = "90232232";
    @InjectMocks
    private DefaultCheckoutComUserFacade testObj;

    @Mock
    private UserFacade userFacadeMock;

    @Mock
    private UserService userServiceMock;

    @Mock
    private CheckoutComCustomerAccountServiceAdapter checkoutComCustomerAccountServiceAdapterMock;

    @Mock
    private Populator<CCPaymentInfoData, CreditCardPaymentInfoModel> cardPaymentInfoReversePopulatorMock;

    @Mock
    private CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationServiceMock;


    private final CustomerModel currentCustomer = new CustomerModel();
    @Mock
    private CreditCardPaymentInfoModel creditCardPaymentInfoModelMock;
    private final CCPaymentInfoData ccPaymentInfoData = new CCPaymentInfoData();

    @Before
    public void setUp() throws Exception {
        when(checkoutComMerchantConfigurationServiceMock.isNasUsed()).thenReturn(true);
    }

    @Test
    public void removeCCPaymentInfo_shouldCallCheckoutComCustomerAccountServiceAdapterAndRemoveCCPaymentInfo_WhenCreditCardExistsForCurrentCustomer() {
        ensureCurrentUserExists();
        ensureSavedCreditCardsExistsForCurrentCustomer();
        setCreditCardCode(PAYMENT_INFO_CODE1);
        ensureDeletePaymentDoesNothingWhenCalled();

        testObj.removeCCPaymentInfo(PAYMENT_INFO_CODE1);

        verify(checkoutComCustomerAccountServiceAdapterMock).deleteCCPaymentInfo(currentCustomer,
                                                                                 creditCardPaymentInfoModelMock);
        verify(userFacadeMock).removeCCPaymentInfo(PAYMENT_INFO_CODE1);
	}

	@Test
	public void removeCCPaymentInfo_shouldNotCallCheckoutComCustomerAccountServiceAdapterAndCallRemoveCCPaymentInfo_WhenCreditCardDoesNotExistsForCurrentCustomer() {
		ensureCurrentUserExists();
		ensureSavedCreditCardsExistsForCurrentCustomer();
		setCreditCardCode(PAYMENT_INFO_CODE2);
		ensureDeletePaymentDoesNothingWhenCalled();

		testObj.removeCCPaymentInfo(PAYMENT_INFO_CODE1);

		verify(checkoutComCustomerAccountServiceAdapterMock, times(0)).deleteCCPaymentInfo(currentCustomer,
																						   creditCardPaymentInfoModelMock);
		verify(userFacadeMock).removeCCPaymentInfo(PAYMENT_INFO_CODE1);
	}

	@Test
	public void updateCreditCardDetails_shouldCallCheckoutComCustomerAccountServiceAdapter_WhenCreditCardExistsForCurrentCustomer() {
		ensureCurrentUserExists();
		ensureSavedCreditCardsExistsForCurrentCustomer();
		setCreditCardCode(PAYMENT_INFO_CODE1);
		ensureUpdateCreditCardDetailsDoesNothingWhenCalled();

		testObj.updateCreditCardDetails(PAYMENT_INFO_CODE1, ccPaymentInfoData);

		verify(checkoutComCustomerAccountServiceAdapterMock).updateCreditCardDetails(currentCustomer,
																					 creditCardPaymentInfoModelMock);
	}

	@Test
	public void updateCreditCardDetails_shouldNotCallCheckoutComCustomerAccountServiceAdapter_WhenCreditCardDoesNotExistsForCurrentCustomer() {
		ensureCurrentUserExists();
		ensureSavedCreditCardsExistsForCurrentCustomer();
		setCreditCardCode(PAYMENT_INFO_CODE2);
		ensureUpdateCreditCardDetailsDoesNothingWhenCalled();

		testObj.updateCreditCardDetails(PAYMENT_INFO_CODE1, ccPaymentInfoData);

		verify(checkoutComCustomerAccountServiceAdapterMock, times(0)).updateCreditCardDetails(currentCustomer,
																							   creditCardPaymentInfoModelMock);
	}

	@Test
	public void updateCCPaymentInfo_shouldCallPopulatorAndCheckoutComCustomerAccountServiceAdapterAndUserFacade_WhenCreditCardExistsForCurrentCustomer() {
		ensureCurrentUserExists();
		ensureSavedCreditCardsExistsForCurrentCustomer();
		setCreditCardCode(PAYMENT_INFO_CODE1);
		ensureReversePopulatorDoesNothingWhenCalled();
		ensureUpdateCreditCardDetailsDoesNothingWhenCalled();

		testObj.updateCCPaymentInfo(ccPaymentInfoData);

		verify(cardPaymentInfoReversePopulatorMock).populate(ccPaymentInfoData, creditCardPaymentInfoModelMock);
		verify(checkoutComCustomerAccountServiceAdapterMock).updateCreditCardDetails(currentCustomer,
																					 creditCardPaymentInfoModelMock);
		verify(userFacadeMock).updateCCPaymentInfo(ccPaymentInfoData);

	}

	@Test
	public void updateCCPaymentInfo_shouldNotCallPopulatorAndNotCallCheckoutComCustomerAccountServiceAdapterAndCallUserFacade_WhenCreditCardDoesNotExistsForCurrentCustomer() {
		ensureCurrentUserExists();
		ensureSavedCreditCardsExistsForCurrentCustomer();
		setCreditCardCode(PAYMENT_INFO_CODE2);
		ensureReversePopulatorDoesNothingWhenCalled();
		ensureUpdateCreditCardDetailsDoesNothingWhenCalled();

		ccPaymentInfoData.setId(PAYMENT_INFO_CODE1);
		testObj.updateCCPaymentInfo(ccPaymentInfoData);

		verifyZeroInteractions(cardPaymentInfoReversePopulatorMock);
		verify(checkoutComCustomerAccountServiceAdapterMock, times(0)).updateCreditCardDetails(currentCustomer,
																							   creditCardPaymentInfoModelMock);
		verify(userFacadeMock).updateCCPaymentInfo(ccPaymentInfoData);
	}

	@Test
	public void unlinkCCPaymentInfo_shouldCallUnlinkCCPaymentInfoW() {
		testObj.unlinkCCPaymentInfo(PAYMENT_INFO_CODE1);

		verify(userFacadeMock).unlinkCCPaymentInfo(PAYMENT_INFO_CODE1);
	}

	@Test
	public void setDefaultPaymentInfo_shouldCallSetDefaultPaymentInfo() {
		testObj.setDefaultPaymentInfo(ccPaymentInfoData);

		verify(userFacadeMock).setDefaultPaymentInfo(ccPaymentInfoData);
	}

	@Test
	public void syncSessionLanguage_shouldCallSyncSessionLanguage() {
		testObj.syncSessionLanguage();

		verify(userFacadeMock).syncSessionLanguage();
	}

	@Test
	public void syncSessionCurrency_shouldCallSyncSessionCurrency() {
		testObj.syncSessionCurrency();

		verify(userFacadeMock).syncSessionCurrency();
	}

	@Test
	public void isAnonymousUser_shouldCallIsAnonymousUser() {
		final Boolean isAnonymousUser = true;
		when(userFacadeMock.isAnonymousUser()).thenReturn(isAnonymousUser);

		final boolean result = testObj.isAnonymousUser();

		verify(userFacadeMock).isAnonymousUser();
		assertThat(result).isSameAs(result);
	}

	@Test
	public void isUserExisting_shouldCallIsUserExisting() {
		final String userId = "userId";
		final Boolean isUserExisting = true;
		when(userFacadeMock.isUserExisting(userId)).thenReturn(isUserExisting);

		final boolean result = testObj.isUserExisting(userId);

		verify(userFacadeMock).isUserExisting(userId);
		assertThat(result).isSameAs(isUserExisting);
	}

	@Test
	public void getUserUID_shouldCallGetUserUID() {
		final String userId = "userId";
		final String userUID = "userUID";
		when(userFacadeMock.getUserUID(userId)).thenReturn(userUID);

		final String result = testObj.getUserUID(userId);

		verify(userFacadeMock).getUserUID(userId);
		assertThat(userUID).isSameAs(result);
	}

	@Test
	public void setCurrentUser_shouldCallSetCurrentUser() {
		final String userId = "userId";

		testObj.setCurrentUser(userId);

		verify(userFacadeMock).setCurrentUser(userId);
	}

	@Test
	public void getTitles_shouldCallGetTitles() {
		final List<TitleData> theList = List.of();
		when(userFacadeMock.getTitles()).thenReturn(theList);

		final List<TitleData> result = testObj.getTitles();

		verify(userFacadeMock).getTitles();
		assertThat(result).isSameAs(theList);
	}

	@Test
	public void isAddressBookEmpty_shouldCallIsAddressBookEmpty() {
		final Boolean isAddressBookEmpty = true;
		when(userFacadeMock.isAddressBookEmpty()).thenReturn(isAddressBookEmpty);

		final boolean result = testObj.isAddressBookEmpty();

		verify(userFacadeMock).isAddressBookEmpty();
		assertThat(result).isSameAs(isAddressBookEmpty);
	}

	@Test
	public void getAddressBook_shouldCallGetAddressBook() {
		final List<AddressData> addressBooks = List.of();
		when(userFacadeMock.getAddressBook()).thenReturn(addressBooks);

		final List<AddressData> result = testObj.getAddressBook();

		verify(userFacadeMock).getAddressBook();
		assertThat(result).isSameAs(addressBooks);
	}

	@Test
	public void addAddress_shouldCallAddAddress() {
		final AddressData addressData = new AddressData();

		testObj.addAddress(addressData);

		verify(userFacadeMock).addAddress(addressData);
	}

	@Test
	public void removeAddress_shouldCallRemoveAddress() {
		final AddressData addressData = new AddressData();

		testObj.removeAddress(addressData);

		verify(userFacadeMock).removeAddress(addressData);
	}

	@Test
	public void editAddress_shouldCallEditAddress() {
		final AddressData addressData = new AddressData();

		testObj.editAddress(addressData);

		verify(userFacadeMock).editAddress(addressData);
	}

	@Test
	public void getDefaultAddress_shouldCallGetDefaultAddress() {
		final AddressData addressData = new AddressData();
		when(userFacadeMock.getDefaultAddress()).thenReturn(addressData);

		final AddressData result = testObj.getDefaultAddress();

		verify(userFacadeMock).getDefaultAddress();
		assertThat(result).isSameAs(addressData);
	}

	@Test
	public void setDefaultAddress_shouldCallSetDefaultAddress() {
		final AddressData addressData = new AddressData();

		testObj.setDefaultAddress(addressData);

		verify(userFacadeMock).setDefaultAddress(addressData);
	}

	@Test
	public void getAddressForCode_shouldCallGetAddressForCode() {
		final String code = "code";
		final AddressData addressData = new AddressData();
		when(userFacadeMock.getAddressForCode(code)).thenReturn(addressData);

		final AddressData result = testObj.getAddressForCode(code);

		verify(userFacadeMock).getAddressForCode(code);
		assertThat(result).isEqualTo(addressData);
	}

	@Test
	public void isDefaultAddress_shouldCallIsDefaultAddress() {
		final String addressId = "addressId";
		final Boolean isDefaultAddress = true;
		when(userFacadeMock.isDefaultAddress(addressId)).thenReturn(isDefaultAddress);

		final boolean result = testObj.isDefaultAddress(addressId);

		verify(userFacadeMock).isDefaultAddress(addressId);
		assertThat(result).isSameAs(isDefaultAddress);
	}

	@Test
	public void getCCPaymentInfos_shouldCallGetCCPaymentInfos() {
		final boolean saved = true;
		final List<CCPaymentInfoData> paymentInfos = List.of();
		when(userFacadeMock.getCCPaymentInfos(saved)).thenReturn(paymentInfos);

		final List<CCPaymentInfoData> result = testObj.getCCPaymentInfos(saved);

		verify(userFacadeMock).getCCPaymentInfos(saved);
		assertThat(result).isSameAs(paymentInfos);
	}

	@Test
	public void getCCPaymentInfoForCode_shouldCallGetCCPaymentInfoForCode() {
		final String code = "code";
		final CCPaymentInfoData paymentInfoData = new CCPaymentInfoData();
		when(userFacadeMock.getCCPaymentInfoForCode(code)).thenReturn(paymentInfoData);

		final CCPaymentInfoData result = testObj.getCCPaymentInfoForCode(code);

		verify(userFacadeMock).getCCPaymentInfoForCode(code);
		assertThat(result).isSameAs(paymentInfoData);
	}

	private void ensureDeletePaymentDoesNothingWhenCalled() {
		doNothing().when(checkoutComCustomerAccountServiceAdapterMock).deleteCCPaymentInfo(currentCustomer,
																						   creditCardPaymentInfoModelMock);
	}

	private void setCreditCardCode(final String paymentInfoCode) {
		doReturn(PK.parse(paymentInfoCode)).when(creditCardPaymentInfoModelMock).getPk();
		ccPaymentInfoData.setId(paymentInfoCode);
	}

	private void ensureSavedCreditCardsExistsForCurrentCustomer() {
		when(checkoutComCustomerAccountServiceAdapterMock.getCreditCardPaymentInfos(currentCustomer, false)).thenReturn(
				List.of(creditCardPaymentInfoModelMock));
	}

	private void ensureCurrentUserExists() {
		when(userServiceMock.getCurrentUser()).thenReturn(currentCustomer);
	}

	private void ensureUpdateCreditCardDetailsDoesNothingWhenCalled() {
		doNothing().when(checkoutComCustomerAccountServiceAdapterMock)
				   .updateCreditCardDetails(currentCustomer, creditCardPaymentInfoModelMock);
	}

	private void ensureReversePopulatorDoesNothingWhenCalled() {
		doNothing().when(cardPaymentInfoReversePopulatorMock)
				   .populate(ccPaymentInfoData, creditCardPaymentInfoModelMock);
	}

}
