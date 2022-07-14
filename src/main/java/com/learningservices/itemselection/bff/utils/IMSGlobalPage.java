package com.learningservices.itemselection.bff.utils;

import java.io.File;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.*;
import org.testng.Assert;

import com.learningservices.utils.ElementLayer;
import com.learningservices.utils.Log;
import com.learningservices.utils.RestAssuredAPI;
import com.learningservices.utils.WebDriverFactory;

import io.restassured.response.Response;

public class IMSGlobalPage extends LoadableComponent<IMSGlobalPage> {

    private final WebDriver driver;
    private boolean isPageLoaded;
    public ElementLayer uielement;
    
    public static final String DELETE_ATTRIBUTE = "delete_json_attribute";
    public static final String EMPTY_JSONOBJECT_ATTRIBUTE = "empty_jsonObject_attribute";
    public static final SimpleDateFormat dateFormatIMS = new SimpleDateFormat("yyyy-MM-dd");
    public boolean create_course_with_only_title = false;

	private static PropertyReader configProperty = PropertyReader.getInstance();
    public static String PLATFORM_URL = "https://lti-ri.imsglobal.org/platforms/%s";
    public static String DEEPLINK_ENDPOINT = "/contexts/%s/deep_links";
    private static String MEMBERSHIP_ENDPOINT = "/contexts/%s/memberships";
    public static String ROSTER_ENDPOINT = "/resource_links/%s/rosters";
    public static String EDIT_RESOURCE_ENDPOINT = "/resource_links/%s/edit";
    public static final String ACCESSTOKEN_ENDPOINT = "/access_tokens";
    public static final String AUTHORIZATION_ENDPOINT = "/authorizations/new";

    private By jwksUrlXPath = By.xpath(".//p[@class=\"card-text\"][b[contains(text(),\"well-known/jwks URL\")]]");
    private static String AUTH_PARAMETER_LOCATOR = "p:nth-child(%s) > i";
    private By txtTitleXPath = By.xpath(".//p[@class=\"card-text\"][b[text()=\"Title:\"]]");
    private By txtNameXPath = By.xpath(".//p[@class=\"card-text\"][b[text()=\"Name:\"]]");
    private By txtUserXPath = By.xpath(".//p[@class=\"card-text\"][b[text()=\"User:\"]]");
    private By txtLabelXPath = By.xpath(".//p[@class=\"card-text\"][b[text()=\"Label:\"]]");
    private By txtLineItemXPath = By.xpath(".//p[@class=\"card-text\"][b[text()=\"LineItem:\"]]");
    private By btnDeleteXpath = By.xpath(".//a[text()='Delete']");
    private By btnOIDCLaunchXpath = By.xpath(".//a[text()='Launch Resource Link (OIDC)']");
    private By btnViewResourceLinkXpath = By.xpath(".//a[text()='View Resource Link']");

	// Role vocabularies for LTI-A standard
	public enum SystemRoles {
		Administrator("http://purl.imsglobal.org/vocab/lis/v2/system/person#Administrator"),
		None("http://purl.imsglobal.org/vocab/lis/v2/system/person#None");

		private String role;

		SystemRoles(String roleName) {
			this.role = roleName;
		}

		@Override
		public String toString() {
			return role;
		}
	}

	public enum InstitutionRoles {
		Instructor("http://purl.imsglobal.org/vocab/lis/v2/institution/person#Instructor"),
		Learner("http://purl.imsglobal.org/vocab/lis/v2/institution/person#Learner"),
		Mentor("http://purl.imsglobal.org/vocab/lis/v2/institution/person#Mentor"),
		Other("http://purl.imsglobal.org/vocab/lis/v2/institution/person#Other"),
		Staff("http://purl.imsglobal.org/vocab/lis/v2/institution/person#Staff"),
		Student("http://purl.imsglobal.org/vocab/lis/v2/institution/person#Student");

