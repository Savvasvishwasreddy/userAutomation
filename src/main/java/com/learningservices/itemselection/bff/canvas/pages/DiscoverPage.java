package com.learningservices.itemselection.bff.canvas.pages;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONObject;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindAll;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.LoadableComponent;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;

import com.learningservices.itemselection.bff.utils.PropertyReader;
import com.learningservices.itemselection.bff.utils.RealizeUtils;
import com.learningservices.utils.ElementLayer;
import com.learningservices.utils.Log;
import com.learningservices.utils.ShadowDOMUtils;
import com.learningservices.utils.StopWatch;

public class DiscoverPage extends LoadableComponent<DiscoverPage> {

	private final WebDriver driver;
	private String discoverUrl;
	private boolean isPageLoaded;
	public ElementLayer uielement;
	public Modals modals;

	// STRING CONSTANTS
	private boolean loadFromLTIAIFrame = false;
	private static final String MYPROGRAMS_LABEL = "My Programs";
	public static final String SORTBY_RELEVANCE = "Relevance";
	public static final String SORTBY_ASCENDING_ORDER = "Title (A-Z)";
	public static final String SORTBY_DESCENDING_ORDER = "Title (Z-A)";
	public static final String NO_ITEM_ADDED_LABEL = "No item added";
	public static final String ADDING_ITEM_LABEL = "%s item(s) added";
	public static final String JWT_SIGNATURE_ERROR_MESSAGE = "500 JWT signature does not match locally computed signature. JWT validity cannot be asserted and should not be trusted.";
	public static final String CPS_IO_ERROR_MESSAGE = "500 : I/O error on POST request for \"class-provisioning-service.nightly.savvasrealizedev.com/classprovisioning-service/v1/provision/classes\": Attempted read from closed stream.; nested exception is java.io.IOException: Attempted read from closed stream.";
	public static final String SIGNING_KEY_ERROR_MESSAGE = "Error in resolving signing key: key id is required in header";
	public static final String SIGNING_KEY_INVALIDKEYID_ERROR_MESSAGE = "Error in resolving signing key. key Id invalidkeyid specified in header does not occur in json web key sets";
	public static final String UNAUTHORIZED_ERROR_MESSAGE = "401 Oops! You are not authorized to access this page.";
	public static final String UNAUTHORIZED_STUDENTS_ERROR_MESSAGE = "Students may not access this page.";
	public static final String MISSING_FIELD_ERROR_MESSAGE = "null is not a valid LaunchRequestProviderType";
	public static String NO_ALGORITHM_ERROR_MESSAGE = "java.security.NoSuchAlgorithmException: %s KeyFactory not available";
	public static final String MISSING_ATTRIBUTE_ERROR_MESSAGE = "422 [{\"attributeError\":\"%s must be present\",\"attributeName\":\"%s\"}]";
	public static final String MISMATCH_ATTRIBUTE_ERROR_MESSAGE = "422 [{\"attributeError\":\"must match \\\"%s\\\"\",\"attributeName\":\"%s\"}]";
	public static final String BLANK_ATTRIBUTE_ERROR_MESSAGE = "422 [{\"attributeError\":\"%s should not be blank.\",\"attributeName\":\"%s\"}]";
	public static final String OAUTH_TOKEN_ERROR_MESSAGE = "404 - {\"error_description\":\"oAuth Token auth_scope GET Failed. Reason: access_token not found\",\"status\":404,\"error\":\"not_found\"}";
	private static final String PROGRAMS_DESCRIPTION_LABEL = "To discover content, select programs and then click OK. You can return to this page to add more programs. Once a program is added, it cannot be removed.";
	public static final String INVALID_KEY_FORMAT_ERROR_MESSAGE = "500 : java.security.spec.InvalidKeySpecException: java.security.InvalidKeyException: invalid key format";
	public static final String INVALID_EPOCH_TIME_ERROR_MESSAGE = "500 : java.lang.IllegalArgumentException: 'exp' value does not appear to be ISO-8601-formatted: %s";
	public static final String MALFORMED_JWT_ERROR_MESSAGE = "io.jsonwebtoken.MalformedJwtException: JWT strings must contain exactly 2 period characters. Found: 0";
	public static final String EXPIRED_JWT_ERROR_MESSAGE = "io.jsonwebtoken.ExpiredJwtException: JWT expired at %s. Current time: %s, a difference of %s milliseconds. Allowed clock skew: 0 milliseconds.";
	public static final String INVALID_CSRF_TOKEN_ERROR_MESSAGE = "%s CSRF token is not valid or expired";
	public static final String DATA_NOT_FOUND_ERROR_MESSAGE = "Launch data not found for %s";
	public static final String ZERO_STATE_CONTENT_MESSAGE = "We couldn't find anything for \"%s\". Try searching for other keywords or clearing some of your filters.";
	public static final String USER_AGREEMENT_LABEL = "User Agreement";
	public static final String PRIVACY_POLICY_LABEL = "Privacy Policy";
	public static final String CREDITS_LABEL = "Credits";	
	public static final String COPYRIGHT_INFORMATION_TEXT = "Copyright © 2021 Savvas Learning Company LLC or its affiliates. All Rights Reserved.";
	public static final String USER_AGREEMENT_LINK_URL = "https://www.savvasrealize.com/community/prelogin/userAgreement.html";
	public static final String PRIVACY_POLICY_LINK_URL = "https://www.savvasrealize.com/community/prelogin/privacy/corporate/privacy/learning-services-privacy-policy.html";
	public static final String CREDITS_LINK_URL = "https://media.pk12ls.com/curriculum/credits/index.html";
	public static final String OOPS_ERROR_MESSAGE = "Oops! Something went wrong";
	public static final String HANG_TIGHT_HEADING = "Hang tight!";
	public static final String HANG_TIGHT_ERROR_MESSAGE = "Your teacher has not activated your class with Savvas Realize.";
	public static final String HANG_TIGHT_ERROR_MESSAGE_REFERENCE = "Reference: ";
	public static final String NOT_FOR_DISPLAY_DISTRICT = "Oops! Your school district, \"%s,\" is inactive. Click here to learn more and resolve the issue.";
	public static final String LINK_NOT_FOR_DISPLAY_DISTRICT = "Click here to learn more and resolve the issue.";
	private static final String btnBrowseLocator ="#Browse";
	private static final String btnMylibraryLocator =".platform__navbar--itemName";
	private static final String btnSharedwithmeLocator ="div > button:nth-child(2)";
	public static final String ACTIVATE_CLASS_SUPPORT_LINK_MESSAGE = "Click here to learn how your teacher can activate your class.";
	public static final String SEARCH_CONTENT_ZERO_STATE_MESSAGE_ENGLISH1 = "We couldn't find anything for ";
	public static final String SEARCH_CONTENT_ZERO_STATE_MESSAGE_ENGLISH2 = "Try searching for other keywords or clearing some of your filters.";
	public static final String TXT_HEADER_ZERO_STATE_PAGE_ENGLISH = "Welcome to Your New Playlist";
	public static final String TXT_ZERO_STATE_PAGE_ENGLISH = "To edit Playlist, open Savvas Realize using the Launch button on the top menu bar.";
	public static final String CREATE_CUSTOM_MESSAGE = "To create content, open Savvas Realize using the Launch button on the top menu bar.";
	public static final String CREATE_CUSTOM_LINK = "Click here to learn more.";
	public static final String LEARNMORE_NEW_PAGE_URL = "https://sites.google.com/view/savvas-realize-lms-integration/faqs/how-to-create-custom-content-in-my-library";
	public static final String INVALID_EMAILID_ERROR = "{\"timestamp\":\"%s,\"\"path\":\"/classprovisioning-service/v1/provision/classes\",\"status\":400,\"error\":\"BAD_REQUEST\",\"exception\":\"org.springframework.web.client.HttpClientErrorException$BadRequest\",\"message\":\"400 Bad Request {\\\"error\\\":\\\"Email is invalid\\\",\\\"code\\\":\\\"WC_UP_002\\\"}\",\"application\":\"classprovisioning-service\",\"errorParams\":\"400 Bad Request {\\\"error\\\":\\\"Email is invalid\\\"=errorParamValue,\\\"code\\\":\\\"WC_UP_002\\\"}=errorParamValue\"}";
	private static final String btnImportInMyLibraryLocator = "[class*='import-link']";
	public static final String LANGUAGE_ENGLISH ="English";
	public static final String LANGUAGE_SPANISH ="Spanish";
	private static final String importCartLocatorMyLib = ".dropdown-trigger__label";
	private static final String importCartContentLocatorMyLib = "span.menu-item__label";
	private static final String btnImportLoc=".import-link.hydrated";

