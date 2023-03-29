import java.util.ArrayList;


/*
 作业：模拟机
以程�?模拟MIPS/RISC-V�?行，功能包括：
汇编器：将汇编程�?转�?��?机器�?。能有较�?�活的格�?，�?�以处�?�格�?指令�?表达�?�?有出错信�?�。
汇编�??汇编：汇编指令与机器�?的相互转�?�。
模拟器：根�?�机器�?模拟执行�?�以�?行简�?�汇编程�?。
--------
1�?模拟器�?行界�?�设计：�?�以命令行或窗�?�界�?�。�?�以执行指令的汇编�?�??汇编，�?�以�?�步执行指令观察寄存器�?内存的�?�化。（命令行版�?��?�考DEBUG）
2�?指令伪指令的汇编�??汇编：将汇编指令转�?��?二进制机器�?，能够处�?�标�?��?�?��?。
3�?MMU存储器管�?��?�元：存储器存�?�模拟。大头�?头，对�?�?对�?，Cache，虚拟存储。
4�?格�?指令表达�?处�?�：对于汇编程�?中的格�?指令�?表达�?的处�?�。�?�考网页格�?指令。
个人版模拟器应实现：
>指令：常用5~10�?�；
>命令：
-->R-看寄存器， 				- DONE
-->D-数�?�方�?看内存，	    - DONE
-->U-指令方�?看内存，        - DONE
-->A-写汇编指令到内存，      - NOT YET
-->T-�?�步执行内存中的指令    - DONE
also some others

need to also be able to process variables, not yet done
	idea: add a varBuffer in assembler
	

will need an array for memory, array for storing register values
and an int store for pc, ir

void exec(){IR = memory[pc] pc+=4}
	then according to stuff that happens in IR, get the opcodes etc. etc.

 * */

public class Emulator {
	byte[] memory;
	int[] REG = new int[32];
	int PC;
	int IR;
	
	int hi, lo;
	
	String input;
	
	static enum Registers  {
		ZERO(0),AT(1),V0(2),V1(3),A0(4),A1(5),A2(6),A3(7),
		T0(8),T1(9),T2(10),T3(11),T4(12),T5(13),T6(14),T7(15),
		S0(16),S1(17),S2(18),S3(19),S4(20),S5(21),S6(22),S7(23),
		T8(24),T9(25),K0(26),K1(27),GP(28),SP(29),FP(30),RA(31);
		
		private int value;
	
		Registers(int val){
			this.value = val;
		}
		
		public String getName() {
			return "$" + this.name();
		}
		
		public int getVal() {
			return this.value;
		}
		
	};
	
	
	public Emulator(int memsize) {
		memory = new byte[memsize];
		
		PC = 0;
	}
	
	private String getRegister(int reg){
        for(Registers r : Registers.values()){
            if(r.value == reg) return "$" + r.name();
        }
        return "ERRORREGISTER";
    }
	
	public String outputData() {
		String result = "";
		
		for (int i = 0; i < memory.length; i++) {
			result = result + String.format("%02X", memory[i]) + " ";
			if ((i+1) % 16 == 0 && i != 0) {
				result += "\n";
			}
		}
		
		return result;
	}
	
	public String outputRegisters() {
		String result = "";
		
		for(int i = 0; i < 32; i++) {
			result = result +  getRegister(i) + ":" + String.format("%08X", REG[i])+"    ";
			
			
			if ((i+1) % 4 == 0 && i != 0) {
				result = result + "\n";
			}
			
		}
		result += "$HI:" + String.format("%08X", hi) + "    " + "$LO:" + String.format("%08X", lo);
		
		return result;
	}
	
	private int convertBytesToInt(int addr) {
        return ((memory[addr] & 0xFF) << 24) |
                ((memory[addr+1] & 0xFF) << 16) |
                ((memory[addr+2] & 0xFF) << 8) |
                ((memory[addr+3] & 0xFF));
    }
	
	public void step() {
		IR = convertBytesToInt(PC); 
		PC += 4;
		
		execute();
		
	}

	private void execute() {
		if ((IR & 0xFC000000) == 0) {
			executeRType();
		}
		else if((IR & 0x08000000) != 0) {
			executeJType();
		}
		else {
			executeIType();
		}
	}