		private String role;

		InstitutionRoles(String roleName) {
			this.role = roleName;
		}

		@Override
		public String toString() {
			return role;
		}
	}

	public enum ContextRoles {
		Administrator("http://purl.imsglobal.org/vocab/lis/v2/membership#Administrator"),
		ContentDeveloper("http://purl.imsglobal.org/vocab/lis/v2/membership#ContentDeveloper"),
		Instructor("http://purl.imsglobal.org/vocab/lis/v2/membership#Instructor"),
		Learner("http://purl.imsglobal.org/vocab/lis/v2/membership#Learner"),
		TeachingAssistant("http://purl.imsglobal.org/vocab/lis/v2/membership/Instructor#TeachingAssistant"),
		Mentor("http://purl.imsglobal.org/vocab/lis/v2/membership#Mentor");

		private String role;

		ContextRoles(String roleName) {
			this.role = roleName;
		}

		@Override
		public String toString() {
			return role;
		}
	}

	public enum OtherRoles {
		Helper("http://purl.imsglobal.org/vocab/lis/v2/unknown/unknown#Helper"),
		Unknown("http://purl.imsglobal.org/vocab/lis/v2/uknownrole/unknown#Unknown"),
		ContentDeveloper("ContentDeveloper"),
		Instructor("Instructor"),
		Learner("Learner"),
		Empty("");
		
		private String role;

		OtherRoles(String roleName) {
			this.role = roleName;
		}

		@Override
		public String toString() {
			return role;
		}
	}

	public enum GradingProgress {
		FullyGraded("FullyGraded"),
		Pending("Pending"),
		PendingManual("PendingManual"),
		Failed("Failed"),
		NotReady("NotReady");

		private String progress;

		GradingProgress(String progress) {
			this.progress = progress;
		}

		@Override
		public String toString() {
			return progress;
		}
	}

	public enum ActivityProgress {
		Initialized("Initialized"),
		Started("Started"),
		InProgress("InProgress"),
		Submitted("Submitted"),
		Completed("Completed");

		private String progress;

		ActivityProgress(String progress) {
			this.progress = progress;
		}

		@Override
		public String toString() {
			return progress;
		}
	}

    @FindBy(xpath="//div[@class='container']//h2[text()='Tool Provider Certification']")
    WebElement txtToolProviderCertification;
    
    @FindBy(xpath="//div[@class='container']//h2[text()='Configure Tool Provider']")
    WebElement txtConfigureToolProvider;
    
    @FindBy(xpath="//h3[text()='Tool Consumer Gradebook']")
    WebElement txtToolConsumerGradeBook;
    
    @FindBy(css=".alert-info h3")
    WebElement txtReplaceResult;
    
    @FindBy(css = "input[value*='Go to Configuration']")
    List<WebElement> btnGoToConfiguration;
    
    @FindBy(css=".btn-primary[value*='Go to Results']")
    List<WebElement> btnGoToResults;
    
    @FindBy(css=".btn-primary[value*='Back to Tests']")
    List<WebElement> btnBackToTests;

    @FindBy(css = "#tab_info.active")
    WebElement tabInfoSelected;
    
    @FindBy(id="tab_connection")
    WebElement tabConnection;
    
    @FindBy(id="tab_launch")
    WebElement tabLaunch;

    @FindBy(id = "id_name")
    WebElement txtSoftware;

    @FindBy(id = "id_version")
    WebElement txtVersion;
    
    @FindBy(id="id_url")
    WebElement txtLaunchURL;
    
    @FindBy(id="id_key")
    WebElement txtConsumerKey;
    
    @FindBy(id="id_secret")
    WebElement txtConsumerSecret;

    @FindBy(css = "input[value='Save settings']")
    WebElement btnSaveSettings;
    
    @FindBy(css=".form-check-input")
    WebElement chkbxUseWindow;
    
