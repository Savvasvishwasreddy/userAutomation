package com.learningservices.itemselection.mfe.pages;

import java.util.ArrayList;


import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
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

public class ProgramPage extends LoadableComponent<ProgramPage> {

	WebDriver driver;
	private boolean isPageLoaded;
	public ElementLayer elementLayer;
	public static List<Object> pageFactoryKey = new ArrayList<Object>();
	public static List<String> pageFactoryValue = new ArrayList<String>();

	@IFindBy(how = How.CSS, using = "div.title-bar_title", AI = false)
	public WebElement lblTitle;

	@IFindBy(how = How.CSS, using = "button.import-link", AI = false)
	public List<WebElement> btnImports;

	@IFindBy(how = How.CSS, using = "div.teacherResource__actions button.import-link", AI = false)
	public List<WebElement> btnTRImports;

	@IFindBy(how = How.CSS, using = "span.teacherResource__icon--down", AI = false)
	public WebElement trDropdownOpener;
	
	@IFindBy(how = How.CSS, using = ".icon-search.searchAllPrograms", AI = false)
	public WebElement txtBrowseAllContent;
	
	@IFindBy(how = How.XPATH, using = "//a[@aria-label='Go back']", AI = false)
	public WebElement btnbackArrow;

	@IFindBy(how = How.XPATH, using = "(//img[@alt='defaultProgramImg'])[1]", AI = false)
	public WebElement lnkFirstProduct;

	@IFindBy(how = How.XPATH, using = "(//img[@alt='defaultProgramImg'])[2]", AI = false)
	public WebElement lnksecondProduct;

	@IFindBy(how = How.XPATH, using = "((//span[contains(text(),'+ Import')])[1])", AI = false)
	public WebElement lnkSecondImport;

	@IFindBy(how = How.XPATH, using = "//span[text()='Exit']", AI = false)
	public WebElement btnExitContent;

	@IFindBy(how = How.XPATH, using = "//button[@aria-label='Import']", AI = false)
	public List<WebElement> lstImportBtn;

	@IFindBy(how = How.XPATH, using = "(//button[@aria-label='Import'])[1]", AI = false)
	public WebElement lnkFirstImport;

	@IFindBy(how = How.XPATH, using = "(//i[@class='icon-search searchAllPrograms'])", AI = false)
	public WebElement lnkbrowseAllContent;

	@IFindBy(how = How.CSS, using = "ion-label.programFilter__optionLabel", AI = false)
	public WebElement btnDigits;

	@IFindBy(how = How.XPATH, using = "((//span[contains(text(),'+ Import')]", AI = false)
	public List<WebElement> lstSecondProductImportbtn;

	@IFindBy(how = How.XPATH, using = "(//ion-label[text()='Import'])[1]", AI = false)
	public WebElement lnkBrowseAllContentFirstImport;

	@IFindBy(how = How.CSS, using = "div.content-row__title", AI = false)
	public WebElement firstItem;

	public String btnImportLstForBrowseAllContent = "//ion-label[text()='Import']";
	public String btnImportLst = "//button[@aria-label='Import']";
	public String btnImportSeconfProductLst="//span[contains(text(),'+ Import')]";
	public String lstProgContentImportBtn = "//button[@aria-label='Import']";

	/**********************************************************************************************************
	 ************************************ShadowDOM********************************************************
	 ***********************************************************************************************************/
	public String lnkBrowseShadowDOM[] = {"cel-platform-navbar", "document.querySelector('cel-platform-navbar').shadowRoot.querySelector('span[aria-label=\"Browse\"]')"};
	public String txtImportedItemsAddedShadowDOM[] = {"cel-platform-navbar", "document.querySelector('cel-dropdown-menu').shadowRoot.querySelector('span')"};
	public String txtImportedItemsShadowDOM[] = {"cel-platform-navbar", "document.querySelector('cel-dropdown-menu').shadowRoot.querySelector('span')"};

