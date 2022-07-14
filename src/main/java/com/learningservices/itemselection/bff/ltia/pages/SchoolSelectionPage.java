package com.learningservices.itemselection.bff.ltia.pages;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.text.StringEscapeUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.LoadableComponent;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.learningservices.itemselection.bff.utils.RealizeUtils;
import com.learningservices.utils.ElementLayer;
import com.learningservices.utils.Log;
import com.learningservices.utils.ShadowDOMUtils;

public class SchoolSelectionPage extends LoadableComponent<SchoolSelectionPage> {
	private boolean isPageLoaded;
	private final WebDriver driver;
	public ElementLayer uielement;

	private static final String schoolNamesInDropDownCSS = "span.menu-item__label";
	private static final String chooseAnotherSchoolCSS = "div.schoolSelectContainer label[for='others']";
	private static final String schoolNamesDropDownContainerCSS = "div.schoolSelectContainer .dropdown__menu";
	public static final String CHOOSE_ANOTHER_SCHOOL_TEXT = "Choose another school in your organization";
	public final String SCHOOL_SELECTION_COURSE_TITLE = "Class Setup";
	public final String SCHOOL_SELECTION_COURSE_HEADING = "Which school does \"%s\" belong to?";
	public final String COURSE_DROPDOWN_SCHOOL_TEXT = "Choose or search a school";
	public final String SCHOOL_SELECTION_PAGE_INSTRUCTION = "Include all the schools where you teach to access all content available to you";
	public final String SCHOOL_SELECTION_PAGE_TITLE = "User Account Setup";
	public final String SCHOOL_SELECTION_PAGE_HEADING = "Select your school(s)";
	public final String SCHOOL_SELECTION_PAGE_SEARCH_DROPDOWN = "Select or search your school(s)";
	public final String SCHOOL_SELECTION_PAGE_SUPPORT_LINK_TEXT = "Click here to learn more and get help.";
	public static final String OOPS_ERROR_MESSAGE = "Oops! Something went wrong";
	private static final String linkNoSchoolLocator ="span.support_link";
	public final String NO_ACTIVE_SCHOOL_ERROR_MESSAGE = "Oops! There are no active schools in your school district, \"LTIABasicDistrictF.\" Click here to learn more and resolve the issue.";
	public final String CANT_SELECT_SCHOOL_ERROR_MESSAGE = "Oops! You cannot select the school your class belongs to because your account is not associated with any active schools. Click here to learn more and resolve the issue.";
	public static final String LEARNMORE_NEWPAGE_URL_FOR_CLASS = "https://sites.google.com/view/savvas-realize-lms-integration/faqs/lets-set-up-your-class-with-savvas-realize";
	public static final String LEARNMORE_NEWPAGE_URL_FOR_USER = "https://sites.google.com/view/savvas-realize-lms-integration/faqs/lets-set-up-your-savvas-realize-user-account";
	public static final String BLUE = "#0072ee";
	
    @FindBy(css = "img.realize-logo, img[aria-label*='Realize logo']")
	WebElement realizeLogo;

	@FindAll({ @FindBy(css = "input#cel-type-ahead-search-input-bar"),
			@FindBy(css = "div.schoolSelectContainer input#cel-search-dropdown") })
	WebElement txtSearchSchool;
	
	@FindBy(className = "page-title")
	WebElement lblSchoolSelectionPageTitle;

	// ----------- School Selection of Teacher -----------//

	@FindBy(css = "div.welcome-message")
	WebElement welcomeMessage;

	@FindBy(className = "schoolSelectLabel")
	WebElement lblSchoolSelectionHeading;

	@FindBy(className = "schoolSelectInstruction")
	WebElement lblSchoolSelectionInstruction;

	@FindBy(className = "searchDropdown__singleSelect")
	WebElement drpSearchSchoolForTeacher;

	@FindBy(css = "button.search-bar_icon_button")
	WebElement btnDropdownArrow;

	@FindBy(css = "input#cel-type-ahead-search-input-bar")
	WebElement txtSearchSchoolForTeacher;

	@FindBy(css = "div.search-bar-with-list")
	WebElement txtSearchSchoolDropDownList;

	@FindBy(css = ".searchDropdown__singleSelect .dropdown-menu-list")
	WebElement searchDropdownList;

	@FindBy(css = "div.chip.sc-cel-type-ahead-search-bar")
	List<WebElement> lstSelectedSchools;

	@FindBy(css = "span.chip-text")
	List<WebElement> lstSelectedSchoolTitles;

	@FindBy(css = "div.select_button>cel-button")
	WebElement btnSelect;
	
	@FindBy(css = "span.support-link")
	WebElement lnkUserAndClassSetup;

	// ----------- School Selection of Course -----------//
	
	@FindBy(css = "div.schoolSelectContainer div.radio-button-label")
	WebElement lblSchoolSelectionTitleForCourse;

	@FindBy(css = "div.schoolSelectContainer div.radio-button-label")
	WebElement lblSchoolSelectionHeadingForCourse;

	@FindBy(css = "div.schoolSelectContainer input:not(#others)")
	List<WebElement> lstTeacherSelectedSchoolRadioButtons;

	@FindBy(css = "div.schoolSelectContainer div.radio-button-labels")
	List<WebElement> lstTeacherSelectedSchoolLabels;

	@FindBy(css = chooseAnotherSchoolCSS)
	WebElement rbChooseAnotherSchool;

	@FindBy(css = "div.schoolSelectContainer input#cel-search-dropdown")
	WebElement txtSearchSchoolForCourse;

	@FindBy(css = schoolNamesDropDownContainerCSS)
	WebElement divSearchDropdownListContainer;
	
	@FindBy(css = "div[class='default-error-message']")
    WebElement lblErrorMsg;
	
	@FindBy(css = "div[class='custom-error-message'] h1")
	WebElement txtErrorheader;
	
	@FindBy(css = "div[class='custom-error-message'] p")
	WebElement txtErrorMessage;
	
	@FindBy(css = "div.error-icon-wrapper")
    WebElement sadFaceImage;
	
	@FindBy(css = ".custom-error-message :nth-child(3)")
	WebElement lnkSupport;

	public SchoolSelectionPage(WebDriver driver) {
		this.driver = driver;
		ElementLocatorFactory finder = new AjaxElementLocatorFactory(driver, RealizeUtils.realizeMinElementWait);
		PageFactory.initElements(finder, this);
	}

	@Override
	protected void isLoaded() throws Error {
		if (!isPageLoaded) {
			Assert.fail();
		}

		if (isPageLoaded && !RealizeUtils.waitForElement(driver, realizeLogo)) {
			Log.fail("School selection page did not open up.", driver);
		}

		uielement = new ElementLayer(driver);
	}

