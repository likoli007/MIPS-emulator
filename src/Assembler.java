import java.io.*;
import java.util.*;


//TODO: pseudoinstructions，expression eval


class Label{
	public int address;
	public String name;
	
	Label(String n, int a){
		this.address = a;
		this.name = n;
	}
	
}

public class Assembler {
	ArrayList<String> InstructionBuffer;
	ArrayList<Integer> MachineCodeBuffer;
	ArrayList<Label> AddressBuffer;
	ArrayList<Byte> DataBuffer;
	
	static ExpEvaluator Evaluator = new ExpEvaluator();
	static String[] R_types = {
	         "ADD", "ADDU", "AND", "DIV", "DIVU", "JALR", "JR", "MFHI", "MFLO", "MTHI", "MTLO",
			 "MULT", "MULTU", "NOR", "OR", "SLL", "SLLV", "SLT", "SLTU", "SRA", "SRAV", "SRL",
			 "SRLV", "SUB", "SUBU", "SYSCALL", "XOR", "BREAK"};
	static String[] I_types = {
			"ADDI", "ADDIU", "ANDI", "BEQ", "BGEZ", "BGTZ", "BLEZ", "BLTZ", "BNE", "LB",
			"LBU" ,	"LH", "LHU", "LUI", "LW", "LWC1", "ORI", "SB", "SLTI", "SLTIU",
			"SH", "SW", "SWC1",	"XORI"};
	static String[] J_types = {
			"J", "JAL"};
	
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

	public Assembler(){
		InstructionBuffer = new ArrayList<String>();
		MachineCodeBuffer = new ArrayList<Integer>();
		AddressBuffer = new ArrayList<Label>();
		DataBuffer = new ArrayList<Byte>();
	}
	
	
	public boolean run(String input, boolean isASM) throws Exception {
		flushArrays();
		if (isASM) {
			createInstructionBuffer(input);
			try {
				assemble();
			} catch (Exception e) {
				flushArrays();
				throw e;
			}
		}
		else {
			createMachineCodeBuffer(input);
			deassemble();
		}
		
		return true;
	}
	

	private void createMachineCodeBuffer(String input) {
		int num;
		String[] lines = input.split("\n");
		
		for(int i = 0; i < lines.length; i++) {
			if(lines[i].isBlank() || lines[i].isEmpty()) {
				continue;
			}
			
			if(lines[i].contains("0x")) {
				lines[i] = lines[i].replace("0x", "");
			}
			num = Integer.parseUnsignedInt(lines[i], 16);
			MachineCodeBuffer.add(num);	
		}

	}

	private void createInstructionBuffer(String input) {
		String[] lines = input.split("\n");
		
		for(int i = 0; i < lines.length; i++) {
			if(lines[i].isBlank() || lines[i].isEmpty()) {
				continue;
			}
			
			if(lines[i].contains(";")) {
				lines[i] = lines[i].substring(0,lines[i].indexOf(';'));
			}
			if(!(lines[i].isBlank() || lines[i].isEmpty())) {
				InstructionBuffer.add(lines[i]);
			}
		}
		
	}
	
	private void deassemble() {
		String line;
		String reg;
		String[] lineList;
		String hex, eval;
		boolean labelError = false;
		
		int num;
		
		int rs, rt, rd, op, shamt, func;
		
		int result = 0; 
		int expResult;
		
		for(int i = 0; i < MachineCodeBuffer.size(); i++){
			num = MachineCodeBuffer.get(i);
			
			if ((num & 0xFC000000) == 0) {
				line = deassembleRType(num);
			}
			else if((num & 0x08000000) != 0) {
				line = deassembleJType(num);
			}
			else {
				line = deassembleIType(num);
			}
			
			InstructionBuffer.add(line);
		}	
		return;
	}
	
	public static String deassembleInstruction(int num){
		String line = "";
		
		if ((num & 0xFC000000) == 0) {
			line = deassembleRType(num);
		}
		else if((num & 0x08000000) != 0) {
			line = deassembleJType(num);
		}
		else {
			line = deassembleIType(num);
		}
		
		return line;
	}
	
