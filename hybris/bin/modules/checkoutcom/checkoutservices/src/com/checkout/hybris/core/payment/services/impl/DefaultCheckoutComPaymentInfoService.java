package com.checkout.hybris.core.payment.services.impl;

import com.checkout.hybris.core.address.services.CheckoutComAddressService;
import com.checkout.hybris.core.enums.EnvironmentType;
import com.checkout.hybris.core.merchant.services.CheckoutComMerchantConfigurationService;
import com.checkout.hybris.core.model.CheckoutComAPMPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComBenefitPayPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.model.PayloadModel;
import com.checkout.hybris.core.order.daos.CheckoutComOrderDao;
import com.checkout.hybris.core.payment.daos.CheckoutComPaymentInfoDao;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentInfoService;
import com.checkout.payments.CardSourceResponse;
import com.checkout.payments.ResponseSource;
import de.hybris.platform.commerceservices.service.data.CommerceCheckoutParameter;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.order.impl.DefaultPaymentInfoService;
import de.hybris.platform.servicelayer.model.ModelService;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang.StringUtils.EMPTY;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Default implementation of the {@link CheckoutComPaymentInfoService}
 */
public class DefaultCheckoutComPaymentInfoService extends DefaultPaymentInfoService implements CheckoutComPaymentInfoService {

    private static final Logger LOG = LogManager.getLogger(DefaultCheckoutComPaymentInfoService.class);
    protected static final String CART_CANNOT_BE_NULL_ERROR_MSG = "Cart cannot be null.";

    protected final CheckoutComAddressService addressService;
    protected final CheckoutComPaymentInfoDao paymentInfoDao;
    protected final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService;
    protected final CheckoutComOrderDao checkoutComOrderDao;


