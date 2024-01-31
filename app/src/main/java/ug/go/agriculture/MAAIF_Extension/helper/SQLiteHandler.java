/**
 * Author: Ravi Tamada
 * URL: www.androidhive.info
 * twitter: http://twitter.com/ravitamada
 * */
package ug.go.agriculture.MAAIF_Extension.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Date;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class SQLiteHandler extends SQLiteOpenHelper {
	private static final String TAG = SQLiteHandler.class.getSimpleName();

	// All Static variables
	private static final int DATABASE_VERSION = 2;
	private static String DATABASE_PATH = "";
	private static final String DATABASE_NAME = "agriserv.db";

	// User Data tables
	private static final String TABLE_USER = "user";
	private static final String TABLE_ACTIVITY = "mod_ediary";
	private static final String TABLE_ACTIVITY_QUARTER = "user_quaterly_activities";
	private static final String TABLE_DISTRICT = "district";
	private static final String TABLE_COUNTY = "county";
	private static final String TABLE_SUBCOUNTY = "subcounty";


	// Login Table Columns names
	public static final String KEY_ID = "id";
	public static final String KEY_UID = "uid";
	public static final String KEY_USER_CATEGORY_ID = "user_category_id";
	public static final String KEY_USER_DISTRICT_ID = "district_id";
	public static final String KEY_FIRST_NAME = "first_name";
	public static final String KEY_LAST_NAME = "last_name";
	public static final String KEY_USERNAME = "username";
	public static final String KEY_EMAIL = "email";
	public static final String KEY_PASSWORD = "password";
	public static final String KEY_CREATED = "created";
	public static final String KEY_PHONE = "phone";
	public static final String KEY_SUBCOUNTY = "subcounty";
	public static final String KEY_DISTRICT = "district";
	public static final String KEY_USER_CATEGORY = "user_category";
	public static final String KEY_PHOTO = "photo";
	public static final String KEY_GENDER = "gender";
	public static final String KEY_NAME = "name";
	public static final String KEY_TOPICS = "topics";
	public static final String KEY_ENTREPRIZES = "entreprizes";
	public static final String KEY_ACTIVITIES = "activities";
	public static final String KEY_SYNCED = "synced";


	private SQLiteDatabase mDataBase;
	private SQLiteDatabase mDb;
	private final Context mContext;
	private boolean mNeedUpdate = false;

	public SQLiteHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		if (android.os.Build.VERSION.SDK_INT >= 17)
			DATABASE_PATH = context.getApplicationInfo().dataDir + "/databases/";
		else
			DATABASE_PATH = "/data/data/" + context.getPackageName() + "/databases/";
		this.mContext = context;

		copyDataBase();

		this.getReadableDatabase();
	}

	public void updateDataBase() throws IOException {
		if (mNeedUpdate) {
			File dbFile = new File(DATABASE_PATH + DATABASE_NAME);
			if (dbFile.exists())
				dbFile.delete();

			copyDataBase();

			mNeedUpdate = false;
		}
	}

	private boolean checkDataBase() {
		File dbFile = new File(DATABASE_PATH + DATABASE_NAME);
		return dbFile.exists();
	}

	private void copyDataBase() {
		if (!checkDataBase()) {
			this.getReadableDatabase();
			this.close();
			try {
				copyDBFile();
			} catch (IOException mIOException) {
				throw new Error("Error Copying DataBase");
			}
		}
	}

	private void copyDBFile() throws IOException {
		InputStream mInput = mContext.getAssets().open(DATABASE_NAME);
		//InputStream mInput = mContext.getResources().openRawResource(R.raw.info);
		OutputStream mOutput = new FileOutputStream(DATABASE_PATH + DATABASE_NAME);
		byte[] mBuffer = new byte[1024];
		int mLength;
		while ((mLength = mInput.read(mBuffer)) > 0)
			mOutput.write(mBuffer, 0, mLength);
		mOutput.flush();
		mOutput.close();
		mInput.close();
	}

	public boolean openDataBase() throws SQLException {
		mDataBase = SQLiteDatabase.openDatabase(DATABASE_PATH + DATABASE_NAME, null, SQLiteDatabase.CREATE_IF_NECESSARY);
		return mDataBase != null;
	}

	@Override
	public synchronized void close() {
		if (mDataBase != null)
			mDataBase.close();
		super.close();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (newVersion > oldVersion)
			mNeedUpdate = true;
	}

	/**
	 * Storing user details in database
	 * */
	public void addUser(String first_name, String last_name, String username, String email, String password, String phone, String subcounty, String district, String user_category, String photo, String gender, String created, String user_category_id, String uid, String district_id) {
		//SQLiteDatabase db = this.getWritableDatabase();
		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}


		ContentValues values = new ContentValues();
		values.put(KEY_FIRST_NAME, first_name);
		values.put(KEY_LAST_NAME, last_name);
		values.put(KEY_USERNAME, username);
		values.put(KEY_EMAIL, email);
		values.put(KEY_PASSWORD, password);
		values.put(KEY_PHONE, phone);
		values.put(KEY_SUBCOUNTY, subcounty);
		values.put(KEY_DISTRICT, district); // Email
		values.put(KEY_USER_CATEGORY, user_category); // Email
		values.put(KEY_PHOTO, photo); // Email
		values.put(KEY_GENDER, gender); // Email
		values.put(KEY_CREATED, created); // Created At
		values.put(KEY_UID, uid); // Created At
		values.put(KEY_USER_CATEGORY_ID, user_category_id); // Created At
		values.put(KEY_USER_DISTRICT_ID, district_id); // Created At

		// Inserting Row
		long id = mDb.insert(TABLE_USER, null, values);
		mDb.close(); // Closing database connection

		Log.d(TAG, "New user inserted into sqlite: " + id);
	}

	/** OUTBREAKS & CRISES */
	public List<Map<String, Object>> outbreaks_crises_stats(){
		List<Map<String, Object>> res = new ArrayList<>();
		Map<String, Object> obj = new HashMap<>();

		// read db
		SQLiteDatabase dbRead = getReadableDatabase();

		// pending
		String queryUnsynced = "SELECT COUNT(*) FROM mod_crises_outbreaks WHERE synced = 0";
		Cursor cursorUnsynced = dbRead.rawQuery(queryUnsynced, null);
		int unsynced_count = 0;
		if (cursorUnsynced.moveToFirst()) {
			unsynced_count = cursorUnsynced.getInt(0);
		}
		obj.put("unsynced", unsynced_count);

		// total outbreaks
		String[] outbreaks_whereArgs = {"Outbreak"};
		String queryOutbreaks = "SELECT COUNT(*) FROM mod_crises_outbreaks WHERE a9 = ?";
		Cursor outbreaksSynced = dbRead.rawQuery(queryOutbreaks, outbreaks_whereArgs);
		int outbreaks_count = 0;
		if (outbreaksSynced.moveToFirst()) {
			outbreaks_count = outbreaksSynced.getInt(0);
		}
		obj.put("outbreaks", outbreaks_count);

		// total crises
		String[] crises_whereArgs = {"Crisis"};
		String queryCrises = "SELECT COUNT(*) FROM mod_crises_outbreaks WHERE a9 = ?";
		Cursor cursorCrises = dbRead.rawQuery(queryCrises, crises_whereArgs);
		int crises_count = 0;
		if (cursorCrises.moveToFirst()) {
			crises_count = cursorCrises.getInt(0);
		}
		obj.put("crises", crises_count);

		Integer total = outbreaks_count + crises_count;
		obj.put("all", total);
		Integer synced = total - unsynced_count;
		obj.put("synced", synced);

		res.add(obj);

		// close db connection
		dbRead.close();

		return res;
	}

	public void addOutbreakCrisis(String a1, String a2, String a3,
								  String a4, String a5, String a6, String a7,
								  String a8, String a9, String a10, String a11,
								  String a12, String a13, String a14, String a15,
								  String a16, String a1a,String a1b,String a1c) {

		// Fetching user details from SQLite
		HashMap<String, String> user = this.getUserDetails();
		String uid =  user.get("uid");
		Integer user_id  = Integer.valueOf(uid);

		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		Integer idx = 1;

		Date date= new Date();

		java.util.Date dt = new java.util.Date();

		java.text.SimpleDateFormat sdf =
				new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String currentTime = sdf.format(dt);
		String b18 = getSerialNumber();

		ContentValues values = new ContentValues();
		values.put("a1", a1);
		values.put("a2", a2);
		values.put("a3", a3);
		values.put("a4", a4);
		values.put("a5", a5);
		values.put("a6", a6);
		values.put("a7", a7);
		values.put("a8", a8);
		values.put("a9", a9);
		values.put("a10", a10);
		values.put("a11", a11);
		values.put("a12", a12);
		values.put("a13", a13);
		values.put("a14", a14);
		values.put("a15", a15);
		values.put("a16", a16);
		values.put("synced", 0);
		values.put("a17", b18);
		values.put("a18", currentTime);
		values.put("a19", user_id );
		values.put("a22", a1a );
		values.put("a23", a1b );
		values.put("a24", a1c );

		// Inserting Row
		long id = mDb.insert("mod_crises_outbreaks", null, values);
		mDb.close(); // Closing database connection

		Log.d(TAG, "New Crisis/Outbreak inserted into sqlite: " + id);
	}

	public void addPlannedActivity(String activity, String topic, String entreprize,
								   String subcounty, String village, String ben_group, String reference,
								   String reference_contact, String males, String females, String remarks, String latitude, String longitude, String parish, String district, String lessons, String recommendations, String description) {



		// Fetching user details from SQLite
		HashMap<String, String> user = this.getUserDetails();
		String uid =  user.get("uid");
		Integer user_id  = Integer.valueOf(uid);


		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		Integer idx = 1;

		Date date= new Date();

		java.util.Date dt = new java.util.Date();

		java.text.SimpleDateFormat sdf =
				new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String currentTime = sdf.format(dt);
		String b18 = getSerialNumber();

		ContentValues values = new ContentValues();
		values.put("a1", topic);
		values.put("a2", activity);
		values.put("a3", entreprize);
		values.put("a4", parish);
		values.put("a5", district);
		values.put("a6", subcounty);
		values.put("a7", village);
		values.put("a8", ben_group);
		values.put("a9", reference);
		values.put("a10", reference_contact);
		values.put("a11", males);
		values.put("a12", females);
		values.put("a13", remarks);
		values.put("a14", user_id);
		values.put("a15", latitude);
		values.put("a16", longitude);
		values.put("synced", 0);
		values.put("a17", currentTime);
		values.put("a18", b18);
		values.put("a20", 0); // planned or unplanned activity
		values.put("a21", lessons);
		values.put("a22", recommendations);
		values.put("a22", description);

		// Inserting Row
		long id = mDb.insert(TABLE_ACTIVITY, null, values);
		mDb.close(); // Closing database connection

		Log.d(TAG, "New daily activity inserted into sqlite: " + id);
	}


	public void addNewMarket(String a1, String a2, String a3, String a8, String a9, String a10, String a11, String a12, String a13,String a15, String a16,
							 String district, String subcounty,  String parish, String village) {



		// Fetching user details from SQLite
		HashMap<String, String> user = this.getUserDetails();
		String uid =  user.get("uid");
		Integer user_id  = Integer.valueOf(uid);


		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		Integer idx = 1;

		Date date= new Date();

		java.util.Date dt = new java.util.Date();

		java.text.SimpleDateFormat sdf =
				new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String currentTime = sdf.format(dt);
		String b18 = getSerialNumber();

		ContentValues values = new ContentValues();
		values.put("a1", a1);
		values.put("a2", a2);
		values.put("a3", a3);
		values.put("a4", parish);
		values.put("a5", district);
		values.put("a6", subcounty);
		values.put("a7", village);
		values.put("a8", a8);
		values.put("a9", a9);
		values.put("a10", a10);
		values.put("a11", a11);
		values.put("a12", a12);
		values.put("a13", a13);
		values.put("a14", user_id);
		values.put("a15", a15);
		values.put("a16", a16);
		values.put("synced", 0);
		values.put("a17", currentTime);
		values.put("a18", b18);


		// Inserting Row
		long id = mDb.insert("db_market", null, values);
		mDb.close(); // Closing database connection

		Log.d(TAG, "New Market inserted into sqlite: " + id);
	}

	public void dumpActivities(
			String a1, String a2, String a3, String a4, String a5, String a6, String a7, String a8, String a9,String a10,
			String a11, String a12, String a13, String a14, String a15, String a16, String a17, String a18, String a19,String a20,
			String a21, String a22, String a23, String a24, String a25, String a26, String a27, String a28, String a29,String a30,
			String a31, String a32, String a33, String a34, String a35, String a36, String a37, String a38, String a39,String a40,
			String a41, String a42, String a43, String a44, String a45, String a46, String a47, String a48, String a49,String a50
	) {



		// Fetching user details from SQLite
		HashMap<String, String> user = this.getUserDetails();
		String uid =  user.get("uid");
		Integer user_id  = Integer.valueOf(uid);


		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		Integer idx = 1;

		Date date= new Date();

		java.util.Date dt = new java.util.Date();

		java.text.SimpleDateFormat sdf =
				new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String currentTime = sdf.format(dt);
		String b18 = getSerialNumber();

		ContentValues values = new ContentValues();
		values.put("a1", a1);
		values.put("a2", a2);
		values.put("a3", a3);
		values.put("a4", a4);
		values.put("a5", a5);
		values.put("a6", a6);
		values.put("a7", a7);
		values.put("a8", a8);
		values.put("a9", a9);
		values.put("a10", a10);
		values.put("a11", a11);
		values.put("a12", a12);
		values.put("a13", a13);
		values.put("a14", a14);
		values.put("a15", a15);
		values.put("a16", a16);
		values.put("a17", a17);
		values.put("a18", a18);
		values.put("a19", a19);
		values.put("a20", a20);
		values.put("a21", a21);
		values.put("a22", a22);
		values.put("a23", a23);
		values.put("a24", a24);
		values.put("a25", a25);
		values.put("a26", a26);
		values.put("a27", a27);
		values.put("a28", a28);
		values.put("a29", a29);
		values.put("a30", a30);
		values.put("a31", a31);
		values.put("a32", a32);
		values.put("a33", a33);
		values.put("a34", a34);
		values.put("a35", a35);
		values.put("a36", a36);
		values.put("a37", a37);
		values.put("a38", a38);
		values.put("a39", a39);
		values.put("a40", a40);
		values.put("a41", a41);
		values.put("a42", a42);
		values.put("a43", a43);
		values.put("a44", a44);
		values.put("a45", a45);
		values.put("a46", a46);
		values.put("a47", a47);
		values.put("a48", a48);
		values.put("a49", a49);
		values.put("a50", a50);
		values.put("synced", 1);

		// Inserting Row
		long id = mDb.insert("mod_ediary", null, values);
		mDb.close(); // Closing database connection

		Log.d(TAG, "New Activity inserted into sqlite: " + id);
	}


	public void dumpOutbreaks(
			String a1, String a2, String a3, String a4, String a5, String a6, String a7, String a8, String a9,String a10,
			String a11, String a12, String a13, String a14, String a15, String a16, String a17, String a18, String a19,String a20,
			String a21, String a22, String a23, String a24, String a25, String a26, String a27, String a28, String a29,String a30,
			String a31, String a32, String a33, String a34, String a35, String a36, String a37, String a38, String a39,String a40,
			String a41, String a42, String a43, String a44, String a45, String a46, String a47, String a48, String a49,String a50
	) {



		// Fetching user details from SQLite
		HashMap<String, String> user = this.getUserDetails();
		String uid =  user.get("uid");
		Integer user_id  = Integer.valueOf(uid);


		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		Integer idx = 1;

		Date date= new Date();

		java.util.Date dt = new java.util.Date();

		java.text.SimpleDateFormat sdf =
				new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String currentTime = sdf.format(dt);
		String b18 = getSerialNumber();

		ContentValues values = new ContentValues();
		values.put("a1", a1);
		values.put("a2", a2);
		values.put("a3", a3);
		values.put("a4", a4);
		values.put("a5", a5);
		values.put("a6", a6);
		values.put("a7", a7);
		values.put("a8", a8);
		values.put("a9", a9);
		values.put("a10", a10);
		values.put("a11", a11);
		values.put("a12", a12);
		values.put("a13", a13);
		values.put("a14", a14);
		values.put("a15", a15);
		values.put("a16", a16);
		values.put("a17", a17);
		values.put("a18", a18);
		values.put("a19", a19);
		values.put("a20", a20);
		values.put("a21", a21);
		values.put("a22", a22);
		values.put("a23", a23);
		values.put("a24", a24);
		values.put("a25", a25);
		values.put("a26", a26);
		values.put("a27", a27);
		values.put("a28", a28);
		values.put("a29", a29);
		values.put("a30", a30);
		values.put("a31", a31);
		values.put("a32", a32);
		values.put("a33", a33);
		values.put("a34", a34);
		values.put("a35", a35);
		values.put("a36", a36);
		values.put("a37", a37);
		values.put("a38", a38);
		values.put("a39", a39);
		values.put("a40", a40);
		values.put("a41", a41);
		values.put("a42", a42);
		values.put("a43", a43);
		values.put("a44", a44);
		values.put("a45", a45);
		values.put("a46", a46);
		values.put("a47", a47);
		values.put("a48", a48);
		values.put("a49", a49);
		values.put("a50", a50);
		values.put("synced", 1);

		// Inserting Row
		long id = mDb.insert("mod_crises_outbreaks", null, values);
		mDb.close(); // Closing database connection

		Log.d(TAG, "New Outbreak inserted into sqlite: " + id);
	}

	public void dumpAllTopics(String id, String name, String category) {

		// Fetching user details from SQLite
		HashMap<String, String> user = this.getUserDetails();
		String uid =  user.get("uid");

		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}



		ContentValues values = new ContentValues();