	public ArrayList<Integer> getMachineCodeBufferArray(){
		return MachineCodeBuffer;
	}
	
	
	private void assemble() throws Exception {
		String line;
		String reg;
		String[] lineList;
		String hex, eval;
		boolean labelError = false;
		
		int result = 0; 
		int expResult;
		
		for(int i = 0; i < InstructionBuffer.size(); i++){
			line = InstructionBuffer.get(i);
			line = line.toUpperCase();
			line = line.replace("$", "");
			
			
			if(line.contains(".data")==true){
				int j;
				String varName, varType, content;
				
				for( j = i+1; j < InstructionBuffer.size(); j++) {
					line = InstructionBuffer.get(j);
					if(line.contains(".text") == true) {
						break;
					}
					lineList = line.split(" ");
					
					
					
				}
				
				i = j;
				continue;
			}
			
			
			lineList = line.split(" ");
			
			for(int j = 0; j < lineList.length; j++) {
				if (lineList[j].contains(":")){
					if (uniqueLabel(lineList[j])) {
						addLabel(lineList[j], i);
						lineList[j] = "";
					}
					else {
						labelError = true;
						break;
					}
					
				}
				if (isRegister(lineList[j])) {
					reg = getRegister(lineList[j]); 
					lineList[j] = lineList[j].replace(reg,String.valueOf(Registers.valueOf(reg).getVal()) );
				}
				if(containsExp(lineList[j])) {
					if(lineList[j].charAt(lineList[j].length()-1) == ')') {
						expResult = evalExp(lineList[j].substring(0, lineList[j].lastIndexOf('(')));
						lineList[j] = lineList[j].replace(lineList[j].substring(0, lineList[j].lastIndexOf('(')), String.valueOf(expResult));
					}
					else {
						expResult = evalExp(lineList[j]);
						lineList[j] = String.valueOf(expResult);
					}
					
				}
				if (isHex(lineList[j])) {
					if(!(lineList[j].contains("(")))
						lineList[j] = String.valueOf(Integer.parseInt(lineList[j].substring(2), 16));
					else {
						hex = lineList[j].substring(2, lineList[j].indexOf('('));
						lineList[j] = lineList[j].replace("0X"+hex, String.valueOf(Integer.parseInt(hex, 16)));		
					}
				}
				
				
			}
			
			if(labelError) {
				throw new Exception("标题错误，请查看");
			}
			
			line = String.join(" ",lineList).trim();
			
			if (line.isEmpty() || line.isBlank()){
				InstructionBuffer.remove(i);
				i--;
			}
			else {
				InstructionBuffer.set(i, line);
			}
			
			
		}
		
		for(int i = 0; i < InstructionBuffer.size(); i++) {
			line = InstructionBuffer.get(i);
			lineList = line.split(" ");
			
			if (Arrays.stream(R_types).anyMatch(lineList[0]::equals)) {
				result = assembleRType(lineList);
			}
			else if(Arrays.stream(I_types).anyMatch(lineList[0]::equals)) {
				result = assembleIType(lineList, i);
			}
			else if (Arrays.stream(J_types).anyMatch(lineList[0]::equals)) {
				result = assembleJType(lineList);
			}

			MachineCodeBuffer.add(result);
			
			
			
		}
		return;
	}
	
	public String output(boolean isASM) {
		if(isASM) {
			return getMachineCodeBufferString(); 
		}
		else {
			return getInstructionBufferString();
		}
		
		
	}

	private String getMachineCodeBufferString() {
		String result = new String();
		int num;
		
		for(int i = 0; i < MachineCodeBuffer.size(); i++) {
			num = MachineCodeBuffer.get(i);
			result = result + "0x" + (String.format("%08X", num) + "\n");
		}
		return result;
	}
	
	private String getInstructionBufferString() {
		String result = new String();
		
		for(int i = 0; i < InstructionBuffer.size(); i++) {
			result = result + InstructionBuffer.get(i) + "\n";
		}
		return result;
	}
	
	private void flushArrays() {
		MachineCodeBuffer.clear();
		InstructionBuffer.clear();
		AddressBuffer.clear();
		
	}

	
	private boolean isRegister(String text) {

	    for (Registers r : Registers.values()) {
	        if (r.name().equals(text)) {
	            return true;
	        }
	        else if(text.contains(r.name()) && text.contains("(")) {
	        	return true;
	        }
	    }

	    return false;
	}
	
