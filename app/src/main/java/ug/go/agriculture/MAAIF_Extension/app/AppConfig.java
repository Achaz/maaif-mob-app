package ug.go.agriculture.MAAIF_Extension.app;

public class AppConfig {
	// Server user login url
	public static String URL_LOGIN = "https://extension.agriculture.go.ug/?action=doLoginAPI";
	public static String URL_REGISTER_NEW = "https://extension.agriculture.go.ug/?action=apiSaveFarmer";
	public static String URL_FARMER_SELF_REGISTRATION = "https://extension.agriculture.go.ug/?action=apiFarmerSelfRegistration";
	public static String URL_VERIFY_ACCOUNT = "https://extension.agriculture.go.ug/?action=apiVerifyAccount";
	public static String URL_RESEND_VERIFICATION_CODE = "https://extension.agriculture.go.ug/?action=apiResendVerificationCode";
	public static String URL_FORGOT_PASSWORD_VERIFY_ACCOUNT = "https://extension.agriculture.go.ug/?action=apiVerifyAccountUser";
	public static String URL_FORGOT_PASSWORD = "https://extension.agriculture.go.ug/?action=apiCheckUserExists";
	public static String URL_FORGOT_PASSWORD_CHANGE_PASSWORD = "https://extension.agriculture.go.ug/?action=apiUpdatePassword";
	public static String URL_FORGOT_PASSWORD_RESEND_VERIFICATION_CODE = "https://extension.agriculture.go.ug/?action=apiForgotPasswordResendVerificationCode";

	public static String URL_QUATELY_ACTIVITIES = "https://extension.agriculture.go.ug/?action=syncQuarterlyActivitiesAPI&uid=";
	public static String URL_USER_ACTIVITIES = "https://extension.agriculture.go.ug/?action=syncDailyActivitiesAPI&uid=";
	public static String URL_USER_OUTBREAKS= "https://extension.agriculture.go.ug/?action=syncOutbreaksAPI&uid=";
	public static String URL_WEEATHER_INFO = "https://extension.agriculture.go.ug/?action=syncWeatherInfo&district=";

	//Seed Data from Web Portal
	public static String ULR_SEEDDATA = "https://extension.agriculture.go.ug/?action=syncAllSeedData";
	//	public static String URL_ACTIVITIES = "https://extension.agriculture.go.ug/?action=syncAllActivities";
	public static String URL_ENTREPRISES = "https://extension.agriculture.go.ug/?action=syncAllEntreprises";
	public static String URL_DISTRICTS = "https://extension.agriculture.go.ug/?action=syncAllDistricts";
	public static String URL_COUNTIES = "https://extension.agriculture.go.ug/?action=syncAllCounties";
	public static String URL_SUBCOUNTIES = "https://extension.agriculture.go.ug/?action=syncAllSubcounties";
	public static String URL_PARISHES = "https://extension.agriculture.go.ug/?action=syncAllParishes";
	public static String URL_VILLAGES = "https://extension.agriculture.go.ug/?action=syncAllVillages";
	public static String URL_DISTRICT_COUNTIES = "https://extension.agriculture.go.ug/?action=syncCountiesByDistrict&district=";
	public static String URL_DISTRICT_SUBCOUNTIES = " https://extension.agriculture.go.ug/?action=syncSubcountiesByDistrict&district=";
	public static String URL_DISTRICT_PARISHES = "https://extension.agriculture.go.ug/?action=syncParishesByDistrict&district=";
	public static String URL_DISTRICT_VILLAGES = "https://extension.agriculture.go.ug/?action=syncVillagesByDistrict&district=";
//	public static String URL_GRM_NATURE = "https://extension.agriculture.go.ug/?action=syncGRMNatureAPI";
//	public static String URL_GRM_TYPE = "https://extension.agriculture.go.ug/?action=syncGRMTypesAPI";
//	public static String URL_GRM_MODE_RECEIPT = "https://extension.agriculture.go.ug/?action=syncGRMModeOfReceiptAPI";
//	public static String URL_GRM_FEEDBACKMODE = "https://extension.agriculture.go.ug/?action=syncGRMFeedbackModeAPI";
//	public static String URL_GRM_SETTLEMENT = "https://extension.agriculture.go.ug/?action=syncGRMSettlementAPI";


	// Server user register url
	public static String URL_REGISTER = "https://extension.agriculture.go.ug/android_login_api/register.php";
	public static String GET_QUESTIONS = "https://extension.agriculture.go.ug/?action=apiGetQuestions&uid=";
	//public static String GET_QUESTIONS = "http://192.168.43.219/backend/?action=apiGetQuestions&uid=";
	public static String GET_QUESTION_DETAILS = "https://extension.agriculture.go.ug/?action=apiGetQuestion&id=";
	public static String GET_QUESTION_RESPONSES = "https://extension.agriculture.go.ug/?action=apiGetQuestionResponses&id=";
	public static String SAVE_QUESTION_RESPONSE = "https://extension.agriculture.go.ug/?action=apiSaveQuestionResponse&id=";
	public static String SEND_FARMER_QUESTION = "https://extension.agriculture.go.ug/?action=apiSaveFarmerQuestion";
	//public static String SEND_FARMER_QUESTION = "http://192.168.43.219/backend/?action=apiSaveFarmerQuestion";
}
