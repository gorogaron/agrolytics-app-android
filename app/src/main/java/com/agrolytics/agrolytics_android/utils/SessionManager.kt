package com.agrolytics.agrolytics_android.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(var context: Context) {

	private val USER_ID = "user.id"
	private val USER_LEADER_ID = "user.leader.id"
	private val USER_ROLE = "user.role"
	private val USER_EMAIL = "user.email"
	private val USER_FORESTRY_ID = "user.forestry.id"
	private val USER_FIRST_LOGIN = "user.first_login"
	private val WOOD_LENGTH = "wood.length"
	private val WOOD_TYPE = "wood.type"
	private val WOOD_ROD_LENGTH = "wood.rod.length"
	private val SESSION_FILE_NAME = "session.filename"

	private val sharedPreferences: SharedPreferences =
		context.getSharedPreferences(SESSION_FILE_NAME, Context.MODE_PRIVATE)

	var woodLength: Float
		get() = sharedPreferences.getFloat(WOOD_LENGTH, 1.0f)
		set(length) = sharedPreferences.edit().putFloat(WOOD_LENGTH, length).apply()

	var woodType: String
		get() = sharedPreferences.getString(WOOD_TYPE, "")!!
		set(type) = sharedPreferences.edit().putString(WOOD_TYPE, type).apply()

	var rodLength: Float
		get() = sharedPreferences.getFloat(WOOD_ROD_LENGTH, 1.0f)
		set(length) = sharedPreferences.edit().putFloat(WOOD_ROD_LENGTH, length).apply()

	var userRole: String
		get() = sharedPreferences.getString(USER_ROLE, "")!!
		set(userRole) = sharedPreferences.edit().putString(USER_ROLE, userRole).apply()

	var userID: String
		get() = sharedPreferences.getString(USER_ID, "")!!
		set(userID) = sharedPreferences.edit().putString(USER_ID, userID).apply()

	var leaderID: String
		get() = sharedPreferences.getString(USER_LEADER_ID, "")!!
		set(leaderID) = sharedPreferences.edit().putString(USER_LEADER_ID, leaderID).apply()

	var userEmail: String
		get() = sharedPreferences.getString(USER_EMAIL, "")!!
		set(userEmail) = sharedPreferences.edit().putString(USER_EMAIL, userEmail).apply()

	var forestryID: String
		get() = sharedPreferences.getString(USER_FORESTRY_ID, "")!!
		set(forestryID) = sharedPreferences.edit().putString(USER_FORESTRY_ID, forestryID).apply()

	var firstLogin: String
		get() = sharedPreferences.getString(USER_FIRST_LOGIN, "")!!
		set(firstLogin) = sharedPreferences.edit().putString(USER_FIRST_LOGIN, firstLogin).apply()

	fun clearSession() {
		sharedPreferences.edit()
			.remove(WOOD_LENGTH)
			.remove(WOOD_TYPE)
			.remove(USER_ROLE)
			.remove(USER_ID)
			.remove(USER_LEADER_ID)
			.remove(USER_EMAIL)
			.remove(USER_FORESTRY_ID)
			.remove(WOOD_ROD_LENGTH)
			.apply()
	}

}