# Chisel Examples for the iCESugar FPGA Board

This repository collects building blocks and example designs written in the [Chisel](https://github.com/chipsalliance/chisel3)
hardware construction language and targeting the [iCESugar FPGA Board](https://github.com/wuxx/icesugar).

## Install Dependencies

To compile the Chisel designs you need an up to date JDK (OpenJDK 8 or newer) as well as a version of the
[Scala Build Tool (sbt)](https://www.scala-sbt.org/download.html).

To turn designs into FPGA configurations you need [yosys](https://github.com/YosysHQ/yosys) for synthesis,
[nextpnr-ice40](https://github.com/YosysHQ/nextpnr) and [icepack](https://github.com/YosysHQ/icestorm).
You can either build these tools from source or download [pre-built binaries](https://github.com/FPGAwars/apio).

## Building and Uploading the Blink Example

To build and upload the blink example, you need to edit the `iceLinkPath` in `src/design/Blink.scala`
to point to the directory that your iCELink is mounted to.

Now you can compile and program the blink example using `sbt`:
```shell
sbt "run designs.BlinkGenerator"
```

Alternatively, you can open the project in an IDE like
[IntelliJ Community Edition](https://www.jetbrains.com/idea/download/)
with the [Scala Plugin](https://plugins.jetbrains.com/plugin/1347-scala) by clicking on the `build.sbt` file.
Then you will be able to directly execute the `BlinkGenerator` from your IDE.
In IntelliJ, you will see a green arrow next to the `object BlinkGenerator extends App {` line.
