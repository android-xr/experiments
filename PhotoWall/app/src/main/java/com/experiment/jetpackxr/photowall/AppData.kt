package com.experiment.jetpackxr.photowall

import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.xr.runtime.Session

data class AppData(
    val context : Context, // Main Activity Context
    val session: Session,
    val lifecycleCoroutineScope : LifecycleCoroutineScope
)
