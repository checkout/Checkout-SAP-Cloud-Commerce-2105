<%@ attribute name="cartData" required="true" type="de.hybris.platform.commercefacades.order.data.CartData" %>
<%@ attribute name="paymentInfo" required="true"
              type="de.hybris.platform.commercefacades.order.data.CCPaymentInfoData" %>
<%@ attribute name="checkoutComPaymentInfo" required="true"
              type="com.checkout.hybris.facades.beans.CheckoutComPaymentInfoData" %>
<%@ attribute name="showPaymentInfo" required="false" type="java.lang.Boolean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<spring:htmlEscape defaultHtmlEscape="true"/>

<c:if test="${showPaymentInfo}">

    <ul class="checkout-order-summary-list">
    <li class="checkout-order-summary-list-heading">
    <div class="title"><spring:theme code="checkout.multi.payment" text="Payment:"/></div>
    <div class="address">
    <c:choose>
        <c:when test="${not empty paymentInfo}">
            <c:if test="${not empty paymentInfo.billingAddress}"> ${fn:escapeXml(paymentInfo.billingAddress.title)}</c:if>
            ${fn:escapeXml(paymentInfo.billingAddress.firstName)} ${fn:escapeXml(paymentInfo.billingAddress.lastName)}, ${fn:escapeXml(paymentInfo.cardTypeData.name)},
            ${fn:escapeXml(paymentInfo.cardNumber)}, ${fn:escapeXml(paymentInfo.expiryMonth)}/${fn:escapeXml(paymentInfo.expiryYear)}
            <br/>
            <c:if test="${not empty paymentInfo.billingAddress}">${fn:escapeXml(paymentInfo.billingAddress.line1)},
                <c:if test="${not empty paymentInfo.billingAddress.line2}">${fn:escapeXml(paymentInfo.billingAddress.line2)},</c:if>
                ${fn:escapeXml(paymentInfo.billingAddress.town)}, ${fn:escapeXml(paymentInfo.billingAddress.region.name)}&nbsp;${fn:escapeXml(paymentInfo.billingAddress.postalCode)}, ${fn:escapeXml(paymentInfo.billingAddress.country.name)}
            </c:if>
            <br/><c:if
                test="${not empty paymentInfo.billingAddress.phone }">${fn:escapeXml(paymentInfo.billingAddress.phone)}</c:if>
        </c:when>
        <c:otherwise>
            <c:if test="${not empty checkoutComPaymentInfo.billingAddress}"> ${fn:escapeXml(checkoutComPaymentInfo.billingAddress.title)}</c:if>
            ${fn:escapeXml(checkoutComPaymentInfo.billingAddress.firstName)} ${fn:escapeXml(checkoutComPaymentInfo.billingAddress.lastName)}, ${fn:escapeXml(checkoutComPaymentInfo.type)}
            <br/>
            <c:if test="${not empty checkoutComPaymentInfo.billingAddress}">${fn:escapeXml(checkoutComPaymentInfo.billingAddress.line1)},
                <c:if test="${not empty checkoutComPaymentInfo.billingAddress.line2}">${fn:escapeXml(checkoutComPaymentInfo.billingAddress.line2)},</c:if>
                ${fn:escapeXml(checkoutComPaymentInfo.billingAddress.town)}, ${fn:escapeXml(checkoutComPaymentInfo.billingAddress.region.name)}&nbsp;${fn:escapeXml(checkoutComPaymentInfo.billingAddress.postalCode)}, ${fn:escapeXml(checkoutComPaymentInfo.billingAddress.country.name)}
            </c:if>
            <br/><c:if
                test="${not empty checkoutComPaymentInfo.billingAddress.phone }">${fn:escapeXml(checkoutComPaymentInfo.billingAddress.phone)}</c:if>
            </div>
            </li>
            </ul>
        </c:otherwise>
    </c:choose>
</c:if>
