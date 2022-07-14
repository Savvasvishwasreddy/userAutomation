package com.learningservices.itemselection.mfe.testscripts;

import java.util.HashMap;
import java.util.List;

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

public class AddContentsSuite extends GDriveUtils {

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
	 * RLZ34456:Verify Canvas teacher is able to add a content to import cart from the program TOC page
	 * @param browser
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", description = "RLZ34456:Verify Canvas teacher is able to add a content to import cart from the program TOC page")
	public void RLZ34456(String browser) throws Exception {
		
		WebDriver chromeDriver = WebDriverFactory.get(browser);

		EventFiringWebDriver driver = new EventFiringWebDriver(chromeDriver);
		EventListener eventListner = new EventListener();
		driver.register(eventListner);

		HashMap<String,String> testData = TestDataUtils.getTestData(testDataWorkbookName, sheetName, "RLZ34456");
	
		Log.testCaseInfo(testData.get("TC_ID")+ ":"+testData.get("DESCRIPTION") + " [" + browser + "]");
		try {
		    LoginPage login = new LoginPage(driver, webSite).get();
		    String json = JSONParsarUtils.readFileFromJsonFile("RLZ34456_Programs.json");
		    BrowseProgramsPage browsePage = login.login(driver, testData.get("USERNAME"), testData.get("PASSWORD"));
		    json = JSONParsarUtils.readFileFromJsonFile("RLZ34456_Programs_Contents.json");
		    DevTools tools = RequestMockUtils.setResponse(chromeDriver, contentGraphqlURL, "getContents", "post", json);
		    ProgramPage programPage = browsePage.clickOnFirstProduct(driver);
		    Log.message("Program page gets opened");
		    RequestMockUtils.closeMock(tools);
		    RequestMockUtils.networkDisable(chromeDriver);
		    
		    List<String> expectedCartValue = JSONParsarUtils.getSearchTitles(json);
		    programPage.clickOnFirstImport(driver);
		    HomepageHeader header = new HomepageHeader(driver);
		    header.verifyImportMessage(driver);
		    header.verifyImportedItemsCount(driver, 1);
		    header.openCart(driver);
		    List<String> actualCartValue = header.getCartList(driver);
		    Log.assertThat(expectedCartValue.equals(actualCartValue), "Added Content is displayed in the cart", "Added Content is incorrectly displayed in the cart", driver);
		} catch (Exception e) {
			Log.exception(e, driver);
		} finally {
			driver.quit();
		    Log.testCaseResult();
			Log.endTestCase();
		}
	}

