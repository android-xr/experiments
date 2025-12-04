package com.experiment.jetpackxr.photowall

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.xr.runtime.Config
import androidx.xr.runtime.Session
import androidx.xr.runtime.math.IntSize2d
import androidx.xr.runtime.math.Pose
import androidx.xr.runtime.math.Quaternion
import androidx.xr.runtime.math.Vector3
import androidx.xr.scenecore.PanelEntity
import androidx.xr.scenecore.scene

private const val UI_SCALE = 2.0f

@SuppressLint("RestrictedApi")
class MainActivity : ComponentActivity() {
    private lateinit var appData: AppData
    private val photoEntityMap = mutableMapOf<String, PhotoEntity>()
    private var bodyLockedPoseInitialized = false
    private var movingToTargetPose = false
    private var targetPose = Pose.Identity
    private var sessionHelper: SessionLifecycleHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (sessionHelper == null) {
            sessionHelper = SessionLifecycleHelper(
                this,
                Config(
                    planeTracking = Config.PlaneTrackingMode.HORIZONTAL_AND_VERTICAL,
                    headTracking = Config.HeadTrackingMode.LAST_KNOWN,
                ),
                { session -> onSessionAvailable(session) }
            )
            lifecycle.addObserver(sessionHelper!!)
        }
    }

    private fun onSessionAvailable(session: Session) {
        appData = AppData(this, session, lifecycleScope)

        val view = findViewById<View>(android.R.id.content).rootView
        view.setBackgroundColor(Color.Transparent.toArgb())
        val mainPanelEntity = session.scene.mainPanelEntity
        mainPanelEntity.sizeInPixels = IntSize2d((600 * UI_SCALE).toInt(), (120 * UI_SCALE).toInt())

        setContent {
            Column(
                modifier = Modifier
                    .background(color = Color(0xFF141414))
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MainUI({ modelName -> handleButtonClick(modelName) })
            }
        }

        view.postOnAnimation {
            updateBodyLockedPose(view, session.scene.mainPanelEntity)
        }

    }

    private fun handleButtonClick(modelName: String) {
        val photoEntity = photoEntityMap[modelName]
        if (photoEntity == null) {
            // photoEntity doesn't exist, create it and add to map
            photoEntityMap[modelName] = PhotoEntity(appData, modelName)
        } else {
            // photoEntity exists, destroy it and remove from map
            photoEntity.shutdown()
            photoEntityMap.remove(modelName)
        }
    }

    /**
     * Updates the pose of the body-locked panel based on the user's head position.
     *
     * This function calculates the new position and rotation for the panel to ensure it remains
     * in front of the user, and smoothly transitions to a new position if the user moves too far
     * or turns too much.
     *
     * @param view The View associated with the panel, used for posting animation updates.
     * @param panelEntity The PanelEntity representing the body-locked panel.
     */
    private fun updateBodyLockedPose(view: View, panelEntity: PanelEntity) {
        appData.session.scene.spatialUser.head?.let { headActivityPose ->
            if (!bodyLockedPoseInitialized) {
                // move UI to body-locked pose when spatialUser.head is valid
                panelEntity.setPose(centeredPanelPose())
                bodyLockedPoseInitialized = true
            } else if (movingToTargetPose) {
                val positionOffset =
                    (panelEntity.getPose().translation - targetPose.translation).length
                val rotationOffset =
                    Quaternion.angle(panelEntity.getPose().rotation, targetPose.rotation)
                if (positionOffset < stopMovingOffset && rotationOffset < stopMovingAngle) {
                    // stop moving if close to default pose
                    movingToTargetPose = false
                } else {
                    // smooth move to target pose
                    val panelPos = Vector3.lerp(
                        panelEntity.getPose().translation,
                        targetPose.translation,
                        moveLerpVal
                    )
                    val panelRot = Quaternion.slerp(
                        panelEntity.getPose().rotation,
                        targetPose.rotation,
                        moveLerpVal
                    )
                    panelEntity.setPose(Pose(panelPos, panelRot))
                }
            } else {
                // check if MainUI needs to reposition
                val headPose = headActivityPose.activitySpacePose
                val horizontalForward =
                    Vector3(headPose.forward.x, 0f, headPose.forward.z).toNormalized()
                val horizontalHeadPose = Pose(
                    headPose.translation,
                    Quaternion.fromLookTowards(horizontalForward, Vector3.Up)
                )
                val relPanelPos =
                    horizontalHeadPose.inverse.transformPoint(panelEntity.getPose().translation)
                val horizontalPanelPos = Vector3(relPanelPos.x, 0f, relPanelPos.z)
                val horizontalAngleDegrees =
                    Vector3.degreesBetween(horizontalPanelPos, Vector3.Backward)
                val horizontalPanelDist = horizontalPanelPos.length
                if (horizontalAngleDegrees > repositionAngle ||
                    horizontalPanelDist < repositionDistMin ||
                    horizontalPanelDist > repositionDistMax ||
                    relPanelPos.y < repositionHeightMin ||
                    relPanelPos.y > repositionHeightMax
                ) {
                    val targetPos = horizontalHeadPose.transformPoint(defaultMainUIOffset)
                    val targetRot = Quaternion.fromLookTowards(
                        -horizontalForward,
                        Vector3.Up
                    ) * defaultMainUIRotation
                    targetPose = Pose(targetPos, targetRot)
                    movingToTargetPose = true
                }
            }
        }
        view.postOnAnimation { updateBodyLockedPose(view, panelEntity) }
    }

    /**
     * Calculate a pose to center the panel in front of the user, at a default offset.
     *
     * @return The calculated pose.
     */
    private fun centeredPanelPose(): Pose {
        val headPose = appData.session.scene.spatialUser.head?.activitySpacePose ?: Pose.Identity
        val horizontalForward = Vector3(headPose.forward.x, 0f, headPose.forward.z).toNormalized()
        val horizontalHeadPose =
            Pose(headPose.translation, Quaternion.fromLookTowards(horizontalForward, Vector3.Up))
        val targetPos = horizontalHeadPose.transformPoint(defaultMainUIOffset)
        val targetRot =
            Quaternion.fromLookTowards(-horizontalForward, Vector3.Up) * defaultMainUIRotation
        return Pose(targetPos, targetRot)
    }

    private companion object {
        val defaultMainUIOffset =
            Vector3(0f, -0.4f * GlobalConstants.SCALE_FIX, 0.8f * GlobalConstants.SCALE_FIX)
        val defaultMainUIRotation = Quaternion.fromEulerAngles(-20f, 0f, 0f)
        const val moveLerpVal = 0.02f
        const val repositionAngle = 70f
        const val repositionDistMin = 0.6f * GlobalConstants.SCALE_FIX
        const val repositionDistMax = 1.0f * GlobalConstants.SCALE_FIX
        const val repositionHeightMin = -0.5f * GlobalConstants.SCALE_FIX
        const val repositionHeightMax = -0.3f * GlobalConstants.SCALE_FIX
        const val stopMovingOffset = 0.01f
        const val stopMovingAngle = 1.0f
    }
}

