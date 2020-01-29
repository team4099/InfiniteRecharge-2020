package com.team4099.robot2020.statemachines

object ShootingStatemachine {
    enum class ShootState {
        OFF,
        SPIN_UP_FLYWHEEL,
        SHOOT,
        UNJAM,
    }

    var wantedShootState: ShootState = ShootState.OFF
        set (value) {
            when (value) {
                ShootState.OFF -> {
                    Shooter.shooterState = Shooter.State.IDLE
                }
                ShootState.SPIN_UP_FLYWHEEL -> {
                    // not implemented yet
                }
                ShootState.SHOOT -> {
                    Shooter.shooterState = Shooter.State.SHOOTING
                }
                ShootState.UNJAM -> {
                    Shooter.setOpenLoop(-0.75)
                }
            }
            field = value
        }
}