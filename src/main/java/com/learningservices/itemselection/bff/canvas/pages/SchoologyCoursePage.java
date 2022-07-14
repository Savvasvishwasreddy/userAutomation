package com.learningservices.itemselection.bff.canvas.pages;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.LoadableComponent;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.learningservices.itemselection.bff.utils.RealizeUtils;
import com.learningservices.utils.ElementLayer;
import com.learningservices.utils.Log;

public class SchoologyCoursePage {
	
	private boolean isPageLoaded;
	public ElementLayer uielement;

	public static final String SCHOOLOGY_TIMEZONE = "America/New_York";
	private TimeZone UTCTimeZone = TimeZone.getTimeZone("UTC");
	private TimeZone schoologyTimeZone = TimeZone.getTimeZone(SCHOOLOGY_TIMEZONE);
	public static final DateTimeFormatter DUE_DATE_FORMAT = DateTimeFormatter.ofPattern("M/dd/yy");
	public static final DateTimeFormatter DUE_TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mma");
	public static final DateTimeFormatter ASSIGNMENT_DATETIME_FORMAT = DateTimeFormatter.ofPattern("M/dd/yy hh:mma");

	// XPath Constants
	private static final String toolNameXpath = "//*[@class='action-lti-app']/a[span[normalize-space(text())='%s']]";
	private static final String materialsXpath = ".//ancestor::tr[contains(@class, 'type-document')]";
	private static final String editOptionCSSSelector = "ul.action-links:not([style*='none']) .action-edit>a";
	private static final String deleteOptionCSSSelector = "ul.action-links:not([style*='none']) .action-delete>a";
	private static final String actionCSSSelector = "div.action-links-unfold";

	@FindBy(css = ".material-app-title")
	WebElement lnkToolName;

	@FindBy(id = "schoology-app-container")
	WebElement resourceSelectionIframe;

	@FindBy(id = "external-tool-iframe")
	WebElement iframeSchoologyTool;

	@FindBy(css = "div[class='popups-close']")
	WebElement closeIcon;

	// **** course Option ********//

	@FindBy(css = "div#s-course-settings div.action-links-unfold")
	WebElement lnkCourseOptions;

	@FindBy(css = "div#s-course-settings ul.action-links:not([style*='none'])")
	WebElement drpCourseOptionsMenu;

	@FindBy(css = "div#s-course-settings a.s-course-preview-members")
	WebElement lnkViewCourseAsOption;

	@FindBy(css = "div.preview-members-popup")
	WebElement viewCourseAsPopup;

	@FindBy(css = "div.preview-members-popup td.user-name")
	List<WebElement> lstUserNameInViewCourseAsPopup;

	@FindBy(css = "div.masquerade-box a.back")
	WebElement btnBackToCourse;

	// **** Left Side Menu ******* //

	@FindBy(xpath = "//div[@id='left-nav']//a[text()='Materials']")
	WebElement lnkMaterials;

	@FindBy(xpath = "//div[@id='left-nav']//a[text()='Gradebook']")
	WebElement lnkGradebook;

	@FindBy(xpath = "//div[@id='left-nav']//a[text()='Grades']")
	WebElement lnkGrades;

	@FindBy(xpath = "//div[@id='left-nav']//a[text()='Members']")
	WebElement lnkMembers;

	// **** Materials ********//

	@FindBy(css = "div[class='course-content-action-links']")
	WebElement lnkAddMaterials;

	@FindBy(css = "div.has-material-apps:not([style*='none'])")
	WebElement drpAddMaterials;

	@FindBy(css = "#course-profile-materials-folders")
	WebElement courseContent;

	@FindBy(css = "div.attachments-external-tool>a,div.item-title>a")
	List<WebElement> lstContentTitles;

	@FindBy(id = "s-course-materials-document-delete-form")
	WebElement deletePopup;

	@FindBy(css = "#s-course-materials-document-delete-form input.form-submit")
	WebElement btnDelete;

	@FindBy(css = "div.gradebook-course span.title>a")
	List<WebElement> lstAssignmentTitlesInGrades;

	// **** External Tool Popup ********//

	@FindBy(css = "form#s-external-tool-add-link-form")
	WebElement externalToolPopup;

	@FindBy(css = "form#s-external-tool-add-link-form input#edit-external-tool-grading-enabled")
	WebElement chkEnableGrading;

	@FindBy(css = "form#s-external-tool-add-link-form input#edit-external-tool-title")
	WebElement txtTitle;
	
	@FindBy(css = "form#s-external-tool-add-link-form input#edit-external-tool-grading-max-points")
	WebElement txtMaxPoints;

	@FindBy(css = "form#s-external-tool-add-link-form input.due-date")
	WebElement txtDueDate;

	@FindBy(css = "form#s-external-tool-add-link-form div.time-input input.form-text")
	WebElement txtDueTime;

	@FindBy(css = "form#s-external-tool-add-link-form input#edit-submit")
	WebElement btnSubmit;

	@FindBy(css = "form#s-external-tool-add-link-form a.cancel-btn")
	WebElement btnCancel;



}
