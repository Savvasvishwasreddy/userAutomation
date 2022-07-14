package com.learningservices.itemselection.bff.canvas.pages;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.LoadableComponent;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.learningservices.itemselection.bff.utils.RealizeUtils;
import com.learningservices.utils.ElementLayer;
import com.learningservices.utils.Log;
import com.learningservices.utils.StopWatch;

public class CanvasLoginPage extends LoadableComponent<CanvasLoginPage> {
	private final WebDriver driver;
	private String canvasUrl;
	private boolean isPageLoaded;
	public ElementLayer uielement;

	@FindAll({ @FindBy(id = "pseudonym_unique_id"), @FindBy(id = "pseudonym_session_unique_id") })
	WebElement txtUserName;

	@FindAll({ @FindBy(id = "pseudonym_password"), @FindBy(id = "pseudonym_session_password") })
	WebElement txtPassword;

	@FindBy(css = "form[id='login_form'] .Button.Button--login")
	WebElement btnLogin;

	@FindBy(css = "form#registration_confirmation_form button[type='submit']")
	WebElement btnRegister;
	
	@FindBy(css = "input#user_terms_of_use")
	WebElement chkIAgree;
	
	/**
	 * 
	 * Constructor class for Login page Here we initializing the driver for page
	 * factory objects. For Ajax element waiting time has added while initialization
	 * 
	 * @param driver - WebDriver
	 * @param url    - canvas login URL
	 */
	public CanvasLoginPage(WebDriver driver, String url) {
		this.driver = driver;
		canvasUrl = url;
		PageFactory.initElements(driver, this);
	}

	@Override
	protected void isLoaded() {

		if (!isPageLoaded) {
			Assert.fail();
		}

		if (isPageLoaded && !RealizeUtils.waitForElement(driver, txtPassword, 2)) {
			Log.fail("Canvas Login page did not open up. Site might be down.", driver);
		}
		uielement = new ElementLayer(driver);
	}

	@Override
	protected void load() {
		isPageLoaded = true;
		Log.message("Canvas URL : " + canvasUrl);
		driver.get(canvasUrl);
		RealizeUtils.waitForRealizePageLoad(driver);
	}

	/**
     * Login to Canvas dashboard
     * 
     * @param username
     *            as string
     * @param password
     *            as string
     * @param screenShot
     *            to capture screenShot
     * @return CanvasDashboardPage
     */
	public CanvasDashboardPage loginToCanvas(String username, String password, boolean screenShot) {
		enterLoginDetails(username, password, screenShot);
		return new CanvasDashboardPage(driver).get();
	}
	
	
	/**
	 * Enter user name
	 * 
	 * @param userName as string
	 */
	public void enterUserName(String userName) {
		(new WebDriverWait(driver, Duration.ofSeconds(30)).pollingEvery(Duration.ofMillis(200))
				.ignoring(NoSuchElementException.class, StaleElementReferenceException.class)
				.withMessage("Unable to find username text box")).until(ExpectedConditions.visibilityOf(txtUserName));
		txtUserName.clear();
		txtUserName.sendKeys(userName);
		Log.event("Entered the UserName: " + userName);
	}

	/**
	 * Enter password
	 * 
	 * @param pwd as string
	 */
	public void enterPassword(String pwd) {
		(new WebDriverWait(driver, Duration.ofSeconds(30)).pollingEvery(Duration.ofMillis(200))
				.ignoring(NoSuchElementException.class, StaleElementReferenceException.class)
				.withMessage("Unable to find password text box")).until(ExpectedConditions.visibilityOf(txtPassword));
		txtPassword.clear();
		txtPassword.sendKeys(pwd);
		Log.event("Entered the Password: " + pwd);

	}

	/**
	 * Click signIn button on login page
	 * 
	 */
	public void clickBtnSignIn() {
		final long startTime = StopWatch.startTime();
		(new WebDriverWait(driver, Duration.ofSeconds(30)).pollingEvery(Duration.ofMillis(200))
				.ignoring(NoSuchElementException.class, StaleElementReferenceException.class)
				.withMessage("Unable to click signIn button on login page"))
						.until(ExpectedConditions.elementToBeClickable(btnLogin));
		((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnLogin);
		Log.event("Clicked signIn button on login page", StopWatch.elapsedTime(startTime));
	}
	
	/**
	 * Login to Canvas Application
	 * 
	 * @param username   as string
	 * @param password   as string
	 * @param screenShot to capture screenShot
	 */
	public void enterLoginDetails(String username, String password, boolean screenShot) {
		Log.event("Login to the Canvas");
		enterUserName(username);
		enterPassword(password);
		clickBtnSignIn();
		Log.message("Logged into Canvas application as (" + username + "/" + password + ")", driver, screenShot);
	}
}