	// COLOR CONSTANTS
	private static final String SAVE_BUTTON_COLOR = "#02549c";
	public  static final String ADD_BUTTON_COLOR = "#026ecb";
	private static final String ADD_BUTTON_COLOR_MOUSEHOVER = "#02549c";
	private static final String VIEW_BUTTON_COLOR_MOUSEHOVER = "#000000";
	public static final String BLACK_COLOR = "#000000"; 
	public static final String BLUE_COLOR = "#006be0";
	public static final String LIGHTBLUE = "#0072ee";
	public static final String FACET_COLOR = "#000000";


	// CSS LOCATORS

	private static final String btnFirstNameLocator = "span.utility__item--userName"; 
	private static final String userIconLocator = "div.utility__item--iconContainer.row-center";
	private static final String btnMyProgramsLocator ="#my-programs";
	private static final String txtProgramsLabelDescriptionLocator ="p.program__subtitle";
	private static final String userInfoPopOverLocator ="ul.utility__item--menuContainer";
	private static final String btnUserAgreementLocator ="#user-agreements";
	private static final String btnPrivacyPolicyLocator ="#privacy-policy";
	private static final String btnCreditsLocator ="#credits";
	private static final String lblRevisionIdLocator =".utility-nav-item-footer-primary-text";
	private static final String lblCopyrightInformationLocator =".utility-nav-item-footer-secondary-text";
	private static final String importCartLocator ="div.dropdown";
	private static final String lstContentTitlesInCartLocator ="li.menu-item";
	private static final String importCartScrollBarLocator ="ul[role='list']";
	private static final String ContentTitlesInCartLocator ="span.menu-item__label";
	private static final String openCartActiveAreaLocator ="ul.menu--active";	
	private static final By checkboxLocator =  By.cssSelector("cel-checkbox.checkbox");
	private static final By contentCheckmarkLocator = By.cssSelector("ion-icon[name='checkmark-circle']");
	private static final By contentTitleListLocator = By.cssSelector("ion-card-title[class^='card-header__title']");
	private static final By programTitleListLocator = By.cssSelector(".program-row__text");
	private static final By programListLocator = By.cssSelector(".item-container__list");
	private static final By viewButtonLocator = By.cssSelector("ion-button.button__secondary");
	private static final By addButtonLocator = By.cssSelector("ion-button.button__primary");
	private static final By removeLinkLocator = By.cssSelector("ion-button.button__remove");	
	private static final By sortByOptionsLocator  = By.cssSelector("ion-item.sc-ion-select-popover");
	private static final By facetCheckBoxLocator = By.cssSelector("ion-checkbox.facetFilter__checkbox");
	private static final By programFacetValueLocator = By.cssSelector("ion-label.programFilter__optionLabel");
	private static final By facetValuesListLocator = By.cssSelector("ion-item.facetFilter__filterItem ion-label");
	private static final String infoQuickLinkLocator = ".menu-item a.menu-item__link span[aria-label='info']";
	private static final By subscriptionNameList = By.cssSelector("ion-card-subtitle > ul.card-header__program-nameslist li");
	private static final String txtSubscriptionNameList = "ion-card-subtitle > ul.card-header__program-nameslist li>div.card-header__program-name-text";
	private static final By subscriptionListWithComma = By.cssSelector("ion-card-subtitle > ul.card-header__program-nameslist");
	private static final String btnImportCartDownArrowLocator =".dropdown-trigger__caret_disabled";
	private static final By btnImportInTOCLocator =  By.cssSelector("button.import-link");
	private static final String btnImportLocator = ".import-button";
	private static final String imgRealizeLogoLocator =".realize-logo";
	private static final String btnUserNameLocator =".utility__item--userLabel";
	private static final String drpDownSortBy_browse ="img[alt='icon caret down']";
	private static final By subjectTitleListLocator = By.cssSelector("a.program-row_subject__thumbnail");
	private static final By favoriteIconSelectedLocator = By.cssSelector("i[class='icon-fav_book_selected']");
	private static final By favoriteIconUnselectedLocator = By.cssSelector(".icon-fav_book_not_selected");
	private static final By favoriteProgramName = By.xpath("//*[@class='icon-fav_book_selected']/parent::a/parent::div/a[@class='program-row__text']");
	private static final String lstTocItemsLocator = ".details h3 > a";
	private static final String userFirstNameLocator = "div.utility-nav-item-header-primary-text";
	private static final By btnImportInPlaylist = By.cssSelector("div content-row cel-button.import-link");

