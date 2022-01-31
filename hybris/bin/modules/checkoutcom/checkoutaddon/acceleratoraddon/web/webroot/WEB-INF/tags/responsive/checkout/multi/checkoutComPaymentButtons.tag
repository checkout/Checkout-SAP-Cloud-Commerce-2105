<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/responsive/template" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>
<%@ taglib prefix="chko-multi-checkout" tagdir="/WEB-INF/tags/addons/checkoutaddon/responsive/checkout/multi" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>

<c:set var="countryCode" value=""/>

<c:choose>
    <c:when test="${not empty selectedCountryCode}">
        <c:set var="countryCode" value="${selectedCountryCode}"/>
    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="${not empty cartData.paymentInfo.billingAddress}">
                <c:set var="countryCode" value="${cartData.paymentInfo.billingAddress.country.isocode}"/>
            </c:when>
            <c:otherwise>
                <c:set var="countryCode" value="${cartData.deliveryAddress.country.isocode}"/>
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>

<chko-multi-checkout:paymentButtons requestCountryCode="${countryCode}"
                                    requestCurrencyCode="${cartData.totalPrice.currencyIso}"
                                    paymentDetailsForm="${paymentDetailsForm}"/>
