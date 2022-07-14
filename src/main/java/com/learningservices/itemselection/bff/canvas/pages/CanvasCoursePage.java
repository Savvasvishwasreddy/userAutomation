package com.learningservices.itemselection.bff.canvas.pages;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.LocalFileDetector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.LoadableComponent;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.learningservices.itemselection.bff.utils.PropertyReader;
import com.learningservices.itemselection.bff.utils.RealizeUtils;
import com.learningservices.utils.ElementLayer;
import com.learningservices.utils.Log;
import com.learningservices.utils.RestAssuredAPI;

public class CanvasCoursePage extends LoadableComponent<CanvasCoursePage> {

	public SideNavBar sideNav;
	public ElementLayer uielement;
	private boolean isPageLoaded;
	private boolean isLoadInNewTab = false;
	private final WebDriver driver;
	private static PropertyReader configProperty = PropertyReader.getInstance();
	private final String CANVAS_URL = configProperty.getProperty("canvas.savvas.base.url");
	public static final String CREATE_CUSTOM_LINK = "Click here to learn more.";
	public static final String CLASS_SETUP_HEADING = "Import Content from Savvas";
	public static final String CLASS_SETUP_ERROR_MESSAGE = "To set up a Realize class, import at least one content item from a Savvas program.";
	public static final String UNAUTHORIZED_ERROR_MESSAGE = "Oops! You are not authorized to access this page.";
	public static final String HOVER_ASSIGNMENT_SCORE_TEXT = "Click to test a different score";
	public static final String OOPS_MESSAGE = "Oops!Something went wrong.";
	public static final DateTimeFormatter ASSIGNMENT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	public static final DateTimeFormatter ASSIGNMENT_DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	public static final String LEARNMORE_NEW_PAGE_URL = 
			"https://sites.google.com/view/savvas-realize-lms-integration/faqs/how-i-add-a-product-to-my-class-so-i-can-add-custom-content";
	public static final String CANVAS_TIMEZONE = "America/Denver";
	private TimeZone canvasTimeZone = TimeZone.getTimeZone(CANVAS_TIMEZONE);
	private TimeZone UTCTimeZone = TimeZone.getTimeZone("UTC");

	public enum CanvasUserRole {
		Student("Student"),
		Teacher("Teacher"),
		TA("TA"),
		Designer("Designer"),
		Observer("Observer");

		private String role;

		CanvasUserRole(String role) {
			this.role = role;
		}

		@Override
		public String toString() {
			return role;
		}
	}

	public enum SubmissionType {
		NoSubmission("No Submission"), Online("Online"), OnPaper("On Paper"), ExternalTool("External Tool");

		private String type;

		SubmissionType(String type) {
			this.type = type;
		}

		public String toString() {
			return type;
		}
	}

	public enum DateType {
		Due("Due"), Until("Until"), Available("Available from");

		private String date;

		DateType(String date) {
			this.date = date;
		}

		public String toString() {
			return date;
		}
	}
	
	public enum SubmissionAttempts {
		Unlimited("Unlimited"), Limited("Limited");

		private String attempts;

		SubmissionAttempts(String attempts) {
			this.attempts = attempts;
		}

