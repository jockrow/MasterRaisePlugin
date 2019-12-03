package masterraise.tools;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.Macros;
import org.gjt.sp.jedit.Registers;
import org.gjt.sp.jedit.Registers.Register;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.textarea.JEditTextArea;

import masterraise.Text;

/**
 * Tools for Html
 * @author Richard Mart�nez 2011/05/13
 *
 */
public class Html extends Text{
	private JDialog dialog = new JDialog(view, "Convert Query", true);
	int i = 0;
	
	public Entities getEntities(){
		return new Entities();
	}
	
	/**
	 * Method htmlNamedEntities()
	 * Replace all chars like as accent for html Entities
	 */
	public class Entities{
		String result = "";
		JCheckBox chkInvert = new JCheckBox("Inverse Conversion", false);
		JButton ok = new JButton("OK");

		String ENTITY_ACCENT = "&aacute; -> �";
		String ACCENT_ENTITY = "� -> &aacute;";
		
		public void showGui(){
			KeyListener ka = new KeyListener(){
				public void keyReleased(KeyEvent ke){
					if (ke.getKeyCode() == KeyEvent.VK_ESCAPE){
						dialog.dispose();
					}
				}

				public void keyPressed(KeyEvent ke){}
				public void keyTyped(KeyEvent ke){}
			};

			ActionListener al = new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					switch(e.getActionCommand()){
						case "OK":
							dialog.dispose();
							setResult();
							processText();
						break;
						case "Cancel":
							dialog.dispose();
						break;
					}
				}
			};

			dialog = new JDialog(view, "Convert Html Entities", true);
			JPanel content = new JPanel(new BorderLayout());
			content.setBorder(new EmptyBorder(12, 12, 12, 12));
			dialog.setContentPane(content);
			JPanel fieldPanel = new JPanel(new GridLayout(2, 2));

