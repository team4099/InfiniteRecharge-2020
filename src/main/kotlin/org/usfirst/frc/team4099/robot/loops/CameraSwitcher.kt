package org.usfirst.frc.team4099.robot.loops

import edu.wpi.first.wpilibj.CameraServer

/** Manages the shutting off of components and subsystems when at risk of brownout.
 * It does this through a multitude of steps:
 * 1. Constantly monitor the Battery voltage
 * 2.
 * 3.
 */
class CameraSwitcher private constructor() : Loop {
    enum class Camera {

    }



    override fun onStart() {

    }

    override fun onLoop() {

    }

    override fun onStop() {

    }

    fun switchCamera(camera: Camera) {

    }

    companion object {
        val instance = CameraSwitcher()
    }
}