@Composable
fun MainUI(onButtonClick: (String) -> Unit) {
    Surface {
        Row(
            modifier = Modifier.background(Color(0xFF141414)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            ImageButton(R.drawable.thumb1, "models/framed-art-1.glb", onButtonClick)
            ImageButton(R.drawable.thumb2, "models/framed-art-2.glb", onButtonClick)
            ImageButton(R.drawable.thumb3, "models/framed-art-3.glb", onButtonClick)
            ImageButton(R.drawable.thumb4, "models/framed-art-4.glb", onButtonClick)
            ImageButton(R.drawable.thumb5, "models/framed-art-5.glb", onButtonClick)
        }
    }
}

@Composable
fun ImageButton(buttonResourceId: Int, modelName: String, onButtonClick: (String) -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    var isActive by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size((80 * UI_SCALE).dp)
            .hoverable(interactionSource = interactionSource)
            .clickable(interactionSource = interactionSource, indication = null) {
                isActive = !isActive
                onButtonClick(modelName)
            }
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (isPressed) {
            ButtonPressImage()
        } else if (isHovered) {
            ButtonHoverImage()
        }
        ButtonThumbnailImage(buttonResourceId, if (isActive) 0.3f else 1.0f)
        if (isActive) {
            if (isHovered || isPressed) {
                ButtonRestartImage()
            }
        }
    }
}

@Composable
fun ButtonPressImage() {
    Image(
        painter = painterResource(R.drawable.press),
        contentDescription = "",
        modifier = Modifier
            .size((80 * UI_SCALE).dp)
            .fillMaxSize()
    )
}

@Composable
fun ButtonHoverImage() {
    Image(
        painter = painterResource(R.drawable.hover),
        contentDescription = "",
        modifier = Modifier
            .size((80 * UI_SCALE).dp)
            .fillMaxSize()
    )
}

@Composable
fun ButtonThumbnailImage(resourceId: Int, alpha: Float) {
    Image(
        painter = painterResource(resourceId),
        contentDescription = "",
        modifier = Modifier.size((80 * UI_SCALE).dp),
        alpha = alpha
    )
}

@Composable
fun ButtonRestartImage() {
    Image(
        painter = painterResource(R.drawable.restart),
        contentDescription = "",
        modifier = Modifier.size((80 * UI_SCALE).dp)
    )
}
