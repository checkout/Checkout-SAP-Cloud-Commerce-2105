<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/responsive/formElement"%>
<%@ taglib prefix="chko-address" tagdir="/WEB-INF/tags/addons/checkoutaddon/responsive/checkout/address" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<jsp:useBean id="date" class="java.util.Date" />
<fmt:formatDate value="${date}" pattern="yyyy" var="currentYear" />
<spring:htmlEscape defaultHtmlEscape="true" />

<spring:url value="/my-account/payment-details" var="paymentDetailsUrl" htmlEscape="false" />

<div class="back-link border">
    <div class="row">
        <div class="container-lg col-md-6">
            <button type="button" class="addressBackBtn" data-back-to-addresses="${fn:escapeXml(paymentDetailsUrl)}">
                <span class="glyphicon glyphicon-chevron-left"></span>
            </button>
            <span class="label"><spring:theme code="checkoutcom.account.updatePaymentMethod.updatePaymentMethod" /></span>
        </div>
    </div>
</div>

<div class="row">
    <div class="container-lg col-md-6">
        <div class="account-section-content">
            <div class="account-section-form">
                <form:form
                        id="payment-form"
                        modelAttribute="creditCardDataForm"
                        method="POST"
                        action="${submitPaymentDataUrl}"
                >
                    <c:set
                            var="imageUrlAddon"
                            value="${fn:replace(commonResourcePath, 'responsive/common', 'addons/checkoutaddon/responsive/common/images/card-icons')}"
                    />

                    <div class="form-group">
                        <label for="card-holder-name">
                            <spring:theme code="checkoutcom.multi.cardholdername.label" text="Card holder name" />
                        </label>
                        <div class="input-container form-control">
                            <input type="hidden" name="creditCardCode" value="${code}" class="field" />
                            <input type="text" id="card-holder-name" name="accountHolderName" class="field" value="${creditCardDataForm.accountHolderName}" required/>
                        </div>
                    </div>

                    <div class="form-group">
                        <label>
                            <spring:theme code="checkoutcom.multi.cardnumber.label" text="Card number" />
                        </label>
                        <div class="input-container card-number form-control">
                            <div class="icon-container">
                                <img id="icon-card-number" src="${imageUrlAddon}/card.svg" alt="PAN" />
                            </div>
                            <div class="card-number-frame field">${creditCardDataForm.cardNumber}</div>
                        </div>
                    </div>

                    <div class="from-group">
                        <div class="date-and-code">
                            <div>
                                <label for="expiryMonth">
                                    <spring:theme code="checkoutcom.multi.expirymonth.label"/>
                                </label>
                                <div class="input-container expiry-date form-control">
                                    <div class="icon-container">
                                        <img src="${imageUrlAddon}/exp-date.svg" alt="Expiry date" />
                                    </div>
                                    <input type="number" id="expiryMonth" name="expiryMonth" class="expiry-date-frame field" min="1" max="12" value="${creditCardDataForm.expiryMonth}" required/>
                                </div>
                            </div>

                            <div>
                                <label for="expiryYear"><spring:theme code="checkoutcom.multi.expiryyear.label"/></label>
                                <div class="input-container cvv form-control">
                                    <div class="icon-container">
                                        <img src="${imageUrlAddon}/exp-date.svg" alt="Expiry date" />
                                    </div>
                                    <input type="number" id="expiryYear" name="expiryYear" class="expiry-date-frame field"  min="${currentYear}" value="${creditCardDataForm.expiryYear}" required/>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="accountActions">
                        <div class="row">
                            <div class="col-sm-6 col-sm-push-6 accountButtons">
                                <ycommerce:testId code="editAddress_saveAddress_button">
                                    <button
                                            class="btn btn-primary btn-block show_processing_message"
                                            type="submit"
                                    >
                                        <spring:theme code="text.button.save" />
                                    </button>
                                </ycommerce:testId>
                            </div>
                            <div class="col-sm-6 col-sm-pull-6 accountButtons">
                                <ycommerce:testId code="editAddress_cancelAddress_button">
                                    <a class="btn btn-block btn-default" href="${fn:escapeXml(paymentDetailsUrl)}">
                                        <spring:theme code="text.button.cancel" />
                                    </a>
                                </ycommerce:testId>
                            </div>
                        </div>
                    </div>
                </form:form>
            </div>
        </div>
    </div>
</div>
