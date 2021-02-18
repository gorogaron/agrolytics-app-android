package com.agrolytics.agrolytics_android.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(var context: Context) {

	private val SESSION_ID = "session.id"
	private val USER_ID = "user.id"
	private val USER_LEADER_ID = "user.leader_id"
	private val USER_EXPIRE_DATE = "user.expire_date"
	private val USER_ROLE = "user.role"
	private val USER_EMAIL = "user.email"
	private val USER_FORESTRY_ID = "forestry.id"
	private val USER_FORESTRY_NAME = "forestry.name"
	private val WOOD_LENGTH = "wood.length"
	private val WOOD_TYPE = "wood.type"
	private val ROD_LENGTH = "wood.rod.length"
	private val SESSION_FILE_NAME = "session.filename"

	private val sharedPreferences: SharedPreferences =
		context.getSharedPreferences(SESSION_FILE_NAME, Context.MODE_PRIVATE)


	var sessionId: String
		get() = sharedPreferences.getString(SESSION_ID, "")!!
		set(sessionId) = sharedPreferences.edit().putString(SESSION_ID, sessionId).apply()

	var userId: String
		get() = sharedPreferences.getString(USER_ID, "")!!
		set(userID) = sharedPreferences.edit().putString(USER_ID, userID).apply()

	var userEmail: String
		get() = sharedPreferences.getString(USER_EMAIL, "")!!
		set(userEmail) = sharedPreferences.edit().putString(USER_EMAIL, userEmail).apply()

	var userExpireDate: Long
		get() = sharedPreferences.getLong(USER_EXPIRE_DATE, 1600000000)
		set(expireDate) = sharedPreferences.edit().putLong(USER_EXPIRE_DATE, expireDate).apply()

	var userRole: String
		get() = sharedPreferences.getString(USER_ROLE, "")!!
		set(userRole) = sharedPreferences.edit().putString(USER_ROLE, userRole).apply()

	var leaderId: String
		get() = sharedPreferences.getString(USER_LEADER_ID, "")!!
		set(leaderID) = sharedPreferences.edit().putString(USER_LEADER_ID, leaderID).apply()

	var forestryId: String
		get() = sharedPreferences.getString(USER_FORESTRY_ID, "")!!
		set(forestryId) = sharedPreferences.edit().putString(USER_FORESTRY_ID, forestryId).apply()

	var forestryName: String
		get() = sharedPreferences.getString(USER_FORESTRY_NAME, "")!!
		set(forestryName) = sharedPreferences.edit().putString(USER_FORESTRY_NAME, forestryName).apply()

	var woodLength: Float
		get() = sharedPreferences.getFloat(WOOD_LENGTH, 1.0f)
		set(length) = sharedPreferences.edit().putFloat(WOOD_LENGTH, length).apply()

	var woodType: String
		get() = sharedPreferences.getString(WOOD_TYPE, "")!!
		set(type) = sharedPreferences.edit().putString(WOOD_TYPE, type).apply()

	var rodLength: Float
		get() = sharedPreferences.getFloat(ROD_LENGTH, 1.0f)
		set(length) = sharedPreferences.edit().putFloat(ROD_LENGTH, length).apply()


	fun clearSession() {
		sharedPreferences.edit()
			.remove(WOOD_LENGTH)
			.remove(WOOD_TYPE)
			.remove(USER_ROLE)
			.remove(USER_ID)
			.remove(USER_EXPIRE_DATE)
			.remove(USER_LEADER_ID)
			.remove(USER_EMAIL)
			.remove(USER_FORESTRY_ID)
			.remove(USER_FORESTRY_NAME)
			.remove(ROD_LENGTH)
			.remove(SESSION_ID)
			.apply()
	}

}