	// XPATH STRING CONSTANTS
	private static String singleTocItemLocatorMFE = "//div[contains(@class,'content-row__title')]//*[contains(text(),'%s')] | //div[contains(@class,'content-row__title')]/h3/a/span/following-sibling::text()[contains(.,'%s')]/..";
	private static String assignQuickLink = ".assign-link";
	private static String infoQuickLink = ".info-link";
	private static String addToPlaylistQuickLink = ".playlist-link";
	private static String customizeQuickLink = ".customize-link";
	private static String editQuickLink = ".edit-link";
	private static String versionQuickLink = ".version-link";
	private static String teacherResourceQuickLink = ".teacher-resources-link";
	private static String remediationQuickLink = ".remediation-link";
	private static String publishQuickLink = "a[data-e2e-id='publish-link']";
	private static final String btnLaunchRealizeLocator =".launch-realize-button";
	private static final String lblCartItemNumberLocator = ".dropdown-trigger__label";
	private static final String browseBtnLocator = ".platform__navbar--itemNameActive";
	private static final String libraryBtnLocator = ".platform__navbar--itemName";

	// XPATH STRING CONSTANTS
	private static final String sortByOptionsXpath = "//ion-label[contains(text(),'%s')]";
	private static final String facetTitleXpath = "//ion-list[contains(text(),'%s')]";
	private static final By linkUserAgreementXpath = By.xpath("//h1[contains(normalize-space(text()),'Terms of Use')]");
	private static final By linkPrivacyPolicyXpath = By.xpath("//h1[contains(normalize-space(text()),'Privacy Policy')]");
	private static final By linkCreditsXpath = By.xpath("//p[contains(text(),'Credits')]");
	private static final String browseSortByOptionsXpath = "//*/a/span[contains(text(),'%s')]";

	// shadowdom locator for topnav
	public static final By newTopnavBarCssSelector = By.cssSelector("[class*=platform-nav-bar]");
	public static final String lnkProgramsTab_SDM_Locator = "#browse,#program[role='tab']";
	public static final String lnkMyLibraryTab_SDM_Locator = ".platform__navbar--itemName";
	//shadow dom locator for my library
	private static final String lstContentsItemsLocator = ".contentRow__contentTitle ion-card-header,div>a[class='contentTitle']";
	private static final String contentDatelocator = " div.contentRow__dateInfo.ng-binding.ng-scope,div[class='contentRow__dateInfo']";
	private static final String paginationInMyContent = "ul.paginator"; 
	public static final String shareWithMeTabMfe="div[class='toggle-button'] >button[aria-pressed='false']";
	public static final String myContentTabMfe="div[class='toggle-button'] >button[aria-pressed='false']";
	private static final String contentEllipses = ".contentAction__ellipses,span[aria-label='icon_ellipsis']";
	public static final String lstContentItem ="ion-card-header[data-e2e-id='contentItemTitle'],div .contentRow__details";
	public static final String goBackCss="cel-icon[aria-label='icon go back']";
	private static final String paginationInShareWithMe = "ul.paginator";
	private static final By txtSharedAssessmentUserInfo = By.cssSelector("div.content-row__user span");
	private static final String STARTBUTTON = "button[ng-click*='startButton']";


