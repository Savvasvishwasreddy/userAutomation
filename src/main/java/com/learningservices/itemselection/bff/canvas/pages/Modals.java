package com.learningservices.itemselection.bff.canvas.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.pagefactory.ElementLocatorFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.learningservices.itemselection.bff.utils.RealizeUtils;
import com.learningservices.utils.ElementLayer;
import com.learningservices.utils.Log;
import com.learningservices.utils.WebDriverFactory;

/**
 * Modals for Realize
 */
public class Modals {

    private final WebDriver driver;
    public ElementLayer uielement;

    private boolean loadFromCanvas = false;
    private static final String CLASS_NO_STUD_MSG = "You have to add students to a class before creating an assignment. Add students to your class at any time by going to your class and clicking or tapping the Students & groups quick link under the class name.";
    public static final String EASYBRIDGE_CLASS_NO_STUD_MSG = "To create an assignment, you first need to add students to your class. Check your Student Information System (SIS) for student rosters and let your district administrator know what's missing.";
    public static final String NO_CLASSES_MSG = "You have to create or unhide a class before you can make an assignment.";
    public static final String CLASS_NO_PROGRAMS_MSG = "You won't be able to create assignments and your students won't be able to sign in until you add programs to your classes. Would you like to do that now?";
    private static final String REMOVE_WARNING_MSG = "Are you sure you want to remove this? You will lose your customized Table of Contents and go back to the original.";
    public final String REMOVE_ASSESSMENT_WARNING_MSG = "Are you sure you want to remove this? You will lose your customized assessment and go back to the original version.";
    private static final String SAVE_CHANGES_MSG = "You have unsaved changes. Save?";
    private static final String ITS_COMING_MSG = "We're working on making fantastic content for you. Please check back soon.";
    public static final String ASSIGNED_TEST_MSG = "The test you're customizing has already been assigned. Your new changes will appear the next time you assign this test.";
    public static final String ASSIGNED_TEST_MSG_MFE = "Your new changes will appear the next time you assign this test.";
    public static final String ITEM_BROKEN_MSG = "You have not added any questions to this test. Please customize the test to add questions before assigning it.";
    public final String DISCUSS_WARNING_MESSAGE="You're so close but a few fields still need to be filled out.";
    public final String SESSION_TIMEOUT_WARNING_MESSAGE="We haven't heard from you in a while, so we signed you out. Select Continue to sign back in.";
    public final String SESSION_TIMEOUT_WARNING_MESSAGE_SPANISH="Como no hemos visto ninguna actividad, cerramos la sesión. Para volver a iniciar la sesión hay que seleccionar Continuar.";
    public final String CONFIRM_PUBLISH_HEADER_EN = "Confirm Publish";
    public final String CONFIRM_PUBLISH_HEADER_ES = "Confirmar publicación";
    public final String CONFIRM_PUBLISH_MSG_EN = "Are you sure you want to publish this item and all of its children?";
    public final String CONFIRM_PUBLISH_MSG_ES = "¿Está seguro de que desea publicar este elemento y todos sus elementos secundarios?";
    public final String CREATE_KEY_WARNING_MESSAGE = "There was an error creating the keyset. Please try again later or contact support.";
	private static final String GREEN_COLOR ="#68a51a"; 
    private static final String WHITE_COLOR ="#ffffff";
    private static final String PINK_COLOR ="#C20C60";
    private static final String BLUE_COLOR ="#026ecb";
    private static final String BLACK_COLOR="#000000";
    private static final String WHITE_SMOKE_COLOR ="#f3f3f3";
    private static final String ORANGE_COLOR = "#EF5827";
    private static final String GREY_COLOR = "#4A4A4A";
    
