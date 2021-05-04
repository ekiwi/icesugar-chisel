package design.fpga4fun

import chisel3._
import chisel3.experimental.Analog
import fpga.boards.icesugar._
import fpga.ip.ice40._

// inspired by https://www.fpga4fun.com/Opto2.html
class MakingAnLedHalfLit extends IceSugarTop {
  val rgb = IO(Vec(3, Analog(1.W)))

  val leds = Module(new RGBLedDriver(8, 8, 8))
  rgb <> leds.io.toLed
  leds.io.powerUp := true.B
  leds.io.enabled := true.B
  val (blue, red, green) = (leds.io.pwm(0), leds.io.pwm(1), leds.io.pwm(2))
  green := 0.U
  blue := 0.U

  // the actual LED toggle code
  val toggle = Reg(UInt(1.W))
  toggle := ~toggle
  red := toggle
}


object MakingAnLedHalfLitGenerator extends App {
  val pcf =
    """set_io clock 35
      |set_io rgb_0 39
      |set_io rgb_1 40
      |set_io rgb_2 41
      |""".stripMargin
  val bin = IceSugar.makeBin(new MakingAnLedHalfLit, pcf)
  val iceLinkPath = "/run/media/kevin/iCELink/"
  IceSugar.program(bin, iceLinkPath)
}