	//******** Distance Learning ********//

	@FindBy(css = "#mat-slide-toggle-1-input")
	WebElement toggleDistanceLearning;

	@FindBy(css = "#mat-slide-toggle-2-input")
	WebElement toggleDistanceLearning2;

	@FindBy(css = "#mat-slide-toggle-3-input")
	WebElement toggleDistanceLearning3;

	@FindBy(css = ".filter-bar_toggle_container .mat-slide-toggle-label")
	WebElement fldDistanceLearning;

	@FindBy(css = ".mat-slide-toggle-content")
	WebElement lableDistanceLearningToggle;

	@FindBy(css = ".mat-slide-toggle-bar")
	WebElement btnDistanceLearningToggleSwitch;

	public static final String iconDistanceLearningLocator = ".distance-learning--icon,.content-icon--distanceLearning";
	public static final String teacherIconLinkList = ".ng-isolate-scope .teacher-icon > a,.content-icon > cel-icon";
	public static final String lblDistanceLearningEnglish = "Show Distance Learning Resources";
	public static final String lblDistanceLearningSpanish = "Mostrar Recursos de aprendizaje a distancia";
	public static final String DISTANCE_LEARNING_TOOLTIP_MESSAGE = "This item is recommended as a resource for distance learning.";
	//    private static final String DISTANCE_LEARNING_TOOLTIP_MESSAGE_SPANISH = "Este artículo se recomienda como recurso para el aprendizaje a distancia." ;


	// PROPERTY READER
	private static PropertyReader configProperty = PropertyReader.getInstance();
	// *********Discovery Page Header***************
	@FindBy(css = "div.nav-bar-container")
	WebElement divHeaderToolbar;

	@FindBy(css = "ion-title.header__title h1")
	WebElement txtHeaderTitle;

	@FindBy(css = "ion-button.header__user-btn")
	WebElement btnUserIcon;

	@FindBy(css = "ion-card-title[class^='card-header__title']")
	List<WebElement> lstContentTitles;

	@FindBy(css = "a.mat-tooltip-trigger.content-row__text")
	List<WebElement> lstContentTitlesInTOC;

	@FindBy(css = "li.item-container__list")
	List<WebElement> lstPrograms;

	@FindBy(css = "ion-button[class^='button button__secondary']")
	List<WebElement> lstViewBtns;

	@FindBy(css = "ion-button[class^='button button__primary']")
	List<WebElement> lstAddBtns;

	@FindBy(css = "h1.program_head")
	WebElement txtMyProgramsLabelTitle;

	@FindBy(css = "cel-checkbox.checkbox")
	List<WebElement> lstProgramCheckbox;

	@FindBy(css = ".item-container__list")
	List<WebElement> lstDisplayedPrograms;

	@FindBy(css = "li.item-container__list-disabled")
	List<WebElement> lstSelectedPrograms;

	@FindBy(css ="cel-button.button__active__toolbar")
	WebElement btnOK;

	@FindBy(css = "p[class='loading']")
	WebElement pageloading;

	@FindBy(css = "search-skeleton-loading:not([hidden])")
	WebElement searchLoading;

	@FindBy(css = "input.searchbar-input")
	WebElement txtSearchBar;

	@FindBy(css = "div > ion-title[class*='footer__title']")
	WebElement footerLabel; 

	@FindBy(css = "div>h1")
	WebElement lblLoginErrorHeader;

	@FindAll({
		@FindBy(css = "div[data-e2e-id='error-code-message']"),
		@FindBy(css = "body>pre")
	})
	WebElement lblLoginErrorMsg;

	@FindBy(css = "div.custom-error-message")
	WebElement loginErrorMessage;

	@FindBy(css = "html > body")
	WebElement lblLoginErrorMsgHeader;

	@FindBy(css = "div.default-error-message,div[data-e2e-id='error-heading'], div.error-message")
	WebElement lblLoginErrorHeading;

	@FindBy(css = "div[data-e2e-id='error-technical-message']>span, div#error-technical-message")
	WebElement hiddenErrorMessage;

	@FindBy(tagName = "pre")
	WebElement lblJSONErrorMsg;

	@FindBy(css = "div.custom-error-message .heading")
	WebElement lblHangTightHeading;

	@FindBy(css = "div.error-icon-wrapper > img")
	WebElement hangTightIcon;

	@FindBy(css = "div.error-icon-wrapper")
	WebElement sadFaceImage;

	@FindBy(css = "div.custom-error-message p.sub-heading")
	WebElement lblHangTightSubHeading;

	@FindBy(css = "div.custom-error-message :nth-child(3)")
	WebElement lblHangTightRefId;

	@FindBy(css = "div.custom-error-message p.sub-heading")
	WebElement lblHangTightReference ;

	@FindBy(css = ".support_link")
	WebElement lnkSupport;

	@FindBy(css = "button.searchbar-clear-button")
	WebElement btnClose;

	@FindBy(css = ".popover-viewport")
	WebElement userInfoPopOver;

	@FindBy(css = "ion-item.user-menu__item ion-label")
	List<WebElement> lstMenus; 

	@FindBy(css = "ion-label.sortby-label")
	WebElement lblSortBy;