		public String toString() {
			return attempts;
		}
	}

// XPATH STRING CONSTANTS
	private static final String toolNameXpath = "(.//a[normalize-space(text()) and normalize-space(.)=\"%s\"])";
	private static final String assignmentNameXpath = "(.//a[normalize-space(text())=\"%s\"])";
	private static final String externalToolWindowXpath = "./ancestor::div[contains(@class,'ui-dialog') and contains(@class,'ui-resizable')]";
	private static final String expandModuleLinkCSS = "div#context_modules>div.context_module span.ig-header-title.expand_module_link[title='%s']";
	private static final String closeButtonInDialogCSS = ".ui-dialog:not([style*='display: none']) .ui-dialog-titlebar-close";
	private static final String modulePublishIcon = ".ig-header-admin span.publish-icon";
	private static final String itemMoreIcon = ".ig-admin div i[class='icon-more']"; 
	private static final String lstOfStdAssignmentNamesColumn = "//a[text()='%s']";
	private static final String assignmentComments = "tr[id='comments_thread_%s'] > td span";
	private static final String commentsTimestamp = "tr[id='comments_thread_%s'] > td td";
	private static final String assignmentOutOfScoreInTeacherGrade = "//span[text()='%s']/ancestor::span[@class='Gradebook__ColumnHeaderDetail']//span[@class='assignment-points-possible']/span";
	private static final String latestAssignmentScoreFromGradebookHistory = "//table/tbody/tr/td[3][text()=\"%s\"]/../td[5]/span[normalize-space(text())=\"%s\"]/../../td[6]";
	private static String MEMBERSHIP_ENDPOINT = "/courses/%s/names_and_roles";
	public final String assignmentSaveSuccessMessage = "Success! The assignment has been saved.";
	private By editLinkCSS = By.cssSelector("ul.ui-menu>li.ui-menu-item>a.edit_item_link");
	private By editLinkForAssignment = By.cssSelector("a.edit_assignment");
	private By deleteLinkCSS = By.cssSelector("ul.ui-menu>li.ui-menu-item>a.delete_item_link");
	private By deleteAssignmentCSS = By.cssSelector("ul.ui-menu>li.ui-menu-item>a.delete_assignment");

	/*-------------------------------- All Courses Page --------------------------*/
	
	@FindBy(css = ".header>img")
	WebElement realizeLogo;
	
	@FindBy(css = ".custom-error-message .heading")
	WebElement lblClassSetupHeading;
	
	@FindBy(css = ".custom-error-message .sub-heading")
	WebElement lblClassSetupSubHeading;
	
	@FindBy(css = ".support_link")
	WebElement lnkSupport;
	
	@FindBy(xpath = "//h2[text()='All Courses']")
	WebElement lblAllCoursesHeader;

	@FindBy(css = "button#start_new_course")
	WebElement btnAddNewCourse;
	
	@FindBy(css = "form#new_course_form input#course_name")
	WebElement txtCourseName;
	
	@FindBy(css = "form#new_course_form select#course_license")
	WebElement drpCourseLicense;
	
	@FindBy(xpath = "//button/span[text()='Create course']")
	WebElement btnCreateCourse;
	
	/*-------------------------------- Course Home Page --------------------------*/

	@FindBy(css = "nav a.home")
	WebElement btnHome;

	@FindBy(css = ".content-navbar-inner.clearfix>h1")
	WebElement titleBar;

	@FindBy(id = "tool_content")
	WebElement scoFrame;

	@FindBy(css = "li[class='ic-flash-success']")
	WebElement txtAlertSuccessMessage;

	@FindBy(css = "span[aria-controls*='module']:not([style='display: none;']) span:not([class*='ellipsis'])")
	List<WebElement> lstHeaderNames;
	
	@FindBy(xpath = "//span[@id='ui-id-3']/following-sibling::button/span")
	WebElement closeIconOfExternalToolPopUp;

	@FindBy(css = "button.btn.btn-primary.add_module_link")
	WebElement btnAddNewModule;
	
	@FindBy(css = "div.ui-dialog:not([style*='none']) button.add_item_button.ui-button-text-only")
	WebElement btnAddItem;

	@FindBy(css = "form#add_context_module_form")
	WebElement mdlAddNewModule;

	@FindBy(css = "input#context_module_name")
	WebElement txtModuleName;

	@FindBy(css = "div.form-controls button.submit_button")
	WebElement btnAddModule;

	@FindBy(css = "button.btn-publish")
	WebElement btnPublishCourse;
	
	@FindBy(css = "button.btn-published")
	WebElement btnPublishedCourse;
	
	@FindBy(css = "div#oopsMessage")
	WebElement oopsMessage;
	
