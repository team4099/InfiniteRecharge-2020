package org.usfirst.frc.team4099.lib.util

import edu.wpi.first.wpilibj.PIDOutput

/**
 * Created by plato2000 on 2/14/17.
 */
class PIDOutputReceiver : PIDOutput {
    var output = 0.0
        private set

    override fun pidWrite(output: Double) {
        this.output = output
    }
}