	@FindBy(css = "ion-select.searchbar-dropdown__sortby")
	WebElement drpDownSortBy;

	@FindBy(css = "ion-item.sc-ion-select-popover")
	List<WebElement> lstSortByOptions;    

	@FindBy(css = "search-facet-list > ion-content.md.hydrated")
	WebElement facetFilterBox;

	@FindBy(css = "ion-text.facetFilter__title")
	WebElement lblFilters;

	@FindBy(css = "ion-button.facetFilter__clearAll")
	WebElement lnkClearAll;

	@FindBy(css = "ion-toggle.programFilter__toggle")
	List<WebElement> programToggleButtons;

	@FindAll({ @FindBy(id = "resource_selection_iframe"), @FindBy(id = "external-tool-iframe") })
	WebElement resourceSelectionIframe;

	@FindBy(css = "ion-text.browseContent__zeroStateMsg")
	WebElement zeroStateContentMessage;

	@FindBy(css = "ion-list.facetFilter__filterTitle")
	List<WebElement> lstFacetFilterSections;

	@FindBy(css = ".oops__message")
	WebElement oopsErrorMessage;

	@FindBy(css = ".error__message")
	WebElement errorMessage;

	@FindBy(css = "ion-text.user-menu__revision")
	WebElement lblRevisionId;

	@FindBy(css = "ion-text.user-menu__copyright")
	WebElement lblCopyrightInformation;

	@FindBy(css = "ion-label.user-menu__header__title")
	WebElement lblUserName;

	@FindBy(css = "span.user-menu__header__subtitle")
	WebElement lblUserRole;

	@FindBy(css = "ion-item[class*='testOnly_checkBoxItem'] > ion-checkbox")
	WebElement testsOnlyCheckbox;

	@FindBy(css = "ion-text.filter_selectedText")
	List<WebElement> lstSelectedFacetValuesUnderSearch;

	@FindBy(css = "ion-label.user-menu__header__title")
	WebElement txtTeacherName;

	@FindBy(css = ".icon-search.searchAllPrograms")
	WebElement browseAllContentButton;

	@FindBy(css = "div.browse__message")
	WebElement browseProgramsTitle;

	@FindBy(css = "div[class='content-row__title'] a[class*='content-row__text'], .details h3, .content " + lstTocItemsLocator)
	List<WebElement> lstTOC;

	@FindBy(css = ".teacherResource__title")
	List<WebElement> lstTeacherResourceContents;

	@FindBy(css = "[class*='lesson-menu__assign']")
	WebElement btnAssignAll;

	@FindBy(css = ".lesson-menu__playlist")
	WebElement btnAddAllToPlaylist;

	@FindBy(css = ".mat-expansion-panel-header-title")
	WebElement drpCreateContent;

	@FindBy(css = "[name='icon_rearrange'] + span")
	WebElement btnRearrange;

	@FindBy(css = "[name='icon_upload'] + span")
	WebElement btnMyContent;

	@FindBy(css = ".dropdown-select-options li")
	List<WebElement> lstSortByOptions_browse; 

	@FindBy(css = "a.program-row_subject__thumbnail")
	List<WebElement> lstSubjectTitles;

	@FindBy(css = "i[class='icon-fav_book_selected']")
	List<WebElement> lstFavouriteIcon;

	@FindBy(css = ".icon-fav_book_not_selected")
	List<WebElement> lstFavouriteSelectIcon;

	@FindBy(xpath = "//*[@class='icon-fav_book_selected']/parent::a/parent::div/a[@class='program-row__text']")
	List<WebElement> lstFavouriteSelectProgramName;

	@FindBy(xpath = "//span[text()='View Original']/parent::cel-button")
	WebElement btnViewOriginal;

	@FindBy(css = "[name='icon_customize'] + span")
	WebElement btnEdit;

	@FindBy(css = ".lesson-menu__info")
	WebElement btnLessonInfo;

	@FindBy(css = ".teacherResource__accContainer")
	WebElement btnTeacherResource;

	@FindBy(css = "*[data-e2e-id='teacherResourceTitle']")
	List<WebElement> lstTeacherResourcesTitle;

	@FindBy(css = "a[data-e2e-id='teacherResourceTitle']")
	List<WebElement> lstTeacherResourcesTitleMFE;

	@FindBy(css = "ul li a.program-row__text")
	List<WebElement> lnkProgramsName;

	@FindBy(css = "p.program__subtitle")
	WebElement txtMyProgramsSubHeader;

	@FindBy(css = ".program__container .program__setting")
	WebElement headerMyPrograms;

	@FindBy(css = "div.overlay")
	WebElement cartOutside;

	@FindBy(css = "a[class='title-bar_back_arrow']")
	WebElement backBtn;

	@FindBy(css = ".item-group-condensed .ig-title.title.item_link")
	List<WebElement> lstModuleResources;

	@FindBy(css = "a.mat-tooltip-trigger.content-row__text")
	List<WebElement> lstTopics;

	@FindBy(css = "ul li button.import-link")
	List<WebElement> lstImportBtn;

	@FindBy(css = "cel-button.hydrated")
	WebElement linkInErrorPage;
	/*--------------------------------Browse Program Page --------------------------*/

	@FindBy(css = ".program-row__text")
	List<WebElement> lstProgramsInBrowsePage;


	@FindBy(css = "a[data-e2e-id='teacherResourceTitle']")
	List<WebElement> lstContentTitlesInTeacherResource;
	/*--------------------------------Reading Spot --------------------------*/

