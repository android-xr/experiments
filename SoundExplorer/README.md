# Sound Explorer

Sound Explorer is an immersive application implemented entirely using the Jetpack XR SDK that 
demonstrates the spatial audio capabilities of AndroidXR. Users are able to build a musical 
composition by combining different sounds represented by 3d objects in space. Users can change 
sounds and manipulate their position in 3d space using hand gestures.

For more information, please [read the documentation](https://developer.android.com/develop/xr).

# Screenshots

<img src="screenshots/screenshot_01.png" alt="Screenshot">
<img src="screenshots/screenshot_02.png" alt="Screenshot">
<img src="screenshots/screenshot_03.png" alt="Screenshot">

# Features

This experiment demonstrates:

- Spatial Audio using the SpatialAudioTrack API's
- Loading and manipulating GLTF models as entities in a scene graph
- Scene graph manipulation using the JetPack SceneCore library
- Custom interactions with 3d objects
- 2D UI elements built using Jetpack Compose
- 3D UI elements built with using Jetpack Compose for XR.

## Spatial Audio

Sound Explorer is an immersive experience where the user places objects throughout their space to 
build a musical composition from 18 audio samples. SpatialAudioTrack apis are used for playback. 
Users perceive each sample as playing back from its associated object in 3d space.

## Loading GLTF Models as Scene Graph Entities

The sounds that make up the user’s composition are represented visually as different 3d models. 
Models are loaded and added to the scene graph as instances of GltfModelEntity. Each model has a 
tap animation associated with it.

## Scene Graph Manipulation

A simple hierarchy of multiple SceneCore entities is used to control the behaviors associated with 
each 3d object. Each object is parented to one entity that controls local idle rotation of the 
object. Another is manipulated by the user when they drag objects around the scene.

## Custom Object Movement

A simple acceleration based movement interaction handler is implemented to enable users to move 3d 
objects around the scene. Some custom logic is implemented to arbitrate between tapping on an 
object to start/stop playback and tap-dragging on an object to change its sound/location.

## Jetpack Compose for XR

Application creates subspace, within app where UI and 3D models placed. SpatialPanel used to create 
application menu, where user can select 3D shape to place it on scene or revert it. Selected 3D shape
placed in front of user FOV, based on user head position.

# 💻 Development Environment

**Sound Explorer** uses the Gradle build system and can be imported directly into Android Studio
(make sure you are using the latest stable version available
[here](https://developer.android.com/studio)).

# Additional Resources

- https://developer.android.com/xr
- https://developer.android.com/develop/xr
- https://developer.android.com/design/ui/xr
- https://developer.android.com/develop/xr#bootcamp

# License

**Sound Explorer** is distributed under the terms of the Apache License (Version 2.0). See the
[license](LICENSE) for more information.