    // Google Classroom Link Accounts Modal Messages & Button Texts
    public static final String LINK_ACCOUNTS_MODAL_TITLE = "Google Classroom";
    public static final String LINK_ACCOUNTS_MODAL_MESSAGE_CL_ENGLISH = "Your teacher has linked your Google Classroom class with your Realize class. In order to submit assignments, you must link Google classroom account with your Realize account.";
    public static final String LINK_ACCOUNTS_MODAL_MESSAGE_CL_SPANISH = "Su profesor vinculó la clase de Google Classroom con la de Realize. Para enviar las actividades, debe vincular la cuenta de Google Classroom con la de Realize.";
    public static final String LINK_ACCOUNTS_MODAL_MESSAGE_EL_ENGLISH = "Your Realize class is linked with Google Classroom. Before sending an assignment to your teacher, click Link Accounts to connect to Google Classroom.";
    public static final String LINK_ACCOUNTS_MODAL_MESSAGE_EL_SPANISH = "Su clase Realize está vinculada con Google Classroom. Antes de enviarle la asignación a su profesor, haga clic en Vincular cuentas para conectarse a Google Classroom.";
    public static final String LINK_ACCOUNTS_MODAL_MESSAGE_TEACHER_ENGLISH = "You are associated with a Google class that is connected with Realize. In order to share information with Google Classroom you need to link your Realize account to Google. Click Link Accounts to get started.";
    public static final String LINK_ACCOUNTS_MODAL_MESSAGE_TEACHER_SPANISH = "Está asociado con una clase de Google que está conectada con Realize. Para compartir información con Google Classroom necesita enlazar su cuenta de Realize con Google. Haga clic en Vincular cuentas para comenzar.";
    public static final String LINK_ACCOUNTS_BUTTON_TEXT_ENGLISH = "Link Accounts";
    public static final String LINK_ACCOUNTS_BUTTON_TEXT_SPANISH = "Vincular cuentas";
    public static final String CANCEL_BUTTON_TEXT_ENGLISH = "Cancel";
    public static final String CANCEL_BUTTON_TEXT_SPANISH = "Cancelar";
    public static final String ENGLISH = "English";
    public static final String SPANISH = "Spanish";
    public static final String THEME_CLASSIC = "Classic";
    public static final String THEME_EARLYLEARNER = "EarlyLearner";
    
    @FindBy(css = ".modal-header .close,.dialog-header__icon-close")
    WebElement btnModalCloseIcon;
    
    @FindBy(css = "div cel-icon[class='dialog-header__icon-close hydrated']")
    WebElement btnModalCloseIconMFE;

    @FindBy(css = ".modal-footer [ng-click='close()']")
    WebElement btnCancel;

    @FindBy(css = ".modal-footer [data-e2e-id='ok']")
    WebElement btnModalOk;
    
    @FindBy(xpath = "//span[text()='OK']/..|//span[text()='Ok']/..|//span[text()='Bueno']/..")
    WebElement btnModalOkMFE;

   //Info modal
    
    @FindBy(id = "infoDialog")
    WebElement modalInfo;
    
    @FindBy(css = "#info-modal,#infoDialog")
    WebElement modalInfoMFE;
    
    @FindBy(id = "infoDialogTitle")
    WebElement txtInfoModalTitle;
    
    @FindBy(css = ".dialog-header__message,#infoDialogTitle,h1[class='dialog-header__message']")
    WebElement txtInfoModalTitleMFE;

    @FindBy(css = "[data-e2e-info-field='description']:not(.ng-hide)")
    WebElement sectionDescription;
    
    @FindBy(css = "[data-e2e-info-field='description']")
    WebElement sectionDescriptionMFE;
    
    @FindBy(css = "[data-e2e-info-field='description']:not(.ng-hide) .infoListTitle")
    WebElement txtDescriptionSubheader;

    @FindBy(css = "[data-e2e-info-field='description']:not(.ng-hide) [data-e2e-info-field='descriptionText']")
    WebElement txtDescription;
    
    @FindBy(css = ".info-item__description,[data-e2e-info-field='description']:not(.ng-hide) [data-e2e-info-field='descriptionText']")
    WebElement txtDescriptionMFE;

    @FindBy(css = "[data-e2e-info-field='description']:not(.ng-hide) .moreOrLess a")
    WebElement lnkMoreLessDescription;

