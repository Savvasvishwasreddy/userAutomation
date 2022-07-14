package com.learningservices.itemselection.mfe.pages;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.How;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;
import org.openqa.selenium.support.ui.LoadableComponent;
import org.testng.Assert;

import com.learningservices.itemselection.mfe.utils.ItemSelectionBrowserActions;
import com.learningservices.itemselection.mfe.utils.WaitUtils;
import com.learningservices.utils.ElementLayer;
import com.learningservices.utils.Log;
import com.learningservices.utils.ShadowDOMUtils;
import com.learningservices.utils.Utils;

import LSTFAI.customfactories.AjaxElementLocatorFactory;
import LSTFAI.customfactories.IFindBy;
import LSTFAI.customfactories.PageFactory;

public class BrowseAllContentPage  extends LoadableComponent<BrowseAllContentPage> {

	WebDriver driver;
	private boolean isPageLoaded;
	public ElementLayer elementLayer;
	public static List<Object> pageFactoryKey = new ArrayList<Object>();
	public static List<String> pageFactoryValue = new ArrayList<String>();

	@IFindBy(how = How.CSS, using = "ion-label.programFilter__optionLabel", AI = false)
	public WebElement btnDigits;

	@IFindBy(how = How.XPATH, using = "(//ion-label[text()='Import'])[1]", AI = false)
	public WebElement lnkBrowseAllContentFirstImport;

	@IFindBy(how = How.XPATH, using = "//ion-label[text()='Import']", AI = false)
	public List<WebElement> lstBrowseAllContentimport;

	@IFindBy(how = How.XPATH, using = "//button[@aria-label='Import']", AI = false)
	public List<WebElement> lstProgContentImportBtn;

	@IFindBy(how = How.XPATH, using = "//span[text()='Exit']", AI = false)
	public WebElement btnExitContent;

	@IFindBy(how = How.CSS, using = "ion-card-title.card-header__title", AI = false)
	public WebElement btnBrowseAllContentFirstContent;

	public String browseAllContentFirstContentBtn = "ion-card-title.card-header__title";

	@IFindBy(how = How.XPATH, using = "(//ion-label[text()='Import'])", AI = false)
	public WebElement lnkFirstProgImport;

	@IFindBy(how = How.XPATH, using = "//ion-label[text()='Import']", AI = false)
	public List<WebElement> lstProgImportBtn;

	@IFindBy(how = How.XPATH, using = "//ion-label[text()='Import']", AI = false)
	public WebElement btnImportDiscoverSearchPage;

	@IFindBy(how = How.XPATH, using = "//input[@placeholder='Search']", AI = false)
	public WebElement searchBox;

	public String btnImportLstForBrowseAllContent = "//ion-label[text()='Import']";
	public String btnImportLst = "//button[@aria-label='Import']";
	public String btnImportSeconfProductLst="//span[contains(text(),'+ Import')]";
	public String btnProgramImport ="//ion-label[text()='Import']";
	public String btnProgramsHeader = "//ion-card-title[@role='heading']";
	public String btnImportDiscover = "//ion-label[text()='Import']";

	/**********************************************************************************************************
	 ************************************ShadowDOM********************************************************
	 ***********************************************************************************************************/
	public String lnkBrowseShadowDOM[] = {"cel-platform-navbar", "document.querySelector('cel-platform-navbar').shadowRoot.querySelector('span[aria-label=\"Browse\"]')"};
	public String txtImportedItemsAddedShadowDOM[] = {"cel-platform-navbar", "document.querySelector('cel-dropdown-menu').shadowRoot.querySelector('span')"};
	public String txtImportedItemsShadowDOM[] = {"cel-platform-navbar", "document.querySelector('cel-dropdown-menu').shadowRoot.querySelector('span')"};
	public String iconImportCartShadowDOM[] = {"cel-dropdown-menu", "document.querySelector('cel-dropdown-menu').shadowRoot.querySelector('cel-icon').shadowRoot.querySelector('.icon-inner')"};

	@Override
	protected void load() {
		isPageLoaded = true;
		WaitUtils.waitForPageLoad(driver);
	}// load


	@Override
	protected void isLoaded() {
		if (!isPageLoaded) {
			Assert.fail();
		}

		if (isPageLoaded && !(Utils.waitForElement(driver, btnDigits))) {
			Log.fail("Page did not open up. Site might be down.", driver);
		}
		elementLayer = new ElementLayer(driver);
	}// isLoaded

	public BrowseAllContentPage() {}

	public BrowseAllContentPage(WebDriver driver){
		this.driver = driver;
		ElementLocatorFactory finder = new AjaxElementLocatorFactory(driver, Utils.maxElementWait);
		PageFactory.initElements(finder, this);
		elementLayer = new ElementLayer(driver);
	}