	@FindBy(css = "div[data-e2e-id='error-code-message'] > span:nth-child(1)")
	WebElement txtErrorCode;
	
	@FindBy(css = "div[data-e2e-id='error-code-message'] > span:nth-child(2)")
	WebElement txtErrorMessage;
	
	@FindBy(css = "div[data-e2e-id='error-technical-message'] > span")
	WebElement txtTechnicalErrorMessage;
	
	@FindBy(css = "div#content")
	WebElement courseContent;
	
	@FindBy(css = "form#edit_item_form input#content_tag_url")
	WebElement txtContentUrlInEditForm;
	
	@FindBy(css = "button.cancel_button>span")
	WebElement btnCancelInEditForm;
	
	@FindBy(css = "button.button_type_submit>span")
	WebElement btnUpdateInEditForm;
	
	@FindBy(css = "#easy_student_view")
	WebElement btnStudentView;
	
	@FindBy(css = ".reset_test_student")
	WebElement btnResetStudent;
	
	@FindBy(css = "a[class='Button leave_student_view']")
	WebElement btnLeaveStudentView;
	
	@FindBy(css = ".ig-row")
	 List<WebElement> lstModuleResources;
	
	/*-------------------------------- Course Assignments Page --------------------------*/
	
	@FindBy(css = "nav a.assignments")
	WebElement lnkAssignment;
	
	@FindBy(css = ".new_assignment")
	WebElement btnAddNewAssignment;

	@FindBy(css = "div#content")
	WebElement assignmentContent;
	
	@FindBy(id = "assignment_name")
	WebElement txtAssignmentName;

	@FindBy(css = "input.DueDateInput")
	WebElement txtDueDate;

	@FindBy(css = "div.from input.date_field")
	WebElement txtFromDate;
	
	@FindBy(css = "div.to input.date_field")
	WebElement txtToDate;

	@FindBy(css = "button.save_and_publish")
	WebElement btnSaveAndPublish;
	
	@FindBy(css = "button[type='submit']")
	WebElement btnSaveInAssignmentEditPage;

	@FindBy(css = "div a.button-sidebar-wide > i")
	WebElement btnEditAssignment;

	@FindBy(css = "select#assignment_submission_type")
	WebElement drpSubmissionType;
	
	@FindBy(css = "input#assignment_points_possible")
	WebElement txtPoints;
	
	@FindBy(css = "input#assignment_external_tool_tag_attributes_url")
	WebElement txtEnterExternalToolUrl;
	
	@FindBy(css = "button#assignment_external_tool_tag_attributes_url_find")
	WebElement btnFindExternalTool;
	
	@FindBy(css = "div.ui-dialog div#select_context_content_dialog")
	WebElement dlgConfigureExternalTool;

	@FindAll({ @FindBy(css = "div#select_context_content_dialog:not([style*='none']) input#external_tool_create_url"),
			@FindBy(css = "input#assignment_external_tool_tag_attributes_url") })
	WebElement txtFindExternalToolUrl;

	@FindBy(css = "input#external_tool_create_title")
	WebElement txtPageNameImportModal;

	@FindBy(css = "div.ui-dialog:not([style*='none']) div.ui-dialog-buttonpane button.add_item_button")
	WebElement btnSelect;

	@FindBy(css = "input#assignment_external_tool_tag_attributes_new_tab")
	WebElement chkLoadThisToolInNewTab;

	@FindBy(css = "div#assignment_online_submission_types input[type='checkbox']")
	List<WebElement> lstSubmissionTypesCheckbox;

	@FindBy(css = "li a[href*='/assignments/'][class*='title']")
	List<WebElement> lstAssignmentTitles;
	
	@FindBy(className = "save-rrs__button")
	WebElement btnSave;
	
	@FindBy(css = "div[class*='alert__message']")
	WebElement successMsgInAssignmentViewer;
	
	@FindBy(css = closeButtonInDialogCSS)
	List<WebElement> lstcloseIcon;
	
