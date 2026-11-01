package com.example.util

import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.BuildConfig
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory

private const val TAG = "AppCheckInit"

private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

/**
 * Ensures FirebaseApp is reliably initialized in all environments,
 * using automatic resource lookup first and falling back to explicit
 * FirebaseOptions if the app was built without processed resources.
 */
fun ensureFirebaseApp(context: Context): FirebaseApp {
    if (FirebaseApp.getApps(context).isNotEmpty()) {
        return FirebaseApp.getInstance()
    }

    try {
        val app = FirebaseApp.initializeApp(context)
        if (app != null) {
            Log.d(TAG, "FirebaseApp initialized from resources successfully.")
            return app
        }
    } catch (e: Exception) {
        Log.w(TAG, "Standard Firebase initialization from resources failed, applying fallback options", e)
    }

    // Explicit fallback options to guarantee FirebaseApp is initialized
    // even in custom CI/CD, GitHub Actions, or when google-services.json was not generated into resources
    val options = FirebaseOptions.Builder()
        .setApplicationId("1:203682204759:android:2800cb6c8a3ae1a226a712")
        .setApiKey("AIzaSyD9kUfOz3Eh5F6cyNUXR_RQzjUZJT780OI")
        .setProjectId("gen-lang-client-0819178339")
        .setGcmSenderId("203682204759")
        .setStorageBucket("gen-lang-client-0819178339.firebasestorage.app")
        .build()

    val app = FirebaseApp.initializeApp(context.applicationContext, options)
    Log.d(TAG, "FirebaseApp initialized from fallback options successfully.")
    return app
}

/**
 * Configures Firebase App Check for AI Logic.
 * Uses DebugAppCheckProviderFactory in debug builds with the token from launch Intent,
 * and RecaptchaAppCheckProviderFactory in release builds.
 */
fun configureAppCheck(context: Context, intent: Intent?) {
    try {
        val app = ensureFirebaseApp(context)

        if (BuildConfig.DEBUG) {
            val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
            if (!debugToken.isNullOrEmpty()) {
                val persistenceKey = app.persistenceKey
                val prefs = context.getSharedPreferences(
                    "$DEBUG_STORE_PREFIX$persistenceKey",
                    Context.MODE_PRIVATE
                )
                // Synchronous commit required so the token is written before DebugAppCheckProviderFactory reads it
                prefs.edit()
                    .putString(DEBUG_SECRET_KEY, debugToken)
                    .commit()
                Log.d(TAG, "App Check debug secret configured successfully.")
            } else {
                Log.w(TAG, "No debug token found in Intent extra.")
            }

            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Log.d(TAG, "DebugAppCheckProviderFactory installed.")
        } else {
            val resId = context.resources.getIdentifier(RECAPTCHA_SITE_KEY_RES, "string", context.packageName)
            if (resId != 0) {
                val siteKey = context.getString(resId)
                if (siteKey.isNotBlank()) {
                    FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                        RecaptchaAppCheckProviderFactory.getInstance(siteKey)
                    )
                    Log.d(TAG, "RecaptchaAppCheckProviderFactory installed.")
                } else {
                    Log.e(TAG, "recaptcha_site_key resource string is blank.")
                }
            } else {
                Log.e(TAG, "recaptcha_site_key string resource not found in package.")
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize Firebase App Check", e)
    }
}
