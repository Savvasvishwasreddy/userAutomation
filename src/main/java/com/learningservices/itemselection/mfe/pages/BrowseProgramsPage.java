package com.learningservices.itemselection.mfe.pages;

import java.util.ArrayList;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
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

public class BrowseProgramsPage extends LoadableComponent<BrowseProgramsPage> {

	WebDriver driver;
	private boolean isPageLoaded;
	public ElementLayer elementLayer;
	public static List<Object> pageFactoryKey = new ArrayList<Object>();
	public static List<String> pageFactoryValue = new ArrayList<String>();

	@IFindBy(how = How.CSS, using = ".icon-search.searchAllPrograms", AI = false)
	public WebElement txtBrowseAllContent;

	@IFindBy(how = How.XPATH, using = "(//img[@alt='defaultProgramImg'])[1]", AI = false)
	public WebElement lnkFirstProgram;

	@IFindBy(how = How.XPATH, using = "//label[@class='import-label']", AI = false)
	public WebElement btncartImport;

	@IFindBy(how = How.XPATH, using = "//cel-button[@aria-disabled='true']", AI = false)
	public WebElement btncartImportForDisable;

	@IFindBy(how = How.XPATH, using = "(//ion-label[text()='Import'])[1]", AI = false)
	public WebElement lnkFirstProgImport;

	@IFindBy(how = How.XPATH, using = "//div[@class='overlay']", AI = false)
	public WebElement outsideImportCartDropDown;

	@IFindBy(how = How.XPATH, using = "//input[@aria-label='search text']", AI = false)
	public WebElement searchBox;

	@IFindBy(how = How.XPATH, using = "//a[@aria-label='Go back']", AI = false)
	public WebElement btnbackArrow;

	@IFindBy(how = How.XPATH, using = "(//a[@class='program-row__text'])[1]", AI = false)
	public WebElement lnkFirstProduct;

	@IFindBy(how = How.XPATH, using = "(//div[@class='content-row'])[1]", AI = false)
	public WebElement btnProgramFirstContent;

	@IFindBy(how = How.XPATH, using = "//ion-label[text()='Import']", AI = false)
	public WebElement btnImportDiscoverSearchPage;

	@IFindBy(how = How.XPATH, using = "//button[@aria-label='Import']", AI = false)
	public List<WebElement> lstImportBtn; 

	@IFindBy(how = How.XPATH, using = "(//button[@aria-label='Import'])[1]", AI = false)
	public WebElement lnkFirstImport;

	@IFindBy(how = How.XPATH, using = "(//i[@class='icon-search searchAllPrograms'])", AI = false)
	public WebElement btnBrowseAllContent;

	@IFindBy(how = How.CSS, using = "ion-label.programFilter__optionLabel", AI = false)
	public WebElement btnDigits;

	@IFindBy(how = How.XPATH, using = "//ion-toggle[@class='programFilter__toggle ng-pristine ng-valid ios in-item interactive hydrated ng-touched ion-pristine ion-valid ion-touched']", AI = false)
	public WebElement btnSelenium;

	@IFindBy(how = How.XPATH, using = "(//img[@alt='defaultProgramImg'])[2]", AI = false)
	public WebElement lnksecondProduct;

	@IFindBy(how = How.XPATH, using = "((//span[contains(text(),'+ Import')])[1])", AI = false)
	public WebElement lnkSecondImport;

	@IFindBy(how = How.CSS, using = "ion-label.programFilter__optionLabel", AI = false)
	public WebElement btndDigit;

	@IFindBy(how = How.XPATH, using = "((//span[contains(text(),'+ Import')]", AI = false)
	public List<WebElement> lstSecondProductImportbtn;

	@IFindBy(how = How.XPATH, using = "(//ion-label[text()='Import'])[1]", AI = false)
	public WebElement lnkBrowseAllContentFirstImport;

	@IFindBy(how = How.XPATH, using = "//ion-label[text()='Import']", AI = false)
	public List<WebElement> lstBrowseAllContentimport;

	@IFindBy(how = How.XPATH, using = "//span[text()='Exit']", AI = false)
	public WebElement btnExitContent;

	@IFindBy(how = How.XPATH, using = "//button[@aria-label='Import']", AI = false)
	public List<WebElement> lstProgContentImportBtn;

	@IFindBy(how = How.XPATH, using = "//ion-label[text()='Import']", AI = false)
	public List<WebElement> lstProgImportBtn;

