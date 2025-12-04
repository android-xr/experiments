package com.experiment.jetpackxr.photowall

import android.annotation.SuppressLint
import android.util.Log
import androidx.xr.runtime.Session
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Ray
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.Entity
import androidx.xr.scenecore.GltfModelEntity
import androidx.xr.scenecore.InputEvent
import androidx.xr.scenecore.InteractableComponent
import androidx.xr.scenecore.scene
import kotlinx.coroutines.launch
import java.util.concurrent.Executor
import kotlin.math.abs

class PhotoEntity(val appData : AppData, modelName : String) {
    private val snapPreview = SnapPreview(appData)
    private val entityAnchor = EntityAnchor(appData)
    private lateinit var photoEntity : Entity
    private var modelLoaded = false
    private var dragState = DragState.IDLE
    private var dragPointerType = InputEvent.Pointer.POINTER_TYPE_DEFAULT
    private var dragInitialPointerRotation : Quaternion = Quaternion.Identity
    private var dragInitialPanelRotation : Quaternion = Quaternion.Identity
    private var dragInitialPanelOffset : Vector3 = Vector3.Backward
    private var dragCenterOffset = Vector3.Zero

    init {
        appData.lifecycleCoroutineScope.launch {
            try {
                val gltfModelEntity = appData.session.loadGltfModel(modelName)
                initPhotoEntity(gltfModelEntity)
            } catch (e: Exception) {
                Log.e(TAG, "loadGltfModel failed: $e")
            }
        }
    }

    fun shutdown() {
        snapPreview.hide()
        if (modelLoaded) {
            entityAnchor.clear()
            photoEntity.dispose()
        }
    }

    @SuppressLint("RestrictedApi")
    private fun initPhotoEntity(entity : GltfModelEntity) {
        val headPose = appData.session.scene.spatialUser.head?.activitySpacePose ?: Pose.Identity
        val horizontalViewDir = Vector3(headPose.forward.x, 0f, headPose.forward.z).toNormalized()
        val panelPos = headPose.translation + (horizontalViewDir * initialPanelDistance)
        val panelRot = Quaternion.fromLookTowards(-horizontalViewDir, Vector3.Up)
        entity.parent = appData.session.scene.activitySpace
        entity.setScale(GlobalConstants.SCALE_FIX)
        entity.setPose(Pose(panelPos, panelRot))
        attachInteractableComponent(
            entity,
            appData.session,
            appData.session.activity.mainExecutor
        )
        photoEntity = entity
        modelLoaded = true
    }

    private fun attachInteractableComponent(entity : Entity, session : Session, executor : Executor) {
        val comp = InteractableComponent.create(session, executor) { event ->
            when (event.action) {
                InputEvent.Action.ACTION_DOWN -> {
                    if (dragState == DragState.IDLE) {
                        dragStart(event)
                        dragState = DragState.DRAGGING
                        dragPointerType = event.pointerType
                    }
                }
                InputEvent.Action.ACTION_MOVE -> {
                    if (dragState == DragState.DRAGGING && event.pointerType == dragPointerType) {
                        dragUpdate(event)
                    }
                }
                InputEvent.Action.ACTION_UP -> {
                    if (dragState == DragState.DRAGGING && event.pointerType == dragPointerType) {
                        dragEnd(event)
                        dragState = DragState.IDLE
                    }
                }
                InputEvent.Action.ACTION_CANCEL -> {
                    if (dragState == DragState.DRAGGING && event.pointerType == dragPointerType) {
                        dragCancel()
                        dragState = DragState.IDLE
                    }
                }
            }
        }
        entity.addComponent(comp)
    }

    private fun dragStart(event : InputEvent) {
        // clear any previous anchor
        entityAnchor.clear()

        // prepare for drag
        val pointerRay = Ray(event.origin, event.direction.toNormalized())

        if(event.hitInfoList.isEmpty()) return

        initDrag(pointerRay, event.hitInfoList[0].hitPosition)

        // snap to preview pose if wall found
        val hitPose = snapPreview.show(pointerRay)
        if (hitPose != null) {
            photoEntity.setPose(hitToCenterPose(hitPose))
        }
    }

