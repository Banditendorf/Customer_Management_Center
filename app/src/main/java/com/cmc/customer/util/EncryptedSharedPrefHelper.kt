package com.cmc.customer.util

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.cmc.customer.model.User
import com.cmc.customer.model.UserPermissions
import com.google.gson.Gson
import java.io.IOException
import java.security.GeneralSecurityException
import javax.crypto.AEADBadTagException

object EncryptedSharedPrefHelper {

    private const val PREF_NAME = "CMC_portal_secure_prefs"
    private const val MASTER_KEY_ALIAS = "CMC_portal_master_key"

    private const val KEY_EMAIL = "email"
    private const val KEY_PASSWORD = "password"
    private const val KEY_REMEMBER = "remember_me"
    private const val KEY_YETKILER = "yetkiler_json"

    private const val TAG = "EncryptedPrefs"

    private fun getSecurePrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context, MASTER_KEY_ALIAS)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return try {
            EncryptedSharedPreferences.create(
                context,
                PREF_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: AEADBadTagException) {
            context.deleteSharedPreferences(PREF_NAME)
            Log.e(TAG, "AEADBadTagException â€“ Bozuk keyset sÄ±fÄ±rlandÄ±: ${e.message}")
            recreatePrefs(context, masterKey)
        } catch (e: GeneralSecurityException) {
            context.deleteSharedPreferences(PREF_NAME)
            Log.e(TAG, "SecurityException â€“ Prefs sÄ±fÄ±rlandÄ±: ${e.message}")
            recreatePrefs(context, masterKey)
        } catch (e: IOException) {
            context.deleteSharedPreferences(PREF_NAME)
            Log.e(TAG, "IOException â€“ Prefs sÄ±fÄ±rlandÄ±: ${e.message}")
            recreatePrefs(context, masterKey)
        }
    }

    private fun recreatePrefs(context: Context, masterKey: MasterKey): SharedPreferences {
        return EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // âœ… "Beni hatÄ±rla" iÅŸlemleri
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    fun saveRememberMe(context: Context, email: String) {
        getSecurePrefs(context).edit().apply {
            putString(KEY_EMAIL, email)
            putBoolean(KEY_REMEMBER, true)
            apply()
        }
        Log.d(TAG, "Email ve remember kaydedildi")
    }

    fun clearRememberMe(context: Context) {
        getSecurePrefs(context).edit().apply {
            remove(KEY_EMAIL)
            putBoolean(KEY_REMEMBER, false)
            apply()
        }
        Log.d(TAG, "Remember bilgileri temizlendi")
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // âœ… GiriÅŸ bilgileri
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    fun saveCredentials(
        context: Context,
        email: String,
        password: String,
        remember: Boolean = true
    ) {
        getSecurePrefs(context).edit().apply {
            putString(KEY_EMAIL, email)
            putString(KEY_PASSWORD, password)
            putBoolean(KEY_REMEMBER, remember)
            apply()
        }
        Log.d(TAG, "GiriÅŸ bilgileri kaydedildi")
    }

    fun clearCredentials(context: Context) {
        getSecurePrefs(context).edit().apply {
            remove(KEY_EMAIL)
            remove(KEY_PASSWORD)
            remove(KEY_REMEMBER)
            apply()
        }
        Log.d(TAG, "GiriÅŸ bilgileri temizlendi")
    }

    fun getSavedEmail(context: Context): String? {
        return getSecurePrefs(context).getString(KEY_EMAIL, null)
    }

    fun getSavedPassword(context: Context): String? {
        return getSecurePrefs(context).getString(KEY_PASSWORD, null)
    }

    fun isRemembered(context: Context): Boolean {
        return getSecurePrefs(context).getBoolean(KEY_REMEMBER, false)
    }

    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
    // âœ… Yetki (UserPermissions) iÅŸlemleri
    // â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€

    fun saveUserPermissions(context: Context, user: User) {
        val json = Gson().toJson(user.permissions)
        getSecurePrefs(context).edit().putString(KEY_YETKILER, json).apply()
        Log.d(TAG, "Yetkiler kaydedildi: $json")
    }

    fun getSavedPermissions(context: Context): UserPermissions {
        val json = getSecurePrefs(context).getString(KEY_YETKILER, null)
        return try {
            if (json != null) {
                Gson().fromJson(json, UserPermissions::class.java).also {
                    Log.d(TAG, "Yetkiler yÃ¼klendi: $json")
                }
            } else {
                Log.w(TAG, "Yetki JSON null, boÅŸ UserPermissions dÃ¶ndÃ¼")
                UserPermissions()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Yetkiler Ã§Ã¶zÃ¼mlenemedi, hata: ${e.message}")
            UserPermissions()
        }
    }
}
