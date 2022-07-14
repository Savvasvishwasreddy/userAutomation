package com.learningservices.itemselection.mfe.testscripts;

import java.util.HashMap;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.DevTools;
import org.testng.ITestContext;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.learningservices.itemselection.mfe.pages.BrowseAllContentPage;
import com.learningservices.itemselection.mfe.pages.BrowseProgramsPage;
import com.learningservices.itemselection.mfe.pages.EventListener;
import com.learningservices.itemselection.mfe.pages.HomepageHeader;
import com.learningservices.itemselection.mfe.pages.LoginPage;
import com.learningservices.itemselection.mfe.pages.ProgramPage;
import com.learningservices.itemselection.mfe.utils.GDriveUtils;
import com.learningservices.itemselection.mfe.utils.JSONParsarUtils;
import com.learningservices.itemselection.mfe.utils.RequestMockUtils;
import com.learningservices.utils.DataProviderUtils;
import com.learningservices.utils.Log;
import com.learningservices.utils.TestDataUtils;
import com.learningservices.utils.WebDriverFactory;

import LSTFAI.customfactories.EventFiringWebDriver;

public class TopNavSuite extends GDriveUtils {

	String webSite, browser, contentGraphqlURL, browseAllContentGraphqlURL, sheetName;
	
	@BeforeTest(alwaysRun = true)
	public void init(ITestContext context) {
		webSite = (System.getenv("webSite") != null ? System.getenv("webSite")
				: context.getCurrentXmlTest().getParameter("webSite"));
		if(webSite.contains("savvasrealizedev.com")) {
			contentGraphqlURL = "https://stage.toc-viewer-bff.savvasrealizedev.com/graphql";
			browseAllContentGraphqlURL = "https://browse-content-bff-service.nightly.savvasrealizedev.com/graphql";
			sheetName = "data_stage";
		} else {
			contentGraphqlURL = "https://toc-viewer-bff.savvasrealize.com/graphql";
			browseAllContentGraphqlURL = "https://browse-content-bff-service.savvasrealize.com/graphql";
			sheetName = "data_prod";			
		}
	}