    @FindBy(css=".btn.btn-primary[value*='Go to Tests']")
    List<WebElement> btnGoToTests;
    
    @FindBy(css=".btn-secondary[value*='Next section']")
    WebElement btnNextSection;
    
    @FindBy(css=".btn-info[value='First test']")
    WebElement btnFirstTest;
    
    @FindBy(css=".btn-info[value*='Next test']")
    WebElement btnNextTest;
    
    @FindBy(css=".btn-warning[title*='Launch as']")
    WebElement btnLaunchAs;
    
    @FindBy(id="id_rlid")
    WebElement selectResource;
    
    @FindBy(id="id_uid")
    WebElement selectUser;
    
    @FindBy(css=".text-center>h3")
    WebElement txtSectionSixOutcome;
    
    @FindBy(id="id_rlid")
    WebElement drplnkResource;
    
    @FindBy(css="#id_rlid>option")
    List<WebElement> lstResource;
    
    @FindBy(id="id_uid")
    WebElement drptxtUser;
    
    @FindBy(css="#id_uid>option")
    List<WebElement> lstUser;
    
    @FindBy(xpath="//button[text()='Launch']")
    WebElement btnLaunch;
    
    @FindBy(css="#id_testFrame")
    WebElement testFrame;
    
    @FindBy(xpath="//a[text()='View gradebook']")
    WebElement btnViewGradeBook;
    
    @FindBy(css="table[class*='table-bordered']>thead th")
    List<WebElement> lstTableHeader;
    
    @FindBy(xpath="//table[contains(@class,'table-bordered')]/tbody/tr/td/a")
    List<WebElement> lstTableResultsSection;
    
    @FindBy(id = "content-viewer")
    List<WebElement> pageContentViewer;
    
    @FindBy(css="#id_testFrame")
    WebElement frameSCO;
    
    @FindBy(xpath="//table[contains(@class,'table-bordered')]/tbody/tr/td[7]")
    List<WebElement> lstResourceId;
    
//*************Navigation**********************
    
    @FindBy(css="nav>ul.pagination")
    WebElement paginationNavbar;
    
    @FindBy(css="nav>ul.pagination>li.next:not(.disabled)")
    WebElement btnNext;
    
//*************LTI v1.3************************

    @FindBy(css="input[value='Post request']")
    WebElement btnPostRequest;
    
    @FindBy(css="a.btn-primary")
    WebElement btnSendRequest;
    
    @FindBy(xpath = "//a[text()='Launch Deep Link']")
    WebElement btnLaunchDeepLink;

    @FindBy(xpath = "//a[text()='Edit Platform']")
    WebElement btnEditPlatform;
    
    @FindBy(css="input[value='Perform Launch']")
    WebElement btnPerformLaunch;
    
    @FindBy(xpath="//a[text()=\"Platform Keys\"]")
    WebElement lnkPlatformKeys;

    @FindBy(css="pre")
    WebElement txtErrorResponse;
    
    @FindBy(css = "#state")
    WebElement csrfToken;
    
    @FindAll({
    	@FindBy(css="div.container>pre:nth-of-type(1)>code"),
    	@FindBy(css="div.row:nth-of-type(5) pre>code")
    })
    WebElement jsonBodyRequest;
    
    @FindAll({
    	@FindBy(css="div.container>pre:nth-of-type(2)>code"),
    	@FindBy(css="div.row:nth-of-type(4) pre>code")
    })
    WebElement txtJsonJWT;
    
    @FindBy(xpath="//p[b[contains(text(), \"Platform's private key\")]]")
    WebElement txtPlatformPrivateKey;

    @FindBy(xpath="//p[b[contains(text(), \"Platform's public key\")]]")
    WebElement txtPlatformPublicKey;

    @FindBy(css = "div.card")
    List<WebElement> lstResourceLink;
    
    @FindBy(css = "div.card")
    List<WebElement> lstCourseLink;
    
