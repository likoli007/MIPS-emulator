import java.awt.*;
import java.awt.event.*;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.DefaultCaret;





public class Application extends JFrame implements ActionListener{
	static final String assemblyRules = 
					  "           _           \r\n"
					+ "          (_)          \r\n"
					+ " _ __ ___  _ _ __  ___ \r\n"
					+ "| '_ ` _ \\| | '_ \\/ __|\r\n"
					+ "| | | | | | | |_) \\__ \\\r\n"
					+ "|_| |_| |_|_| .__/|___/\r\n"
					+ "            | |        \r\n"
					+ "            |_|      \r\n "
					+"PLEASE WRITE EACH INSTRUCTION ON A NEW LINE\n"
					+"THIS ASSEMBLER SUPPORTS BOTH UPPER AND LOWER CASE\n"
					+"THIS ASSEMBLER DOES NOT SUPPORT COMMAS (,) PLEASE USE SPACES\n"
					+"ALSO DO NOT USE UNNECESSARY SPACES OR THE ASSEMBLER MAY BRAKE\n"
					+"    lb $s0 123 ($t1) WILL LEAD TO A CRASH!!!\n"
					+"EXAMPLE OF ALLOWED REGISTER WRITING FORMS:\n"
					+"    s0, S0, $s0, $S0, $16, 16\n"
					+"THIS ASSEMBLER SUPPORTS COMMENTS\n"
					+"THIS ASSEMBLER SUPPORTS HEX, BUT EACH HEX NUMBER NEEDS TO BE PRECEDED BY '0x'\n"
					+"THIS ASSEMBLER SUPPORTS ONLINE EXPRESSION EVALUATION, BUT ONLY FOR DECIMALS!\n"
					+"    lb s0 12*4(T0) IS OK!\n"
					+"    lb s0 0x12*4(T0) IS NOT OK!\n"
					+"LABELS CAN BE CHAINED BUT NEED SPACES BETWEEN, EXAMPLE:\n"
					+"	 LABEL1: label2:\n"
					;
			
	static final String deassemblyRules = 
			  "           _           \r\n"
			+ "          (_)          \r\n"
			+ " _ __ ___  _ _ __  ___ \r\n"
			+ "| '_ ` _ \\| | '_ \\/ __|\r\n"
			+ "| | | | | | | |_) \\__ \\\r\n"
			+ "|_| |_| |_|_| .__/|___/\r\n"
			+ "            | |        \r\n"
			+ "            |_|       \r\n"
			+"---------------------MANUAL INPUT MENU---------------------\n"
			+"PLEASE INPUT THE NUMBER IN HEXADECIMAL(NEEDS TO HAVE 0x) OR DECIMAL\n"
			+"EXAMPLE:\n"
			+"    0x014B4820\n"
			+"    12345678\n"	
			+"----------------------FILE INPUT MENU----------------------"
			+"FILE SHOULD BE A BYTE FILE, THIS PROGRAM WILL READ 4 BYTES!"
			+"PLEASE MAKE CHANGES TO YOUR FILE ACCORDING TO THESE RULES!";
	
	
	JPanel menuPanel, optionPanel, bigMenuPanel, bigIOPanel;
	JTextArea inputArea, outputArea;
	
	JRadioButton assemblyButton, deassemblyButton, emulatorButton;
	ButtonGroup modeButtonGroup;
	JScrollPane scrollPane1, scrollPane2;
	JButton saveInputButton, saveOutputButton,loadButton, runButton;
	JButton registerButton, dataModeButton, commandModeButton, stepButton;
	JButton ruleButton, asmButton;
	
	JLabel topLabel;
	
	Assembler assembler;
	Emulator emulator;

	static final int MEMSIZE = 8000;
	
	boolean isReg = true;
	
