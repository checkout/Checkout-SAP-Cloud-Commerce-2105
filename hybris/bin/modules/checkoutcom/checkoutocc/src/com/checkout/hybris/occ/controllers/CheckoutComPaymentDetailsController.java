package com.checkout.hybris.occ.controllers;

import com.checkout.hybris.commercefacades.user.CheckoutComUserFacade;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.commerceservices.request.mapping.annotation.RequestMappingOverride;
import de.hybris.platform.commercewebservicescommons.dto.order.PaymentDetailsWsDTO;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.RequestParameterException;
import de.hybris.platform.core.PK;
import de.hybris.platform.webservicescommons.cache.CacheControl;
import de.hybris.platform.webservicescommons.cache.CacheControlDirective;
import de.hybris.platform.webservicescommons.errors.exceptions.WebserviceValidationException;
import de.hybris.platform.webservicescommons.mapping.DataMapper;
import de.hybris.platform.webservicescommons.swagger.ApiBaseSiteIdAndUserIdParam;
import de.hybris.platform.webservicescommons.util.YSanitizer;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Controller
@RequestMapping(value = "/{baseSiteId}/users/{userId}/paymentdetails")
@CacheControl(directive = CacheControlDirective.PRIVATE)
@Api(tags = "Payment Details")
public class CheckoutComPaymentDetailsController {

	private static final Logger LOG = LogManager.getLogger(CheckoutComPaymentDetailsController.class);
	private static final String OBJECT_NAME_PAYMENT_DETAILS = "paymentDetails";

	@Resource(name = "checkoutComUserFacade")
	protected CheckoutComUserFacade checkoutComUserFacade;

	@Resource(name = "paymentDetailsDTOValidator")
	protected Validator paymentDetailsDTOValidator;

	@Resource(name = "dataMapper")
	protected DataMapper dataMapper;

	@RequestMappingOverride
	@Secured({"ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP"})
	@RequestMapping(value = "/{paymentDetailsId}", method = RequestMethod.PUT, consumes = {
			MediaType.APPLICATION_JSON_VALUE,
			MediaType.APPLICATION_XML_VALUE
	})
	@ApiOperation(nickname = "replacePaymentDetails", value = "Updates existing customer's credit card payment info.",
				  notes =
						  "Updates existing customer's credit card payment info based on the "
								  + "payment info ID. Attributes not given in request will be defined again (set to " +
								  "null or default).")
	@ApiBaseSiteIdAndUserIdParam
	@ResponseStatus(HttpStatus.OK)
	public void replacePaymentDetails(
			@ApiParam(value = "Payment details identifier.", required = true) @PathVariable
			final String paymentDetailsId,
			@ApiParam(value = "Payment details object.", required = true) @RequestBody
			final PaymentDetailsWsDTO paymentDetails) {
		final CCPaymentInfoData paymentInfoData = getPaymentInfo(paymentDetailsId);
		final boolean isAlreadyDefaultPaymentInfo = paymentInfoData.isDefaultPaymentInfo();

		validate(paymentDetails, OBJECT_NAME_PAYMENT_DETAILS, getPaymentDetailsDTOValidator());
		getDataMapper().map(paymentDetails, paymentInfoData,
							"accountHolderName,cardNumber,cardType,issueNumber,startMonth,expiryMonth,startYear," +
									"expiryYear,subscriptionId,defaultPaymentInfo,saved,billingAddress"
									+ "(firstName,lastName,titleCode,line1,line2,town,postalCode,region(isocode)," +
									"country(isocode),defaultAddress)",
							true);

		getUserFacade().updateCCPaymentInfo(paymentInfoData);
		if (paymentInfoData.isSaved() && !isAlreadyDefaultPaymentInfo && paymentInfoData.isDefaultPaymentInfo()) {
			getUserFacade().setDefaultPaymentInfo(paymentInfoData);
		}
	}

	@Secured({ "ROLE_CUSTOMERGROUP", "ROLE_TRUSTED_CLIENT", "ROLE_CUSTOMERMANAGERGROUP" })
    @RequestMapping(value = "/{paymentDetailsId}", method = RequestMethod.DELETE)
    @ApiOperation(nickname = "removePaymentDetails", value = "Deletes customer's credit card payment details.", notes = "Deletes a customer's credit card payment details based on a specified paymentDetailsId.")
    @ApiBaseSiteIdAndUserIdParam
    @ResponseStatus(HttpStatus.OK)
    @RequestMappingOverride
    public void removePaymentDetails(
        @ApiParam(value = "Payment details identifier.", required = true) @PathVariable final String paymentDetailsId)
    {
        LOG.debug("removePaymentDetails: id = {}", sanitize(paymentDetailsId));
        getPaymentInfo(paymentDetailsId);
        getUserFacade().removeCCPaymentInfo(paymentDetailsId);
    }

	protected CCPaymentInfoData getPaymentInfo(final String paymentDetailsId) {
		LOG.debug("getPaymentInfo : id = {}", sanitize(paymentDetailsId));
		try {
			final CCPaymentInfoData paymentInfoData = getUserFacade().getCCPaymentInfoForCode(paymentDetailsId);
			if (paymentInfoData == null) {
				throw new RequestParameterException("Payment details [" + sanitize(paymentDetailsId) + "] not found.",
													RequestParameterException.UNKNOWN_IDENTIFIER, "paymentDetailsId");
			}
			return paymentInfoData;
		}
		catch (final PK.PKException e) {
			throw new RequestParameterException("Payment details [" + sanitize(paymentDetailsId) + "] not found.",
												RequestParameterException.UNKNOWN_IDENTIFIER, "paymentDetailsId", e);
		}
	}

	protected void validate(final Object object, final String objectName, final Validator validator) {
		final Errors errors = new BeanPropertyBindingResult(object, objectName);
		validator.validate(object, errors);
		if (errors.hasErrors()) {
			throw new WebserviceValidationException(errors);
		}
	}

	protected String sanitize(final String input) {
		return YSanitizer.sanitize(input);
	}

	protected Validator getPaymentDetailsDTOValidator() {
		return paymentDetailsDTOValidator;
	}

	protected UserFacade getUserFacade() {
		return checkoutComUserFacade;
	}

	protected DataMapper getDataMapper() {
		return dataMapper;
	}
}
