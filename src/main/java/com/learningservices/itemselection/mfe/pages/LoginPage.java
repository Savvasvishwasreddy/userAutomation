package com.learningservices.itemselection.mfe.pages;

import java.util.ArrayList;

import java.util.List;

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
import com.learningservices.utils.Utils;

import LSTFAI.customfactories.AjaxElementLocatorFactory;
import LSTFAI.customfactories.IFindBy;
import LSTFAI.customfactories.PageFactory;

public class LoginPage extends LoadableComponent<LoginPage> {
	
	WebDriver driver;
	private String appURL;
	private boolean isPageLoaded;
	public ElementLayer elementLayer;
	public static List<Object> pageFactoryKey = new ArrayList<Object>();
	public static List<String> pageFactoryValue = new ArrayList<String>();
	
	@IFindBy(how = How.ID, using = "username", AI = true)
	public WebElement fldUsername;
	
	@IFindBy(how = How.ID, using = "password", AI = true)
	public WebElement fldPassword;
	
	@IFindBy(how = How.NAME, using = "submit1", AI = false)
	public WebElement btnSignin;
	
	@IFindBy(how = How.XPATH, using = "(//img[@alt='defaultProgramImg'])[1]", AI = false)
	public WebElement lnkFirstProduct;
	
	@IFindBy(how = How.XPATH, using = "(//span[contains(text(),'Import')])[1]", AI = false)
	public WebElement lnkFirstImport;
	
	public LoginPage() {}
	
	public LoginPage(WebDriver driver){
		this.driver = driver;
		ElementLocatorFactory finder = new AjaxElementLocatorFactory(driver, Utils.maxElementWait);
		PageFactory.initElements(finder, this);
		elementLayer = new ElementLayer(driver);
	}
	
	public LoginPage(WebDriver driver, String url) {
        appURL = url;
        this.driver = driver;
        ElementLocatorFactory finder = new AjaxElementLocatorFactory(driver, Utils.maxElementWait);
        PageFactory.initElements(finder, this);
        driver.get(appURL);
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

		if (isPageLoaded && !(Utils.waitForElement(driver, fldUsername))) {
			Log.fail("Page did not open up. Site might be down.", driver);
		}
		elementLayer = new ElementLayer(driver);
	}// isLoaded

	
	/**
	 * Enters the username in username field
	 * @param driver
	 * @param username - username to be entered
	 * @return 
	 * @throws Exception
	 */
	public LoginPage enterUsername(WebDriver driver, String username) throws Exception {
		ItemSelectionBrowserActions.type(driver, fldUsername, username, "username");
		return this;
	}
	
	/**
	 * Enters the password in password field
	 * @param driver
	 * @param password - Password to be entered
	 * @throws Exception
	 */
	public LoginPage enterPassword(WebDriver driver, String password) throws Exception {
		ItemSelectionBrowserActions.type(driver, fldPassword, password, "password");
		return this;
	}
	
	/**
	 * Clicks on the sign in button
	 * @param driver
	 * @return 
	 * @throws Exception
	 */
	public LoginPage clickSignin(WebDriver driver) throws Exception {
		ItemSelectionBrowserActions.click(driver, btnSignin,"signin");
		return this;
	}
	
	/**
	 * Enters username and password and clicks on sign in button
	 * @param driver
	 * @param username
	 * @param password
	 * @throws Exception
	 */
	public BrowseProgramsPage loginWithValidData(WebDriver driver, String username,String password) throws Exception{
		enterUsername(driver, username);
		enterPassword(driver, password);
		clickSignin(driver);
		return new BrowseProgramsPage(driver).get();
	}

	/**
	 * Login to application
	 * @param driver
	 * @param username
	 * @param password
	 * @return browser programs page
	 * @throws Exception
	 */
	public BrowseProgramsPage login(WebDriver driver, String username,String password) throws Exception{
		enterUsername(driver, username);
		enterPassword(driver, password);
		clickSignin(driver);
		return new BrowseProgramsPage(driver).get();
	}
}
