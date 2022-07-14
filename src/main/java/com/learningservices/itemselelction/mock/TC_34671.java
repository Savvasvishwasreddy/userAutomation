package com.learningservices.itemselelction.mock;

import java.util.HashMap;

import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.learningservices.itemselection.mfe.pages.BrowseAllContentPage;
import com.learningservices.itemselection.mfe.pages.BrowseProgramsPage;
import com.learningservices.itemselection.mfe.pages.ContentInfoPage;
import com.learningservices.itemselection.mfe.pages.EventListener;
import com.learningservices.itemselection.mfe.pages.HomepageHeader;
import com.learningservices.itemselection.mfe.pages.LoginPage;
import com.learningservices.utils.DataProviderUtils;
import com.learningservices.utils.Log;
import com.learningservices.utils.TestDataUtils;
import com.learningservices.utils.WebDriverFactory;

import LSTFAI.customfactories.EventFiringWebDriver;


public class TC_34671 {


	WebDriver driver;
	String webSite, browser;

	@BeforeTest(alwaysRun = true)
	public void init(ITestContext context) {
		webSite = (System.getProperty("webSite") != null ? System.getProperty("webSite")
				: context.getCurrentXmlTest().getParameter("webSite"));
	}
//
	/** Description - Verify Import cart persists after canvas teacher preview content in Discover search page
	 * @param browser
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", description = "TC_RLZ_34671")
	public void TC_SCT_34671(String browser) throws Exception {
		EventFiringWebDriver driver = new EventFiringWebDriver(WebDriverFactory.get(browser));
	    EventListener eventListner = new EventListener();
		driver.register(eventListner);
		
		LoginPage loginPage = new LoginPage(driver, webSite).get();
		HashMap<String, String> testData = TestDataUtils.getTestData("Testing.xlsx", "data", "TC_RLZ_34671");
		Log.testCaseInfo(testData.get("DESCRIPTION") + " [" + browser + "]");
		try {
			BrowseProgramsPage browserProgramsPage = loginPage.loginWithValidData(driver, testData.get("USERNAME"),testData.get("PASSWORD"));
			HomepageHeader header = new HomepageHeader(driver);
			BrowseAllContentPage browseAllPage =browserProgramsPage.clickOnBrowseAllContent(driver);
			browseAllPage.clickOnMultipleImportsAndVerifyTheItemsAddedInBrowseAllContent(driver, 1);
			browseAllPage.clickFirstContentOfBrowseAllContent(driver);
			ContentInfoPage contentInfo= new ContentInfoPage(driver);
			contentInfo.clickExitContent(driver, "");
			header.clickImportedItemsCart(driver, 1);
		} catch (Exception e) {
			Log.exception(e, driver);
		} finally {
			Log.testCaseResult();
			Log.endTestCase();
			driver.quit();
		}
	}
}