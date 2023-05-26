package com.checkout.hybris.core.apm.services.impl;

import com.checkout.hybris.addon.model.CheckoutComAPMComponentModel;
import com.checkout.hybris.core.apm.configuration.CheckoutComAPMConfigurationSettings;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComAPMConfigurationModel;
import com.checkout.hybris.core.model.CheckoutComFawryConfigurationModel;
import com.checkout.hybris.core.model.CheckoutComGlobalAPMConfigurationModel;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.media.MediaModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.internal.dao.GenericDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.checkout.common.Currency.EUR;
import static com.checkout.common.Currency.GBP;
import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.FAWRY;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Locale.FRANCE;
import static java.util.Locale.UK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class DefaultCheckoutComAPMConfigurationServiceTest {

    private static final String APM_CODE = "apmCode";
    private static final String CART_CURRENCY = "GBP";
    private static final String SHIPPING_ADDRESS_COUNTRY_CODE = "IT";
    private static final String BILLING_ADDRESS_COUNTRY_CODE = "UK";

    @InjectMocks
    private DefaultCheckoutComAPMConfigurationService testObj;

    @Mock
    private CartService cartServiceMock;
    @Mock
    private GenericDao<CheckoutComAPMComponentModel> checkoutComApmComponentDaoMock;
    @Mock
    private GenericDao<CheckoutComAPMConfigurationModel> checkoutComApmConfigurationDaoMock;
    @Mock
    private GenericDao<CheckoutComGlobalAPMConfigurationModel> globalAPMConfigurationDaoMock;
    @Mock
    private CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationServiceMock;
    @Mock
    private Map<String, CheckoutComAPMConfigurationSettings> checkoutComAPMConfigurationSettingsMock;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CartModel cartMock;
    @Mock
    private MediaModel mediaMock;
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private AddressModel paymentAddressMock, shippingAddressMock;
    @Mock
    private CheckoutComAPMComponentModel component1Mock, component2Mock;
    @Mock
    private CountryModel restrictedCountryMock;
    @Mock
    private CurrencyModel restrictedCurrencyMock;
    @Mock
    private CheckoutComFawryConfigurationModel fawryConfigurationModelMock;
    @Mock
    private CheckoutComAPMConfigurationSettings apmConfigurationSettingsMock;
    @Mock
    private CheckoutComGlobalAPMConfigurationModel checkoutComGlobalAPMConfigurationMock;
    @Mock
    private CheckoutComAPMConfigurationModel apmConfiguration1Mock, apmConfiguration2Mock;

    @Before
    public void setUp() {
        testObj = Mockito.spy(new DefaultCheckoutComAPMConfigurationService(cartServiceMock, checkoutComApmComponentDaoMock,
                checkoutComApmConfigurationDaoMock, globalAPMConfigurationDaoMock, checkoutComMerchantConfigurationServiceMock,
                checkoutComAPMConfigurationSettingsMock));

        when(restrictedCountryMock.getIsocode()).thenReturn(FRANCE.getCountry());
        when(restrictedCurrencyMock.getIsocode()).thenReturn(EUR);
        when(apmConfiguration1Mock.getRestrictedCountries()).thenReturn(ImmutableSet.of(restrictedCountryMock));
        when(apmConfiguration1Mock.getRestrictedCurrencies()).thenReturn(ImmutableSet.of(restrictedCurrencyMock));
        when(checkoutComAPMConfigurationSettingsMock.containsKey(APM_CODE)).thenReturn(Boolean.TRUE);
        when(checkoutComAPMConfigurationSettingsMock.get(APM_CODE)).thenReturn(apmConfigurationSettingsMock);

        when(cartServiceMock.getSessionCart()).thenReturn(cartMock);
        when(cartServiceMock.getSessionCart()).thenReturn(cartMock);
        when(cartMock.getCurrency().getIsocode()).thenReturn(CART_CURRENCY);
        when(cartMock.getPaymentAddress()).thenReturn(paymentAddressMock);
        when(cartMock.getDeliveryAddress()).thenReturn(shippingAddressMock);
        when(paymentAddressMock.getCountry().getIsocode()).thenReturn(BILLING_ADDRESS_COUNTRY_CODE);
        when(shippingAddressMock.getCountry().getIsocode()).thenReturn(BILLING_ADDRESS_COUNTRY_CODE);
        when(checkoutComApmComponentDaoMock.find()).thenReturn(List.of(component1Mock, component2Mock));
        when(component1Mock.getApmConfiguration()).thenReturn(apmConfiguration1Mock);
        when(component2Mock.getApmConfiguration()).thenReturn(apmConfiguration2Mock);
        when(component1Mock.getVisible()).thenReturn(Boolean.TRUE);
        when(component2Mock.getVisible()).thenReturn(Boolean.TRUE);
        when(checkoutComMerchantConfigurationServiceMock.isNasUsed()).thenReturn(true);
        when(globalAPMConfigurationDaoMock.find()).thenReturn(List.of(checkoutComGlobalAPMConfigurationMock));
    }

    @Test(expected = IllegalArgumentException.class)
    public void isApmAvailable_WhenCountryCodeIsEmpty_ShouldThrowException() {
        testObj.isApmAvailable(apmConfiguration1Mock, UK.getCountry(), "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void isApmAvailable_WhenCurrencyCodeIsEmpty_ShouldThrowException() {
        testObj.isApmAvailable(apmConfiguration1Mock, "", GBP);
    }

    @Test
    public void isApmAvailable_WhenApmConfigurationIsNull_ShouldReturnTrue() {
        final boolean result = testObj.isApmAvailable(null, UK.getCountry(), GBP);

        assertTrue(result);
    }

    @Test
    public void isApmAvailable_WhenSiteConfigurationIsEnabledForNAS_andAPMIsNot_ShouldReturnFalse() {
        when(checkoutComGlobalAPMConfigurationMock.getNasAPMs()).thenReturn(List.of());

        final boolean result = testObj.isApmAvailable(apmConfiguration1Mock, UK.getCountry(), GBP);

        assertFalse(result);
    }

    @Test
    public void isApmAvailable_WhenSiteConfigurationAndAPMAreNotEnabledForNAS_andAPMOrBaseSiteDoNotHaveRestrictions_ShouldReturnTrue() {
        when(checkoutComMerchantConfigurationServiceMock.isNasUsed()).thenReturn(false);
        when(checkoutComGlobalAPMConfigurationMock.getAbcAPMs()).thenReturn(List.of(apmConfiguration1Mock));
        when(apmConfiguration1Mock.getRestrictedCountries()).thenReturn(emptySet());
        when(apmConfiguration1Mock.getRestrictedCurrencies()).thenReturn(emptySet());

        final boolean result = testObj.isApmAvailable(apmConfiguration1Mock, UK.getCountry(), GBP);

        assertTrue(result);
    }

    @Test
    public void isApmAvailable_WhenSiteConfigurationAndAPMAreEnabledForNAS_andApmConfigurationDoesNotHaveRestrictions_andBaseSite_ShouldReturnTrue() {
        when(apmConfiguration1Mock.getRestrictedCountries()).thenReturn(emptySet());
        when(apmConfiguration1Mock.getRestrictedCurrencies()).thenReturn(emptySet());
        when(checkoutComGlobalAPMConfigurationMock.getNasAPMs()).thenReturn(List.of(apmConfiguration1Mock));

        final boolean result = testObj.isApmAvailable(apmConfiguration1Mock, UK.getCountry(), GBP);

        assertTrue(result);
    }

    @Test
    public void isApmAvailable_WhenApmRestrictedForDifferentCountry_ShouldReturnFalse() {
        when(apmConfiguration1Mock.getRestrictedCurrencies()).thenReturn(emptySet());

        final boolean result = testObj.isApmAvailable(apmConfiguration1Mock, UK.getCountry(), EUR);

        assertFalse(result);
    }

    @Test
    public void isApmAvailable_WhenApmRestrictedForDifferentCurrency_ShouldReturnFalse() {
        when(apmConfiguration1Mock.getRestrictedCountries()).thenReturn(emptySet());

        final boolean result = testObj.isApmAvailable(apmConfiguration1Mock, UK.getCountry(), GBP);

        assertFalse(result);
    }

    @Test
    public void isApmAvailable_WhenApmRestrictedForDifferentCountryAndCurrency_ShouldReturnTrue() {
        when(checkoutComGlobalAPMConfigurationMock.getNasAPMs()).thenReturn(List.of(apmConfiguration1Mock));

        final boolean result = testObj.isApmAvailable(apmConfiguration1Mock, FRANCE.getCountry(), EUR);

        assertTrue(result);
    }

    @Test
    public void getApmConfigurationByCode_WhenConfigurationIsFound_ShouldReturnIt() {
        when(checkoutComApmConfigurationDaoMock.find(ImmutableMap.of(CheckoutComAPMConfigurationModel.CODE, FAWRY.name()))).thenReturn(List.of(fawryConfigurationModelMock));

        final Optional<CheckoutComAPMConfigurationModel> result = testObj.getApmConfigurationByCode(FAWRY.name());

        assertTrue(result.isPresent());
        assertEquals(fawryConfigurationModelMock, result.get());
    }

    @Test
    public void getApmConfigurationByCode_WhenConfigurationIsNotFound_ShouldReturnOptionalEmpty() {
        when(checkoutComApmConfigurationDaoMock.find(ImmutableMap.of(CheckoutComAPMConfigurationModel.CODE, FAWRY.name()))).thenReturn(emptyList());

        final Optional<CheckoutComAPMConfigurationModel> result = testObj.getApmConfigurationByCode(FAWRY.name());

        assertFalse(result.isPresent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getApmConfigurationByCode_WhenConfigurationCodeIsNull_ShouldThrowException() {
        testObj.getApmConfigurationByCode(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void isApmRedirect_WhenApmCodeEmpty_ShouldThrowException() {
        testObj.isApmRedirect("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void isApmRedirect_WhenApmCodeMissing_ShouldThrowException() {
        when(checkoutComAPMConfigurationSettingsMock.containsKey(APM_CODE)).thenReturn(false);

        testObj.isApmRedirect(APM_CODE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void isApmRedirect_WhenApmCodeNotConfigured_ShouldThrowException() {
        when(checkoutComAPMConfigurationSettingsMock.containsKey(APM_CODE)).thenReturn(true);
        when(checkoutComAPMConfigurationSettingsMock.get(APM_CODE)).thenReturn(null);

        testObj.isApmRedirect(APM_CODE);
    }

    @Test
    public void isApmRedirect_WhenApmCodetConfigured_ShouldReturnConfigurationValue() {
        when(apmConfigurationSettingsMock.getIsApmRedirect()).thenReturn(Boolean.TRUE);

        assertTrue(testObj.isApmRedirect(APM_CODE));
    }

    @Test(expected = IllegalArgumentException.class)
    public void isApmUserDataRequired_WhenApmCodeEmpty_ShouldThrowException() {
        testObj.isApmUserDataRequired("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void isApmUserDataRequired_WhenApmCodeMissing_ShouldThrowException() {
        when(checkoutComAPMConfigurationSettingsMock.containsKey(APM_CODE)).thenReturn(false);

        testObj.isApmUserDataRequired(APM_CODE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void isApmUserDataRequired_WhenApmCodeNotConfigured_ShouldThrowException() {
        when(checkoutComAPMConfigurationSettingsMock.containsKey(APM_CODE)).thenReturn(true);
        when(checkoutComAPMConfigurationSettingsMock.get(APM_CODE)).thenReturn(null);

        testObj.isApmUserDataRequired(APM_CODE);
    }

    @Test
    public void isApmUserDataRequired_WhenApmCodeConfigured_ShouldReturnConfigurationValue() {
        when(apmConfigurationSettingsMock.getIsApmUserDataRequired()).thenReturn(Boolean.TRUE);

        assertTrue(testObj.isApmUserDataRequired(APM_CODE));
    }

    @Test
    public void getAvailableApms_WhenNoAddressInCart_ShouldReturnEmptyList() {
        when(cartMock.getPaymentAddress()).thenReturn(null);
        when(cartMock.getDeliveryAddress()).thenReturn(null);

        final List<CheckoutComAPMConfigurationModel> result = testObj.getAvailableApms();

        assertEquals(0, result.size());
    }

    @Test
    public void getAvailableApms_WhenBillingAddressInCart_ShouldReturnAvailableApmsForBillingAddress() {
        doReturn(true).when(testObj).isApmAvailable(apmConfiguration1Mock, BILLING_ADDRESS_COUNTRY_CODE, CART_CURRENCY);
        doReturn(false).when(testObj).isApmAvailable(apmConfiguration2Mock, BILLING_ADDRESS_COUNTRY_CODE, CART_CURRENCY);

        final List<CheckoutComAPMConfigurationModel> result = testObj.getAvailableApms();

        assertThat(result).hasSize(1);
        assertEquals(apmConfiguration1Mock, result.get(0));
    }

    @Test
    public void getAvailableApms_WhenNoBillingAddressInCart_ShouldReturnAvailableApmsForShippingAddress() {
        when(cartMock.getPaymentAddress()).thenReturn(null);
        when(shippingAddressMock.getCountry().getIsocode()).thenReturn(SHIPPING_ADDRESS_COUNTRY_CODE);
        doReturn(true).when(testObj).isApmAvailable(apmConfiguration1Mock, SHIPPING_ADDRESS_COUNTRY_CODE, CART_CURRENCY);
        doReturn(false).when(testObj).isApmAvailable(apmConfiguration2Mock, SHIPPING_ADDRESS_COUNTRY_CODE, CART_CURRENCY);

        final List<CheckoutComAPMConfigurationModel> result = testObj.getAvailableApms();

        assertThat(result).hasSize(1);
        assertEquals(apmConfiguration1Mock, result.get(0));
    }

    @Test
    public void getAvailableApms_WhenComponentIsNotVisible_ShouldReturnAvailableApms() {
        when(component2Mock.getVisible()).thenReturn(Boolean.FALSE);
        doReturn(true).when(testObj).isApmAvailable(apmConfiguration1Mock, BILLING_ADDRESS_COUNTRY_CODE, CART_CURRENCY);

        final List<CheckoutComAPMConfigurationModel> result = testObj.getAvailableApms();

        assertThat(result).hasSize(1);
        assertEquals(apmConfiguration1Mock, result.get(0));
    }

    @Test
    public void getApmConfigurationMedia_WhenNoApmComponentFound_ShouldReturnOptionalEmpty() {
        when(checkoutComApmComponentDaoMock.find(ImmutableMap.of(CheckoutComAPMComponentModel.APMCONFIGURATION, apmConfiguration1Mock))).thenReturn(Collections.emptyList());

        final Optional<MediaModel> result = testObj.getApmConfigurationMedia(apmConfiguration1Mock);

        assertFalse(result.isPresent());
    }

    @Test
    public void getApmConfigurationMedia_WhenApmComponentFound_ShouldReturnMedia() {
        when(checkoutComApmComponentDaoMock.find(ImmutableMap.of(CheckoutComAPMComponentModel.APMCONFIGURATION, apmConfiguration1Mock))).thenReturn(List.of(component1Mock));
        when(component1Mock.getMedia()).thenReturn(mediaMock);

        final Optional<MediaModel> result = testObj.getApmConfigurationMedia(apmConfiguration1Mock);

        assertTrue(result.isPresent());
        assertEquals(mediaMock, result.get());
    }
}