//		values.put("id", id);
		values.put("name", name);
		values.put("category", category);


		// Inserting Row
		long idv = mDb.insert("topics", null, values);
		mDb.close(); // Closing database connection

		Log.d(TAG, "New Topics Inserted into sqlite: " + id);
	}



	public void dumpAllActivities(String id, String name, String category) {

		// Fetching user details from SQLite
		HashMap<String, String> user = this.getUserDetails();
		String uid =  user.get("uid");

		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		ContentValues values = new ContentValues();
		values.put("id", id);
		values.put("name", name);
		values.put("category", category);

		// Inserting Row
		long idv = mDb.insert("activities", null, values);
		mDb.close(); // Closing database connection

		Log.d(TAG, "New Activities Inserted into sqlite: " + id);
	}



	public void dumpAllEntreprizes(
			String id, String name, String parent_id) {



		// Fetching user details from SQLite
		HashMap<String, String> user = this.getUserDetails();
		String uid =  user.get("uid");

		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}



		ContentValues values = new ContentValues();
		values.put("id", id);
		values.put("name", name);
		values.put("parent_id", parent_id);


		// Inserting Row
		long idv = mDb.insert("entreprizes", null, values);
		mDb.close(); // Closing database connection

		Log.d(TAG, "New Entreprizes Inserted into sqlite: " + id);
	}


	public void dumpAllDistricts(String id, String name) {

		// Fetching user details from SQLite
		HashMap<String, String> user = this.getUserDetails();
		String uid =  user.get("uid");

		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		ContentValues values = new ContentValues();
		values.put("id", id);
		values.put("name", name);

		// Inserting Row
		long idv = mDb.insert("district", null, values);
		mDb.close(); // Closing database connection
	}

	public void addLoggedIn() {

		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		ContentValues values = new ContentValues();
		values.put("is_logged_in", "yes");

		// Inserting Row
		long idv = mDb.insert("is_user_logged_in", null, values);
		mDb.close(); // Closing database connection
	}

	//	markets
	public String checkIfUserIsLoggedIn(){
		String is_logged_in = "no";
		// Select All Query
		String selectQuery = "SELECT  * FROM is_user_logged_in limit 1";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				is_logged_in = cursor.getString(0).toLowerCase();
			} while (cursor.moveToNext());
		}

		// closing connection
		cursor.close();
		db.close();

		// returning names
		return is_logged_in;
	}

	public void resetIsLoggedIn() {

		SQLiteDatabase db = this.getWritableDatabase();
		// Delete All Rows from these tables
		db.delete("is_user_logged_in", null, null);
		db.close();

		Log.d(TAG, "Deleted all user logged in info from sqlite");
	}

	public void addDistrict(String id, String name) {

		// Fetching user details from SQLite
		HashMap<String, String> user = this.getUserDetails();
		String uid =  user.get("uid");

		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		ContentValues values = new ContentValues();
		values.put("id", id);
		values.put("name", name);

		// Inserting Row
		long idv = mDb.insert("district2", null, values);
		mDb.close(); // Closing database connection
	}

	public void addDistrict2(String id, String name) {

		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		ContentValues values = new ContentValues();
		values.put("id", id);
		values.put("name", name);

		// Inserting Row
		long idv = mDb.insert("district2", null, values);
		mDb.close(); // Closing database connection
	}

	// add counties
	public void addCounty(String county_id, String name, String district) {

		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		ContentValues values = new ContentValues();
		values.put("id", county_id);
		values.put("name", name);
		values.put("district_id", district);

		// Inserting Row
		mDb.insert("county", null, values);
		mDb.close(); // Closing database connection

		Log.d(TAG, "County added: " + name);
	}

	// add sub counties
	public void addSubCounty(String subcounty_id, String name, String county_id) {

		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		ContentValues values = new ContentValues();
		values.put("id", subcounty_id);
		values.put("name", name);
		values.put("county_id", county_id);

		// Inserting Row
		mDb.insert("subcounty", null, values);
		mDb.close(); // Closing database connection

		Log.d(TAG, "Subcounty added: " + name);
	}

	// add sub parishes
	public void addParish(String parish_id, String name, String subcounty_id) {

		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		ContentValues values = new ContentValues();
		values.put("id", parish_id);
		values.put("name", name);
		values.put("subcounty_id", subcounty_id);

		// Inserting Row
		mDb.insert("parish", null, values);
		mDb.close(); // Closing database connection

		Log.d(TAG, "parish added: " + name);
	}

	// add villages
	public void addVillage(String village_id, String name, String parish_id) {

		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		ContentValues values = new ContentValues();
		values.put("id", village_id);
		values.put("name", name);
		values.put("parish_id", parish_id);

		// Inserting Row
		mDb.insert("village", null, values);
		mDb.close(); // Closing database connection

		Log.d(TAG, "Village added: " + name);
	}


	public void resetAllTablesSeedData() {

		SQLiteDatabase db = this.getWritableDatabase();
		// Delete All Rows from these tables
		db.delete("activities", null, null);
		db.delete("entreprizes", null, null);
		db.delete("village", null, null);
		db.delete("parish", null, null);
		db.delete("subcounty", null, null);
		db.delete("county", null, null);
		db.delete("district", null, null);
		db.delete("district2", null, null);
		db.close();

		Log.d(TAG, "Deleted all user info from sqlite");
	}

	public void addNewPriceVolume(String a1, String a2, String a3, String a4, String a5, String a6, String a8, String a9) {



		// Fetching user details from SQLite
		HashMap<String, String> user = this.getUserDetails();
		String uid =  user.get("uid");
		Integer user_id  = Integer.valueOf(uid);


		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		Integer idx = 1;

		Date date= new Date();

		java.util.Date dt = new java.util.Date();

		java.text.SimpleDateFormat sdf =
				new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String currentTime = sdf.format(dt);
		String b11 = getSerialNumber();

		ContentValues values = new ContentValues();
		values.put("a1", a1);
		values.put("a2", a2);
		values.put("a3", a3);
		values.put("a4", a4);
		values.put("a5", a5);
		values.put("a6", a6);
		values.put("a7", user_id);
		values.put("a8", a8);
		values.put("a9", a9);
		values.put("a10", currentTime);
		values.put("a11", b11);
		values.put("synced", 0);



		// Inserting Row
		long id = mDb.insert("mod_market_price_volume", null, values);
		mDb.close(); // Closing database connection

		Log.d(TAG, "New Market Price and Volume inserted into sqlite: " + id);
	}


	/**
	 * Storing user details in database
	 * */
	public void addUnPlannedActivity(String activity, String topic, String entreprize,
									 String subcounty, String village, String ben_group, String reference,
									 String reference_contact, String males, String females, String remarks, String latitude, String longitude,  String parish, String district, String lessons, String recommendations, String description) {



		// Fetching user details from SQLite
		HashMap<String, String> user = this.getUserDetails();
		String uid =  user.get("uid");
		Integer user_id  = Integer.valueOf(uid);

		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		Integer idx = 1;

		Date date= new Date();

		java.util.Date dt = new java.util.Date();

		java.text.SimpleDateFormat sdf =
				new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String currentTime = sdf.format(dt);
		String b18 = getSerialNumber();

		ContentValues values = new ContentValues();
		values.put("a1", topic);
		values.put("a2", activity);
		values.put("a3", entreprize);
		values.put("a4", parish);
		values.put("a5", district);
		values.put("a6", subcounty);
		values.put("a7", village);
		values.put("a8", ben_group);
		values.put("a9", reference);
		values.put("a10", reference_contact);
		values.put("a11", males);
		values.put("a12", females);
		values.put("a13", remarks);
		values.put("a14", user_id);
		values.put("a15", latitude);
		values.put("a16", longitude);
		values.put("synced", 0);
		values.put("a17", currentTime);
		values.put("a18", b18);
		values.put("a20", 1); // planned or unplanned activity
		values.put("a21", lessons);
		values.put("a22", recommendations);
		values.put("a23", description);

		// Inserting Row
		long id = mDb.insert(TABLE_ACTIVITY, null, values);
		mDb.close(); // Closing database connection

		Log.d(TAG, "New Unplanned activity inserted into sqlite: " + id);
	}


	/**
	 * Storing user details in database
	 * */
	public void addQP(String activities, String topics, String entreprizes) {
		//SQLiteDatabase db = this.getWritableDatabase();
		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}


		ContentValues values = new ContentValues();
		values.put(KEY_TOPICS, topics);
		values.put(KEY_ENTREPRIZES, entreprizes);
		values.put(KEY_ACTIVITIES, activities);


		// Inserting Row
		long id = mDb.insert(TABLE_ACTIVITY_QUARTER, null, values);
		mDb.close(); // Closing database connection

		Log.d(TAG, "New user QP inserted into sqlite: " + id);
	}



	public void addNewWeatherInfo(String b1, String b2, String b3, String b4,
								  String b5, String b6, String b7, String b8,
								  String b9, String b10, String b11, String b12,
								  String b13, String b14, String b15, String b16,
								  String b17, String b18, String b19, String b20,
								  String b21, String b22, String b23, String b24) {




		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}


		ContentValues values = new ContentValues();
		values.put("id", b1);
		values.put("parish_id", b2);
		values.put("latitude", b3);
		values.put("longitude", b4);
		values.put("forecast_date", b5);
		values.put("maximum_temperature", b6);
		values.put("minimum_temperature", b7);
		values.put("average_temperature", b8);
		values.put("temperature_units", b9);
		values.put("rainfall_chance", b10);
		values.put("rainfall_amount", b11);
		values.put("rainfall_units", b12);
		values.put("windspeed_average", b13);
		values.put("windspeed_units", b14);
		values.put("wind_direction", b15);
		values.put("windspeed_maximum", b16);
		values.put("windspeed_minimum", b17);
		values.put("cloudcover", b18);
		values.put("sunshine_level", b19);
		values.put("soil_temperature", b20);
		values.put("created_at", b21);
		values.put("updated_at", b22);
		values.put("icon", b23);
		values.put("desc", b24);


		// Inserting Row
		long id = mDb.insert("weather_infomation", null, values);
		mDb.close(); // Closing database connection

		Log.d(TAG, "New Weather Information dropped: " + id);
	}


	/**
	 * Getting user data from database
	 * */
	public HashMap<String, String> getUserDetails() {
		HashMap<String, String> user = new HashMap<String, String>();
		String selectQuery = "SELECT  * FROM " + TABLE_USER;

		try {
			mDb = this.getReadableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		//SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = mDb.rawQuery(selectQuery, null);
		// Move to first row
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			user.put("first_name", cursor.getString(1));
			user.put("last_name", cursor.getString(2));
			user.put("username", cursor.getString(3));
			user.put("email", cursor.getString(4));
			user.put("password", cursor.getString(5));
			user.put("phone", cursor.getString(6));
			user.put("subcounty", cursor.getString(7));
			user.put("district", cursor.getString(8));
			user.put("user_category", cursor.getString(9));
			user.put("photo", cursor.getString(10));
			user.put("gender", cursor.getString(11));
			user.put("uid", cursor.getString(12));
			user.put("user_category_id", cursor.getString(13));
			user.put("created", cursor.getString(14));
			user.put("district_id", cursor.getString(15));
		}
		cursor.close();
		mDb.close();
		// return user
		Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

		return user;
	}
	/**
	 * Getting user data from database
	 * */
	public HashMap<String, String> getPlannedAcivity() {
		HashMap<String, String> activity = new HashMap<String, String>();
		String selectQuery = "SELECT  * FROM " + TABLE_ACTIVITY;

		try {
			mDb = this.getReadableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		//SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = mDb.rawQuery(selectQuery, null);
		// Move to first row
		cursor.moveToFirst();
		while (cursor.moveToNext()) {
			if (cursor.getCount() > 0) {
				activity.put("id", cursor.getString(0));
				activity.put("created", cursor.getString(1));
				activity.put("topic", cursor.getString(2));
				activity.put("gps_latitude", cursor.getString(3));
				activity.put("gps_longitude", cursor.getString(4));
				activity.put("entreprise", cursor.getString(5));
				activity.put("subcounty", cursor.getString(6));
				activity.put("village", cursor.getString(7));
				activity.put("notes", cursor.getString(8));
				activity.put("num_ben_males", cursor.getString(9));
				activity.put("num_ben_females", cursor.getString(10));
				activity.put("ben_ref_name", cursor.getString(11));
				activity.put("ben_ref_phone", cursor.getString(12));
				activity.put("ben_group", cursor.getString(13));
				activity.put("user_id", cursor.getString(14));
				activity.put("activity_id", cursor.getString(15));
			}

			Log.d(TAG, "Fetching activities from Sqlite: " + activity.toString());

		}
		cursor.close();
		mDb.close();
		// return user
		// Log.d(TAG, "Fetching activities from Sqlite: " + activity.toString());

		return activity;
	}
	/**
	 * Getting user data from database
	 * */
	public HashMap<String, String> getUserQuarterlyActivies() {
		HashMap<String, String> user = new HashMap<String, String>();
		String selectQuery = "SELECT  * FROM " + TABLE_ACTIVITY_QUARTER;

		try {
			mDb = this.getReadableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		//SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = mDb.rawQuery(selectQuery, null);
		// Move to first row
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			user.put("topics", cursor.getString(1));
			user.put("entreprizes", cursor.getString(2));
			user.put("activities", cursor.getString(3));
		}
		cursor.close();
		mDb.close();
		// return user
		Log.d(TAG, "Fetching user quarterly activities from Sqlite: " + user.toString());

		return user;
	}

	/**
	 * Re crate database Delete all tables and create them again
	 * */
	public void resetAllTables() {

		SQLiteDatabase db = this.getWritableDatabase();
		// Delete All Rows
		String whereClause = "synced = ?";
		String[] whereArgs = {"1"};
		// db.delete("mod_ediary", whereClause, whereArgs);
		db.delete("user", null, null);
		db.delete("user_quaterly_activities", null, null);
		db.delete("weather_infomation", null, null);
		db.delete("db_farmer_group", whereClause, whereArgs);
		db.delete("db_farmers", whereClause, whereArgs);
		db.delete("db_non_state_actors", whereClause, whereArgs);
		db.delete("db_state_actors", whereClause, whereArgs);
		db.delete("mod_advisory", whereClause, whereArgs);
		db.delete("mod_ediary", whereClause, whereArgs);
		db.delete("mod_crises_outbreaks", whereClause, whereArgs);
		db.delete("mod_grm", whereClause, whereArgs);
		db.delete("mod_weather_advisory", whereClause, whereArgs);
		db.close();

		Log.d(TAG, "Deleted all user info from sqlite");
	}


	public void resetWeatherTables() {

		SQLiteDatabase db = this.getWritableDatabase();
		// Delete All Rows
		db.delete("weather_infomation", null, null);
		db.delete("mod_weather_advisory", null, null);
		db.close();

		Log.d(TAG, "Deleted all user info from sqlite");

	}

	//	markets
	public List<String> getAllMarketNames(){
		List<String> names = new ArrayList<String>();
		names.add("---Select Markets---");
		// Select All Query
		String selectQuery = "SELECT  * FROM db_market ORDER BY a1 ASC";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				names.add(cursor.getString(1).toUpperCase());
			} while (cursor.moveToNext());
		}

		// closing connection
		cursor.close();
		db.close();

		// returning names
		return names;
	}

	// districts
	public List<String> getAllDistrictNames(){
		List<String> names = new ArrayList<String>();
		names.add("---Select District---");
		// Select All Query
		String selectQuery = "SELECT name FROM district Order by name asc";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				names.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}

		// closing connection
		cursor.close();
		db.close();

		// returning names
		return names;
	}

	// sync districts
	public List<String> getSyncDistrictNames(){
		List<String> ids = new ArrayList<String>();
		List<String> synced_district_names = new ArrayList<String>();
		List<String> names = new ArrayList<String>();
		names.add("---Select District---");

		// select ids of already synced districts
		String syncedDistrictsSelectQuery = "SELECT * FROM district";
		SQLiteDatabase db_sync = this.getReadableDatabase();
		Cursor cursor_sync = db_sync.rawQuery(syncedDistrictsSelectQuery, null);

		// looping through all rows and adding to list
		if (cursor_sync.moveToFirst()) {
			do {
				ids.add(cursor_sync.getString(0));
				synced_district_names.add(cursor_sync.getString(1).toUpperCase());
				Log.d(TAG, cursor_sync.getString(1).toUpperCase());
			} while (cursor_sync.moveToNext());
		}

		String selectQuery = "SELECT name FROM district2 ORDER BY name ASC";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				names.add(cursor.getString(0));
			} while (cursor.moveToNext());
		}

		// closing connection
		cursor.close();
		db.close();

		names.removeAll(synced_district_names);

		// returning names
		return names;
	}

	public List<String> getAllDistrictSubcounty(String district) {
		List<String> names = new ArrayList<>();
		names.add("---Select Subcounty---");

		// Trim district
		district = district.trim();
		district = district.toUpperCase();
		Log.d(TAG, "district: "+district);

		SQLiteDatabase db = this.getReadableDatabase();
		try {
			// Get subcounties from a district
			String sql = "SELECT s.name " +
					"FROM subcounty s " +
					"JOIN county c ON s.county_id = c.id " +
					"JOIN district d ON c.district_id = d.id " +
					"WHERE d.name = ? " +
					"ORDER BY s.name ASC";
			Cursor cursor = db.rawQuery(sql, new String[]{district});

			// Process results
			while (cursor.moveToNext()) {
				names.add(cursor.getString(0));
			}
			cursor.close();
		} catch (Exception e) {
			// Log error
			Log.e(TAG, "Database error", e);
		} finally {
			db.close();
		}

		return names;
	}

	//Get all subcounties in District
	public List<String> getAllSubcountyParish(String Dis, String subcounty) {
		List<String> names = new ArrayList<>();
		names.add("---Select Parish---");
		Dis = Dis.toUpperCase();
		subcounty = subcounty.toUpperCase();
		String selectQuery = "SELECT parish.name FROM district, county, subcounty, parish " +
				"WHERE subcounty.name = ? AND district.name = ? AND " +
				"district.id = county.district_id AND county.id = subcounty.county_id AND " +
				"subcounty.id = parish.subcounty_id";

		try (SQLiteDatabase db = this.getReadableDatabase();
			 Cursor cursor = db.rawQuery(selectQuery, new String[] {subcounty, Dis})) {
			while (cursor.moveToNext()) {
				names.add(cursor.getString(0));
			}
		}
		return names;
	}

	public List<String> getAllSubcountyParish2( String Dis,String subcounty){
		List<String> ids = new ArrayList<String>();
		List<String> names = new ArrayList<String>();
		names.add("---Select Parish---");
		Integer id = 0 ;
		String[] ids_final;


		// Get District ID
		//String selectQuery = "SELECT  subcounty.id FROM district,county,subcounty WHERE  district.name  LIKE '"+ Dis + "'  AND subcounty.name  LIKE '"+ subcounty + "' AND district.id=county.district_id AND county.id = subcounty.county_id";
		String selectQuery = "SELECT subcounty.id FROM district, county, subcounty WHERE subcounty.name = '"+subcounty+"' AND  district.name = '"+ Dis + "' AND district.id=county.district_id AND county.id = subcounty.county_id";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				id = cursor.getInt(0);
			} while (cursor.moveToNext());
		}



		// Get all parishes in Given District
		String selectQuery2 = "SELECT  * FROM parish WHERE subcounty_id =" + id;

		Cursor cursor2 = db.rawQuery(selectQuery2, null);

		// looping through all rows and adding to list
		if (cursor2.moveToFirst()) {
			do {
				names.add(cursor2.getString(1));
			} while (cursor2.moveToNext());
		}

		// closing connection
		cursor2.close();
		db.close();

		// returning names
		return names;
	}

	//Get all villages within the parish
	public List<String> getAllVillages( String parish, String subcounty, String district){
		List<String> names = new ArrayList<String>();
		Integer id = 0 ;
		names.add("---Select Village---");

		// Get parish ID
		String selectQuery = "SELECT  parish.id FROM subcounty, parish WHERE  subcounty.name  LIKE '"+ subcounty + "' " +
				"   AND parish.name LIKE  '"+ parish +"' AND parish.subcounty_id =subcounty.id";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				id = cursor.getInt(0);
			} while (cursor.moveToNext());
		}


		// Get all Counties in Given District
		String selectQuery3 = "SELECT  * FROM village WHERE parish_id = "+ id + " ORDER BY name ASC";

		Cursor cursor3 = db.rawQuery(selectQuery3, null);

		// looping through all rows and adding to list
		if (cursor3.moveToFirst()) {
			do {
				names.add(cursor3.getString(1));
			} while (cursor3.moveToNext());
		}


		// closing connection
		//cursor3.close();
		db.close();

		// returning names
		return names;
	}


	//Get all subcounties in District
	public List<String> getAllActivites(){
		List<String> names = new ArrayList<String>();
		String  filter = " ";
		names.add("---Select Activity---");

		// Fetching user details from SQLite
		HashMap<String, String> user = this.getUserDetails();
		String user_category_id =  user.get("user_category_id");

		Integer user_cat  = Integer.valueOf(user_category_id);

		switch(user_cat){

			//Agricultural Stuff
			case 8:
			case 19:


				filter = " WHERE category LIKE 1 OR category LIKE 2 ";
				break;

			//Vet stuff
			case 9:
			case 20:
			case 23:
				filter = "  WHERE category LIKE 1 OR category LIKE 3  ";
				break;

			//Fish stuff
			case 7:
			case 21:
				filter = " WHERE category LIKE 1 OR category LIKE 4 ";
				break;
			//Entomologist
			case 12:
				filter = " WHERE category LIKE 1 OR category LIKE 5 ";
				break;

			//Agric Engineer
			case 22:
				filter = " WHERE category LIKE 1 OR category LIKE 6 ";
				break;

			//DPMO,DAO,DVO,DFO
			case 2:
			case 3:
			case 4:
			case 10:

				filter = " WHERE  category LIKE 7 ";
				break;


			default:
				filter = " ";
				break;

		}

		// Get Subcounty ID
		String selectQuery = "SELECT  * FROM activities " + filter + "  ORDER BY name ASC  ";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				names.add(cursor.getString(1));
			} while (cursor.moveToNext());
		}

		// closing connection
		cursor.close();
		db.close();

		// returning names
		return names;
	}


	//Get all subcounties in District
	public List<String> getAllActivitesDaily(){
		List<String> names = new ArrayList<String>();
		String  filter = " ";
		names.add("---Select Activity---");

		// Fetching user details from SQLite
		HashMap<String, String> user = this.getUserQuarterlyActivies();
		String activities =  user.get("activities");

		// Get Subcounty ID
		String selectQuery = "SELECT  * FROM activities WHERE id IN ( " + activities +")  ORDER BY name ASC";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				names.add(cursor.getString(1));
			} while (cursor.moveToNext());
		}

		// closing connection
		cursor.close();
		db.close();

		// returning names
		return names;
	}


	//Get all subcounties in District
	public List<String> getAllTopics(){
		List<String> names = new ArrayList<String>();
		String  filter = " ";
		names.add("---Select Topic---");

		// Fetching user details from SQLite
		HashMap<String, String> user = this.getUserDetails();
		String user_category_id =  user.get("user_category_id");

		Integer user_cat  = Integer.valueOf(user_category_id);

		switch(user_cat){

			//Agricultural Stuff
			case 2:
			case 8:
			case 19:

				filter = " WHERE category LIKE 1 OR category LIKE 2 ";
				break;

			//Vet stuff
			case 3:
			case 9:
			case 20:
			case 23:
				filter = "  WHERE category LIKE 1 OR category LIKE 3  ";
				break;

			//Fish stuff
			case 4:
			case 7:
			case 21:
				filter = " WHERE category LIKE 1 OR category LIKE 4 ";
				break;

			//Entomologist
			case 12:
				filter = " WHERE category LIKE 1 OR category LIKE 5 ";
				break;

			//Agric Engineer
			case 22:
				filter = " WHERE category LIKE 1 OR category LIKE 6 ";
				break;


			default:
				filter = " ";
				break;

		}

		// Get Subcounty ID
		String selectQuery = "SELECT  * FROM topics " + filter + "  ORDER BY name ASC  ";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				names.add(cursor.getString(1));
			} while (cursor.moveToNext());
		}

		// closing connection
		cursor.close();
		db.close();

		// returning names
		return names;
	}
	//Get all subcounties in District
	public List<String> getAllTopicsDaily(){
		List<String> names = new ArrayList<String>();
		String  filter = " ";
		names.add("---Select Topic---");

		// Fetching user details from SQLite
		HashMap<String, String> user = this.getUserQuarterlyActivies();
		String topics =  user.get("topics");

		// Get Subcounty ID
		String selectQuery = "SELECT  * FROM topics WHERE id IN ( " + topics +")  ORDER BY name ASC  ";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				names.add(cursor.getString(1));
			} while (cursor.moveToNext());
		}

		// closing connection
		cursor.close();
		db.close();

		// returning names
		return names;
	}
	//Get all subcounties in District
	public List<String> getAllEntreprizes(){
		List<String> names = new ArrayList<String>();
		String  filter = " ";
		names.add("---Select Entreprize---");

		// Fetching user details from SQLite
		HashMap<String, String> user = this.getUserDetails();
		String user_category_id =  user.get("user_category_id");

		Integer user_cat  = Integer.valueOf(user_category_id);

		switch(user_cat){

			//Agricultural Stuff
			case 2:
			case 8:
			case 19:
			case 22:


				filter = " WHERE parent_id IN (4,5,6,7,8,9,10,11) OR id LIKE 198  ";
				break;

			//Vet stuff
			case 3:
			case 9:
			case 20:
			case 23:
				filter = "  WHERE parent_id IN (78,149,161,162) OR id IN (198,79,80,81,82,85,86,151,152,153,160) ";
				break;

			//Fish stuff
			case 4:
			case 7:
			case 21:
				filter = " WHERE id LIKE 198 OR parent_id =3 ";
				break;


			//Entomologist
			case 12:
				filter = " WHERE id LIKE 198 OR parent_id = 201 ";
				break;


			default:
				filter = " WHERE id LIKE 198 OR parent_id != 1 OR parent_id !=2 OR parent_id  !=3  OR parent_id != 103 OR parent_id != 104 OR parent_id != 106 OR parent_id != 108 ";
				break;

		}

		// Get Subcounty ID
		//	String selectQuery = "SELECT  * FROM entreprizes " + filter + "  ORDER BY name ASC  ";
		String selectQuery = "SELECT  * FROM entreprizes  ORDER BY name ASC  ";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				names.add(cursor.getString(1));
			} while (cursor.moveToNext());
		}

		// closing connection
		cursor.close();
		db.close();

		// returning names
		return names;
	}
	//Get all subcounties in District
	public List<String> getAllEntreprizesDaily(){
		List<String> names = new ArrayList<String>();
		String  filter = " ";
		names.add("---Select Entreprize---");

		// Fetching user details from SQLite
		HashMap<String, String> user = this.getUserQuarterlyActivies();
		String entreprizes =  user.get("entreprizes");

		// Get Subcounty ID

		//String selectQuery = "SELECT  * FROM entreprizes WHERE id IN ( " + entreprizes +")  ORDER BY name ASC  ";
		String selectQuery = "SELECT  * FROM entreprizes WHERE 1  ORDER BY name ASC  ";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				names.add(cursor.getString(1));
			} while (cursor.moveToNext());
		}

		// closing connection
		cursor.close();
		db.close();

		// returning names
		return names;
	}

	public List<String> getAllEntreprizesList(){
		List<String> names = new ArrayList<String>();
		String  filter = " ";
		names.add("---Select Entreprize---");


		// Get Subcounty ID
		String selectQuery = "SELECT  * FROM entreprizes  ORDER BY name ASC  ";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				names.add(cursor.getString(1));
			} while (cursor.moveToNext());
		}

		// closing connection
		cursor.close();
		db.close();

		// returning names
		return names;
	}

	final public static String implode(String glue, List<String> array)
	{
		boolean first = true;
		StringBuilder str = new StringBuilder();
		for (String s : array) {
			if (!first) str.append(glue);
			str.append(s);
			first = false;
		}
		return str.toString();
	}


	/*
	 * This method taking two arguments
	 * first one is the id of the activity for which
	 * we have to update the sync status
	 * and the second one is the status that will be changed
	 * */
	public boolean updateActivityStatus(int id, int status, String a19) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_SYNCED, status);
		contentValues.put("a19", a19);
		db.update(TABLE_ACTIVITY, contentValues, KEY_ID + "=" + id, null);
		db.close();
		return true;
	}

	public boolean updateCrisisStatus(int id, int status, String a21) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_SYNCED, status);
		contentValues.put("a21", a21);
		db.update("mod_crises_outbreaks", contentValues, KEY_ID + "=" + id, null);
		db.close();
		return true;
	}

	public boolean updateMarketStatus(int id, int status, String a19) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_SYNCED, status);
		contentValues.put("a19", a19);
		db.update("db_market", contentValues, KEY_ID + "=" + id, null);
		db.close();
		return true;
	}


	public boolean updatePriceVolumeStatus(int id, int status, String a12) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_SYNCED, status);
		contentValues.put("a12", a12);
		db.update("mod_market_price_volume", contentValues, KEY_ID + "=" + id, null);
		db.close();
		return true;
	}

	/*
	 * this method will give us all the activities stored in sqlite
	 * */
	public Cursor getActivities() {
		SQLiteDatabase db = this.getReadableDatabase();
		String sql = "SELECT * FROM " + TABLE_ACTIVITY + " ORDER BY " + KEY_ID + " DESC LIMIT 10";
		Cursor c = db.rawQuery(sql, null);
		return c;
	}


	public Cursor getOutbreakCrisis() {
		SQLiteDatabase db = this.getReadableDatabase();
		String sql = "SELECT * FROM mod_crises_outbreaks ORDER BY " + KEY_ID + " DESC";
		Cursor c = db.rawQuery(sql, null);
		return c;
	}

	@RequiresApi(api = Build.VERSION_CODES.O)
	public Cursor getWeatherData(String district, String subcounty, String parish, String type) {
		SQLiteDatabase db = this.getReadableDatabase();
		Integer id = 0 ;

		// Get Parish ID
		String selectQuery = "SELECT  id FROM weather_parishes WHERE  District  LIKE '"+ district + "'  AND Subcounty  LIKE '"+ subcounty + "' AND Parish LIKE '"+ parish+ "'";

		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				id = cursor.getInt(0);
			} while (cursor.moveToNext());
		}

		String limit_type = " ";

		if(type.startsWith("Next 7"))
		{
			LocalDate today = LocalDate.now();
			LocalDate tomorrow = today.plusDays(6);
			limit_type = " AND DATE(forecast_date) BETWEEN '"+today+"' AND '"+tomorrow+"'  ";
		} else if(type.startsWith("Next 14"))
		{
			LocalDate today = LocalDate.now();
			LocalDate tomorrow = today.plusDays(14);
			limit_type = " AND DATE(forecast_date) BETWEEN '"+today+"' AND '"+tomorrow+"'  ";
		}
		else if(type.startsWith("Last 7"))
		{
			LocalDate today = LocalDate.now();
			LocalDate tomorrow = today.minusDays(7);
			limit_type = " AND DATE(forecast_date) BETWEEN '"+tomorrow+"' AND '"+today+"'  ";
		}

		//TODO debug
		Log.d(TAG, limit_type);

		String sql = "SELECT * FROM weather_infomation WHERE parish_id="+ id +" "+limit_type+" ORDER BY DATE(forecast_date) ASC LIMIT 14";
		Cursor c = db.rawQuery(sql, null);
		return c;
	}


	public Cursor getMarkets() {
		SQLiteDatabase db = this.getReadableDatabase();
		String sql = "SELECT * FROM db_market ORDER BY " + KEY_ID + " DESC";
		Cursor c = db.rawQuery(sql, null);
		return c;
	}

	public Cursor getPricesVolumes() {
		SQLiteDatabase db = this.getReadableDatabase();
		String sql = "SELECT * FROM mod_market_price_volume ORDER BY " + KEY_ID + " DESC";
		Cursor c = db.rawQuery(sql, null);
		return c;
	}

	/*
	 * this method will give us all the activities stored in sqlite
	 * */
	public Cursor getActivitiesRecent() {
		SQLiteDatabase db = this.getReadableDatabase();
		String sql = "SELECT * FROM activity_daily ORDER BY id DESC LIMIT 2";
		Cursor c = db.rawQuery(sql, null);
		return c;
	}


	/*
	 * this method is for getting all the unsynced activities
	 * so that we can sync it with database
	 * */
	public Cursor getUnsyncedActivities() {
		SQLiteDatabase db = this.getReadableDatabase();
		String sql = "SELECT * FROM " + TABLE_ACTIVITY + " WHERE synced=0;";
		Cursor c = db.rawQuery(sql, null);
		return c;
	}

	public Cursor getUnsyncedOutbreaks() {
		SQLiteDatabase db = this.getReadableDatabase();
		String sql = "SELECT * FROM mod_crises_outbreaks WHERE synced=0;";
		Cursor c = db.rawQuery(sql, null);
		return c;
	}

	public Cursor getUnsyncedMarkets() {
		SQLiteDatabase db = this.getReadableDatabase();
		String sql = "SELECT * FROM db_market WHERE synced=0;";
		Cursor c = db.rawQuery(sql, null);
		return c;
	}

	public Cursor getUnsyncedPricesVolumes() {
		SQLiteDatabase db = this.getReadableDatabase();
		String sql = "SELECT * FROM mod_market_price_volume WHERE synced=0;";
		Cursor c = db.rawQuery(sql, null);
		return c;
	}

	/**
	 * Re crate database Delete all tables and create them again
	 * */
	public void deleteUsersAndFarmers() {

		SQLiteDatabase db = this.getWritableDatabase();
		// Delete All Rows
		db.delete("user", null, null);
		db.delete("farmer", null, null);
		db.close();
		Log.d(TAG, "Deleted all user info from sqlite");

	}
	/*
	 * this method is for getting all the unsynced farmers
	 * so that we can sync it with database
	 * */

	// delete farmer profile
	public void deleteFarmerProfile(int farmerId) {
		SQLiteDatabase db = this.getWritableDatabase();
		String whereClause = "id = ?";
		String[] whereArgs = {String.valueOf(farmerId)};
		db.delete("farmer", whereClause, whereArgs);
		db.close();
	}

	// save farmer to db
	public List<Map<String, Object>> addFarmer(String a1, String a2, String a3, String a4, String a5, String a6, String a7, String a8, String a9, String a9x, String a10, String a11, String a12,  String a13, String a14, String a15, String a16, String a17, String a18, int synced, int farmer_category, int education_level, int primary_language, int secondary_language) {
		List<Map<String, Object>> res = new ArrayList<>();

		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		// read db
		SQLiteDatabase dbRead = getReadableDatabase();

		if (synced == 0) {
			// check if NIN already exists
			 try{
				 String queryNIN = "SELECT COUNT(*) FROM farmer WHERE a7 = ?";
				 String[] selectionArgsNIN = { a7 };
				 Cursor cursorNIN = dbRead.rawQuery(queryNIN, selectionArgsNIN);
				 int countNIN = 0;
				 if (cursorNIN.moveToFirst()) {
					 countNIN = cursorNIN.getInt(0);
				 }
				 cursorNIN.close();
				 if(countNIN != 0){
					 dbRead.close(); // Closing read database connection
					 mDb.close(); // Closing write database connection

					 Map<String, Object> ninRes = new HashMap<>();
					 ninRes.put("message", "NIN number already exists with another farmer profile");
					 ninRes.put("count", countNIN);
					 ninRes.put("success", false);
					 res.add(ninRes);

					 return res;
				 }
			 } catch (SQLException e) {
				 Log.d(TAG, "Exception error: "+e.getMessage());
				 Map<String, Object> errRes = new HashMap<>();
				 errRes.put("message", "Error checking NIN..."+e.getMessage());
				 errRes.put("success", false);
				 res.add(errRes);
			 }

			// check if contact already exists
			try {
				String queryContact = "SELECT COUNT(*) FROM farmer WHERE a5 = ?";
				String[] selectionArgsContact = { a5 };
				Cursor cursorContact = dbRead.rawQuery(queryContact, selectionArgsContact);
				int countContact = 0;
				if (cursorContact.moveToFirst()) {
					countContact = cursorContact.getInt(0);
				}
				cursorContact.close();
				if(countContact != 0){
					dbRead.close(); // Closing read database connection
					mDb.close(); // Closing write database connection

					Map<String, Object> contactRes = new HashMap<>();
					contactRes.put("message", "Phone number already exists with another farmer profile");
					contactRes.put("count", countContact);
					contactRes.put("success", false);
					res.add(contactRes);

					return res;
				}
			} catch (SQLException e) {
				Log.d(TAG, "Exception error: "+e.getMessage());
				Map<String, Object> errRes = new HashMap<>();
				errRes.put("message", "Error checking contact..."+e.getMessage());
				errRes.put("success", false);
				res.add(errRes);
			}

			// check if email already belongs to another farmer
			if (!a6.isEmpty()){
				try {
					String queryEmail = "SELECT COUNT(*) FROM farmer WHERE a6 = ?";
					String[] selectionArgsEmail = { a6 };
					Cursor cursorEmail = dbRead.rawQuery(queryEmail, selectionArgsEmail);
					int countEmail = 0;
					if (cursorEmail.moveToFirst()) {
						countEmail = cursorEmail.getInt(0);
					}
					cursorEmail.close();
					if(countEmail != 0){
						dbRead.close(); // Closing read database connection
						mDb.close(); // Closing write database connection

						Map<String, Object> emailRes = new HashMap<>();
						emailRes.put("message", "Email address already exists with another farmer profile");
						emailRes.put("count", countEmail);
						emailRes.put("success", false);
						res.add(emailRes);

						return res;
					}
				} catch (SQLException e) {
					Log.d(TAG, "Exception error: "+e.getMessage());
					Map<String, Object> errRes = new HashMap<>();
					errRes.put("message", "Error checking email...");
					errRes.put("success", false);
					res.add(errRes);
				}
			}

			// get parish id
			int district_id = getIdByName(a10, "district");
			int county_id = getCountyIdBySubCountyName(a11);
			int subcounty_id = getIdByName(a11, county_id, "county_id", "subcounty");
			int parish_id = getIdByName(a12, subcounty_id, "subcounty_id", "parish");
			int village_id = getIdByName(a13, parish_id, "parish_id", "village");

			// get enterprise id
			int enterprise_one = getIdByName(a14, "entreprizes");
			int enterprise_two = getIdByName(a15, "entreprizes");
			int enterprise_three = getIdByName(a16, "entreprizes");

			String gender = "M";
			if (a4 == "Female")
				gender = "F";

			ContentValues values = new ContentValues();
			values.put("a1", a1); // first name
			values.put("a2", a2); // last name
			values.put("a3", a3); // farmer type
			values.put("a4", a4); // gender
			values.put("a5", a5); // contact
			values.put("a6", a6); // email
			values.put("a7", a7); // NIN
			values.put("a8", a8); // education level
			values.put("a9", a9); // primary language
			values.put("a9x", a9x); // secondary language
			values.put("a10", a10); // district
			values.put("a11", a11); // subcounty
			values.put("a12", a12); // parish
			values.put("a13", a13); // village
			values.put("a14", a14); // ent one
			values.put("a15", a15); // ent two
			values.put("a16", a16); // ent three
			values.put("a17", a17); // farmer belongs to a farmer group?
			values.put("a18", a18); // farmer group name
			values.put("synced", synced);
			values.put("primary_language", primary_language);
			values.put("secondary_language", secondary_language);
			values.put("education_level", education_level);
			values.put("farmer_type", farmer_category);
			values.put("district_id", district_id);
			values.put("subcounty_id", subcounty_id);
			values.put("parish_id", parish_id);
			values.put("village_id", village_id);
			values.put("enterprise_one", enterprise_one);
			values.put("enterprise_two", enterprise_two);
			values.put("enterprise_three", enterprise_three);
			values.put("gender", gender);

			// Inserting Row
			// long id = mDb.insert("farmer", null, values);
			try {
				mDb.insert("farmer", null, values);
				dbRead.close(); // Closing database connection
				mDb.close(); // Closing database connection

				Map<String, Object> successRes = new HashMap<>();
				successRes.put("message", "Farmer profile added successfully...");
				successRes.put("success", true);
				res.add(successRes);

				Log.d(TAG, "New farmer profile inserted into the database successfully...");
			} catch (SQLException e) {
				Log.d(TAG, "Exception error: "+e.getMessage());
				Map<String, Object> errRes = new HashMap<>();
				errRes.put("message", "Error adding farmer profile...");
				errRes.put("success", false);
				res.add(errRes);
			}
		}

		return res;
	}

	public Cursor getUnsyncedFarmers() {
		SQLiteDatabase db = this.getReadableDatabase();
		String sql = "SELECT * FROM farmer WHERE synced = 0";
		Cursor c = db.rawQuery(sql, null);
		return c;
	}

	public Cursor getFarmers() {
		// Fetching user details from SQLite
		HashMap<String, String> user = this.getUserDetails();
		String id =  user.get("uid");

		SQLiteDatabase db = this.getReadableDatabase();
		String sql = "SELECT * FROM farmer ORDER BY  id ASC";
		Cursor c = db.rawQuery(sql, null);
		return c;
	}
	// String sql = "SELECT * FROM farmer WHERE a21='"+ id  +"' ORDER BY  id ASC;";

	public boolean updateSyncedFarmerProfile(int id, int status, String reason) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_SYNCED, status);
		contentValues.put("reason", reason);
		db.update("farmer", contentValues, KEY_ID + "=" + id, null);
		db.close();
		return true;
	}

	public List<Map<String, Object>> farmerStats(){
		List<Map<String, Object>> res = new ArrayList<>();
		Map<String, Object> obj = new HashMap<>();

		// read db
		SQLiteDatabase dbRead = getReadableDatabase();

		// unsynced
		String queryUnsynced = "SELECT COUNT(*) FROM farmer WHERE synced = 0";
		Cursor cursorUnsynced = dbRead.rawQuery(queryUnsynced, null);
		int unsynced_count = 0;
		if (cursorUnsynced.moveToFirst()) {
			unsynced_count = cursorUnsynced.getInt(0);
		}
		obj.put("unsynced", unsynced_count);

		// synced
		String querySynced = "SELECT COUNT(*) FROM farmer WHERE synced = 1";
		Cursor cursorSynced = dbRead.rawQuery(querySynced, null);
		int synced_count = 0;
		if (cursorSynced.moveToFirst()) {
			synced_count = cursorSynced.getInt(0);
		}
		obj.put("synced", synced_count);

		// synced but failed
		String queryFailedSynced = "SELECT COUNT(*) FROM farmer WHERE synced = 2";
		Cursor cursorFailedSynced = dbRead.rawQuery(queryFailedSynced, null);
		int failed_synced_count = 0;
		if (cursorFailedSynced.moveToFirst()) {
			failed_synced_count = cursorFailedSynced.getInt(0);
		}
		obj.put("failed_synced", failed_synced_count);

		int total_farmer = unsynced_count + synced_count + failed_synced_count;
		obj.put("all", total_farmer);

		res.add(obj);

		// close db connection
		dbRead.close();

		return res;
	}

	/** farmer groups */
	// all farmers groups
	public Cursor getFarmerGroups() {
		// Fetching user details from SQLite
		HashMap<String, String> user = this.getUserDetails();
		String id =  user.get("uid");

		SQLiteDatabase db = this.getReadableDatabase();
		String sql = "SELECT * FROM db_farmer_group ORDER BY  id ASC;";
		Cursor c = db.rawQuery(sql, null);
		return c;
	}

	// unsynced farmer groups
	public Cursor getUnsyncedFarmerGroups() {
		SQLiteDatabase db = this.getReadableDatabase();
		String sql = "SELECT * FROM db_farmer_group WHERE synced = 0";
		Cursor c = db.rawQuery(sql, null);
		return c;
	}

	public int getUnsyncedFarmerGroupsCount() {
		String countQuery = "SELECT * FROM db_farmer_group WHERE synced = 0";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}

	// stats farmer groups
	public List<Map<String, Object>> farmerGroupsStats(){
		List<Map<String, Object>> res = new ArrayList<>();
		Map<String, Object> obj = new HashMap<>();

		// read db
		SQLiteDatabase dbRead = getReadableDatabase();

		// unsynced
		String queryUnsynced = "SELECT COUNT(*) FROM db_farmer_group WHERE synced = 0";
		Cursor cursorUnsynced = dbRead.rawQuery(queryUnsynced, null);
		int unsynced_count = 0;
		if (cursorUnsynced.moveToFirst()) {
			unsynced_count = cursorUnsynced.getInt(0);
		}
		obj.put("unsynced", unsynced_count);

		// synced
		String querySynced = "SELECT COUNT(*) FROM db_farmer_group WHERE synced = 1";
		Cursor cursorSynced = dbRead.rawQuery(querySynced, null);
		int synced_count = 0;
		if (cursorSynced.moveToFirst()) {
			synced_count = cursorSynced.getInt(0);
		}
		obj.put("synced", synced_count);

		int total_farmer_groups = unsynced_count + synced_count;
		obj.put("all", total_farmer_groups);

		res.add(obj);

		// close db connection
		dbRead.close();

		return res;
	}

	public List<Map<String, Object>> addFarmersGroup(String groupName, String category, String level_of_operation, String email, String phone_number, String contact_person_name, String district, String subcounty, String parish, String village, String enterprise, String isRegistered, String regId,  String number_of_members, String latitude, String longitude, String year_of_establishment, int synced) {
		List<Map<String, Object>> res = new ArrayList<>();

		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		// read db
		SQLiteDatabase dbRead = getReadableDatabase();

		if (synced == 0)
		{
			// check if name already exists
			try{
				String queryName = "SELECT COUNT(*) FROM db_farmer_group WHERE a1 = ?";
				String[] selectionArgsName = { groupName };
				Cursor cursorName = dbRead.rawQuery(queryName, selectionArgsName);
				int countName = 0;
				if (cursorName.moveToFirst()) {
					countName = cursorName.getInt(0);
				}
				cursorName.close();
				if(countName != 0){
					dbRead.close(); // Closing read database connection
					mDb.close(); // Closing write database connection

					Map<String, Object> nameRes = new HashMap<>();
					nameRes.put("message", "Group name already exists");
					nameRes.put("count", countName);
					nameRes.put("success", false);
					res.add(nameRes);

					return res;
				}
			} catch (SQLException e) {
				Log.d(TAG, "Exception error: "+e.getMessage());
				Map<String, Object> errRes = new HashMap<>();
				errRes.put("message", "Error checking group name..."+e.getMessage());
				errRes.put("success", false);
				res.add(errRes);
			}

			// check if registration id
			if (regId != "")
			{
				try {
					String queryRegId = "SELECT COUNT(*) FROM db_farmer_group WHERE a13 = ?";
					String[] selectionArgsRegId = { regId };
					Cursor cursorRegId = dbRead.rawQuery(queryRegId, selectionArgsRegId);
					int countRegId = 0;
					if (cursorRegId.moveToFirst()) {
						countRegId = cursorRegId.getInt(0);
					}
					cursorRegId.close();
					if(countRegId != 0){
						dbRead.close(); // Closing read database connection
						mDb.close(); // Closing write database connection

						Map<String, Object> regIDRes = new HashMap<>();
						regIDRes.put("message", "Registration ID already exists...");
						regIDRes.put("count", countRegId);
						regIDRes.put("success", false);
						res.add(regIDRes);

						return res;
					}
				} catch (SQLException e) {
					Log.d(TAG, "Exception error: "+e.getMessage());
					Map<String, Object> errRes = new HashMap<>();
					errRes.put("message", "Error group registration ID email...");
					errRes.put("success", false);
					res.add(errRes);
				}
			}

			// get parish id
			int district_id = getIdByName(district, "district");
			int county_id = getCountyIdBySubCountyName(subcounty);
			int subcounty_id = getIdByName(subcounty, county_id, "county_id", "subcounty");
			int parish_id = getIdByName(parish, subcounty_id, "subcounty_id", "parish");

			// get enterprise id
			int enterprise_id = getIdByName(enterprise, "entreprizes");

			ContentValues values = new ContentValues();
			values.put("a1", groupName);
			values.put("a2", category);
			values.put("a3", level_of_operation);
			values.put("a4", email);
			values.put("a5", phone_number);
			values.put("a6", contact_person_name);
			values.put("a7", district);
			values.put("a8", subcounty);
			values.put("a9", parish);
			values.put("a10", village);
			values.put("a11", enterprise);
			values.put("a12", isRegistered);
			values.put("a13", regId);
			values.put("a14", number_of_members);
			values.put("a15", latitude);
			values.put("a16", longitude);
			values.put("year_of_establishment", year_of_establishment);
			values.put("parish_id", parish_id);
			values.put("enterprise_id", enterprise_id);
			values.put("synced", synced);

			// Inserting Row
			try {
				mDb.insert("db_farmer_group", null, values);
				dbRead.close(); // Closing database connection
				mDb.close(); // Closing database connection

				Map<String, Object> successRes = new HashMap<>();
				successRes.put("message", "Farmer group added successfully...");
				successRes.put("success", true);
				res.add(successRes);

				Log.d(TAG, "New farmer group inserted into the database successfully...");
			} catch (SQLException e) {
				Log.d(TAG, "Exception error: "+e.getMessage());
				Map<String, Object> errRes = new HashMap<>();
				errRes.put("message", "Error adding farmer group...");
				errRes.put("success", false);
				res.add(errRes);
			}
		}

		return res;
	}

	// get county
	public int getCountyIdBySubCountyName(String name) {
		SQLiteDatabase db = this.getReadableDatabase();
		String query = "SELECT county_id FROM subcounty WHERE name = ?";
		String[] selectionArgs = new String[]{name};

		Cursor cursor = null;
		int id = -1;

		try {
			cursor = db.rawQuery(query, selectionArgs);

			if (cursor.moveToFirst()) {
				id = cursor.getInt(0);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return id;
	}

	// get id by name
	public int getIdByName(String name, String tbl) {
		return getIdByName(name, null, "", tbl);
	}

	public int getIdByName(String name, Integer cdnId, String cdnCol, String tbl) {
		SQLiteDatabase db = this.getReadableDatabase();
		String query;
		String[] selectionArgs;

		if (cdnId != null && !cdnCol.isEmpty()) {
			query = "SELECT id FROM " + tbl + " WHERE name = ? AND " + cdnCol + " = ?";
			selectionArgs = new String[]{name, cdnId.toString()};
		} else {
			query = "SELECT id FROM " + tbl + " WHERE name = ?";
			selectionArgs = new String[]{name};
		}

		Cursor cursor = null;
		int id = -1;

		try {
			cursor = db.rawQuery(query, selectionArgs);

			if (cursor.moveToFirst()) {
				id = cursor.getInt(0);
			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

		return id;
	}

	private String[] addElementToArray(String[] array, String newElement) {
		// Create a new array with an increased size
		String[] newArray = new String[array.length + 1];

		// Copy the existing elements to the new array
		System.arraycopy(array, 0, newArray, 0, array.length);

		// Add the new element to the new array
		newArray[array.length] = newElement;

		// Return the new array
		return newArray;
	}

	// updated synced farmer group
	public boolean updateSyncedFarmerGroup(int id, int status) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_SYNCED, status);
		db.update("db_farmer_group", contentValues, KEY_ID + "=" + id, null);
		db.close();
		return true;
	}

	public Cursor getOtherMarketPlayers() {
		SQLiteDatabase db = this.getReadableDatabase();
		String sql = "SELECT * FROM db_non_state_actors ORDER BY  id ASC;";
		Cursor c = db.rawQuery(sql, null);
		return c;
	}

	// get unsynced other market players
	public Cursor getUnsyncedOtherMarketPlayers() {
		SQLiteDatabase db = this.getReadableDatabase();
		String sql = "SELECT * FROM db_non_state_actors WHERE synced = 0";
		Cursor c = db.rawQuery(sql, null);
		return c;
	}

	public List<Map<String, Object>> otherPlayersStats(){
		List<Map<String, Object>> res = new ArrayList<>();
		Map<String, Object> obj = new HashMap<>();

		// read db
		SQLiteDatabase dbRead = getReadableDatabase();

		// unsynced
		String queryUnsynced = "SELECT COUNT(*) FROM db_non_state_actors WHERE synced = 0";
		Cursor cursorUnsynced = dbRead.rawQuery(queryUnsynced, null);
		int unsynced_count = 0;
		if (cursorUnsynced.moveToFirst()) {
			unsynced_count = cursorUnsynced.getInt(0);
		}
		obj.put("unsynced", unsynced_count);

		// synced
		String querySynced = "SELECT COUNT(*) FROM db_non_state_actors WHERE synced = 1";
		Cursor cursorSynced = dbRead.rawQuery(querySynced, null);
		int synced_count = 0;
		if (cursorSynced.moveToFirst()) {
			synced_count = cursorSynced.getInt(0);
		}
		obj.put("synced", synced_count);

		int total = unsynced_count + synced_count;
		obj.put("all", total);

		res.add(obj);

		// close db connection
		dbRead.close();

		return res;
	}

	public List<Map<String, Object>> addOtherPlayer(String a1, String a2, String a3, String a4, String a5, String a6, String a7, String a8, String a10, String a11, String a12, String a13, String a14,  String a15, String a16, int synced) {
		List<Map<String, Object>> res = new ArrayList<>();

		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		// read db
		SQLiteDatabase dbRead = getReadableDatabase();

		if (synced == 0) {
			// check if name already exists
			try{
				String queryName = "SELECT COUNT(*) FROM db_non_state_actors WHERE a1 = ?";
				String[] selectionArgsName = { a1 };
				Cursor cursorName = dbRead.rawQuery(queryName, selectionArgsName);
				int countName = 0;
				if (cursorName.moveToFirst()) {
					countName = cursorName.getInt(0);
				}
				cursorName.close();
				if(countName != 0){
					dbRead.close(); // Closing read database connection
					mDb.close(); // Closing write database connection

					Map<String, Object> nameRes = new HashMap<>();
					nameRes.put("message", "Organisation name already exists");
					nameRes.put("count", countName);
					nameRes.put("success", false);
					res.add(nameRes);

					return res;
				}
			} catch (SQLException e) {
				Log.d(TAG, "Exception error: "+e.getMessage());
				Map<String, Object> errRes = new HashMap<>();
				errRes.put("message", "Error checking organisation name..."+e.getMessage());
				errRes.put("success", false);
				res.add(errRes);
			}

			// check if registration id
			if (a6 != ""){
				try {
					String queryRegId = "SELECT COUNT(*) FROM db_non_state_actors WHERE a6 = ?";
					String[] selectionArgsRegId = { a6 };
					Cursor cursorRegId = dbRead.rawQuery(queryRegId, selectionArgsRegId);
					int countRegId = 0;
					if (cursorRegId.moveToFirst()) {
						countRegId = cursorRegId.getInt(0);
					}
					cursorRegId.close();
					if(countRegId != 0){
						dbRead.close(); // Closing read database connection
						mDb.close(); // Closing write database connection

						Map<String, Object> regIDRes = new HashMap<>();
						regIDRes.put("message", "Registration ID already exists...");
						regIDRes.put("count", countRegId);
						regIDRes.put("success", false);
						res.add(regIDRes);

						return res;
					}
				} catch (SQLException e) {
					Log.d(TAG, "Exception error: "+e.getMessage());
					Map<String, Object> errRes = new HashMap<>();
					errRes.put("message", "Error organisation registration ID...");
					errRes.put("success", false);
					res.add(errRes);
				}
			}
		}

		ContentValues values = new ContentValues();
		values.put("a1", a1);
		values.put("a2", a2);
		values.put("a3", a3);
		values.put("a4", a4);
		values.put("a5", a5);
		values.put("a6", a6);
		values.put("a7", a7);
		values.put("a8", a8);
		values.put("a10", a10);
		values.put("a11", a11);
		values.put("a12", a12);
		values.put("a13", a13);
		values.put("a14", a14);
		values.put("a15", a15);
		values.put("a16", a16);
		values.put("synced", synced);

		// Inserting Row
		try {
			mDb.insert("db_non_state_actors", null, values);
			dbRead.close(); // Closing database connection
			mDb.close(); // Closing database connection

			Map<String, Object> successRes = new HashMap<>();
			successRes.put("message", "Other market player added successfully...");
			successRes.put("success", true);
			res.add(successRes);

			Log.d(TAG, "New other market player inserted into the database successfully...");
		} catch (SQLException e) {
			Log.d(TAG, "Exception error: "+e.getMessage());
			Map<String, Object> errRes = new HashMap<>();
			errRes.put("message", "Error adding other market player...");
			errRes.put("success", false);
			res.add(errRes);
		}

		return res;
	}
	// updated synced other market player
	public boolean updateSyncedOtherMarketPlayer(int id, int status, String other_market_player_id) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_SYNCED, status);
		contentValues.put("a50", other_market_player_id);
		db.update("db_non_state_actors", contentValues, KEY_ID + "=" + id, null);
		db.close();
		return true;
	}


	public static String getSerialNumber() {
		String serialNumber;

		try {
			Class<?> c = Class.forName("android.os.SystemProperties");
			Method get = c.getMethod("get", String.class);

			serialNumber = (String) get.invoke(c, "gsm.sn1");
			if (serialNumber.equals(""))
				serialNumber = (String) get.invoke(c, "ril.serialnumber");
			if (serialNumber.equals(""))
				serialNumber = (String) get.invoke(c, "ro.serialno");
			if (serialNumber.equals(""))
				serialNumber = (String) get.invoke(c, "sys.serialnumber");
			if (serialNumber.equals(""))
				serialNumber = Build.SERIAL;

			// If none of the methods above worked
			if (serialNumber.equals(""))
				serialNumber = null;
		} catch (Exception e) {
			e.printStackTrace();
			serialNumber = null;
		}

		return serialNumber;
	}


	public HashMap<String, String> getVillageDetails( String a1, String a2, String a3, String a4) {
		String a1b = a1.toUpperCase();

		HashMap<String, String> village = new HashMap<String, String>();
		String selectQuery = "SELECT village.id  FROM district,county,subcounty,parish,village WHERE district.id = county.district_id AND county.id = subcounty.county_id AND subcounty.id = parish.subcounty_id AND parish.id  = village.parish_id AND district.name=\'"+ a1b + "\' AND subcounty.name=\'"+ a2 + "\' AND parish.name=\'"+ a3 + "\' AND village.name=\'"+ a4 + "\'";

		try {
			mDb = this.getReadableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		//SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = mDb.rawQuery(selectQuery, null);
		// Move to first row
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			village.put("id", cursor.getString(0));

		}
		cursor.close();
		mDb.close();
		// return village
		Log.d(TAG, "Fetching village from Sqlite: " + village.toString());

		return village;
	}

	public void addEOI(String b1,String b2, String b3, String b4,
					   String b5, String b6, String b7, String b8,
					   String b9, String b10, String b11, String b12,
					   String b13, String b14, String b15, String b16,
					   String b17, String b18, String b19, String b20,String b25,
					   String b26, String b27) {



		// Fetching user details from SQLite
		HashMap<String, String> user = this.getUserDetails();
		String uid =  user.get("uid");
		Integer b21  = Integer.valueOf(uid);

		// Fetching village details from SQLite
		HashMap<String, String> village = this.getVillageDetails(b10,b11,b12,b13);
		String vid =  village.get("id");
		Integer b31  = Integer.valueOf(vid);


		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		Integer idx = 1;

		Date date= new Date();

		java.util.Date dt = new java.util.Date();

		java.text.SimpleDateFormat sdf =
				new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String b29 = sdf.format(dt);
		String b28 = getSerialNumber();


		ContentValues values = new ContentValues();
		values.put("a1", b1);
		values.put("a2", b2);
		values.put("a3", b3);
		values.put("a4", b4);
		values.put("a5", b5);
		values.put("a6", b6);
		values.put("a7", b7);
		values.put("a8", b8);
		values.put("a9", b9);
		values.put("a10", b10);
		values.put("a11", b11);
		values.put("a12", b12);
		values.put("a13", b13);
		values.put("a14", b14);
		values.put("a15", b15);
		values.put("a16", b16);
		values.put("a17", b17);
		values.put("a18", b18);
		values.put("a19", b19);
		values.put("a20", b20);
		values.put("a21", b21);
		values.put("a25", b25);
		values.put("a26", b26);
		values.put("a27", b27);
		values.put("a28", b28);
		values.put("a29", b29);
		values.put("a31", b31);
		values.put("synced", 0);


		// Inserting Row
		long id = mDb.insert("farmer", null, values);
		mDb.close(); // Closing database connection

		Log.d(TAG, "New farmer EOI inserted into sqlite: " + id);
	}


	/**
	 * Storing user details in database
	 * */
	public void addNewGrievance(String district, String subcounty, String parish, String name, String age,
								String gender,  String phone, String feedback, String anonymous,
								String date_of_grievance, String gNature, String gType, String gTypeNotListed, String modeReceipt,
								String description, String past_actions,
								String settle_otherwise, String latitude, String longitude, String ref_number,String village)
	{

		// Fetching user details from SQLite
		HashMap<String, String> user = this.getUserDetails();
		String uid =  user.get("uid");
		Integer user_id  = Integer.valueOf(uid);

		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		// get parish id
		int parish_id = 0;
		String selectQuery = "SELECT  parish.id FROM subcounty, parish WHERE  subcounty.name  LIKE '"+ subcounty + "' " +
				"   AND parish.name LIKE  '"+ parish +"' AND parish.subcounty_id =subcounty.id";
		Cursor cursor = mDb.rawQuery(selectQuery, null);
		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				parish_id = cursor.getInt(0);
			} while (cursor.moveToNext());
		}
		Log.d(TAG, "Parish ID: " + parish_id);

		Integer idx = 1;

		Date date= new Date();

		java.util.Date dt = new java.util.Date();

		java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		String currentTime = sdf.format(dt);
		String b18 = getSerialNumber();

		ContentValues values = new ContentValues();
		values.put("a1", district);
		values.put("a2", subcounty);
		values.put("a3", String.valueOf(parish_id));
		values.put("a4", village);
		values.put("a5", name);
		values.put("a6", age);
		values.put("a7", gender);
		values.put("a8", phone);
		values.put("a9", feedback);
		values.put("a10", anonymous);
		values.put("a11", date_of_grievance);
		values.put("a12", gNature);
		values.put("a13", gType);
		values.put("a14", gTypeNotListed);
		values.put("a15", modeReceipt);
		values.put("a16", description);
		values.put("a17", past_actions);
		values.put("a18", settle_otherwise);
		values.put("a19", latitude);
		values.put("a20", longitude);
		values.put("a21", ref_number);
		values.put("a22", currentTime);
		values.put("a23", b18);
		values.put("a24", user_id);
		values.put("synced", 0);

		// Inserting Row
		long id = mDb.insert("mod_grm", null, values);
		mDb.close(); // Closing database connection

		Log.d(TAG, "New Grievance inserted into sqlite: " + id);
	}


	public List<String> getAllSyncedGrievances(){

		if(! isTableExists("mod_grm", true)){
			Toast.makeText(mContext.getApplicationContext(), "Storage for grievance not found", Toast.LENGTH_SHORT).show();
		}
		else {
			List<String> names = new ArrayList<String>();
			// Select All Query
			String selectQuery = "SELECT ref_number FROM mod_grm WHERE synced=1 ORDER BY id DESC";

			SQLiteDatabase db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery(selectQuery, null);

			// looping through all rows and adding to list
			if (cursor.moveToFirst()) {
				int columnIndex = cursor.getColumnIndex("ref_number");
				do {
					String refNumber = cursor.getString(columnIndex); // Get the ref_number
					names.add(refNumber);
				} while (cursor.moveToNext());
			}

			// closing connection
			cursor.close();
			db.close();

			// returning names
			return names;
		}
		return null;
	}


	public List<String> getAllModeofReceipt(){
		List<String> names = new ArrayList<String>();
		names.add("---Select Mode of Receipt---");

		// Select All Query
		String selectQuery = "SELECT  * FROM mode_of_receipt ORDER BY name ASC";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				names.add(cursor.getString(1).toUpperCase());
			} while (cursor.moveToNext());
		}

		// closing connection
		cursor.close();
		db.close();

		// returning names
		return names;
	}
	public List<String> getAllSettlement(){
		List<String> names = new ArrayList<String>();
		names.add("---Select Settlement---");

		// Select All Query
		String selectQuery = "SELECT  * FROM grievance_settle_otherwise ORDER BY id ASC";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				names.add(cursor.getString(1).toUpperCase());
			} while (cursor.moveToNext());
		}

		// closing connection
		cursor.close();
		db.close();

		// returning names
		return names;
	}

	public List<String> getAllFeedback(){
		List<String> names = new ArrayList<String>();
		names.add("---Select Feedback Mode---");

		// Select All Query
		String selectQuery = "SELECT  * FROM feedback_mode ORDER BY id ASC";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				names.add(cursor.getString(1).toUpperCase());
			} while (cursor.moveToNext());
		}

		// closing connection
		cursor.close();
		db.close();

		// returning names
		return names;
	}


	public List<String> getAllNature(){
		List<String> names = new ArrayList<String>();
		names.add("---Select Grievance Nature---");

		// Select All Query
		String selectQuery = "SELECT  * FROM grievance_nature ORDER BY id ASC";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				names.add(cursor.getString(1).toUpperCase());
			} while (cursor.moveToNext());
		}

		// closing connection
		cursor.close();
		db.close();

		// returning names
		return names;
	}

	public List<String> getAllTypes( String nature){
		List<String> ids = new ArrayList<String>();
		List<String> names = new ArrayList<String>();
		names.add("---Select Grievance Type---");
		Integer id = 0 ;
		String[] ids_final;
		SQLiteDatabase db = this.getReadableDatabase();

		// Get all parishes in Giveen District
		String selectQuery2 = "SELECT  * FROM grivance_type WHERE grievance_nature ='" + nature + "'";

		Cursor cursor2 = db.rawQuery(selectQuery2, null);

		// looping through all rows and adding to list
		if (cursor2.moveToFirst()) {
			do {
				names.add(cursor2.getString(1));
			} while (cursor2.moveToNext());
		}

		// closing connection
		cursor2.close();
		db.close();

		// returning names
		return names;
	}


	public boolean updateGrievanceStatus(int id, int status) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_SYNCED, status);
		db.update("mod_grm", contentValues, KEY_ID + "=" + id, null);
		db.close();
		return true;
	}

	public boolean updateGrievance(int id, int status, String grievance_id, String grievance_reference) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put(KEY_SYNCED, status);
		contentValues.put("grievance_id", grievance_id);
		contentValues.put("ref_number", grievance_reference);
		db.update("mod_grm", contentValues, KEY_ID + "=" + id, null);
		db.close();
		return true;
	}

	public boolean updateGrievanceRef(int id, String ref) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues contentValues = new ContentValues();
		contentValues.put("ref_number", ref);
		db.update("mod_grm", contentValues, KEY_ID + "=" + id, null);
		db.close();
		return true;
	}

	/*
	 * this method will give us all the grievances stored in sqlite
	 * */
	public Cursor getGrievances() {
		SQLiteDatabase db = this.getReadableDatabase();
		String sql = "SELECT * FROM mod_grm ORDER BY id DESC";
		Cursor c = db.rawQuery(sql, null);
		return c;
	}

	public List<Map<String, Object>> grievancesStats(){
		List<Map<String, Object>> res = new ArrayList<>();
		Map<String, Object> obj = new HashMap<>();

		// read db
		SQLiteDatabase dbRead = getReadableDatabase();

		// unsynced
		String queryUnsynced = "SELECT COUNT(*) FROM mod_grm WHERE synced = 0";
		Cursor cursorUnsynced = dbRead.rawQuery(queryUnsynced, null);
		int unsynced_count = 0;
		if (cursorUnsynced.moveToFirst()) {
			unsynced_count = cursorUnsynced.getInt(0);
		}
		obj.put("unsynced", unsynced_count);

		// synced
		String querySynced = "SELECT COUNT(*) FROM mod_grm WHERE synced = 1";
		Cursor cursorSynced = dbRead.rawQuery(querySynced, null);
		int synced_count = 0;
		if (cursorSynced.moveToFirst()) {
			synced_count = cursorSynced.getInt(0);
		}
		obj.put("synced", synced_count);

		int total_grievances = unsynced_count + synced_count;
		obj.put("all", total_grievances);

		res.add(obj);

		// close db connection
		dbRead.close();

		return res;
	}

	/*
	 * this method is for getting all the unsynced activities
	 * so that we can sync it with database
	 * */
	public Cursor getUnsyncedGrievances() {
		SQLiteDatabase db = this.getReadableDatabase();
		String sql = "SELECT * FROM mod_grm where synced = 0";
		Cursor c = db.rawQuery(sql, null);
		return c;
	}
