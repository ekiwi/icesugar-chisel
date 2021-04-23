// Copyright 2020 The Regents of the University of California
// released under BSD 3-Clause License
// author: Kevin Laeufer <laeufer@cs.berkeley.edu>

package design

import chisel3._
import chisel3.experimental.Analog
import fpga.boards.icesugar._
import fpga.ip.ice40._

class Blink extends IceSugarTop {
  val leds = Module(new RGBLedDriver(8, 8, 8))
  
  val rgb = IO(Vec(3, Analog(1.W)))
  rgb <> leds.io.toLed
  leds.io.powerUp := true.B
  leds.io.enabled := true.B
  val (blue, red, green) = (leds.io.pwm(0), leds.io.pwm(1), leds.io.pwm(2))

  val counter = RegInit(0.U(26.W))
  counter := counter + 1.U
  red := counter(23)
  green := counter(24)
  blue := counter(25)
}

object BlinkGenerator extends App {
  val pcf =
    """set_io clock 35
      |set_io rgb_0 39
      |set_io rgb_1 40
      |set_io rgb_2 41
      |""".stripMargin
  val bin = IceSugar.makeBin(new Blink, pcf)
  val iceLinkPath = "/run/media/brandon/iCELink/"
  IceSugar.program(bin, iceLinkPath)
}