	Application(){
		assembler = new Assembler();
		emulator = new Emulator(MEMSIZE);
		
		topLabel = new JLabel("欢迎使用MIPS模拟器");
		
		inputArea = new JTextArea(10, 50);
		scrollPane1 = new JScrollPane(inputArea); 
		inputArea.setEditable(true);
		//scrollPane1.add(inputArea);
		DefaultCaret caret = (DefaultCaret)inputArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		
		outputArea = new JTextArea(10, 50);
		scrollPane2 = new JScrollPane(outputArea); 
		outputArea.setEditable(false);
		//scrollPane2.add(outputArea);
		
		
		
	
		assemblyButton = new JRadioButton("汇编模模式");
		assemblyButton.setActionCommand("ASM");
		deassemblyButton = new JRadioButton("反汇编模式");
		deassemblyButton.setActionCommand("DSM");
		//emulatorButton = new JRadioButton("模拟器模�?");
		//emulatorButton.setActionCommand("EMU");
		
		assemblyButton.addActionListener(this);
		deassemblyButton.addActionListener(this);
		//emulatorButton.addActionListener(this);
		
		
		ruleButton = new JButton();
		ruleButton.setPreferredSize(new Dimension(100, 25));
		ruleButton.setText("显示规则");
		ruleButton.setVisible(true);
		
		asmButton = new JButton();
		asmButton.setPreferredSize(new Dimension(100, 25));
		asmButton.setText("纯汇编/反汇编");
		asmButton.setVisible(true);
		
		saveInputButton = new JButton();
		saveInputButton.setPreferredSize(new Dimension(100,25));
		saveInputButton.setText("保存输入区");
		saveInputButton.setVisible(false);
		
		saveOutputButton = new JButton();
		saveOutputButton.setPreferredSize(new Dimension(100,25));
		saveOutputButton.setText("保存输出结果");
		saveOutputButton.setVisible(false);
		
		loadButton = new JButton();
		loadButton.setPreferredSize(new Dimension(100,25));
		loadButton.setText("加载");
		loadButton.setVisible(false);
		
		runButton = new JButton();
		runButton.setPreferredSize(new Dimension(100,25));
		runButton.setText("编译");
		runButton.setVisible(false);
		
		stepButton = new JButton();
		stepButton.setPreferredSize(new Dimension(100,25));
		stepButton.setText("下步");
		stepButton.setVisible(false);
		
		registerButton = new JButton();
		registerButton.setPreferredSize(new Dimension(100,25));
		registerButton.setText("查看寄存器");
		registerButton.setVisible(false);
		
		dataModeButton = new JButton();
		dataModeButton.setPreferredSize(new Dimension(100,25));
		dataModeButton.setText("查看数据");
		dataModeButton.setVisible(false);
		
		optionPanel = new JPanel();
		optionPanel.setLayout(new BoxLayout(optionPanel, BoxLayout.PAGE_AXIS));
		optionPanel.add(ruleButton);
		optionPanel.add(loadButton);
		optionPanel.add(saveInputButton);
		optionPanel.add(saveOutputButton);
		optionPanel.add(asmButton);
		optionPanel.add(runButton);
		optionPanel.add(stepButton);
		optionPanel.add(registerButton);
		optionPanel.add(dataModeButton);
		startAssembly();
		
		asmButton.addActionListener(this);
		loadButton.addActionListener(this);
		ruleButton.addActionListener(this);
		saveInputButton.addActionListener(this);
		saveOutputButton.addActionListener(this);
		runButton.addActionListener(this);
		stepButton.addActionListener(this);
		registerButton.addActionListener(this);
		dataModeButton.addActionListener(this);
		
		
		
		modeButtonGroup = new ButtonGroup();
		
		modeButtonGroup.add(assemblyButton);
		modeButtonGroup.add(deassemblyButton);
		//modeButtonGroup.add(emulatorButton);
		assemblyButton.setSelected(true);
		
		menuPanel = new JPanel();
		
		//menuPanel.setLayout(new BoxLayout(menuPanel, ));
		menuPanel.add(assemblyButton);
		menuPanel.add(deassemblyButton);
		//menuPanel.add(emulatorButton);
		
		
		bigMenuPanel = new JPanel();
		bigMenuPanel.setLayout(new BoxLayout(bigMenuPanel, BoxLayout.PAGE_AXIS));
		bigMenuPanel.add(optionPanel);
		bigMenuPanel.add(menuPanel);
		
		bigIOPanel = new JPanel();
		bigIOPanel.setLayout(new BoxLayout(bigIOPanel, BoxLayout.PAGE_AXIS));
		bigIOPanel.setMaximumSize(new Dimension(5, 20));
		bigIOPanel.add(scrollPane1);
		bigIOPanel.add(scrollPane2);
		
		//this.add(inputArea, BorderLayout.WEST);
		//this.add(outputArea, BorderLayout.SOUTH);
		this.add(topLabel, BorderLayout.NORTH);
		this.add(bigIOPanel, BorderLayout.WEST);
		this.add(bigMenuPanel, BorderLayout.EAST);
		//this.add(optionPanel, BorderLayout.EAST);
		//this.add(menuPanel, BorderLayout.EAST);
		this.pack();
		this.setVisible(true);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == assemblyButton) {
			clearOptions();
			startAssembly();
		}
		if(e.getSource() == deassemblyButton) {
			clearOptions();
			startDeassembly();
		}
		
