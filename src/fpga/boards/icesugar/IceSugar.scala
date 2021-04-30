// Copyright 2020 The Regents of the University of California
// released under BSD 3-Clause License
// author: Kevin Laeufer <laeufer@cs.berkeley.edu>

package fpga.boards.icesugar

import chisel3._
import chisel3.experimental.{ChiselAnnotation, annotate}
import firrtl.annotations.PresetAnnotation
import firrtl.options.TargetDirAnnotation
import firrtl.stage.{FirrtlCircuitAnnotation, FirrtlStage, OutputFileAnnotation}
import fpga.tools.ChiselCompiler

import java.io.{File, PrintWriter}
import java.nio.file.Files
import scala.sys.process._

/** iCESugar FPGA Board Support for Chisel: https://github.com/wuxx/icesugar */
object IceSugar {
  /** generates Verilog, then runs yosys and nextpnr */
  def makeBin(makeTop: => IceSugarTop, pcf: String, name: String = "", buildDir: String = "build"): BinFile = {
    // elaborate circuit first in order to get the name of the toplevel module
    val (state, top) = ChiselCompiler.elaborate(() => makeTop)

    // create a build directory
    val topName = if(name.isEmpty) top.name else name
    val dir = new File(buildDir, topName)
    dir.mkdirs()

    // run firrtl compiler
    val annos = state.annotations ++ Seq(
      FirrtlCircuitAnnotation(state.circuit),
      TargetDirAnnotation(dir.getAbsolutePath),
    )
    val res = (new FirrtlStage).execute(Array("-E", "sverilog"), annos)
    val verilogFile = res.collectFirst { case OutputFileAnnotation(file) => file + ".sv" }.get

    // generate pcf file
    val pcfFile = topName + ".pcf"
    // TODO: generate automatically
    val pcfWriter = new PrintWriter(dir.getAbsolutePath + "/" + pcfFile)
    pcfWriter.write(pcf)
    pcfWriter.close()

    // run yosys
    val jsonFile = topName + ".json"
    val yosysCmd = List("yosys", "-l", "yosys.log", "-p", s"synth_ice40 -json $jsonFile", verilogFile)
    val yosysRet = Process(yosysCmd, dir) ! ProcessLogger(_ => ())
    if(yosysRet != 0) {
      throw new RuntimeException(s"Failed to run yosys! Please consult: ${dir.getAbsolutePath}/yosys.log for details.")
    }

    // run nextpnr
    val ascFile = topName + ".asc"
    val freq = top.clockFrequency / 1000 / 1000
    require(freq * 1000 * 1000 == top.clockFrequency)
    val nextpnrCmd = List("nextpnr-ice40",
      "-l", "next.log",
      "--up5k",
      "--package", "sg48",
      "--freq", freq.toString,
      "--pcf", pcfFile,
      "--json", jsonFile,
      "--asc", ascFile
    )
    val nextRet = Process(nextpnrCmd, dir) ! ProcessLogger(_ => ())
    if(nextRet != 0) {
      throw new RuntimeException(s"Failed to run nextpnr! Please consult: ${dir.getAbsolutePath}/next.log for details.")
    }

    // run icepack
    val binFile = topName + ".bin"
    val icepackCmd = List("icepack", ascFile, binFile)
    val icepackRet = (Process(icepackCmd, dir) #> new File(dir.getAbsolutePath + "/ice.log")).!
    if(icepackRet != 0) {
      throw new RuntimeException(s"Failed to run icepack! Please consult: ${dir.getAbsolutePath}/ice.log for details.")
    }

    BinFile(dir.getPath + "/" + binFile)
  }

  /** program IceSugar board */
  def program(bin: BinFile, iceLinkPath: String): Unit = {
    val binFile = new File(bin.path)
    if(!binFile.isFile) {
      throw new RuntimeException(s"Failed to find binary file at ${binFile.getAbsoluteFile}")
    }

    val linkDir = new File(iceLinkPath)
    if(!linkDir.isDirectory) {
      throw new RuntimeException(s"Failed to find iCELink at ${linkDir.getAbsolutePath}")
    }

    val dst = new File(linkDir, "image.bin")
    Files.copy(binFile.toPath, dst.toPath)
  }
}

/** icepack output */
case class BinFile(path: String)


trait IceSugarTop extends Module with RequireAsyncReset {
  // 12 MHz Clock
  def clockFrequency: Long = 12000000

  // FPGA Bitstream Reset
  // The IceSugar board has a push button connected to CRESET_B which will
  // initiate a configuration download.
  // See section "4.6. External Reset" in the "iCE40 UltraPlus(TM) Family" data sheet.
  annotate(new ChiselAnnotation {
    override def toFirrtl = PresetAnnotation(reset.toTarget)
  })
}
