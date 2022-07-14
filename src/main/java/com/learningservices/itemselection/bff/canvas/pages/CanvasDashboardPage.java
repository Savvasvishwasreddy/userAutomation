package com.learningservices.itemselection.bff.canvas.pages;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.LoadableComponent;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.learningservices.itemselection.bff.utils.RealizeUtils;
import com.learningservices.utils.ElementLayer;
import com.learningservices.utils.Log;
import com.learningservices.utils.Utils;

public class CanvasDashboardPage extends LoadableComponent<CanvasDashboardPage> {

	public SideNavBar sideNav;
	public ElementLayer uielement;
	private boolean isPageLoaded;
	private final WebDriver driver;

	@FindBy(xpath = "//h1/span[text()='Dashboard']")
	WebElement lblDashboardHeader;
	
	@FindBy(css = "#start_new_course")
	WebElement btnNewCourse;

	@FindBy(css = "form#new_course_form input#course_name")
	WebElement txtCourseName;
	
	@FindBy(css = "form#new_course_form select#course_license")
	WebElement drpCourseLicense;
	
	@FindBy(xpath = "//button/span[text()='Create course']")
	WebElement btnCreateCourse;
	
	@FindBy(css = "li[id*='context_module'] div a[class*='title']:not([class='item_name'])")
	List<WebElement> lstContentName;

	@FindBy(css = "#global_nav_accounts_link")
	WebElement adminMenu;
	
	@FindBy(css = "div.accounts-tray a[href*=\"/accounts/1\"]")
	WebElement adminMenuSavvasRealize;
	
	@FindBy(css = ".menu-item #global_nav_courses_link")
	WebElement courseMenu;
	
	@FindBy(css = "a[href='/courses']")
	WebElement allCourses;
	
	@FindBy(css = "button[name='accept']")
	WebElement courseAccept;
	
	@FindBy(css = "#content > div > table > tbody > tr > td:nth-child(2)")
	WebElement email;
	
	@FindBy(xpath = "//a[text()='Delete from Savvas Realize']")
	WebElement lnkDeletePeople;
	
	@FindBy(xpath = "//li[contains(@text, 'classes.action.createClasses')]")
    WebElement lnkCreateClasses;
	
	/**
	 * 
	 * Constructor class for Login page Here we initializing the driver for page
	 * factory objects. For Ajax element waiting time has added while initialization
	 * 
	 * @param driver - WebDriver
	 */
	public CanvasDashboardPage(WebDriver driver) {
		this.driver = driver;
		PageFactory.initElements(driver, this);
	}

	@Override
	protected void isLoaded() {

		if (!isPageLoaded) {
			Assert.fail();
		}
		
		if (isPageLoaded && !RealizeUtils.waitForElement(driver, lblDashboardHeader, 20)) {
			Log.fail("Canvas Dashboard page did not open up after 20 sec.", driver);
		}

		sideNav = new SideNavBar(driver);
		uielement = new ElementLayer(driver);
	}

	@Override
	protected void load() {
		isPageLoaded = true;
		Utils.waitForPageLoad(driver);
	}

	/**
	 * To verify the canvas home page and check start new course button and title on
	 * home page
	 * 
	 * @param screenShot to capture screenShot
	 * 
	 * @return boolean if all element present return true else false
	 */
	public boolean verifyCanvasDashboardPage(boolean screenShot) {
		Log.event("Verifying Canvas Dashboard");
		boolean status = false;
		if (RealizeUtils.waitForElement(driver, btnNewCourse) || RealizeUtils.waitForElement(driver, lblDashboardHeader)) {
			status = true;
		} else if (!RealizeUtils.waitForElement(driver, btnNewCourse)) {
			return false;
		}
		Log.message("Canvas Dashboard verified successfully", driver, screenShot);
		return status;
	}