	@FindBy(css = ".row.main__section_row")
	WebElement btnOpenNewWin;

	@FindBy(css = ".container.unAssignedContentItem.ng-binding.ng-scope")
	WebElement txtTurnitinAssign;

	@FindBy(css = "a.title-bar_back_arrow")
	WebElement btnBackArrowMFE;

	@FindBy(css = "span.dropdown-select-label")
	WebElement selected_SortByOption;

	@FindBy(css = ".program-dropdown .program-dropdown__button>a")
	WebElement drpProgramSelection;

	@FindBy(css = ".program-dropdown__selected-program")
	WebElement programDropDownSelectedProgram;

	@FindBy(css = ".dropdown-menu>.list_group .program-dropdown__content")
	List<WebElement> lstProgramsInDropdown;

	@FindBy(css = ".dropdown-menu .browserLink")
	List<WebElement> lstProgramsInDropdownWithBrowseAllContent;

	@FindBy(css=".program-dropdown__selected-program.dropdown-on-select")
	WebElement drpProgramSelectionOpen;

	@FindBy(css = ".title-bar_title span")
	WebElement txtContentHeader;

	@FindBy(className = "content-row_list_content")
	List<WebElement> lstTocListView;

	@FindBy(xpath = "//span[text()='Thumbnail view']/..")
	WebElement btnThumbnailView;

	@FindBy(className = "content-row_thumbnail_content")
	List<WebElement> lstTocThumbnailView;

	@FindBy(css = ".filter-bar_button.last")
	WebElement btnListView;

	/* My library elements*/

	@FindBy(css = "refine-by.myLibrary__refineBy--largeScreen>div>ul>li>cel-button>span,refine-by.sharedWithMe__refineBy--largeScreen>div>ul>li>cel-button>span")
	List<WebElement> lstRefineByfacetValues;

	@FindBy(css = lstContentsItemsLocator)
	List<WebElement> lstContent;

	@FindBy(css = "ion-searchbar[id='ionicSearchBar']:not([data-e2e-id='helpSearchBar'])>div>input,input[placeholder='Enter Keyword'],#cel-sb-0")
	WebElement searchBox;

	@FindBy(css = ".searchBar__button,form span[class='sc-cel-search-bar']")
	WebElement searchButton;

	@FindBy(css = ".searchbar-input-container >input,div form>input[class='sc-cel-search-bar']")
	List<WebElement> lstSearchBox;

	WebElement lblpaginationMfeLoc;

	@FindBy(css = "ul.paginator")
	WebElement pagination;

	@FindBy(css=".contentList__itemsCount,content-list .contentList__itemsCount,content-list >div[class^='contentList__itemsCount']")
	WebElement lblItemCount;

	@FindBy(css = "refine-by.myLibrary__refineBy--largeScreen>div>ul>li>cel-button>span.refineBy__selectedFacetValue,"
			+ "refine-by.sharedWithMe__refineBy--largeScreen>div ul>li>cel-button>span.refineBy__selectedFacetValue")
	WebElement selectedFacetUnderRefineByLabel;

	@FindBy(css = ".searchBar__resetbutton,.sc-cel-search-bar>cel-icon")
	WebElement btnSearchReset;

	@FindBy(css = ".searchBar__selectedFacet")
	WebElement selectedFacet;

	@FindBy(css="span[class='refineBy__selectedFacetValue']")
	WebElement selectedFacetRefineBy;

	@FindBy(css = ".searchBar__selectedFacet .icon-remove,cel-icon[class^='searchBar__facetRemoveIcon']")
	WebElement selectedFacetRemoveIcon;

	@FindBy(css="ion-card-header[data-e2e-id='contentItemTitle'],div .contentRow__details, div.content-row__title")
	List<WebElement> contentItem;

	@FindBy(css = ".backNavArrow.back-link > a")
	WebElement exitBtn;

	@FindBy(css = "div.createContent__userInfoLabel")
	WebElement createContentCustomMessage;

	@FindBy(css = "a.createContent__userInfoLink")
	WebElement createContentCustomHelpLink;

	@FindBy(css = "[data-e2e-id='contentItemTitle'] a.contentIonTitle, .contentRow__details__wrapper .contentRow__details a")
	List<WebElement> lstPlayListContent;

	@FindBy(css = "h2.playlistZeroState__subheaderLabel")
	WebElement playlistZeroStateHeader;

	@FindBy(css = "div.playlistZeroState__contentArea")
	WebElement playlistZeroMessage;

	@FindBy(css = ".version-link__icon.hydrated")//a.version-link.ng-star-inserted
	WebElement versionsBtn;

	@FindBy(css = "a.contentTitle, div.content-row__title")
	List<WebElement> lstContentTitlesInMyLibrary;

	@FindBy(css="img.media-thumbnail__image")
	List<WebElement> shareWithMeTabContentThumbnails;

	@FindBy(css="cel-icon[class='refineBy__removeSign hydrated']")
	WebElement shareWithMeRemoveFacet;

	@FindBy(css="span[class='refineBy__selectedFacetValue']")
	WebElement selectedFacetShareWithMe;

	@FindBy(css = "div content-row div.contentRow__details")
	List<WebElement> lstplaylistContents;

	@FindBy(css = "div content-row cel-button.import-link")
	List<WebElement> lstImpBtnPlaylistPage;

