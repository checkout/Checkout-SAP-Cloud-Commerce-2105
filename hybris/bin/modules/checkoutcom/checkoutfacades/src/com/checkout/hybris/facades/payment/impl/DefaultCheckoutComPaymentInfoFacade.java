package com.checkout.hybris.facades.payment.impl;

import com.checkout.hybris.core.model.CheckoutComAchPaymentInfoModel;
import com.checkout.hybris.core.model.CheckoutComCreditCardPaymentInfoModel;
import com.checkout.hybris.core.payment.details.mappers.CheckoutComUpdatePaymentInfoStrategyMapper;
import com.checkout.hybris.core.payment.details.strategies.CheckoutComUpdatePaymentInfoStrategy;
import com.checkout.hybris.core.payment.enums.CheckoutComPaymentType;
import com.checkout.hybris.core.payment.resolvers.CheckoutComPaymentTypeResolver;
import com.checkout.hybris.core.payment.services.CheckoutComPaymentInfoService;
import com.checkout.hybris.facades.beans.*;
import com.checkout.hybris.facades.payment.CheckoutComPaymentInfoFacade;
import com.checkout.hybris.facades.payment.converters.CheckoutComApmMappedPaymentInfoReverseConverter;
import com.checkout.payments.GetPaymentResponse;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.Assert;

import static com.checkout.hybris.core.payment.enums.CheckoutComPaymentType.*;
import static de.hybris.platform.servicelayer.util.ServicesUtil.validateParameterNotNull;
import static org.codehaus.groovy.runtime.InvokerHelper.asList;

/**
 * Default implementation of the {@link CheckoutComPaymentInfoFacade}
 */
public class DefaultCheckoutComPaymentInfoFacade implements CheckoutComPaymentInfoFacade {

    protected final CartService cartService;
    protected final CheckoutComPaymentInfoService paymentInfoService;
    protected final Converter<CCPaymentInfoData, CheckoutComCreditCardPaymentInfoModel> checkoutComCCPaymentInfoReverseConverter;
    protected final CheckoutComPaymentTypeResolver checkoutComPaymentTypeResolver;
    protected final CheckoutComApmMappedPaymentInfoReverseConverter checkoutComApmMappedPaymentInfoReverseConverter;
    protected final CheckoutComUpdatePaymentInfoStrategyMapper checkoutComUpdatePaymentInfoStrategyMapper;
    protected final Converter<AchPaymentInfoData, CheckoutComAchPaymentInfoModel> checkoutComAchPaymentInfoReverseConverter;

