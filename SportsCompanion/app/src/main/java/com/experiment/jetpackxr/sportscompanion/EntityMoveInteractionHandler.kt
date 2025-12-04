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

package com.experiment.jetpackxr.sportscompanion

import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.InputEvent
import androidx.xr.scenecore.InputEvent.Action
import java.util.function.Consumer
import kotlin.math.abs

/**
 * Handles user input events to move an [Entity] in 3D space with smooth acceleration and deceleration.
 *
 * This listener processes [InputEvent]s to enable dragging an entity. It calculates
 * the movement based on the initial hit point, desired linear acceleration, and an optional dead zone.
 *
 * @property entity The [Entity] that this listener will move.
 * @property linearAcceleration The rate at which the entity will accelerate and decelerate during movement, in meters per second squared.
 * @property deadZone The minimum distance (in meters) the input must move from the initial touch point before movement is initiated. This helps prevent accidental movement from small jitters. Defaults to 0.0f.
 * @property onInputEventBubble An optional [Consumer] to which unhandled input events (like taps if no movement occurred) are bubbled. Defaults to null.
 * @property onMovementStarted An optional lambda function that is invoked when the entity starts moving (i.e., after the dead zone is crossed). Defaults to null.
 */
class EntityMoveInteractionHandler(
    val entity: Entity,
    val linearAcceleration: Float,
    val deadZone: Float = 0.0f,
    val onInputEventBubble: Consumer<InputEvent>? = null,
    val onMovementStarted: (() -> Unit)? = null
) : Consumer<InputEvent> {

    companion object{
    private const val EPSILON = 0.001f
}

    /**
     * Stores data related to an ongoing interaction (drag) with the entity.
     *
     * @property initialHitPoint The point in world space where the input ray first intersected the interaction plane.
     * @property initialHitDistance The distance from the input ray's origin to the [initialHitPoint].
     * @property initialHitOffsetFromObjOrigin The vector from the entity's origin to the [initialHitPoint].
     * @property timeStartNs The system time in nanoseconds when the interaction started.
     * @property pointerType The type of pointer (e.g., hand, controller) that initiated the interaction.
     * @property lastUpdateTimeNs The system time in nanoseconds of the last movement update.
     * @property currentLinearVelocity The current linear velocity of the entity in meters per second.
     * @property performedMove A flag indicating whether the entity has moved beyond the [deadZone].
     */
    data class InteractionData(
        val initialHitPoint: Vector3,
        val initialHitDistance: Float,
        val initialHitOffsetFromObjOrigin: Vector3,
        val timeStartNs: Long,
        val pointerType: Int,

        var lastUpdateTimeNs: Long = timeStartNs,
        var currentLinearVelocity: Double = 0.0,
        var performedMove: Boolean = false
    )

    private var currentInteraction: InteractionData? = null

    /**
     * Calculates the intersection point of a ray with a plane.
     *
     * @param rayOrigin The origin point of the ray.
     * @param rayDirection The direction vector of the ray.
     * @param planeNormal The normal vector of the plane.
     * @param planePoint A point lying on the plane.
     * @return The intersection point as a [Vector3] if the ray intersects the plane in front of its origin, otherwise null.
     */
    private fun intersectRayWithPlane(rayOrigin: Vector3, rayDirection: Vector3, planeNormal: Vector3, planePoint: Vector3): Vector3? {
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

    /**
     * Processes incoming input events to handle entity movement.
     *
     * - On [Action.ACTION_DOWN]: Initializes the interaction if the input ray hits an
     *   interaction plane defined at the entity's position.
     * - On [Action.ACTION_UP]: Finalizes the interaction. If no significant movement
     *   occurred (less than [deadZone]), the event is bubbled to [onInputEventBubble]
     *   to be handled as a potential tap.
     * - On [Action.ACTION_MOVE]: Updates the entity's position based on the input's
     *   current location, applying smooth acceleration and deceleration towards the target position.
     *   If the movement is within the [deadZone], it is ignored.
     *
     * @param inputEvent The [InputEvent] to process.
     */
    override fun accept(inputEvent: InputEvent) {

        if (inputEvent.action == Action.ACTION_DOWN) {
            // check if the user tried to simultaneously interact with the object with multiple inputs/hands
            val ci = this.currentInteraction
            if (ci != null && ci.pointerType != inputEvent.pointerType) {
                return
            }

            // inputEvent.hitInfo info doesn't appear to be available yet, so for now construct a plane to approximate the initial ray hit location
            val interactionPlaneP = entity.getPose().translation
            val interactionPlaneN = -inputEvent.direction.toNormalized()

            val hitPoint = intersectRayWithPlane(inputEvent.origin, inputEvent.direction, interactionPlaneN, interactionPlaneP)
            if (hitPoint == null) {
                return
            }

            this.currentInteraction = InteractionData(
                initialHitPoint = hitPoint,
                initialHitDistance = (hitPoint - inputEvent.origin).length,
                initialHitOffsetFromObjOrigin = hitPoint - interactionPlaneP,
                timeStartNs = System.nanoTime(),
                pointerType = inputEvent.pointerType)

        } else if (inputEvent.action == Action.ACTION_UP) {
            val ci = this.currentInteraction
            if (ci == null || !ci.performedMove || ci.pointerType != inputEvent.pointerType) {
                // bubble the event as a tap if it wasn't handled
                this.onInputEventBubble?.accept(inputEvent)
            }

            if (ci != null && ci.pointerType == inputEvent.pointerType) {
                this.currentInteraction = null
            }

        } else if (inputEvent.action == Action.ACTION_MOVE) {
            val ci = this.currentInteraction
            if (ci == null || ci.pointerType != inputEvent.pointerType) {
                return
            }

            val targetPosition = (inputEvent.direction.toNormalized() * ci.initialHitDistance) + inputEvent.origin

            if (!ci.performedMove) {
                if ((targetPosition - ci.initialHitPoint).lengthSquared < (deadZone * deadZone)) {
                    return
                }

                this.currentInteraction?.performedMove = true
                this.onMovementStarted?.invoke()
            }

            val currentTimeNs = System.nanoTime()
            val deltaTimeNs = (currentTimeNs - ci.lastUpdateTimeNs)
            val deltaTimeS = deltaTimeNs.toDouble() * 0.000000001

            this.currentInteraction?.lastUpdateTimeNs = currentTimeNs

            // todo- consider accounting for rotation

            val targetEntityPosition = targetPosition - ci.initialHitOffsetFromObjOrigin
            val displacementToGoal = targetEntityPosition - this.entity.getPose().translation

            if (displacementToGoal.lengthSquared < EPSILON) {
                return
            }

            val distanceToStop = (ci.currentLinearVelocity * ci.currentLinearVelocity) / (2.0f * linearAcceleration)
            val distanceToGoal = displacementToGoal.length

            val linearAccel: Double =
                if (distanceToStop >= distanceToGoal) {
                    // need to slow down
                    -(ci.currentLinearVelocity * ci.currentLinearVelocity) / (2.0f * distanceToGoal)
                } else {
                    // ok to speed up
                    linearAcceleration.toDouble()
                }

            val linearVel = ci.currentLinearVelocity + (linearAccel * deltaTimeS)
            val linearDisplacement = linearVel * deltaTimeS

            if (abs(linearDisplacement) >= distanceToGoal) {
                this.entity.setPose(Pose(targetEntityPosition, this.entity.getPose().rotation))
                this.currentInteraction?.currentLinearVelocity = 0.0
                return
            }

            val entityPosition = this.entity.getPose().translation +
                    (displacementToGoal.toNormalized() * linearDisplacement.toFloat())
            this.currentInteraction?.currentLinearVelocity = linearVel

            this.entity.setPose(Pose(entityPosition, this.entity.getPose().rotation))

        } else {
            this.onInputEventBubble?.accept(inputEvent)
        }
    }
}