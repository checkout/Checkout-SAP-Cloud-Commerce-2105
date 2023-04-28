package com.checkout.hybris.addon.controllers.payment;

import com.checkout.dto.plaidlink.PlaidLinkCreationResponseStorefrontDTO;
import com.checkout.dto.redirect.RedirectObject;
import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;
import com.checkout.hybris.facades.beans.PlaidLinkCreationResponse;
import com.checkout.hybris.facades.payment.CheckoutComACHConsentFacade;
import com.checkout.hybris.facades.payment.ach.CheckoutComAchFacade;
import com.checkout.hybris.facades.payment.ach.consent.exceptions.CustomerConsentException;
import com.checkout.hybris.facades.payment.plaidlink.CheckoutComPlaidLinkFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.i18n.I18NService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.validation.support.BindingAwareModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.io.IOException;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@RunWith(MockitoJUnitRunner.class)
public class CheckoutComACHControllerTest {

    private static final String PUBLIC_TOKEN = "public-token";
    private static final String ACC_ERROR_MSGS = "accErrorMsgs";

    private static final String LINK_TOKEN = "linkToken";

    @Spy
    @InjectMocks
    private CheckoutComACHController testObj;

    @Mock
    private I18NService i18NServiceMock;
    @Mock
    private CheckoutComAchFacade checkoutComAchFacadeMock;
    @Mock
    private CheckoutComPlaidLinkFacade checkoutComPlaidLinkFacadeMock;
    @Mock
    private CheckoutComACHConsentFacade checkoutComACHConsentFacadeMock;
    @Mock
    private Converter<PlaidLinkCreationResponseStorefrontDTO, PlaidLinkCreationResponse> plaidLinkCreationResponseStorefrontDTOPlaidLinkCreationResponseConverterMock;

    @Mock
    private MessageSource messageSourceMock;

    private final BindingAwareModelMap model = new BindingAwareModelMap();
    private final AchBankInfoDetailsData achBankInfoDetailsData = new AchBankInfoDetailsData();
    private final RedirectAttributesModelMap redirectAttributes = new RedirectAttributesModelMap();

    private final PlaidLinkCreationResponse plaidLinkCreationResponse = new PlaidLinkCreationResponse();
    private final PlaidLinkCreationResponseStorefrontDTO plaidLinkCreationResponseStorefrontDTO =
        new PlaidLinkCreationResponseStorefrontDTO();

    @Before
    public void setUp() throws Exception {
        when(plaidLinkCreationResponseStorefrontDTOPlaidLinkCreationResponseConverterMock.convert(
            plaidLinkCreationResponseStorefrontDTO))
            .thenReturn(plaidLinkCreationResponse);
        when(checkoutComPlaidLinkFacadeMock.getBankAccountDetailsData(plaidLinkCreationResponse)).thenReturn(
            achBankInfoDetailsData);
        when(checkoutComPlaidLinkFacadeMock.linkTokenCreate()).thenReturn(LINK_TOKEN);
        plaidLinkCreationResponseStorefrontDTO.setPublicToken(PUBLIC_TOKEN);
        plaidLinkCreationResponseStorefrontDTO.setCustomerConsents(Boolean.TRUE);
    }

    @Test
    public void linkTokenCreate_shouldCallLinkTokenCreate() throws IOException {
        ensureTokenCreateReturnsLinkTokenCreateResponse();

        final String result = testObj.linkTokenCreate();

        verify(checkoutComPlaidLinkFacadeMock).linkTokenCreate();
        assertThat(result).isEqualTo(LINK_TOKEN);
    }

    @Test
    public void itemPublicTokenExchange_shouldReturnUrlWithoutRedirectPrefix_whenPlaceOrderGoesFine() throws
            CMSItemNotFoundException,
            IOException,
            CustomerConsentException {
        ensureGetBankDetailsAreRetrievedBasedOnPublicToken();
        ensureAuthorisePlaceOrderAndRedirectReturnsAPrefixedUrl();

        final RedirectObject result = testObj.itemPublicTokenExchange(plaidLinkCreationResponseStorefrontDTO,
                redirectAttributes,
                model);
        verify(checkoutComAchFacadeMock).setPaymentInfoAchToCart(achBankInfoDetailsData);
        verify(testObj).authorisePlaceOrderAndRedirectToResultPage(model, redirectAttributes);
        assertThat(result.getUrl()).isEqualTo("/path/to/go");
    }

    @Test
    public void itemPublicTokenExchange_shouldReturnUrlWithoutRedirectPrefix_whenPlaceOrderGoesWrong() throws
            CMSItemNotFoundException,
            IOException, CustomerConsentException {
        ensureGetBankDetailsAreRetrievedBasedOnPublicToken();
        ensureAuthorisePlaceOrderAndRedirectReturnsAPrefixedUrl();
        ensureAuthorisePlaceOrderAndRedirectAddsErrorsToRedirectAttributes();

        final RedirectObject result = testObj.itemPublicTokenExchange(plaidLinkCreationResponseStorefrontDTO,
                redirectAttributes,
                model);

        final InOrder inOrder = inOrder(checkoutComPlaidLinkFacadeMock, checkoutComAchFacadeMock, checkoutComACHConsentFacadeMock, testObj);
        inOrder.verify(checkoutComPlaidLinkFacadeMock).getBankAccountDetailsData(plaidLinkCreationResponse);
        inOrder.verify(checkoutComAchFacadeMock).setPaymentInfoAchToCart(achBankInfoDetailsData);
        inOrder.verify(checkoutComACHConsentFacadeMock).createCheckoutComACHConsent(achBankInfoDetailsData, true);
        inOrder.verify(testObj).authorisePlaceOrderAndRedirectToResultPage(model, redirectAttributes);
        assertThat(result.getUrl()).isEqualTo("/path/to/go");
        assertThat(result.getErrors().get(ACC_ERROR_MSGS).get(0)).isEqualTo("translated message");
    }

    private void ensureAuthorisePlaceOrderAndRedirectAddsErrorsToRedirectAttributes() {
        GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.ERROR_MESSAGES_HOLDER, "checkout.error");
        final Locale currentLocale = new Locale("en");
        when(i18NServiceMock.getCurrentLocale()).thenReturn(currentLocale);
        when(messageSourceMock.getMessage("checkout.error", null, currentLocale)).thenReturn("translated message");
    }

    private void ensureAuthorisePlaceOrderAndRedirectReturnsAPrefixedUrl() throws CMSItemNotFoundException {
        doReturn("redirect:/path/to/go")
                .when(testObj).authorisePlaceOrderAndRedirectToResultPage(model, redirectAttributes);
    }

    private void ensureGetBankDetailsAreRetrievedBasedOnPublicToken() throws IOException {
        when(checkoutComPlaidLinkFacadeMock.getBankAccountDetailsData(plaidLinkCreationResponse)).thenReturn(
            achBankInfoDetailsData);
    }

    private void ensureTokenCreateReturnsLinkTokenCreateResponse() throws IOException {
        when(checkoutComPlaidLinkFacadeMock.linkTokenCreate()).thenReturn(LINK_TOKEN);
    }
}
