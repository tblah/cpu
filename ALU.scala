// ALU for the CPU

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
    along with Foobar.  If not, see http://www.gnu.org/licenses/.*/

package picomipscpu
import Chisel._

// possible values for control signals
object ALUops {
    val numOpts = 9
    val loadA :: loadB :: add :: sub :: and :: or :: xor :: notA :: notB :: Nil = Range(0, numOpts).toList
}

// ALU flags interface
class ALUflags extends Bundle {
    val zero = Bool( OUTPUT )
    // TODO: negative, carry
}

// ALU implementation
class ALU (wordSize: Int) extends Module {
    val io = new Bundle {
        val control = UInt( INPUT, log2Up(ALUops.numOpts) )
        val dataA = SInt( INPUT, wordSize )
        val dataB = SInt( INPUT, wordSize )
        val result = UInt( OUTPUT, wordSize )
        val flags = new ALUflags()
    }

    // default values
    io.result := UInt(0)

    // work out io.result
    switch (io.control) {
        is (UInt(ALUops.loadA)) {
            io.result := io.dataA
        } 
        is (UInt(ALUops.loadB)) {
            io.result := io.dataB
        } 
        is (UInt(ALUops.add)) {
            io.result := io.dataA + io.dataB
        } 
        is (UInt(ALUops.sub)) {
            io.result := io.dataA - io.dataB
        } 
        is (UInt(ALUops.and)) {
            io.result := io.dataA & io.dataB
        } 
        is (UInt(ALUops.or)) {
            io.result := io.dataA | io.dataB
        } 
        is (UInt(ALUops.xor)) {
            io.result := io.dataA ^ io.dataB
        } 
        is (UInt(ALUops.notA)) {
            io.result := ~io.dataA
        } 
        is (UInt(ALUops.notB)) {
            io.result := ~io.dataB
        } 
    }

    // work out io.flags.zero
    when (io.result === UInt(0)) {
        io.flags.zero := Bool(true)
    } .otherwise {
        io.flags.zero := Bool(false)
    }
}

// testbench
class ALUtests (dut: ALU) extends Tester(dut) {
    // loadA
    poke( dut.io.control, ALUops.loadA )
    poke( dut.io.dataA, 10 )
    step(1)
    expect( dut.io.result, 10 )
    expect( dut.io.flags.zero, 0 )

    // loadB
    poke( dut.io.control, ALUops.loadB )
    poke( dut.io.dataB, 0 )
    step(1)
    expect( dut.io.result, 0 )
    expect( dut.io.flags.zero, 1 )

    // add
    poke( dut.io.control, ALUops.add )
    poke( dut.io.dataA, 1 )
    poke( dut.io.dataB, 2 )
    step(1)
    expect( dut.io.result, 3 )
    expect( dut.io.flags.zero, 0 )

    // sub
    poke( dut.io.control, ALUops.sub )
    poke( dut.io.dataA, 12 )
    poke( dut.io.dataB, -3 )
    step(1)
    expect( dut.io.result, 15 )
    expect( dut.io.flags.zero, 0 )

    // and
    poke( dut.io.control, ALUops.and )
    poke( dut.io.dataA, 4 )
    poke( dut.io.dataB, 2 )
    step(1)
    expect( dut.io.result, 0 )
    expect( dut.io.flags.zero, 1 )

    // or
    poke( dut.io.control, ALUops.or )
    poke( dut.io.dataA, 2 )
    poke( dut.io.dataB, 4 )
    step(1)
    expect( dut.io.result, 6 )
    expect( dut.io.flags.zero, 0 )

    // xor
    poke( dut.io.control, ALUops.xor )
    poke( dut.io.dataA, 6 )
    poke( dut.io.dataB, 4 )
    step(1)
    expect( dut.io.result, 2 )
    expect( dut.io.flags.zero, 0 )

    // notA
    poke( dut.io.control, ALUops.notA )
    poke( dut.io.dataA, 100 )
    step(1)
    expect( dut.io.result, 0xff9b ) // this will break if you change the testWordSize!!
    expect( dut.io.flags.zero, 0 )

    // notB
    poke( dut.io.control, ALUops.notB )
    poke( dut.io.dataB, 100 )
    step(1)
    expect( dut.io.result, 0xff9b ) // this will break if you change the testWordSize!!
    expect( dut.io.flags.zero, 0 )
}

// boilerplate
object alu {
    val testWordSize = 16 // if you change this you will need ot change the tests for notA and notB
    def main(args: Array[String]): Unit = {
        chiselMainTest(Array[String]("--backend", "c", "--compile", "--test", "--genHarness"),
            () => Module(new ALU(testWordSize))){c => new ALUtests(c)}
  }
}