	private void executeIType() {
		int rs = (IR >> 21) & 0x1f;
		int rt = (IR >> 16) & 0x1f;
		int imm = (IR & 0xFFFF);
		

		
		switch(IR >>> 26) {
		case 8:
			//addi
			//rt = rs + imm
			REG[rt] = REG[rs] + imm; 
			break;
		case 9:
			//addiu
			//rt = rs + imm (unsigned)
			REG[rt] = Integer.valueOf(Integer.toUnsignedString(REG[rs] + imm));
			break;
		case 12:
			//andi
			//rt = rs & imm
			REG[rt] = REG[rs] & imm;
			break;
		case 4:
			//beq
			//if rs == rt, goto pc + imm
			if(REG[rs] == REG[rt]) {
				PC += imm * 4;
			} 
			break;
		case 1: 
			//bgez or bltz
			//if rs bgez or bltz 0, goto pc + imm
			if(((IR >> 16) & 0x1f) == 1) {
				if(REG[rs] >= 0) {
					PC += imm * 4;
				}
			}
			else {
				if(REG[rs] < 0) {
					PC += imm * 4;
				}
			}
			break;
		case 7:
			//bgtz
			//if rs > 0, goto pc + imm
			if(REG[rs] > 0) {
				PC += imm * 4;
			}
			break;
		case 6:
			//blez
			//if rs <= 0 goto pc +imm
			if(REG[rs] <= 0) {
				PC += imm * 4;
			}
			break;
		case 5:
			//bne
			//if rs != rt goto pc + imm
			if(REG[rs] != REG[rt]) {
				PC += imm * 4;
			}
			break;
		case 32:
			//lb
			//rt = Memory[rs + imm]
			REG[rt] = memory[REG[rs]+imm];
			break;
		case 36:
			//lbu
			//rt = memory[rs + imm]
			REG[rt] = memory[REG[rs]+imm] & 0xff;
			break;
		case 33:
			//lh
			//return "LH " + rt + " " + imm + "(" + rs + ")"; 
			break;
		case 37:
			//lhu
			//return "LHU " + rt + " " + imm + "(" + rs + ")"; 
			break;
		case 15:
			//lui
			//rt's top 16 bits = imm
			REG[rt] = REG[rt] | (imm << 16);
			break;
		case 35:
			//lw
			//rt = Memory[rs + imm] 
			REG[rt] = convertBytesToInt(REG[rs]+imm);
			break;
		case 49:
			//lwc1
			//return "LWC1 " + rt + " " + imm + "(" + rs + ")"; 
			break;
		case 13:
			//ori
			//rt = rs | imm
			REG[rt] = REG[rs] | imm; 
			break;
		case 40:
			//sb
			//memory[imm+rs] = rt
			memory[imm + REG[rs]] = (byte)(REG[rt] & 0xff);
			break;
		case 10:
			//slti
			//if rs < imm rt == 1 otherwise 0
			if(REG[rs] < (short)imm) {
				REG[rt] = 1;
			}
			else {
				REG[rt] = 0;
			} 
			break;
		case 11:
			//sltiu
			//if rs < imm rt == 1 otherwise 0 unsigned
			if (Integer.valueOf(Integer.toUnsignedString(Integer.valueOf(REG[rs]))) < (imm & 0xffff)) {
				
			}
			break;
		case 41:
			//lh
			//return "LH " + rt + " " + imm + "(" + rs + ")"; 
			break;
		case 43:
			//sw
			//memory[rs+imm] = rt
			memory[REG[rs]+imm] = (byte)((REG[rt] & 0xFF000000) >>> 24);
			memory[REG[rs]+imm+1] = (byte)((REG[rt] & 0x00FF0000) >>> 16);
			memory[REG[rs]+imm+2] = (byte)((REG[rt] & 0x0000FF00) >>> 8);
			memory[REG[rs]+imm+3] = (byte)((REG[rt] & 0x000000FF));
			break;
		case 59:
			//swc1
			//return "SWC1 " + rt + " " + imm + "(" + rs + ")"; 
			break;
		case 14:
			//xori
			//rt = rs xor imm
			REG[rt] = REG[rs] ^ imm; 
			break;
		}

		if(REG[0] != 0) {
			REG[0] = 0;
		}
	}

	private void executeJType() {
		int imm = IR & 0x3FFFFFF;
		
		switch((IR & 0x0C000000) >> 26) {
		case 2:
			//j
			//new address = high 4 of pc, imm, two 0 bits
			PC = (PC & 0xF0000000) | (imm << 2);
		case 3:
			//jal
			REG[31] = PC;
			PC = (PC & 0xF0000000) | (imm << 2);
		}
		
	}