	@Override
	protected void load() {
		isPageLoaded = true;
		RealizeUtils.waitForSchoolSelectionPageLoad(driver, 60);
	}

	// ----------- Common Reusables -----------//

	/**
	 * To click Select button in School selection page
	 * 
	 * @param screenShot
	 * @throws Exception
	 */
	public void clickSelectButton(boolean screenShot) throws Exception {
		Log.event("Clicking 'Select' button in School selection page");
		if (RealizeUtils.waitForElement(driver, btnSelect)) {
			if (!isSelectButtonDisabled()) {
				btnSelect.click();
				Log.message("Clicked Select button in School selection page", driver, screenShot);
			} else {
				throw new Exception("Select button is disabled in School selection page");
			}
		} else {
			throw new Exception("Unable to find Select button in School selection page");
		}
	}

	/**
	 * To verify Select button is enabled or not
	 * 
	 * @return
	 */
	public boolean isSelectButtonDisabled() {
		boolean flag = false;
		try {
			if (btnSelect.getAttribute("disabled") != null && btnSelect.getAttribute("disabled").equals("true")) {
				flag = true;
			}
		} catch (Exception e) {
			Log.event("Select button is not visible or exists. Error: " + e.getMessage());
		}
		return flag;
	}
	
	/**
	 * To verify Click here to learn more and get help hyperlink is displayed or not
	 * 
	 * @return
	 */
	public boolean verifySupportLinkDisplayed() {
		boolean flag = false;
		try {
			if (RealizeUtils.waitForElement(driver, lnkUserAndClassSetup) && RealizeUtils.checkColor(lnkUserAndClassSetup, BLUE)
					&& lnkUserAndClassSetup.getCssValue("text-align").contains("center")) {
				flag = true;
			}
		} catch (Exception e) {
			Log.event("Click here to learn more and get help hyperlink is not visible or exists. Error: " + e.getMessage());
		}
		return flag;
	}

	/**
	 * To search given keyword in the school selection textbox
	 * 
	 * @param searchText - Keyword to search
	 * @param screenShot
	 * @throws Exception
	 */
	public void searchInSchoolSelectionDropdown(String searchText, boolean screenShot) throws Exception {
		Log.event("Entering '" + searchText + "' in school selection textbox");
		if (RealizeUtils.waitForElement(driver, txtSearchSchool)) {
			RealizeUtils.scrollIntoView(driver, txtSearchSchool);
			RealizeUtils.clearFields(txtSearchSchool, driver, false);
			txtSearchSchool.click();
			RealizeUtils.nap(1);
			txtSearchSchool.sendKeys(searchText);
			Log.message("Entered '" + searchText + "' in school selection textbox", driver, screenShot);
		} else {
			throw new Exception("'Choose or search a school' textbox not found");
		}
	}

	// ----------- School Selection of Teacher -----------//

	public boolean isSchoolSelectionPageForTeacherLoaded() {
		Log.event("Verifying School selection page for teacher is loaded or not");
		return (RealizeUtils.waitForElement(driver, realizeLogo) && RealizeUtils.waitForElement(driver, welcomeMessage))
				&& (RealizeUtils.waitForElement(driver, btnSelect) && RealizeUtils.waitForElement(driver, lblSchoolSelectionPageTitle));
	}

	/**
	 * To click down arrow in school selection page for teacher
	 * 
	 * @throws Exception
	 */
	public void clickDownArrowInSchoolSelectionDropdownForTeacher() throws Exception {
		Log.event("Clicking dropdown arrow in School selection page for teacher");
		try {
			if (RealizeUtils.waitForElement(driver, btnDropdownArrow)) {
				btnDropdownArrow.click();
			} else {
				throw new Exception("Down arrow in school selection page not found");
			}
		} catch (Exception e) {
			throw new Exception(
					"Unable to click down arrow in school selection page for teacher. Error: " + e.getMessage());
		}
	}

	/**
	 * To verify school dropdown list for teacher is displaying or not
	 * 
	 * @return
	 */
	public boolean isSchoolDropDownListForTeacherDisplaying() {
		return RealizeUtils.waitForElement(driver, txtSearchSchoolDropDownList, 5);
	}

	/**
	 * To select given list of schools in school selection dropdown for teacher
	 * 
	 * @param schoolNames - List of school display names
	 * @param screenShot - to take a Screenshot
	 * @throws Exception
	 */
	public void selectGivenSchoolsInSchoolSelectionDropdown(List<String> schoolNames, boolean screenShot) throws Exception {
		Log.event("Selecting given schools in School selection page dropdown for teacher");
		try {
			if (!isSchoolDropDownListForTeacherDisplaying())
				clickDownArrowInSchoolSelectionDropdownForTeacher();
			
			RealizeUtils.waitForElement(driver, txtSearchSchoolDropDownList, 15);
			List<WebElement> lstSchoolNameDropdownOptions = ShadowDOMUtils.findElements(schoolNamesInDropDownCSS,
					driver);
			for (String school : schoolNames) {
				searchInSchoolSelectionDropdown(school.replace("...", ""), true);
				WebElement schoolElement = RealizeUtils.getMachingTextElementFromList(lstSchoolNameDropdownOptions,
						school);
				WebElement checkbox = schoolElement.findElement(By.xpath("../input"));
				if (!checkbox.isSelected()) {
					RealizeUtils.clickJS(driver, schoolElement);
					Log.message("Selected '" + school + "' from school selection dropdown", driver, screenShot);
				} else {
					throw new Exception("<b>" + school + "</b> is already selected in school selection dropdown");
				}
			}
		} catch (Exception e) {
			throw new Exception(
					"Unable to select given schools in School Selection page for teacher. Error: " + e.getMessage());
		}
	}

	/**
	 * To remove given schools from the selected school tiles
	 * 
	 * @param schoolNames - List of school display names
	 * @throws Exception
	 */
	public void removeSelectedSchoolFromSearchBar(List<String> schoolNames) throws Exception {
		Log.event("Removing given schools in School selection page dropdown for teacher");
		try {
			RealizeUtils.waitForListElement(driver, lstSelectedSchools, 10);
			for (String school : schoolNames) {
				WebElement schoolElement = RealizeUtils.getMachingTextElementFromList(lstSelectedSchoolTitles, school);
				WebElement closeIcon = schoolElement.findElement(By.xpath(".."))
						.findElement(By.className("chip-close-icon"));
				RealizeUtils.scrollElementIntoMiddle(driver, closeIcon);
				RealizeUtils.clickJS(driver, closeIcon);
			}
		} catch (Exception e) {
			throw new Exception(
					"Unable to remove selected schools in School Selection page for teacher. Error: " + e.getMessage());
		}
	}