		if(e.getSource() == emulatorButton) {
			clearOptions();
			startEmulation();
		}
		if(e.getSource() == loadButton) {
			JFileChooser j = new JFileChooser();
            int r = j.showOpenDialog(null);
            
            if (r == JFileChooser.APPROVE_OPTION){
                openFile(j.getSelectedFile().getAbsolutePath());
            }
		}
		if(e.getSource() == saveOutputButton) {
			JFileChooser j = new JFileChooser();
			int r = j.showSaveDialog(j);
			
			if(r == JFileChooser.APPROVE_OPTION) {
				saveOutputFile(j.getSelectedFile().getAbsolutePath());
			}
			
		}
		
		if(e.getSource() == saveInputButton) {
			JFileChooser j = new JFileChooser();
			int r = j.showSaveDialog(j);
			
			if(r == JFileChooser.APPROVE_OPTION) {
				saveInputFile(j.getSelectedFile().getAbsolutePath());
			}
		}
		
		if(e.getSource() == runButton) {
			try {
				assembler.run(inputArea.getText(), modeButtonGroup.getSelection().getActionCommand().equals("ASM") ? true: false);
				showResults();
				emulator = new Emulator(MEMSIZE);
				topLabel.setText((modeButtonGroup.getSelection().getActionCommand().equals("ASM") ? "汇编" :"反汇编") + "成功！");
				emulator.loadProgram(assembler.getMachineCodeBufferArray());
				inputArea.setText(emulator.printState());
				
						
				displayRegisters();
				startEmulation();
			} catch (Exception err) {
				topLabel.setText(err.getMessage());
			}
		}
		if(e.getSource() == asmButton) {
			try {
				assembler.run(inputArea.getText(), modeButtonGroup.getSelection().getActionCommand().equals("ASM") ? true: false);
				showResults();
			} catch (Exception err) {
				// TODO Auto-generated catch block
				topLabel.setText(err.getMessage());
			}
			
		}
		
		if(e.getSource() == ruleButton) {
			displayRules();
		}
		
