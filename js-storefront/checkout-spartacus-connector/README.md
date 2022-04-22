# Checkout.com Spartacus Connector for SAP Commerce Cloud
Checkout.com provides an end-to-end platform that helps you move faster, instead of holding you back. With flexible tools, granular data and deep insights, it’s the payments tech that unleashes your potential. So you can innovate, adapt to your markets, create outstanding customer experiences, and make smart decisions faster. The Connector for SAP Commerce Cloud (formerly Hybris) enables customers to implement a global payment strategy through a single integration in a secure, compliant and unified approach.

This [Checkout.com](https://www.checkout.com/) library adds payments capabilities to the Spartacus Storefront for SAP Commerce Cloud. 

## Release Compatibility
This library is tailored to the [Spartacus](https://sap.github.io/spartacus-docs/) Storefront:

This release is compatible with:
* Spartacus: version 4.2.x
* SAP Commerce Cloud: version 2105
* Angular CLI: Version 10.1 or later, < 11.
* Node.js: The most recent 12.x version is recommended, < 13.
* Yarn: Version 1.15 or later.
* Requires Spartacus Feature Modules: `checkout` and `order`

## Development 
Install the [Checkout.com SAP Commerce Cloud Connector](https://github.com/checkout/SAP-Cloud-Commerce-2105).
Run `yarn` and then `yarn run start` and the server will start on [http://localhost:4200](http://localhost:4200).   
When you make some changes in the code of library, you need to rebuild it (but rebuilding of the "example-storefront" is not required) - `yarn run start` is a shortcut for this.

## Testing
Run `ng test --watch` during development to automatically run the Angular test suite when you change a file

## Publish to NPM
Increase the version number in `package.json`

Make sure you are logged in and your account has access to the Checkout.com organisation 

* Remove all files from the node_modules/ and dist/ folders
* Run `yarn install` to install all required dependencies
* Run `yarn run test` to validate that there are no existing blocking errors in the code
* Run `yarn run build-prod` to build the library

If you are satisfied with the results from the tests and the library is built without problems you can publish the package.
* Run `npm publish --access public` in both `dist/checkout-spartacus-connector` and `dist/checkout-spartacus-translations`.

## Extending components
The source code of the connector can be found on [GitHub](https://github.com/checkout/SAP-Cloud-Commerce-2105). 

If you need to extend components, you can fork the repository so you are able to upgrade to future releases. In this fork, you can make your changes and import the library in your storefront.

If you don't want to fork, you can `extend` components, copy the template and the Angular Component into your project. This will mean that you have to be vigilant when a new release of the library is integrated.


## Release notes

### Release 4.2.0 
Use this release if you are using Spartacus 4.2.x
* Upgrade to Spartacus 4.2
* Show first name + last name as the card account holder
* Fix for ApplePay transaction status

### Release 1.0.1
* Added support for server-side rendering (SSR)

### Release 1.0.0
* Initial release of the Spartacus Connector for SAP Commerce Cloud
* Connectivity to the SAP Commerce API (OCC) and API endpoints configurable
* Credit Card payments
* APM’s
   * AliPay
   * ApplePay
   * Bancontact
   * Benefit Pay
   * EPS
   * Fawry
   * Giropay
   * GooglePay
   * iDeal
   * Klarna
   * KNet
   * Mada
   * Multibanco
   * Oxxo
   * PayPal
   * Poli
   * Przelewy24
   * QPay
   * Sepa
   * Sofort 
* Credit card form placeholder localisation
* Display card payment icon
* Lazy loaded feature module
* Translations moved to separate node module