    @FindBy(css = "[data-e2e-info-field='description']:not(.ng-hide) .info-item__toggle")
    WebElement lnkMoreLessDescriptionMFE;
    
    @FindBy(css = "[data-e2e-info-field='standards']:not(.ng-hide)")
    WebElement sectionStandards;
    
    @FindBy(css = "[data-e2e-info-field='standards']")
    WebElement sectionStandardsMFE;
    
    @FindBy(css = "[data-e2e-info-field='standards']:not(.ng-hide) .infoListTitle")
    WebElement txtStandardsSubheader;

    @FindBy(css = "[data-e2e-info-field='standards']:not(.ng-hide) .standards-list li:not(.ng-hide), .info-modal__standards .info-item__standards")
    List<WebElement> lstStandardsItems;

    @FindBy(css = "[data-e2e-info-field='standards']:not(.ng-hide) .info-item__standards, .info-modal__standards .info-item__standards")
    List<WebElement> lstStandardsItemsMFE;
    
    @FindBy(css = "[data-e2e-info-field='standards']:not(.ng-hide) .moreOrLess a, .info-modal__standards .info-item__toggle")
    WebElement lnkMoreLessStandards;

    @FindBy(css = "[data-e2e-info-field='standards']:not(.ng-hide) .info-item__toggle, .info-modal__standards .info-item__toggle")
    WebElement lnkMoreLessStandardsMFE;
    
    @FindBy(css = "[data-e2e-info-field='materials']:not(.ng-hide)")
    WebElement sectionMaterials;
    
    @FindBy(css = "[data-e2e-info-field='materials']:not(.ng-hide) .infoListTitle")
    WebElement txtMaterialsSubheader;

    @FindBy(css = "[data-e2e-info-field='materials']:not(.ng-hide) [data-e2e-info-field='materialsText']")
    WebElement txtMaterials;

	@FindBy(css = ".info-item__text")
	WebElement txtMaterialsMFE;

    @FindBy(css = "[data-e2e-info-field='pacing']:not(.ng-hide)")
    WebElement sectionPacing;

    @FindBy(css = "[data-e2e-info-field='pacing']:not(.ng-hide) .infoListTitle")
    WebElement txtPacingSubheader;

    @FindBy(css = "[data-e2e-info-field='pacing']:not(.ng-hide) .infoItem")
    WebElement txtPacing;

    @FindBy(css = "[data-e2e-info-field='keywords']:not(.ng-hide)")
    WebElement sectionKeywords;
    
    @FindBy(css = "[data-e2e-info-field='keywords']")
    WebElement sectionKeywordsMFE;

    @FindBy(css = "[data-e2e-info-field='keywords']:not(.ng-hide) .infoListTitle")
    WebElement txtKeywordsSubheader;

    @FindBy(css = "[data-e2e-info-field='keywords']:not(.ng-hide) .infoItem")
    WebElement txtKeywords;

    @FindBy(css = ".modal-footer [ng-click='close()']")
    WebElement btnInfoModalClose;
    
    @FindBy(css = "cel-dialog-footer cel-button[class*='dialog-button'], close__button dialog-button hydrated, .modal-footer [ng-click='close()']")
    WebElement btnInfoModalCloseMFE;
    
    @FindBy(css = "#infoDialog .standards-list .term")
    List<WebElement> lstStandardsTerms;

    @FindBy(css = ".info-modal-standard__id")
    List<WebElement> lstStandardsTermsMFE;
    
    //another Modal
    
    @FindBy(css = "[data-e2e-info-field='author']:not(.ng-hide)")
    WebElement sectionAuthor;

    @FindBy(css = "[data-e2e-info-field='author']:not(.ng-hide) .infoListTitle")
    WebElement txtAuthorSubheader;

    @FindBy(css = "[data-e2e-info-field='author']:not(.ng-hide) .infoItem")
    WebElement txtAuthorItems;

    @FindBy(css = "[data-e2e-info-field='isbn']:not(.ng-hide)")
    WebElement sectionIsbn;

    @FindBy(css = "[data-e2e-info-field='isbn']:not(.ng-hide) .infoListTitle")
    WebElement txtIsbnSubheader;

