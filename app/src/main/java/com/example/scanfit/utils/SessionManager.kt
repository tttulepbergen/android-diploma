package com.example.scanfit.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.scanfit.network.NetworkClient

class SessionManager(context: Context) {
    private var prefs: SharedPreferences =
        context.getSharedPreferences("ScanFitPrefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val USER_ID = "user_id"
        const val USER_ROLE = "user_role"
    }

    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun saveRefreshToken(token: String) {
        val editor = prefs.edit()
        editor.putString(REFRESH_TOKEN, token)
        editor.apply()
    }

    fun fetchRefreshToken(): String? {
        return prefs.getString(REFRESH_TOKEN, null)
    }

    fun saveUserId(id: Int) {
        val editor = prefs.edit()
        editor.putInt(USER_ID, id)
        editor.apply()
    }

    fun fetchUserId(): Int {
        return prefs.getInt(USER_ID, -1)
    }

    fun saveUserRole(role: String) {
        val editor = prefs.edit()
        editor.putString(USER_ROLE, role)
        editor.apply()
    }

    fun fetchUserRole(): String? {
        return prefs.getString(USER_ROLE, "basic")
    }

    fun isVip(): Boolean {
        return fetchUserRole() == "vip"
    }

    suspend fun refreshUserRole() {
        val token = fetchAuthToken() ?: return
        val response = NetworkClient.userApiService.getUserRole(token)
        if (response.success) {
            saveUserRole(response.data?.code ?: "basic")
        }
    }

    fun clearData() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}
