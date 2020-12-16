// Copyright 2020 The Regents of the University of California
// released under BSD 3-Clause License
// author: Kevin Laeufer <laeufer@cs.berkeley.edu>

package fpga.tools

import chisel3.RawModule
import chisel3.stage._
import chisel3.stage.phases.Convert
import firrtl.annotations.Annotation
import firrtl.stage.FirrtlCircuitAnnotation

/** Allows programmatic access to the Builder elaboration and the Converter to Firrtl */
object ChiselCompiler {
  private val converter = new Convert
  def elaborate[M <: RawModule](gen: () => M): (firrtl.CircuitState, M) = {
    // run Builder.build(Module(gen()))
    val genAnno = ChiselGeneratorAnnotation(gen)
    val elaborationAnnos = genAnno.elaborate

    // extract elaborated module
    val dut = elaborationAnnos.collectFirst{ case DesignAnnotation(d) => d}.get

    // run Converter.convert(a.circuit) and toFirrtl on all annotations
    val converterAnnos = converter.transform(elaborationAnnos)
    val chirrtl = converterAnnos.collectFirst { case FirrtlCircuitAnnotation(c) => c }.get
    val annos = converterAnnos.filterNot(isInternalAnno)
    val state = firrtl.CircuitState(chirrtl, annos)

    (state, dut.asInstanceOf[M])
  }
  private def isInternalAnno(a: Annotation): Boolean = a match {
    case _: FirrtlCircuitAnnotation | _: DesignAnnotation[_] | _:ChiselCircuitAnnotation => true
    case _=> false
  }
}