	/**
	 * To uncheck given schools from the selected school dropdown list
	 * 
	 * @param schoolNames - List of school display names
	 * @throws Exception
	 */
	public void uncheckSelectedSchoolFromDropdown(List<String> schoolNames) throws Exception {
		Log.event("Unchecking given schools in School selection page dropdown for teacher");
		try {
			if (!isSchoolDropDownListForTeacherDisplaying())
				clickDownArrowInSchoolSelectionDropdownForTeacher();

			RealizeUtils.waitForElement(driver, txtSearchSchoolDropDownList, 15);
			List<WebElement> lstSchoolNameDropdownOptions = ShadowDOMUtils.findElements(schoolNamesInDropDownCSS,
					driver);
			for (String school : schoolNames) {
				WebElement schoolElement = RealizeUtils.getMachingTextElementFromList(lstSchoolNameDropdownOptions,
						school);
				RealizeUtils.scrollElementIntoMiddle(driver, schoolElement);
				WebElement checkbox = schoolElement.findElement(By.xpath("../input"));
				if (checkbox.isSelected()) {
					RealizeUtils.clickJS(driver, schoolElement);
					Log.message("Unchecked '" + school + "' from school selection dropdown");
				} else {
					throw new Exception("<b>" + school + "</b> is not selected in school selection dropdown");
				}
			}
		} catch (Exception e) {
			throw new Exception(
					"Unable to uncheck given schools in School Selection page for teacher. Error: " + e.getMessage());
		}
	}

	/**
	 * To get list of schools in School Selection page
	 * 
	 * @return list of schools name
	 * @throws Exception
	 */
	public List<String> getSchoolsInSchoolSelectionDropdown() throws Exception {
		Log.event("Getting list of schools in School Selection page");
		List<String> listOfSchools = new ArrayList<String>();
		List<WebElement> lstSchoolNameDropdownOptions = ShadowDOMUtils.findElements(schoolNamesInDropDownCSS, driver);
		for (WebElement element : lstSchoolNameDropdownOptions) {
			listOfSchools.add(RealizeUtils.getVisibleTextFromElement(element, driver));
		}
		return listOfSchools;
	}

	/**
	 * To get the count for the schools in School Selection page
	 * 
	 * @param toolName
	 * @return count of the schools
	 */
	public int getSchoolsCountInSchoolSelectionDropdown(String toolName) {
		Log.event("Getting number of schools in School Selection page");
		int count = 0;
		List<WebElement> lstSchoolNameDropdownOptions = ShadowDOMUtils.findElements(schoolNamesInDropDownCSS, driver);
		count = lstSchoolNameDropdownOptions.size();
		Log.message("Total number of schools " + count + " is displayed in school selection dropdown for tool " + toolName);
		return count;
	}
	
	/**
	 * Click on the 'Click here to learn more and get help.' link in School Selection page
	 */
	public void clickLearnMoreAndGetHelpLink() {
		Log.event("Clicking the 'Click here to learn more and get help' link");
		RealizeUtils.waitForElement(driver, lnkUserAndClassSetup);
		lnkUserAndClassSetup.click();
		Log.message("Clicked the 'Click here to learn more and get help' support link", driver, true);
	}

	// ----------- School Selection of Course -----------//

	public boolean isSchoolSelectionPageForCourseLoaded() {
		Log.event("Verifying School selection page for course is loaded or not");
		return (RealizeUtils.waitForElement(driver, realizeLogo) && RealizeUtils.waitForElement(driver, lblSchoolSelectionPageTitle))
				&& (RealizeUtils.waitForElement(driver, lblSchoolSelectionHeadingForCourse) 
				&& RealizeUtils.waitForElement(driver, btnSelect));
	}

