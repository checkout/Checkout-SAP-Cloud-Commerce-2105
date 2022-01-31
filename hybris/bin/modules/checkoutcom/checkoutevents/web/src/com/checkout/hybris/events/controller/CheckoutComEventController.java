package com.checkout.hybris.events.controller;

import com.checkout.hybris.events.facades.CheckoutComEventFacade;
import com.checkout.hybris.events.validators.CheckoutComRequestEventValidator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Controller to receive web hook events from checkout.com
 */
@Controller
@RequestMapping(value = "/receive-event")
public class CheckoutComEventController {

    protected static final Logger LOG = LogManager.getLogger(CheckoutComEventController.class);

    @Resource
    private CheckoutComRequestEventValidator checkoutComRequestEventValidator;
    @Resource
    private CheckoutComEventFacade checkoutComEventFacade;

    /**
     * Controller method to receives the checkout.com webhook events
     *
     * @param request   the http request
     * @param eventBody the event body
     */
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping
    public void receiveEvent(final HttpServletRequest request, @RequestBody final String eventBody) {
        final String ckoSignature = request.getHeader("cko-signature");
        if (LOG.isDebugEnabled()) {
            LOG.debug("Received event signature: [{}]; ", ckoSignature);
            LOG.debug("Received event body: [{}]", eventBody);
        }

        boolean isCkoSignatureValid;

        try {
            isCkoSignatureValid = checkoutComRequestEventValidator.isCkoSignatureValid(ckoSignature, eventBody);
        } catch (final InvalidKeyException | NoSuchAlgorithmException e) {
            LOG.error("Exception while validating the event body.", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Exception while converting the event body to hmac.", e);
        }

        if (isCkoSignatureValid) {
            checkoutComEventFacade.publishPaymentEvent(eventBody);
        } else {
            LOG.error("The cko-signature [{}] is not valid.", ckoSignature);
        }

    }
}