	@FindBy(css = "div.ui-dialog:not([style*='display: none']) div.create_assignment_dialog")
	WebElement dlgEditAssignment;
	
	@FindBy(css = "div.ui-dialog:not([style*='display: none']) form[data-view='edit-assignment'] button.more_options")
	WebElement btnMoreOption;
	
	@FindBy(css = "div#allowed-attempts-target select")
	WebElement drpSubmissionAttempts;
	

	/*----------------------------------Canvas Import Content Page --------------------------*/

	@FindBy(css = "a[class*='import_content']")
	WebElement lnkImportContent;

	@FindBy(css = "#chooseMigrationConverter")
	WebElement drpContentType;

	@FindBy(css = "#migrationFileUpload")
	WebElement txtSourceFile;

	@FindBy(css = "input[name='selective_import']")
	List<WebElement> lstContent;

	@FindBy(css = "#submitMigration")
	WebElement btnImport;

	@FindBy(css = "#progress")
	WebElement lstJobs;

	@FindBy(css = "#progress ul li:nth-child(1) div:nth-child(4) span")
	WebElement currentJobStatus;

	@FindBy(css = "span[class='label label-warning']")
	WebElement jobStatusCompleted;
	
	@FindBy(css = ".icon-unpublish")
	WebElement iconPublish;

	/*-------------------------------- Grades Page --------------------------*/

	@FindBy(css = "nav a.grades")
	WebElement lnkGrades;

	@FindBy(css = "div#content")
	WebElement gradeContent;

	@FindBy(css = "div.slick-cell.student>div.student-name>a")
	List<WebElement> lstStudentNamesInGradeBook;
	
	@FindBy(css = "div.slick-header-column.assignment span.assignment-name")
	List<WebElement> lstAssignmentNamesColumn;
	
	@FindBy(css = "div.container_1 div.slick-viewport div.slick-row")
	List<WebElement> lstStudentsGradeRow;
	
	@FindBy(xpath = "//div[@class='gradebook-menus']//button")
	WebElement btnGradebook;
	
	@FindBy(xpath = "//span[text()='Gradebook History…']")
	WebElement btnGradebookHistory;
	
	@FindBy(css = ".StudentContextTray-QuickLinks__Link")
	WebElement btnStudentTrayGrade;
	
	/*-------------------------------- Modules Page --------------------------*/
	
	@FindBy(css = "form#tool_form div.load_tab button:not([disabled])")
	WebElement btnLoadModuleInNewWindow;
	
	@FindBy(css = "input#external_tool_create_new_tab")
	WebElement chkLoadInNewTab;
	
	/*--------------------------------Course Settings Page --------------------------*/

	@FindBy(css = ".settings")
	WebElement lnkSettings;

	@FindBy(css = "a.modules")
	WebElement lnkModules;

	@FindBy(css = ".people")
	WebElement lnkPeople;
	
	@FindBy(xpath = "//span[text()='Modules']")
	WebElement btnModules;

	@FindBy(css = "div.todo-list.Sidebar__TodoListContainer")
	WebElement divToDoList;

	@FindBy(css = "a.submit_assignment_link")
	WebElement btnSubmitAssignment;

	@FindBy(css = "iframe.tool_launch")
	WebElement iframeCanvasTool;

	@FindBy(css = "div.new-window a")
	WebElement btnOpenInNewWindow;

	@FindBy(css = "li[title='Click to insert a link to this item.'] > a")
	List<WebElement> lstModulesToAdd;

	@FindBy(css = ".add_module_item_link")
	List<WebElement> addContentBtn;

	@FindBy(css = ".context_module")
	List<WebElement> lstModules;

	@FindBy(css = "#add_module_item_select")
	WebElement addModuleItemSelect;
	
	@FindBy(xpath = "(.//*[normalize-space(text()) and normalize-space(.)='Cancel'])[5]/following::span[1]")
	WebElement addItemBtn;