	@IFindBy(how = How.CSS, using = "li.program-list_thumbnail_item", AI = false)
	public WebElement lnkFirstProductList;

	public String btnImportLst = "//button[@aria-label='Import']";
	public String btnImportDiscover = "//ion-label[text()='Import']";
	public String btnProgramImport ="//ion-label[text()='Import']";
	public String btnPrograms = "//img[@alt='defaultProgramImg']";
	public String btnProgramsHeader = "//ion-card-title[@role='heading']";
	public String pressEnterKey = "Keys.ENTER";
	public String btnImportSeconfProductLst="//span[contains(text(),'+ Import')]";
	public String btnImportLstForBrowseAllContent = "//ion-label[text()='Import']";

	/**********************************************************************************************************
	 ************************************ShadowDOM********************************************************
	 ***********************************************************************************************************/
	public String lnkBrowseShadowDOM[] = {"cel-platform-navbar", "document.querySelector('cel-platform-navbar').shadowRoot.querySelector('span[aria-label=\"Browse\"]')"};
	public String lnkMyLibraryShadowDOM[] = {"cel-platform-navbar", "document.querySelector('cel-platform-navbar').shadowRoot.querySelector(\"span[aria-label='My Library']\")"};
	public String txtImportedItemsShadowDOM[] = {"cel-platform-navbar", "document.querySelector('cel-dropdown-menu').shadowRoot.querySelector('span')"};
	public String clearImportContentShadowDOM[]= {"cel-icon","document.querySelector('cel-dropdown-menu').shadowRoot.querySelector('cel-dropdown-menu-box').shadowRoot.querySelector('cel-icon').shadowRoot.querySelector('img')"};
	public String importShadowDOM[]= {"cel-button","document.querySelector('label')"};
	public String verifyImportContentShadowDOM[]= {"cel-dropdown-menu","document.querySelector('cel-dropdown-menu').shadowRoot.querySelector('cel-dropdown-menu-box').shadowRoot.querySelector('span.menu-item__label')"};
	public String btnCartImportShadowDOM[] = {"cel-button", "document.querySelector('cel-dropdown-menu').shadowRoot.querySelector('span')"};
	public String txtImportedItemsAddedShadowDOM[] = {"cel-platform-navbar", "document.querySelector('cel-dropdown-menu').shadowRoot.querySelector('span')"};
	public String txtNoItemAddedShadowDOM[] = {"cel-dropdown-menu", "document.querySelector('cel-dropdown-menu').shadowRoot.querySelector('span')"};
	public String iconImportCartShadowDOM[] = {"cel-dropdown-menu", "document.querySelector('cel-dropdown-menu').shadowRoot.querySelector('cel-icon').shadowRoot.querySelector('.icon-inner')"};
	public String BrowseAllCartDropDownSidePannelShadowDOM[] = {"cel-dropdown-menu", "document.querySelector('cel-dropdown-menu').shadowRoot.querySelector('cel-dropdown-menu-box').shadowRoot.querySelector('ul')"};
	public String lnkImportShadowDOM[] = {"cel-dropdown-menu", "document.querySelector('cel-dropdown-menu').shadowRoot.querySelector('span[class*=\"label\"]')"};

	public BrowseProgramsPage() {}
	
	public BrowseProgramsPage(WebDriver driver){
		this.driver = driver;
		ElementLocatorFactory finder = new AjaxElementLocatorFactory(driver, Utils.maxElementWait);
		PageFactory.initElements(finder, this);
		elementLayer = new ElementLayer(driver);
	}

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