	/**
	 * RLZ34507:Verify LTIA teacher stays in same page and Import cart items persist when clicking Browse in Browse Programs page
	 * @param browser
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", description = "RLZ34507:Verify LTIA teacher stays in same page and Import cart items persist when clicking Browse in Browse Programs page")
	public void RLZ34507(String browser) throws Exception {
		
		WebDriver chromeDriver = WebDriverFactory.get(browser);
		EventFiringWebDriver driver = new EventFiringWebDriver(chromeDriver);
		EventListener eventListner = new EventListener();
		driver.register(eventListner);
		
		
		LoginPage loginPage = new LoginPage(driver, webSite).get();
		HashMap<String, String> testData = TestDataUtils.getTestData(testDataWorkbookName, sheetName, "RLZ34507");
		Log.testCaseInfo(testData.get("TC_ID")+ ":"+testData.get("DESCRIPTION") + " [" + browser + "]");
		try {
			BrowseProgramsPage browsePage = loginPage.loginWithValidData(driver, testData.get("USERNAME"),testData.get("PASSWORD"));
			String json = JSONParsarUtils.readFileFromJsonFile("RLZ_34461_Programs_Contents.json");
			json = JSONParsarUtils.readFileFromJsonFile("RLZ_34461_Programs_Contents.json");
			DevTools tools = RequestMockUtils.setResponse(chromeDriver, contentGraphqlURL, "getContents", "post", json);
			ProgramPage programPage = browsePage.clickOnFirstProduct(driver);
			HomepageHeader header = new HomepageHeader(driver);
			RequestMockUtils.closeMock(tools);
			programPage.clickOnMultipleImportsAndVerifyTheItemsAdded(driver, 2);
			header.clickBrowse(driver);
			browsePage = new BrowseProgramsPage(driver);
			header.verifyImportedItemsCount(driver, 2);
		} catch (Exception e) {
			Log.exception(e, driver);
		} finally {
			driver.quit();
			Log.testCaseResult();
			Log.endTestCase();
		}
	}

	/**
	 * RLZ34530:Verify LTIA teacher navigates to Browse Programs page and Import cart items persist when clicking Browse in Discover search page
	 * @param browser
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", description = "RLZ34530:Verify LTIA teacher navigates to Browse Programs page and Import cart items persist when clicking Browse in Discover search page")
	public void RLZ34530(String browser) throws Exception {
		WebDriver chromeDriver = WebDriverFactory.get(browser);
		EventFiringWebDriver driver = new EventFiringWebDriver(chromeDriver);
		EventListener eventListner = new EventListener();
		driver.register(eventListner);
		LoginPage LoginPage = new LoginPage(driver, webSite).get();
		HashMap<String,String> testData = TestDataUtils.getTestData(testDataWorkbookName, sheetName, "RLZ34530");
		Log.testCaseInfo(testData.get("TC_ID")+ ":"+testData.get("DESCRIPTION") + " [" + browser + "]");
		try {
			BrowseProgramsPage BrowseProgramsPage = LoginPage.loginWithValidData(driver, testData.get("USERNAME"), testData.get("PASSWORD"));
			String json = JSONParsarUtils.readFileFromJsonFile("BrowseAll_Contents.json");
			DevTools tools = RequestMockUtils.setResponse(chromeDriver, browseAllContentGraphqlURL, "getSearchResult", "post", json);
			BrowseAllContentPage BrowseAllContentPage = BrowseProgramsPage.clickBrowseAll(driver).get();
			HomepageHeader HomepageHeader = BrowseAllContentPage.clickOnBrowseAllImportsUsingSizeAndVerify(driver,5);
			RequestMockUtils.closeMock(tools);
			HomepageHeader.clickBrowse(driver);
			BrowseProgramsPage = new BrowseProgramsPage(driver).get();
			HomepageHeader.verifyImportedProgramItemsCount(driver, 5);
		} catch (Exception e) {
			Log.exception(e, driver);
		} finally {
			Log.testCaseResult();
			Log.endTestCase();
			driver.quit();
		}
	}

	/**
	 * RLZ34531:Verify LTIA teacher navigates to Discover search page and Import cart items persist when clicking Browse All Content in Browses Program page
	 * @param browser
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", description = "RLZ34531:Verify LTIA teacher navigates to Discover search page and Import cart items persist when clicking Browse All Content in Browses Program page")
	public void RLZ34531(String browser) throws Exception {
		WebDriver chromeDriver = WebDriverFactory.get(browser);
		EventFiringWebDriver driver = new EventFiringWebDriver(chromeDriver);
		EventListener eventListner = new EventListener();
		driver.register(eventListner);
		LoginPage loginPage = new LoginPage(driver, webSite).get();
		HashMap<String, String> testData = TestDataUtils.getTestData(testDataWorkbookName, sheetName, "RLZ34531");
		Log.testCaseInfo(testData.get("TC_ID")+ ":"+testData.get("DESCRIPTION") + " [" + browser + "]");
		try {
			BrowseProgramsPage browsePage = loginPage.loginWithValidData(driver, testData.get("USERNAME"),testData.get("PASSWORD"));
			String json = JSONParsarUtils.readFileFromJsonFile("RLZ_34461_Programs_Contents.json");
			json = JSONParsarUtils.readFileFromJsonFile("RLZ_34461_Programs_Contents.json");
			DevTools tools = RequestMockUtils.setResponse(chromeDriver, contentGraphqlURL, "getContents", "post", json);
			ProgramPage programPage = browsePage.clickOnFirstProduct(driver);
			HomepageHeader header = new HomepageHeader(driver);
			RequestMockUtils.closeMock(tools);
			programPage.clickOnMultipleImportsAndVerifyTheItemsAdded(driver, 2);
			header.clickBrowse(driver);
			browsePage = new BrowseProgramsPage(driver).get();
			programPage.clickOnBrowseAllContent(driver);
			header.verifyImportedItemsCount(driver, 2);
		} catch (Exception e) {
			Log.exception(e, driver);
		} finally {
			Log.testCaseResult();
			Log.endTestCase();
			driver.quit();
		}
	}
}
