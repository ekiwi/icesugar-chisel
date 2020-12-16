// Copyright 2020 The Regents of the University of California
// released under BSD 3-Clause License
// author: Kevin Laeufer <laeufer@cs.berkeley.edu>

package fpga.ip.ice40

import chisel3._
import chisel3.experimental.{Analog, ExtModule}

/** Chisel wrapper around the built-in RGB Driver IP
 *  Please note that the `toLed` wires are hard wired: https://github.com/YosysHQ/nextpnr/issues/254
 * */
class RGBLedDriver(current0: Int, current1: Int, current2: Int, halfCurrentMode: Boolean = false) extends Module {
  val io = IO(new RGBLedDriverIO)

  // determine possible current steps
  val currentStepSize = if(halfCurrentMode) 2 else 4
  val minCurrent = 0
  val maxCurrent = currentStepSize * 6

  private def convertCurrent(current: Int): String = {
    require(current >= 0, "Current may not be negative!")
    require(current <= maxCurrent, s"Current of $current mA exceeds maximum ($maxCurrent mA)!")
    require(current % currentStepSize == 0, s"Current of $current mA is not a multiple of $currentStepSize mA!")
    val ones = current / currentStepSize
    (Seq("0b") ++ Seq.fill(6 - ones)("0") ++ Seq.fill(ones)("1")).mkString("")
  }

  val ip = Module(new SB_RGBA_DRV(
    mode = if(halfCurrentMode) "0b1" else "0b0",
    rgb0 = convertCurrent(current0),
    rgb1 = convertCurrent(current1),
    rgb2 = convertCurrent(current2),
  ))

  ip.CURREN := io.powerUp
  ip.RGBLEDEN := io.enabled
  ip.RGB0PWM := io.pwm(0)
  ip.RGB1PWM := io.pwm(1)
  ip.RGB2PWM := io.pwm(2)
  io.toLed(0) <> ip.RGB0
  io.toLed(1) <> ip.RGB1
  io.toLed(2) <> ip.RGB2
}

class RGBLedDriverIO extends Bundle {
  val powerUp = Input(Bool())
  val enabled = Input(Bool())
  val pwm = Input(Vec(3, Bool()))
  val toLed = Vec(3, Analog(1.W))
}

/** Chisel declaration of the built-in RGB Led Driver IP
 *  See "iCE40 LED Driver Usage Guide" for more information.
 * */
class SB_RGBA_DRV(mode: String = "0b0", rgb0: String = "0b000011", rgb1: String = "0b000011", rgb2: String = "0b000011")
  extends ExtModule(Map("CURRENT_MODE" -> mode, "RGB0_CURRENT" -> rgb0, "RGB1_CURRENT" -> rgb1, "RGB2_CURRENT" -> rgb2)) {
  val CURREN = IO(Input(Bool()))
  val RGBLEDEN = IO(Input(Bool()))
  val RGB0PWM = IO(Input(Bool()))
  val RGB1PWM = IO(Input(Bool()))
  val RGB2PWM = IO(Input(Bool()))
  val RGB0 = IO(Analog(1.W))
  val RGB1 = IO(Analog(1.W))
  val RGB2 = IO(Analog(1.W))
}