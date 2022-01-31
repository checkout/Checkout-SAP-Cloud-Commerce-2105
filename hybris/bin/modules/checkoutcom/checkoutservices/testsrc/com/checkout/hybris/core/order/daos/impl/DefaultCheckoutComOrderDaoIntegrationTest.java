package com.checkout.hybris.core.order.daos.impl;

import com.checkout.hybris.core.order.daos.CheckoutComOrderDao;
import de.hybris.bootstrap.annotations.IntegrationTest;
import de.hybris.platform.catalog.enums.ArticleApprovalStatus;
import de.hybris.platform.catalog.model.CatalogModel;
import de.hybris.platform.catalog.model.CatalogVersionModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.product.ProductModel;
import de.hybris.platform.core.model.product.UnitModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.order.interceptors.DefaultAbstractOrderEntryPreparer;
import de.hybris.platform.servicelayer.ServicelayerBaseTest;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.servicelayer.user.UserService;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Optional;

import static org.junit.Assert.*;

@IntegrationTest
public class DefaultCheckoutComOrderDaoIntegrationTest extends ServicelayerBaseTest {

    private static final String ORDER_REFERENCE_VALUE = "ORDER_REFERENCE";
    private static final String WRONG_REFERENCE = "wrong_reference";
    private static final String NEW_ORDER = "neworder1";
    private static final String CART_ORDER = "newcart1";
    private static String CART_REFERENCE_VALUE;

    @Resource
    private CheckoutComOrderDao orderDao;

    @Resource
    private UserService userService;
    @Resource
    private ModelService modelService;
    @Resource
    private TypeService typeService;
    @Resource
    private ConfigurationService configurationService;

    private CurrencyModel currency;
    private UserModel user;

    @Before
    public void setUp() {
        getOrCreateLanguage("de");

        setUpOrderEntryPrepare();
        currency = setUpCurrency();
        user = userService.getAnonymousUser();
        setUpProduct();
        setUpOrder();

        final CartModel cart = createCart(CART_ORDER);
        cart.setCheckoutComPaymentReference(CART_REFERENCE_VALUE);
        modelService.save(cart);
        CART_REFERENCE_VALUE = cart.getCheckoutComPaymentReference();
    }

    @Test(expected = IllegalArgumentException.class)
    public void findAbstractOrderForPaymentReferenceNumber_WhenReferenceIsNull_ShouldThrowException() {
        orderDao.findAbstractOrderForPaymentReferenceNumber(null);
    }

    @Test
    public void findAbstractOrderForPaymentReferenceNumber_WhenReferenceIsWrong_ShouldReturnOptionalEmpty() {
        final Optional<AbstractOrderModel> result = orderDao.findAbstractOrderForPaymentReferenceNumber(WRONG_REFERENCE);

        assertFalse(result.isPresent());
    }

    @Test
    public void findAbstractOrderForPaymentReferenceNumber_WhenOrderReferenceIsCorrect_ShouldFindTheOrder() {
        final Optional<AbstractOrderModel> result = orderDao.findAbstractOrderForPaymentReferenceNumber(ORDER_REFERENCE_VALUE);

        assertTrue(result.isPresent());
        final OrderModel order = (OrderModel) result.get();
        assertEquals(ORDER_REFERENCE_VALUE, order.getCheckoutComPaymentReference());
        assertNull(order.getOriginalVersion());
    }

    @Test
    public void findAbstractOrderForPaymentReferenceNumber_WhenCartReferenceIsCorrect_ShouldFindTheCart() {
        final Optional<AbstractOrderModel> result = orderDao.findAbstractOrderForPaymentReferenceNumber(CART_REFERENCE_VALUE);

        assertTrue(result.isPresent());
        final CartModel cart = (CartModel) result.get();
        assertEquals(CART_REFERENCE_VALUE, cart.getCheckoutComPaymentReference());
    }

    private OrderModel createOrder(final String code) {
        final OrderModel order = modelService.create(OrderModel.class);
        order.setCode(code);
        order.setDate(new Date());
        order.setCurrency(currency);
        order.setNet(Boolean.TRUE);
        order.setUser(user);
        return order;
    }

    private CartModel createCart(final String code) {
        final CartModel cart = modelService.create(CartModel.class);
        cart.setCode(code);
        cart.setDate(new Date());
        cart.setCurrency(currency);
        cart.setNet(Boolean.TRUE);
        cart.setUser(user);
        return cart;
    }

    private void setUpOrder() {
        final OrderModel order = createOrder(NEW_ORDER);
        order.setCheckoutComPaymentReference(ORDER_REFERENCE_VALUE);
        modelService.save(order);
        final OrderModel clonedOrder = modelService.clone(order);
        clonedOrder.setOriginalVersion(order);
        clonedOrder.setVersionID("v1");
        modelService.save(clonedOrder);
    }

    private void setUpProduct() {
        final UnitModel unit = setUpUnitModel();
        final CatalogVersionModel catalogVersion = setUpCatalogVersion();

        final ProductModel prod = modelService.create(ProductModel.class);
        prod.setCode("product");
        prod.setUnit(unit);
        prod.setCatalogVersion(catalogVersion);
        prod.setApprovalStatus(ArticleApprovalStatus.APPROVED);
    }

    private CatalogVersionModel setUpCatalogVersion() {
        final CatalogModel cat = modelService.create(CatalogModel.class);
        cat.setId("catalog");
        final CatalogVersionModel cv = modelService.create(CatalogVersionModel.class);
        cv.setCatalog(cat);
        cv.setVersion("online");
        cv.setActive(Boolean.TRUE);
        modelService.saveAll(cat, cv);
        return cv;
    }

    private CurrencyModel setUpCurrency() {
        currency = modelService.create(CurrencyModel.class);
        currency.setIsocode("XYZ");
        currency.setActive(Boolean.TRUE);
        currency.setConversion(1.0d);
        currency.setDigits(2);
        currency.setSymbol("CCC");
        modelService.save(currency);
        return currency;
    }

    private UnitModel setUpUnitModel() {
        final UnitModel unit = modelService.create(UnitModel.class);
        unit.setCode("unit");
        unit.setConversion(1.0d);
        unit.setUnitType("type");
        modelService.save(unit);
        return unit;
    }

    private void setUpOrderEntryPrepare() {
        final DefaultAbstractOrderEntryPreparer preparer = new DefaultAbstractOrderEntryPreparer();
        preparer.setTypeService(typeService);
        preparer.setConfigurationService(configurationService);
    }
}