//	String sql = "SELECT * FROM grievance WHERE " + KEY_SYNCED + " = 0;";

	/**
	 * Getting user data from database
	 * */
	public HashMap<String, String> getDistrictDetails( String district_id) {
		HashMap<String, String> district = new HashMap<String, String>();
		String selectQuery = "SELECT  * FROM district WHERE name='"+district_id+"'";

		try {
			mDb = this.getReadableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		//SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = mDb.rawQuery(selectQuery, null);
		// Move to first row
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			district.put("name", cursor.getString(1));
			district.put("id", cursor.getString(0));
		}
		cursor.close();
		mDb.close();
		// return user
		Log.d(TAG, "Fetching district from Sqlite: " + district.toString());

		return district;
	}

	public HashMap<String, String> getDistrict2Details( String district_name) {
		HashMap<String, String> district = new HashMap<String, String>();
		String selectQuery = "SELECT  * FROM district2 WHERE name='"+district_name+"'";

		try {
			mDb = this.getReadableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		//SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = mDb.rawQuery(selectQuery, null);
		// Move to first row
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			district.put("name", cursor.getString(1));
			district.put("id", cursor.getString(0));
		}
		cursor.close();
		mDb.close();
		// return user
		Log.d(TAG, "Fetching district from Sqlite: " + district.toString());

		return district;
	}

	public List<String> getAllGenders(){
		List<String> names = new ArrayList<String>();

		// Select All Query
		String selectQuery = "SELECT  * FROM gender ORDER BY id ASC";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				names.add(cursor.getString(1).toUpperCase());
			} while (cursor.moveToNext());
		}

		// closing connection
		cursor.close();
		db.close();

		// returning names
		return names;
	}


	public List<String> getAllAnonymous(){
		List<String> names = new ArrayList<String>();

		// Select All Query
		String selectQuery = "SELECT  * FROM booltf ORDER BY id DESC";

		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				names.add(cursor.getString(1).toUpperCase());
			} while (cursor.moveToNext());
		}

		// closing connection
		cursor.close();
		db.close();

		// returning names
		return names;
	}
























	////New stuff
	public int getCountFarmers() {
//		String countQuery = "SELECT * FROM db_farmer ";
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.rawQuery(countQuery, null);
//		int count = cursor.getCount();
//		cursor.close();
		return 0;
	}

	public int getCountFarmerGroups() {
		String countQuery = "SELECT * FROM db_farmer_group";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}

	public int getCountWeatherParishes(String district) {
		String countQuery = "SELECT * FROM weather_parishes WHERE District LIKE '"+ district + "'";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}

	public int getCountWeatherData() {
		String countQuery = "SELECT * FROM weather_infomation";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}

	public int getCountIrrigationSchemes() {
//		String countQuery = "SELECT * FROM db_irrigation_scheme";
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.rawQuery(countQuery, null);
//		int count = cursor.getCount();
//		cursor.close();
		return 0;
	}

	public int getCountMarkets() {
//		String countQuery = "SELECT * FROM db_market";
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.rawQuery(countQuery, null);
//		int count = cursor.getCount();
//		cursor.close();
		return 0;
	}


	public int getCountMarketsSynced() {
//		String countQuery = "SELECT * FROM db_market WHERE synced=1";
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.rawQuery(countQuery, null);
//		int count = cursor.getCount();
//		cursor.close();
		return 0;
	}

	public int getCountNonStateActors() {
		String countQuery = "SELECT * FROM db_non_state_actors";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}

	public int getCountParishChiefs() {
		String countQuery = "SELECT * FROM db_parishchiefs";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}

	public int getCountProjects() {
//		String countQuery = "SELECT * FROM db_project";
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.rawQuery(countQuery, null);
//		int count = cursor.getCount();
//		cursor.close();
		return 0;
	}

	public int getCountStateActors() {
		String countQuery = "SELECT * FROM db_state_actors";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}

	public int getCountStorageWarehouses() {
//		String countQuery = "SELECT * FROM db_storagewarehouses";
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.rawQuery(countQuery, null);
//		int count = cursor.getCount();
//		cursor.close();
		return 0;
	}

	public int getCountSubcountyChiefs() {
//		String countQuery = "SELECT * FROM db_subcountychiefs";
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.rawQuery(countQuery, null);
//		int count = cursor.getCount();
//		cursor.close();
		return 0;
	}



	public int getCountProfileTotals() {
		return getCountFarmers()+getCountFarmerGroups()+
				getCountIrrigationSchemes()+getCountMarkets()+
				getCountNonStateActors()+getCountParishChiefs()+
				getCountProjects()+getCountStateActors()+
				getCountStorageWarehouses()+getCountSubcountyChiefs();
	}

	public int getCountActivities() {
		String countQuery = "SELECT * FROM mod_ediary";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}




	public int getCountFarmersUnsynced() {
//		String countQuery = "SELECT * FROM db_farmer WHERE synced=0 ";
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.rawQuery(countQuery, null);
//		int count = cursor.getCount();
//		cursor.close();
		return 0;
	}

	public int getCountFarmerGroupsUnsynced() {
		String countQuery = "SELECT * FROM db_farmer_group WHERE synced=0";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}

	public int getCountIrrigationSchemesUnsynced() {
//		String countQuery = "SELECT * FROM db_irrigation_scheme WHERE synced=0";
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.rawQuery(countQuery, null);
//		int count = cursor.getCount();
//		cursor.close();
		return 0;
	}

	public int getCountMarketsUnsynced() {
//		String countQuery = "SELECT * FROM db_market WHERE synced=0";
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.rawQuery(countQuery, null);
//		int count = cursor.getCount();
//		cursor.close();
		return 0;
	}

	public int getCountNonStateActorsUnsynced() {
		String countQuery = "SELECT * FROM db_non_state_actors WHERE synced=0";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}

	public int getCountParishChiefsUnsynced() {
		String countQuery = "SELECT * FROM db_parishchiefs WHERE synced=0";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}

	public int getCountProjectsUnsynced() {
//		String countQuery = "SELECT * FROM db_project WHERE synced=0";
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.rawQuery(countQuery, null);
//		int count = cursor.getCount();
//		cursor.close();
		return 0;
	}

	public int getCountStateActorsUnsynced() {
		String countQuery = "SELECT * FROM db_state_actors WHERE synced=0";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}

	public int getCountStorageWarehousesUnsynced() {
//		String countQuery = "SELECT * FROM db_storagewarehouses WHERE synced=0";
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.rawQuery(countQuery, null);
//		int count = cursor.getCount();
//		cursor.close();
		return 0;
	}

	public int getCountSubcountyChiefsUnsynced() {
//		String countQuery = "SELECT * FROM db_subcountychiefs WHERE synced=0";
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.rawQuery(countQuery, null);
//		int count = cursor.getCount();
//		cursor.close();
		return 0;
	}


	public int getCountActivitiesUnsynced() {
		String countQuery = "SELECT * FROM mod_ediary WHERE synced=0";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}


	public int getCountAdvisoryUnsynced() {
		String countQuery = "SELECT * FROM mod_advisory WHERE synced=0";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}


	public int getCountCrisesOutbreaksUnsynced() {
		String countQuery = "SELECT * FROM mod_crises_outbreaks WHERE synced=0";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}


	public int getCountGRMUnsynced() {
		String countQuery = "SELECT * FROM mod_grm WHERE synced=0";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}


	public int getCountKMUUnsynced() {
//		String countQuery = "SELECT * FROM mod_kmu WHERE synced=0";
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.rawQuery(countQuery, null);
//		int count = cursor.getCount();
//		cursor.close();
		return 0;
	}


	public int getCountEdiaryUnsynced() {
		String countQuery = "SELECT * FROM mod_ediary WHERE synced=0";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}


	public int getCountEdiarySynced() {
		String countQuery = "SELECT * FROM mod_ediary WHERE synced=1";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}


	public int getCountEdiaryPlanned() {
		String countQuery = "SELECT * FROM mod_ediary WHERE a20='0'";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}


	public int getCountEdiaryUnplanned() {
		String countQuery = "SELECT * FROM mod_ediary WHERE a20='1'";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}



	public int getCountMarketPriceVolumeUnsynced() {
//		String countQuery = "SELECT * FROM mod_market_price_volume WHERE synced=0";
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.rawQuery(countQuery, null);
//		int count = cursor.getCount();
//		cursor.close();
		return 0;
	}


	public int getCountMarketPriceVolumeSynced() {
//		String countQuery = "SELECT * FROM mod_market_price_volume WHERE synced=1";
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.rawQuery(countQuery, null);
//		int count = cursor.getCount();
//		cursor.close();
		return 0;
	}


	public int getCountMarketPriceVolume() {
//		String countQuery = "SELECT * FROM mod_market_price_volume ";
//		SQLiteDatabase db = this.getReadableDatabase();
//		Cursor cursor = db.rawQuery(countQuery, null);
//		int count = cursor.getCount();
//		cursor.close();
		return 0;
	}


	public int getCountPendingUploadTotals() {
		return getCountFarmersUnsynced()+getCountFarmerGroupsUnsynced()+
				getCountIrrigationSchemesUnsynced()+getCountMarketsUnsynced()+
				getCountNonStateActorsUnsynced()+
				getCountParishChiefsUnsynced()+getCountProjectsUnsynced()+
				getCountStateActorsUnsynced()+getCountStorageWarehousesUnsynced()+
				getCountSubcountyChiefsUnsynced()+ getCountActivitiesUnsynced()+
				getCountAdvisoryUnsynced()+getCountCrisesOutbreaksUnsynced()+getCountGRMUnsynced()+
				getCountKMUUnsynced()+getCountMarketPriceVolumeUnsynced();
	}

	public HashMap<String, String> getUserAuthDetails() {
		HashMap<String, String> user = new HashMap<String, String>();
		String selectQuery = "SELECT  * FROM user_auth";

		try {
			mDb = this.getReadableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		//SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = mDb.rawQuery(selectQuery, null);
		// Move to first row
		cursor.moveToFirst();
		if (cursor.getCount() > 0) {
			user.put("email", cursor.getString(1));
			user.put("password", cursor.getString(2));

		}
		cursor.close();
		mDb.close();
		// return user
		Log.d(TAG, "Fetching user auth from Sqlite: " + user.toString());

		return user;
	}
	public int getCountAllQuestions() {
		String countQuery = "SELECT id FROM farmer_questions";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}
	public int getCountAnsweredQuestions() {
		String countQuery = "SELECT id FROM farmer_questions WHERE responses > 0";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}
	public int getCountUnAnsweredQuestions() {
		String countQuery = "SELECT id FROM farmer_questions WHERE responses = 0";
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}
	public Cursor getQuestions() {
		SQLiteDatabase db = this.getReadableDatabase();
		String sql = "SELECT * FROM farmer_questions ORDER BY id DESC ";
		Cursor c = db.rawQuery(sql, null);
		return c;
	}

	public Cursor getMyQuestions(int userId) {
		SQLiteDatabase db = this.getReadableDatabase();
		String sql = "SELECT * FROM farmer_questions where user_id="+userId+" ORDER BY id DESC ";
		Cursor c = db.rawQuery(sql, null);
		return c;
	}

	public void addNewQuestion(String id,
							   String keyword, String farmer_id, String farmer, String parish_id, String telephone, String body, String enterprise_id, String inquiry_source, String created_at, String updated_at,
							   String has_media,String media_url,String responses, String sender,String user_id) {




		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}


		ContentValues values = new ContentValues();
		values.put("id", id);
		values.put("parish_id", parish_id);
		values.put("farmer_id", farmer_id);
		values.put("farmer", farmer);
		values.put("telephone", telephone);
		values.put("enterprise_id", enterprise_id);
		values.put("inquiry_source", inquiry_source);
		values.put("has_media", has_media);
		values.put("media_url", media_url);
		values.put("responses", responses);
		values.put("created_at", created_at);
		values.put("updated_at", updated_at);
		values.put("body", body);
		values.put("sender",sender);
		values.put("user_id",user_id);


		// Inserting Row
		long sid = mDb.insert("farmer_questions", null, values);
		mDb.close(); // Closing database connection

		Log.d(TAG, "New farmer questions added: " + id);
	}

	public boolean isTableExists(String tableName, boolean openDb) {
		SQLiteDatabase db = this.getReadableDatabase();
		if(openDb) {
			if(db == null || !db.isOpen()) {
				db = getReadableDatabase();
			}

			if(!db.isReadOnly()) {
				db.close();
				db = getReadableDatabase();
			}
		}

		String query = "select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'";
		try (Cursor cursor = db.rawQuery(query, null)) {
			if(cursor!=null) {
				if(cursor.getCount()>0) {
					return true;
				}
			}
			return false;
		}
	}

	public void logTables() {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);

		if (c.moveToFirst()) {
			while ( !c.isAfterLast() ) {
				Log.e("Table Name=> ", ""+c.getString(0));
				c.moveToNext();
			}
		}
	}

	// Add Farmer question
	public List<Map<String, Object>> addNewFarmerQuestion(String a1, String a2, String a3, String a4, String a5, String a6, String a7, String a8, String a9, int synced) {
		List<Map<String, Object>> res = new ArrayList<>();

		try {
			mDb = this.getWritableDatabase();
		} catch (SQLException mSQLException) {
			throw mSQLException;
		}

		// read db
		SQLiteDatabase dbRead = getReadableDatabase();

		ContentValues values = new ContentValues();
		values.put("a1", a1);
		values.put("a2", a2);
		values.put("a3", a3);
		values.put("a4", a4);
		values.put("a5", a5);
		values.put("a6", a6);
		values.put("a7", a7);
		values.put("a8", a8);
		values.put("a9", a9);
		values.put("synced", synced);

		// Inserting Row
		try {
			mDb.insert("mod_advisory", null, values);
			dbRead.close(); // Closing database connection
			mDb.close(); // Closing database connection

			Map<String, Object> successRes = new HashMap<>();
			successRes.put("message", "New farmer question added successfully...");
			successRes.put("success", true);
			res.add(successRes);

			Log.d(TAG, "New farmer question inserted into the database successfully...");
		} catch (SQLException e) {
			Log.d(TAG, "Exception error: "+e.getMessage());
			Map<String, Object> errRes = new HashMap<>();
			errRes.put("message", "Error adding farmer question ...");
			errRes.put("success", false);
			res.add(errRes);
		}

		return res;
	}

}
