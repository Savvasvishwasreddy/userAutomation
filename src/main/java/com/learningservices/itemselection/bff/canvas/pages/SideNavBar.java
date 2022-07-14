package com.learningservices.itemselection.bff.canvas.pages;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.learningservices.itemselection.bff.utils.RealizeUtils;
import com.learningservices.utils.ElementLayer;
import com.learningservices.utils.Log;

public class SideNavBar {

	public ElementLayer uielement;
	private final WebDriver driver;

	// XPATH STRING CONSTANTS
	private static final String linkInSideTrayXpath = ".//a[text()=\"%s\"]";

	@FindBy(css = "#global_nav_profile_link")
	WebElement lnkAccountInNavBar;

	@FindBy(css = "#global_nav_accounts_link")
	WebElement lnkAdminInNavBar;

	@FindBy(css = "#global_nav_dashboard_link")
	WebElement lnkDashboardInNavBar;

	@FindBy(css = "#global_nav_courses_link")
	WebElement lnkCoursesInNavBar;

	@FindBy(xpath = "//button//span[text()='Logout']")
	WebElement btnLogout;

	@FindBy(css = "div.tray-with-space-for-global-nav")
	WebElement sideNavTray;

	@FindBy(css = "div.tray-with-space-for-global-nav ul a")
	WebElement sideNavTrayElement;

	@FindBy(css = "li a[href*='/courses/']")
	List<WebElement> lstCourseTitle;

	@FindBy(xpath = "//*[contains(@class, 'courses-tray')]//a[text()='All Courses']")
	WebElement lnkAllCoursesInCoursesTray;

	/**
	 * 
	 * Constructor class for TopNavBar Here we initializing the driver for page
	 * factory objects and specific wait time for Ajax element
	 * 
	 * @param driver
	 */
	public SideNavBar(WebDriver driver) {
		this.driver = driver;
		ElementLocatorFactory finder = new AjaxElementLocatorFactory(driver, 5);
		PageFactory.initElements(finder, this);
		uielement = new ElementLayer(driver);
	}
	
	
	/**
	 * To click Account link in Side NavBar to open courses tray
	 * @param screenshot
	 */
	public void clickCoursesLink(boolean screenshot) {
		Log.event("Clicking on 'Courses' link");
		(new WebDriverWait(driver, Duration.ofSeconds(20)).pollingEvery(Duration.ofMillis(2)).ignoring(NoSuchElementException.class)
				.withMessage("Unable to locate the alert box."))
						.until(ExpectedConditions
								.invisibilityOfElementLocated(By.cssSelector("li[class='ic-flash-success']")));
		RealizeUtils.waitForElementToBeClickable(lnkCoursesInNavBar, driver, 20);
		RealizeUtils.scrollIntoView(driver, lnkCoursesInNavBar);
		lnkCoursesInNavBar.click();
		RealizeUtils.waitForElement(driver, sideNavTray, 15);
		Log.message("Clicked on 'Courses' link", driver, screenshot);
	}
	
	/**
	 * Navigate to canvas courses page by clicking Courses link
	 * 
	 * @param screenshot - to capture screenshot
	 * @throws Exception 
	 */
	public CanvasCoursePage navigateToCoursesPage(String courseName, boolean screenshot) throws Exception {
		Log.event("Navigating to " + courseName + " page");
		clickCoursesLink(screenshot);
		RealizeUtils.waitForElement(driver, sideNavTrayElement, 30);
		selectCourseInSideTray(courseName, false);
		CanvasCoursePage coursesPage = new CanvasCoursePage(driver).get();
		Log.message("Navigated to " + courseName + " page", driver, screenshot);
		return coursesPage;
	}
	
	/**
	 * To select a particular course from list of course in side tray
	 * 
	 * @param courseName - name of the course
	 * @param screenShot
	 */
	public void selectCourseInSideTray(String courseName, boolean screenShot) throws Exception {
		Log.event("Clicking on " + courseName + " program");
		RealizeUtils.waitForElement(driver, lnkAllCoursesInCoursesTray);
		WebElement courseLink = RealizeUtils.getMachingTextElementFromList(lstCourseTitle, courseName);
		RealizeUtils.scrollIntoView(driver, courseLink);
		RealizeUtils.clickJS(driver, courseLink);
		RealizeUtils.waitForPageLoad(driver);
		Log.message("Clicked the course '" + courseName + "' in side tray", driver, screenShot);
	}

}