	/**
	 * Used to verify imported items count
	 * @param driver
	 * @param itemCount
	 */
	public void verifyImportedContentItemsCount(WebDriver driver,  int itemCount) {
		WebElement importedItemsCount =ShadowDOMUtils.getWebElement(driver, this.txtImportedItemsShadowDOM[0], this.txtImportedItemsShadowDOM[1]);
		ItemSelectionBrowserActions.waitForExpectedTextToBePresent(driver, importedItemsCount, itemCount+" item(s) added", "imported items count");
	}

	/**
	 * Adding multiple import contents and verfiy the added content in browseall content
	 * @param driver
	 * @param importsCount
	 */
	public void clickOnMultipleImportsAndVerifyTheItemsAddedInBrowseAllContent(WebDriver driver, int... importsCount) {
		int givenImportCount = importsCount.length > 0?importsCount[0]:1;
		ItemSelectionBrowserActions.waitForElementToBePresent(driver, lnkBrowseAllContentFirstImport, "import button");
		if(lstBrowseAllContentimport.size() >=  givenImportCount) { //click import button for the given counts
			for (int i=1 ; i<=givenImportCount; i++) {
				WebElement importBtnIndex = driver.findElement(By.xpath("("+this.btnImportLstForBrowseAllContent+")["+(i)+"]"));
				ItemSelectionBrowserActions.click(driver, importBtnIndex, "Import button - "+i, importsCount);
			}
			this.verifyImportedContentItemsCount(driver, givenImportCount);
		}else {
			Log.fail("Actual import buttons displaying is "+lstProgContentImportBtn.size()+" whereas the given count is "+givenImportCount);
		}
	}

	/**
	 * To click on first content of browseall page
	 * @param driver
	 * @throws InterruptedException 
	 */
	public ContentInfoPage clickFirstContentOfBrowseAllContent(WebDriver driver) throws InterruptedException {
		ItemSelectionBrowserActions.waitForElementToBeClickable(driver, btnBrowseAllContentFirstContent, "first product");
		ItemSelectionBrowserActions.click(driver, btnBrowseAllContentFirstContent, "first Content");
		Utils.waitForPageLoad(driver);
		//ItemSelectionBrowserActions.waitForElementToBeInvisible(driver, btnBrowseAllContentFirstContent,"Browse All first content", 3);
		int waitTime = 30;
		boolean elementNotDisplayed = false;
		for(int i=0; i<waitTime; i++) {
			if(driver.findElements(By.cssSelector(this.browseAllContentFirstContentBtn)).size() > 0) {
				Log.event("Browse all first content still displaying");
			}else {
				elementNotDisplayed = true;
				break;
			}
			Thread.sleep(500);
		}
		Log.assertThat(elementNotDisplayed, "Browse all first content not displayed", "Browse all first content still displaying", driver);
		return new ContentInfoPage(driver);
	}

	/**
	 * To click on exit button for info content
	 * @param driver
	 */
	public BrowseAllContentPage clickExitContent(WebDriver driver) {
		ItemSelectionBrowserActions.click(driver, btnExitContent, "Exit button");
		return this;
	}

	/**
	 * This method verifies imported program items count
	 * @param driver
	 * @param itemCount
	 */
	public void verifyImportedProgramItemsCount(WebDriver driver, int itemCount) {
		WebElement importedItemsCount = ShadowDOMUtils.getWebElement(driver, this.txtImportedItemsShadowDOM[0],this.txtImportedItemsShadowDOM[1]);
		ItemSelectionBrowserActions.waitForExpectedTextToBePresent(driver, importedItemsCount,itemCount + " item(s) added", "imported items count");
	}

