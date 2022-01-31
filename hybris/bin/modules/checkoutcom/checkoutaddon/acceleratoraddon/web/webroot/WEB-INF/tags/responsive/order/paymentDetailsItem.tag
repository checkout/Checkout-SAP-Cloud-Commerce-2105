<%@ tag body-content="empty" trimDirectiveWhitespaces="true" %>
<%@ attribute name="order" required="true" type="de.hybris.platform.commercefacades.order.data.AbstractOrderData" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<spring:htmlEscape defaultHtmlEscape="true"/>
<div class="label-order">
    <spring:theme code="text.account.paymentType"/>
</div>
<div class="value-order">
    <c:choose>
        <c:when test="${not empty order.paymentInfo && not empty order.paymentInfo.cardTypeData}">
            ${fn:escapeXml(order.paymentInfo.cardTypeData.name)}
            ${fn:escapeXml(order.paymentInfo.cardNumber)}
        </c:when>
        <c:otherwise>
            ${fn:escapeXml(order.checkoutComPaymentInfo.type)}
        </c:otherwise>
    </c:choose>
</div>