	@FindBy(css = ".resource_selection")
	List<WebElement> lstresources;

	@FindBy(css = "#tab-tools-link")
	WebElement tabApp;

	@FindBy(css = "div.externalApps_buttons_container a.view_tools_link.pull-right")
	WebElement btnViewAppConfigurations;

	@FindBy(css = "form.ConfigurationForm")
	WebElement mdlConfigurationForm;
	
	@FindBy(css = ".collectionViewItems tr i.icon-settings")
	WebElement trCertUrl;

	@FindBy(css = "a.icon-edit")
	WebElement iconEdit;
	
	@FindBy(css = "#right-side a>i.icon-trash")
	WebElement lnkDeleteCourse;

	@FindBy(css = ".form-actions button.btn-danger")
	WebElement btnConfirmDeleteCourse;
	
	@FindBy(css = "input[placeholder='Consumer Key']")
	WebElement txtConsumerKey;

	@FindBy(css = "input[placeholder='[Unchanged]'],input[placeholder='Shared Secret']")
	WebElement txtSharedSecretKey;

	@FindBy(id = "submitExternalAppBtn")
	WebElement btnSumbit;

	@FindBy(css = "a[aria-label='Add App']")
	WebElement btnAddApp;

	@FindBy(css = "input[placeholder='Name']")
	WebElement txtAddAppName;

	@FindBy(css = "input[placeholder='Launch URL']")
	WebElement txtLaunchURL;

	@FindBy(id = "context_external_tools_select")
	WebElement externalToolItems;

	@FindBy(css = "div.ui-dialog.ui-resizable:not([style*='display: none']) div#resource_selection_dialog")
	WebElement resourceSelectionDialog;

	@FindBy(id = "resource_selection_iframe")
	WebElement resourceSelectionIframe;

	@FindBy(css = "tbody[data-automation=\"courses list\"] a[href*=\"setting\"]")
	WebElement courseSettingIcon;

	@FindBy(css = ".context_module div:not([id=\"\"]) h2")
	List<WebElement> lstModuleName;
	
	/*--------------------------------Course People Page --------------------------*/

	@FindBy(css = "a#addUsers")
	WebElement btnAddPeople;
	
	@FindBy(css = "input[data-view*='inputFilter']")
	WebElement txtSearchPeople;
	
	@FindBy(css = ".admin-links a .icon-more")
	WebElement lnkMoreOptions;
	
	@FindBy(css = "ul li[class*='ui-menu-item'] a[data-event*='removeFromCourse']")
	WebElement selectRemoveFromCourseOption;
	
	@FindBy(css = "a[class='al-trigger btn']")
	WebElement btnToolShortcutInModule;
	
	@FindBy(css = "button[class*='al-trigger btn']")
	WebElement btnToolShortcutInAssignment;
	
	@FindBy(css = "a[class='menu_tray_tool_link ui-corner-all']")
	List<WebElement> lstToolsInModule;
	
	@FindBy(css = "a[class='ui-corner-all']")
	List<WebElement> lstToolsInAssignment;
	
	@FindBy(css = "div[class*='item-group-condensed context_module     editable_context_module context_module_hover']")
	WebElement moduleSection;
	
	@FindBy(css = "a[class*='ig-title']")
	List<WebElement> lstAssignments;
		
	
	/**
	 * 
	 * Constructor class for Login page Here we initializing the driver for page
	 * factory objects. For Ajax element waiting time has added while initialization
	 * 
	 * @param driver - WebDriver
	 */
	public CanvasCoursePage(WebDriver driver) {
		this.driver = driver;
		PageFactory.initElements(driver, this);
	}

	@Override
	protected void isLoaded() {

		if (!isPageLoaded) {
			Assert.fail();
		}

		if (isPageLoaded && !RealizeUtils.waitForElement(driver, btnHome, 2)
				&& !RealizeUtils.waitForElement(driver, lblAllCoursesHeader, 2)) {
			Log.fail("Canvas Courses page did not open up after 20 sec.", driver);
		}

		sideNav = new SideNavBar(driver);
		uielement = new ElementLayer(driver);
	}