    public DefaultCheckoutComPaymentInfoService(final CheckoutComAddressService addressService,
                                                final CheckoutComPaymentInfoDao paymentInfoDao,
                                                final CheckoutComMerchantConfigurationService checkoutComMerchantConfigurationService,
                                                final CheckoutComOrderDao checkoutComOrderDao) {
        this.addressService = addressService;
        this.paymentInfoDao = paymentInfoDao;
        this.checkoutComMerchantConfigurationService = checkoutComMerchantConfigurationService;
        this.checkoutComOrderDao = checkoutComOrderDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createPaymentInfo(final PaymentInfoModel paymentInfoModel, final CartModel cartModel) {
        validateParameterNotNull(paymentInfoModel, "Payment info model cannot be null");
        validateParameterNotNull(cartModel, "CartModel cannot be null");

        PaymentInfoModel paymentInfoModelToSave;
        final UserModel user = cartModel.getUser();

        if (paymentInfoModel instanceof CheckoutComCreditCardPaymentInfoModel) {
            paymentInfoModelToSave = setUpCardPaymentInfo((CheckoutComCreditCardPaymentInfoModel) paymentInfoModel, cartModel, user);
        } else if (paymentInfoModel instanceof CheckoutComAPMPaymentInfoModel) {
            paymentInfoModelToSave = setUpRedirectApmPaymentInfo((CheckoutComAPMPaymentInfoModel) paymentInfoModel, cartModel);
        } else {
            throw new IllegalArgumentException("Given payment info model is not valid " + paymentInfoModel.getItemtype());
        }

        cloneAndSetBillingAddressFromCart(cartModel, paymentInfoModelToSave);
        paymentInfoModelToSave.setUser(user);
        paymentInfoModelToSave.setSaved(false);
        callSuperModelService().save(paymentInfoModelToSave);

        cartModel.setPaymentInfo(paymentInfoModel);
        callSuperModelService().save(cartModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removePaymentInfo(final CartModel cartModel) {
        validateParameterNotNull(cartModel, "CartModel cannot be null");
        validateParameterNotNull(cartModel.getPaymentInfo(), "Payment info model cannot be null");

        callSuperModelService().remove(cartModel.getPaymentInfo());
        cartModel.setPaymentInfo(null);
        callSuperModelService().save(cartModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidCreditCardPaymentInfo(final CartModel cartModel) {
        return cartModel.getPaymentInfo() instanceof CheckoutComCreditCardPaymentInfoModel && isNotBlank(((CheckoutComCreditCardPaymentInfoModel) cartModel.getPaymentInfo()).getCardToken());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidRedirectApmPaymentInfo(final CartModel cartModel) {
        return cartModel.getPaymentInfo() instanceof CheckoutComAPMPaymentInfoModel && isNotBlank(((CheckoutComAPMPaymentInfoModel) cartModel.getPaymentInfo()).getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValidPaymentInfo(final CartModel cartModel) {
        return isValidCreditCardPaymentInfo(cartModel) || isValidRedirectApmPaymentInfo(cartModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isUserDataRequiredApmPaymentMethod(final CartModel cartModel) {
        validateParameterNotNull(cartModel, CART_CANNOT_BE_NULL_ERROR_MSG);
        final PaymentInfoModel paymentInfo = cartModel.getPaymentInfo();
        validateParameterNotNull(paymentInfo, "PaymentInfo cannot be null for the session cart.");

        return paymentInfo instanceof CheckoutComAPMPaymentInfoModel && ((CheckoutComAPMPaymentInfoModel) paymentInfo).getUserDataRequired();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addQRCodeDataToBenefitPaymentInfo(final CheckoutComBenefitPayPaymentInfoModel paymentInfo, final String qrCode) {
        validateParameterNotNull(paymentInfo, "PaymentInfo cannot be null.");
        checkArgument(isNotBlank(qrCode), "User data cannot be empty.");

        paymentInfo.setQrCode(qrCode);
        callSuperModelService().save(paymentInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPaymentId(final String paymentId, final PaymentInfoModel paymentInfo) {
        validateParameterNotNull(paymentInfo, "PaymentInfo cannot be null for the cart.");
        checkArgument(isNotBlank(paymentId), "Payment id cannot be empty.");

        paymentInfo.setPaymentId(paymentId);
        callSuperModelService().save(paymentInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addSubscriptionIdToUserPayment(final CheckoutComCreditCardPaymentInfoModel paymentInfo, final ResponseSource source) {
        validateParameterNotNull(paymentInfo, "PaymentInfo cannot be null");

        if (paymentInfo.getMarkToSave() && source instanceof CardSourceResponse && ((CardSourceResponse) source).getId() != null) {
            paymentInfo.getUser().getPaymentInfos().stream()
                    .filter(CheckoutComCreditCardPaymentInfoModel.class::isInstance)
                    .filter(paymentInfoModel -> paymentInfoModel.getCode().equalsIgnoreCase(paymentInfo.getCode()))
                    .findAny()
                    .ifPresent(userPayment -> updateUserPaymentInfo((CardSourceResponse) source, (CheckoutComCreditCardPaymentInfoModel) userPayment));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSiteIdFromPaymentId(final String paymentId) {
        final List<PaymentInfoModel> paymentInfos = paymentInfoDao.findPaymentInfosByPaymentId(paymentId);

        final Optional<AbstractOrderModel> abstractOrder = paymentInfos.stream()
                .map(ItemModel::getOwner)
                .filter(AbstractOrderModel.class::isInstance)
                .map(AbstractOrderModel.class::cast)
                .findAny();

        if (!paymentInfos.isEmpty() && abstractOrder.isPresent()) {
            return abstractOrder.get().getSite().getUid();
        }
        return EMPTY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AbstractOrderModel> findAbstractOrderByPaymentId(final String paymentId) {
        return paymentInfoDao.findPaymentInfosByPaymentId(paymentId)
                .stream()
                .map(ItemModel::getOwner)
                .filter(AbstractOrderModel.class::isInstance)
                .map(AbstractOrderModel.class::cast)
                .collect(toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<PaymentInfoModel> getPaymentInfosByPaymentId(final String paymentId) {
        return paymentInfoDao.findPaymentInfosByPaymentId(paymentId);
    }

    protected void updateUserPaymentInfo(final CardSourceResponse source, final CheckoutComCreditCardPaymentInfoModel userPayment) {
        userPayment.setSaved(true);
        userPayment.setSubscriptionId(source.getId());
        callSuperModelService().save(userPayment);
    }

    /**
     * Workaround: Extra address created for the payment info to don't corrupt the cart once deleting payment info
     *
     * @param cartModel        the cart with source address
     * @param paymentInfoModel payment info to update
     * @return AddressModel the cloned address model
     */
    protected AddressModel cloneAndSetBillingAddressFromCart(final CartModel cartModel, final PaymentInfoModel paymentInfoModel) {
        validateParameterNotNull(cartModel, CART_CANNOT_BE_NULL_ERROR_MSG);
        validateParameterNotNull(cartModel.getPaymentAddress(), "Payment Address cannot be null.");

        final AddressModel paymentAddress = cartModel.getPaymentAddress();
        final AddressModel clonedAddress = addressService.cloneAddressForOwner(paymentAddress, paymentInfoModel);
        clonedAddress.setBillingAddress(true);
        clonedAddress.setShippingAddress(false);
        clonedAddress.setOwner(paymentInfoModel);
        paymentInfoModel.setBillingAddress(clonedAddress);
        return clonedAddress;
    }

    protected CheckoutComAPMPaymentInfoModel setUpRedirectApmPaymentInfo(final CheckoutComAPMPaymentInfoModel paymentInfoModel, final CartModel cartModel) {
        paymentInfoModel.setCode(generatePaymentInfoCode(cartModel));
        return paymentInfoModel;
    }

    protected CheckoutComCreditCardPaymentInfoModel setUpCardPaymentInfo(final CheckoutComCreditCardPaymentInfoModel paymentInfoModel, final CartModel cartModel, final UserModel user) {
        paymentInfoModel.setCode(generatePaymentInfoCode(cartModel));
        return paymentInfoModel;
    }

    protected CommerceCheckoutParameter createCommerceCheckoutParameter(final CartModel cart) {
        final CommerceCheckoutParameter parameter = new CommerceCheckoutParameter();
        parameter.setEnableHooks(true);
        parameter.setCart(cart);
        return parameter;
    }

    protected String generatePaymentInfoCode(final AbstractOrderModel cartModel) {
        return cartModel.getCode() + "_" + UUID.randomUUID();
    }

    protected ModelService callSuperModelService() {
        return getModelService();
    }


    public void saveRequestAndResponseInOrder(final AbstractOrderModel abstractOrder, final String request, final String response) {
        setPaymentRequestPayload(request, abstractOrder);
        setPaymentResponsePayload(response, abstractOrder);
        callSuperModelService().save(abstractOrder);
    }

    public void saveResponseInOrderByPaymentReference(final String paymentReference, final String response) {
        if (StringUtils.isNotBlank(paymentReference)) {
            final Optional<AbstractOrderModel> result = checkoutComOrderDao.findAbstractOrderForPaymentReferenceNumber(paymentReference);
            if (result.isPresent()) {
                final AbstractOrderModel abstractOrder = result.get();
                setPaymentResponsePayload(response, abstractOrder);
                callSuperModelService().save(abstractOrder);
            }
        }
    }

    private void setPaymentRequestPayload(final String request, final AbstractOrderModel abstractOrder) {
        Optional.ofNullable(request).ifPresent(paymentRequest -> {
            final List<PayloadModel> requestList = new ArrayList<>(abstractOrder.getRequestsPayload());
            requestList.add(createPayloadModel(request));
            abstractOrder.setRequestsPayload(requestList);
        });
    }

    private void setPaymentResponsePayload(final String response, final AbstractOrderModel abstractOrder) {
        Optional.ofNullable(response).ifPresent(paymentResponse -> {
            final List<PayloadModel> requestList = new ArrayList<>(abstractOrder.getResponsesPayload());
            requestList.add(createPayloadModel(response));
            abstractOrder.setResponsesPayload(requestList);

        });
    }

    protected PayloadModel createPayloadModel(final String payload) {
        final PayloadModel payloadModel = callSuperModelService().create(PayloadModel.class);
        payloadModel.setPayload(payload);
        callSuperModelService().save(payloadModel);
        return payloadModel;
    }

    public void logInfoOut(final String payload) {
        if (checkoutComMerchantConfigurationService.getEnvironment().equals(EnvironmentType.TEST)) {
            LOG.info("*** PAYLOAD OUT ***");
            LOG.info(payload);
            LOG.info("*** PAYLOAD OUT END ***");
        }
    }

}