    @FindBy(css = "[data-e2e-info-field='isbn']:not(.ng-hide) .infoItem")
    WebElement txtIsbnItems;

    @FindBy(css = "[data-e2e-info-field='levels']:not(.ng-hide)")
    WebElement sectionLevels;

    @FindBy(css = "[data-e2e-info-field='levels']:not(.ng-hide) .infoListTitle")
    WebElement txtLevelsSubheader;

    @FindBy(css = "[data-e2e-info-field='levels']:not(.ng-hide) .infoItem")
    WebElement txtLevelsItems;

    @FindBy(css = "[data-e2e-info-field='comprehensionSkills']:not(.ng-hide)")
    WebElement sectionComprehensionSkills;

    @FindBy(css = "[data-e2e-info-field='comprehensionSkills']:not(.ng-hide) .infoListTitle")
    WebElement txtComprehensionSkillsSubheader;

    @FindBy(css = "[data-e2e-info-field='comprehensionSkills']:not(.ng-hide) .infoItem")
    WebElement txtComprehensionSkillsItems;

    @FindBy(css = "[data-e2e-info-field='contentAreas']:not(.ng-hide)")
    WebElement sectionContentAreas;

    @FindBy(css = "[data-e2e-info-field='contentAreas']:not(.ng-hide) .infoListTitle")
    WebElement txtContentAreasSubheader;

    @FindBy(css = "[data-e2e-info-field='contentAreas']:not(.ng-hide) .infoItem")
    WebElement txtContentAreasItems;

    @FindBy(css = "[data-e2e-info-field='genres']:not(.ng-hide)")
    WebElement sectionGenres;

    @FindBy(css = "[data-e2e-info-field='genres']:not(.ng-hide) .infoListTitle")
    WebElement txtGenresSubheader;

    @FindBy(css = "[data-e2e-info-field='genres']:not(.ng-hide) .infoItem")
    WebElement txtGenresItems;

    @FindBy(css = "[data-e2e-info-field='textFeatures']:not(.ng-hide)")
    WebElement sectionTextFeatures;

    @FindBy(css = "[data-e2e-info-field='textFeatures']:not(.ng-hide) .infoListTitle")
    WebElement txtTextFeaturesSubheader;

    @FindBy(css = "[data-e2e-info-field='textFeatures']:not(.ng-hide) .infoItem")
    WebElement txtTextFeaturesItems;

    //Centers modals
    
    @FindBy(id = "centerThemeDialog")
    WebElement modalCenter;
    
    @FindBy(id = "centerThemeDialogTitle")
    WebElement txtCenterModalTitle;

    @FindBy(css = "#centerThemeDialogContent .infoListTitle")
    WebElement txtCenterModalSubheader;

    @FindBy(css = "#centerThemeDialogContent .infoItem")
    WebElement txtCenterModalDescription;

    @FindBy(css = "#centerThemeDialogContent .thumbSelector")
    List<WebElement> lstCenterModalThumbnails;

    @FindBy(css = ".modal-footer [ng-click='close()']")
    WebElement btnCenterModalCancel;

    @FindBy(css = ".modal-footer [data-e2e-id='launchCenter']:not(.ng-hide)")
    WebElement btnCenterModalLaunch;

    @FindBy(css = ".modal-footer [data-e2e-id='sendEmail']:not(.ng-hide)")
    WebElement btnCenterModalEmail;
    
    //Teacher resources modal
    
    @FindBy(id = "teacherSupportDialog")
    WebElement modalTeacherResources;
    
    @FindBy(css = ".teacher-resource-info")
    WebElement modalTeacherResourcesMFE;
    
    @FindBy(id = "dialog-header__message")
    WebElement txtTeacherResourcesModalTitle;
    
    @FindBy(xpath = "//h1[contains(text(),'Teacher Resources')]")
    WebElement txtTeacherResourcesModalTitleMFE;
    
    @FindBy(css = "#teacherSupportDialogContent .title")
    List<WebElement> lstTeacherResourcesModalContentItemsTitle;
    