	@Override
	protected void load() {
		isPageLoaded = true;
		RealizeUtils.waitForRealizePageLoad(driver);
	}
	
	/**
	 * To click Add Module Item button
	 * 
	 * @param screenShot to capture screenShot
	 * @param take       context module name
	 * 
	 */
	public void clickAddContentToModule(String contextModuleName, boolean screenShot) throws Exception {
		boolean isFound = false;
		Log.event("Clicking on 'Add Module Item' link");
		RealizeUtils.waitForPageLoad(driver, 60);
		for (WebElement moduleName : lstModules) {
			if (moduleName.getText().trim().contains(contextModuleName)) {
				isFound = true;
				int indexOfModule = lstModules.indexOf(moduleName);
				RealizeUtils.scrollIntoView(driver, moduleName);
				((JavascriptExecutor) driver).executeScript("arguments[0].click();", addContentBtn.get(indexOfModule));
				Log.message("Clicked on 'Add Module Item' link", driver, screenShot);
				break;
			}
		}

		if (!isFound)
			throw new Exception(contextModuleName + " is not found in module page");
	}

	
	/**
	 * Select External Tool content type from drop down list
	 * 
	 * @param screenShot to capture screenShot
	 * 
	 */
	public void selectExternalToolResource(boolean screenShot) {
		Log.event("Selecting 'External Tool' content type from dropdown list");
		RealizeUtils.waitForElement(driver, addModuleItemSelect);
		Select lstContentType = new Select(addModuleItemSelect);
		lstContentType.selectByValue("context_external_tool");
		Log.message("Selected 'External Tool from dropdown", driver, screenShot);
	}
	
	/**
	 * Click given external tool name in Add Item to LTIA module dialog
	 * 
	 * @param toolName - LTI-A external tool name
	 * @param screenShot - to capture screenShot
	 * @throws Exception 
	 */
	public void clickGivenExternalTool(String toolName, boolean screenShot) throws Exception {
		try {
			Log.event("Clicking " + toolName + " in Add Item to LTIA module dialog");
			RealizeUtils.waitForElement(driver, externalToolItems, 60);
			WebElement toolLink = externalToolItems.findElement(By.xpath(String.format(toolNameXpath, toolName)));
			RealizeUtils.waitForElementToBeClickable(toolLink, driver, 20);
			RealizeUtils.scrollElementIntoMiddle(driver, toolLink);
			toolLink.click();
			RealizeUtils.waitForElement(driver, resourceSelectionDialog, 30);
			Log.message("Clicked '" + toolName + "' in Add Item to LTIA module dialog", driver, screenShot);
		} catch (NoSuchElementException e) {
			throw new Exception("'" + toolName + "' not found in Adding Item to LTIA module dialog");
		} catch (Exception err) {
			Log.event("Error in clicking given tool name in Canvas application " + err.getMessage());
		}
	}
	/**
	 * To adjust the window size as per the given size
	 * @param window - Window Element
	 * @param top - top position in pixel
	 * @param left - left position in pixel
	 * @param width - window width in pixel
	 * @param height - window height in pixel
	 */
	private void adjustWindowSize(WebElement window, int top, int left, int width, int height) {
		String actualStyle = window.getAttribute("style");
		String[] styles = actualStyle.split("; ");
		for (int index = 0; index < styles.length; index++) {
			if (top > 0 && styles[index].startsWith("top")) {
				styles[index] = String.format("top: %dpx", top);
			} else if (left > 0 && styles[index].startsWith("left")) {
				styles[index] = String.format("left: %dpx", left);
			} else if (width > 0 && styles[index].startsWith("width")) {
				styles[index] = String.format("width: %dpx", width);
			} else if (height > 0 && styles[index].startsWith("height")) {
				styles[index] = String.format("height: %dpx", height);
			}
		}
		JavascriptExecutor jse = (JavascriptExecutor)driver;
		String expectedStyle = String.join("; ", styles);
		jse.executeScript("arguments[0].setAttribute('style', arguments[1])", window, expectedStyle);
	}
	
