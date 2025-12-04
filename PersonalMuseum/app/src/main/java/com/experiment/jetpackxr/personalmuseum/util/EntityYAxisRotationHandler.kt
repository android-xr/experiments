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

import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.InputEvent
import androidx.xr.scenecore.InputEvent.Action
import java.util.function.Consumer
import kotlin.math.abs

/**
 * Implements a grab and rotate behavior, intended to be used as an input event listener passed to an InteractableComponent
 * Currently assumes that you want to rotate about an object's Y axis, but could be generalized to support any axis.
 */
class EntityYAxisRotationHandler(
    /** Entity to rotate */
    val gltfEntity: GltfModelEntity?,
    /** Rate at which to rotate the entity in degrees / meter */
    val linearToAngularMovementScalar: Float = 135.0f,
    /** Optional callback to perform some action when the rotation changes */
    val onEntityRotated: ((Entity, Float) -> Unit)? = null)  : Consumer<InputEvent> {

    companion object  {
        private const val EPSILON = 0.001f
    }

    data class PlaneInteractionData(
        val initialRotationY: Float,
        val interactionPlaneP: Vector3,
        val interactionPlaneN: Vector3,
        val interactionDirection: Vector3
    )

    private var currentInteraction: PlaneInteractionData? = null

    private fun intersectRayWithPlane(rayOrigin: Vector3,
                                      rayDirection: Vector3,
                                      planeNormal: Vector3,
                                      planePoint: Vector3): Vector3? {
        val dirDotN = rayDirection dot planeNormal
        if (abs(dirDotN) < EPSILON) {
            return null
        }
        val t = ((planePoint - rayOrigin) dot planeNormal) / dirDotN
        if (t <= 0.0f) {
            return null
        }
        return (rayDirection * t) + rayOrigin
    }

    override fun accept(inputEvent: InputEvent) {
        when (inputEvent.action) {
            Action.ACTION_DOWN -> {
                // todo- leveraging hitInfo would be a little better than just using the object's translation
                // to construct the plane, but it seems to not be hooked up yet.
                gltfEntity?.let {
                    // when action begins, establish a plane to ray cast onto
                    val interactionPlaneP = it.getPose().translation // inputEvent.hitInfo?.hitPosition
                    val interactionPlaneN = -inputEvent.direction.toNormalized()

                    // the probably won't grab the object exactly at the origin,
                    // so set interactionPlaneP to the initial ray intersection
                    val interactionPlanePAdjusted = intersectRayWithPlane(
                        inputEvent.origin,
                        inputEvent.direction,
                        interactionPlaneN,
                        interactionPlaneP
                    )
                    if (interactionPlanePAdjusted == null) {
                        return
                    }

                    // project the object's up vector onto the plane so we can determine which direction the user moves
                    val entityUpProjOnPlane = Vector3.projectOnPlane(it.getPose().up, interactionPlaneN)
                    if (entityUpProjOnPlane.lengthSquared < EPSILON) {
                        return
                    }

                    // compute a line that we can project future hits onto to determine how far the user moves
                    val interactionDirection = interactionPlaneN.cross(entityUpProjOnPlane.toNormalized())

                    // store the initial rotation of the entity
                    val initialRotationY = gltfEntity.getPose().rotation.eulerAngles.y

                    currentInteraction = PlaneInteractionData(
                        initialRotationY,
                        interactionPlanePAdjusted,
                        interactionPlaneN,
                        interactionDirection
                    )
                }
            }
            Action.ACTION_UP -> {
                currentInteraction = null
            }
            Action.ACTION_MOVE -> {
                val ci = currentInteraction
                if (ci == null) {
                    return
                }

                // find the intersection on the interaction plane
                val p = intersectRayWithPlane(
                    inputEvent.origin,
                    inputEvent.direction,
                    ci.interactionPlaneN,
                    ci.interactionPlaneP
                )
                if (p == null) {
                    return
                }

                // compute a vector to represent movement across the plane
                val mv = p - ci.interactionPlaneP

                // project mv onto interactionDirection to determine how far the user moved
                // (assume interactionDirection is normalized)
                val distance = (mv dot ci.interactionDirection) / ci.interactionDirection.lengthSquared

                gltfEntity?.let {
                    // update the entity's rotation accordingly
                    val currentRotationEuler: Vector3 = it.getPose().rotation.eulerAngles
                    val newRotationY = ci.initialRotationY - (distance * linearToAngularMovementScalar)

                    it.setPose(Pose(
                        it.getPose().translation,
                        Quaternion.fromEulerAngles(currentRotationEuler.x, newRotationY, currentRotationEuler.z)
                    ))

                    onEntityRotated?.invoke(it, newRotationY)
                }
            }
        }
    }
}