<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<form:form id="checkoutComPaymentDataForm"
           modelAttribute="paymentDataForm"
           method="POST"
           action="${submitPaymentDataUrl}"
           cssClass="paymentDataForm">
    <div class="sepa-form-container">

        <c:set var="firstName" value=""/>
        <c:if test="${not empty cartData.checkoutComPaymentInfo.billingAddress and not empty cartData.checkoutComPaymentInfo.billingAddress.firstName}">
            <c:set var="firstName" value="${cartData.checkoutComPaymentInfo.billingAddress.firstName}"/>
        </c:if>
        <label class="paymentFormFieldLabel" for="formAttributes['${'firstName'}']"><spring:theme
                code="checkoutcom.sepa.first.name.label"
                text="FIRST NAME"/></label>
        <form:input cssClass="form-control sepa-required" type="text" id="firstName"
                    path="formAttributes['${'firstName'}']"
                    value="${firstName}"/>
        <form:errors path="formAttributes['${'firstName'}']" cssClass="error-message"/>

        <c:set var="lastName" value=""/>
        <c:if test="${not empty cartData.checkoutComPaymentInfo.billingAddress and not empty cartData.checkoutComPaymentInfo.billingAddress.lastName}">
            <c:set var="lastName" value="${cartData.checkoutComPaymentInfo.billingAddress.lastName}"/>
        </c:if>
        <label class="paymentFormFieldLabel" for="formAttributes['${'lastName'}']"><spring:theme
                code="checkoutcom.sepa.last.name.label"
                text="LAST NAME"/></label>
        <form:input cssClass="form-control" type="text" id="lastName"
                    path="formAttributes['${'lastName'}']"
                    value="${lastName}"/>
        <form:errors path="formAttributes['${'lastName'}']" cssClass="error-message"/>

        <label class="paymentFormFieldLabel" for="formAttributes['${'paymentType'}']"><spring:theme
                code="checkoutcom.sepa.payment.type.label"
                text="ACCOUNT TYPE"/></label>

        <form:select id="paymentType" cssClass="form-control sepa-required"
                     path="formAttributes['${'paymentType'}']">

            <form:option value=""><spring:theme code="checkoutcom.payment.dropdown.select.label"/></form:option>
            <c:forEach items="${sepaPaymentTypes}" var="paymentType">
                <option value="${paymentType.key}" label="${paymentType.value}"></option>
            </c:forEach>
        </form:select>
        <form:errors path="formAttributes['${'paymentType'}']" cssClass="error-message"/>

        <label class="paymentFormFieldLabel" for="formAttributes['${'accountIban'}']"><spring:theme
                code="checkoutcom.sepa.account.iban.label"
                text="ACCOUNT IBAN"/></label>
        <form:input cssClass="form-control sepa-required" type="text" id="accountIban"
                    path="formAttributes['${'accountIban'}']"/>
        <form:errors path="formAttributes['${'accountIban'}']" cssClass="error-message"/>

        <c:set var="addressLine1" value=""/>
        <c:if test="${not empty cartData.checkoutComPaymentInfo.billingAddress and not empty cartData.checkoutComPaymentInfo.billingAddress.line1}">
            <c:set var="addressLine1" value="${cartData.checkoutComPaymentInfo.billingAddress.line1}"/>
        </c:if>
        <label class="paymentFormFieldLabel" for="formAttributes['${'addressLine1'}']"><spring:theme
                code="checkoutcom.sepa.address.line1.label"
                text="ADDRESS LINE 1"/></label>
        <form:input cssClass="form-control sepa-required" type="text" id="addressLine1"
                    path="formAttributes['${'addressLine1'}']"
                    value="${addressLine1}"/>
        <form:errors path="formAttributes['${'addressLine1'}']" cssClass="error-message"/>

        <c:set var="addressLine2" value=""/>
        <c:if test="${not empty cartData.checkoutComPaymentInfo.billingAddress and not empty cartData.checkoutComPaymentInfo.billingAddress.line2}">
            <c:set var="addressLine2" value="${cartData.checkoutComPaymentInfo.billingAddress.line2}"/>
        </c:if>
        <label class="paymentFormFieldLabel" for="formAttributes['${'addressLine2'}']"><spring:theme
                code="checkoutcom.sepa.address.line2.label"
                text="ADDRESS LINE 2 (OPTIONAL)"/></label>
        <form:input cssClass="form-control" type="text" id="addressLine2"
                    path="formAttributes['${'addressLine2'}']"
                    value="${addressLine2}"/>
        <form:errors path="formAttributes['${'addressLine2'}']" cssClass="error-message"/>

        <c:set var="city" value=""/>
        <c:if test="${not empty cartData.checkoutComPaymentInfo.billingAddress and not empty cartData.checkoutComPaymentInfo.billingAddress.town}">
            <c:set var="city" value="${cartData.checkoutComPaymentInfo.billingAddress.town}"/>
        </c:if>
        <label class="paymentFormFieldLabel" for="formAttributes['${'city'}']"><spring:theme
                code="checkoutcom.sepa.city.label"
                text="CITY"/></label>
        <form:input cssClass="form-control sepa-required" type="text" id="city"
                    path="formAttributes['${'city'}']"
                    value="${city}"/>
        <form:errors path="formAttributes['${'city'}']" cssClass="error-message"/>

        <c:set var="postalCode" value=""/>
        <c:if test="${not empty cartData.checkoutComPaymentInfo.billingAddress and not empty cartData.checkoutComPaymentInfo.billingAddress.postalCode}">
            <c:set var="postalCode" value="${cartData.checkoutComPaymentInfo.billingAddress.postalCode}"/>
        </c:if>
        <label class="paymentFormFieldLabel" for="formAttributes['${'postalCode'}']"><spring:theme
                code="checkoutcom.sepa.postal.code.label"
                text="POSTAL CODE/ZIP CODE"/></label>
        <form:input cssClass="form-control sepa-required" type="text" id="postalCode"
                    path="formAttributes['${'postalCode'}']"
                    value="${postalCode}"/>
        <form:errors path="formAttributes['${'postalCode'}']" cssClass="error-message"/>

        <label class="paymentFormFieldLabel" for="formAttributes['${'country'}']"><spring:theme
                code="checkoutcom.sepa.country.label"
                text="COUNTRY"/></label>
        <form:select id="sepaCountry" path="formAttributes['${'country'}']" cssClass="form-control sepa-required">
        <c:forEach items="${countries}" var="country">
        <option value="${country.isocode}" label="${country.name}"
            ${cartData.checkoutComPaymentInfo.billingAddress.country.isocode == country.isocode ? 'selected="selected"' : ''}>
            </c:forEach>
                </form:select>
                <form:errors path="formAttributes['${'country'}']" cssClass="error-message"/>

                <form:hidden id="type" path="formAttributes['${'type'}']" value="${paymentMethod}"/>

            <div class="sepa-info" style="display: none">
                <p>
                    <spring:theme code="checkoutcom.sepa.customer.mandate.paragraph1.label"
                                  arguments="${cartData.store}"/>
                    <spring:theme code="checkoutcom.sepa.customer.mandate.paragraph2.label"/>
                </p>
            </div>
    </div>
</form:form>