	/**
	 * To maximize the canvas dialog fit to Screen size
	 */
	private void fitToScreen() {
		WebElement outerDialog = resourceSelectionDialog.findElement(By.xpath(externalToolWindowXpath));
		JavascriptExecutor js = (JavascriptExecutor)driver;
		Long viewportWidth = (Long)js.executeScript("return window.innerWidth;");
		Long viewportHeight = (Long)js.executeScript("return window.innerHeight;");
		adjustWindowSize(outerDialog, 10, 10, (int)(viewportWidth - 50), (int)(viewportHeight - 50));
		adjustWindowSize(resourceSelectionDialog, -1, -1, (int)(viewportWidth - 55), (int)(viewportHeight - 55));
		if (!((RemoteWebDriver) driver).getCapabilities().getBrowserName().matches(".*Edge.*")) {
			RealizeUtils.waitForElement(driver, resourceSelectionIframe);
		}
		adjustWindowSize(resourceSelectionIframe, -1, -1, (int)(viewportWidth - 60), (int)(viewportHeight*8/9));
	}
	
	/**
	 * Switch to resource selection IFrame in Canvas Page
	 */
	public void switchToResourceSelectionIframe() {
		try {
			switchToDefaultContent();
			RealizeUtils.waitForElement(driver, resourceSelectionIframe);
			driver.switchTo().frame(resourceSelectionIframe);
		} catch (Exception err) {
			if (err.getMessage().contains("cannot determine loading status")) {
				RealizeUtils.nap(30);
				switchToDefaultContent();
				RealizeUtils.waitForElement(driver, resourceSelectionIframe);
				driver.switchTo().frame(resourceSelectionIframe);
			} else {
				Log.event("Error in switching to resource selection frame, Error: " + err.getMessage());
				throw err;
			}
		}
	}
	/**
	 * To get the URL inside the iFrame
	 * 
	 * @return
	 */
	public String getIFrameURL() {
		String url = null;
		url = (String) ((JavascriptExecutor) driver).executeScript("return document.location.href");
		Log.event("URL in canvas iFrame: " + url);
		return url;
	}
	
	/**
	 * To wait until the canvas IFrame to load a page
	 */
	private void waitForCanvasIFrameToLoad() {
		int MAX_TRY = 10;
		int WAIT_TIME = 5; // In seconds
		switchToResourceSelectionIframe();
		while (MAX_TRY-- > 0 && getIFrameURL().startsWith(CANVAS_URL)) {
			RealizeUtils.nap(WAIT_TIME);
			switchToResourceSelectionIframe();
		}
	}
	
	/**
	 * Switch to default modal
	 */
	public void switchToDefaultContent() {
		driver.switchTo().defaultContent();
	}
	
	/**
	 * To click Ok button in alert box
	 */
	public void acceptAlert() {
		try {
			Alert alert = driver.switchTo().alert();
			Log.event("Alert Box is displayed");
			alert.accept();
			Log.message("Clicked 'Ok' button in the alert box");
		} catch (NoAlertPresentException e) {
			Log.event("No Alert box is displayed");
		}
	}
	
	/**
	 * To click on closing External Tool dialog box
	 * 
	 * @param screenShot to capture screenShot
	 * 
	 */
	public void clickCloseExternalTool(boolean screenShot) {
		Log.event("Clicking on X icon in External Tool dialog box");
		if (lstcloseIcon.size() > 0) {
			WebElement closeIcon = lstcloseIcon.get(lstcloseIcon.size() - 1);
			if (RealizeUtils.waitForElement(driver, closeIcon, 30)) {
				RealizeUtils.clickJS(driver, closeIcon);
				acceptAlert();
				Log.message("Clicked on X icon in External Tool dialog box", driver, screenShot);
			} else {
				Log.message("Close(X) button in External Tool dialog box is not visible", driver, screenShot);
			}
		} else {
			Log.message("Close(X) button in External Tool dialog box is not exists", driver, screenShot);
		}
	}
	
	
	
