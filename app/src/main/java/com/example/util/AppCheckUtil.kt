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
private const val FALLBACK_RECAPTCHA_SITE_KEY = "6Lfx1wYuAAAAAPxebD6qi4uYFKGBE2dKHsjrh8_u"

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
 * When running in the AI Studio cloud runner or testing harnesses, the launch intent
 * provides a registered debug token, so DebugAppCheckProviderFactory is installed.
 * When running as a standalone APK on a user's phone, no intent debug token exists,
 * so RecaptchaAppCheckProviderFactory is installed using the registered site key.
 * This prevents the "Firebase app invalid token" rejection.
 */
fun configureAppCheck(context: Context, intent: Intent?) {
    try {
        val app = ensureFirebaseApp(context)
        val persistenceKey = app.persistenceKey
        val prefs = context.getSharedPreferences(
            "$DEBUG_STORE_PREFIX$persistenceKey",
            Context.MODE_PRIVATE
        )

        // 1. Check if a debug token was delivered via Intent (AI Studio preview / automated test runner)
        val intentDebugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
        if (!intentDebugToken.isNullOrEmpty()) {
            prefs.edit()
                .putString(DEBUG_SECRET_KEY, intentDebugToken)
                .commit()
            Log.d(TAG, "App Check debug token stored from launch Intent.")
        }

        // 2. Resolve reCAPTCHA Enterprise site key
        val resId = context.resources.getIdentifier(RECAPTCHA_SITE_KEY_RES, "string", context.packageName)
        val siteKey = if (resId != 0) {
            val key = context.getString(resId)
            if (key.isNotBlank()) key else FALLBACK_RECAPTCHA_SITE_KEY
        } else {
            FALLBACK_RECAPTCHA_SITE_KEY
        }

        // 3. Select appropriate provider:
        // If an intent debug token is provided (AI Studio environment), use DebugAppCheckProviderFactory.
        // Otherwise, for real devices and standalone APKs, use RecaptchaAppCheckProviderFactory.
        if (!intentDebugToken.isNullOrEmpty()) {
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Log.d(TAG, "DebugAppCheckProviderFactory installed via Intent debug token.")
        } else if (siteKey.isNotBlank()) {
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                RecaptchaAppCheckProviderFactory.getInstance(siteKey)
            )
            Log.d(TAG, "RecaptchaAppCheckProviderFactory installed with registered site key.")
        } else {
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Log.d(TAG, "DebugAppCheckProviderFactory installed as fallback.")
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize Firebase App Check", e)
    }
}

fun saveCustomDebugToken(context: Context, token: String) {
    val app = ensureFirebaseApp(context)
    val persistenceKey = app.persistenceKey
    val prefs = context.getSharedPreferences(
        "$DEBUG_STORE_PREFIX$persistenceKey",
        Context.MODE_PRIVATE
    )
    prefs.edit().putString(DEBUG_SECRET_KEY, token.trim()).commit()
}
