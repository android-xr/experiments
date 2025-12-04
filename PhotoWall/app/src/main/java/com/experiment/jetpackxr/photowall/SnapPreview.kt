package com.experiment.jetpackxr.photowall

import android.annotation.SuppressLint
import android.util.Log
import androidx.xr.arcore.HitResult
import androidx.xr.arcore.Plane
import androidx.xr.arcore.hitTest
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Ray
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.scene
import kotlinx.coroutines.launch

/**
 * Manages the display of a grid preview for placing photos on a wall.
 *
 * This class handles:
 * - Loading and displaying a grid model.
 * - Performing raycasts to detect wall surfaces.
 * - Snapping the grid to the wall surface at discrete positions.
 * - Hiding and showing the grid.
 *
 * @property appData The application data containing the ARCore session and SceneCore session.
 */
@SuppressLint("RestrictedApi")
class SnapPreview(appData : AppData) {
    private val session = appData.session
    private var wallGridEntity : GltfModelEntity? = null
    private var lastPose : Pose? = null
    private var lastPlaneId : Int = 0

    init {
        appData.lifecycleCoroutineScope.launch {
            try {
                wallGridEntity = session.loadGltfModel(gridModelName)
                wallGridEntity?.parent = session.scene.activitySpace
                wallGridEntity?.setScale(entityScale)
                wallGridEntity?.setEnabled(false)
            } catch (e : Exception) {
                Log.e(TAG, "loadGltfModel failed: $e")
            }
        }
    }

    /**
     * Shows the grid preview at the intersection of a ray with a wall.
     *
     * @param scRay The ray from SceneCore.
     * @return The pose of the grid if a hit is found, otherwise null.
     */
    fun show(scRay : Ray) : Pose? {
        val hitResult = wallHitTest(scRay)
        if (!hitResult.isHit) {
            hideWallGrid()
            return null
        }
        else {
            val hitPose = hitResult.pose
            showWallGrid(hitResult)
            return hitPose
        }
    }

    /**
     * Hides the grid preview.
     */
    fun hide() {
        hideWallGrid()
    }

    /**
     * Shows the grid preview at the given hit result.
     *
     * @param hitResult The hit result from the wall hit test.
     */
    private fun showWallGrid(hitResult : WallHitResult) {
        val hitPose = hitResult.pose
        if (lastPose == null || hitResult.planeId != lastPlaneId) {
            // snap to pose if previously hidden or moved to another wall plane
            wallGridEntity?.setPose(hitPose)
            wallGridEntity?.setEnabled(true)
            lastPose = hitPose
            lastPlaneId = hitResult.planeId
        }
        else {
            // else, update grid - only move grid in grid-sized steps
            val refPose = lastPose!!
            val relOffset = refPose.inverse.transformPoint(hitPose.translation)
            val xSteps = (relOffset.x / gridStepSize).toInt()
            val ySteps = (relOffset.y / gridStepSize).toInt()
            if (xSteps != 0 || ySteps != 0) {
                val xPos = xSteps * gridStepSize
                val yPos = ySteps * gridStepSize
                val relPos = Vector3(xPos, yPos, relOffset.z)
                val newPos = refPose.transformPoint(relPos)
                lastPose = Pose(newPos, hitPose.rotation)
                wallGridEntity?.setPose(lastPose!!)
                wallGridEntity?.setEnabled(true)
            }
        }
    }

    /**
     * Hides the grid preview and resets the last pose and plane ID.
     */
    private fun hideWallGrid() {
        wallGridEntity?.setEnabled(false)
        lastPose = null
        lastPlaneId = 0
    }

    /**
     * Performs a hit test against the ARCore session trackables, filtering for wall planes.
     *
     * @param scRay The SceneCore ray to test.
     * @return A WallHitResult indicating if a wall was hit, and its pose and plane.
     */
    private fun wallHitTest(scRay : Ray) : WallHitResult {
        // convert activity space ray to perception space ray
        val scPose = Pose(scRay.origin, Quaternion.fromLookTowards(scRay.direction, Vector3.Up))
        val xrPose = session.perceptionPose(scPose)
        val xrRay = Ray(xrPose.translation, xrPose.backward) // backward is actually the "LookToward" direction

        // get all trackables intersecting with ray
        val hitResults: List<HitResult> = try {
            hitTest(session, xrRay)
        } catch (e : Exception) {
            /**
             * sometimes hitTest generates exceptions:
             *
             * java.lang.IllegalStateException: Trackable not found.
             * java.lang.IllegalArgumentException: No Active Trackable found for the given hit result.
             *
             * These seem like internal exceptions that should not be propagated. Just treat as no
             * hit results in this case.
             */
            Log.w(TAG, "wallHitTest() hitTest exception e:$e")
            emptyList()
        }

        // find nearest wall plane in direction of ray
        val planeHit = hitResults.firstOrNull {
            it.trackable is Plane &&  // only Planes
            (it.trackable as Plane).state.value.label == Plane.Label.WALL &&  // only Walls
            (it.hitPose.translation - xrRay.origin).dot(xrRay.direction) > 0 // only positive distance along ray
        }

        // if wall found, return activity space pose. Otherwise, return no hit
        if (planeHit != null) {
            val flushPose = session.activityPose(planeHit.hitPose)
            val verticalRotation = Quaternion.fromLookTowards(flushPose.up, Vector3.Up)
            val wallPose = Pose(flushPose.translation, verticalRotation)
            return WallHitResult(true, wallPose, planeHit.trackable as Plane, planeHit.trackable.hashCode())
        }
        else {
            return WallHitResult(false)
        }
    }

    private companion object {
        data class WallHitResult(
            val isHit : Boolean,
            val pose : Pose = Pose.Identity,
            val plane : Plane? = null,
            val planeId : Int = 0)

        private val gridModelName = "models/PhotoWallGrid-30.glb"
        private val gridSpacing = 0.137025f // specific grid spacing for PhotoWallGrid-30

        private val gridScale = 1.0f // grid scale for base model size
        private val entityScale = gridScale * GlobalConstants.SCALE_FIX
        private val gridStepSize = gridSpacing * entityScale
        private const val TAG = "SnapPreview"
    }
}