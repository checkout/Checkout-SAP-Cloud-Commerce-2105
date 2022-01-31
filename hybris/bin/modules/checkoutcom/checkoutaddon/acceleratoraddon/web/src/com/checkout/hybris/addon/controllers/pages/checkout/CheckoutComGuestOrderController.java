package com.checkout.hybris.addon.controllers.pages.checkout;

import com.checkout.hybris.addon.constants.CheckoutaddonConstants;
import com.checkout.hybris.addon.controllers.CheckoutaddonControllerConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.ThirdPartyConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractPageController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.commercefacades.order.OrderFacade;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * This controller handles Checkout Com guest order view
 */
@Controller
@RequestMapping("/checkout-com/guest")
public class CheckoutComGuestOrderController extends AbstractPageController {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComGuestOrderController.class);

    protected static final String ORDER_GUID_PATH_VARIABLE_PATTERN = "{orderGUID:.*}";
    protected static final String ORDER_DETAIL_CMS_PAGE = "order";
    protected static final String REDIRECT_ORDER_EXPIRED = REDIRECT_PREFIX + "/orderExpired";
    protected static final String CHECKOUT_COM_GUEST_ORDER_PAGE = "checkoutComGuestOrderPage";

    @Resource
    protected OrderFacade orderFacade;
    @Resource
    protected ConfigurationService configurationService;

    @GetMapping(value = "/order/" + ORDER_GUID_PATH_VARIABLE_PATTERN)
    public String order(@PathVariable("orderGUID") final String orderGUID, final Model model, final HttpServletResponse response)
            throws CMSItemNotFoundException {
        try {
            final ContentPageModel orderDetailPage = getContentPageForLabelOrId(ORDER_DETAIL_CMS_PAGE);
            storeCmsPageInModel(model, orderDetailPage);
            model.addAttribute(ThirdPartyConstants.SeoRobots.META_ROBOTS, ThirdPartyConstants.SeoRobots.NOINDEX_NOFOLLOW);
            setUpMetaDataForContentPage(model, orderDetailPage);
            final OrderData orderDetails = orderFacade.getOrderDetailsForGUID(orderGUID);
            model.addAttribute("orderData", orderDetails);
        } catch (final UnknownIdentifierException e) {
            LOG.warn("Attempted to load an order that does not exist or is not visible");
            model.addAttribute(ThirdPartyConstants.SeoRobots.META_ROBOTS, ThirdPartyConstants.SeoRobots.NOINDEX_NOFOLLOW);
            GlobalMessages.addErrorMessage(model, "system.error.page.not.found");
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return CheckoutaddonControllerConstants.Views.Pages.Guest.CheckoutGuestOrderErrorPage;
        } catch (final IllegalArgumentException ae) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(ae);
            }
            return REDIRECT_ORDER_EXPIRED;
        }
        final ContentPageModel checkoutComGuestOrderPage = getContentPageForLabelOrId(CHECKOUT_COM_GUEST_ORDER_PAGE);
        storeCmsPageInModel(model, checkoutComGuestOrderPage);
        setUpMetaDataForContentPage(model, checkoutComGuestOrderPage);

        return configurationService.getConfiguration().getString(CheckoutaddonConstants.CHECKOUT_ADDON_PREFIX)
                + CheckoutaddonControllerConstants.Views.Pages.Guest.CheckoutGuestOrderPage;
    }

}