    @FindBy(css = ".teacher-resource-info__title")
    List<WebElement> lstTeacherResourcesModalContentItemsTitleMFE;
    
    @FindBy(css = ".teacher-resource-info__title>span,.details>h3>span.title")
    List<WebElement> lstTeacherResourcesModalItems;

    @FindBy(css = ".modal-footer [ng-click='close()']")
    WebElement btnTeacherResourcesModalClose;
    
    @FindBy(xpath = "//div[@class='teacher-resource-info']//following :: cel-button/span")
    WebElement btnTeacherResourcesModalCloseMFE;

    //Create an assignment modal   
    
    @FindBy(id = "assignmentModal")
    WebElement modalCreateAssignment;
    
    @FindBy(id = "createAssignmentDialogTitle")
    WebElement txtCreateAssignmentModalTitle;

    @FindBy(css = ".modal-footer [ng-click*='saveAssignment']")
    WebElement btnAssign;

    @FindBy(css = ".modal-footer [ng-click='close(true)']")
    WebElement btnCreateAssignmentModalCancel;
    
    //Create assignments - Easybridge modal
    
    @FindBy(id = "assignModalZeroStudentStateFederatedUser")
    WebElement modalCreateAssignmentsEasybridge;
    
    //Assigned test modal

    @FindBy(id = "assessmentAssignedDialog")
    WebElement modalAssignedTest;
    
    @FindBy(id = "dialogTitle")
    WebElement txtAssignedTestTitle;

    @FindBy(id = "dialogContent")
    WebElement txtAssignedTestModalMessage;

    @FindBy(css = ".customized__assign")
    WebElement txtAssignedTestModalMessageMFE;
    
    @FindBy(xpath = "//span[text()='Bueno']/..|//button[@data-e2e-id='ok']")
    WebElement btnAssignedTestModalOk;
    
    @FindBy(xpath = "//span[text()='Ok']/..|//span[text()='Bueno']/..")
    WebElement btnAssignedTestModalOkMFE;

    @FindBy(css = ".modal-footer [data-e2e-id='cancel']")
    WebElement btnAssignedTestModalCancel;
    
    @FindBy(xpath = "//span[text()='Cancel']/..|//button[@data-e2e-id='cancel']")
    WebElement btnAssignedTestModalCancelMFE;

    //Assigning - common to all
 
    @FindBy(id = "dialogTitle")
    WebElement txtAssigningModalTitle;

    @FindBy(id = "dialogContent")
    WebElement txtAssigningModalMessage;

    @FindBy(css = ".modal-footer [data-e2e-id='cancel']")
    WebElement btnAssigningModalCancel;

    //Assigning - Zero Students modal

    @FindBy(id = "assignModalZeroStudentStateModal")
    WebElement modalAssigningZeroStudents;
    
    @FindBy(css = ".modal-footer [data-e2e-id='ok']")
    WebElement btnAssigningZeroStudentsModalGoToClasses;

    //Assigning - Zero Classes modal

    @FindBy(id = "assignModalZeroClassStateModal")
    WebElement modalAssigningZeroClasses;
    
    @FindBy(css = ".modal-footer [data-e2e-id='ok']")
    WebElement btnAssigningZeroClassesModalCreateClass;

    //Assigning - Zero Programs modal

    @FindBy(id = "assignModalNoPrograms")
    WebElement modalAssigningZeroPrograms;
    
    @FindBy(css = ".modal-footer [data-e2e-id='cancel']")
    WebElement btnAssigningZeroProgramsModalNotNow;

    @FindBy(css = ".modal-footer [data-e2e-id='ok']")
    WebElement btnAssigningZeroProgramsModalLetsDoIt;

    //Assigning - Missing Item modal

    @FindBy(id = "assignModalBrokenItemModal")
    WebElement modalAssigningBrokenItem;
        
    @FindBy(id = "dialogContent")
    WebElement txtAssigningBrokenItemModalMessage;

    //Customizing broken item - It's coming! modal

    @FindBy(css = "div[id*='assessment.customize.notAvailable'],div[class*='dialog-wrapper--visible']")
    WebElement modalCustomizingBrokenItem;
    