	@FindBy(css = "div .import-link") 
	List<WebElement> lstImportBtnMyLibPage;

	@FindBy(css = "div.import-side-container")
	WebElement importCartDropDownMyLibPage;

	@FindBy(css = "div[class*='av__exit'] > a, div[class*='backNavArrow'] > a, a[class*='header-exitLink']")
	WebElement btnExit;

	@FindBy(css = "div.title-bar_title")
	WebElement tocHeader; 

	@FindBy(css = "div .mat-tooltip-trigger.content-row__text")
	List<WebElement> lstContentTOCpage;

	@FindBy(css="div.content-row__title")
	List<WebElement> programNameLocator;

	@FindBy(css = "div#content-viewer-title>span")
	WebElement contentTitle;

	@FindBy(css="div.student_voice")
	WebElement customizeNoteLocator;

	public DiscoverPage(WebDriver driver) {
		this.driver = driver;
		this.discoverUrl = configProperty.getProperty("ltia.itemSelection.webapp.url");
		PageFactory.initElements(driver, this);
	}

	@Override
	protected void load() {
		if (!isPageLoaded) {
			Assert.fail();
		}

		if (isPageLoaded && !(driver.getCurrentUrl().toLowerCase().contains(discoverUrl))) {
			Log.fail("Discovery Page did not open up. Site might be down.", driver);
		}
	}

	@Override
	protected void isLoaded() throws Error {
		RealizeUtils.waitForItemSelectionPageLoad(driver, 120);
		if (!RealizeUtils.waitUntilElementDisappear(driver, searchLoading,10)) {
			isPageLoaded = RealizeUtils.waitUntilElementDisappear(driver, searchLoading,10);
		} else {
			if (driver.getCurrentUrl().toLowerCase().endsWith("program-select")) {
				isPageLoaded = RealizeUtils.waitForElement(driver, txtMyProgramsLabelTitle) && 
						RealizeUtils.waitForElement(driver, btnOK);
			} if (driver.getCurrentUrl().toLowerCase().endsWith("program")) {
				isPageLoaded = RealizeUtils.waitForElement(driver, divHeaderToolbar);
			} else {
				isPageLoaded = true;
			}
			Log.event("Discover page loaded successfully");
		}
		uielement = new ElementLayer(driver);
		modals = new Modals(driver);
	}

	//********************** Common *******************************//

	/**
	 * To set loadFromLTIAIFrame boolean to open inside Iframe
	 * 
	 * @param openInIframe
	 */
	public void setLoadFromLTIAIframe(boolean openInIframe) {
		loadFromLTIAIFrame = openInIframe;
	}

	/**
	 * Switch to default modal
	 */
	public void switchToDefaultContent() {
		RealizeUtils.nap(3);
		driver.switchTo().defaultContent();
	}

