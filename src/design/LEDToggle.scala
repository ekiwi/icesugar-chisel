// Copyright 2020 The Regents of the University of California
// released under BSD 3-Clause License
// author: Kevin Laeufer <laeufer@cs.berkeley.edu>

package design

import chisel3._
import chisel.lib.uart.LEDInput
import chisel3.experimental.Analog
import fpga.boards.icesugar._

class LEDToggle extends IceSugarTop {
  val toggle = Module(new LEDInput(frequency = clockFrequency.toInt, baudRate = 115200))

  val tx = IO(Output(UInt(1.W)))
  tx := toggle.io.txd
  val rx = IO(Input(UInt(1.W)))
  toggle.io.rxd := rx
  toggle.io.channel.ready := true.B

  val rgb = IO(Vec(3, Analog(1.W)))
  toggle.io.powerUp := true.B
  toggle.io.enabled := true.B
  rgb <> toggle.io.toLed
  val (blue, red, green) = (toggle.io.pwm(0), toggle.io.pwm(1), toggle.io.pwm(2))

  //TODO: Why does this not properly toggle the LED?
//  val REDToggle = RegInit(false.B)
//  when (toggle.io.channel.bits === 114.U) {
//    REDToggle := !REDToggle
//  }

  //TODO: For some reason, both 9 and r turns on LED, both s and n turn it off
  val REDToggle = RegInit(false.B)
  when (toggle.io.channel.bits === 114.U) {
    REDToggle := true.B
  }.elsewhen(toggle.io.channel.bits === 115.U) {
    REDToggle := false.B
  }

  red := REDToggle
  green := 0.U
  blue := 0.U
}

object LEDToggleGenerator extends App {
  val pcf =
    """set_io clock 35
      |set_io rx 4
      |set_io tx 6
      |set_io rgb_0 39
      |set_io rgb_1 40
      |set_io rgb_2 41
      |""".stripMargin
  val bin = IceSugar.makeBin(new LEDToggle, pcf)
  val iceLinkPath = "/run/media/brandon/iCELink/"
  IceSugar.program(bin, iceLinkPath)
}
