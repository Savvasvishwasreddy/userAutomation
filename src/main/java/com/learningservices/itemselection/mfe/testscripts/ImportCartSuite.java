package com.learningservices.itemselection.mfe.testscripts;

import java.util.ArrayList;
import java.util.HashMap;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.DevTools;
import org.testng.ITestContext;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.learningservices.itemselection.mfe.pages.BrowseAllContentPage;
import com.learningservices.itemselection.mfe.pages.BrowseProgramsPage;
import com.learningservices.itemselection.mfe.pages.ContentInfoPage;
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

public class ImportCartSuite extends GDriveUtils {

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
	 *RLZ34423:Verify UI of the Import cart in 'Link Resource from External Tool' modal
	 * @param browser
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", description = "RLZ34423:Verify UI of the Import cart in 'Link Resource from External Tool' modal")
	public void RLZ34423(String browser) throws Exception {
		EventFiringWebDriver driver = new EventFiringWebDriver(WebDriverFactory.get(browser));
		EventListener eventListner = new EventListener();
		driver.register(eventListner);
		LoginPage LoginPage = new LoginPage(driver, webSite).get();
		HashMap<String,String> testData = TestDataUtils.getTestData(testDataWorkbookName, sheetName, "RLZ34423");
		Log.testCaseInfo(testData.get("TC_ID")+ ":"+testData.get("DESCRIPTION") + " [" + browser + "]");
		try {
			LoginPage.loginWithValidData(driver, testData.get("USERNAME"),testData.get("PASSWORD"));
			HomepageHeader HomePageHeader = new HomepageHeader(driver);
			HomePageHeader.verifyCartImportButtonDisabled(driver);
			HomePageHeader.verifyCartContainsTxtNoItemAdded(driver);
			HomePageHeader.verifyBgColourOfAddCartIsBlue(driver);
		} catch (Exception e) {
			Log.exception(e, driver);
		} finally {
			driver.quit();
			Log.testCaseResult();
			Log.endTestCase();
		}
	}

