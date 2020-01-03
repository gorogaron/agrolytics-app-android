package com.agrolytics.agrolytics_android.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(var context: Context) {

	private val KEY_LENGHT = "KeyLenght"
	private val KEY_ROLE = "KeyRole"
	private val KEY_USER_REF = "KeyUserRef"
	private val KEY_USER_ID = "KeyUserId"
	private val KEY_LEADER_ID = "KeyLeaderId"
	private val KEY_USER_EMAIL = "KeyUserEmail"
	private val KEY_FORERSTRY_ID = "KeyForestryId"
	private val KEY_FIRST_LOGIN = "KeyFirstLogin"
	private val KEY_MODE = "KeyMode"
	private val SESSION_PREF_NAME = "PREF_SESSION_STORAGE"
	private val sharedPreferences: SharedPreferences =
			context.getSharedPreferences(SESSION_PREF_NAME, Context.MODE_PRIVATE)

	var length: Float
		get() = sharedPreferences.getFloat(KEY_LENGHT, 1.0f)
		set(length) = sharedPreferences.edit().putFloat(KEY_LENGHT, length).apply()

	var userRole: String
		get() = sharedPreferences.getString(KEY_ROLE, "")
		set(userRole) = sharedPreferences.edit().putString(KEY_ROLE, userRole).apply()

	var userReference: String
		get() = sharedPreferences.getString(KEY_USER_REF, "")
		set(userReference) = sharedPreferences.edit().putString(KEY_USER_REF, userReference).apply()

	var userID: String
		get() = sharedPreferences.getString(KEY_USER_ID, "")
		set(userID) = sharedPreferences.edit().putString(KEY_USER_ID, userID).apply()

	var leaderID: String
		get() = sharedPreferences.getString(KEY_LEADER_ID, "")
		set(leaderID) = sharedPreferences.edit().putString(KEY_LEADER_ID, leaderID).apply()

	var userEmail: String
		get() = sharedPreferences.getString(KEY_USER_EMAIL, "")
		set(userEmail) = sharedPreferences.edit().putString(KEY_USER_EMAIL, userEmail).apply()

	var forestryID: String
		get() = sharedPreferences.getString(KEY_FORERSTRY_ID, "")
		set(forestryID) = sharedPreferences.edit().putString(KEY_FORERSTRY_ID, forestryID).apply()

	var firstLogin: String
		get() = sharedPreferences.getString(KEY_FIRST_LOGIN, "")
		set(firstLogin) = sharedPreferences.edit().putString(KEY_FIRST_LOGIN, firstLogin).apply()

	var mode: String
		get() = sharedPreferences.getString(KEY_MODE, "rod")
		set(mode) = sharedPreferences.edit().putString(KEY_MODE, mode).apply()

	fun clearSession() {
		sharedPreferences.edit()
				.remove(KEY_ROLE)
				.remove(KEY_USER_REF)
				.remove(KEY_LENGHT)
				.remove(KEY_USER_ID)
				.remove(KEY_LEADER_ID)
				.remove(KEY_USER_EMAIL)
				.remove(KEY_FORERSTRY_ID)
				.remove(KEY_FIRST_LOGIN)
				.remove(KEY_MODE)
				.apply()
	}

}