			JLabel lblInvert = new JLabel(ACCENT_ENTITY);
			chkInvert.addKeyListener(ka);
			chkInvert.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					lblInvert.setText(lblInvert.getText().equals(ENTITY_ACCENT) ? ACCENT_ENTITY : ENTITY_ACCENT);
				}
			});

			fieldPanel.add(chkInvert);
			fieldPanel.add(lblInvert);
			content.add(fieldPanel, "North");

			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
			buttonPanel.setBorder(new EmptyBorder(12, 50, 0, 80));
			JButton cancel = new JButton("Cancel");
			ok.setPreferredSize(cancel.getPreferredSize());
			dialog.getRootPane().setDefaultButton(ok);
			buttonPanel.add(ok);
			buttonPanel.add(Box.createHorizontalStrut(6));
			buttonPanel.add(cancel);
			content.add(buttonPanel, "South");

			ok.addActionListener(al);
			cancel.addActionListener(al);

			dialog.pack();
			dialog.setLocationRelativeTo(view);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		}
		
		public void processText(){
			String t = iniSelectedText();

			if(chkInvert.isSelected()){
				for(int i=0; i<ARR_CHARS.length; i++){
					t = t.replaceAll(ARR_CHARS[i][2], ARR_CHARS[i][0]);
				}
			}
			else{
				for(int i=0; i<ARR_CHARS.length; i++){
					if(!ARR_CHARS[i][0].equals("&")){
						t = t.replaceAll(ARR_CHARS[i][0], ARR_CHARS[i][2]);
					}
				}
			}

			endSelectedText(t);
		}

		public void setResult(){
			result = String.valueOf(chkInvert.isSelected());
		}

		public String getResult(){
			return result;
		}
	}
	
	/**
	 * Show table Html Entities Table
	 */
	public void showEntitiesTable(){
		int[]arr = new int[3];
		JTable table = new JTable();
		
		KeyListener kl = new KeyListener(){
			@Override
			public void keyTyped(KeyEvent e) { }

			@Override
			public void keyPressed(KeyEvent e){
				JEditTextArea ta = jEdit.getActiveView().getTextArea();
				arr[i]=e.getKeyCode();
				i++;
				if(e.getKeyCode() == KeyEvent.VK_ESCAPE)
				{
					if(Macros.confirm(view, "Are you sure to close?", JOptionPane.YES_NO_OPTION)==0){
						dialog.dispose();
					}
					else{
						table.requestFocus();
					}
				}
				else if (e.getKeyCode() == KeyEvent.VK_ENTER)
				{
					ta.setSelectedText(ARR_CHARS[table.getSelectedRow()][table.getSelectedColumn()]);
					dialog.dispose();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if(table.getSelectedRowCount() == 1 && (arr[0]==KeyEvent.VK_CONTROL && arr[1]==KeyEvent.VK_C || arr[0]==KeyEvent.VK_SHIFT && arr[1]==KeyEvent.VK_INSERT)){
					Registers.setRegister('$', (Register) table.getValueAt(table.getSelectedRow() + table.getSelectedRowCount() - 1, table.getSelectedColumn() + table.getSelectedColumnCount() - 1));
				}
				i = 0;
			}
		};

		table.setModel(new DefaultTableModel(ARR_CHARS, new String[]{"Result", "Description", "Entity Name", "Entity Number"}));
		table.setRowSorter(new TableRowSorter<TableModel>(table.getModel()));
		table.setDefaultEditor(Object.class, null);
		table.getColumnModel().getColumn(0).setPreferredWidth(25);
		table.getColumnModel().getColumn(1).setPreferredWidth(150);
		table.addKeyListener(kl);

		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				table.requestFocus();
			}
		});

		JScrollPane tablePane = new JScrollPane(table);
		tablePane.setPreferredSize(new Dimension(550, 300));

		dialog = new JDialog(view, "Html Entities Table", false);
		Container c = dialog.getContentPane();
		c.add(tablePane, "North");
		dialog.pack();
		dialog.setLocationRelativeTo(view);
		dialog.setVisible(true);
	}
	

	/**
	 * Extract a list for all fields from html file 
	 */
	public String getFieldsList(){
	//TODO:VERIFICAR QUE AL DAR DESHACER SE DEVUELVA AL TEXTO ORIGINAL
		final String TYPE_FIELDS = "(select|input|textarea|datalist)";
		Buffer bfTmp = openTempBuffer();
		String fields = "";

		if(!findBuffer("<[ \\t]*" + TYPE_FIELDS + "[ \\t].*>", "air")){
			Macros.message(view, "Fields not found");
			return "";
		}
		
		textArea.goToBufferStart(false);
		
		//TODO:hay que revisar que no pregunte a cada momento el reemplazo
		while(findBuffer("<[ \\t]*" + TYPE_FIELDS + "[ \\t].*>", "ir")){
			fields += textArea.getSelectedText() + "\n";
		}

		textArea.setText(fields);
		
		replaceBuffer("<[ \t]*" + TYPE_FIELDS, "\n$0", "ir");
		replaceBuffer("(^\n)|(^.*type[ \t]*=[ \t\"\']*(submit|reset|button).*\n)", "", "ir");
		replaceBuffer(">.*", ">", "r");

		fields = bfTmp.getText();
		closeTempBuffer(bfTmp);
		return fields;
	}
	
	/**
	 * remove tag from Html options, showing only title that user see at combo
	 * <pre>
	 * @example
	 * {@code
	 * <OPTION value=-1>Seleccione</OPTION><OPTION value=127>Bogot&#225; D.C.-Aero Ambulancias 24 Ltda
	 * </OPTION><OPTION value=298>Bogot&aacute; D.C.-Aga Fano Fabrica Nacional De Oxigeno S A Cundi</OPTION><OPTION value=324>Bogot&#225; D.C.-Ambulancias Abc Ltda</OPTION>
	 * <OPTION value=371>Bogot&#225; D.C.-Ambulancias auxilios y emergencias</OPTION><OPTION value=346>Bogot&#225; D.C.-Ambulancias Meyday Ltda</OPTION><OPTION value=326>Bogot&#225; D.C.-Ambulancias Urgencia Vital</OPTION>
	 * <OPTION value=327>Ni&#241;o</OPTION>
	 * <OPTION value=328>NI&#209;O Jes&uacute;s</OPTION>
	 * } 
	 *
	 * To:
	 * -1	Seleccione
	 * 127	Bogot� D.C.-Aero Ambulancias 24 Ltda
	 * 298	Bogot� D.C.-Aga Fano Fabrica Nacional De Oxigeno S A Cundi
	 * 324	Bogot� D.C.-Ambulancias Abc Ltda
	 * 371	Bogot� D.C.-Ambulancias auxilios y emergencias
	 * 346	Bogot� D.C.-Ambulancias Meyday Ltda
	 * 326	Bogot� D.C.-Ambulancias Urgencia Vital
	 * 327	Ni�o
	 * 328	NI�O Jes�s
	 */
	public String options2Csv(){
		String strSyntaxError = "Syntax Error Html Options";
		String t=iniSelectedText();

		if(!Pattern.compile("(?m)<option[ ]+.*</option>").matcher(t.toLowerCase()).find()){
			Macros.message(view, strSyntaxError);
			return "";
		}

		t=t.replaceAll("(?mi)<[ \t]*option", "\n<option");
		t=t.replaceAll("(?mi)(.*<option[ \t]+value[ \t]*=|[ \t]*<[ \t]*/option.*)", "");
		t=t.replaceAll("(?mi)[ \t]*>[ \t]*", "\t");
		t=t.replaceAll("(?mi)^\n", "");

		for(int i=0; i<ARR_CHARS.length; i++){
			t=t.replaceAll(ARR_CHARS[i][2] + "|" + ARR_CHARS[i][3], ARR_CHARS[i][0]);
			if(ARR_CHARS[i][0].equals(LOW_ENIE)){
				break;
			}
		}
		endSelectedText(t);
		return t;
	}
}