    @FindBy(id = "dialogTitle")
    WebElement txtCustomizingBrokenItemTitle;
        
    @FindBy(css = "div[class*='customize-info__header']")
    WebElement txtCustomizingBrokenItemTitleMFE;
    
    @FindBy(id = "dialogContent")
    WebElement txtCustomizingBrokenItemModalMessage;

    @FindBy(css = "div[class*='customize-info__text']")
    WebElement txtCustomizingBrokenItemModalMessageMFE;

   //Remove changes modal
    
    @FindBy(id = "removeCustomizedWarningModal")
    WebElement modalRemoveCustomizedWarning;
    
    @FindBy(id = "dialogTitle")
    WebElement txtRemoveModalTitle;

    @FindBy(id = "dialogContent")
    WebElement txtRemoveModalMessage;

    @FindBy(css = ".modal-footer .secondary")
    WebElement btnRemoveModalCancel;

    @FindBy(css = ".modal-footer button:not(.secondary)")
    WebElement btnRemoveModalOk;
    
    //Save changes modal
    
    @FindBy(css = "#unsavedEditDialog, #unsavedChangesModal, [class*='btf-modal modal__content']")
    WebElement modalSaveChangesWarning;

    @FindBy(css = "#unsavedEditDialog h1, #unsavedChangesModal h1, #modalTitle")
    WebElement txtSaveChangesModalTitle;

    @FindBy(css = "#unsavedEditDialogContent, #unsavedChangesModalContent, #dialogContent, [data-e2e-id='manualScore-modalBody-description']")
    WebElement txtSaveChangesModalMessage;

    @FindBy(css = "[data-e2e-id='cancel'], div.btf-modal.modal__content.manualScore__modal span:nth-child(1) > button")
    WebElement btnContinueWithoutSaving;

    @FindBy(css = "[data-e2e-id='ok'], div.btf-modal.modal__content.manualScore__modal span:nth-child(2) > button")
    WebElement btnSaveChanges;
    
    //Progress modal
    
    @FindBy(id = "progressDialog")
    WebElement modalProgress;

    //Associated items modal
    
    private static final String txtAnswerMcLabelLocator = ".responseLabel";
    private static final String txtAnswerMcLabelLocatorMFE = ".questions-for-skill__responseLabel";
    private static final String txtAnswerLocator = ".responseText";
    private static final String txtAnswerLocatorMFE = "[class*='questions-for-skill__responseText']";
    private static final String iconCorrectAnswerLocator = ".score-icon:not(.ng-hide) .icon-ok-sign";
    private static final String iconCorrectAnswerLocatorMFE = ".icon-checkmark:not(.ng-hide)";

    @FindBy(id = "questionsAssociatedToSkillDialog")
    WebElement modalAssociatedItems;

    @FindBy(id = "questionsAssociatedToSkillDialogTitle")
    WebElement txtAssociatedItemsModalTitle;

    @FindBy(className = "question-number")
    List<WebElement> lstAssociatedItemsModalQuestionNumbers;

    @FindBy(className = "questions-for-skill__questionNumber")
    List<WebElement> lstAssociatedItemsModalQuestionNumbersMFE;
    		
    @FindBy(css = "[data-question-type='multiple-choice']")
    List<WebElement> lstAssociatedItemsModalMcQuestions;

    @FindBy(xpath = "//*[@ng-reflect-ng-switch='MULTIPLE_CHOICE']//ancestor :: li[@class='questions-for-skill__questionItem']")
    List<WebElement> lstAssociatedItemsModalMcQuestionsMFE;
    
    @FindBy(css = "[data-question-type='gridded-response']")
    List<WebElement> lstAssociatedItemsModalGrQuestions;
  
    @FindBy(xpath = "//*[@ng-reflect-ng-switch='GRIDDED_RESPONSE']//ancestor :: li[@class='questions-for-skill__questionItem']")
    List<WebElement> lstAssociatedItemsModalGrQuestionsMFE;
    
