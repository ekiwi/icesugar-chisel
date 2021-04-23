// Copyright 2020 The Regents of the University of California
// released under BSD 3-Clause License
// author: Kevin Laeufer <laeufer@cs.berkeley.edu>

package design

import chisel3._
import chisel.lib.uart.UartMain
import fpga.boards.icesugar._

class Echo extends IceSugarTop {
  val echo = Module(new UartMain(frequency = clockFrequency.toInt, baudRate = 115200, false))

  val tx = IO(Output(UInt(1.W)))
  tx := echo.io.txd
  val rx = IO(Input(UInt(1.W)))
  echo.io.rxd := rx
}

object EchoGenerator extends App {
  val pcf =
    """set_io clock 35
      |set_io tx 6
      |set_io rx 4
      |""".stripMargin
  val bin = IceSugar.makeBin(new Echo, pcf)
  val iceLinkPath = "/run/media/brandon/iCELink/"
  IceSugar.program(bin, iceLinkPath)
}
