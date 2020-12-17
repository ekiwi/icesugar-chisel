// Copyright 2020 The Regents of the University of California
// released under BSD 3-Clause License
// author: Kevin Laeufer <laeufer@cs.berkeley.edu>

package design

import chisel.lib.uart.Sender
import chisel3._
import fpga.boards.icesugar._

/** Uses a UART transmitter from the ip-contributions library to send "Hello World!" on startup. */
class HelloWorld extends IceSugarTop {
  val tx = IO(Output(UInt(1.W)))
  val sender = Module(new Sender(frequency = clockFrequency.toInt, baudRate = 115200))
  tx := sender.io.txd
}

object HelloWorldGenerator extends App {
  val pcf =
    """set_io clock 35
      |set_io tx 6
      |""".stripMargin
  val bin = IceSugar.makeBin(new HelloWorld, pcf)
  val iceLinkPath = "/run/media/kevin/iCELink/"
  IceSugar.program(bin, iceLinkPath)
}
