/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.experiment.jetpackxr.personalmuseum.util


import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.xr.runtime.Session
import androidx.xr.runtime.SessionCreateApkRequired
import androidx.xr.runtime.SessionCreateSuccess
import androidx.xr.runtime.SessionCreateUnsupportedDevice
import androidx.xr.runtime.SessionResumeSuccess

/**
 * Observer class to manage the lifecycle of the Jetpack XR Runtime Session based on the lifecycle
 * owner (activity).
 */
class RunTimeSessionHelper(
    internal val onCreateCallback: (Session) -> Unit,
) : DefaultLifecycleObserver {

    internal lateinit var session: Session
    internal lateinit var requestPermissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(owner: LifecycleOwner) {
        // Sessions can only be instantiated with an instance of [ComponentActivity].
        check(owner is ComponentActivity) { "owner is not an instance of ComponentActivity" }
        when (val result = Session.create(owner)) {
            is SessionCreateSuccess -> {
                session = result.session
                onCreateCallback.invoke(session)
            }
            is SessionCreateApkRequired -> {
                Log.e(TAG, "Session APK required")
            }
            is SessionCreateUnsupportedDevice -> {
                Log.e(TAG, "Session unsupported device")
            }
        }
    }

    companion object {
        private val TAG = this::class.simpleName
    }
}