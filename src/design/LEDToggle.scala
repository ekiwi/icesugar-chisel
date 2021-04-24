// Copyright 2020 The Regents of the University of California
// released under BSD 3-Clause License
// author: Kevin Laeufer <laeufer@cs.berkeley.edu>

package design

import chisel3._
import chisel.lib.uart.{BufferedTx, Rx, UartIO}
import chisel3.experimental.Analog
import chisel3.util._
import fpga.boards.icesugar._
import fpga.ip.ice40.RGBLedDriver

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

  //Begin: Toggle LEDS logic
  val REDtoggle = RegInit(false.B)
  val GREENtoggle = RegInit(false.B)
  val BLUEtoggle = RegInit(false.B)

  when (toggle.io.channel.fire()) {
    switch (toggle.io.channel.bits) {
      is (114.U) {REDtoggle := !REDtoggle}  //Toggle red on (lowercase) r press
      is (103.U) {GREENtoggle := !GREENtoggle}  //Toggle green on (lowercase) g press
      is (98.U)  {BLUEtoggle := !BLUEtoggle}  //Toggle blue on (lowercase) b press
    }
  }

  red := REDtoggle
  green := GREENtoggle
  blue := BLUEtoggle
}

/** Combine RGB and Input/Output. */
class LEDInput(frequency: Int, baudRate: Int) extends Module {
  val io = IO(new Bundle {
    val txd = Output(UInt(1.W))
    val rxd = Input(UInt(1.W))
    val channel = new UartIO()
    val powerUp = Input(Bool())
    val enabled = Input(Bool())
    val pwm = Input(Vec(3, Bool()))
    val toLed = Vec(3, Analog(1.W))
  })
  val tx = Module(new BufferedTx(frequency, baudRate))
  val rx = Module(new Rx(frequency, baudRate))
  io.txd := tx.io.txd
  rx.io.rxd := io.rxd
  tx.io.channel <> rx.io.channel
  io.channel <> rx.io.channel

  val leds = Module(new RGBLedDriver(8, 8, 8))
  leds.io.powerUp := io.powerUp
  leds.io.enabled := io.enabled
  leds.io.pwm := io.pwm
  leds.io.toLed <> io.toLed
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
