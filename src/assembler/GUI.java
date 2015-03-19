package assembler;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class GUI extends JFrame implements ActionListener{
	
	private JMenuBar menuBar;
	private JMenu fileMenu, help;
	private JMenuItem open, exit, about;
	private JTextArea assemblyCode, binaryCode;
	private JButton translate;
	private JFileChooser fileChooser;
	private File input, output;
	
	public GUI(){
		
		

		Container contentPane = getContentPane();
	    contentPane.setLayout(new FlowLayout());
		
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		
		open = new JMenuItem("Open");
		open.addActionListener(this);
		fileMenu.add(open);
		
		fileMenu.addSeparator();
		
		exit = new JMenuItem("Exit");
		exit.addActionListener(this);
		fileMenu.add(exit);	
		
		help = new JMenu("Help");
		menuBar.add(help);
		
		about = new JMenuItem("About");
		about.addActionListener(this);
		help.add(about);
		
		assemblyCode = new JTextArea(30,38);
		binaryCode = new JTextArea(30,38);
		assemblyCode.setEditable(false);
		binaryCode.setEditable(false);
		
		translate = new JButton("Translate");
		translate.addActionListener(this);
		
		add(new JScrollPane(assemblyCode));
		add(translate);
		add(new JScrollPane(binaryCode));
		
		fileChooser = new JFileChooser();
		
		setTitle("Assembler");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(1000,550);
		setResizable(false);
		setVisible(true);
		
	}

	@Override
	public void actionPerformed(ActionEvent evt) {
		//handles events for the menu items
				String source = evt.getActionCommand();
				
				if(source.equals("Open")) open();	
				if(source.equals("Exit")) System.exit(0);
				if(source.equals("About")) about();
				
			//handles events for the buttons in the toolbar
				if(evt.getSource() instanceof JButton) {		
				JButton clicked = (JButton)evt.getSource();
				
				if(clicked == translate) translate();
				
				}
				
		
	}

	private void about() {
		JOptionPane.showMessageDialog(this, "Group members: "
				+ "\nChris Tulsi  14/0719/1627"
				+ "\nSomebody"
				+ "\nSomebody"
				+ "\nSomebody"
				+ "\nSomebody");
		
	}

	private void open() {
		
		int ret = fileChooser.showOpenDialog(null);
		
		if (ret == JFileChooser.APPROVE_OPTION) {
			
			input = fileChooser.getSelectedFile();
            //This is where a real application would open the file.
			int extension = input.getName().indexOf('.');
			String fileName = input.getName().substring(0, extension);
		    output = new File(input.getParent(), fileName + ".hack");
            
		    try {
				FileReader reader = new FileReader(input.getAbsolutePath());
				BufferedReader br = new BufferedReader(reader);
				assemblyCode.read(br, null);
				br.close();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		    
        } else {
           System.out.println("rtyu");
        }
		
	}
	
	private void translate() {
		
			try {
																				
				PrintWriter writeOut = new PrintWriter(output);

				Parser p = new Parser(input); // create Parser

				SymbolTable st = new SymbolTable(p); // create SymbolTable

				int currentInstruction = 0; // set current line number to 0

				// while(true) loop includes last instruction, unlike
				// while(p.hasMoreCommands())
				while (true) {
					if (p.commandType().equals("L_COMMAND")) { // ignore L_COMMANDs
															
						if (p.hasMoreCommands()) {
							p.advance();
							continue;
						} else {
							break;
						}
					} else if (p.commandType().equals("A_COMMAND")) { // handle A_COMMANDs
																		
						String machine = "0";
						try {
							int loc = Integer.parseInt(p.symbol()); // get integer
																	
							String binary = Integer.toBinaryString(loc); // convert to binary String
																			
							if (binary.length() < 15) { // add leading zeros if necessary
														 
								for (int i = 0; i < 15 - binary.length(); i++) {
									machine += "0";
								}
							}
							machine += binary;
						} catch (NumberFormatException nfe) { // if symbol is not an integer, check symbol table
															
							if (st.contains(p.symbol())) {
								String binary = Integer.toBinaryString(st
										.getAddress(p.symbol())); // if in symbol table, retrieve
																
								if (binary.length() < 15) { // value and convert to binary string
															 
									for (int i = 0; i < 15 - binary.length(); i++) {
										machine += "0";
									}
								}
								machine += binary;
							} else { // if not in symbol table
								st.addEntry(p.symbol()); // add current symbol (next available memory
														
								String binary = Integer.toBinaryString(st
										.getAddress(p.symbol())); // handled by Symbol Table Module)
																
								if (binary.length() < 15) { // convert value to binary String
									
									for (int i = 0; i < 15 - binary.length(); i++) {
										machine += "0";
									}
								}
								machine += binary;
							}
						}
						writeOut.println(machine); //write one instruction to
						currentInstruction++;
					} else if (p.commandType().equals("C_COMMAND")) { // handle C_COMMANDs
																		
						String machine = "111"; // start string with "111"
						String comp = Code.comp(p.comp()); // get codes from Code module
						String dest = Code.dest(p.dest());
						String jump = Code.jump(p.jump());
						
						if (!(dest.equals("NG") || comp.equals("NG") || jump
								.equals("NG"))) { // if no invalid codes
							machine += comp + dest + jump; // add all codes to final string
							
							writeOut.println(machine);
							currentInstruction++;
						
						} else { // handles invalid codes
							System.out.println("Error at instruction "
									+ currentInstruction + " of .asm file.");
							System.out
									.println("Resulting .hack file is incomplete.");
							writeOut.close();
							return;
						}
					} else { // handles invalid instructions
						System.out.println("Error at instruction "
								+ currentInstruction + " of .asm file.");
						System.out
								.println("Resulting .hack file is incomplete.");
						writeOut.close();
						return;
					}

					if (p.hasMoreCommands()) { // advance if more commands
						p.advance();
					} else { // break if last command
						break;
					}
				}

				writeOut.close();
				
				try {
					FileReader reader = new FileReader(output.getAbsolutePath());
					BufferedReader br2 = new BufferedReader(reader);
					binaryCode.read(br2, null);
					br2.close();
					
				} catch (Exception e) {
					e.printStackTrace();
				}

			} catch (Exception e) {
				System.out.println("Error: " + e.getMessage());
			}

	}

	
}
