<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="order" required="true" type="de.hybris.platform.commercefacades.order.data.AbstractOrderData" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="order" tagdir="/WEB-INF/tags/responsive/order" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<spring:htmlEscape defaultHtmlEscape="true"/>
<div class="label-order">
    <spring:theme code="text.account.paymentDetails.billingAddress"/>
</div>
<div class="value-order">
    <c:choose>
        <c:when test="${not empty order.paymentInfo && not empty order.paymentInfo.cardTypeData}">
            <order:addressItem address="${order.paymentInfo.billingAddress}"/>
        </c:when>
        <c:otherwise>
            <order:addressItem address="${order.checkoutComPaymentInfo.billingAddress}"/>
        </c:otherwise>
    </c:choose>
</div>