	/**
	 *RLZ34424:Verify canvas teacher is able to see number of content Items added to the Import cart
	 * @param browser
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", description = "RLZ34424:Verify canvas teacher is able to see number of content Items added to the Import cart")
	public void RLZ34424(String browser) throws Exception {
		WebDriver chromeDriver = WebDriverFactory.get(browser);
		EventFiringWebDriver driver = new EventFiringWebDriver(chromeDriver);
		EventListener eventListner = new EventListener();
		driver.register(eventListner);
		LoginPage LoginPage = new LoginPage(driver, webSite).get();
		HashMap<String,String> testData = TestDataUtils.getTestData(testDataWorkbookName, sheetName, "RLZ34424");
		Log.testCaseInfo(testData.get("TC_ID")+ ":"+testData.get("DESCRIPTION") + " [" + browser + "]");
		try {
			BrowseProgramsPage BrowseProgramsPage=LoginPage.loginWithValidData(driver, testData.get("USERNAME"),testData.get("PASSWORD"));
			HomepageHeader HomePageHeader = new HomepageHeader(driver);
			HomePageHeader.verifyCartContainsTxtNoItemAdded(driver);
			String json = JSONParsarUtils.readFileFromJsonFile("Program_Contents.json");
			DevTools tools = RequestMockUtils.setResponse(chromeDriver, contentGraphqlURL, "getContents", "post", json);
			ProgramPage ProgramPage =BrowseProgramsPage.clickOnFirstProduct(driver);
			ProgramPage.clickOnProgramContentImportsUsingIndexAndVerify(driver,1);
			ProgramPage.clickOnProgramContentImportsUsingIndexAndVerify(driver, 2);
			ProgramPage.clickOnProgramContentImportsUsingIndexAndVerify(driver, 3);	
			RequestMockUtils.closeMock(tools);
		} catch (Exception e) {
			Log.exception(e, driver);
		} finally {
			driver.quit();
			Log.testCaseResult();
			Log.endTestCase();
		}
	}

	/**
	 *RLZ34425:Verify scroll bar in the Import cart dropdown when lots of contents are added to the cart
	 * @param browser
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", description = "RLZ34425:Verify scroll bar in the Import cart dropdown when lots of contents are added to the cart")
	public void RLZ34425(String browser) throws Exception {
		WebDriver chromeDriver = WebDriverFactory.get(browser);
		EventFiringWebDriver driver = new EventFiringWebDriver(chromeDriver);
		EventListener eventListner = new EventListener();
		driver.register(eventListner);
		LoginPage LoginPage = new LoginPage(driver, webSite).get();
		HashMap<String,String> testData = TestDataUtils.getTestData(testDataWorkbookName, sheetName, "RLZ34425");
		Log.testCaseInfo(testData.get("TC_ID")+ ":"+testData.get("DESCRIPTION") + " [" + browser + "]");
		try {
			BrowseProgramsPage BrowseProgramsPage=LoginPage.loginWithValidData(driver, testData.get("USERNAME"),testData.get("PASSWORD"));
			String json = JSONParsarUtils.readFileFromJsonFile("BrowseAll_Contents.json");
			DevTools tools = RequestMockUtils.setResponse(chromeDriver, browseAllContentGraphqlURL, "getSearchResult", "post", json);
			BrowseAllContentPage BrowseAllContentPage=BrowseProgramsPage.clickBrowseAll(driver);
			HomepageHeader HomepageHeader = BrowseAllContentPage.clickOnBrowseAllImportsUsingSizeAndVerify(driver, 20);
			RequestMockUtils.closeMock(tools);
			HomepageHeader.clickImportCartDropdownIcon(driver);
			HomepageHeader.verifyAbleToScrollThroughAllItemsInImportCartDropDown(driver,20); 
		} catch (Exception e) {
			Log.exception(e, driver);
		} finally {
			driver.quit();
			Log.testCaseResult();
			Log.endTestCase();
		}
	}

	/**
	 *RLZ34426:Verify canvas teacher is able to see list of contents in the import cart before importing to canvas
	 * @param browser
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", description = "RLZ34426:Verify canvas teacher is able to see list of contents in the import cart before importing to canvas")
	public void RLZ34426(String browser) throws Exception {
		WebDriver chromeDriver = WebDriverFactory.get(browser);
		EventFiringWebDriver driver = new EventFiringWebDriver(chromeDriver);
		EventListener eventListner = new EventListener();
		driver.register(eventListner);
		LoginPage LoginPage = new LoginPage(driver, webSite).get();
		HashMap<String,String> testData = TestDataUtils.getTestData(testDataWorkbookName, sheetName, "RLZ34426");
		Log.testCaseInfo(testData.get("TC_ID")+ ":"+testData.get("DESCRIPTION") + " [" + browser + "]");
		try {
			BrowseProgramsPage BrowseProgramsPage = LoginPage.loginWithValidData(driver, testData.get("USERNAME"),testData.get("PASSWORD"));
			String json = JSONParsarUtils.readFileFromJsonFile("BrowseAll_Contents.json");
			DevTools tools = RequestMockUtils.setResponse(chromeDriver, browseAllContentGraphqlURL, "getSearchResult", "post", json);
			BrowseAllContentPage BrowseAllContentPage = BrowseProgramsPage.clickBrowseAll(driver);
			HomepageHeader HomepageHeader = BrowseAllContentPage.clickOnBrowseAllImportsUsingSizeAndVerify(driver, 6);
			RequestMockUtils.closeMock(tools);
			ArrayList<String> browseAllProgramNamesForGivenCount = BrowseAllContentPage.getBrowseAllProgramNamesForGivenCount(driver,6);
			ArrayList<String> browseAllProgramImgForGivenCount = BrowseAllContentPage.getBrowseAllProgramImgForGivenCount(driver,6);
			HomepageHeader.clickImportCartDropdownIcon(driver);
			ArrayList<String> programNamesInAddToCartDropDrownForGivenCount = HomepageHeader.getBrowseAllProgramNamesInAddToCartDropDrownForGivenCount(driver,6);
			ArrayList<String> ProgramImgInAddToCartDropDrownForGivenCount = HomepageHeader.getBrowseAllProgramImgInAddToCartDropDrownForGivenCount(driver,6);
			HomepageHeader.verifyBrowseAllAddedProgramsNamesInCartMatchesWithImported(driver,browseAllProgramNamesForGivenCount,programNamesInAddToCartDropDrownForGivenCount);
			HomepageHeader.verifyBrowseAllAddedProgramsImgInCartMatchesWithImported(driver,ProgramImgInAddToCartDropDrownForGivenCount,browseAllProgramImgForGivenCount);
			HomepageHeader.verifyBrowseAllRemoveIconPresentForImportedProgramsForGivenCount(driver,6);
		} catch (Exception e) {
			Log.exception(e, driver);
		} finally {
			driver.quit();
			Log.testCaseResult();
			Log.endTestCase();
		}
	}

	/**
	 *RLZ34427:Verify canvas teacher is able to hide list of contents in the import cart
	 * @param browser
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", description = "RLZ34427:Verify canvas teacher is able to hide list of contents in the import cart")
	public void RLZ34427(String browser) throws Exception {
		WebDriver chromeDriver = WebDriverFactory.get(browser);
		EventFiringWebDriver driver = new EventFiringWebDriver(chromeDriver);
		EventListener eventListner = new EventListener();
		driver.register(eventListner);
		LoginPage LoginPage = new LoginPage(driver, webSite).get();
		HashMap<String,String> testData = TestDataUtils.getTestData(testDataWorkbookName, sheetName, "RLZ34427");
		Log.testCaseInfo(testData.get("TC_ID")+ ":"+testData.get("DESCRIPTION") + " [" + browser + "]");
		try {
			BrowseProgramsPage BrowseProgramsPage = LoginPage.loginWithValidData(driver, testData.get("USERNAME"),testData.get("PASSWORD"));
			String json = JSONParsarUtils.readFileFromJsonFile("BrowseAll_Contents.json");
			DevTools tools = RequestMockUtils.setResponse(chromeDriver, browseAllContentGraphqlURL, "getSearchResult", "post", json);
			BrowseAllContentPage BrowseAllContentPage = BrowseProgramsPage.clickBrowseAll(driver);
			HomepageHeader HomepageHeader = BrowseAllContentPage.clickOnBrowseAllImportsUsingSizeAndVerify(driver, 6);
			RequestMockUtils.closeMock(tools);
			HomepageHeader.clickImportCartDropdownIcon(driver);
			HomepageHeader.verifyBrowseAllCartDropDownSidePannelIsDisplayed(driver);
			HomepageHeader.clickImportCartDropdownIcon(driver);
			HomepageHeader.verifyBrowseAllCartDropDownSidePannelIsNotDisplayed(driver);
			HomepageHeader.clickImportCartDropdownIcon(driver);
			HomepageHeader.clickOutsideImportCartDropDown(driver);
		} catch (Exception e) {
			Log.exception(e, driver);
		} finally {
			driver.quit();
			Log.testCaseResult();
			Log.endTestCase();
		}
	}

	/**
	 * RLZ34461:Verify canvas teacher is able to remove all contents from the Import cart 
	 * @param browser
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", description = "RLZ34461:Verify canvas teacher is able to remove all contents from the Import cart")
	public void RLZ34461(String browser) throws Exception {
		WebDriver chromeDriver = WebDriverFactory.get(browser);
		EventFiringWebDriver driver = new EventFiringWebDriver(chromeDriver);
		EventListener eventListner = new EventListener();
		driver.register(eventListner);
		LoginPage loginPage = new LoginPage(driver, webSite).get();
		HashMap<String, String> testData = TestDataUtils.getTestData(testDataWorkbookName, sheetName, "RLZ34461");
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
			json = JSONParsarUtils.readFileFromJsonFile("RLZ_34461_Programs_Contents2.json");
			json = JSONParsarUtils.readFileFromJsonFile("RLZ_34461_Programs_Contents2.json");
			tools = RequestMockUtils.setResponse(chromeDriver, contentGraphqlURL, "getContents", "post", json);
			browsePage.clickOnSecondProduct(driver);
			programPage.clickOnMultipleImportsAndVerifyTheItemsAddedForSecondProduct(driver, 3);
			RequestMockUtils.closeMock(tools);
			header.clickImportedItemsCart(driver, 5);
			header.clickTheCrossButtons(driver, 5);
			header.verifyNoItemsAdded(driver);
			header.verifyImportBtnIsDisabled(driver);
		} catch (Exception e) {
			Log.exception(e, driver);
		} finally {
			driver.quit();
			Log.testCaseResult();
			Log.endTestCase();
		}
	}

	/** 
	 * RLZ34429:Verify long content name truncated in the Import cart
	 * @param browser
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", description = "RLZ34429:Verify long content name truncated in the Import cart")
	public void RLZ34429(String browser) throws Exception {
		WebDriver chromeDriver = WebDriverFactory.get(browser);
		EventFiringWebDriver driver = new EventFiringWebDriver(chromeDriver);
		EventListener eventListner = new EventListener();
		driver.register(eventListner);

		HashMap<String,String> testData = TestDataUtils.getTestData(testDataWorkbookName, sheetName, "RLZ34429");

		Log.testCaseInfo(testData.get("TC_ID")+ ":"+testData.get("DESCRIPTION") + " [" + browser + "]");
		try {
			LoginPage loginPage = new LoginPage(driver, webSite).get();
			BrowseProgramsPage browserProgramsPage = loginPage.loginWithValidData(driver, testData.get("USERNAME"),testData.get("PASSWORD"));
			String json = JSONParsarUtils.readFileFromJsonFile("RLZ_34429_Programs_Contents.json");
			DevTools tools = RequestMockUtils.setResponse(chromeDriver, browseAllContentGraphqlURL, "getSearchResult", "post", json);
			BrowseAllContentPage browseAllPage =browserProgramsPage.clickOnBrowseAllContent(driver);
			JSONParsarUtils.getSearchContent(json);
			browseAllPage.clickOnMultipleImportsAndVerifyTheItemsAddedInBrowseAllContent(driver,1);
			RequestMockUtils.closeMock(tools);
			HomepageHeader header = new HomepageHeader(driver);
			header.clickImportedItemsCart(driver, 1);
			header.hoverOnImportContent(driver);
		} catch (Exception e) {
			Log.exception(e, driver);
		} finally {
			driver.quit();
			Log.testCaseResult();
			Log.endTestCase();
		}
	}

	/**
	 * RLZ34671:Verify Import cart persists after canvas teacher preview content in Discover search page
	 * @param browser
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", description = "RLZ34671:Verify Import cart persists after canvas teacher preview content in Discover search page")
	public void RLZ34671(String browser) throws Exception {
		EventFiringWebDriver driver = new EventFiringWebDriver(WebDriverFactory.get(browser));
		EventListener eventListner = new EventListener();
		driver.register(eventListner);
		LoginPage loginPage = new LoginPage(driver, webSite).get();
		HashMap<String, String> testData = TestDataUtils.getTestData(testDataWorkbookName, sheetName, "RLZ34671");
		Log.testCaseInfo(testData.get("TC_ID")+ ":"+testData.get("DESCRIPTION") + " [" + browser + "]");
		try {
			BrowseProgramsPage browserProgramsPage = loginPage.loginWithValidData(driver, testData.get("USERNAME"),testData.get("PASSWORD"));
			BrowseAllContentPage browseAllPage =browserProgramsPage.clickOnBrowseAllContent(driver);
			browseAllPage.clickOnMultipleImportsAndVerifyTheItemsAddedInBrowseAllContent(driver, 1);
			String contentName = browseAllPage.getFirstContentText();
			browseAllPage.clickFirstContentOfBrowseAllContent(driver);
			ContentInfoPage contentInfo= new ContentInfoPage(driver);
			contentInfo.clickExitContent(driver, contentName);
			HomepageHeader header = new HomepageHeader(driver);
			header.clickImportedItemsCart(driver, 1);
		} catch (Exception e) {
			Log.exception(e, driver);
		} finally {
			driver.quit();
			Log.testCaseResult();
			Log.endTestCase();
		}
	}

	/**
	 * RLZ35007:Verify canvas designer is able to import large number of contents from discover page as module resource
	 * @param browser
	 * @throws Exception
	 */
	@Test(dataProviderClass = DataProviderUtils.class, dataProvider = "getBrowser", description = "RLZ35007:Verify canvas designer is able to import large number of contents from discover page as module resource")
	public void RLZ35007(String browser) throws Exception {
		WebDriver chromeDriver = WebDriverFactory.get(browser);
		EventFiringWebDriver driver = new EventFiringWebDriver(chromeDriver);
		EventListener eventListner = new EventListener();
		driver.register(eventListner);
		LoginPage LoginPage = new LoginPage(driver, webSite).get();
		HashMap<String,String> testData = TestDataUtils.getTestData(testDataWorkbookName, sheetName, "RLZ35007");
		Log.testCaseInfo(testData.get("TC_ID")+ ":"+testData.get("DESCRIPTION") + " [" + browser + "]");
		try {
			BrowseProgramsPage BrowsePage=LoginPage.loginWithValidData(driver, testData.get("USERNAME"),testData.get("PASSWORD"));
			String json = JSONParsarUtils.readFileFromJsonFile("BrowseAll_Contents2.json");
			DevTools tools = RequestMockUtils.setResponse(chromeDriver, browseAllContentGraphqlURL, "getSearchResult", "post", json);
			BrowseAllContentPage BrowseAllContentPage=BrowsePage.clickBrowseAll(driver);
			HomepageHeader HomepageHeader = BrowseAllContentPage.clickOnBrowseAllImportsUsingSizeAndVerify(driver, 50);
			RequestMockUtils.closeMock(tools);
			HomepageHeader.clickBrowse(driver);
			BrowsePage = new BrowseProgramsPage(driver).get();
			String json2 = JSONParsarUtils.readFileFromJsonFile("Program_Contents2.json");
			DevTools tools2 = RequestMockUtils.setResponse(chromeDriver, contentGraphqlURL, "getContents", "post", json2);
			ProgramPage programPage = BrowsePage.clickOnFirstProduct(driver);
			programPage.clickOnMultipleImportsOnFirstProduct(driver, 50);
			RequestMockUtils.closeMock(tools2);
			HomepageHeader.verifyImportedItemsCount(driver, 100);
		} catch (Exception e) {
			Log.exception(e, driver);
		} finally {
			Log.testCaseResult();
			Log.endTestCase();
			driver.quit();
		}
	}
}