    @FindBy(className = "question-indent")
    List<WebElement> lstAssociatedItemsModalQuestionText;
    
    @FindBy(className = "questions-for-skill__questionText")
    List<WebElement> lstAssociatedItemsModalQuestionTextMFE;

    @FindBy(css = ".modal-footer [ng-click='close()']")
    WebElement btnAssociatedItemsModalClose;
    
	// newly added
	public static final String qlAssign = ".quicklinks li[icon = 'share'], .assign-link";

	// Discuss warning modal
    
    @FindBy(css=".alert.alert-error.fade.in.closable")
    WebElement mdlDiscussWarning;
    
    @FindBy(css=".icon-remove")
    WebElement iconRemove;
    
    @FindBy(css=".icon-exclamation-sign")
    WebElement iconExclamation;
    
    @FindBy(css=".message-container:not(.hide)")
    WebElement txtWarningModal;
    
    @FindBy(id="sessionTimeoutDialog")
    WebElement mdlSessionTimeoutWarning;
    
    @FindBy(id = "sessionTimeoutDialogContent")
    WebElement txtSessionTimeoutModalMessage;
    
    @FindBy(xpath="//h2[contains(.,'Session timed out')]")
    WebElement mdlK2SessionTimeoutWarning;
    
    @FindBy(css = ".btf-modal.modal__content span.standardModal__description")
    WebElement txtK2SessionTimeoutModalMessage;
    
    @FindBy(css = ".btf-modal.modal__content button")
    WebElement btnK2SessionTimeoutContinue;
    
    // Confirm Publish modal
    
    @FindBy(id = "simpleDialog")
    WebElement mdlConfirmPublish;
    
    @FindBy(css = ".publish-link__dialog .dialog-wrapper")
    WebElement mdlConfirmPublishMFE;
    
    @FindBy(id = "dialogTitle")
    WebElement txtConfirmPublishHeader;
    
    @FindBy(css = ".publish-info__header")
    WebElement txtConfirmPublishHeaderMFE;
    
    @FindBy(id = "dialogContent")
    WebElement txtConfirmPublish;
    
    @FindBy(css = ".publish-info__text")
    WebElement txtConfirmPublishMFE;
    
    @FindBy(css = "[data-e2e-id='cancel']")
    WebElement btnCancelPublish;
    
    @FindBy(xpath = "//cel-button[@class='dialog-button hydrated'][1]")
    WebElement btnCancelPublishMFE;
    
    @FindBy(css = "[data-e2e-id='ok']")
    WebElement btnPublish;
    
    @FindBy(xpath = "//cel-button[@class='dialog-button hydrated'][2]")
    WebElement btnPublishMFE;
    
    @FindBy(css = ".icon-remove")
    WebElement iconClose;
    
    @FindBy(css = "div#sessionTimeoutDialog button")
    WebElement btnSessionTimeoutContinue;
    
    @FindBy(css = ".modal-header")
    WebElement btnModalHeader;  
    
    @FindBy(css="div.dialog-header")
    WebElement btnModalHeaderMFE;  
    
    @FindBy(css="dialog-header__message")
    WebElement btnModalHeaderMessageMFE;
    
    // Select Program modal
    
    private static final String optionsQLInSelectProgramModal = "ul .nested-quicklinks .dropdown-toggle:not(.ng-binding)";
    private static final String qlAssignItem="ul > li:not([class*='hide'])[text*='assign'] > a";
    private static final String qlPublish="ul > li:not([class*='hide'])[text*='publish'] > a";
    private static final String qlInfo ="ul > li:not([class*='hide'])[text*='viewInfo'] > a";
    private static final String qlCustomize = "ul > li:not([class*='hide'])[text*='Customize'] > a";
    private static final String qlRemediation="ul > li:not([class*='hide'])[text*='remediation'] > a";
    private static final String qlTeacherResource="ul > li:not([class*='hide'])[text*='teacherResources'] > a";
    private static final String qlOptions="ul > li:not([class*='hide'])[text*='options'] > a";
    
    @FindBy(css = ".programGroupViewDialog")
    WebElement mdlSelectProgram;
    