		if (isPageLoaded && !(Utils.waitForElement(driver, txtBrowseAllContent))) {
			Log.fail("Page did not open up. Site might be down.", driver);
		}
		elementLayer = new ElementLayer(driver);
	}// isLoaded

	/**
	 * Clicks on the first program present in the browse tab
	 * @param driver
	 * @return Program page
	 * @throws Exception
	 */
	public ProgramPage clickOnFirstProduct(WebDriver driver) throws Exception {
		Utils.waitForElement(driver, lnkFirstProduct);
		ItemSelectionBrowserActions.scrollToViewElement(lnkFirstProduct, driver);
		ItemSelectionBrowserActions.click(driver, lnkFirstProduct, "first product");
		return new ProgramPage(driver).get();	
	}

	/**
	 * Clicks on the import button present in the first content
	 * @param driver
	 * @throws Exception
	 */
	public void clickOnFirstProductImportBtn(WebDriver driver) throws Exception {
		ItemSelectionBrowserActions.click(driver, lnkFirstImport, "first product import button");
	}

	/**
	 * This method verifies imported content items count
	 * @param driver
	 * @param itemCount
	 */
	public void verifyImportedContentItemsCount(WebDriver driver,  int itemCount) {
		WebElement importedItemsCount =ShadowDOMUtils.getWebElement(driver, this.txtImportedItemsShadowDOM[0], this.txtImportedItemsShadowDOM[1]);
		ItemSelectionBrowserActions.waitForExpectedTextToBePresent(driver, importedItemsCount, itemCount+" item(s) added", "imported items count");
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
	 * Click on the my library tab
	 * @param driver
	 * @throws Exception
	 */
	public void clickMyLibrary(WebDriver driver) throws Exception {
		WebElement myLibrary =ShadowDOMUtils.getWebElement(driver, this.lnkMyLibraryShadowDOM[0], this.lnkMyLibraryShadowDOM[1]);
		ItemSelectionBrowserActions.click(driver, myLibrary, "My Library");
	}

	/**
	 * @param driver
	 * @throws Exception
	 */
	public void clickOnSecondProductImportBtn(WebDriver driver) throws Exception {
		ItemSelectionBrowserActions.click(driver, lnkSecondImport, "first product import button");
	}

	/**
	 * Adding multiple import contents and verfiy the added content for second product
	 * @param driver
	 * @param importsCount
	 */
	public void clickOnMultipleImportsAndVerifyTheItemsAddedForSecondProduct(WebDriver driver, int... importsCount) {
		int givenImportCount = importsCount.length > 0?importsCount[0]:1;
		ItemSelectionBrowserActions.waitForElementToBePresent(driver, lnkSecondImport, "import button");
		if(lstImportBtn.size() >=  givenImportCount) { //click import button for the given counts
			for (int i=1 ; i<=givenImportCount; i++) {
				WebElement importBtnIndex = driver.findElement(By.xpath("("+this.btnImportSeconfProductLst+")["+(i)+"]"));
				ItemSelectionBrowserActions.click(driver, importBtnIndex, "Import button - "+i, importsCount);
			}
		}else {
			Log.fail("Actual import buttons displaying is "+lstSecondProductImportbtn.size()+" whereas the given count is "+givenImportCount);
		}
	}

	/**
	 * clicks import button for a programs table of content.
	 * By default this method will click on #1 import button only and if the importsCount value is passed 
	 * it will iterate and click those given import button(s) and verify the items added
	 * @param driver
	 * @param importsCount
	 */
	public void clickProgamContentsImportUsingSizeAndVerify(WebDriver driver, int... importsCount) {
		int givenImportCount = importsCount.length > 0?importsCount[0]:1;
		ItemSelectionBrowserActions.waitForElementToBePresent(driver, lnkFirstImport, "import button");
		if(lstProgContentImportBtn.size() >=  givenImportCount) { //click import button for the given counts
			for (int i=1 ; i<=givenImportCount; i++) {
				WebElement importBtnIndex = driver.findElement(By.xpath("("+this.btnImportSeconfProductLst+")["+(i)+"]"));
				ItemSelectionBrowserActions.click(driver, importBtnIndex, "Import button - "+i, importsCount);
			}
		}else {
			Log.fail("Actual import buttons displaying is "+lstSecondProductImportbtn.size()+" whereas the given count is "+givenImportCount);
		}

	}
	
	/**
	 * To click the cross button to remove the contents from import cart
	 * @param driver
	 * @param count
	 * @throws Exception
	 */
	public void clickTheCrossButtons(WebDriver driver,int... count ) throws Exception {
		int givenImportCount = count.length > 0?count[0]:1;
		WebElement clickCrossBtn =ShadowDOMUtils.getWebElement(driver, this.clearImportContentShadowDOM[0], this.clearImportContentShadowDOM[1]);
		
		ItemSelectionBrowserActions.waitForElementToBePresent(driver, clickCrossBtn, "import button");
		
		for (int i=1 ; i<=givenImportCount; i++) {
			ItemSelectionBrowserActions.click(driver, clickCrossBtn, "clicked the cross button");
		}
		
	}

	/**
	 * To verify import button is disabled when no content is added in cart
	 * @param driver
	 */
	public void verifyImportBtnIsDisabled(WebDriver driver) {
		ItemSelectionBrowserActions.verifyElementIsDisabledUsingAttribute(driver, btncartImportForDisable, btncartImportForDisable, "Import button");
	}

	/**
	 * To do mouse over action on imported content
	 * @param driver
	 */
	public void hoverOnImportContent(WebDriver driver) {
		WebElement importedItemstext =ShadowDOMUtils.getWebElement(driver, this.verifyImportContentShadowDOM[0], this.verifyImportContentShadowDOM[1]);
		ItemSelectionBrowserActions.waitForExpectedTextToBePresent(driver, importedItemstext, "LTIA long title Assessment abc323456789 423456789 523456789 623456789 723456789 ...", "Truncated Text viewed");
		Actions actions = new Actions(driver);
        actions.moveToElement(importedItemstext).perform();
        String fullText = importedItemstext.getAttribute("title");
        System.out.println("expected full text" +fullText);
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
	 * Click browseAll button.
	 * @param driver
	 * @return Browse All Content Page
	 */
	public BrowseAllContentPage clickBrowseAll(WebDriver driver) {
		ItemSelectionBrowserActions.click(driver, txtBrowseAllContent, "browse All Content");
		return new BrowseAllContentPage(driver);
	}

	/**
	 * This method verifies imported program items count 
	 * @param driver
	 * @param itemCount
	 */
	public void verifyImportedProgramItemsCount(WebDriver driver,  int itemCount) {
		WebElement importedItemsCount =ShadowDOMUtils.getWebElement(driver, this.txtImportedItemsShadowDOM[0], this.txtImportedItemsShadowDOM[1]);
		ItemSelectionBrowserActions.waitForExpectedTextToBePresent(driver, importedItemsCount, itemCount+" item(s) added", "imported items count");
	}
	
	/**
	 * clicks import button for Browse All
	 * By default this method will click on #1 import button only and if the importsCount value is passed 
	 * it will iterate and click those given import button(s).
	 * @param driver
	 * @param importsCount
	 */
	public void clickOnBrowseAllImportsUsingIndexAndVerify(WebDriver driver, int... importsCount) {
		int givenImportCount = importsCount.length > 0?importsCount[0]:1;
		ItemSelectionBrowserActions.waitForElementToBePresent(driver, lnkFirstProgImport, "import button");
		if(lstProgImportBtn.size() >=  givenImportCount) { //click import button for the given counts
			WebElement importBtnIndex = driver.findElement(By.xpath("("+this.btnProgramImport+")["+givenImportCount+"]"));
				ItemSelectionBrowserActions.click(driver, importBtnIndex, "Import button - "+givenImportCount, importsCount);
				this.verifyImportedProgramItemsCount(driver, givenImportCount);
		}else {
			Log.fail("Actual import buttons displaying is "+lstBrowseAllContentimport.size()+" whereas the given count is "+givenImportCount);
			Log.fail("Actual import buttons displaying is "+lstProgImportBtn.size()+" whereas the given count is "+givenImportCount);
		}
	}

	/**
	 * Verify the cart message displayed for particular time period
	 * @param driver
	 * @param invisiblityTime - Number of seconds message to be displayed
	 */
	public void verifyItemAddedSuccessfullyDisplayedFewSecs(WebDriver driver, int invisiblityTime) {
		WebElement itemAddedMsg =ShadowDOMUtils.getWebElement(driver, this.txtImportedItemsAddedShadowDOM[0], this.txtImportedItemsAddedShadowDOM[1]);
		ItemSelectionBrowserActions.waitForExpectedTextToBeVisibleForFewSecs(driver, itemAddedMsg, "Item Added Successfully", "Item added message", invisiblityTime);	
	}

	/**
	 * verifies cart import button disabled
	 * @param driver
	 */
	public void verifyCartImportButtonDisabled(WebDriver driver) {
		ItemSelectionBrowserActions.verifyElementIsDisabledUsingAttribute(driver, btncartImport,btncartImportForDisable, "cart import button");	
	}

	/**
	 * Verify cart contains "No item added" text
	 * @param driver
	 */
	public void verifyCartContainsTxtNoItemAdded(WebDriver driver) {
		WebElement textNoItemAdded =ShadowDOMUtils.getWebElement(driver, this.txtNoItemAddedShadowDOM[0], this.txtNoItemAddedShadowDOM[1]);
		ItemSelectionBrowserActions.verifyElementTextIsDisplayed(driver,textNoItemAdded,"No item added","verifyCartContainsTxtNoItemsAdded");
	}

	/**
	 * This method verifies BGV colour of Add to cart button
	 * @param driver
	 */
	public void verifyBgColourOfAddCartIsBlue(WebDriver driver) {
		ItemSelectionBrowserActions.verifyRGBColourOfElement(driver,btncartImport,"#006be0","Add Cart-Blue");
	}

	/**
	 * This method clicks on products using index in Browse programs page
	 * @param driver
	 * @param productCount
	 * @return Program Page
	 * @throws Exception
	 */
	public ProgramPage clickOnProductUsingIndex(WebDriver driver, int... productCount) throws Exception{
		int givenProductCount = productCount.length > 0?productCount[0]:1;
		ItemSelectionBrowserActions.waitForElementToBePresent(driver, lnkFirstProgram, "import button");
		WebElement nthProgram = driver.findElement(By.xpath("("+this.btnPrograms+")["+givenProductCount+"]"));
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", nthProgram);
		ItemSelectionBrowserActions.click(driver, nthProgram,givenProductCount+" Program");
		return new ProgramPage(driver);
	}

	public ArrayList<String> getBrowseAllProgramNamesForGivenCount(WebDriver driver, int... importsCount) {
		int givenImportCount = importsCount.length > 0?importsCount[0]:1;
		ArrayList<String> programHeaders = new ArrayList<>();
		for(int i=0;i<givenImportCount;i++) {
			String programHeader = driver.findElement(By.xpath("("+this.btnProgramsHeader+")["+givenImportCount+"]")).getText();
			programHeaders.add(programHeader);

		}
		return programHeaders;
	}

	/**
	 * Returns program names for a given count in Add to cart dropdown
	 * @param driver
	 * @param importsCount
	 * @return
	 */
	public ArrayList<String> getBrowseAllProgramNamesInAddToCartDropDrownForGivenCount(WebDriver driver, int... importsCount) {
		int givenImportCount = importsCount.length > 0?importsCount[0]:1;
		ArrayList<String> programHeaders = new ArrayList<>();
		WebElement programHead=(WebElement)((JavascriptExecutor)driver).executeScript("return document.querySelector(\"cel-dropdown-menu\")"
				+ ".shadowRoot.querySelector(\"cel-dropdown-menu-box\").shadowRoot.querySelector(\"li:nth-"
				+ "child("+givenImportCount+") span.menu-item__label\")");
		for(int i=0;i<givenImportCount;i++) {
			String programHeader = programHead.getText();
			programHeaders.add(programHeader);

		}
		return programHeaders;
	}

	/**
	 * This method verifies Added program names matches with the imported contents in dropdown
	 * @param driver
	 * @param expected
	 * @param actual
	 */
	public void verifyBrowseAllAddedProgramsNamesInCartMatchesWithImported(WebDriver driver,ArrayList<String> expected,ArrayList<String> actual) {
		ItemSelectionBrowserActions.verifyTwoListOfStringsEqual(driver,btncartImport,expected,actual,"Imported Programs Names");
	}

	/**
	 * Open the program name using name of the program
	 * @param driver current webdriver
	 * @param programName - Name of the program to be opened
	 * @return program page
	 */
	public ProgramPage openProgram(WebDriver driver, String programName) {
		ItemSelectionBrowserActions.scrollToElementAndclick(driver, driver.findElement(By.xpath("//a[text()='" + programName + "']")), programName);
		return new ProgramPage(driver);
	}

	/**
	 * This method verifies added program images matches with the imported contents in dropdown
	 * @param driver
	 * @param expected
	 * @param actual
	 */
	public void verifyBrowseAllAddedProgramsImgInCartMatchesWithImported(WebDriver driver,ArrayList<String> expected,ArrayList<String> actual) {
		ItemSelectionBrowserActions.verifyTwoListOfStringsEqual(driver,btncartImport,expected,actual,"Imported Programs Images");
	}

	/**
	 * This method returns program images as text in Add to cart dropdown for a given count 
	 * @param driver
	 * @param importsCount
	 * @return
	 */
	public ArrayList<String> getBrowseAllProgramImgInAddToCartDropDrownForGivenCount(WebDriver driver, int... importsCount) {
		int givenImportCount = importsCount.length > 0?importsCount[0]:1;
		ArrayList<String> programImgs = new ArrayList<>();
		WebElement programImg=(WebElement)((JavascriptExecutor)driver).executeScript("return document.querySelector"
				+ "(\"cel-dropdown-menu\").shadowRoot.querySelector(\"cel-dropdown-menu-box\").shadowRoot.querySelector"
				+ "(\"li:nth-child("+givenImportCount+") img\")");
		for(int i=0;i<givenImportCount;i++) {
			String programImage = programImg.getText();
			programImgs.add(programImage);

		}
		return programImgs;
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
		WebElement programImg=(WebElement)((JavascriptExecutor)driver).executeScript("return document.querySelector"
				+ "(\"content-row:nth-child("+givenImportCount+") ion-img\").shadowRoot.querySelector(\"img\")");
		for(int i=0;i<givenImportCount;i++) {
			String programImage = programImg.getText();
			programImgs.add(programImage);

		}
		return programImgs;
	}

	/**
	 *This method verifies Remove Icon Present For Imported Programs For Given Count in Add to cart dropdown
	 *By default will verify the first program
	 * @param driver
	 * @param importsCount
	 */
	public void verifyBrowseAllRemoveIconPresentForImportedProgramsForGivenCount(WebDriver driver, int... importsCount) {
		int givenImportCount = importsCount.length > 0?importsCount[0]:1;
		WebElement btnRemove=(WebElement)((JavascriptExecutor)driver).executeScript("return document.querySelector"
				+ "(\"cel-dropdown-menu\").shadowRoot.querySelector(\"cel-dropdown-menu-box\").shadowRoot."
				+ "querySelector(\"li:nth-child("+givenImportCount+") cel-icon\").shadowRoot.querySelector(\"img\")");
		for(int i=0;i<givenImportCount;i++) {
			ItemSelectionBrowserActions.verifyElementDisplayed(driver,btncartImport,btnRemove,"Remove Button");
		}
	}

	/**
	 * This methos verifies Browse All Cart DropDown Side Pannel Is Displayed
	 * @param driver
	 */
	public void verifyBrowseAllCartDropDownSidePannelIsDisplayed(WebDriver driver) {
		WebElement BrowseAllCartDropDownSidePannel=(WebElement)((JavascriptExecutor)driver).executeScript("return document.querySelector"
				+ "(\"cel-dropdown-menu\").shadowRoot.querySelector(\"cel-dropdown-menu-box\").shadowRoot.querySelector"
				+ "(\"ul\")");
		ItemSelectionBrowserActions.verifyElementDisplayed(driver,btncartImport,BrowseAllCartDropDownSidePannel,"Browse All Cart DropDown sidePannel");
	}

	/**
	 * This method verifies Browse All Cart DropDown Side Pannel Is Not Displayed
	 * @param driver
	 */
	public void verifyBrowseAllCartDropDownSidePannelIsNotDisplayed(WebDriver driver) {
		boolean present;
		try {
			ShadowDOMUtils.getWebElement(driver, this.BrowseAllCartDropDownSidePannelShadowDOM[0], this.BrowseAllCartDropDownSidePannelShadowDOM[1]);
			present = true;
		} catch (JavascriptException e) {
			present = false;
		}
		Assert.assertFalse(present, "Browse All Cart DropDown Side Pannel Is Not Displayed");	
	}

	/**
	 * This method clicks Outside Import Cart DropDown
	 * @param driver
	 */
	public void clickOutsideImportCartDropDown(WebDriver driver) {
		ItemSelectionBrowserActions.click(driver, outsideImportCartDropDown, "outside Import Cart DropDown");

	}

	/**
	 * This method verifies whether Able To Scroll Through All Items In Import Cart DropDown
	 * @param driver
	 * @param importsCount
	 */
	public void verifyAbleToScrollThroughAllItemsInImportCartDropDown(WebDriver driver, int... importsCount) {
		int givenImportCount = importsCount.length > 0?importsCount[0]:1;

		for(int i=0;i<givenImportCount;i++) {
			WebElement ItemsInImportCartDropDown=(WebElement)((JavascriptExecutor)driver).executeScript("return document.querySelector(\"cel-dropdown-menu\")"
					+ ".shadowRoot.querySelector(\"cel-dropdown-menu-box\").shadowRoot.querySelector(\"li:nth-"
					+ "child("+givenImportCount+") span.menu-item__label\")");
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", ItemsInImportCartDropDown);

		}
	}

	/**
	 * This method clicks on specific program using name
	 * @param driver
	 * @param programName
	 * @return 
	 * @throws Exception
	 */
	public ProgramPage clickBrowseProgramsUsingName(WebDriver driver,String programName) throws Exception{
		ItemSelectionBrowserActions.waitForElementToBePresent(driver,txtBrowseAllContent , "import button");
		WebElement ProgName = driver.findElement(By.xpath("//a[text()='"+programName+"']"));
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", ProgName);
		ItemSelectionBrowserActions.click(driver, ProgName,"Program Name");
		return new ProgramPage(driver).get();
	}

	/**
	 * This method verifies whether "Item Added Successfully" text displayed 
	 * @param driver
	 */
	public void verifyImportMessage(WebDriver driver) {
		WebElement importedItemsCount =ShadowDOMUtils.getWebElement(driver, this.lnkImportShadowDOM[0], this.lnkImportShadowDOM[1]);
		ItemSelectionBrowserActions.waitForExpectedTextToBePresent(driver, importedItemsCount, "Item Added Successfully", "imported message");
	}

	/**
	 * This method checks whether the given input message is diaplayed 
	 * @param driver
	 * @param message
	 */
	public void verifyImportMessage(WebDriver driver, String message) {
		WebElement importedItemsCount =ShadowDOMUtils.getWebElement(driver, this.lnkImportShadowDOM[0], this.lnkImportShadowDOM[1]);
		ItemSelectionBrowserActions.waitForExpectedTextToBePresent(driver, importedItemsCount,message, "imported message");
	}

	/**
	 * clicks import button for a programs table of content.
	 * By default this method will click on #1 import button only and if the importsCount value is passed 
	 * it will click the given count import button
	 * @param driver
	 * @param importCount
	 */
	public void clickProgamContentsImportUsingCount(WebDriver driver, int... importCount) {
		int givenImportCount = importCount.length > 0?importCount[0]:1;
		ItemSelectionBrowserActions.waitForElementToBePresent(driver, lnkFirstImport, "import button");
		WebElement importBtnIndex = driver.findElement(By.xpath("("+this.btnImportLst+")["+(givenImportCount)+"]"));
		ItemSelectionBrowserActions.click(driver, importBtnIndex, "Import button - "+givenImportCount, importCount);
	}

	/**
	 * clicks import button for a programs table of content in discover search page.
	 * By default this method will click on #1 import button only and if the importsCount value is passed 
	 * it will click the given count import button
	 * @param driver
	 * @param importCount
	 */
	public void clickProgamContentsImportInDiscoverSearchUsingCount(WebDriver driver, int... importCount) {
		int givenImportCount = importCount.length > 0?importCount[0]:1;
		ItemSelectionBrowserActions.waitForElementToBePresent(driver, btnImportDiscoverSearchPage, "import button");
		WebElement importBtnIndex = driver.findElement(By.xpath("("+this.btnImportDiscover+")["+(givenImportCount)+"]"));
		ItemSelectionBrowserActions.click(driver, importBtnIndex, "Import button - "+givenImportCount, importCount);
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
		ItemSelectionBrowserActions.click(driver, importBtnIndex, "Import button - "+givenImportCount, importsCount);
	}

	/**
	 * To click on first content of browseall page
	 * @param driver
	 * @throws Exception
	 */
	public BrowseAllContentPage clickOnBrowseAllContent(WebDriver driver) throws Exception {
		ItemSelectionBrowserActions.click(driver, btnBrowseAllContent, "Browse All Content button");
		ItemSelectionBrowserActions.waitForElementToBePresent(driver, btnDigits, "element is present");
		return new BrowseAllContentPage(driver).get();
	}

	/**
	 * @param driver
	 * @throws Exception
	 */
	public ProgramPage clickOnSecondProduct(WebDriver driver) throws Exception {
		ItemSelectionBrowserActions.click(driver, lnksecondProduct, "Second product");
		return new ProgramPage(driver).get();
	}
	
	/**
	 * Clicks on the browse tab
	 * @param driver
	 * @throws Exception
	 */
	public void clickBrowse(WebDriver driver) throws Exception {
		WebElement browse =ShadowDOMUtils.getWebElement(driver, this.lnkBrowseShadowDOM[0], this.lnkBrowseShadowDOM[1]);
		ItemSelectionBrowserActions.click(driver, browse, "browse button");
	}
}