	/**
	 *RLZ34457:Verify Canvas teacher is able to add multiple content to import cart from the program TOC page.
	 * @param browser
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", description = "RLZ34457:Verify Canvas teacher is able to add multiple content to import cart from the program TOC page")
	public void RLZ34457(String browser) throws Exception {
		
		WebDriver chromeDriver = WebDriverFactory.get(browser);

		EventFiringWebDriver driver = new EventFiringWebDriver(chromeDriver);
		EventListener eventListner = new EventListener();
		driver.register(eventListner);

		HashMap<String,String> testData = TestDataUtils.getTestData(testDataWorkbookName, sheetName, "RLZ34457");

		Log.testCaseInfo(testData.get("TC_ID")+ ":"+testData.get("DESCRIPTION") + " [" + browser + "]");
		try {
		    LoginPage login = new LoginPage(driver, webSite).get();
		    BrowseProgramsPage browsePage = login.login(driver, testData.get("USERNAME"), testData.get("PASSWORD"));
		    String json = JSONParsarUtils.readFileFromJsonFile("RLZ34457_Programs_Contents.json");
		    DevTools tools = RequestMockUtils.setResponse(chromeDriver, contentGraphqlURL, "getContents", "post", json);
		    ProgramPage programPage = browsePage.clickOnFirstProduct(driver);
		    Log.message("Program page gets opened");
		    RequestMockUtils.closeMock(tools);
		    List<String> expectedCartValue = JSONParsarUtils.getSearchTitles(json);
		    int itemCount = programPage.clickOnMultipleImport(driver);
		    HomepageHeader header = new HomepageHeader(driver);
		    header.verifyImportedItemsCount(driver, itemCount);
		    header.openCart(driver);
		    List<String> actualCartValue = header.getCartList(driver);
		    Log.assertThat(expectedCartValue.equals(actualCartValue), "Added Content is displayed in the cart", "Added Content is incorrectly displayed in the cart", driver);
		} catch (Exception e) {
			Log.exception(e, driver);
		} finally {
			driver.quit();
		    Log.testCaseResult();
			Log.endTestCase();
		}
	}

	/**
	 *RLZ34458:Verify Canvas teacher is able to add multiple content to import cart from the lesson TOC page.
	 * @param browser
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", description = "RLZ34458:Verify Canvas teacher is able to add multiple content to import cart from the lesson TOC page.")
	public void RLZ34458(String browser) throws Exception {
		
		WebDriver chromeDriver = WebDriverFactory.get(browser);

		EventFiringWebDriver driver = new EventFiringWebDriver(chromeDriver);
		EventListener eventListner = new EventListener();
		driver.register(eventListner);

		HashMap<String,String> testData = TestDataUtils.getTestData(testDataWorkbookName, sheetName, "RLZ34458");

		Log.testCaseInfo(testData.get("TC_ID")+ ":"+testData.get("DESCRIPTION") + " [" + browser + "]");

		try {
		    LoginPage login = new LoginPage(driver, webSite).get();
		    BrowseProgramsPage browsePage = login.login(driver, testData.get("USERNAME"), testData.get("PASSWORD"));
		    ProgramPage programPage = browsePage.openProgram(driver, testData.get("PROGRAM_NAME")).get();
		    Log.message("Program page gets opened");
		    String json = JSONParsarUtils.readFileFromJsonFile("RLZ34458_Lessons_Contents.json");
		    DevTools tools = RequestMockUtils.setResponse(chromeDriver, contentGraphqlURL, "getContents", "post", json);
		    programPage.openItem(driver, testData.get("ITEM_NAME"));
		    int itemCount = programPage.clickOnMultipleImport(driver);
		    RequestMockUtils.closeMock(tools);
		    List<String> expectedCartValue = JSONParsarUtils.getSearchTitles(json);
		    HomepageHeader header = new HomepageHeader(driver);
		    header.verifyImportedItemsCount(driver, itemCount);
		    header.openCart(driver);
		    List<String> actualCartValue = header.getCartList(driver);
		    Log.assertThat(expectedCartValue.equals(actualCartValue), "Added Content is displayed in the cart", "Added Content is incorrectly displayed in the cart", driver);
		} catch (Exception e) {
			Log.exception(e, driver);
		} finally {
			driver.quit();
		    Log.testCaseResult();
			Log.endTestCase();
		}
	}

	/**
	 *RLZ34459:Verify Canvas teacher is able to add multiple assignable content to import cart from the teacher resource in the program TOC page
	 * @param browser
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", description = "RLZ34459:Verify Canvas teacher is able to add multiple assignable content to import cart from the teacher resource in the program TOC page")
	public void RLZ34459(String browser) throws Exception {
		
		EventFiringWebDriver driver = new EventFiringWebDriver(WebDriverFactory.get(browser));
		EventListener eventListner = new EventListener();
		driver.register(eventListner);

		HashMap<String,String> testData = TestDataUtils.getTestData(testDataWorkbookName, sheetName, "RLZ34459");

		Log.testCaseInfo(testData.get("TC_ID")+ ":"+testData.get("DESCRIPTION") + " [" + browser + "]");
		try {
		    LoginPage login = new LoginPage(driver, webSite).get();
		    BrowseProgramsPage browsePage = login.login(driver, testData.get("USERNAME"), testData.get("PASSWORD"));
		    ProgramPage programPage = browsePage.openProgram(driver, testData.get("PROGRAM_NAME"));
		    Log.message("Program page gets opened");
		    programPage.openTeacherResource();
		    int itemCount = programPage.clickOnMultipleImportTR(driver, 2);
		    HomepageHeader header = new HomepageHeader(driver);
		    header.verifyImportedItemsCount(driver, itemCount);
		} catch (Exception e) {
			Log.exception(e, driver);
		} finally {
			driver.quit();
		    Log.testCaseResult();
			Log.endTestCase();
		}
	}

	/**
	 *RLZ34460:Verify Canvas teacher is able to add multiple assignable content to import cart from the teacher resource in the lesson
	 * @param browser
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", description = "RLZ34460:Verify Canvas teacher is able to add multiple assignable content to import cart from the teacher resource in the lesson")
	public void RLZ34460(String browser) throws Exception {
		
		EventFiringWebDriver driver = new EventFiringWebDriver(WebDriverFactory.get(browser));
		EventListener eventListner = new EventListener();
		driver.register(eventListner);

		HashMap<String,String> testData = TestDataUtils.getTestData(testDataWorkbookName, sheetName, "RLZ34460");

		Log.testCaseInfo(testData.get("TC_ID")+ ":"+testData.get("DESCRIPTION") + " [" + browser + "]");
		try {
		    LoginPage login = new LoginPage(driver, webSite).get();
		    BrowseProgramsPage browsePage = login.login(driver, testData.get("USERNAME"), testData.get("PASSWORD"));
		    ProgramPage programPage = browsePage.openProgram(driver, testData.get("PROGRAM_NAME")).get();
		    Log.message("Program page gets opened");
		    programPage.openItem(driver, testData.get("ITEM_NAME")).get();
		    //if(sheetName.contains("prod")) {
		    	programPage.openItem(driver, testData.get("CONTENT_NAME")).get();
		   // }
		    Log.message("Lesson page gets opened");
		    programPage.openTeacherResource();
		    int itemCount = programPage.clickOnMultipleImportTR(driver, 1);
		    HomepageHeader header = new HomepageHeader(driver);
		    header.verifyImportedItemsCount(driver, itemCount);
		} catch (Exception e) {
			Log.exception(e, driver);
		} finally {
			driver.quit();
		    Log.testCaseResult();
			Log.endTestCase();
		}
	}

	/**
	 *RLZ34661:Verify Import cart displays message when canvas teacher click +Import for the content not in cart
	 * @param browser
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", description = "RLZ34661:Verify Import cart displays message when canvas teacher click +Import for the content not in cart")
	public void RLZ34661(String browser) throws Exception {
		WebDriver chromeDriver = WebDriverFactory.get(browser);
		EventFiringWebDriver driver = new EventFiringWebDriver(chromeDriver);
		EventListener eventListner = new EventListener();
		driver.register(eventListner);
		LoginPage LoginPage = new LoginPage(driver, webSite).get();
		HashMap<String,String> testData = TestDataUtils.getTestData(testDataWorkbookName, sheetName, "RLZ34661");
		Log.testCaseInfo(testData.get("TC_ID")+ ":"+testData.get("DESCRIPTION") + " [" + browser + "]");
		try {
			BrowseProgramsPage BrowseProgramsPage = LoginPage.loginWithValidData(driver, testData.get("USERNAME"), testData.get("PASSWORD"));
			String json = JSONParsarUtils.readFileFromJsonFile("RLZ34661_Program_Contents.json");
			DevTools tools = RequestMockUtils.setResponse(chromeDriver, contentGraphqlURL, "getContents", "post", json);
			ProgramPage ProgramPage = BrowseProgramsPage.clickOnFirstProduct(driver);
			ProgramPage.clickOnFirstImport(driver);
			HomepageHeader homepageHeader = new HomepageHeader(driver);
			homepageHeader.verifyImportMessage(driver);
			homepageHeader.waitForText(driver, "1 item(s) added");
			RequestMockUtils.closeMock(tools);
			ProgramPage.clickbackArrowInProgram(driver);
			json = JSONParsarUtils.readFileFromJsonFile("RLZ34661_Program_Contents_1.json");
			tools = RequestMockUtils.setResponse(chromeDriver, contentGraphqlURL, "getContents", "post", json);
			BrowseProgramsPage.clickOnSecondProduct(driver);
			ProgramPage.clickOnFirstImport(driver);
			homepageHeader = new HomepageHeader(driver);
			homepageHeader.verifyImportMessage(driver);
			homepageHeader.waitForText(driver, "2 item(s) added");
			RequestMockUtils.closeMock(tools);
			ProgramPage.clickbackArrowInProgram(driver);
			String jsons = JSONParsarUtils.readFileFromJsonFile("BrowseAll_Contents.json");
			tools = RequestMockUtils.setResponse(chromeDriver, browseAllContentGraphqlURL, "getSearchResult", "post", jsons);
			BrowseAllContentPage BrowseAllContentPage = BrowseProgramsPage.clickBrowseAll(driver);
			BrowseAllContentPage.clickOnBrowseAllImportsUsingCount(driver);
			homepageHeader = new HomepageHeader(driver);
			homepageHeader.verifyImportMessage(driver);
			homepageHeader.waitForText(driver, "3 item(s) added");
			RequestMockUtils.closeMock(tools);
			BrowseAllContentPage.clickOnBrowseAllImportsUsingCount(driver,2);
			homepageHeader = new HomepageHeader(driver);
			homepageHeader.verifyImportMessage(driver);
		} catch (Exception e) {
			Log.exception(e, driver);
		} finally {
			driver.quit();
			Log.testCaseResult();
			Log.endTestCase();
		}
	}

	/**
	 *RLZ34662:Verify Import cart displays message when canvas teacher click +Import for the content already in cart
	 * @param browser
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", description = "RLZ34662:Verify Import cart displays message when canvas teacher click +Import for the content already in cart")
	public void RLZ34662(String browser) throws Exception {
		EventFiringWebDriver driver = new EventFiringWebDriver(WebDriverFactory.get(browser));
		EventListener eventListner = new EventListener();
		driver.register(eventListner);
		LoginPage LoginPage = new LoginPage(driver, webSite).get();
		HashMap<String,String> testData = TestDataUtils.getTestData(testDataWorkbookName, sheetName, "RLZ34662");
		Log.testCaseInfo(testData.get("TC_ID")+ ":"+testData.get("DESCRIPTION") + " [" + browser + "]");
		try {
			BrowseProgramsPage BrowseProgramsPage = LoginPage.loginWithValidData(driver, testData.get("USERNAME"), testData.get("PASSWORD"));
			ProgramPage ProgramPage = BrowseProgramsPage.openProgram(driver, testData.get("PROGRAM_NAME")).get();
			ProgramPage.importItem(driver, testData.get("ITEM_NAME"));
			HomepageHeader homepageHeader = new HomepageHeader(driver);
			homepageHeader.verifyImportMessage(driver);
			homepageHeader.waitForText(driver, "1 item(s) added");
			ProgramPage.importItem(driver, testData.get("ITEM_NAME"));
			homepageHeader.verifyImportMessage(driver,"Item Already Added");
			ProgramPage.clickbackArrowInProgram(driver);
			BrowseAllContentPage BrowseAllContentPage = BrowseProgramsPage.clickBrowseAll(driver);
			BrowseAllContentPage.searchForContentInDiscoverSearchPage(driver, testData.get("ITEM_NAME"));
			BrowseAllContentPage.clickProgamContentsImportInDiscoverSearchUsingCount(driver);
			homepageHeader = new HomepageHeader(driver);
			homepageHeader.verifyImportMessage(driver,"Item Already Added");
			BrowseAllContentPage.searchForContentInDiscoverSearchPage(driver, testData.get("CONTENT_NAME"));
			BrowseAllContentPage.clickProgamContentsImportInDiscoverSearchUsingCount(driver);
			homepageHeader = new HomepageHeader(driver);
			homepageHeader.verifyImportMessage(driver);
			homepageHeader.waitForText(driver, "2 item(s) added");
			BrowseAllContentPage.clickProgamContentsImportInDiscoverSearchUsingCount(driver);
			homepageHeader = new HomepageHeader(driver);
			homepageHeader.verifyImportMessage(driver,"Item Already Added");
			homepageHeader.clickBrowse(driver);
			BrowseProgramsPage = new BrowseProgramsPage(driver).get();
			BrowseProgramsPage.clickBrowseProgramsUsingName(driver, testData.get("PROGRAM_NAME"));
			ProgramPage.importItem(driver, testData.get("CONTENT_NAME"));
			homepageHeader = new HomepageHeader(driver);
			homepageHeader.verifyImportMessage(driver,"Item Already Added");
			ProgramPage.importItem(driver, testData.get("CONTENT_NAME_1"));
			homepageHeader = new HomepageHeader(driver);
			homepageHeader.verifyImportMessage(driver);
		} catch (Exception e) {
			Log.exception(e, driver);
		} finally {
			driver.quit();
			Log.testCaseResult();
			Log.endTestCase();
		}
	}
}