    @FindBy(css = "div.card")
    List<WebElement> lstLineItemLink;
    
    @FindBy(css = "div.card")
    List<WebElement> lstResultsLink;
    
    @FindBy(xpath = "//a[text()='Launch Resource Link (OIDC)']")
    WebElement btnLaunchResourceLink_OIDC;
    
    @FindBy(xpath = "//a[text()='Launch Resource Link']")
    WebElement btnLaunchResourceLink;
    
    @FindBy(xpath = "//a[text()='View Resource Link']")
    WebElement btnViewResourceLink;
    
    @FindBy(xpath = "//a[text()='Launch with New User']")
    WebElement btnLaunchWithNewUser;
    
    @FindBy(xpath = "//a[text()='Add Course']")
    WebElement btnAddCourse;

    @FindBy(xpath = "//a[text()='Add Resource Link']")
    WebElement btnAddResourceLink;

	@FindBy(css = "input#platform_audience")
	WebElement txtAudience;

	@FindBy(css = "input#platform_client_id")
	WebElement txtClientId;

	@FindBy(css = "input[value='Save']")
    WebElement btnSave;

//********** New Course Fields ******************
    
    @FindBy(css = "input#context_label")
    WebElement txtContextLabel;
    
    @FindBy(css = "input#context_title")
    WebElement txtContextTitle;
    
    @FindBy(css = "input#context_type_of_context")
    WebElement txtTypeOfContext;

//********* New Resource Fields *****************
    
    @FindBy(css = "div.items")
    WebElement drpResourceLinkContext;
    
    @FindBy(css = "div.option")
    List<WebElement> lstContextDropdownOptions;
    
    @FindBy(css = "input#resource_link_description")
    WebElement txtResourceLinkDescription;
    
    @FindBy(css = "input#resource_link_title")
    WebElement txtResourceLinkTitle;
    
    @FindBy(css = "input#resource_link_tool_link_url")
    WebElement txtToolLinkUrl;
    
    @FindBy(css = "input#resource_link_login_initiation_url")
    WebElement txtLoginInitiationUrl;
    
    @FindBy(css = "input#resource_link_role")
    WebElement txtResourceLinkRole;
    
    @FindBy(css = "input#resource_link_custom_claim_content")
    WebElement txtCustomClaimContent;

//********* New Line Item Fields *****************
    
    @FindBy(xpath = "//a[text()='Add Line Item']")
    WebElement btnAddLineItem;
    
    @FindBy(css = "input#line_item_resourceid")
    WebElement txtLineItemResourceId;
    
    @FindBy(css = "input#line_item_score_maximum")
    WebElement txtLineItemScoreMaximum;
    
    @FindBy(css = "input#line_item_label")
    WebElement txtLineItemLabel;

    @FindBy(css = "input#line_item_tag")
    WebElement txtLineItemTag;
    
    @FindBy(css = "input#line_item_start_date_time")
    WebElement txtLineItemStartDate;
    
    @FindBy(css = "input#line_item_end_date_time")
    WebElement txtLineItemEndDate;
    
    
    /**
     * 
     * Constructor class for IMS Global Page page Here we initializing the
     * driver for page factory objects. For ajax element waiting time has added
     * while initialization
     * 
     * @param driver
     * @param url
     */
    public IMSGlobalPage(WebDriver driver) {

        this.driver = driver;
        PageFactory.initElements(driver, this);
    }

    @Override
    protected void isLoaded() {

        if (!isPageLoaded) {
            Assert.fail();
        }

        if (isPageLoaded && !(driver.getCurrentUrl().toLowerCase().contains("//apps.imsglobal.org"))) {
            Log.fail("IMS Global Page did not open up. Site might be down.", driver);
        }
        uielement = new ElementLayer(driver);
    }

    @Override
    protected void load() {

        isPageLoaded = true;
        RealizeUtils.waitForRealizePageLoad(driver);
    }

}