	/**
	 * To create new Course with given course License from Dashboard Page
	 * @param courseTitle
	 * @param courseLicense
	 * @return - CanvasCoursePage
	 * @throws Exception 
	 */
	public CanvasCoursePage createNewCourse(String courseTitle, String courseLicense) throws Exception {
		Log.event("Creating a new Course from Dashboard Page");
		if (RealizeUtils.waitForElement(driver, btnNewCourse, 30)) {
			btnNewCourse.click();
			RealizeUtils.waitForElementToBeClickable(txtCourseName, driver, 20);
			Log.message("Clicked 'Start a New Course' button in Dashboard Page", driver, true);

			txtCourseName.sendKeys(courseTitle);

			if (courseLicense != null && courseLicense.length() > 1)
				RealizeUtils.selectOption(drpCourseLicense, courseLicense);

			btnCreateCourse.click();
			Log.message("Created a new course: " + courseTitle);
		} else {
			throw new Exception("'Start a New Course' button not found in Dashboard Page");
		}
		return new CanvasCoursePage(driver).get();
	}
	
	/**
	 * To create a new course from Dashboard Page
	 * @param title - name of the course
	 * @return - CanvasCoursePage
	 * @throws Exception 
	 */
	public CanvasCoursePage createNewCourse(String title) throws Exception {
		return createNewCourse(title, null);
	}

	/**
	 * To select a particular content from list of content
	 * 
	 * @param contentName - name of the content
	 * @param screenShot
	 */
	public void selectContentFromCourse(String contentName, boolean screenShot) throws Exception {
		Log.event("Clicking on " + contentName + " content");
		WebElement element = null;
		element = RealizeUtils.getMachingTextElementFromList(lstContentName, contentName);
		RealizeUtils.scrollIntoView(driver, element);
		((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
		RealizeUtils.waitForPageLoad(driver);
		Log.message("Clicked the course : '" + contentName + "'", driver, screenShot);
	}
	
	 /**
     * To scroll into particular element
     * 
     * @param driver -
     * @param element - the element to scroll to
     */
    public static void scrollIntoView(final WebDriver driver, WebElement element) {
        try {
            String scrollElementIntoMiddle = "var viewPortHeight = Math.max(document.documentElement.clientHeight, window.innerHeight || 0);"
                                            + "var elementTop = arguments[0].getBoundingClientRect().top;"
                                            + "window.scrollBy(0, elementTop-(viewPortHeight/2));";
            ((JavascriptExecutor) driver).executeScript(scrollElementIntoMiddle, element);
            (new WebDriverWait(driver, Duration.ofSeconds(20)).pollingEvery(Duration.ofMillis(500)).ignoring(NoSuchElementException.class, StaleElementReferenceException.class).withMessage("Realize spinners/page not loading")).until(RealizeUtils.realizeLoad);
        } catch (Exception ex) {
            Log.event("Moved to element..");
        }
    }

	/**
	 * To click on 'Admin > Savvas Realize' page
	 * 
	 * @param screenShot
	 */
	public void clickAdminMenu(boolean screenShot) {
		Log.event("Clicking on Admin > Savvas Realize page");
		RealizeUtils.waitForElement(driver, adminMenu);
		adminMenu.click();
		RealizeUtils.waitForElement(driver, adminMenuSavvasRealize, 60);
		adminMenuSavvasRealize.click();
		Log.message("Navigated to Admin > Savvas Realize page", driver, screenShot);
	}
	
	/**
	 * To click on Course in Menu section
	 * 
	 * @param screenShot
	 */
	public void clickCourseMenu(boolean screenShot) {
		try {
			Log.event("Clicking on Course in Menu section");
			RealizeUtils.waitForElement(driver, courseMenu);
			courseMenu.click();
			RealizeUtils.waitForElement(driver, allCourses, 60);
			allCourses.click(); 
			Log.message("Navigated to Courses page", driver, screenShot);
		} catch (Exception e) {
			Log.message("Error in clicking Course Menu. Error: " + e.getMessage());
		}
	}

	/**
	 * To click on Accept button for invited Course
	 * 
	 * @param screenShot
	 */
	public void clickAcceptButton(boolean screenShot) {
		try {
			Log.event("Clicking on Accept button for invited Course");
			RealizeUtils.waitForElement(driver, courseAccept);
			courseAccept.click();
			Log.message("Clicked Accept button for new invited Course", driver, screenShot);
		} catch (Exception e) {
			Log.message("Error in clicking Accept button for invited Course. Error: " + e.getMessage());
		}
	}	
}