    public DefaultCheckoutComPaymentInfoFacade(final CartService cartService,
                                               final CheckoutComPaymentInfoService paymentInfoService,
                                               final Converter<CCPaymentInfoData, CheckoutComCreditCardPaymentInfoModel> checkoutComCCPaymentInfoReverseConverter,
                                               final CheckoutComPaymentTypeResolver checkoutComPaymentTypeResolver,
                                               final CheckoutComApmMappedPaymentInfoReverseConverter checkoutComApmMappedPaymentInfoReverseConverter,
                                               final CheckoutComUpdatePaymentInfoStrategyMapper checkoutComUpdatePaymentInfoStrategyMapper,
                                               final Converter<AchPaymentInfoData, CheckoutComAchPaymentInfoModel> checkoutComAchPaymentInfoReverseConverter) {
        this.cartService = cartService;
        this.paymentInfoService = paymentInfoService;
        this.checkoutComCCPaymentInfoReverseConverter = checkoutComCCPaymentInfoReverseConverter;
        this.checkoutComPaymentTypeResolver = checkoutComPaymentTypeResolver;
        this.checkoutComApmMappedPaymentInfoReverseConverter = checkoutComApmMappedPaymentInfoReverseConverter;
        this.checkoutComUpdatePaymentInfoStrategyMapper = checkoutComUpdatePaymentInfoStrategyMapper;
        this.checkoutComAchPaymentInfoReverseConverter = checkoutComAchPaymentInfoReverseConverter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPaymentInfoToCart(final Object paymentInfoData) {
        if (cartService.hasSessionCart()) {
            final CartModel sessionCart = cartService.getSessionCart();
            if (sessionCart.getPaymentInfo() != null) {
                paymentInfoService.removePaymentInfo(sessionCart);
            }
            PaymentInfoModel paymentInfoModel;
            if (paymentInfoData instanceof CCPaymentInfoData && StringUtils.isNotBlank(((CCPaymentInfoData) paymentInfoData).getCardType())) {
                paymentInfoModel = checkoutComCCPaymentInfoReverseConverter.convert((CCPaymentInfoData) paymentInfoData);
                paymentInfoService.createPaymentInfo(paymentInfoModel, sessionCart);
            } else if (paymentInfoData instanceof AchPaymentInfoData) {
                paymentInfoModel = checkoutComAchPaymentInfoReverseConverter.convert((AchPaymentInfoData) paymentInfoData);
                paymentInfoService.createPaymentInfo(paymentInfoModel, sessionCart);
            } else if (paymentInfoData instanceof APMPaymentInfoData) {
                final APMPaymentInfoData apmPaymentInfoData = (APMPaymentInfoData) paymentInfoData;
                final CheckoutComPaymentType paymentType = checkoutComPaymentTypeResolver.resolvePaymentMethod(apmPaymentInfoData.getType());
                paymentInfoModel = checkoutComApmMappedPaymentInfoReverseConverter.convertAPMPaymentInfoData(apmPaymentInfoData, paymentType);
                paymentInfoService.createPaymentInfo(paymentInfoModel, sessionCart);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object createPaymentInfoData(final String paymentMethod) {
        switch (CheckoutComPaymentType.valueOf(paymentMethod)) {
            case CARD:
                return new CCPaymentInfoData();
            case FAWRY:
                final FawryPaymentInfoData paymentInfoData = new FawryPaymentInfoData();
                paymentInfoData.setType(FAWRY.name());
                return paymentInfoData;
            case IDEAL:
                final IdealPaymentInfoData idealPaymentInfoData = new IdealPaymentInfoData();
                idealPaymentInfoData.setType(IDEAL.name());
                return idealPaymentInfoData;
            case ACH:
                final AchPaymentInfoData achPaymentInfoData = new AchPaymentInfoData();
                return achPaymentInfoData;
            case SEPA:
                final SepaPaymentInfoData sepaPaymentInfoData = new SepaPaymentInfoData();
                sepaPaymentInfoData.setType(SEPA.name());
                return sepaPaymentInfoData;
            case KLARNA:
                final KlarnaPaymentInfoData klarnaPaymentInfoData = new KlarnaPaymentInfoData();
                klarnaPaymentInfoData.setType(KLARNA.name());
                return klarnaPaymentInfoData;
            case OXXO:
                final OxxoPaymentInfoData oxxoPaymentInfoData = new OxxoPaymentInfoData();
                oxxoPaymentInfoData.setType(OXXO.name());
                return oxxoPaymentInfoData;
            default:
                if (asList(CheckoutComPaymentType.values()).contains(CheckoutComPaymentType.valueOf(paymentMethod))) {
                    final APMPaymentInfoData apmPaymentInfoData = new APMPaymentInfoData();
                    apmPaymentInfoData.setType(paymentMethod);
                    return apmPaymentInfoData;
                }
                throw new IllegalArgumentException("Payment method not supported: " + paymentMethod);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTokenMissingOnCardPaymentInfo(final CartData cartData) {
        Assert.notNull(cartData, "Cart cannot be null.");

        final CCPaymentInfoData paymentInfo = cartData.getPaymentInfo();

        return paymentInfo != null && StringUtils.isBlank(paymentInfo.getPaymentToken());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePaymentInfoFromResponse(final GetPaymentResponse paymentResponse, final PaymentInfoModel paymentInfo) {
        validateParameterNotNull(paymentInfo, "PaymentInfo cannot be null.");

        final CheckoutComPaymentType paymentType = checkoutComPaymentTypeResolver.resolvePaymentType(paymentInfo);
        final CheckoutComUpdatePaymentInfoStrategy updatePaymentInfoStrategy = checkoutComUpdatePaymentInfoStrategyMapper.findStrategy(paymentType);
        updatePaymentInfoStrategy.processPaymentResponse(paymentResponse);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processPaymentDetails(final GetPaymentResponse paymentResponse) {
        if (!cartService.hasSessionCart()) {
            throw new IllegalArgumentException("The current session does not have a cart");
        }
        final CartModel sessionCart = cartService.getSessionCart();
        final PaymentInfoModel paymentInfo = sessionCart.getPaymentInfo();
        validateParameterNotNull(paymentInfo, "The cart payment info is null");

        updatePaymentInfoFromResponse(paymentResponse, paymentInfo);
    }
}
