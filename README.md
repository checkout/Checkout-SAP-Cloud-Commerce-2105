# Checkout.com Connector for SAP Commerce Cloud
Checkout.com provides an end-to-end platform that helps you move faster, instead of holding you back. With flexible tools, granular data and deep insights, it’s the payments tech that unleashes your potential. So you can innovate, adapt to your markets, create outstanding customer experiences, and make smart decisions faster.
The Connector for SAP Commerce Cloud (formerly Hybris) enables customers to implement a global payment strategy through a single integration in a secure, compliant and unified approach. 

## Release Compatibility
This release is compatible with:
- SAP Commerce: B2C Accelerator of SAP Commerce Cloud 2105. It is advised to install the latest patch version of SAP Commerce Cloud.
- SAP Commerce REST API (OCC).
- Spartacus 4.2.
- Java 11.
- Checkout.com Java SDK version 3.

It is advised to use the latest release of this Connector available in GitHub.

# Installation and Usage

## Installation of the Connector with checkout.com payment functionality

Ensure that the version of SAP Commerce is supported for the plugin. Please view the Release Compatibility section for the current list of supported versions.

The Connector contains several extensions. Follow the following steps to include the Connector into your SAP Commerce application:

1. Unzip the supplied plugin zip file

2. Copy the extracted folders to the ${HYBRIS_BIN_DIR} of your SAP Commerce installation.

3. Run the ```ant clean``` command from within your bin/platform directory.

5. Copy the following lines into your localextensions.xml after <path dir="${HYBRIS_BIN_DIR}"/>. The extensions do not rely on any absolute paths so it is also possible to place the extensions in a different location (such as ${HYBRIS_BIN_DIR}/custom).
Run the command ```<path autoload="true" dir="${HYBRIS_BIN_DIR}/modules/checkoutcom"/>```

6. Run the commands below to install specific add-ons of the yaccelatorstorefront (replace "yacceleratorstorefront" with your custom storefront if relevant)

### Add-ons:
- B2C Accelerator: ```ant addoninstall -Daddonnames="checkoutaddon" -DaddonStorefront.yacceleratorstorefront="yacceleratorstorefront"```

### Optional

1. The checkoutsampledataaddon is optional, and can be installed by running the command:
```ant addoninstall -Daddonnames="checkoutsampledataaddon" -DaddonStorefront.yacceleratorstorefront="yacceleratorstorefront"```

2. Run the ```ant clean all``` command from within your bin/platform directory.

3. Run ```hybrisserver.sh``` to startup the SAP Commerce server.

4. Update your running system.using ```ant updatesystem```

Except for setting up your hosts file, the Checkout.com Connector will work initially without any external setup needed.

The add-ons are independent and can be installed on separate server instances.

## Installing the Connector using recipes

The Connector ships with gradle recipes to be used with the SAP Commerce installer:

B2C: b2c_acc_plus_checkout_com with b2c and checkout.com functionality. The recipe is based on the b2b_acc_plus recipes.

Recipes can be found under the installer folder. To use recipes on a clean installation, copy the folder hybris to your ${HYBRIS_BIN_DIR}

For local installations, the recipe generates a local.properties file with the properties defined in the recipe. You can optionally add your local.properties to the customconfig folder.

For cloud installations, generate a manifest.json that reflects different properties files per environment and aspect, languages packs and add-ons.

Install the Connector using recipes. Run the following commands:
- Create a solution from the accelerator templates and install the addons.
```HYBRIS_HOME/installer$ ./install.sh -r [RECIPE_NAME] setup```
- Build and initialize the platform
```HYBRIS_HOME/installer$ ./install.sh -r [RECIPE_NAME] initialize```
- Start a commerce suite instance
```HYBRIS_HOME/installer$ ./install.sh -r [RECIPE_NAME] start```
  
