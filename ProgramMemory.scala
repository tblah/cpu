// program memory rom

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

// abstract representation of an instruction
class Instruction( opcodeSize: Int, argSize: Int ) extends Bundle {
    val opcode = UInt(width=opcodeSize)
    val arg1 = UInt(width=argSize)
    val arg2 = UInt(width=argSize)
}

object Instruction {
    def apply( opcodeSize: Int, argSize: Int, opcode: Int, arg1: Int, arg2: Int ) : Instruction = {
        val ret = new Instruction( opcodeSize, argSize )
        ret.opcode := UInt(opcode)
        ret.arg1 := UInt(arg1)
        ret.arg2 := UInt(arg2)
        ret // return ret
    }
}

// implementation
class ProgramMemory( opcodeSize: Int, gprAddrLength: Int, pcLength: Int ) extends Module {
    var io = new Bundle {
        val address = UInt(INPUT, width=pcLength)
        val out = new Instruction( opcodeSize, gprAddrLength )
    }

    val programText = Array( // imediate decrement example from README
        Instruction(opcodeSize, gprAddrLength, opcodes.ldi, 0, 6),
        Instruction(opcodeSize, gprAddrLength, opcodes.ld, 0, 0),
        Instruction(opcodeSize, gprAddrLength, opcodes.subi, 0, 1),
        Instruction(opcodeSize, gprAddrLength, opcodes.ld, 0, 0)
    )

    val rom = Vec( programText )

    io.out := rom( io.address )
}

// TODO tests
