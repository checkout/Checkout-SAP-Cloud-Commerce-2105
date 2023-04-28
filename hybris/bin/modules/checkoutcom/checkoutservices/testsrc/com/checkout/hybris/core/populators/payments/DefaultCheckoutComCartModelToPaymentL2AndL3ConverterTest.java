package com.checkout.hybris.core.populators.payments;

import com.checkout.hybris.core.currency.services.CheckoutComCurrencyService;
import com.checkout.payments.PaymentRequest;
import com.checkout.payments.Product;
import com.checkout.payments.RequestSource;
import com.checkout.payments.TokenSource;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.CartEntryModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.product.UnitModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.util.TaxValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComCartModelToPaymentL2AndL3ConverterTest {

	private static final String USD = "USD";
	private static final double TOTAL_PRICE = 5000.0;
	private static final long TOTAL_PRICE_WITH_PENNIES = 500000L;
	private static final double TOTAL_TAX = 10.0;
	private static final long TOTAL_TAX_WITH_PENNIES = 10000L;
	private static final String CART_CODE = "0000001";
	private static final String TAX_NUMBER = "123456789";
	private static final String SONY_A1_NAME = "Sony A1";
	private static final String CANON_R6_NAME = "Canon R6";
	private static final String USA = "US";
	private static final String POSTAL_CODE = "NY500";
	private static final String STK = "STK";
	private static final String SONY_A1_CODE = "sony-00001";
	private static final String CANON_R6_CODE = "canon-992322";
	private static final double SHIPPING_AMOUNT = 2.99D;
	private static final double DISCOUNTED_AMOUNT = 5.99D;
	private static final long DISCOUNTED_AMOUNT_WITH_PENNIES = 59900L;
	private static final long SHIPPING_AMOUNT_WITH_PENNIES = 29900L;
	private static final long SONY_A1_QUANTITY = 2L;
	private static final double SONY_A1_PRICE_TOTAL = 2000.0D;
	private static final double SONY_A1_BASE_PRICE = 1000.D;
	private static final double SONY_A1_TAXES = 2.0D;
	private static final long SONY_A1_PRICE_TOTAL_WITH_PENNIES = 200000L;
	private static final long SONY_A1_BASE_PRICE_WITH_PENNIES = 100000L;
	private static final long SONY_A1_TAXES_WITH_PENNIES = 200L;
	private static final long CANON_R6_QUANTITY = 3L;
	private static final double CANON_R6_TAXES = 4.0D;
	private static final long CANON_R6_TAXES_WITH_PENNIES = 400L;
	private static final double CANON_R6_PRICE_TOTAL = 4500.0D;
	private static final Long CANON_R6_PRICE_TOTAL_WITH_PENNIES = 450000L;
	private static final double CANON_R6_BASE_PRICE = 1500.D;

	private static final Long CANON_R6_BASE_PRICE_WITH_PENNIES = 150000L;


	@Spy
	@InjectMocks
	private DefaultCheckoutComCartModelToPaymentL2AndL3Converter testObj;

	private PaymentRequest<RequestSource> paymentRequest;

	@Mock
	private CheckoutComCurrencyService checkoutComCurrencyServiceMock;


	private final CartModel cartModel = new CartModel();

	@Spy
	private CurrencyModel currencyModel;
	@Spy
	private CustomerModel customerModel;

	@Spy
	private AddressModel deliveryAddressModel;

	@Spy
	private CountryModel countryModel;

	@Spy
	private UnitModel unitModel;

	@Before
	public void setUp() throws Exception {
		paymentRequest = PaymentRequest.fromSource(new TokenSource("cardToken"), USD, TOTAL_PRICE_WITH_PENNIES);
	}

	@Test
	public void convert_shouldCallPopulateL2LevelFieldsAndPopulateL3Fields() {
		doNothing().when(testObj).populateL2Fields(cartModel, paymentRequest);
		doNothing().when(testObj).populateL3Fields(cartModel, paymentRequest);

		testObj.convert(cartModel, paymentRequest);

		verify(testObj).populateL2Fields(cartModel, paymentRequest);
		verify(testObj).populateL3Fields(cartModel, paymentRequest);
	}

	@Test
	public void populateL2LevelFields_shouldPopulateL2Fields() {
		populateCartFields(cartModel, CART_CODE, USD, USA, POSTAL_CODE, TOTAL_PRICE, SHIPPING_AMOUNT,
						   DISCOUNTED_AMOUNT,
						   TOTAL_TAX,
						   TAX_NUMBER);

		mockCheckoutComCurrencyServiceCallToPennies(USD, TOTAL_PRICE_WITH_PENNIES, TOTAL_PRICE_WITH_PENNIES);
		mockCheckoutComCurrencyServiceCallToPennies(USD, TOTAL_TAX, TOTAL_TAX_WITH_PENNIES);
		mockCheckoutComCurrencyServiceCallToPennies(USD, SHIPPING_AMOUNT, SHIPPING_AMOUNT_WITH_PENNIES);
		mockCheckoutComCurrencyServiceCallToPennies(USD, DISCOUNTED_AMOUNT, DISCOUNTED_AMOUNT_WITH_PENNIES);

		testObj.populateL2Fields(cartModel, paymentRequest);

		assertThat(paymentRequest.getProcessing().getOrderId()).isEqualTo(CART_CODE);
		assertThat(paymentRequest.getProcessing().getTaxAmount()).isEqualTo(TOTAL_TAX_WITH_PENNIES);
		assertThat(paymentRequest.getAmount()).isEqualTo(TOTAL_PRICE_WITH_PENNIES);
		assertThat(paymentRequest.getCurrency()).isEqualTo(USD);
		assertThat(paymentRequest.getCustomer().getTaxNumber()).isEqualTo(TAX_NUMBER);
	}

	@Test
	public void populateL3LevelFields_shouldPopulateL3Fields() {
		populateCartFields(cartModel, CART_CODE, USD, USA, POSTAL_CODE, TOTAL_PRICE, SHIPPING_AMOUNT,
						   DISCOUNTED_AMOUNT,
						   TOTAL_TAX, TAX_NUMBER);

		mockCheckoutComCurrencyServiceCallToPennies(USD, TOTAL_PRICE_WITH_PENNIES, TOTAL_PRICE_WITH_PENNIES);
		mockCheckoutComCurrencyServiceCallToPennies(USD, TOTAL_TAX, TOTAL_TAX_WITH_PENNIES);
		mockCheckoutComCurrencyServiceCallToPennies(USD, SHIPPING_AMOUNT, SHIPPING_AMOUNT_WITH_PENNIES);
		mockCheckoutComCurrencyServiceCallToPennies(USD, DISCOUNTED_AMOUNT, DISCOUNTED_AMOUNT_WITH_PENNIES);

		testObj.populateL3Fields(cartModel, paymentRequest);

		assertThat(paymentRequest.getShipping().getAddress().getCountry()).isEqualTo(USA);
		assertThat(paymentRequest.getShipping().getAddress().getZip()).isEqualTo(POSTAL_CODE);
		assertThat(paymentRequest.getProcessing().getDiscountAmount()).isEqualTo(DISCOUNTED_AMOUNT_WITH_PENNIES);
		assertThat(paymentRequest.getProcessing().getShippingAmount()).isEqualTo(SHIPPING_AMOUNT_WITH_PENNIES);
		assertItemIsEqual(paymentRequest.getItems().get(0), SONY_A1_NAME, SONY_A1_CODE, (int) SONY_A1_QUANTITY,
						  SONY_A1_PRICE_TOTAL_WITH_PENNIES, SONY_A1_BASE_PRICE_WITH_PENNIES, STK,
						  SONY_A1_TAXES_WITH_PENNIES);
		assertItemIsEqual(paymentRequest.getItems().get(1), CANON_R6_NAME, CANON_R6_CODE, (int) CANON_R6_QUANTITY,
						  CANON_R6_PRICE_TOTAL_WITH_PENNIES, CANON_R6_BASE_PRICE_WITH_PENNIES, STK,
						  CANON_R6_TAXES_WITH_PENNIES);
	}

	private void assertItemIsEqual(final Product product, final String productName, final String productCode,
								   final int quantity, final Long totalPrice, final Long basePrice,
								   final String unitOfMeasure,
								   final Long taxPrice) {
		assertThat(product.getName()).isEqualTo(productName);
		assertThat(product.getCommodityCode()).isEqualTo(productCode);
		assertThat(product.getReference()).isEqualTo(productCode);
		assertThat(product.getUnitOfMeasure()).isEqualTo(unitOfMeasure);
		assertThat(product.getQuantity()).isEqualTo(quantity);
		assertThat(product.getTotalAmount()).isEqualTo(totalPrice);
		assertThat(product.getUnitPrice()).isEqualTo(basePrice);
		assertThat(product.getTaxAmount()).isEqualTo(taxPrice);
	}


	private void populateCartFields(final CartModel cartModel,
									final String cartCode,
									final String currencyIsocode,
									final String deliveryAddressCountryCode,
									final String postalCode,
									final double totalPrice,
									final double shippingAmount,
									final double discountedAmount,
									final double totalTax,
									final String taxNumber) {
		mockDeliverAddressCountryCode(deliveryAddressModel, deliveryAddressCountryCode);
		deliveryAddressModel.setPostalcode(postalCode);
		mockCurrencyIsocode(currencyModel, currencyIsocode);
		populateCustomerFields(customerModel, taxNumber);
		populateCartEntries(cartModel);
		cartModel.setCurrency(currencyModel);
		cartModel.setUser(customerModel);
		cartModel.setTotalPrice(totalPrice);
		cartModel.setTotalTax(totalTax);
		cartModel.setTotalDiscounts(discountedAmount);
		cartModel.setDeliveryCost(shippingAmount);
        cartModel.setCheckoutComPaymentReference(cartCode);
	}

	private void mockDeliverAddressCountryCode(final AddressModel deliveryAddressModel, final String isocode) {
		cartModel.setDeliveryAddress(deliveryAddressModel);
		doReturn(countryModel).when(deliveryAddressModel).getCountry();
		doReturn(isocode).when(countryModel).getIsocode();
	}

	private void populateCartEntries(final CartModel cartModel) {
		final CartEntryModel cartEntryModel1 = createCartEntryModel(SONY_A1_NAME, SONY_A1_CODE, SONY_A1_QUANTITY,
																	SONY_A1_PRICE_TOTAL, SONY_A1_BASE_PRICE,
																	SONY_A1_TAXES,
																	cartModel,
																	STK);
		mockCheckoutComCurrencyServiceCallToPennies(USD, SONY_A1_PRICE_TOTAL, SONY_A1_PRICE_TOTAL_WITH_PENNIES);
		mockCheckoutComCurrencyServiceCallToPennies(USD, SONY_A1_BASE_PRICE, SONY_A1_BASE_PRICE_WITH_PENNIES);
		mockCheckoutComCurrencyServiceCallToPennies(USD, SONY_A1_TAXES, SONY_A1_TAXES_WITH_PENNIES);
		final CartEntryModel cartEntryModel2 = createCartEntryModel(CANON_R6_NAME, CANON_R6_CODE, CANON_R6_QUANTITY,
																	CANON_R6_PRICE_TOTAL,
																	CANON_R6_BASE_PRICE, CANON_R6_TAXES,
																	cartModel, STK);
		mockCheckoutComCurrencyServiceCallToPennies(USD, CANON_R6_PRICE_TOTAL, CANON_R6_PRICE_TOTAL_WITH_PENNIES);
		mockCheckoutComCurrencyServiceCallToPennies(USD, CANON_R6_BASE_PRICE, CANON_R6_BASE_PRICE_WITH_PENNIES);
		mockCheckoutComCurrencyServiceCallToPennies(USD, CANON_R6_TAXES, CANON_R6_TAXES_WITH_PENNIES);
		cartModel.setEntries(List.of(cartEntryModel1, cartEntryModel2));
	}

	private CartEntryModel createCartEntryModel(final String productName, final String productCode,
												final Long quantity,
												final double price,
												final double basePrice,
												final double taxes, final CartModel cartModel,
												final String unit) {
		final CartEntryModel cartEntryModel = Mockito.spy(CartEntryModel.class);
		cartEntryModel.setProduct(createProductModel(productName, productCode));
		cartEntryModel.setQuantity(quantity);
		cartEntryModel.setTotalPrice(price);
		cartEntryModel.setBasePrice(basePrice);
		doReturn(List.of(createTaxValueModel(taxes))).when(cartEntryModel).getTaxValues();
		cartEntryModel.setOrder(cartModel);
		unitModel.setCode(unit);
		cartEntryModel.setUnit(unitModel);

		return cartEntryModel;
	}

	private TaxValue createTaxValueModel(final double taxes) {
		final TaxValue taxValue = new TaxValue("taxCode", taxes, false, "taxName");
		return taxValue;
	}

	private ProductModel createProductModel(final String productName, final String code) {
		final ProductModel productModel = Mockito.spy(ProductModel.class);
		doReturn(productName).when(productModel).getName();
		productModel.setCode(code);
		return productModel;
	}

	private void mockCurrencyIsocode(final CurrencyModel currencyModel, final String currencyIsocode) {
		when(currencyModel.getIsocode()).thenReturn(currencyIsocode);
	}

	private void populateCustomerFields(final CustomerModel userModel, final String taxNumber) {
		userModel.setTaxNumber(taxNumber);
	}

	private void mockCheckoutComCurrencyServiceCallToPennies(final String currency, final double originAmount,
															 final Long targetAmount) {
		when(checkoutComCurrencyServiceMock.convertAmountIntoPennies(currency, originAmount)).thenReturn(targetAmount);
	}

}
