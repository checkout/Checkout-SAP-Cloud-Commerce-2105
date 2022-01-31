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
    <div class="ach-form-container">

        <c:set var="accountHolderName" value=""/>
        <c:if test="${not empty cartData.checkoutComPaymentInfo.billingAddress and not empty cartData.checkoutComPaymentInfo.billingAddress.firstName}">
            <c:set var="accountHolderName"
                   value="${cartData.checkoutComPaymentInfo.billingAddress.title} ${cartData.checkoutComPaymentInfo.billingAddress.firstName} ${cartData.checkoutComPaymentInfo.billingAddress.lastName}"/>
        </c:if>

        <label class="paymentFormFieldLabel" for="formAttributes['${'accountHolderName'}']"><spring:theme
                code="checkoutcom.ach.accountholder.name.label"
                text="ACCOUNT HOLDER NAME"/></label>
        <form:input cssClass="form-control ach-filed-required" type="text" id="accountHolderName"
                    path="formAttributes['${'accountHolderName'}']"
                    value="${accountHolderName}"/>
        <form:errors path="formAttributes['${'accountHolderName'}']" cssClass="error-message"/>

        <label class="paymentFormFieldLabel" for="formAttributes['${'accountType'}']"><spring:theme
                code="checkoutcom.ach.account.type.label"
                text="ACCOUNT TYPE"/></label>
        <form:select id="accountType" cssClass="form-control ach-filed-required" path="formAttributes['${'accountType'}']">
            <form:option value=""><spring:theme code="checkoutcom.payment.dropdown.select.label" /></form:option>
            <form:options items="${achAccountTypes}"></form:options>
        </form:select>
        <form:errors path="formAttributes['${'accountType'}']" cssClass="error-message"/>

        <label class="paymentFormFieldLabel" for="formAttributes['${'accountNumber'}']"><spring:theme
                code="checkoutcom.ach.account.number.label"
                text="ACCOUNT NUMBER"/></label>
        <form:input cssClass="form-control ach-filed-required" type="text" id="accountNumber"
                    path="formAttributes['${'accountNumber'}']"/>
        <form:errors path="formAttributes['${'accountNumber'}']" cssClass="error-message"/>

        <label class="paymentFormFieldLabel" for="formAttributes['${'routingNumber'}']"><spring:theme
                code="checkoutcom.ach.routing.number.label"
                text="ROUTING NUMBER"/></label>
        <form:input cssClass="form-control ach-filed-required" type="text" id="routingNumber"
                    path="formAttributes['${'routingNumber'}']"/>
        <form:errors path="formAttributes['${'routingNumber'}']" cssClass="error-message"/>

        <label id="companyNameLabel" class="paymentFormFieldLabel" for="formAttributes['${'companyName'}']"
               style="display: none"><spring:theme
                code="checkoutcom.ach.company.name.label"
                text="COMPANY NAME"/></label>
        <form:input cssClass="form-control" type="text" id="companyName"
                    path="formAttributes['${'companyName'}']" style="display: none"/>
        <form:errors path="formAttributes['${'companyName'}']" cssClass="error-message company-name-error"/>

        <form:hidden id="type" path="formAttributes['${'type'}']" value="${paymentMethod}"/>

        <div class="ach-info" style="display: none">
            <label>
                <spring:theme code="checkoutcom.ach.customer.info.title.label" arguments="${cartData.baseStoreName}"/>
            </label>
            <p>
                <spring:theme code="checkoutcom.ach.customer.info.paragraph.label"/>
            </p>
        </div>
    </div>
</form:form>
