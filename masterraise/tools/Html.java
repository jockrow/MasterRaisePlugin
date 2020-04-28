package masterraise.tools;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

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

import org.gjt.sp.jedit.Macros;
import org.gjt.sp.jedit.Registers;
import org.gjt.sp.jedit.Registers.Register;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.textarea.JEditTextArea;

import masterraise.Text;

/**
 * Tools for Html
 * @author Richard Martínez 2011/05/13
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
		boolean accent = false;
		JCheckBox chkInvert = new JCheckBox("Inverse Conversion", false);
		JButton ok = new JButton("OK");

		String ENTITY_ACCENT = "&aacute; -> á";
		String ACCENT_ENTITY = "á -> &aacute;";

		public Entities() { }

		public Entities(boolean accent) {
			this.accent = accent;
		}

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
						accent = !chkInvert.isSelected();
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
					lblInvert.setText(lblInvert.getText().equals(ACCENT_ENTITY) ? ENTITY_ACCENT : ACCENT_ENTITY);
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

		public String processText(){
			String t = iniSelectedText();

			if(accent){
				for(int i=0; i<ARR_CHARS.length; i++){
					if(!ARR_CHARS[i][0].equals("&")){
						t = t.replaceAll(ARR_CHARS[i][0], ARR_CHARS[i][2]);
					}
				}
			}
			else{
				for(int i=0; i<ARR_CHARS.length; i++){
					t = t.replaceAll(ARR_CHARS[i][2], ARR_CHARS[i][0]);
				}
			}

			endSelectedText(t);
			return t;
		}

		public boolean isAccent(){
			return accent;
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

	public String convertHtmlEntities(boolean chkInvert){
		return new Entities(chkInvert).processText();
	}

	/**
	 * Extract a list for all fields from html file 
	 */
	public String getFieldsList(){
		openTmpBuffer();

		if(!findBuffer("<" + HTML_FILTER_FIELDS, "air")){
			Macros.error(view, "Fields not found");
			return "";
		}

		replaceBuffer("<[ \\t]*", "\\n<", "r");
		replaceBuffer("^((?!<" + HTML_FILTER_FIELDS + ").)*$", "", "ir");
		replaceBuffer("(.*type[ \t]*=[ \t\"\']*" + HTML_NOT_FILTER_FIELDS + ".*)|" + BLANK_LINE, "", "ir");

		String fields = getBfTmp().getText();
		closeTmpBuffer();
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
	 * 127	Bogotá D.C.-Aero Ambulancias 24 Ltda
	 * 298	Bogotá D.C.-Aga Fano Fabrica Nacional De Oxigeno S A Cundi
	 * 324	Bogotá D.C.-Ambulancias Abc Ltda
	 * 371	Bogotá D.C.-Ambulancias auxilios y emergencias
	 * 346	Bogotá D.C.-Ambulancias Meyday Ltda
	 * 326	Bogotá D.C.-Ambulancias Urgencia Vital
	 * 327	Niño
	 * 328	NIÑO Jesús
	 */
	public String options2Csv(){
		String strSyntaxError = "Syntax Error Html Options";
		String t=iniSelectedText();

		if(countOccurrences(t.toLowerCase(), "(?m)<option[ ]+.*</option>", "ir") <= 0){
			Macros.error(view, strSyntaxError);
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
