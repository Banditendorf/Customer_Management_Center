package com.cmc.customer.permission

import android.content.Context
import android.util.Log
import com.cmc.customer.model.User
import com.cmc.customer.model.UserPermissions
import com.google.gson.Gson

/**
 * PermissionManager: KullanÄ±cÄ± izin bilgisini SharedPreferences ile Ã¶nbelleÄŸe alÄ±r,
 * uygulama boyunca tekil Ã¶rnek (singleton) olarak izni sorgulama imkÃ¢nÄ± sunar.
 */
class PermissionManager private constructor(context: Context) {

    companion object {
        private const val PREFS_NAME = "CMC_prefs"
        private const val KEY_USER = "key_current_user"
        private const val TAG = "PermissionManager"

        @Volatile
        private var instance: PermissionManager? = null

        /**
         * PermissionManager Ã¶rneÄŸini al
         */
        fun getInstance(context: Context): PermissionManager =
            instance ?: synchronized(this) {
                instance ?: PermissionManager(context.applicationContext).also { instance = it }
            }
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    /**
     * Sunucudan dÃ¶nen User nesnesini JSON'a Ã§evirip kaydeder (iÃ§inde permissions da var)
     */
    fun saveUser(user: User) {
        val json = gson.toJson(user)
        prefs.edit()
            .putString(KEY_USER, json)
            .apply()
        Log.d(TAG, "KullanÄ±cÄ± kaydedildi: $json")
    }

    /**
     * Ã–nbelleÄŸe alÄ±nmÄ±ÅŸ User nesnesini dÃ¶ner; yoksa default User()
     */
    fun loadUser(): User {
        val json = prefs.getString(KEY_USER, null)
        return if (json.isNullOrEmpty()) {
            Log.w(TAG, "KayÄ±tlÄ± kullanÄ±cÄ± bulunamadÄ±. VarsayÄ±lan kullanÄ±cÄ± dÃ¶ndÃ¼rÃ¼ldÃ¼.")
            User()
        } else {
            try {
                val user = gson.fromJson(json, User::class.java)
                Log.d(TAG, "KullanÄ±cÄ± yÃ¼klendi: $json")
                user
            } catch (e: Exception) {
                Log.e(TAG, "KullanÄ±cÄ± verisi parse edilemedi. Hata: ${e.message}")
                User()
            }
        }
    }

    /**
     * KaydedilmiÅŸ izinleri dÃ¶ner; null durumlarda gÃ¼venli ÅŸekilde default boÅŸ izin dÃ¶ner
     */
    private fun loadPermissions(): UserPermissions {
        val permissions = loadUser().permissions
        return permissions ?: UserPermissions().also {
            Log.w(TAG, "Permissions null geldi, boÅŸ UserPermissions oluÅŸturuldu.")
        }
    }

    /**
     * Yetki kontrol metotlarÄ±
     */
    fun canManageUsers(): Boolean = loadPermissions().manageUser
    fun canManageMachines(): Boolean = loadPermissions().manageMachine
    fun canManageCompanies(): Boolean = loadPermissions().manageCompany
    fun canManageMaintenance(): Boolean = loadPermissions().manageMaintenance
    fun canManageCategories(): Boolean = loadPermissions().manageCategory
    fun canManageMaterials(): Boolean = loadPermissions().manageMaterial
    fun canCallCustomer(): Boolean = loadPermissions().callCustomer
    fun canViewCompanies(): Boolean = loadPermissions().viewCompanies
    fun canViewMaintenancePlans(): Boolean = loadPermissions().viewMaintenancePlans
    fun canViewPreparationLists(): Boolean = loadPermissions().viewPreparationLists
    fun canViewMaterialsList(): Boolean = loadPermissions().viewMaterialsList
    fun canViewUsers(): Boolean = loadPermissions().viewUsers
    fun canViewNotifications(): Boolean = loadPermissions().viewNotifications

    /**
     * KullanÄ±cÄ± Ã§Ä±kÄ±ÅŸÄ± (logout) iÃ§in Ã¶nbelleÄŸi temizle
     */
    fun clearSession() {
        prefs.edit()
            .remove(KEY_USER)
            .apply()
        Log.i(TAG, "Oturum temizlendi, kullanÄ±cÄ± Ã§Ä±kÄ±ÅŸÄ± yapÄ±ldÄ±.")
    }
}
