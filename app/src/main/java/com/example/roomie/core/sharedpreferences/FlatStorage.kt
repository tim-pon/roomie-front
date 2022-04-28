package com.example.roomie.core.sharedpreferences

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

object FlatStorage : LiveData<Int>() {

    private lateinit var mPreferences: SharedPreferences
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    private val TOKEN_KEY = "flat_id"
    private val TOKEN_KEY_2 = "flat_name"

    /**
     * initialize the EncryptedSharedPreferences
     * An implementation of SharedPreferences that encrypts keys and values to store sensitive data securely
     */
    fun init(context: Context) {
        mPreferences = EncryptedSharedPreferences.create(
            "flat_shared_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun setFlatId(token: Int) {
        val preferencesEditor = mPreferences.edit()
        preferencesEditor.putInt(TOKEN_KEY, token)
        preferencesEditor.apply()
    }


    fun setFlatName(token: String) {
        val preferencesEditor = mPreferences.edit()
        preferencesEditor.putString(TOKEN_KEY_2, token)
        preferencesEditor.apply()
    }


    /**
     * get flat id
     */
    fun getFlatId(): Int {
        return mPreferences.getInt(TOKEN_KEY, 0)
    }

    /**
     * to get the flat name
     */
    fun getFlatName(): String? {
        return mPreferences.getString(TOKEN_KEY_2, "Unknown")
    }

    /**
     * Interface definition for a callback to be invoked when a shared preference is changed.
     * Called when a shared preference is changed, added, or removed.
     */
    val mTokenSharedPreferenceListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences: SharedPreferences?, key: String? ->
            if (key == TOKEN_KEY) {
                value = getFlatId()
            }
        }

    /**
     * Called when an active observer is observing the data
     */
    override fun onActive() {
        super.onActive()
        value = mPreferences.getInt(TOKEN_KEY, 0)
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