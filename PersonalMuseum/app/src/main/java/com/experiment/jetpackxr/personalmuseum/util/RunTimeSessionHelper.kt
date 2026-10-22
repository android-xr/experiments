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
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.xr.runtime.Session
import androidx.xr.runtime.SessionCreateApkRequired
import androidx.xr.runtime.SessionCreateSuccess
import androidx.xr.runtime.SessionCreateTimedOut
import androidx.xr.runtime.SessionCreateUnknownError
import androidx.xr.runtime.SessionCreateUnsupportedDevice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Observer class to manage the lifecycle of the Jetpack XR Runtime Session based on the lifecycle
 * owner (activity).
 */
class RunTimeSessionHelper(
    internal val onCreateCallback: (Session) -> Unit,
) : DefaultLifecycleObserver {

    internal lateinit var session: Session

    override fun onCreate(owner: LifecycleOwner) {
        // Sessions can only be instantiated with an instance of [ComponentActivity].
        check(owner is ComponentActivity) { "owner is not an instance of ComponentActivity" }

        owner.lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                Session.create(
                    context = owner,
                    coroutineContext = coroutineContext,
                    lifecycleOwner = owner
                )
            }
            when (result) {
                is SessionCreateSuccess -> {
                    session = result.session
                    onCreateCallback.invoke(session)
                }
                is SessionCreateApkRequired -> {
                    Log.e(TAG, "Session APK required: ${result.requiredApk}")
                }
                is SessionCreateUnsupportedDevice -> {
                    Log.e(TAG, "Session unsupported device")
                }
                is SessionCreateTimedOut -> {
                    Log.e(TAG, "Session creation timed out")
                }
                is SessionCreateUnknownError -> {
                    Log.e(TAG, "Session creation unknown error: ${result.errorMessage}")
                }
                else -> {
                    Log.e(TAG, "Session creation failed: $result")
                }
            }
        }
    }

    companion object {
        private val TAG = this::class.simpleName
    }
}