// encapsulating module for the cpu

/*  This file is part of picomips-cpu.

    picomips-cpu is a free hardware design: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    picmips-cpu is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with picomips-cpu.  If not, see http://www.gnu.org/licenses/.*/

package picomipscpu
import Chisel._

// TODO: automatic pcSize
class CPU (pcSize: Int, gprAddrSize: Int) {
    val wordSize = 2*gprAddrSize // see instruction documentation in README for ldi and add  
    val io = new Bundle {
        val out = UInt(OUTPUT, width=wordSize) // dummy output connected to the ALU's output so that we have something to test
    }

    // instances of sub-modules
    val alu = new ALU( wordSize ) 
    val gpr = new Registers( wordSize, gprAddrSize ) 
    val pc = new ProgramCounter( pcSize ) 
    val decoder = new Decoder( pcSize ) 
    val programROM = new ProgramMemory( gprAddrSize, pcSize ) 

    // multiplexer controlling if we are looking at an immediate value or a value from a register
    var imediateMux = Mux( decoder.io.immediate, gpr.io.read2.data, programROM.io.out.arg2 )

    // wiring
    alu.io.control <> decoder.io.aluControl
    alu.io.dataA <> gpr.io.read1.data
    alu.io.dataB := imediateMux
    alu.io.result <> gpr.io.write.data
    alu.io.flags <> decoder.io.aluFlags

    programROM.io.address := pc.io.instruct.pcOut

    pc.io.instruct.branchAddr := programROM.io.out.arg2 ## programROM.io.out.arg1 //concatination
    pc.io.mode := decoder.io.pcControl

    decoder.io.opcode := programROM.io.out.opcode

    gpr.io.read1.address := programROM.io.out.arg1
    gpr.io.read2.address := programROM.io.out.arg2
    gpr.io.write.address := programROM.io.out.arg1
    gpr.io.write.data := alu.io.result
}