		if (e.getSource() == registerButton) {
			displayRegisters();
		}
		if (e.getSource() == dataModeButton) {
			displayData();
		}
		if(e.getSource() == stepButton) {
			emulateStep();
		}
	}


	private void emulateStep() {
		emulator.step();
		inputArea.setText(emulator.printState());
	
		outputArea.setText(isReg == true ? emulator.outputRegisters() : emulator.outputData());
	}


	private void displayData() {
		String result = emulator.outputData();
		
		outputArea.setText(result);
	}


	private void displayRegisters() {
		String result = emulator.outputRegisters();
		
		outputArea.setText(result);
		
	}


	private void displayRules() {
		String content = "ERROR DURING CONTENT SHOW";
		
		if(modeButtonGroup.getSelection().getActionCommand().equals("ASM")) {
			content = assemblyRules;
		}
		else if(modeButtonGroup.getSelection().getActionCommand().equals("DSM")) {
			content = deassemblyRules;
		}
		
		
		JOptionPane.showMessageDialog(this, content,
                "规则", JOptionPane.INFORMATION_MESSAGE);
		
	}


	private void saveInputFile(String absolutePath) {
		boolean saved = modeButtonGroup.getSelection().getActionCommand().equals("ASM") ? saveTextFile(absolutePath, inputArea) : saveBinaryFile(absolutePath, inputArea);
		
		if(saved) {
			topLabel.setText("保存成功！");
		}
		else {
			topLabel.setText("保存失败！");
		}
	}


	private void showResults() {
		String result = assembler.output(modeButtonGroup.getSelection().getActionCommand().equals("ASM") ? true : false); 
		
		outputArea.setText(result);
	}


	private void saveOutputFile(String absolutePath) {
		
		//can be expanded to include txt, but not in app for now
		boolean saved = modeButtonGroup.getSelection().getActionCommand().equals("ASM") ? saveBinaryFile(absolutePath, outputArea) : saveTextFile(absolutePath, outputArea);
		
		if(saved) {
			topLabel.setText("保存成功！");
		}
		else {
			topLabel.setText("保存失败！");
		}
		
		
	}

	
	

	private boolean saveBinaryFile(String absolutePath, JTextArea contentHolder) {
		String[] lines = contentHolder.getText().split("\n");
		ByteArrayOutputStream bout = new ByteArrayOutputStream(lines.length * 4);
		DataOutputStream dout = new DataOutputStream(bout);
		FileOutputStream fout;
		
		int num;
		try {
			fout = new FileOutputStream(absolutePath);
			
			for(int i = 0; i < lines.length; i++) {
				if(lines[i].isBlank() || lines[i].isEmpty()) {
					continue;
				}
				
				if(lines[i].contains("0x")) {
					lines[i] = lines[i].replace("0x", "");
					if(lines[i].isBlank() || lines[i].isEmpty()) {
						continue;
					}
					num = Integer.parseUnsignedInt(lines[i], 16);
				}
				
				else {
					num = Integer.parseUnsignedInt(lines[i]);
				}
				
				dout.writeInt(num);
				
			}
			bout.writeTo(fout);
			fout.flush();
			fout.close();  
		} catch (Exception e) {
			return false;
		}
		return true;
	}


	private boolean saveTextFile(String absolutePath, JTextArea contentHolder) {
		String content = contentHolder.getText();
		try {
			FileWriter writerObj = new FileWriter(absolutePath, false);
	        writerObj.write(content);
	        writerObj.close();
	        } catch (IOException e) {
	            return false;
	        }
		return true;
	}


	private void openFile(String absolutePath) {
		String input = modeButtonGroup.getSelection().getActionCommand().equals("ASM")  ? openTextFile(absolutePath) : openBinaryFile(absolutePath);
		//boolean result = assembler.openFile(absolutePath, modeButtonGroup.getSelection().getActionCommand().equals("ASM") ? true: false );
	
		
		inputArea.setText(input);
		topLabel.setText("读入文件成功！点击“执行“按钮执行" + (modeButtonGroup.getSelection().getActionCommand().equals("ASM") ? "汇编": "反汇编"));

	}


	private String openBinaryFile(String fileName) {
		File f;
		int num;
		f = new File(fileName);
		String result = "";
	    
	    try {
	    	InputStream inputStream = new FileInputStream(fileName);
	        long fileSize = new File(fileName).length();
	        byte[] allBytes = new byte[(int) fileSize];
	        int bytesRead = inputStream.read(allBytes);
	
	        for(int i = 0; i <= allBytes.length - 4; i = i+4) {
	        	num = ((allBytes[i] & 0xFF) << 24) | ((allBytes[i+1] & 0xFF) <<16)| ((allBytes[i+2] & 0xFF) << 8) | (allBytes[i+3]  & 0xFF);
	            result = result + "0x" + (String.format("%08X", num) + "\n");
	        }

	    } catch (IOException e) {
	        topLabel.setText("读入文件错误�?（确定用了�?�适文件格�?�?�？）");
	        return "";
	    }
	    return result;
	}

	private String openTextFile(String fileName) {
		File f;
		String data;
		String result = "";
		f = new File(fileName);
	    Scanner myReader;
		try {
			myReader = new Scanner(f);
			while (myReader.hasNextLine()) {
			    data = myReader.nextLine();
			    if(!(data.isBlank() || data.isEmpty()))
			    	result = result + data + "\n";
			}
			myReader.close();
		} catch (FileNotFoundException e) {
			topLabel.setText("读入文件错误！（确定用了合适文件格式吗？）");
			return "";
		}
		return result;
	}


	private void startEmulation() {
		topLabel.setText("MIPS模拟器");
		
		loadButton.setVisible(true);
		stepButton.setVisible(true);
		registerButton.setVisible(true);
		dataModeButton.setVisible(true);
		asmButton.setVisible(false);
		
		
		optionPanel.validate();
        optionPanel.repaint();
	}


	private void startDeassembly() {
		topLabel.setText("MIPS反汇编");
		loadButton.setVisible(true);
		saveInputButton.setVisible(true);
		saveOutputButton.setVisible(true);
		runButton.setVisible(true);
		
		optionPanel.validate();
        optionPanel.repaint();
	}


	private void startAssembly() {
		topLabel.setText("MIPS汇编");
		loadButton.setVisible(true);
		saveInputButton.setVisible(true);
		saveOutputButton.setVisible(true);
		runButton.setVisible(true);
		
		optionPanel.validate();
        optionPanel.repaint();
	}


	private void clearOptions() {
		loadButton.setVisible(false);
		saveInputButton.setVisible(false);
		saveOutputButton.setVisible(false);
		//runButton.setVisible(false);
		//stepButton.setVisible(false);
		//registerButton.setVisible(false);
		//dataModeButton.setVisible(false);
		inputArea.setText("");
		outputArea.setText("");
	}
}
