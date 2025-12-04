package com.experiment.jetpackxr.photowall

import android.util.Log
import androidx.xr.arcore.Anchor
import androidx.xr.arcore.AnchorCreateResourcesExhausted
import androidx.xr.arcore.AnchorCreateSuccess
import androidx.xr.runtime.TrackingState
import androidx.xr.runtime.math.Pose
import androidx.xr.scenecore.Entity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class EntityAnchor(val appData : AppData) {
    private var anchor : Anchor? = null
    private var anchorUpdateJob : Job? = null

    fun anchor(entity : Entity, pose : Pose) {
        // remove any previous anchor
        clear()

        // create new anchor, update entity pose when anchor pose changes
        val arPose = appData.session.perceptionPose(pose)
        val result = Anchor.create(appData.session, arPose)
        if (result is AnchorCreateSuccess) {
            anchor = result.anchor
            anchorUpdateJob = CoroutineScope(SupervisorJob()).launch {
                result.anchor.state.collect { state ->
                    if (state.trackingState == TrackingState.TRACKING) {
                        entity.setPose(appData.session.activityPose(state.pose))
                    }
                }
            }
        } else if(result is AnchorCreateResourcesExhausted) {
            Log.w(TAG, "Failed to create anchor: Anchor pool exhausted")
        }
    }

    fun clear() {
        anchorUpdateJob?.cancel()
        anchorUpdateJob = null
        anchor?.detach()
        anchor = null
    }

    companion object {
        private const val TAG = "EntityAnchor"
    }
}