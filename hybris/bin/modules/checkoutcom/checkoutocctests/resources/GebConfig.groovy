/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved.
 */
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.openqa.selenium.remote.DesiredCapabilities


//*************************************************
//***********   HTMLUnit Driver  ******************
//*************************************************
DesiredCapabilities caps = DesiredCapabilities.firefox();
driver = { new HtmlUnitDriver(caps) }


//=================================================
//*********Working*********************************
//*************************************************

//*************************************************
//************* Firefox Driver ********************
//*************************************************
//driver={new FirefoxDriver()}


waiting { timeout = 30 }

reportsDir = "geb-reports"
