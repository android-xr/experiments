package com.experiment.jetpackxr.photowall

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.xr.runtime.Config
import androidx.xr.runtime.Session
import androidx.xr.runtime.SessionConfigureConfigurationNotSupported
import androidx.xr.runtime.SessionConfigureSuccess
import androidx.xr.runtime.SessionCreateApkRequired
import androidx.xr.runtime.SessionCreateSuccess
import androidx.xr.runtime.SessionCreateUnsupportedDevice

/**
 * Observer class to manage the lifecycle of the JXR Runtime Session based on the lifecycle owner
 * (activity).
 */
@SuppressLint("RestrictedApi")
class SessionLifecycleHelper(
    val activity: ComponentActivity,
    val config: Config = Config(),
    val onSessionAvailable: (Session) -> Unit = {}
) : DefaultLifecycleObserver {
    private lateinit var session: Session
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(owner: LifecycleOwner) {
        registerRequestPermissionLauncher(activity)
        createSession()
    }

    private fun createSession() {
        try {
            when (val result = Session.create(activity)) {
                is SessionCreateSuccess -> {
                    session = result.session
                    configureSession()
                }
                is SessionCreateApkRequired -> {
                    Log.e(TAG, "Session APK required")
                }
                is SessionCreateUnsupportedDevice -> {
                    Log.e(TAG, "Session unsupported device")
                }
            }

        } catch (e: SecurityException) {
            requestPermissionLauncher.launch(getMissingPermissions().toTypedArray())
        }

    }

    @SuppressLint("RestrictedApi")
    private fun configureSession() {
        try {
            when (val configResult = session.configure(config)) {
                is SessionConfigureConfigurationNotSupported -> {
                    showErrorMessage("Session configuration not supported.")
                    activity.finish()
                }
                is SessionConfigureSuccess -> {
                    onSessionAvailable(session)
                }
                else -> Log.e(TAG, "Session configuration failed.")
            }
        } catch (e: SecurityException) {
            requestPermissionLauncher.launch(getMissingPermissions().toTypedArray())
        }

    }

    private fun getMissingPermissions() : List<String> {
        val missingPermissionList = mutableListOf<String>()
        for (permission in requiredPermissions) {
            if (!isPermissionGranted(permission)) {
                missingPermissionList.add(permission)
            }
        }
        return  missingPermissionList
    }

    private fun isPermissionGranted(permission : String) :Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun registerRequestPermissionLauncher(activity: ComponentActivity) {
        requestPermissionLauncher =
            activity.registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val allPermissionsGranted = permissions.all { it.value }
                if (!allPermissionsGranted) {
                    Toast.makeText(
                        activity,
                        "Required permissions were not granted, closing activity. ",
                        Toast.LENGTH_LONG,
                    )
                        .show()
                    activity.finish()
                } else {
                    activity.recreate()
                }
            }
    }

    companion object {
        private const val TAG = "SessionLifecycleHelper"
        private val requiredPermissions : List<String> = listOf(
            "android.permission.SCENE_UNDERSTANDING_COARSE",
            "android.permission.HEAD_TRACKING",
            "android.permission.HAND_TRACKING"
        )
    }

    private fun <F> showErrorMessage(error: F) {
        Log.e(TAG, error.toString())
        Toast.makeText(activity, error.toString(), Toast.LENGTH_LONG).show()
    }
}