package com.example.roomie.core.sharedpreferences

import android.content.Context
import android.content.SharedPreferences
import androidx.core.os.persistableBundleOf
import androidx.lifecycle.LiveData
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

/**
 * singleton Object
 */

object SecureStorage : LiveData<String>() {

    /**
     * @param mPreferences
     * @param masterKeyAlias The alias of the master key to use - for encryption (used to encrypt data encryption keys for encrypting files and preference)
     * @param TOKEN_KEY the associated value to the key in which the token is stored
     * @param authenticationState to check if token is stored in shared pref or not to get the AuthenticationState of the user
     */
    private lateinit var mPreferences: SharedPreferences
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val TOKEN_KEY = "auth_token"
    private val User_KEY = "user_id"
    private lateinit var authenticationState: AuthenticationState

    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED, INVALID_AUTHENTICATION
    }

    /**
     * initialize the EncryptedSharedPreferences
     * An implementation of SharedPreferences that encrypts keys and values to store sensitive data securely
     */
    fun init (context: Context) {

        /**
         * @param fileName: The name of the file to open
         * @param masterKeyAlias: The alias of the master key to use.
         * @param AES256_SIV: The scheme to use for encrypting keys.
         * @param AES256_GCM: he scheme to use for encrypting values.
         */
        mPreferences = EncryptedSharedPreferences.create(
            "secret_shared_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
        if (getAuthToken() != "" && getAuthToken() != null) {
            authenticationState = AuthenticationState.AUTHENTICATED
        } else {
            authenticationState = AuthenticationState.UNAUTHENTICATED
        }
    }

    /**
     * stores the auth token in the EncryptedSharedPreferences for user authorization
     * @param token the actual token to identify the user
     * @param authenticationState is set to AUTHENTICATED when an valid toke is stored
     */
    fun setAuthToken (token: String, userId: Int, authenticationState: AuthenticationState) {
        val preferencesEditor = mPreferences.edit()
        preferencesEditor.putString(TOKEN_KEY, token)
        SecureStorage.authenticationState = authenticationState
        preferencesEditor.putInt(User_KEY, userId)
        preferencesEditor.apply()

    }

    /**
     * to get the auth token for user authorization and authentication
     */
    fun getAuthToken (): String? {
        return mPreferences.getString(TOKEN_KEY, null)
    }

    /**
     * to get the auth token for user authorization and authentication
     */
    fun getUserId (): Int? {
        return mPreferences.getInt(User_KEY, 0)
    }

    /**
     * checks if the user is AUTHENTICATED or not
     */
    fun getAuthenticationState () : AuthenticationState {
        return authenticationState
    }

    /**
     * Interface definition for a callback to be invoked when a shared preference is changed.
     * Called when a shared preference is changed, added, or removed.
     */
    private val mTokenSharedPreferenceListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences: SharedPreferences?, key: String? ->
            if (key == TOKEN_KEY) {
                value = getAuthToken()!!
                AuthenticationState.AUTHENTICATED
            }
        }

    /**
     * Called when an active observer is observing the data
     */
    override fun onActive() {
        super.onActive()
        value = mPreferences.getString(TOKEN_KEY, null)
        mPreferences.registerOnSharedPreferenceChangeListener(mTokenSharedPreferenceListener)
    }

    /**
     * Called when no active observer is observing the data
     */
    override fun onInactive() {
        super.onInactive()
        mPreferences.unregisterOnSharedPreferenceChangeListener(mTokenSharedPreferenceListener)
    }

}