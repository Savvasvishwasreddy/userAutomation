package com.learningservices.itemselection.mfe.pages;

import java.util.ArrayList;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
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

public class HomepageHeader extends LoadableComponent<HomepageHeader> {

	WebDriver driver;
	private boolean isPageLoaded;
	public ElementLayer elementLayer;
	public static List<Object> pageFactoryKey = new ArrayList<Object>();
	public static List<String> pageFactoryValue = new ArrayList<String>();

	@IFindBy(how = How.CSS, using = "div.header", AI = false)
	public WebElement lblHeader;

	@IFindBy(how = How.XPATH, using = "//span[text()='Exit']", AI = false)
	public WebElement btnExitContent;

	@IFindBy(how = How.XPATH, using = "//label[@class='import-label']", AI = false)
	public WebElement btncartImport;
	
	@IFindBy(how = How.XPATH, using = "//cel-button[@aria-disabled='true']", AI = false)
	public WebElement btncartImportForDisable;
	
	@IFindBy(how = How.XPATH, using = "//div[@class='overlay']", AI = false)
	public WebElement outsideImportCartDropDown;

	/**********************************************************************************************************
	 ************************************ShadowDOM********************************************************
	 ***********************************************************************************************************/
	public String lnkImportShadowDOM[] = {"cel-dropdown-menu", "document.querySelector('cel-dropdown-menu').shadowRoot.querySelector('span[class*=\"label\"]')"};
	public String btnOpenImportShadowDOM[] = {"cel-dropdown-menu", "document.querySelector('cel-dropdown-menu').shadowRoot.querySelector('button.dropdown-trigger')"};
	public String txtCartItemsShadowDOM[] = {"cel-dropdown-menu", "document.querySelector('cel-dropdown-menu').shadowRoot.querySelector('cel-dropdown-menu-box')"};
	public String verifyImportContentShadowDOM[]= {"cel-dropdown-menu","document.querySelector('cel-dropdown-menu').shadowRoot.querySelector('cel-dropdown-menu-box').shadowRoot.querySelector('span.menu-item__label')"};
	public String txtImportedItemsAddedShadowDOM[] = {"cel-platform-navbar", "document.querySelector('cel-dropdown-menu').shadowRoot.querySelector('span')"};
	public String clearImportContentShadowDOM[]= {"cel-icon","document.querySelector('cel-dropdown-menu').shadowRoot.querySelector('cel-dropdown-menu-box').shadowRoot.querySelector('cel-icon').shadowRoot.querySelector('img')"};
	public String txtNoItemAddedShadowDOM[] = {"cel-dropdown-menu", "document.querySelector('cel-dropdown-menu').shadowRoot.querySelector('span')"};
	public String iconImportCartShadowDOM[] = {"cel-dropdown-menu", "document.querySelector('cel-dropdown-menu').shadowRoot.querySelector('cel-icon').shadowRoot.querySelector('.icon-inner')"};
	public String BrowseAllCartDropDownSidePannelShadowDOM[] = {"cel-dropdown-menu", "document.querySelector('cel-dropdown-menu').shadowRoot.querySelector('cel-dropdown-menu-box').shadowRoot.querySelector('ul')"};
	public String lnkBrowseShadowDOM[] = {"cel-platform-navbar", "document.querySelector('cel-platform-navbar').shadowRoot.querySelector('span[aria-label=\"Browse\"]')"};
	public String txtImportedItemsShadowDOM[] = {"cel-platform-navbar", "document.querySelector('cel-dropdown-menu').shadowRoot.querySelector('span')"};
	public String btnExit[]= {"cel-button","document.querySelector('cel-button').shadowRoot.querySelector('span')"};

	public String cartList = "span.menu-item__label";

	public HomepageHeader() {}

	public HomepageHeader(WebDriver driver){
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

		if (isPageLoaded && !(Utils.waitForElement(driver, lblHeader))) {
			Log.fail("Page did not open up. Site might be down.", driver);
		}
		elementLayer = new ElementLayer(driver);
	}// isLoaded

