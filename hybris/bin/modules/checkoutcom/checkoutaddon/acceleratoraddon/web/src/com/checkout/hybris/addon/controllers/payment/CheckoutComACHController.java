package com.checkout.hybris.addon.controllers.payment;

import com.checkout.dto.plaidlink.PlaidLinkCreationResponseStorefrontDTO;
import com.checkout.dto.redirect.RedirectObject;
import com.checkout.hybris.addon.controllers.pages.checkout.steps.CheckoutComSummaryCheckoutStepController;
import com.checkout.hybris.facades.beans.AchBankInfoDetailsData;
import com.checkout.hybris.facades.beans.PlaidLinkCreationResponse;
import com.checkout.hybris.facades.payment.CheckoutComACHConsentFacade;
import com.checkout.hybris.facades.payment.ach.CheckoutComAchFacade;
import com.checkout.hybris.facades.payment.ach.consent.exceptions.CustomerConsentException;
import com.checkout.hybris.facades.payment.plaidlink.CheckoutComPlaidLinkFacade;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessage;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
@RequestMapping(value = "/checkout/payment/checkout-com/ach")
public class CheckoutComACHController extends CheckoutComSummaryCheckoutStepController {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComACHController.class);

    @Resource
    protected CheckoutComAchFacade checkoutComAchFacade;
    @Resource
    protected CheckoutComPlaidLinkFacade checkoutComPlaidLinkFacade;
    @Resource
    protected CheckoutComACHConsentFacade checkoutComACHConsentFacade;
    @Resource
    protected Converter<PlaidLinkCreationResponseStorefrontDTO, PlaidLinkCreationResponse> plaidLinkCreationResponseStorefrontDTOPlaidLinkCreationResponseConverter;

    @ResponseBody
    @RequireHardLogIn
    @PostMapping(value = "/link/token/create", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public String linkTokenCreate() throws IOException {
        return checkoutComPlaidLinkFacade.linkTokenCreate();
    }

    @ResponseBody
    @RequireHardLogIn
    @PostMapping(value = "/item/public_token/exchange", consumes = {MediaType.APPLICATION_JSON_VALUE})
    public RedirectObject itemPublicTokenExchange(
        @ApiParam(required = true)
        @RequestBody final PlaidLinkCreationResponseStorefrontDTO plaidLinkCreationResponseStorefrontDTO,
        final RedirectAttributes redirectAttributes,
        final Model model) throws IOException, CMSItemNotFoundException, CustomerConsentException {
        final PlaidLinkCreationResponse plaidLinkCreationResponse =
            plaidLinkCreationResponseStorefrontDTOPlaidLinkCreationResponseConverter.convert(
                plaidLinkCreationResponseStorefrontDTO);
        final AchBankInfoDetailsData bankAccountDetailsData = checkoutComPlaidLinkFacade.getBankAccountDetailsData(
            plaidLinkCreationResponse);
        checkoutComAchFacade.setPaymentInfoAchToCart(bankAccountDetailsData);
        checkoutComACHConsentFacade.createCheckoutComACHConsent(bankAccountDetailsData,
                                                                plaidLinkCreationResponseStorefrontDTO.getCustomerConsents());
        final String url = authorisePlaceOrderAndRedirectToResultPage(model, redirectAttributes);

        return createRedirectObjectFrom(url, redirectAttributes);
    }

    @Override
    protected String authorisePlaceOrderAndRedirectToResultPage(final Model model,
                                                                final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException {
        return super.authorisePlaceOrderAndRedirectToResultPage(model, redirectAttributes);
    }

    protected RedirectObject createRedirectObjectFrom(final String url, final RedirectAttributes redirectAttributes) {
        final String redirectUrl = StringUtils.remove(url, "redirect:");
        final RedirectObject redirectObject = new RedirectObject();
        redirectObject.setUrl(redirectUrl);
        redirectObject.setErrors(
            translateFlashAttributes((Map<String, List<GlobalMessage>>) redirectAttributes.getFlashAttributes()));

        return redirectObject;
    }

    private Map<String, List<String>> translateFlashAttributes(final Map<String, List<GlobalMessage>> flashAttributes) {
        return flashAttributes.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue()))
                              .map(entryPair -> Pair.of(entryPair.getKey(), entryPair.getValue().stream()
                                                                                     .map(
                                                                                         message -> getMessageSource().getMessage(
                                                                                             message.getCode(), null,
                                                                                             getI18nService().getCurrentLocale()))
                                                                                     .collect(Collectors.toList())))
                              .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

}