	public ProgramPage() {}

	public ProgramPage(WebDriver driver){
		this.driver = driver;
		ElementLocatorFactory finder = new AjaxElementLocatorFactory(driver, Utils.maxElementWait);
		PageFactory.initElements(finder, this);
		elementLayer = new ElementLayer(driver);
	}

	@Override
	protected void load() {
		isPageLoaded = true;
		//Utils.waitForPageLoad(driver);
		WaitUtils.waitForPageLoad(driver);
	}// load


	@Override
	protected void isLoaded() {
		if (!isPageLoaded) {
			Assert.fail();
		}

		if (isPageLoaded && !(Utils.waitForElement(driver, lblTitle))) {
			Log.fail("Page did not open up. Site might be down.", driver);
		}
		elementLayer = new ElementLayer(driver);
	}// isLoaded

	/**
	 * Clicks on import button of first content
	 * @param driver
	 * @return same page
	 */
	public ProgramPage clickOnFirstImport(WebDriver driver) {
		ItemSelectionBrowserActions.scrollToElementAndclickWithoutCapture(driver, btnImports.get(0), "Import button");
		return this;
	}

	/**
	 * Clicks all the import button in the contents
	 * @param driver
	 * @return number of the import buttons clicked
	 */
	public int clickOnMultipleImport(WebDriver driver) {
		btnImports.forEach(importBtn -> ItemSelectionBrowserActions.scrollToElementAndclick(driver, importBtn, "Import button"));
		return btnImports.size();
	}

	/**
	 * Clicks on the first import button in the Teachers resource
	 * @param driver
	 * @return same page
	 */
	public ProgramPage clickOnFirstImportTR(WebDriver driver) {
		ItemSelectionBrowserActions.scrollToElementAndclick(driver, btnTRImports.get(0), "Import button");
		return this;
	}

	/**
	 * Clicks all the import button in the Teachers resource
	 * @param driver
	 * @return
	 */
	public int clickOnAllImportTR(WebDriver driver) {
		btnTRImports.forEach(importBtn -> ItemSelectionBrowserActions.click(driver, importBtn, "Import button"));
		return btnTRImports.size();
	}

	/**
	 * Clicks on multiple import button in the teachers resource
	 * @param driver
	 * @param count - Number of import buttons to be clicked
	 * @return number of import button clicked
	 */
	public int clickOnMultipleImportTR(WebDriver driver, int count) {
		for (int i = 0; i < count; i++) {
			ItemSelectionBrowserActions.click(driver, btnTRImports.get(i), "Import button");
		}
		return count;
	}

	/**
	 * Opens the teachers resource drop down
	 */
	public void openTeacherResource() {
		ItemSelectionBrowserActions.click(driver, trDropdownOpener, "Teacher resource drop down");
	}

	/**
	 * Open the program name using name of the program
	 * @param driver current webdriver
	 * @param itemName - Name of the item to be opened
	 * @return returns program page
	 */
	public ProgramPage openItem(WebDriver driver, String itemName) {
		ItemSelectionBrowserActions.scrollToViewElement(driver.findElement(By.xpath("//a[text()='" + itemName + "']//ancestor::content-row")), driver);
		//ItemSelectionBrowserActions.scrollToElementAndclick(driver, driver.findElement(By.xpath("//a[text()='" + itemName + "']")), "Program name");
		ItemSelectionBrowserActions.click(driver, driver.findElement(By.xpath("//a[text()='" + itemName + "']")), "Program name");
		return new ProgramPage(driver);
	}

	/**
	 * Verifies the items cart count message
	 * @param driver
	 * @param itemCount - Number of items added
	 */
	public void verifyImportedItemsCount(WebDriver driver,  int itemCount) {
		WebElement importedItemsCount =ShadowDOMUtils.getWebElement(driver, this.txtImportedItemsAddedShadowDOM[0], this.txtImportedItemsAddedShadowDOM[1]);
		ItemSelectionBrowserActions.waitForExpectedTextToBePresent(driver, importedItemsCount, itemCount+" item(s) added", "imported items count");
	}