	/**
	 * To verify 'Choose another school in your organization' radio button is displayed or not
	 * 
	 * @return
	 */
	public boolean isChooseAnotherSchoolRadioButtonDisplaying() {
		Log.event("Verifying '" + CHOOSE_ANOTHER_SCHOOL_TEXT + "' radio button is not displayed or not");
		boolean isDisplaying = false;
		try {
			(new WebDriverWait(driver, Duration.ofSeconds(30)).pollingEvery(Duration.ofMillis(100))
					.ignoring(NoSuchElementException.class).ignoring(StaleElementReferenceException.class)
					.withMessage("'" + CHOOSE_ANOTHER_SCHOOL_TEXT + "' radio button is not displayed"))
							.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(chooseAnotherSchoolCSS)));
			if (rbChooseAnotherSchool.isDisplayed()) {
				WebElement rbText = rbChooseAnotherSchool.findElement(By.xpath("../../div[@class='radio-button-labels']"));
				isDisplaying = RealizeUtils.verifyWebElementTextEquals(rbText, CHOOSE_ANOTHER_SCHOOL_TEXT);
			}
		} catch (Exception e) {
			Log.event("Unable to find '" + CHOOSE_ANOTHER_SCHOOL_TEXT + "' radio button");
		}
		return isDisplaying;
	}

	/**
	 * To click 'Choose another school in your organization' radio button in school selection page
	 * 
	 * @param screenShot
	 * @throws Exception
	 */
	public void clickChooseAnotherSchoolRadioButton(boolean screenShot) throws Exception {
		Log.event("Clicking '" + CHOOSE_ANOTHER_SCHOOL_TEXT + "' radio button in School selection page");
		if (isChooseAnotherSchoolRadioButtonDisplaying()) {
			RealizeUtils.clickJS(driver, rbChooseAnotherSchool);
			RealizeUtils.waitForElement(driver, txtSearchSchoolForCourse);
			Log.message("Clicked '" + CHOOSE_ANOTHER_SCHOOL_TEXT + "' radio button in School selection page",
					driver, screenShot);
		} else {
			throw new Exception(
					"Unable to find '" + CHOOSE_ANOTHER_SCHOOL_TEXT + "' radio button in School selection page");
		}
	}

	/**
	 * To click down arrow in school selection dropdown for course
	 * 
	 * @throws Exception
	 */
	public void clickDownArrowInSchoolSelectionDropdownForCourse() throws Exception {
		Log.event("Clicking dropdown arrow in School selection page for course");
		try {
			(new WebDriverWait(driver, Duration.ofSeconds(30)).pollingEvery(Duration.ofMillis(100))
					.ignoring(NoSuchElementException.class).ignoring(StaleElementReferenceException.class)
					.withMessage("School selection dropdown for course is not displayed"))
							.until(ExpectedConditions
									.presenceOfElementLocated(By.cssSelector(schoolNamesDropDownContainerCSS)));
			WebElement downArrow = ShadowDOMUtils.findElement("button.dropdown-trigger", driver);
			downArrow.click();
		} catch (Exception e) {
			throw new Exception(
					"Unable to click down arrow in school selection page for course. Error: " + e.getMessage());
		}
	}

	/**
	 * To verify school selection dropdown list is displayed or not
	 * 
	 * @return
	 */
	public boolean isSchoolDropDownListForCourseDisplaying() {
		boolean isDisplaying = false;
		try {
			isDisplaying = RealizeUtils.waitForElement(driver, ShadowDOMUtils.findElement("ul.menu", driver), 5);
		} catch (Exception e) {
			Log.event("School dropdown list for course is not opened. Error: " + e.getMessage());
		}
		return isDisplaying;
	}

	/**
	 * To select given school in school selection dropdown for course
	 * 
	 * @param schoolName - School display name
	 * @param screenShot
	 * @param actions - optional, to perform action click
	 * @throws Exception
	 */
	public void selectGivenSchoolInSchoolSelectionDropdown(String schoolName, boolean screenShot, String... JS) throws Exception {
		Log.event("Selecting given school in School selection page dropdown for course");
		try {
			RealizeUtils.waitForElement(driver, txtSearchSchoolForCourse, 15);
			List<WebElement> lstSchoolNameDropdownOptions = ShadowDOMUtils.findElements(schoolNamesInDropDownCSS,
					driver);
			WebElement schoolElement = RealizeUtils.getMachingTextElementFromList(lstSchoolNameDropdownOptions,
					schoolName);
			RealizeUtils.waitForElementVisible(driver, schoolElement, 15);
			if(JS.length > 0 && JS[0].toString().contains("JS")) {
				RealizeUtils.clickJS(driver, schoolElement);
			} else {
				schoolElement.click();
			}
			Log.message("Selected '" + schoolName + "' from school selection dropdown", driver, screenShot);
		} catch (Exception e) {
			throw new Exception(
					"Unable to select given school in School Selection page for course. Error: " + e.getMessage());
		}
	}

	/**
	 * To select given school in school selection page radio button for course
	 * 
	 * @param schoolName - School display name
	 * @param screenShot
	 * @throws Exception
	 */
	public void selectGivenSchoolForNewCourse(String schoolName, boolean screenShot) throws Exception {
		Log.event("Selecting radio button of given school in School selection page for course");
		boolean isSchoolFound = false;
		RealizeUtils.waitForListElement(driver, lstTeacherSelectedSchoolLabels, 5);
		for (int index = 0; index < lstTeacherSelectedSchoolLabels.size(); index++) {
			if (RealizeUtils.verifyWebElementTextEquals(lstTeacherSelectedSchoolLabels.get(index), schoolName)) {
				isSchoolFound = true;
				RealizeUtils.clickJS(driver, lstTeacherSelectedSchoolRadioButtons.get(index));
				Log.message("Selected '" + schoolName + "' radio button in school selection for course", driver,
						screenShot);
				break;
			}
		}
		if (!isSchoolFound) {
			throw new Exception(schoolName + " not found in School Selection page for course");
		}
	}
	
	/**
	 * To select given school in school selection page radio button for course
	 * 
	 * @param schoolName - School display name
	 * @param screenShot
	 * @throws Exception
	 */
	public void verifyGivenSchoolsAreDisplayedForTeacher(List<String> schoolNames, boolean screenShot) throws Exception {
		Log.event("Selecting radio button of given school in School selection page for course");
		boolean isSchoolFound = false;
		RealizeUtils.waitForListElement(driver, lstTeacherSelectedSchoolLabels, 5);
			for (int index = 0; index < schoolNames.size(); index++) {
				if (RealizeUtils.verifyWebElementTextEquals(lstTeacherSelectedSchoolLabels.get(index), schoolNames.get(index))) {
					isSchoolFound = true;
					Log.message("Verified: Given school : <b>'" + schoolNames.get(index) + "'</b> is displayed", driver,
							screenShot);
				}
			}
		if (!isSchoolFound) {
			throw new Exception("Given schools are not displayed in School Selection page for course");
		}
	}
	
	/**
	 * To get the Course title text on school selection page
	 * 
	 * @return school title
	 */
	public String getSchoolSelectionTitleForCourse(){
		Log.event("Getting the Course title in School Selection page");
		String actualmessage = "";
		if (RealizeUtils.waitForElement(driver, lblSchoolSelectionPageTitle, 10)) {
			actualmessage = RealizeUtils.getVisibleTextFromElement(lblSchoolSelectionPageTitle, driver);
			Log.message("Course title for School Selection page: " + actualmessage);
		}
		return actualmessage;
	}

	/**
	 * To get the course heading text on school selection page
	 * 
	 * @return school title
	 */
	public String getSchoolSelectionHeadingForCourse(){
		Log.event("Getting the Course title in School Selection page");
		String actualmessage = "";
		if (RealizeUtils.waitForElement(driver, lblSchoolSelectionHeadingForCourse, 10)) {
			actualmessage = RealizeUtils.getVisibleTextFromElement(lblSchoolSelectionHeadingForCourse, driver);
			Log.message("Course title for School Selection page: " + StringEscapeUtils.escapeHtml4(actualmessage));
		}
		return actualmessage;
	}

	/**
	 * To verify radio button is displaying for associated school in school selection page for course
	 * 
	 * @param schoolNames
	 * @return true if school having radio button
	 */
	public boolean verifyRadiobuttonForGivenSchool(List<String> schoolNames)  {
		Log.event("Verifying radio button is displaying for associated school in school selection page for course");
		ArrayList<Boolean> status = new ArrayList<Boolean>();
		try {
			RealizeUtils.waitForElement(driver, lblSchoolSelectionHeadingForCourse, 5);
			for (String school : schoolNames) {
				WebElement schoolElement = RealizeUtils.getMachingTextElementFromList(lstTeacherSelectedSchoolLabels, school);
				WebElement checkBox = schoolElement.findElement(By.xpath("../div"));
				if(checkBox.isDisplayed())
					status.add(true);
				else
					status.add(false);
			}
		} catch (Exception e) {
			status.add(false);
			Log.message("Radio button not displayed for associated school in school selection page for course. Error: " + e.getMessage());
		}
		return RealizeUtils.isAllTrue(status);
	}

	/**
	 * To verify the vertical scroll bar inside the Selection selection page
	 * 
	 * @return true if vertical scroll bar is displayed
	 */
	public boolean verifyVerticalScrollBarInSchoolSelectionPage() {
		JavascriptExecutor javascript = (JavascriptExecutor) driver;
        return (Boolean) javascript.executeScript("return document.documentElement.scrollHeight>document.documentElement.clientHeight;");
	}

	/**
	 * To get list of schools associated in School Selection page for course
	 * 
	 * @return list of schools name
	 */
	public List<String> getSelectedSchoolsInSchoolSelectionPage(boolean... includeAnotherSchoolText) {
		Log.event("Getting list of schools associated in School Selection page for course");
		List<String> listOfSchools = new ArrayList<String>();
		for (WebElement element : lstTeacherSelectedSchoolLabels) {
			String schoolName = element.getText().trim();
			if(includeAnotherSchoolText.length > 0 && includeAnotherSchoolText[0] == true){
				listOfSchools.add(schoolName);
			} else {
				if (!schoolName.equals(CHOOSE_ANOTHER_SCHOOL_TEXT))
					listOfSchools.add(schoolName);
			}
		}
		return listOfSchools;
	}

	/**
	 * To verifying school names are sorted in alphabetical order
	 * 
	 * @param expectedschoolNames
	 * @param actualschoolNames
	 * @return true if school names are matched
	 */
    public boolean verifySchoolNamesAreAlphabeticallySorted(List<String> expectedschoolNames, List<String> actualschoolNames) {
        Log.event("Verifying school names are sorted in alphabetical order");
        List<String> actSchoolNames = new ArrayList<String>();
        List<String> expSchoolNames = new ArrayList<String>();
        for (int i = 0; i < actualschoolNames.size(); i++) {
        	actSchoolNames.add(actualschoolNames.get(i));
        	expSchoolNames.add(expectedschoolNames.get(i));
        }
        Collections.sort(expSchoolNames);
        return actSchoolNames.equals(expSchoolNames);
    }

    /**
     * To verify radio button is selected or not for associated school in school selection page for course
     * 
     * @param schoolNames
     * @return true if radio button is selected for school
     */
    public boolean isRadiobuttonSelectedState(List<String> schoolNames)  {
		Log.event("Verifying radio button is selected for associated school in school selection page for course");
		ArrayList<Boolean> status = new ArrayList<Boolean>();
		try {
			RealizeUtils.waitForElement(driver, lblSchoolSelectionHeadingForCourse, 5);
			for (String school : schoolNames) {
				WebElement schoolElement = RealizeUtils.getMachingTextElementFromList(lstTeacherSelectedSchoolLabels, school);
				WebElement checkBox = schoolElement.findElement(By.xpath("../div/input"));
				if(checkBox.isSelected())
					status.add(true);
				else
					status.add(false);
			}
		} catch (Exception e) {
			status.add(false);
			Log.message("Radio button not selected for associated school in school selection page for course. Error: " + e.getMessage());
		}
		return RealizeUtils.isAllTrue(status);
	}
    
    /**
     * To get the text value in search dropdown for course
     * 
     * @param selectedDropdownText
     * 				- if it is true, selected dropdown value will return otherwise it will return default dropdown text
     * @return values in search text box
     */
    public String getTextInSearchDropdownForCourse(boolean... selectedDropdownText){
		Log.event("Getting the text value in search dropdown for course");
		String actualmessage = "";
		if (RealizeUtils.waitForElement(driver, txtSearchSchoolForCourse, 10)) {
			if(selectedDropdownText.length > 0 && selectedDropdownText[0])
				actualmessage = RealizeUtils.getValueOfInputField(txtSearchSchoolForCourse, driver);
			else
				actualmessage = txtSearchSchoolForCourse.getAttribute("placeholder");
			Log.message(actualmessage + " displayed in search dropdown for course");
		}
		return actualmessage;
	}
    
    /**
     * To get school address for given school in school search dropdown
     * 
     * @param schoolName
     * @return school name with address
     */
    public HashMap<String, String> getGivenSchoolAddressInSearchDropdownForCourse(List<String> schoolDisplayName){
		Log.event("Getting the school address for " + schoolDisplayName + " in search dropdown for course");
		HashMap<String, String> hmContentWithAddress = new HashMap<String, String>();
		String address = "";
		List<WebElement> lstSchoolNameDropdownOptions = ShadowDOMUtils.findElements("ul li.menu-item", driver);
		for(String schoolName : schoolDisplayName){
			for (WebElement element : lstSchoolNameDropdownOptions) {
				if(element.getText().contains(schoolName)){
					address = element.getText().replace(schoolName, "").replace("\n", "").trim();
					hmContentWithAddress.put(schoolName, address);
				}
			}
			Log.message("'" + address + "' is displayed for school '" + schoolName + "' in search dropdown for course");
		}
		return hmContentWithAddress;
	}
    
    /**
	 * To click up arrow in school selection dropdown for course
	 * 
	 * @throws Exception
	 */
	public void clickUpArrowInSchoolSelectionDropdownForCourse() throws Exception {
		Log.event("Clicking dropdown arrow in School selection page for course");
		try {
			(new WebDriverWait(driver, Duration.ofSeconds(30)).pollingEvery(Duration.ofMillis(100))
					.ignoring(NoSuchElementException.class).ignoring(StaleElementReferenceException.class)
					.withMessage("'" + CHOOSE_ANOTHER_SCHOOL_TEXT + "' radio button is not displayed"))
							.until(ExpectedConditions
									.presenceOfElementLocated(By.cssSelector(schoolNamesDropDownContainerCSS)));
			WebElement downArrow = ShadowDOMUtils.findElement("button.dropdown-trigger", driver);
			if(downArrow.getAttribute("aria-expanded").equals("true")) 
				downArrow.click();
			else
				Log.message("Already dropdown arrow is closed in School selection page for course");
		} catch (Exception e) {
			throw new Exception(
					"Unable to click down arrow in school selection page for course. Error: " + e.getMessage());
		}
	}
	
    /**
	 * To verify the scroll bar inside the Selection selection page
	 * 
	 * @param scrollPosition
	 * 				- Vertical/Horizontal
	 * @return true if scroll bar is displayed
	 */
	public boolean verifyScrollBarInSchoolDropdown(String scrollPosition) {
		Log.event("Verifying the scroll bar inside the school dropdown in Selection selection page");
		boolean status = false;
		WebElement element = ShadowDOMUtils.findElement("ul.menu", driver);
		if (("vertical").equalsIgnoreCase(scrollPosition))
			status = RealizeUtils.verifyScrollExistForElement(driver, element);
		else if (("horizontal").equalsIgnoreCase(scrollPosition)){
			JavascriptExecutor js = (JavascriptExecutor)driver;
	    	double clientWidth = Double.valueOf(js.executeScript("return arguments[0].clientWidth;", element).toString());
	   	 	double scrollWidth = Double.valueOf(js.executeScript("return arguments[0].scrollWidth;", element).toString());
	   	 	status = clientWidth < scrollWidth;
		}
		return status;
	}

	/**
	 * To verify the tool tip school name for given school
	 * 
	 * @param schoolName
	 * @param expectedToolTipName
	 * @return true if tool tip is matched for school name
	 */
	public boolean verifyToolTipForSchoolInSchoolSelectionDropdown(String schoolName, String expectedToolTipName){
		Log.event("Verifying tool tip message for school " + schoolName + " in search dropdown for course");
		boolean tooltipStatus = false;
		List<WebElement> lstSchoolNameDropdownOptions = ShadowDOMUtils.findElements(schoolNamesInDropDownCSS, driver);
		for (WebElement element : lstSchoolNameDropdownOptions) {
			if(element.getText().contains(schoolName)){
				if (((RemoteWebDriver) driver).getCapabilities().getBrowserName().matches(".*(safari|firefox).*")) {
		            RealizeUtils.moveToElementJS(driver, element);
		        } else {
		        	RealizeUtils.moveToElementSelenium(driver, element);
		        }
				tooltipStatus = element.getAttribute("title").equals(expectedToolTipName);
			}
		}
		return tooltipStatus;
	}

	/**
	 * To verify selected schools is displayed with name and close icon
	 * 
	 * @param schoolNames - List of school display names
	 * @throws Exception
	 */
	public boolean verifySelectedSchoolWithCloseIconFromSearchBar(List<String> schoolNames) throws Exception {
		boolean status = false;
		Log.event("Verifying selected schools is displaying in search text bar with close icon");
		try {
			RealizeUtils.waitForListElement(driver, lstSelectedSchools, 10);
			for (String school : schoolNames) {
				WebElement schoolElement = RealizeUtils.getMachingTextElementFromList(lstSelectedSchoolTitles, school);
				WebElement closeIcon = schoolElement.findElement(By.xpath(".."))
						.findElement(By.className("chip-close-icon"));
				RealizeUtils.scrollElementIntoMiddle(driver, closeIcon);
				if (RealizeUtils.isMatchingElementFoundInList(lstSelectedSchoolTitles, school)) {
					if (RealizeUtils.waitForElement(driver, closeIcon)) {
						status = true;
						Log.message("Selected school '" + school + "' is displaying with close icon in search bar",
								driver);
					}
				} else {
					status = false;
				}

			}
		} catch (Exception e) {
			throw new Exception(
					"Unable to see selected schools is displayed with close icon in search text bar. Error: "
							+ e.getMessage());
		}
		return status;
	}

	/**
	 * Verify scroll bar in search textbox of school selection page
	 * 
	 * @param takeScreenshot
	 * @return boolean
	 */
	public boolean verifySearchTextBoxScrollBar(boolean takeScreenshot) throws Exception {
		boolean status = false;
		try {
			Log.event("Verifying whether the scrollbar is present in search box for schoolselection page");
			RealizeUtils.highlightElement(driver, lstSelectedSchoolTitles.get(0));
			int count = lstSelectedSchoolTitles.size();
			if (count > 3) {
				int beforeScroll = lstSelectedSchoolTitles.get(0).getLocation().getY();
				((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);",
						lstSelectedSchoolTitles.get(count - 1));
				int afterScroll = lstSelectedSchoolTitles.get(0).getLocation().getY();
				if (beforeScroll > afterScroll) {
					status = true;
				}
			}
		} catch (Exception e) {
			throw new Exception("Unable to see the Scroll bar in search box of school selection page for teacher");
		}
		return status;
	}

	/*
	 * To check status of check box for given schools from the school dropdown after
	 * selecting schools list
	 * 
	 * @param schoolNames - List of school display names
	 */
	public boolean verifyCheckBoxStatusInDropdownAftercheck(List<String> schoolNames) throws Exception {
		Log.event("Verifying checkbox status for given schools in School selection page dropdown for teacher");
		boolean status = false;
		try {
			if (!isSchoolDropDownListForTeacherDisplaying())
				clickDownArrowInSchoolSelectionDropdownForTeacher();

			RealizeUtils.waitForElement(driver, txtSearchSchoolDropDownList, 15);
			List<WebElement> lstSchoolNameDropdownOptions = ShadowDOMUtils.findElements(schoolNamesInDropDownCSS,
					driver);
			for (String school : schoolNames) {
				WebElement schoolElement = RealizeUtils.getMachingTextElementFromList(lstSchoolNameDropdownOptions,
						school);
				RealizeUtils.scrollElementIntoMiddle(driver, schoolElement);
				WebElement checkbox = schoolElement.findElement(By.xpath("../input"));
				if (checkbox.isSelected()) {
					Log.message(school + " is  checked in school selection dropdown");
					status = true;
				} else {
					status = false;
					throw new Exception(school + " is not checked in school selection dropdown");

				}
			}
		} catch (Exception e) {
			throw new Exception("Unable to verify the checkbox status for given schools in School dropdown. Error: "
					+ e.getMessage());
		}
		return status;
	}

	/**
	 * To verify removed schools from the selected school tiles
	 * 
	 * @param schoolNames - List of school display names
	 * @throws Exception
	 */
	public boolean verifyRemovedSchoolFromSearchBar(List<String> schoolNames) throws Exception {
		boolean status = false;
		Log.event("Verify removed schools in School search text bar");
		try {
			RealizeUtils.waitForListElement(driver, lstSelectedSchools, 10);
			for (String school : schoolNames) {
				if (RealizeUtils.isMatchingElementFoundInList(lstSelectedSchoolTitles, school)) {
					status = false;
					Log.message("Selected school '" + schoolNames + "' is not removed from search bar", driver);
				} else {
					Log.message("Removed selected school '" + schoolNames + "' from search bar", driver);
					status = true;
				}
			}
		} catch (Exception e) {
			throw new Exception("Unable to verify removed schools in School search text bar. Error: " + e.getMessage());
		}
		return status;
	}

	/**
	 * To verify the check box status after uncheck the schools
	 * 
	 * @param schoolNames - List of school display names
	 */
	public boolean verifyCheckBoxStatusInDropdownAfterUncheck(List<String> schoolNames) throws Exception {
		Log.event("Unchecking given schools in School selection page dropdown for teacher");
		boolean status = false;
		try {
			if (!isSchoolDropDownListForTeacherDisplaying())
				clickDownArrowInSchoolSelectionDropdownForTeacher();

			RealizeUtils.waitForElement(driver, txtSearchSchoolDropDownList, 15);
			List<WebElement> lstSchoolNameDropdownOptions = ShadowDOMUtils.findElements(schoolNamesInDropDownCSS,
					driver);
			for (String school : schoolNames) {
				WebElement schoolElement = RealizeUtils.getMachingTextElementFromList(lstSchoolNameDropdownOptions,
						school);
				RealizeUtils.scrollElementIntoMiddle(driver, schoolElement);
				WebElement checkbox = schoolElement.findElement(By.xpath("../input"));
				if (checkbox.isSelected()) {
					status = false;
					throw new Exception(school + " is not unchecked in school selection dropdown");
				} else {
					Log.message(school + " is  unchecked in school selection dropdown");
					status = true;
				}
			}
		} catch (Exception e) {
			throw new Exception(
					"Unable to verify the checkbox for given schools in School Selection page for teacher. Error: "
							+ e.getMessage());
		}
		return status;
	}

	/**
	 * To check position of vertical scroll bar for given schools from the school
	 * dropdown after selecting schools list
	 * 
	 * @param schoolNames - List of school display names return true if scrollbar is
	 * right side of dropdown
	 */
	public boolean verifyPositionOfVerticalScrollBarInSchoolDropdown(List<String> schoolNames) throws Exception {
		Log.event("Verifying the position of ScrollBar given schools in School selection page dropdown for teacher");
		boolean status = false;
		try {
			if (!isSchoolDropDownListForTeacherDisplaying())
				clickDownArrowInSchoolSelectionDropdownForTeacher();

			RealizeUtils.waitForElement(driver, txtSearchSchoolDropDownList, 15);
			List<WebElement> lstSchoolNameDropdownOptions = ShadowDOMUtils.findElements(schoolNamesInDropDownCSS,
					driver);
			for (String school : schoolNames) {
				WebElement schoolElement = RealizeUtils.getMachingTextElementFromList(lstSchoolNameDropdownOptions,
						school);
				RealizeUtils.scrollElementIntoMiddle(driver, schoolElement);
				WebElement scrollBar = ShadowDOMUtils.findElement("ul.menu", driver);
				if (schoolElement.getLocation().getX() > scrollBar.getLocation().getX()) {
					Log.event("Vertical scroll bar is  displays on right side in school selection dropdown");
					status = true;
				} else {
					status = false;
					Log.event("Scroll bar position value: " + scrollBar.getLocation().getX());
					Log.event("School name position value: " + schoolElement.getLocation().getX());
					throw new Exception(
							"Vertical scroll bar is not displays on right side in school selection dropdown");

				}
			}
		} catch (Exception e) {
			throw new Exception(
					"Unable to verify the vertical scroll bar position for schools in School dropdown. Error: "
							+ e.getMessage());
		}
		return status;
	}

	/**
	 * To check position of check box for given schools from the school dropdown
	 * after selecting schools list
	 * 
	 * @param schoolNames - List of school display names return true if checkbox is
	 * left side of schoolname in dropdown
	 */
	public boolean verifyPositionOfCheckBoxInSchoolDropdown(List<String> schoolNames) throws Exception {
		Log.event("Verifying the position of checkbox in School selection page dropdown for teacher");
		boolean status = false;
		try {
			if (!isSchoolDropDownListForTeacherDisplaying())
				clickDownArrowInSchoolSelectionDropdownForTeacher();

			RealizeUtils.waitForElement(driver, txtSearchSchoolDropDownList, 15);
			List<WebElement> lstSchoolNameDropdownOptions = ShadowDOMUtils.findElements(schoolNamesInDropDownCSS,
					driver);
			for (String school : schoolNames) {
				WebElement schoolElement = RealizeUtils.getMachingTextElementFromList(lstSchoolNameDropdownOptions,
						school);
				RealizeUtils.scrollElementIntoMiddle(driver, schoolElement);
				WebElement checkbox = schoolElement.findElement(By.xpath("../input"));
				if (checkbox.getLocation().getX() < schoolElement.getLocation().getX()) {
					Log.message("check box displays on left of the " + school + " in school selection dropdown");
					status = true;
				} else {
					status = false;
					Log.event("Checkbox position value: " + checkbox.getLocation().getX());
					Log.event("School position value: " + schoolElement.getLocation().getX());
					throw new Exception(
							"check box not displays on left of the " + school + " in school selection dropdown");

				}
			}
		} catch (Exception e) {
			throw new Exception(
					"Unable to verify the checkbox position for schools in School dropdown. Error: " + e.getMessage());
		}
		return status;
	}

	/**
	 * To verify selected schools is displayed with name and close icon
	 * 
	 * @param schoolNames - List of school display names return true if school is
	 *                    listed in search bar
	 * @throws Exception
	 */
	public boolean verifySelectedSchoolFromSearchBar(List<String> schoolNames) throws Exception {
		boolean status = false;
		Log.event("Verifying selected schools is displaying in search text bar");
		try {
			RealizeUtils.waitForListElement(driver, lstSelectedSchools, 10);
			for (String school : schoolNames) {
				if (RealizeUtils.isMatchingElementFoundInList(lstSelectedSchoolTitles, school)) {
					status = true;
					Log.message("Selected school '" + school + "' is displaying in search text bar", driver);
				} else {
					status = false;
					Log.message("Selected school '" + school + "' is not displaying in search text bar", driver);
				}

			}
		} catch (Exception e) {
			throw new Exception(
					"Unable to see selected schools is displayed in search text bar. Error: " + e.getMessage());
		}
		return status;
	}

	/***
	 * To verify the realize logo displays on top left corner return true if present
	 * at top left corner
	 */
	public boolean verifyRealizeLogoPosition() {
		Log.event("Verifying position of realize logo at left corner in school selection page");
		Point logo = realizeLogo.getLocation();
		Point message = welcomeMessage.getLocation();
		Point selectButton = btnSelect.getLocation();
		if (logo.x < message.x && logo.y < selectButton.y)
			return true;
		else
			return false;
	}

	/***
	 * To verify the welcome message center displays on center of the page return
	 * true if present at center
	 */
	public boolean verifyWelcomeMessageAlignment() {
		boolean status = false;
		Log.event("Verifying position of welcome message at center in school selection page");
		if ((welcomeMessage.getCssValue("text-align").contains("center"))) {
			status = true;
		} else {
			status = false;
		}
		return status;
	}

	/**
	 * To get the User account heading text on school selection page for teacher
	 * 
	 * @return
	 */
	public String getSchoolSelectionPageHeadingForTeacher() {
		Log.event("Getting the title in School Selection page");
		String actualmessage = "";
		if (RealizeUtils.waitForElement(driver, lblSchoolSelectionHeading, 10)) {
			actualmessage = RealizeUtils.getVisibleTextFromElement(lblSchoolSelectionHeading, driver);
			Log.message("Title for School Selection page: " + actualmessage);
		}
		return actualmessage;
	}
	
	/**
	 * To get the User account title text on school selection page for teacher
	 * 
	 * @return
	 */
	public String getSchoolSelectionPageTitleForTeacher() {
		Log.event("Getting the title in School Selection page");
		String actualmessage = "";
		if (RealizeUtils.waitForElement(driver, lblSchoolSelectionPageTitle, 10)) {
			actualmessage = RealizeUtils.getVisibleTextFromElement(lblSchoolSelectionPageTitle, driver);
			Log.message("Title for School Selection page: " + actualmessage);
		}
		return actualmessage;
	}

	/**
	 * To get the instruction for School Selection page
	 * 
	 * @param expectedmessage
	 * @param screenShot
	 * @return
	 */
	public String getSchoolSelectionInstructionForTeacher() {
		Log.event("Getting the instruction in School Selection page");
		String actualmessage = "";
		if (RealizeUtils.waitForElement(driver, lblSchoolSelectionInstruction, 10)) {
			actualmessage = RealizeUtils.getVisibleTextFromElement(lblSchoolSelectionInstruction, driver);
			Log.message("Instruction for School Selection page: " + actualmessage);
		}
		return actualmessage;
	}

	/**
	 * To get the text value in search dropdown for course
	 * 
	 * @param selectedDropdownText - if it is true, selected dropdown value will
	 *                             return otherwise it will return default dropdown
	 *                             text
	 * @return
	 */
	public String getTextInSearchDropdownForTeacher(boolean... selectedDropdownText) {
		Log.event("Getting the text value in search dropdown for Teacher");
		String actualmessage = "";
		if (RealizeUtils.waitForElement(driver, txtSearchSchoolForTeacher, 10)) {
			actualmessage = txtSearchSchoolForTeacher.getAttribute("placeholder");
			Log.message(actualmessage + " displayed in search dropdown for course");
		}
		return actualmessage;
	}

	/**
	 * To get external CacheKey from the url
	 * 
	 * @return - externalCacheKey
	 */
	public String getExternalCacheKeyFromUrl() {
		String cacheKey = null;
		Pattern idPattern = Pattern.compile("externalCacheKey=(\\w*)");
		Matcher idMatch = idPattern.matcher(driver.getCurrentUrl().trim());
		if (idMatch.find()) {
			cacheKey = idMatch.group(1).trim();
		}
		return cacheKey;
	}
	 /**
     * To get school address for given school in school search dropdown
     * 
     * @param schoolName
     * @return school name with address
     */
    public HashMap<String, String> getGivenSchoolAddressInSearchDropdownForTeacher(List<String> schoolDisplayName){
		Log.event("Getting the school address for " + schoolDisplayName + " in search dropdown for Teacher");
		HashMap<String, String> hmContentWithAddress = new HashMap<String, String>();
		String address = "";
		List<WebElement> lstSchoolNameDropdownOptions = ShadowDOMUtils.findElements("li a.menu-item__link", driver);
		for(String schoolName : schoolDisplayName){
			for (WebElement element : lstSchoolNameDropdownOptions) {
				if(element.getText().contains(schoolName)){
					address = element.getText().replace(schoolName, "").replace("\n", "").trim();
					hmContentWithAddress.put(schoolName, address);
				}
			}
			Log.message("'" + address + "' is displayed for school '" + schoolName + "' in search dropdown for Teacher");
		}
		return hmContentWithAddress;
	}
    
	/**
	 * To verify 'Something went wrong' error message is display or not
	 * @return
	 */
	public boolean verifySchoolSelectionError() {
		Log.event("Verifying School Selection Error message");
		boolean errorFlag = false;
		try {
			RealizeUtils.waitForElement(driver, lblErrorMsg, 30);
			if (RealizeUtils.verifyWebElementTextContains(lblErrorMsg, OOPS_ERROR_MESSAGE)) {
				errorFlag = true;
				Log.message("Verified: School Selection error message displayed as <b>'" + OOPS_ERROR_MESSAGE + "'</b>");
			} else {
				throw new Exception("School Selection Error Message is not displayed");
			}
		} catch (Exception e) {
			Log.message("Error in verifying School Selection error message. Exception: " + e.getMessage());
		}
		return errorFlag;
	}
	
	/**
	 * To verify no active school Error Message
	 * 
	 * @param expectedMessage
	 * @return - boolean
	 */
	public boolean verifyNoActiveSchoolDisplayedError(String expectedMessage) {
		Log.event("Verifying no active school error message");
		boolean errorMessageFlag = false;

		try {
			if (RealizeUtils.waitForElement(driver, sadFaceImage, 10)) {
				if (sadFaceImage.isDisplayed()&&(sadFaceImage.getCssValue("text-align").contains("center"))) {
					Log.message("Verified: The sad face is displayed in centre of Not for Display district error page");
				} else {
					Log.message(RealizeUtils.FAIL_HTML_BEGIN + "Failed to verify the Sad Face"
							+ RealizeUtils.FAIL_HTML_END);
				}
			}
			
			RealizeUtils.waitForElementVisible(driver, txtErrorheader, 30);
			WebElement btnSchoolLink = ShadowDOMUtils.findElement(linkNoSchoolLocator, driver);
			String actualmessage = RealizeUtils.getVisibleTextFromElement(txtErrorheader, driver) + " " + RealizeUtils.getVisibleTextFromElement(txtErrorMessage, driver) + " " 
			                       + RealizeUtils.getVisibleTextFromElement(btnSchoolLink, driver);
			if (actualmessage.equalsIgnoreCase(expectedMessage)) {
				errorMessageFlag = true;
            } else {
            	errorMessageFlag = false;
            }

		} catch (Exception e) {
			Log.message("Error in verifying no active school error message. Exception: " + e.getMessage());
		}
		return errorMessageFlag;
	}
	
	/**
	 * To click link for no school error message
	 */
	public void clickNoActiveSchoolLink() {
		Log.event("Clicking link to resolve no active school display error");
		WebElement btnSchoolLink = ShadowDOMUtils.findElement(linkNoSchoolLocator, driver);
		if (RealizeUtils.waitForElement(driver, btnSchoolLink)) {
			Set<String> winHandles = driver.getWindowHandles();
			btnSchoolLink.click();
			new WebDriverWait(driver, Duration.ofSeconds(30)).until(ExpectedConditions.numberOfWindowsToBe(winHandles.size()+1));
			RealizeUtils.switchToMostRecentWindow(winHandles, driver);
			Log.message("Clicked link to resolve no active school display error and navigated to a new page",driver);
		} else {
			Log.message("Unable to find link to resolve no active school display error");
		}
	}
	
	/**
	 * Verifying the teacher is redirected to new tab having the expected url
	 * 
	 * @return true if the new tab contains the expected url
	 */
	public boolean verifySetupHelpPageloadedinNewTab(String type) {
		boolean status = false;
		Log.event("Verifying new tab is opened with required page url");
		RealizeUtils.switchToNewWindow(driver);
		RealizeUtils.waitForPageLoad(driver);
		switch (type)  {
		case "User":
			if (driver.getCurrentUrl().equals(LEARNMORE_NEWPAGE_URL_FOR_USER)) {
				status = true;
				Log.message("Realize Help Page is Loaded ", driver, true);
			}
			break;
		case "Class":
			if (driver.getCurrentUrl().equals(LEARNMORE_NEWPAGE_URL_FOR_CLASS)) {
				status = true;
				Log.message("Realize Help Page is Loaded ", driver, true);
			}
			break;
		}
		return status;
	}
}