    @FindBy(id = "programGroupViewTitle")
    WebElement txtSelectProgramHeader;
    
    @FindBy(css = ".programGroupViewDialog .description")
    WebElement txtSelectProgram;
    
    @FindBy(css = ".sectionContent .item")
    List<WebElement> lstcontentItemsInModal;
    
    @FindBy(css = ".modal-header .close")
    WebElement iconCloseSelectProgramModal;
    
    @FindBy(css = ".item h3 a")
    List<WebElement> lstContentItems;
    
    @FindBy(css = ".itemImage.contentThumb img")
    WebElement lstItemImage;	
    
    @FindBy(css="#teacherSupportDialogContent .quicklinks>li")
    WebElement popupPosition;
    
    @FindBy(css="#teacherSupportDialogContent .itemList>li:first-of-type div.details ul>li.quick-link.ng-scope")
    WebElement firstPublishOption;
    
    @FindBy(css="#teacherSupportDialogContent .itemList>li:last-of-type div.details ul>li.quick-link.ng-scope")
    WebElement lastPublishOption;
    
    
    // Add to Playlist modal
    
    @FindBy(css = ".addToPlaylistModal")
	WebElement addToPlaylistModal;
    
    // Info Modal for Customer Admin
    
    @FindBy(css = "div.dialog-header > h1")
    WebElement infoModalTitle;
    
    @FindBy(css = ".dialog-button-group cel-button.dialog-button")
    WebElement btnClose;
    
    @FindBy(css = "span.content-info__item--title")
    List<WebElement> txtFieldLabel;
    
    @FindBy(css = "div.dialog-wrapper__content")
    WebElement infoModalBody;
    
    @FindBy(css = "div[class='pull-right']:not(.ng-hide) .moreOrLess a")
    WebElement moreOrLessLink;
    
    @FindBy(css = "#toc-viewer-iframe")
    WebElement frameTOC;
    
    @FindBy(css ="#simpleDialog,modal[data-e2e-id='connectToGoogle'] div[aria-label='Google Classroom']")
    WebElement mdlLinkAccounts;
    
    @FindBy(css = "#dialogTitle,#modalTitle")
    WebElement txtLinkAccountsModalTitle;
    
    @FindBy(css = "#dialogContent,div.modal__body span")
    WebElement txtLinkAccountsModalDescription;
    
    @FindBy(css = "#dialogContent strong")
    WebElement txtLinkAccountsModalDescriptionBoldFont;
    
    @FindBy(css = "div[id='simpleDialog'] div button:nth-child(1)")
    WebElement btnCancelInLinkAccountsModalForCLTheme;
    
    @FindBy(css = "div[id='simpleDialog'] div button:nth-child(2)")
    WebElement btnLinkAccountsForCLTheme;
    
    @FindBy(css = "div.modal__footer button.button--no")
    WebElement btnCancelInLinkAccountsModalForELTheme;
    
    @FindBy(css = "div.modal__footer button.button--yes")
    WebElement btnLinkAccountsForELTheme;
    
    @FindBy(css = "a[role='button']")
    WebElement iconCloseInLinkAccountsModal;
    
    @FindBy(css = "div[id='simpleDialog'] div button:nth-child(2),div.modal__footer button.button--yes")
    WebElement btnLinkAccounts;
    
    @FindBy(css = "div[id='simpleDialog'] div button:nth-child(1),div.modal__footer button.button--no")
    WebElement btnCancelInLinkAccountsModal;
    
    private static final By txtFieldDescription = By.cssSelector("div[class^='content-info__item--']");
    
    /**
     * 
     * Constructor class for Modals. Here we initialize the driver for
     * page factory objects and specific wait time for Ajax element
     * @param driver -
     */
    public Modals(WebDriver driver) {
        this.driver = driver;
        ElementLocatorFactory finder = new AjaxElementLocatorFactory(driver, RealizeUtils.realizeMinElementWait);
        PageFactory.initElements(finder, this);
        uielement = new ElementLayer(driver);
    }
    
   }