	/**
	 * Used to add and verify the cart
	 * @param driver
	 * @param importsCount
	 */
	public void clickOnMultipleImportsAndVerifyTheItemsAdded(WebDriver driver, int... importsCount) {
		int givenImportCount = importsCount.length > 0 ? importsCount[0] : 1;
		ItemSelectionBrowserActions.waitForElementToBePresent(driver, lnkFirstImport, "import button");
		if (lstImportBtn.size() >= givenImportCount) { // click import button for the given counts
			for (int i = 1; i <= givenImportCount; i++) {
				WebElement importBtnIndex = driver.findElement(By.xpath("(" + this.btnImportLst + ")[" + (i) + "]"));
				//ItemSelectionBrowserActions.click(driver, importBtnIndex, "Import button - " + i, importsCount);
				((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", importBtnIndex);
				ItemSelectionBrowserActions.click(driver, importBtnIndex, "Import button - " + i, importsCount);
			}
			this.verifyImportedItemsCount(driver, givenImportCount);
		} else {
			Log.fail("Actual import buttons displaying is " + lstImportBtn.size() + " whereas the given count is "
					+ givenImportCount);
		}
	}

	/**
	 * Adding multiple import contents and verfiy the added content for second product
	 * @param driver
	 * @param importsCount
	 */
	public int clickOnMultipleImportsAndVerifyTheItemsAddedForSecondProduct(WebDriver driver, int... importsCount) {
		int givenImportCount = importsCount.length > 0?importsCount[0]:1;
		ItemSelectionBrowserActions.waitForElementToBePresent(driver, lnkSecondImport, "import button");
		if(lstImportBtn.size() >=  givenImportCount) { 
			for (int i=1 ; i<=givenImportCount; i++) {
				WebElement importBtnIndex = driver.findElement(By.xpath("("+this.btnImportSeconfProductLst+")["+(i)+"]"));
				ItemSelectionBrowserActions.click(driver, importBtnIndex, "Import button - "+i, importsCount);
			}
		}else {
			Log.fail("Actual import buttons displaying is "+lstSecondProductImportbtn.size()+" whereas the given count is "+givenImportCount);
		}
		return givenImportCount;
	}

	/**
	 * This method verifies imported content items count
	 * @param driver
	 * @param itemCount
	 */
	public int verifyImportedContentItemsCount(WebDriver driver,  int itemCount) {
		WebElement importedItemsCount =ShadowDOMUtils.getWebElement(driver, this.txtImportedItemsShadowDOM[0], this.txtImportedItemsShadowDOM[1]);
		ItemSelectionBrowserActions.waitForExpectedTextToBePresent(driver, importedItemsCount, itemCount+" item(s) added", "imported items count");
		return itemCount;
	}

	/**
	 * @param driver
	 * @throws Exception
	 */
	public ProgramPage clickOnBrowseAllContent(WebDriver driver) throws Exception {
		ItemSelectionBrowserActions.click(driver, lnkbrowseAllContent, "Browse All Content button");
		ItemSelectionBrowserActions.waitForElementToBePresent(driver, btnDigits, "page identifier");
		return this;

	}

	/**
	 * clicks import button for a programs table of content.
	 * By default this method will click on #1 import button only and if the importsCount value is passed 
	 * it will iterate and click those given import button(s)
	 * @param driver
	 * @param importsCount
	 * @return 
	 */
	public HomepageHeader clickOnProgramContentImportsUsingIndexAndVerify(WebDriver driver, int... importsCount) {
		int givenImportCount = importsCount.length > 0?importsCount[0]:1;
		List<WebElement> importBtns = driver.findElements(By.xpath(lstProgContentImportBtn));
		ItemSelectionBrowserActions.waitForElementToBePresent(driver, lnkFirstImport, "import button");
		if(importBtns.size() >=  givenImportCount) { 
				WebElement importBtnIndex = driver.findElement(By.xpath("("+this.btnImportLst+")["+(givenImportCount)+"]"));
				ItemSelectionBrowserActions.click(driver, importBtnIndex, "Import button - "+givenImportCount, importsCount);
				this.verifyImportedContentItemsCount(driver, givenImportCount);
		}else {
			Log.fail("Actual import buttons displaying is "+importBtns.size()+" whereas the given count is "+givenImportCount);
		}
		return new HomepageHeader(driver);
	}
	
	/**
	 * clicks import button for a programs table of content.
	 * By default this method will click on #1 import button only and if the importsCount value is passed 
	 * it will click the given count import button
	 * @param driver
	 * @param importCount
	 * @return 
	 */
	public HomepageHeader clickProgamContentsImportUsingCount(WebDriver driver, int... importCount) {
		int givenImportCount = importCount.length > 0?importCount[0]:1;
		ItemSelectionBrowserActions.waitForElementToBePresent(driver, lnkFirstImport, "import button");
				WebElement importBtnIndex = driver.findElement(By.xpath("("+this.btnImportLst+")["+(givenImportCount)+"]"));
				ItemSelectionBrowserActions.click(driver, importBtnIndex, "Import button - "+givenImportCount, importCount);
		return new HomepageHeader(driver);
	}
	
	/**
	 * Clicks back arrow in Program Page
	 * @param driver
	 */
	public BrowseProgramsPage clickbackArrowInProgram(WebDriver driver) {
		ItemSelectionBrowserActions.scrollToViewElement(btnbackArrow, driver);
		ItemSelectionBrowserActions.clickJS(driver, btnbackArrow, "Back Arrow");
		return new BrowseProgramsPage(driver).get();
	}

	/**
	 * Clicks on multiple import on first product
	 * @param driver
	 * @param importsCount
	 */
	public void clickOnMultipleImportsOnFirstProduct(WebDriver driver, int... importsCount) {
		int givenImportCount = importsCount.length > 0 ? importsCount[0] : 1;
		ItemSelectionBrowserActions.waitForElementToBePresent(driver, lnkFirstImport, "import button");
		if (lstImportBtn.size() >= givenImportCount) { // click import button for the given counts
			for (int i = 1; i <= givenImportCount; i++) {
				WebElement importBtnIndex = driver.findElement(By.xpath("(" + this.btnImportLst + ")[" + (i) + "]"));
				//ItemSelectionBrowserActions.click(driver, importBtnIndex, "Import button - " + i, importsCount);
				((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", importBtnIndex);
				ItemSelectionBrowserActions.click(driver, importBtnIndex, "Import button - " + i, importsCount);
			}
		} else {
			Log.fail("Actual import buttons displaying is " + lstImportBtn.size() + " whereas the given count is "
					+ givenImportCount);
		}
	}

	/**
	 * Import the item using name of the program
	 * @param driver current webdriver
	 * @param itemName - Name of the item to be imported
	 * @return returns program page
	 */
	public ProgramPage importItem(WebDriver driver, String itemName) {
		ItemSelectionBrowserActions.scrollToViewElement(driver.findElement(By.xpath("//a[text()='" + itemName + "']//ancestor::div[@class='content-row_list_info']//button[contains(@class,'import-link')]")), driver);
		//ItemSelectionBrowserActions.scrollToElementAndclick(driver, driver.findElement(By.xpath("//a[text()='" + itemName + "']")), "Program name");
		ItemSelectionBrowserActions.clickWithOutCapture(driver, driver.findElement(By.xpath("//a[text()='" + itemName + "']//ancestor::div[@class='content-row_list_info']//button[contains(@class,'import-link')]")), "Program name");
		return this;
	}
}