	/**
	 * Used to verify items cart count message
	 * @param driver - current web driver
	 * @param itemCount - No of items added
	 * @throws InterruptedException 
	 */
	public HomepageHeader verifyImportedItemsCount(WebDriver driver,  int itemCount) throws InterruptedException {
		boolean isWait;
		int maxTry = 8;
		int tryCount = 0;
		WebElement importedItemsCount =ShadowDOMUtils.getWebElement(driver, this.lnkImportShadowDOM[0], this.lnkImportShadowDOM[1]);
		do {
			if (importedItemsCount.getText().equals(itemCount+" item(s) added")) {
				Log.message(itemCount+" item(s) added message is getting displayed", driver);
				isWait = false;
			} else {
				Thread.sleep(500);
				tryCount++;
				isWait = true;
			}
		} while (isWait && tryCount < maxTry);
		if(tryCount == maxTry) {
			Log.failsoft(itemCount+" item(s) added message is not getting displayed", driver);
		}
		return this;
	}

	/**
	 * Used to verify items cart message "Item Added Successfully"
	 * @param driver - current web driver
	 * @throws InterruptedException
	 */
	public HomepageHeader verifyImportMessage(WebDriver driver) throws InterruptedException {
		boolean isWait;
		int maxTry = 6;
		int tryCount = 0;
		JavascriptExecutor  js = (JavascriptExecutor)driver;
		WebElement importedItemsCount = (WebElement)js.executeScript("return " + this.lnkImportShadowDOM[1]);
		//WebElement importedItemsCount =ShadowDOMUtils.getWebElement(driver, this.lnkImportShadowDOM[0], this.lnkImportShadowDOM[1]);
		do {
			if (importedItemsCount.getText().equals("Item Added Successfully")) {
				Log.message("Item Added Successfully message is getting displayed", driver);
				isWait = false;
			} else {
				Thread.sleep(500);
				tryCount++;
				isWait = true;
			}
		} while (isWait && tryCount < maxTry);
		if(tryCount == maxTry) {
			Log.failsoft("Item Added Successfully message is not getting displayed", driver);
		}
		return this;
	}

	/**
	 * Clicks on imported items in cart
	 * @param driver
	 * @param itemCount
	 * @return
	 */
	public HomepageHeader clickImportedItemsCart(WebDriver driver,  int itemCount) {
		WebElement importedItemsCount =ShadowDOMUtils.getWebElement(driver, this.txtImportedItemsShadowDOM[0], this.txtImportedItemsShadowDOM[1]);
		ItemSelectionBrowserActions.waitForExpectedTextToBePresent(driver, importedItemsCount, itemCount+" item(s) added", "imported items count");
		ItemSelectionBrowserActions.click(driver, importedItemsCount, "import button");
		return this;
	}

	/**
	 * To do mouse over action on imported content
	 * @param driver
	 */
	public HomepageHeader hoverOnImportContent(WebDriver driver) {
		WebElement importedItemstext =ShadowDOMUtils.getWebElement(driver, this.verifyImportContentShadowDOM[0], this.verifyImportContentShadowDOM[1]);
		ItemSelectionBrowserActions.waitForExpectedTextToBePresent(driver, importedItemstext, "Long content abcdefghijklmnopqrstuvwxyz 1234567891011121314151617181920212223242...", "Truncated Text viewed");
		Actions actions = new Actions(driver);
		actions.moveToElement(importedItemstext).perform();
		String fullText = importedItemstext.getAttribute("title");
		Log.message("expected full text" +fullText,driver);
		return this;
	}	

	/**
	 * Clicks on the browse tab
	 * @param driver
	 * @throws Exception
	 */
	public HomepageHeader clickBrowse(WebDriver driver) throws Exception {
		WebElement browse =ShadowDOMUtils.getWebElement(driver, this.lnkBrowseShadowDOM[0], this.lnkBrowseShadowDOM[1]);
		ItemSelectionBrowserActions.click(driver, browse, "browse button");
		return this;
	}

	/**
	 * To click the cross button to remove the contents from import cart
	 * @param driver
	 * @param count
	 * @throws Exception
	 */
	public int clickTheCrossButtons(WebDriver driver,int... count ) throws Exception {
		int givenImportCount = count.length > 0?count[0]:1;
		WebElement clickCrossBtn =ShadowDOMUtils.getWebElement(driver, this.clearImportContentShadowDOM[0], this.clearImportContentShadowDOM[1]);

		ItemSelectionBrowserActions.waitForElementToBePresent(driver, clickCrossBtn, "Cross button");

		for (int i=1 ; i<=givenImportCount; i++) {
			ItemSelectionBrowserActions.click(driver, clickCrossBtn, "clicked the cross button");
		}
		return givenImportCount;
	}