## Installing on [SAP Commerce Cloud](https://help.sap.com/viewer/product/SAP_COMMERCE_CLOUD_PUBLIC_CLOUD/v2105/en-US)
Follow the instructions below to install and deploy the Connector on SAP Commerce Cloud. The sample manifest.json included in the Connector serves as guide for the installation. Adapt your  manifest.json file to include Checkout.com extensions.  
The public, private and shared keys are included as properties in the manifest as placeholder. Add your keys as properties in the SAP Commerce Cloud environments.  

Follow [this guideline](https://help.sap.com/viewer/1be46286b36a4aa48205be5a96240672/v2105/en-US/1ee068bcce7845b8ab4ed9cdd54577fb.html) to prepare the repository for the deployment onto SAP Commerce Cloud. Include the Connector extensions in the folder `core-customize`.

# Spartacus Frontend
Spartacus is a lean, Angular-based JavaScript storefront for SAP Commerce Cloud. Spartacus talks to SAP Commerce Cloud exclusively through the Commerce REST API (OCC). The Connector for SAP Commerce Cloud supports the Spartacus frontend. Check out details and release notes in the Checkout.com repository for Spartacus.  

# Release Notes
- Added support for environments using NAS flows.

# Support
Contact your Checkout.com team if you have any question, technical problem or feature request for the SAP Commerce Cloud Connector.

# Contribution to this Connector
## Our Pledge

In the interest of fostering an open and welcoming environment, we as contributors and maintainers pledge to making participation in our project and
our community a harassment-free experience for everyone, regardless of age, body size, disability, ethnicity, sex characteristics, gender identity and expression,  level of experience, education, socio-economic status, nationality, personal appearance, race, religion, or sexual identity and orientation.

## Our Standards

Examples of behavior that contributes to creating a positive environment include:

* Using welcoming and inclusive language
* Being respectful of differing viewpoints and experiences
* Gracefully accepting constructive criticism
* Focusing on what is best for the community
* Showing empathy towards other community members

Examples of unacceptable behavior by participants include:

* The use of sexualized language or imagery and unwelcome sexual attention or advances
* Trolling, insulting/derogatory comments, and personal or political attacks
* Public or private harassment
* Publishing others' private information, such as a physical or electronic
 address, without explicit permission
* Other conduct which could reasonably be considered inappropriate in a professional setting

## Our Responsibilities

Project maintainers are responsible for clarifying the standards of acceptable behavior and are expected to take appropriate and fair corrective action in response to any instances of unacceptable behavior.

Project maintainers have the right and responsibility to remove, edit, or reject comments, commits, code, wiki edits, issues, and other contributions that are not aligned to this Code of Conduct, or to ban temporarily or permanently any contributor for other behaviors that they deem inappropriate, threatening, offensive, or harmful.

## Scope

This Code of Conduct applies both within project spaces and in public spaces when an individual is representing the project or its community. Examples of representing a project or community include using an official project e-mail address, posting via an official social media account, or acting as an appointed representative at an online or offline event. Representation of a project may be further defined and clarified by project maintainers.

## Enforcement

Instances of abusive, harassing, or otherwise unacceptable behavior may be reported by contacting the project team at support@checkout.com. All complaints will be reviewed and investigated and will result in a response that is deemed necessary and appropriate to the circumstances. The project team is obligated to maintain confidentiality with regard to the reporter of an incident. Further details of specific enforcement policies may be posted separately.

Project maintainers who do not follow or enforce the Code of Conduct in good faith may face temporary or permanent repercussions as determined by other members of the project's leadership.

## Attribution

This Code of Conduct is adapted from the [Contributor Covenant][homepage], version 1.4, available at https://www.contributor-covenant.org/version/1/4/code-of-conduct.html

[homepage]: https://www.contributor-covenant.org

For answers to common questions about this code of conduct, see https://www.contributor-covenant.org/faq

# License
This repository is open source and available under the MIT license.

Copyright (c) 2020 Checkout.com

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