	/**
	 * Select a Tool to launch and redirect to Discover search page in Discovery tool
	 * 
	 * @param screenShot to capture screenShot
	 * @param toolName - to LTI-A tool name
	 * @return DiscoverPage - discover page object
	 * @throws Exception
	 * 
	 */
	public DiscoverPage launchExternalTool(String toolName, boolean screenShot) throws Exception {
		Log.event("Launching " + toolName + " from canvas application");
		RealizeUtils.nap(2);
		clickGivenExternalTool(toolName, screenShot);
		RealizeUtils.scrollInToPage(driver, "Top");
		fitToScreen();
		DiscoverPage discoverPage = new DiscoverPage(driver);
		discoverPage.setLoadFromLTIAIframe(true);
		try {
			waitForCanvasIFrameToLoad();
			discoverPage = new DiscoverPage(driver);
			discoverPage.setLoadFromLTIAIframe(true);
			discoverPage.switchToCanvasIframe();
			if (discoverPage.getIFrameURL().startsWith(CANVAS_URL)) {
				throw new Exception("Unable to switch to resource selection frame");
			}
			discoverPage.waitForBrowseProgramsPageToLoad();			
			discoverPage.clickBrowseAllContentButton(screenShot);
			discoverPage = new DiscoverPage(driver);
			discoverPage.setLoadFromLTIAIframe(true);
			discoverPage.waitForDiscoverPageToLoad();			
		} catch (Exception err) {
			if (err.getMessage().contains("Unable to switch to resource selection frame")) {
				Log.message(RealizeUtils.WARN_HTML_BEGIN + err.getMessage() + RealizeUtils.WARN_HTML_END, driver, true);
				Log.message("</br><i><u>Close and Launch External tool:</u></i>");
				switchToDefaultContent();
				clickCloseExternalTool(true);
				launchExternalTool(toolName, screenShot);
			} else if (err.getMessage().contains("cannot determine loading status")
					|| err.getMessage().contains("target frame detached")) {
				waitForCanvasIFrameToLoad();
				discoverPage = new DiscoverPage(driver);
				discoverPage.setLoadFromLTIAIframe(true);
				discoverPage.waitForBrowseProgramsPageToLoad();
				discoverPage.clickBrowseAllContentButton(screenShot);
				discoverPage = new DiscoverPage(driver);
				discoverPage.setLoadFromLTIAIframe(true);
				discoverPage.waitForDiscoverPageToLoad();				
			} else {
				throw new Exception("Error while loading discover page. Exception: " + err.getMessage());
			}
		}
		Log.message("Launched " + toolName, driver, screenShot);
		return discoverPage;
	}	
	
	/**
	 * To launch content
	 * 
	 * @param String     - content name
	 * @param screenShot to capture screenShot
	 * @throws Exception
	 * 
	 */
	public void launchContent(String name, boolean screenShot) throws Exception {
		Log.event("Launching the content: " + name);
		try {
			RealizeUtils.waitForElement(driver, courseContent);
			WebElement contentToBeClicked = courseContent.findElement(By.cssSelector("a[title='" + name + "']"));
			RealizeUtils.scrollIntoView(driver, contentToBeClicked);
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", contentToBeClicked);
			RealizeUtils.waitForPageLoad(driver);
			RealizeUtils.waitForElement(driver, iframeCanvasTool);
		} catch (NoSuchElementException exp) {
			throw new Exception("Unable to launch content: " + name + ". Error: " + exp.getMessage());
		} catch (Exception err) {
			Log.message("Error while launching canvas content. Exception: " + err.getMessage());
		}
		Log.message("Launched the content: " + name, driver, screenShot);
	}
	
}