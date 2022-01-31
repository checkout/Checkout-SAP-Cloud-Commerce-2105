package com.checkout.hybris.events.order.process.daos.impl;

import com.checkout.hybris.events.order.process.daos.CheckoutComProcessDefinitionDao;
import com.checkout.hybris.fulfilmentprocess.model.CheckoutComVoidProcessModel;
import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.commerceservices.i18n.CommerceCommonI18NService;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.orderprocessing.model.OrderProcessModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.model.BusinessProcessModel;
import de.hybris.platform.returns.model.ReturnProcessModel;
import de.hybris.platform.servicelayer.ServicelayerTransactionalTest;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

@IntegrationTest
public class DefaultCheckoutComProcessDefinitionDaoIntegrationTest extends ServicelayerTransactionalTest {

    private static final String REFUND_ACTION_1 = "refundAction1";
    private static final String REFUND_ACTION_2 = "refundAction2";
    private static final String ORDER_1 = "order1";
    private static final String ORDER_2 = "order2";
    private static final String ORDER_PROCESS_DEFINITION_NAME = "order-process";
    private static final String V1 = "v1";

    @Resource
    private CheckoutComProcessDefinitionDao checkoutComProcessDefinitionDao;

    @Resource
    private ModelService modelService;
    @Resource
    private BusinessProcessService businessProcessService;
    @Resource
    private CommerceCommonI18NService commerceCommonI18NService;

    private OrderProcessModel orderProcess;
    private ReturnProcessModel returnProcess;
    private CheckoutComVoidProcessModel voidProcess;

    @Before
    public void setUp() throws Exception {
        createCoreData();
        createDefaultCatalog();
        importCsv("/commerceservices/test/testCommerceCart.csv", "utf-8");

        createReturnProcess(REFUND_ACTION_1);
        returnProcess = createReturnProcess(REFUND_ACTION_2);

        final OrderModel order1 = createOrder(ORDER_1);
        final OrderModel clonedOrder1 = modelService.clone(order1);
        clonedOrder1.setOriginalVersion(order1);
        clonedOrder1.setVersionID(V1);
        modelService.save(clonedOrder1);
        orderProcess = createOrderProcess(order1);
        final OrderModel order2 = createOrder(ORDER_2);
        final OrderModel clonedOrder2 = modelService.clone(order2);
        clonedOrder2.setOriginalVersion(order2);
        clonedOrder2.setVersionID(V1);
        modelService.save(clonedOrder2);
        createOrderProcess(order2);

        voidProcess = createVoidProcess(order1);
        createVoidProcess(order2);
    }

    @Test
    public void findReturnProcesses_WhenActionIdGiven_ShouldFindProcess() {
        final List<BusinessProcessModel> result = checkoutComProcessDefinitionDao.findWaitingReturnProcesses(REFUND_ACTION_2);

        assertEquals(1, result.size());
        assertSame(returnProcess, result.get(0));
    }

    @Test
    public void findReturnProcesses_WhenNoProcessFoundWithGivenActionId_ShouldReturnEmptyResult() {
        final List<BusinessProcessModel> result = checkoutComProcessDefinitionDao.findWaitingReturnProcesses("someRefundAction");

        assertEquals(0, result.size());
    }

    @Test
    public void findOrderProcesses_WhenOrderCodeAndOrderDefinitionNameGiven_ShouldFindProcess() {
        final List<BusinessProcessModel> result = checkoutComProcessDefinitionDao.findWaitingOrderProcesses(ORDER_1, ORDER_PROCESS_DEFINITION_NAME);

        assertEquals(1, result.size());
        assertSame(orderProcess, result.get(0));
    }

    @Test
    public void findWaitingOrderProcesses_WhenNoProcessFoundWithGivenOrderCode_ShouldReturnEmptyResult() {
        final List<BusinessProcessModel> result = checkoutComProcessDefinitionDao.findWaitingOrderProcesses("some-code", ORDER_PROCESS_DEFINITION_NAME);

        assertEquals(0, result.size());
    }

    @Test
    public void findWaitingOrderProcesses_WhenNoProcessFoundWithGivenDefinitionName_ShouldReturnEmptyResult() {
        final List<BusinessProcessModel> result = checkoutComProcessDefinitionDao.findWaitingOrderProcesses("some-code", "some-definition");

        assertEquals(0, result.size());
    }

    @Test
    public void findWaitingVoidProcesses_WhenOrderCodeGiven_ShouldFindProcess() {
        final List<BusinessProcessModel> result = checkoutComProcessDefinitionDao.findWaitingVoidProcesses(ORDER_1);

        assertEquals(1, result.size());
        assertSame(voidProcess, result.get(0));
    }

    @Test
    public void findWaitingVoidProcesses_WhenNoProcessFoundWithGivenOrderCode_ShouldReturnEmptyResult() {
        final List<BusinessProcessModel> result = checkoutComProcessDefinitionDao.findWaitingVoidProcesses("some-order");

        assertEquals(0, result.size());
    }

    private OrderModel createOrder(final String orderCode) {
        final CustomerModel customer = modelService.create(CustomerModel.class);
        customer.setUid(orderCode);
        customer.setName(orderCode);
        modelService.save(customer);

        final OrderModel order = modelService.create(OrderModel.class);
        order.setCode(orderCode);
        order.setUser(customer);
        order.setCheckoutComPaymentReference("ckoPayRef");
        order.setDate(new java.util.Date());
        order.setCurrency(commerceCommonI18NService.getAllCurrencies().get(0));
        modelService.save(order);
        return order;
    }

    private OrderProcessModel createOrderProcess(final OrderModel order) {
        final OrderProcessModel orderProcess = businessProcessService.createProcess("orderProcess-" + order.getCode(), ORDER_PROCESS_DEFINITION_NAME);
        orderProcess.setOrder(order);
        modelService.save(orderProcess);
        return orderProcess;
    }

    private ReturnProcessModel createReturnProcess(final String refundActionId) {
        final ReturnProcessModel returnProcess = businessProcessService.createProcess("returnProcess-" + refundActionId, "return-process");
        returnProcess.setRefundActionId(refundActionId);
        modelService.save(returnProcess);
        return returnProcess;
    }

    private CheckoutComVoidProcessModel createVoidProcess(final OrderModel order) {
        final CheckoutComVoidProcessModel voidProcess = businessProcessService.createProcess("voidProcess-" + order.getCode(), "void-process");
        voidProcess.setOrder(order);
        modelService.save(voidProcess);
        return voidProcess;
    }
}
