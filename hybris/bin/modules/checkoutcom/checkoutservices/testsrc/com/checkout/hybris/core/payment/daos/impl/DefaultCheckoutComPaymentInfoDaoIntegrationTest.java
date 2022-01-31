package com.checkout.hybris.core.payment.daos.impl;

import com.checkout.hybris.core.payment.daos.CheckoutComPaymentInfoDao;
import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.ServicelayerBaseTest;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertTrue;

@IntegrationTest
public class DefaultCheckoutComPaymentInfoDaoIntegrationTest extends ServicelayerBaseTest {

    private static final String PAYMENT_ID = "paymentId";

    @Resource
    private CheckoutComPaymentInfoDao paymentInfoDao;

    @Resource
    private ModelService modelService;

    private PaymentInfoModel paymentInfoModel1, paymentInfoModel2;

    @Before
    public void setUp() {
        paymentInfoModel1 = createPaymentInfoModel(PAYMENT_ID, null);
        paymentInfoModel2 = createPaymentInfoModel(PAYMENT_ID, paymentInfoModel1);
        createPaymentInfoModel("otherPaymentId", null);
        createPaymentInfoModel("otherPaymentId", paymentInfoModel1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void findPaymentInfoByPaymentId_WhenPaymentIdIsNull_ShouldThrowException() {
        paymentInfoDao.findPaymentInfosByPaymentId(null);
    }

    @Test
    public void findPaymentInfoByPaymentId_WhenPaymentIdIsDefined_ShouldReturnPaymentInfoWithSameIdAndHasOriginalValue() {
        final List<PaymentInfoModel> result = paymentInfoDao.findPaymentInfosByPaymentId(PAYMENT_ID);

        assertTrue(result.containsAll(asList(paymentInfoModel1, paymentInfoModel2)));
    }

    private PaymentInfoModel createPaymentInfoModel(final String paymentId, final PaymentInfoModel originalPaymentInfo) {
        final String uid = String.valueOf(new Date().getTime());

        final UserModel user = modelService.create(UserModel.class);
        user.setUid(uid);
        modelService.save(user);

        final PaymentInfoModel paymentInfoModel = modelService.create(PaymentInfoModel.class);
        paymentInfoModel.setCode(uid);
        paymentInfoModel.setUser(user);
        paymentInfoModel.setPaymentId(paymentId);
        paymentInfoModel.setOriginal(originalPaymentInfo);
        modelService.save(paymentInfoModel);

        return paymentInfoModel;
    }

}