	/**
	 * clicks import button for Browse All By default this method will click on #1
	 * import button only and if the importsCount value is passed it will iterate
	 * and click those given import button(s) and verify the message.
	 * @param driver
	 * @param importsCount
	 * @return
	 */
	public HomepageHeader clickOnBrowseAllImportsUsingSizeAndVerify(WebDriver driver, int... importsCount) {
		int givenImportCount = importsCount.length > 0 ? importsCount[0] : 1;
		ItemSelectionBrowserActions.waitForElementToBePresent(driver, lnkFirstProgImport, "import button");
		if (lstProgImportBtn.size() >= givenImportCount) { // click import button for the given counts
			for (int i = 1; i <= givenImportCount; i++) {
				WebElement importBtnIndex = driver.findElement(By.xpath("(" + this.btnProgramImport + ")[" + (i) + "]"));
				((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", importBtnIndex);
				Utils.waitForElement(driver, importBtnIndex);
				ItemSelectionBrowserActions.scrollToElementAndclick(driver, importBtnIndex, "Import button - " + i, importsCount);
			}
			this.verifyImportedProgramItemsCount(driver, givenImportCount);
		} else {
			Log.fail("Actual import buttons displaying is " + lstProgImportBtn.size() + " whereas the given count is "+ givenImportCount);
		}
		return new HomepageHeader(driver);
	}

	/**
	 * This method returns Program names for a given count
	 * if no count given by default will returns first program name
	 * @param driver
	 * @param importsCount
	 * @return
	 */
	public ArrayList<String> getBrowseAllProgramNamesForGivenCount(WebDriver driver, int... importsCount) {
		int givenImportCount = importsCount.length > 0?importsCount[0]:1;
		ArrayList<String> programHeaders = new ArrayList<>();
		for(int i=1;i<=givenImportCount;i++) {
			String programHeader = driver.findElement(By.xpath("("+this.btnProgramsHeader+")["+i+"]")).getText();
			if(programHeader.length() > 80) {
				programHeader = programHeader.substring(0,80);
				programHeader += "...";
			}

			programHeaders.add(programHeader);
		}
		return programHeaders;
	}

	/**
	 * This method returns program images as text for a given count
	 * @param driver
	 * @param importsCount
	 * @return
	 */
	public ArrayList<String> getBrowseAllProgramImgForGivenCount(WebDriver driver, int... importsCount) {
		int givenImportCount = importsCount.length > 0?importsCount[0]:1;
		ArrayList<String> programImgs = new ArrayList<>();
		for(int i=1;i<=givenImportCount;i++) {
			WebElement programImg=(WebElement)((JavascriptExecutor)driver).executeScript("return document.querySelector"
					+ "(\"content-row:nth-child("+i+") ion-img\").shadowRoot.querySelector(\"img\")");
			String programImage = programImg.getAttribute("src");
			programImgs.add(programImage);	
		}
		return programImgs;
	}

	/**
	 * clicks import button for Browse All
	 * By default this method will click on #1 import button only and if the importsCount value is passed 
	 * it will click the import button for the specified count
	 * @param driver
	 * @param importsCount
	 */
	public void clickOnBrowseAllImportsUsingCount(WebDriver driver, int... importsCount) {
		int givenImportCount = importsCount.length > 0?importsCount[0]:1;
		ItemSelectionBrowserActions.waitForElementToBePresent(driver, lnkFirstProgImport, "import button");
		WebElement importBtnIndex = driver.findElement(By.xpath("("+this.btnProgramImport+")["+(givenImportCount)+"]"));
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", importBtnIndex);
		ItemSelectionBrowserActions.clickWithOutCapture(driver, importBtnIndex, "Import button - "+givenImportCount, importsCount);
	}

	/**
	 * Searches for the content in Browse All Content search field
	 * @param driver
	 * @param content
	 */
	public void searchForContentInDiscoverSearchPage(WebDriver driver,String content) {
		ItemSelectionBrowserActions.waitForElementToBePresent(driver, btnImportDiscoverSearchPage, "btnProgramLastContent");
		ItemSelectionBrowserActions.click(driver, searchBox,"click Search box");
		ItemSelectionBrowserActions.clear(driver, searchBox, "clear Search box");
		ItemSelectionBrowserActions.type(driver,searchBox, content,"Content In Discover Search Page");
		searchBox.sendKeys(Keys.ENTER);
	}

	/**
	 * clicks import button for a programs table of content in Browse All Content page.
	 * By default this method will click on #1 import button only and if the importsCount value is passed 
	 * it will click the given count import button
	 * @param driver
	 * @param importCount
	 */
	public void clickProgamContentsImportInDiscoverSearchUsingCount(WebDriver driver, int... importCount) {
		int givenImportCount = importCount.length > 0?importCount[0]:1;
		ItemSelectionBrowserActions.waitForElementToBePresent(driver, btnImportDiscoverSearchPage, "import button");
		WebElement importBtnIndex = driver.findElement(By.xpath("("+this.btnImportDiscover+")["+(givenImportCount)+"]"));
		ItemSelectionBrowserActions.clickWithOutCapture(driver, importBtnIndex, "Import button - "+givenImportCount, importCount);
	}

	/**
	 * Clicks on Browse All content search field
	 * @param driver
	 */
	public void clickOnSearchField(WebDriver driver) {
		ItemSelectionBrowserActions.waitForElementToBePresent(driver, btnImportDiscoverSearchPage, "btnProgramLastContent");
		ItemSelectionBrowserActions.click(driver, searchBox,"click Search box");
	}

	public String getFirstContentText() {
		return btnBrowseAllContentFirstContent.getText();
	}
}
