package com.experiment.jetpackxr.photowall

import androidx.core.net.toUri
import androidx.xr.runtime.Session
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.GltfModel
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.scene
import kotlinx.coroutines.CancellationException
import kotlin.math.PI

/**
 * Session extension function that loads a Gltf model
 *
 * @param modelName The URL or asset-relative path of a binary Gltf (glb) file to load
 * @return The GltfModelEntity created from loaded model
 * @throws CancellationException Model create operation was cancelled
 * @throws Exception: Failed to load model
 */
suspend fun Session.loadGltfModel(modelName: String): GltfModelEntity {
    return try {
        val model = GltfModel.create(this, modelName.toUri())
        val entity = GltfModelEntity.create(this, model)
        entity
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        throw Exception("Failed to load model $modelName", e)
    }
}

/**
 * Session extension function that transforms an activity pose to a perception pose
 *
 * @param activityPose Activity Space pose
 * @return Perception Space pose
 */
fun Session.perceptionPose(activityPose: Pose): Pose {
    return this.scene.activitySpace.transformPoseTo(activityPose, this.scene.perceptionSpace)
}

/**
 * Session extension function that transforms a perception pose to an activity pose
 *
 * @param perceptionPose Perception Space pose
 * @return Activity Space pose
 */
fun Session.activityPose(perceptionPose: Pose): Pose {
    return this.scene.perceptionSpace.transformPoseTo(perceptionPose, this.scene.activitySpace)
}

/**
 * Float extension function to test if a Float is approximately equal to a test value
 *
 * @param testVal The value to test for equality
 * @param epsilon The range the two values can differ and still be considered equal
 * @return True if approximately equal
 */
fun Float.approximatelyEqual(testVal: Float, epsilon: Float = 1.0E-8f): Boolean {
    return this > testVal - epsilon && this < testVal + epsilon
}

/**
 * Vector3 Companion function that returns the angle between two vectors in degrees
 *
 * The docs for androidx.xr.runtime.math.Vector3.angleBetween() states that the return value
 * is in degrees. But currently it returns radians. This function guarantees the return value
 * is in degrees - even if this bug is fixed. It does this by examining the result of angleBetween()
 * from a know angle. If the known angle is in degrees, it uses angleBetween to get degrees between
 * vector1 and vector2. Otherwise it converts radians returned form angleBetween() to degrees.
 *
 * @param vector1 The first vector
 * @param vector2 The second vector
 * @return The angle between the two vectors in degrees
 */
fun Vector3.Companion.degreesBetween(vector1: Vector3, vector2: Vector3): Float {
    if (angleBetween(Vector3.Up, Vector3.Forward).approximatelyEqual(90f))
        return angleBetween(vector1, vector2)
    else
        return angleBetween(vector1, vector2) * (180f / PI.toFloat())
}

/**
 * Constants used throughout the project
 */
object GlobalConstants {
    /**
     * Currently, there is a bug in SceneCore scale. This can be verified with a measuring tape (in
     * passthrough mode on device). If a 1m cube is loaded into scenecore, it will measure 1.75m.
     * If objects are placed 1m apart in code, they will measure 1.75m apart. If a panel is placed
     * 1m in front of head pose, it will measure 1.75m distant from user. SCALE_FIX is used to
     * correct the scale and relative placement of entities. When the scale is fixed in a future
     * version of scenecore, this value can be set to 1.0f
     */
    const val SCALE_FIX = 0.5714f
}