    private fun dragUpdate(event : InputEvent) {
        val pointerRay = Ray(event.origin, event.direction.toNormalized())
        val hitPose = snapPreview.show(pointerRay)
        if (hitPose != null) {
            photoEntity.setPose(hitToCenterPose(hitPose))
        }
        else {
            // no wall hit, update drag pose
            photoEntity.setPose(updateDrag(pointerRay))
        }
    }

    private fun dragEnd(event : InputEvent) {
        // snap to preview pose or end drag at final pose
        val pointerRay = Ray(event.origin, event.direction.toNormalized())
        val hitPose = snapPreview.show(pointerRay)
        if (hitPose != null) {
            photoEntity.setPose(hitToCenterPose(hitPose))
            entityAnchor.anchor(photoEntity, photoEntity.getPose())
        }
        else {
            // no wall hit, leave photo at final drag pose
            photoEntity.setPose(updateDrag(pointerRay))
        }
        snapPreview.hide()
    }

    private fun dragCancel() {
        // cancel snap, leave photo at last drag pose
        snapPreview.hide()
        photoEntity.setEnabled(true)
    }

    private fun initDrag(pointerRay : Ray, hitPos : Vector3?) {
        dragInitialPanelRotation = photoEntity.getPose().rotation
        dragInitialPanelOffset = photoEntity.getPose().translation - pointerRay.origin
        dragInitialPointerRotation = pointerRotation(pointerRay.direction)
        dragCenterOffset = getCenterOffset(photoEntity, pointerRay, hitPos)
    }

    private fun updateDrag(pointerRay : Ray) : Pose {
        val deltaRotation = pointerRotation(pointerRay.direction) * dragInitialPointerRotation.inverse
        val newPanelRotation = deltaRotation * dragInitialPanelRotation
        val newPanelPos = pointerRay.origin + deltaRotation.times(dragInitialPanelOffset)
        return Pose(newPanelPos, newPanelRotation)
    }

    /**
     * Calculates the offset vector from the hit point to the center of the entity.
     *
     * If the entity is a PanelEntity, hitPos will be valid. If the entity is a GltfModelEntity,
     * hitPos will be null. In that case the hitPos is calculated from the pointer ray
     * intersection with the entity xy plane.
     *
     * @param entity The entity to calculate the offset for.
     * @param ray The ray from the pointer.
     * @param hitPos The hit position of the ray with the entity, if available.
     * @return The offset vector from the hit point to the center of the entity.
     */
    private fun getCenterOffset(entity : Entity, ray : Ray, hitPos : Vector3?) : Vector3 {
        val entityPose = entity.getPose()
        var hitPoint = hitPos
        if (hitPoint == null)
        {
            // if we don't have a hit point, get ray intersection with entity xy plane
            val normal = entityPose.forward
            val denom = normal.dot(ray.direction)
            if (abs(denom) < 0.0001f) {
                // ray parallel to plane, just return zero offset
                return Vector3.Zero
            }
            val distAlongRay = (entityPose.translation - ray.origin).dot(normal) / denom
            hitPoint = ray.origin + (ray.direction * distAlongRay)
        }

        // negate to get vector from hit point to entity center
        return -entityPose.inverse.transformPoint(hitPoint)
    }

    private fun hitToCenterPose(hitPose : Pose) : Pose {
        val previewCenter = hitPose.transformPoint(dragCenterOffset)
        return Pose(previewCenter, hitPose.rotation)
    }

    private fun pointerRotation(rayDir : Vector3) : Quaternion {
        return Quaternion.fromLookTowards(rayDir.toNormalized(), Vector3.Up)
    }

    private companion object {
        enum class DragState {
            IDLE,
            DRAGGING
        }
        const val initialPanelDistance = 1.7f * GlobalConstants.SCALE_FIX
        private const val TAG = "PhotoEntity"
    }


}