	/**
	 * Used to verify no items added in cart
	 * @param driver
	 * @return
	 */
	public HomepageHeader verifyNoItemsAdded(WebDriver driver) {
		WebElement importedItemsText =ShadowDOMUtils.getWebElement(driver, this.txtImportedItemsShadowDOM[0], this.txtImportedItemsShadowDOM[1]);
		ItemSelectionBrowserActions.waitForExpectedTextToBePresent(driver, importedItemsText,"No item added", "imported items count");
		return this;
	}

	/**
	 * To verify import button is disabled when no content is added in cart
	 * @param driver
	 */
	public HomepageHeader verifyImportBtnIsDisabled(WebDriver driver) {
		ItemSelectionBrowserActions.verifyElementIsDisabledUsingAttribute(driver, btncartImportForDisable, btncartImportForDisable, "Import button");
		return this;
	}	

	/**
	 * To click on exit button for info content
	 * @param driver
	 */
	public  void clickExitContent(WebDriver driver) {
		WebElement exitButton =ShadowDOMUtils.getWebElement(driver, this.btnExit[0], this.btnExit[1]);
		Utils.waitForElement(driver, exitButton);
		ItemSelectionBrowserActions.waitForElementToBeClickable(driver, exitButton, "exit button");
		ItemSelectionBrowserActions.click(driver, exitButton, "exit button");
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
	 *This method clicks on Import cart dropdown icon
	 * @param driver
	 * @return 
	 */
	public void clickImportCartDropdownIcon(WebDriver driver) {
		WebElement iconImportCart =ShadowDOMUtils.getWebElement(driver, this.iconImportCartShadowDOM[0], this.iconImportCartShadowDOM[1]);
		ItemSelectionBrowserActions.click(driver, iconImportCart,"Import Cart icon");
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
	 * Returns program names for a given count in Add to cart dropdown
	 * @param driver
	 * @param importsCount
	 * @return
	 */
	public ArrayList<String> getBrowseAllProgramNamesInAddToCartDropDrownForGivenCount(WebDriver driver, int... importsCount) {
		int givenImportCount = importsCount.length > 0?importsCount[0]:1;
		ArrayList<String> programHeaders = new ArrayList<>();
		for(int i=1;i<=givenImportCount;i++) {
			WebElement programHead=(WebElement)((JavascriptExecutor)driver).executeScript("return document.querySelector(\"cel-dropdown-menu\")"
					+ ".shadowRoot.querySelector(\"cel-dropdown-menu-box\").shadowRoot.querySelector(\"li:nth-"
					+ "child("+i+") span.menu-item__label\")");
			String programHeader = programHead.getText();
			programHeaders.add(programHeader);
			
		}
		return programHeaders;
	}

	/**
	 * Opens the cart list
	 * @param driver
	 * @return
	 */
	public HomepageHeader openCart(WebDriver driver) {
		WebElement openImportCart =ShadowDOMUtils.getWebElement(driver, this.btnOpenImportShadowDOM[0], this.btnOpenImportShadowDOM[1]);
		ItemSelectionBrowserActions.click(driver, openImportCart, "Open import card");
		return this;
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
		for(int i=1;i<=givenImportCount;i++) {
			WebElement programImg=(WebElement)((JavascriptExecutor)driver).executeScript("return document.querySelector"
					+ "(\"cel-dropdown-menu\").shadowRoot.querySelector(\"cel-dropdown-menu-box\").shadowRoot.querySelector"
					+ "(\"li:nth-child("+i+") img\")");
			String programImage = programImg.getAttribute("src");
			programImgs.add(programImage);
			
		}
		return programImgs;		
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
	 * This method verifies added program images matches with the imported contents in dropdown
	 * @param driver
	 * @param expected
	 * @param actual
	 */
	public void verifyBrowseAllAddedProgramsImgInCartMatchesWithImported(WebDriver driver,ArrayList<String> expected,ArrayList<String> actual) {
		ItemSelectionBrowserActions.verifyTwoListOfStringsEqual(driver,btncartImport,expected,actual,"Imported Programs Images");
	}
	
	/**
	 *This method verifies Remove Icon Present For Imported Programs For Given Count in Add to cart dropdown
	 *By default will verify the first program
	 * @param driver
	 * @param importsCount
	 */
	public void verifyBrowseAllRemoveIconPresentForImportedProgramsForGivenCount(WebDriver driver, int... importsCount) {
		int givenImportCount = importsCount.length > 0?importsCount[0]:1;
		for(int i=1;i<=givenImportCount;i++) {
			WebElement btnRemove=(WebElement)((JavascriptExecutor)driver).executeScript("return document.querySelector"
					+ "(\"cel-dropdown-menu\").shadowRoot.querySelector(\"cel-dropdown-menu-box\").shadowRoot."
					+ "querySelector(\"li:nth-child("+i+") cel-icon\").shadowRoot.querySelector(\"img\")");
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView();", btnRemove);
			ItemSelectionBrowserActions.verifyElementDisplayed(driver,btncartImport,btnRemove,"Remove Button");
		}
	}
	
	/**
	 * This method verifies Browse All Cart DropDown Side Pannel Is Displayed
	 * @param driver
	 */
	public void verifyBrowseAllCartDropDownSidePannelIsDisplayed(WebDriver driver) {
		WebElement BrowseAllCartDropDownSidePannel=ShadowDOMUtils.getWebElement(driver, this.BrowseAllCartDropDownSidePannelShadowDOM[0], this.BrowseAllCartDropDownSidePannelShadowDOM[1]);
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
	 * This method verifies imported program items count 
	 * @param driver
	 * @param itemCount
	 */
	public void verifyImportedProgramItemsCount(WebDriver driver,  int itemCount) {
		WebElement importedItemsCount =ShadowDOMUtils.getWebElement(driver, this.txtImportedItemsShadowDOM[0], this.txtImportedItemsShadowDOM[1]);
		ItemSelectionBrowserActions.waitForExpectedTextToBePresent(driver, importedItemsCount, itemCount+" item(s) added", "imported items count");
	}
	
	/**
	 * This method checks whether the given input message is diaplayed 
	 * @param driver
	 * @param message
	 * @throws InterruptedException 
	 */
	public void verifyImportMessage(WebDriver driver, String message) throws InterruptedException {
		boolean isWait;
		int maxTry = 4;
		int tryCount = 0;
		JavascriptExecutor  js = (JavascriptExecutor)driver;
		WebElement importedItemsCount = (WebElement)js.executeScript("return " + this.lnkImportShadowDOM[1]);
		//WebElement importedItemsCount =ShadowDOMUtils.getWebElement(driver, this.lnkImportShadowDOM[0], this.lnkImportShadowDOM[1]);
		do {
			if (importedItemsCount.getText().equals(message)) {
				Log.message(message + " message is getting displayed", driver);
				isWait = false;
			} else {
				Thread.sleep(500);
				tryCount++;
				isWait = true;
			}
		} while (isWait && tryCount < maxTry);
		if(tryCount == maxTry) {
			Log.failsoft(message + " message is not getting displayed", driver);
		}
	}
	
	/**
	 * Used to read the cart list
	 * @param driver
	 * @return
	 */
	public List<String> getCartList(WebDriver driver) {
		WebElement host = ShadowDOMUtils.getWebElement(driver, txtCartItemsShadowDOM[0], txtCartItemsShadowDOM[1]);
		SearchContext shadowRoot = (SearchContext) ((JavascriptExecutor) driver).executeScript("return arguments[0].shadowRoot", host);
		List<WebElement> cartItems = shadowRoot.findElements(By.cssSelector(cartList));
		return cartItems.stream().map(element -> element.getText()).collect(Collectors.toList());
	}

	/**
	 * Wait for the text to be present in cart message
	 * @param driver
	 * @param text
	 */
	public void waitForText(WebDriver driver, String text) {
		WebElement importedItemsCount =ShadowDOMUtils.getWebElement(driver, this.lnkImportShadowDOM[0], this.lnkImportShadowDOM[1]);
	    ItemSelectionBrowserActions.waitForExpectedTextToBePresent(driver, importedItemsCount, text, "Import cart", 60);
	}
}