	private void executeRType() {
		int rs = (IR >> 21) & 0x1f;
		int rt = (IR >> 16) & 0x1f;
		int rd = (IR >> 11) & 0x1f;
		int shamt = (IR >> 6) & 0x1f;
		int func = (IR & 0x3F);
		
		switch(func) {
		case 32:
			//ADD
			//rd = rs + rt
			REG[rd] = REG[rs] + REG[rt]; 
			break;
		case 33:
			//ADDU
			//rd = rs + rt (unsigned)
			REG[rd] = Integer.valueOf(Integer.toUnsignedString((REG[rs] + REG[rt]))); 
			break;
		case 36:
			//AND
			//rd = rs & rt
			REG[rd] = REG[rs] & REG[rt]; 
			break;
		case 26:
			//DIV
			//hi, lo = rs / rt, rs % rt
			hi = (int)Math.floor(REG[rs] / REG[rt]);
			lo = REG[rs] % REG[rt];
			break;
		case 27:
			//DIVU
			//hi, lo = rs / rt, rs % rt (unsigned)
			hi = Integer.valueOf(Integer.toUnsignedString((int)Math.floor(REG[rs] / REG[rt])));
			lo = Integer.valueOf(Integer.toUnsignedString(REG[rs] % REG[rt]));
			break;
		case 9:
			//JALR
			//rd = pc + 4  (i add to pc in step already)
			//pc = rs
			REG[rd] = PC;
			PC = REG[rs];
			break;
		case 8:
			//JR
			// pc = rs
			PC = REG[rs];
			break;
		case 16:
			//mfhi
			//rd = hi
			REG[rd] = hi;
			break;
		case 18:
			//mflo
			//rd = lo
			REG[rd] = lo;
			break;
		case 17:
			//mthi
			//hi = rs
			hi = REG[rs];
			break;
		case 19:
			//mtlo
			//lo = rs
			lo = REG[rs];
			break;
		case 24:
			//mult
			//hi = top 32 of rs*rt
			//lo = bot 32 of rs*rt
			hi = (int)(((long)REG[rs] * (long)REG[rt]) >> 32);
			lo = (int)(((long)REG[rs] * (long)REG[rt]) & 0xFFFFFFFF);
			break;
		case 25:
			//multu
			//hi = top 32 of rs*rt unsigned
			//lo = bot 32 of rs*rt unsigned
			hi = Integer.valueOf(Integer.toUnsignedString((int)(((long)REG[rs] * (long)REG[rt]) >> 32)));
			lo = Integer.valueOf(Integer.toUnsignedString((int)(((long)REG[rs] * (long)REG[rt]) & 0xFFFFFFFF)));
			break;
		case 39:
			//nor
			//rd = !(rs || rt)
			REG[rd] = ~(REG[rs] | REG[rt]);
			break;
		case 37:
			//or
			//rd = rs | rt
			REG[rd] = REG[rs] | REG[rt];
			break;
		case 0:
			//sll
			//rd = rt << shamt
			REG[rd] = REG[rt] << shamt;
			break;
		case 4:
			//sllv
			//rd = rt << rs
			REG[rd] = REG[rt] << REG[rs];
			break;
		case 42:
			//slt
			//If $s is less than $t, $d is set to one. It gets zero otherwise.
			REG[rd] = REG[rs] < REG[rt] ? 1 : 0;
			break;
		case 43:
			//sltu
			//If $s is less than $t, $d is set to one. It gets zero otherwise.unsigned
			REG[rd] = Integer.compareUnsigned(REG[rs], REG[rt]) < 0 ? 1 : 0;
			break;
		case 3:
			//sra
			//rd = rt >> shamt
			REG[rd] = rt >> shamt;
			break;
		case 7:
			//srav
			//rd = rt >> rs
			REG[rd] = REG[rt] >> REG[rs];
			break;
		case 2:
			//srl
			//rd = rt >> shamt logical
			REG[rd] = rt >>> shamt;
			break;
		case 6:
			//srlv
			//rd = rt >> rs logical
			REG[rd] = REG[rt] >>> REG[rs];
			break;
		case 34:
			//sub
			//rd = rs - rt
			REG[rd] = REG[rs] - REG[rt];
			break;
		case 35:
			REG[rd] = Integer.valueOf(Integer.toUnsignedString(REG[rs] - REG[rt]));
			break;
		case 12:
			//SYSCALL
			//todo, SYSCALLS
			break;
		case 38:
			//xor
			//rd = rs xor rt
			REG[rd] = REG[rs] ^ REG[rt];
			break;
		case 13:
			//break
			//unsure what to do with this instruction
			break;
		
			
		
		}
		if(REG[0] != 0) {
			REG[0] = 0;
		}
	}

	public void loadProgram(ArrayList<Integer> machineCodeBufferArray) {
		int instruction;
		String translation;
		input = "";
		
		for(int i = 0; i < machineCodeBufferArray.size(); i++) {
			instruction = machineCodeBufferArray.get(i);
			writeToMemory(instruction);
		}	
		PC = 0;
	}

	private void writeToMemory(int instruction) {
		memory[PC] = (byte)((instruction & 0xFF000000) >>> 24);
		memory[PC+1] = (byte)((instruction & 0x00FF0000) >>> 16);
		memory[PC+2] = (byte)((instruction & 0x0000FF00) >>> 8);
		memory[PC+3] = (byte)((instruction & 0x000000FF));
		PC += 4;
	}

	public String printState() {
		String result = "";
		int instruction;
		String translation;
		
		for(int i = 0; i < memory.length; i+=4) {
			instruction = convertBytesToInt(i);
			translation = Assembler.deassembleInstruction(instruction);
			
			result = result + String.format("%08X", instruction) + " | " + translation + (i == PC ? "    <---\n" : "\n");
 		}
		return result;
	}
}