	private static String getRegister(String text) {
	    for (Registers r : Registers.values()) {
	    	if (r.name().equals(text)) {
	            return r.name();
	        }
	        else if(text.contains(r.name())) {
	        	return r.name();
	        }
	    }

	    return "ERR";
	}
		

	private int evalExp(String string){
		double evalRes;
		
		evalRes = ExpEvaluator.analyse(string);
		
		return (int)evalRes;
	}

	private boolean containsExp(String string) {
		if(string.contains("+") || string.contains("-") ||
				string.contains("*") || string.contains("/")) {
			return true;
		}
		return false;
	}

	private boolean uniqueLabel(String string) {
		string = string.substring(0, string.length()-1);
		for (Label l : AddressBuffer) {
			if (l.name.equals(string)) {
				return false;
			}
		}
		return true;
	}
	
	private void addLabel(String string, int index) {
		AddressBuffer.add(new Label(string.substring(0,string.length()-1), index));
	}

	private boolean isHex(String string) {
		try {
			if(string.charAt(0) == '0' && string.charAt(1) == 'X')
				return true;
			else
				return false;
		}catch (Exception e) {
			return false;
		}
		
	}

	private int assembleJType(String[] instruction) {
		int result;
		int addr;
		int op;
		
		op = addr = 0;
		for(int i = 0; i < AddressBuffer.size(); i++) {
			if (AddressBuffer.get(i).name.equals(instruction[1])) {
				addr = AddressBuffer.get(i).address;
			}
		}
		
		switch(instruction[0]) {
		case "J":
			op = 2;
			break;
		case "JAL":
			op = 3;
			break;
		}
		
		if (addr < 0 || ((addr & 0xFF000000) != 0)){
			return -1;
		}
		
		result = (op << 26) + addr;
		return result;
	}