	/**
	 * Switch to resource selection Iframe in Canvas Page
	 */
	public void switchToCanvasIframe() {
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
	 * To wait for Browse Programs page to load in Canvas Application
	 */
	public void waitForBrowseProgramsPageToLoad() {
		if (!RealizeUtils.waitForElement(driver, divHeaderToolbar, 30)) {
			Log.event("Browse Programs page in Discovery tool modal is not loaded yet..");
			if (loadFromLTIAIFrame)
				switchToCanvasIframe();
			PageFactory.initElements(driver, this);
			RealizeUtils.waitForElement(driver, divHeaderToolbar, 60);
		}
		RealizeUtils.waitForElement(driver, browseAllContentButton, 30);
		uielement = new ElementLayer(driver);
		modals = new Modals(driver);
	}

	/**
	 * To wait for discover page to load in Canvas Application
	 */
	public void waitForDiscoverPageToLoad() {
		if (!RealizeUtils.waitForElement(driver, divHeaderToolbar, 30)) {
			Log.event("Discover page is not loaded yet..");
			if (loadFromLTIAIFrame)
				switchToCanvasIframe();
			PageFactory.initElements(driver, this);
			RealizeUtils.waitForElement(driver, divHeaderToolbar, 60);
		}
		if (!RealizeUtils.waitUntilElementDisappear(driver, searchLoading, 10)) {
			isPageLoaded = RealizeUtils.waitUntilElementDisappear(driver, searchLoading, 10);
		}
		RealizeUtils.waitForElement(driver, txtSearchBar);
		uielement = new ElementLayer(driver);
		modals = new Modals(driver);
	}



	/**
	 * To wait for Programs selection page to load in Canvas Application
	 */
	public void waitForProgramSelectionPageToLoad() {
		if (!RealizeUtils.waitForElement(driver, txtMyProgramsLabelTitle, 30)) {
			Log.event("Program selection page in Discovery tool modal is not loaded yet..");
			if (loadFromLTIAIFrame)
				switchToCanvasIframe();
			PageFactory.initElements(driver, this);
			RealizeUtils.waitForElement(driver, txtMyProgramsLabelTitle, 60);
		}
		uielement = new ElementLayer(driver);
		modals = new Modals(driver);
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
	 * To wait for search results to load in the discover page
	 * 	
	 * @param secondsToWait - time in seconds
	 */
	public void waitForSearchResultsToLoad(int... secondsToWait) {
		Log.event("Waiting for search results to load");
		try {
			if (!RealizeUtils.waitUntilElementDisappear(driver, searchLoading, 10)) {
				Log.event("Search page is not loaded, so waiting again");
				RealizeUtils.waitUntilElementDisappear(driver, searchLoading, 10);
			}
			if (secondsToWait.length > 0) {
				RealizeUtils.waitForLocatorToPresent(driver, contentTitleListLocator, secondsToWait[0]);
			} else {
				RealizeUtils.waitForLocatorToPresent(driver, contentTitleListLocator, 60);
			}
			if (lstContentTitles.size() > 0) {
				Log.event("Search results are loaded");
			} else {
				Log.message("No Search results found");
			}
		} catch (Exception err) {
			Log.event("Error in loading search results. Exception: " + err.getMessage());
		}
	}

	/**
	 * To click 'Browse All Content' button
	 * @param  screenshot
	 * @throws Exception if 'Browse All Content' button not found
	 */
	public void clickBrowseAllContentButton(boolean screenshot) throws Exception {
		Log.event("Clicking 'Browse All Content' button in the discover page");
		if (RealizeUtils.waitForElement(driver, browseAllContentButton, 10)) {
			browseAllContentButton.click();
			RealizeUtils.waitUntilElementDisappear(driver, browseProgramsTitle, 10);
			waitForSearchResultsToLoad();
			Log.message("Clicked 'Browse All Content' button in the discover page", driver, screenshot);
		} else {
			throw new Exception("'Browse All Content' button not found");
		}
	}
	
	/**
	 * To select the programs in the program selection page
	 * 
	 * @param lstProgramNames - List of programs to select *
	 * @throws Exception if required program not found
	 */
	public void selectProgram(List<String> lstProgramNames) throws Exception {
		Log.event("Selecting the programs"); 
		RealizeUtils.waitUntilElementDisappear(driver, pageloading, 10);
		if (RealizeUtils.waitForLocatorToPresent(driver, programListLocator, 15)) {
			for (String programName : lstProgramNames) {
				WebElement element = RealizeUtils.getMachingTextElementFromList(lstDisplayedPrograms, programName)
						.findElement(checkboxLocator);
				RealizeUtils.scrollIntoViewIfNeeded(driver, element, 10);
				element.click();
				Log.message("Selected Program:: " + programName, driver);
			}
		} else {
			throw new Exception("No Programs found in the Programs Selection page");
		}
	}

	/**
	 * To click 'Ok' button in the program selection page
	 * 
	 * @throws Exception if 'Ok' button not found
	 */
	public void clickSaveButton() throws Exception {
		Log.event("Clicking 'Ok' button in the program selection page");
		if (RealizeUtils.waitForElement(driver, btnOK)) {
			try {
				RealizeUtils.scrollIntoViewIfNeeded(driver, btnOK, 10);
				btnOK.click();
			} catch (Exception e) {
				Log.event("Unable to click OK button. Error: " + e.getMessage());
				RealizeUtils.clickJS(driver, btnOK);
			}
			RealizeUtils.waitForElement(driver, browseProgramsTitle, 30);
			RealizeUtils.waitForLocatorToPresent(driver, programTitleListLocator, 60);
			Log.message("Clicked 'Ok' button in the program selection page");
		} else {
			throw new Exception("'Ok' button not found");
		}
	}

	/**
	 * To verify the browse program page loaded or not
	 *
	 * @return true if programs are loaded
	 */
	public boolean verifyBrowseProgramPageLoadedOrNot() {
		boolean isLoaded = false;
		Log.event("Verifying browse program page loaded or not");
		try {
			if (!RealizeUtils.waitUntilElementDisappear(driver, searchLoading, 10)) {
				Log.event("Browse program page is not loaded, so waiting again");
				switchToCanvasIframe();
				RealizeUtils.waitUntilElementDisappear(driver, searchLoading, 10);
			}
			RealizeUtils.waitForLocatorToPresent(driver, programTitleListLocator, 20);
			isLoaded = lstProgramsInBrowsePage.size() > 0;
		} catch (Exception err) {
			Log.event("Error in verifying programs results. Exception: " + err.getMessage());
		}
		return isLoaded;
	}
	
	/**
	 * Verify top header in Discover page
	 * @return - true if all the element in header is displayed on discover page
	 */
	public boolean verifyDiscoverHeaderToolbar() {
		boolean status = false;
		
		if (!((RemoteWebDriver) driver).getCapabilities().getBrowserName().matches(".*Edge.*")) {				
			Log.event("To verify top header in Discover page");
			if (!RealizeUtils.waitForElement(driver, divHeaderToolbar, 15)) {
				Log.event("Discover page top header toolbar is not displayed");
				return false;
			}else
				Log.event("Discover page top header toolbar is displayed");
	
			WebElement imgRealizeLogo = ShadowDOMUtils.findElement(imgRealizeLogoLocator, driver);
			if (RealizeUtils.waitForElement(driver, imgRealizeLogo, 10)) {
				status = true;
				Log.event("Realize Logo is displayed in top left corner on the discover page header");
			} else {
				Log.event("Realize Logo is not displayed in top left corner on the discover page header");
				return false;
			}
			
			WebElement btnUserName = ShadowDOMUtils.findElement(btnUserNameLocator, driver);
			if (RealizeUtils.waitForElement(driver, btnUserName, 10)) {
				status = true;
				Log.event("User name is displayed in middle on the discover page header");
			} else {
				Log.event("User name is not displayed in middle on the discover page header");
				return false;
			}
		}else {
			Log.event("Skipped to verify top header in Discover page in Edge browser");
			return true;
		}
		
		return status;
	}


}