	private  int assembleIType(String[] instruction, int currentIndex) {
		int result;
		int op;
		int rs, rt;
		short imm;
		op = rs = rt = imm = 0;
		
		
		switch(instruction[0]) {
		case "ADDI":
			op = 8;
			rt = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2]);
			imm = Short.valueOf(instruction[3]);
			break;
		case "ADDIU":
			op = 9;
			rt = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2]);
			imm = Short.valueOf(instruction[3]);
			break;
		case "ANDI":
			op = 12;
			rt = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2]);
			imm = Short.valueOf(instruction[3]);
			break;
		case "BEQ":
			op = 4;
			rs = Integer.valueOf(instruction[1]);
			rt = Integer.valueOf(instruction[2]);
			if (isValidLabel(instruction[3]))
				imm = parseLabel(instruction[3], currentIndex);
			else
				return -1;
			break;
		case "BGEZ": 
			op = 1;
			rs = Integer.valueOf(instruction[1]);
			rt = 1;
			if (isValidLabel(instruction[2]))
				imm = parseLabel(instruction[2], currentIndex);
			else
				return -1;
			break;
		case "BGTZ":
			op = 7;
			rs = Integer.valueOf(instruction[1]);
			if (isValidLabel(instruction[2]))
				imm = parseLabel(instruction[2], currentIndex);
			else
				return -1;
			break;
		case "BLEZ":
			op = 6;
			rs = Integer.valueOf(instruction[1]);
			if (isValidLabel(instruction[2]))
				imm = parseLabel(instruction[2], currentIndex);
			else
				return -1;
			break;
		case "BLTZ":
			op = 1;
			rs = Integer.valueOf(instruction[1]);
			if (isValidLabel(instruction[2]))
				imm = parseLabel(instruction[2], currentIndex);
			else
				return -1;
			break;
		case "BNE":
			op = 5;
			rs = Integer.valueOf(instruction[1]);
			rt = Integer.valueOf(instruction[2]);
			if (isValidLabel(instruction[2]))
				imm = parseLabel(instruction[3], currentIndex);
			else
				return -1;
			break;
		case "LB":
			op = 32;
			rt = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2].substring(
					instruction[2].indexOf("(")+1,instruction[2].length()-1));
			imm = Short.valueOf(instruction[2].substring(0,
					instruction[2].indexOf("(")));
			break;
		case "LBU":
			op = 36;
			rt = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2].substring(
					instruction[2].indexOf("(")+1,instruction[2].length()-1));
			imm = Short.valueOf(instruction[2].substring(0,
					instruction[2].indexOf("(")));
			break;
		case "LH":
			op = 33;
			rt = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2].substring(
					instruction[2].indexOf("(")+1,instruction[2].length()-1));
			imm = Short.valueOf(instruction[2].substring(0,
					instruction[2].indexOf("(")));
			break;
		case "LHU":
			op = 37;
			rt = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2].substring(
					instruction[2].indexOf("(")+1,instruction[2].length()-1));
			imm = Short.valueOf(instruction[2].substring(0,
					instruction[2].indexOf("(")));
			break;
		case "LUI":
			op = 15;
			rt = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2].substring(
					instruction[2].indexOf("(")+1,instruction[2].length()-1));
			imm = Short.valueOf(instruction[2]);
		case "LW":
			op = 35;
			rt = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2].substring(
					instruction[2].indexOf("(")+1,instruction[2].length()-1));
			imm = Short.valueOf(instruction[2].substring(0,
					instruction[2].indexOf("(")));
			break;
		case "LWC1":
			op = 49;
			rt = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2].substring(
					instruction[2].indexOf("(")+1,instruction[2].length()-1));
			imm = Short.valueOf(instruction[2].substring(0,
					instruction[2].indexOf("(")));
			break;
		case "ORI":
			op = 13;
			rt = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2]);
			imm = Short.valueOf(instruction[3]);
			break;
		case "SB":
			op = 40;
			rt = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2].substring(
					instruction[2].indexOf("(")+1,instruction[2].length()-1));
			imm = Short.valueOf(instruction[2].substring(0,
					instruction[2].indexOf("(")));
			break;
		case "SLTI":
			op = 10;
			rt = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2]);
			imm = Short.valueOf(instruction[3]);
			break;
		case "SLTIU":
			op = 11;
			rt = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2]);
			imm = Short.valueOf(instruction[3]);
			break;
		case "SH":
			op = 41;
			rt = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2].substring(
					instruction[2].indexOf("(")+1,instruction[2].length()-1));
			imm = Short.valueOf(instruction[2].substring(0,
					instruction[2].indexOf("(")));
			break;
		case "SW":
			op = 43;
			rt = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2].substring(
					instruction[2].indexOf("("),instruction[2].length()-1));
			imm = Short.valueOf(instruction[2].substring(0,
					instruction[2].indexOf("(")));
			break;
		case "SWC1":
			op = 59;
			rt = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2].substring(
					instruction[2].indexOf("(")+1,instruction[2].length()-1));
			imm = Short.valueOf(instruction[2].substring(0,
					instruction[2].indexOf("(")));
			break;
		case "XORI":
			op = 14;
			rt = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2]);
			imm = Short.valueOf(instruction[3]);
			break;
		}
		
		
		result = ((op << 26) + (rs << 21) + (rt << 16)) + (int)(imm & 0xFFFF);
		
		return result;
	}

	private boolean isValidLabel(String string) {
		for (Label l : AddressBuffer) {
			if (l.name.equals(string)) {
				return true;
			}
		}
		return false;
	}

	private short parseLabel(String string, int currentIndex) {
		for (Label l : AddressBuffer) {
			if (l.name.equals(string)) {
				return (short) (l.address  - (currentIndex + 1));
			}
		}
		return 0;
	}


	private int assembleRType(String[] instruction) {
		int result;
		int op;
		int rs, rt, rd, sa, func;
		op = rs = rt = rd = sa = func = 0;
		
		
		switch(instruction[0]) {
		case "ADD":
			func = 32;
			rd = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2]);
			rt = Integer.valueOf(instruction[3]);
			break;
		case "ADDU":
			func = 33;
			rd = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2]);
			rt = Integer.valueOf(instruction[3]);
			break;
		case "AND":
			func = 36;
			rd = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2]);
			rt = Integer.valueOf(instruction[3]);
			break;
		case "DIV":
			func = 26;
			rs = Integer.valueOf(instruction[1]);
			rt = Integer.valueOf(instruction[2]);
			break;
		case "DIVU":
			func = 27;
			rs = Integer.valueOf(instruction[1]);
			rt = Integer.valueOf(instruction[2]);
			break;
		case "JALR":
			func = 9;
			rd = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2]);
			break;
		case "JR":
			func = 8;
			rs = Integer.valueOf(instruction[1]);
			break;
		case "MFHI":
			func = 16;
			rd = Integer.valueOf(instruction[1]);
			break;
		case "MFLO":
			func = 18;
			rd = Integer.valueOf(instruction[1]);
			break;
		case "MTHI":
			func = 17;
			rs = Integer.valueOf(instruction[1]);
			break;
		case "MTLO":
			func = 19;
			rs = Integer.valueOf(instruction[1]);
			break;
		case "MULT":
			func = 24;
			rs = Integer.valueOf(instruction[1]);
			rt = Integer.valueOf(instruction[2]);
			break;
		case "MULTU":
			func = 25;
			rs = Integer.valueOf(instruction[1]);
			rt = Integer.valueOf(instruction[2]);
			break;
		case "NOR":
			func = 39;
			rd = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2]);
			rt = Integer.valueOf(instruction[3]);
			break;
		case "OR":
			func = 37;
			rd = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2]);
			rt = Integer.valueOf(instruction[3]);
			break;
		case "SLL":
			func = 0;
			rd = Integer.valueOf(instruction[1]);
			rt = Integer.valueOf(instruction[2]);
			sa = Integer.valueOf(instruction[3]);
			break;
		case "SLLV":
			func = 4;
			rd = Integer.valueOf(instruction[1]);
			rt = Integer.valueOf(instruction[2]);
			rs = Integer.valueOf(instruction[3]);
			break;
		case "SLT":
			func = 42;
			rd = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2]);
			rt = Integer.valueOf(instruction[3]);
			break;
		case "SLTU":
			func = 43;
			rd = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2]);
			rt = Integer.valueOf(instruction[3]);
			break;
		case "SRA":
			func = 3;
			rd = Integer.valueOf(instruction[1]);
			rt = Integer.valueOf(instruction[2]);
			sa = Integer.valueOf(instruction[3]);
			break;
		case "SRAV":
			func = 7;
			rd = Integer.valueOf(instruction[1]);
			rt = Integer.valueOf(instruction[2]);
			rs = Integer.valueOf(instruction[3]);
			break;
		case "SRL":
			func = 2;
			rd = Integer.valueOf(instruction[1]);
			rt = Integer.valueOf(instruction[2]);
			sa = Integer.valueOf(instruction[3]);
		case "SRLV":
			func = 6;
			rd = Integer.valueOf(instruction[1]);
			rt = Integer.valueOf(instruction[2]);
			rs = Integer.valueOf(instruction[3]);
			break;
		case "SUB":
			func = 34;
			rd = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2]);
			rt = Integer.valueOf(instruction[3]);
			break;
		case "SUBU":
			func = 35;
			rd = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2]);
			rt = Integer.valueOf(instruction[3]);
			break;
		case "SYSCALL":
			func = 12;
			break;
		case "XOR":
			func = 38;
			rd = Integer.valueOf(instruction[1]);
			rs = Integer.valueOf(instruction[2]);
			rt = Integer.valueOf(instruction[3]);
			break;
		case "BREAK":
			func = 13;
			break;
		}
		result = (op << 26) + (rs << 21) + (rt << 16) + (rd << 11) + (sa << 6) + func;
		return result;
	}
	
	private static String getRegister(int reg){
        for(Registers r : Registers.values()){
            if(r.value == reg) return "$" + r.name();
        }
        return "ERRORREGISTER";
    }
	
	
	private static String deassembleRType(int num) {	
		String rs = getRegister((num >> 21) & 0x1f);
		String rt = getRegister((num >> 16) & 0x1f);
		String rd = getRegister((num >> 11) & 0x1f);
		String shamt = Integer.toString((num >> 6) & 0x1f);
		int func = (num & 0x3F);
		
		switch(func) {
		case 32:
			return "ADD " + rd + " " + rs + " " + rt;
		case 33:
			return "ADDU " + rd + " " + rs + " " + rt;
		case 36:
			return "AND " + rd + " " + rs + " " + rt;
		case 26:
			return "DIV " + rs + " " + rt;
		case 27:
			return "DIVU " + rs + " " + rt;
		case 9:
			return "JALR " + rd + " " + rs;
		case 8:
			return "JR " + rs;
		case 16:
			return "MFHI " + rd;
		case 18:
			return "MFLO " + rd;
		case 17:
			return "MTHI " + rs;
		case 19:
			return "MTLO " + rs;
		case 24:
			return "MULT " + rs + " " + rt;
		case 25:
			return "MULTU " + rs + " " + rt;
		case 39:
			return "NOR " + rd + " " + rs + " " + rt;
		case 37:
			return "OR " + rd + " " + rs + " " + rt;
		case 0:
			return "SLL " + rd + " " + rt + " " + shamt;
		case 4:
			return "SLLV " + rd + " " + rt + " " + rs;
		case 42:
			return "SLT " + rd + " " + rs + " " + rt;
		case 43:
			return "SLTU " + rd + " " + rs + " " + rt;
		case 3:
			return "SRA " + rd + " " + rt + " " + shamt;
		case 7:
			return "SRAV " + rd + " " + rt + " " + rs;
		case 2:
			return "SRL " + rd + " " + rt + " " + shamt;
		case 6:
			return "SRLV " + rd + " " + rt + " " + rs;
		case 34:
			return "SUB " + rd + " " + rs + " " + rt;
		case 35:
			return "SUBU " + rd + " " + rs + " " + rt;
		case 12:
			return "SYSCALL";
		case 38:
			return "XOR " + rd + " " + rs + " " + rt;
		case 13:
			return "BREAK";
		}
		return "ERROR DURING TRANSLATING R TYPE";
	}
	
	private static String deassembleJType(int num) {
		int imm = num & 0x3FFFFFF;
		
		switch((num & 0x0C000000) >> 26) {
		case 2:
			return "J " + Integer.valueOf(imm);
		case 3:
			return "JAL" + Integer.valueOf(imm);
		}
		
		return "ERROR TRANSLATING J TYPE";
	}
	private static String deassembleIType(int num) {
		String rs = getRegister((num >> 21) & 0x1f);
		String rt = getRegister((num >> 16) & 0x1f);
		String imm = String.valueOf(num & 0xFFFF);
		

		
		switch(num >>> 26) {
		case 8:
			return "ADDI " + rt + " " + rs + " " + imm; 
		case 9:
			return "ADDIU " + rt + " " + rs + " " + Integer.toUnsignedString(Integer.valueOf(imm)); 
		case 12:
			return "ANDI" + rt + " " + rs + " " + imm; 
		case 4:
			return "BEQ " + rs + " " + rt + " " + imm; 
		case 1: 
			if(((num >> 16) & 0x1f) == 1)
				return "BGEZ " + rs + " " + imm; 
			return "BLTZ " + rs + " " + imm; 
		case 7:
			return "BGTZ " + rs + " " + imm; 
		case 6:
			return "BLEZ " + rs + " " + imm; 
		case 5:
			return "BNE " + rs + " " + rt + " " + imm; 
		case 32:
			return "LB " + rt + " " + imm + "(" + rs + ")"; 
		case 36:
			return "LBU " + rt + " " + imm + "(" + rs + ")"; 
		case 33:
			return "LH " + rt + " " + imm + "(" + rs + ")"; 
		case 37:
			return "LHU " + rt + " " + imm + "(" + rs + ")"; 
		case 15:
			return "LUI " + rt + " " + imm; 
		case 35:
			return "LW " + rt + " " + imm + "(" + rs + ")"; 
		case 49:
			return "LWC1 " + rt + " " + imm + "(" + rs + ")"; 
		case 13:
			return "ORI " + rt + " " + rs + " " + imm; 
		case 40:
			return "SB " + rt + " " + imm + "(" + rs + ")"; 
		case 10:
			return "SLTI " + rt + " " + rs + " " + imm; 
		case 11:
			return "SLTIU " + rt + " " + rs + " " + Integer.toUnsignedString(Integer.valueOf(imm)); 
		case 41:
			return "LH " + rt + " " + imm + "(" + rs + ")"; 
		case 43:
			return "SW " + rt + " " + imm + "(" + rs + ")"; 
		case 59:
			return "SWC1 " + rt + " " + imm + "(" + rs + ")"; 
		case 14:
			return "XORI " + rt + " " + rs + " " + imm; 
		}
		
		return "ERROR TRANSLATING I TYPE";
	}
}
