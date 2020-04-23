package masterraise.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.Macros;
import org.gjt.sp.jedit.jEdit;

import masterraise.Text;
import masterraise.files.MrFile;

/**
 * Tools for Query
 * @author Richard Martinez 2015/04/21
 *
 */
public class Query extends Text{
	private Buffer bfTmp = null;
	private String queryType = "";
	private JDialog dialog = new JDialog(view, "Convert Query", true);
	private JPanel content = new JPanel(new BorderLayout());
	private JPanel buttonPanel = new JPanel();
	private JButton btnOk = new JButton("Convert");
	private JButton btnCancel = new JButton("Cancel");
	private String msgSyntaxError = "Syntax Error in %s Query";

	public BeautyQuery getBeautyQuery(){
		return new BeautyQuery("");
	}

	public ConvertQuery getConvertQuery(){
		return new ConvertQuery("", "");
	}

	/**
	 * Verify if query has syntaxError
	 * @return boolean - if true has Syntax error
	 */
	private boolean hasSyntaxError(){
		boolean syntaxError = false;
		if(queryType.indexOf("CSV") < 0) {
			if(queryType.equals("")){
				syntaxError = true;
			}
			else if(queryType.equals("SELECT") && countOccurrences(textArea.getText(), "\\bSELECT.*\\bFROM \\w", "r") == 0){
				syntaxError = true;
			}
			else if(queryType.equals("INSERT") && countOccurrences(textArea.getText(), "\\bINSERT INTO \\w+\\(.*\\) VALUES\\(.*\\)", "r") == 0){
				syntaxError = true;
			}
			else if(queryType.equals("UPDATE") && countOccurrences(textArea.getText(), "\\bUPDATE \\w+ SET \\w+.*=\\p{Print}", "r") == 0){
				syntaxError = true;
			}
			else if(queryType.equals("SP") && countOccurrences(textArea.getText(), "\\(.*\\) VALUES\\(.*\\)", "r") == 0){
				syntaxError = true;
			}
			if(syntaxError){
				msgSyntaxError = String.format(msgSyntaxError, new Object[] {queryType});
				return true;
			}
		}
		else{
			if(queryType.equals("CSV_SELECT JOIN")){
				if(countOccurrences(textArea.getText(), "\\A\\w+" + TRIM_RIGHT, "r") > 0){
					msgSyntaxError = "Please Quit the table name, only must have data";
					return true;
				}
			}
			else{
				if(countOccurrences(textArea.getText(), "\\A\\w+\\n", "r") == 0){
					msgSyntaxError = String.format(msgSyntaxError, new Object[] {"CSV"}) + ", \nMust have table Name in first line";
					return true;
				}
			}
		}

		return syntaxError;
	}

	/**
	 * initial format query
	 */
	private void startFormatQuery(String query){
		replaceBuffer("\\[|\\]", "", "r");
		replaceBuffer("([^\\(])(' )(\\d{2}/\\d{2}/\\d{4})(' )", "TO_DATE('$3', 'dd/MM/yyyy')", "r");
		replaceBuffer("([^\\(])(' )(\\d{4}/\\d{2}/\\d{2})(' )", "TO_DATE('$3', 'yyyy/MM/dd')", "r");
		replaceBuffer(SQL_RESERVED, "_1.toUpperCase()", "br");
		replaceBuffer("[ \\t]+\\(", "(", "r");
		//format decimal
		replaceBuffer(SQL_NUMBER, "_0.replaceAll(\",\", \".\")", "br");
		replaceBuffer(COMMENTS, "", "ir");
		//modify the temp tables, for recovery later
		replaceBuffer("#", SHARP, "");

		if(!query.equals("CSV")){
			smartJoin();
		}

		replaceBuffer(".", DOT, "");
		replaceBuffer(TRIM_COMA, ",", "r");

		if(hasSyntaxError()){
			jEdit._closeBuffer(view,(Buffer) bfTmp);
			Macros.error(view, msgSyntaxError);
			return;
		}
		if(queryType.equals("INSERT") || queryType.equals("SP")) {
			replaceBuffer("\\A\\(|\\)\\z", "", "r");
		}
		
		replaceBuffer("(INSERT INTO \\w+)(\\()", "$1\\n", "r");
		replaceBuffer("\\) VALUES\\(", "\\n", "r");
		replaceBuffer(SQL_QUOTES_VALUES, "_1 + _2.replace(\",\", \"" + COMA + "\")", "br");
		replaceBuffer(SQL_FUNCTION, "_1 + _3.replace(\",\", \"" + COMA + "\")", "br");
		replaceBuffer(")", ROUND_BRACKET_RIGHT, "");
		replaceBuffer("(", ROUND_BRACKET_LEFT, "");
		replaceBuffer("SET ", "", "");
	}

	/**
	 * end format query
	 */
	private void endFormatQuery(){
		replaceBuffer(DOUBLE_SPACES, " ", "r");
		replaceBuffer(DOT, ".", "");
		replaceBuffer(ROUND_BRACKET_RIGHT + "[ ]?", ")", "r");
		replaceBuffer("(\\))(\\w)", ") $2", "r");
		//recovery temp tables
		replaceBuffer(SHARP, "#", "r");
		replaceBuffer(ROUND_BRACKET_LEFT, "(", "");
		replaceBuffer(COMA, ", ", "r");
		replaceBuffer("\\b(AND|OR|,)\\b", "\\n\\t$0", "r");
		replaceBuffer("^[ \\t]*\\n|" + TRIM_RIGHT, "", "r");
	}

	/**
	 * Format a query for easy read
	 */
	public class BeautyQuery{
		private String opts = "ui";
		private JCheckBox chkWithNolock;

		public BeautyQuery(String opts){
			this.opts += opts;
		}

		public void showGui(){
			String bufferText = textArea.getBuffer().getText().toUpperCase();
			chkWithNolock = new JCheckBox("set With Nolock", false);

			KeyAdapter ka = new KeyAdapter(){
				public void keyReleased(KeyEvent evt){
					if(evt.getKeyCode() == KeyEvent.VK_ESCAPE){
						dialog.dispose();
					}
				}
			};

			if(bufferText.indexOf("SELECT") != -1 && bufferText.indexOf("FROM") != -1){
				content.add(chkWithNolock, BorderLayout.NORTH);
				chkWithNolock.addKeyListener(ka);
			}

			btnOk = new JButton("Beautify");
			btnOk.addKeyListener(ka);
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(btnOk, BorderLayout.WEST);
			buttonPanel.add(btnCancel, BorderLayout.EAST);

			btnOk.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent evt){
					if(chkWithNolock.isSelected()) opts += "l";

					if(chkWithNolock.isSelected()){
						Macros.error(view, "You have to Check at least one Option");
					}
					else {
						processText();
					}
					dialog.dispose();
				}
			});

			btnCancel.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent evt){
					dialog.dispose();
				}
			});

			dialog  = new JDialog(view, "Beauty Query", true);
			content.setBorder(new EmptyBorder(8, 8, 8, 8));
			content.setPreferredSize(new Dimension(200, 75));
			content.add(buttonPanel, BorderLayout.SOUTH);

			dialog.setContentPane(content);
			dialog.pack();
			dialog.setLocationRelativeTo(view);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.getRootPane().setDefaultButton(btnOk);
			dialog.setVisible(true);
		}

		/**
		 * Beauty format query
		 * @param opts u: set upper case reserved words, l: set WITH(NOLOCK) when query is SELECT, i: Indent query, f:if this function invoked from beauty Query Macro
		 * @example
		 * <pre>
		 *select distinct Prestar.dbo.autAutorizacionASP.autIDAutorizacion as AUTORIZACON, commDivision_2.divNom AS REGIONAL_IPS_ORIGEN
		 *, dos , '1, 2'
		 *--comentario
		 *, funcion , funcion(par1, 'par2')
		 *from         dbo.commDivision AS commDivision_3 inner join
		 *	 dbo.commDivision with (nolock) on commDivision_3.divIDDivision = dbo.commDivision.divIDDivisionPadre inner join
		 *	 dbo.redIPS AS Ips_Transcriptor on dbo.commDivision.divIDDivision = Ips_Transcriptor.ipsIDDivision inner join
		 *	 Prestar.dbo.autAutorizacionASP 	ON Prestar.dbo.autAutorizacionASP.autIDIPSOrigen <> Prestar.dbo.autDetalleAutorizacionASP.autIDIPS inner join
		 *	 Prestar.dbo.autDetalleAutorizacionASP with (nolock) on
		 *	 Prestar.dbo.autAutorizacionASP.autIDAutorizacion = Prestar.dbo.autDetalleAutorizacionASP.autIDAutorizacion AND
		 *	 Prestar.dbo.autAutorizacionASP.autIDIPSOrigen <> Prestar.dbo.autDetalleAutorizacionASP.autIDIPS inner join
		 *	 dbo.redIPS AS IPS_Origen with (nolock) ON Prestar.dbo.autAutorizacionASP.autIDIPSOrigen = IPS_Origen.ipsIDIPS inner join
		 *	 dbo.commTablaTablas with (nolock) ON Prestar.dbo.autDetalleAutorizacionASP.autEstado = dbo.commTablaTablas.tblCodElemento inner join
		 *	 dbo.commDivision commDivision_2 with (nolock) inner join
		 *	 dbo.commDivision AS commDivision_1 with (nolock) ON commDivision_2.divIDDivision = commDivision_1.divIDDivisionPadre on and  
		 *	 IPS_Origen.ipsIDDivision = commDivision_1.divIDDivision and  Ips_Transcriptor.ipsIDIPS = Prestar.dbo.autDetalleAutorizacionASP.autIDIPS 
		 *	 WHERE Prestar.dbo.autAutorizacionASP.autIDAutorizacion = 1 AND Prestar.dbo.autAutorizacionASP.autIDAutorizacion = 123 OR Prestar.dbo.autAutorizacionASP.autIDAutorizacion = 456
		 *
		 *to:
		 *SELECT DISTINCT Prestar.dbo.autAutorizacionASP.autIDAutorizacion, commDivision_2.divNom
		 *	, dos, '1, 2'
		 *	, funcion, funcion(par1, 'par2')
		 *FROM dbo.commDivision AS commDivision_3
		 *INNER JOIN dbo.commDivision WITH(NOLOCK) ON commDivision_3.divIDDivision = dbo.commDivision.divIDDivisionPadre
		 *INNER JOIN dbo.redIPS AS Ips_Transcriptor ON dbo.commDivision.divIDDivision = Ips_Transcriptor.ipsIDDivision
		 *INNER JOIN Prestar.dbo.autAutorizacionASP ON Prestar.dbo.autAutorizacionASP.autIDIPSOrigen <> Prestar.dbo.autDetalleAutorizacionASP.autIDIPS
		 *INNER JOIN Prestar.dbo.autDetalleAutorizacionASP WITH(NOLOCK) ON Prestar.dbo.autAutorizacionASP.autIDAutorizacion = Prestar.dbo.autDetalleAutorizacionASP.autIDAutorizacion
		 *	AND Prestar.dbo.autAutorizacionASP.autIDIPSOrigen <> Prestar.dbo.autDetalleAutorizacionASP.autIDIPS
		 *INNER JOIN dbo.redIPS AS IPS_Origen WITH(NOLOCK) ON Prestar.dbo.autAutorizacionASP.autIDIPSOrigen = IPS_Origen.ipsIDIPS
		 *INNER JOIN dbo.commTablaTablas WITH(NOLOCK) ON Prestar.dbo.autDetalleAutorizacionASP.autEstado = dbo.commTablaTablas.tblCodElemento
		 *INNER JOIN dbo.commDivision commDivision_2 WITH(NOLOCK)
		 *INNER JOIN dbo.commDivision AS commDivision_1 WITH(NOLOCK) ON commDivision_2.divIDDivision = commDivision_1.divIDDivisionPadre ON
		 *	AND IPS_Origen.ipsIDDivision = commDivision_1.divIDDivision
		 *	AND Ips_Transcriptor.ipsIDIPS = Prestar.dbo.autDetalleAutorizacionASP.autIDIPS
		 *WHERE Prestar.dbo.autAutorizacionASP.autIDAutorizacion = 1
		 *	AND Prestar.dbo.autAutorizacionASP.autIDAutorizacion = 123
		 *	OR Prestar.dbo.autAutorizacionASP.autIDAutorizacion = 456
		 */
		public String processText(){
			bfTmp = openTmpBuffer();

			if(textArea.getText().toUpperCase().indexOf("FROM") >= 0 && textArea.getText().toUpperCase().indexOf("INSERT") >= 0){
				queryType = "INSERT SELECT";
			}
			else if(textArea.getText().toUpperCase().indexOf("FROM") >= 0){
				queryType = "SELECT";
			}
			else if(textArea.getText().toUpperCase().indexOf("INSERT") >= 0){
				queryType = "INSERT";
			}
			else if(textArea.getText().toUpperCase().indexOf("UPDATE") >= 0){
				queryType = "UPDATE";
			}

			startFormatQuery(queryType);

			//			//////TODO:mover a startFormatQuery {{{
			replaceBuffer(SQL_RESERVED_LINE, "\\n$1", "r");
			//			//TODO:Cuando el query es diferente de Select hay que convertirlo directamente
			if(queryType.equals("SELECT")){
				textArea.goToBufferStart(false);
				textArea.goToEndOfWhiteSpace(true);
				replaceSelection(",", "\\n", "r");
				replaceSelection("(\\w+)(\\n.*).*", "_0.replaceAll(\"\\n\", \"	\")", "bir");
				replaceSelection("\\t", ", ", "r");
				replaceSelection("^", "\\t, ", "r");
				replaceSelection("\\t, SELECT", "SELECT", "r");

				if(opts.indexOf('l') >= 0){
					replaceBuffer("(WITH)? [\\(]*NOLOCK[\\)]?", "", "r");
					replaceBuffer("\\b(FROM|JOIN) (\\w+\\.*)+ (\\bAS )?\\w+", "$0 WITH(NOLOCK) ", "r");
				}
			}
			//			//TODO:revisar este
			else if(queryType.equals("INSERT SELECT")){
				replaceBuffer("(\\)[ \\t])(SELECT)", ")\\n$2", "r");
				findBuffer("(SELECT.*\\n)(^.*\\n)+(FROM)", "ar");
				replaceSelection("^", ", ", "r");
				replaceSelection("(, )(" + SQL_RESERVED + ")", "$2", "r");
			}
			//			//////TODO:mover a startFormatQuery }}}

			endFormatQuery();

			String convertedQuery = bfTmp.getText();
			closeTmpBuffer(bfTmp);
			dialog.dispose();

			return convertedQuery;
		}
	}

	/**
	Convert a query in another query
	 */
	public class ConvertQuery{
		private static final String SELECT = "SELECT";
		private static final String SELECT_JOIN = "SELECT JOIN";
		private String query1 = "";
		private String query2 = "";
		private String convertion = "";
		private String nameTable = "";
		private String csvPrefix = "";
		private JRadioButton rbFromInsert = new JRadioButton("INSERT");
		private JRadioButton rbToInsert = new JRadioButton("INSERT");
		private JRadioButton rbFromSelect = new JRadioButton(SELECT);
		private JRadioButton rbToSelect = new JRadioButton(SELECT);
		private JRadioButton rbFromUpdate = new JRadioButton("UPDATE");
		private JRadioButton rbToUpdate = new JRadioButton("UPDATE");
		private JRadioButton rbFromCsv = new JRadioButton("CSV");
		private JRadioButton rbToCsv = new JRadioButton("CSV");

		/**
		 * Constructor for test or bulk process use
		 * @param query1 first query query to convert
		 * @param query2 final converted query
		 */
		public ConvertQuery(String query1, String query2) {
			this.query1 = query1;
			this.query2 = query2;
		}

		/**
		 * Set name table from query
		 */
		private void setNameTable(){
			String regExQuery = "";
			String deleteRegExpToReplace = "";
			switch(query1){
			case "INSERT":
				regExQuery = "\\bINSERT INTO \\w+\\n";
				deleteRegExpToReplace = "^\\w+ \\w+ ";
				break;
			case "SELECT":
				regExQuery = "\\bFROM \\w+ ";
				deleteRegExpToReplace = "^\\w+";
				break;
			case "UPDATE":
				regExQuery = "UPDATE \\w+ ";
				deleteRegExpToReplace = "^\\w+ ";
				break;
			case "CSV":
				if(!query2.equals("SELECT JOIN")){
					regExQuery = "\\A\\w+\\n";
					deleteRegExpToReplace = "";
				}
				else {
					return;
				}
				break;
			}

			findBuffer(regExQuery, "air");
			nameTable = textArea.getSelectedText().replaceAll(deleteRegExpToReplace, "") + "\n";
			textArea.delete();
		}

		private void validation(){
			rbToInsert.setEnabled(true);
			rbToSelect.setEnabled(true);
			rbToUpdate.setEnabled(true);
			rbToCsv.setEnabled(true);
			rbToSelect.setText(SELECT);

			if(rbFromInsert.isSelected()){
				rbToInsert.setEnabled(false);
				rbToCsv.setSelected(true);
			}
			else if(rbFromSelect.isSelected()){
				rbToSelect.setEnabled(false);
				rbToCsv.setEnabled(false);
				rbToUpdate.setSelected(true);
			}
			else if(rbFromUpdate.isSelected()){
				rbToUpdate.setEnabled(false);
				rbToSelect.setSelected(true);
			}
			else if(rbFromCsv.isSelected()){
				rbToCsv.setEnabled(false);
				rbToInsert.setSelected(true);
				rbToSelect.setText(SELECT_JOIN);
			}
		}

		public void showGui(){
			ButtonGroup grpFrom = new ButtonGroup();
			ButtonGroup grpTo = new ButtonGroup();

			KeyListener kl = new KeyListener(){
				public void keyReleased(KeyEvent e){
					if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
						dialog.dispose();
					}
					else if(KeyEvent.VK_LEFT <= e.getKeyCode() && e.getKeyCode() <= KeyEvent.VK_DOWN){
						validation();
					}
				}

				public void keyPressed(KeyEvent e){}
				public void keyTyped(KeyEvent e){}
			};

			ActionListener al = new ActionListener(){
				public void actionPerformed(ActionEvent e){
					switch(e.getActionCommand()){
					case "Convert":
						Enumeration<AbstractButton> en = grpFrom.getElements();

						while(en.hasMoreElements() == true){
							JRadioButton r = (JRadioButton) en.nextElement();
							if(r.isSelected()){
								query1 = r.getText();
								break;
							}
						}

						en = grpTo.getElements();
						while(en.hasMoreElements() == true){
							JRadioButton r = (JRadioButton) en.nextElement();
							if(r.isSelected()){
								query2 = r.getText();
								break;
							}
						}

						dialog.dispose();
						processText();
						break;
					case "Cancel":
						dialog.dispose();
						break;
					default:
						validation();
						break;
					}
				}
			};

			SwingUtilities.invokeLater(new Runnable(){
				public void run(){
					rbFromUpdate.requestFocus();
				}
			});

			content.setBorder(new EmptyBorder(12, 12, 12, 12));
			dialog.setContentPane(content);
			JPanel fieldPanel = new JPanel(new GridLayout(5, 1, 0, 6));

			fieldPanel.add(new JLabel("From Query"));
			fieldPanel.add(new JLabel("To Query"));

			rbFromInsert.setActionCommand("From");
			rbFromInsert.addKeyListener(kl);
			rbFromInsert.addActionListener(al);
			grpFrom.add(rbFromInsert);
			fieldPanel.add(rbFromInsert);

			rbToInsert.setActionCommand("To");
			grpTo.add(rbToInsert);
			fieldPanel.add(rbToInsert);

			rbFromSelect.setActionCommand("From");
			rbFromSelect.addKeyListener(kl);
			rbFromSelect.addActionListener(al);
			grpFrom.add(rbFromSelect);
			fieldPanel.add(rbFromSelect);

			rbToSelect.setActionCommand("To");
			grpTo.add(rbToSelect);
			fieldPanel.add(rbToSelect);

			rbFromUpdate.setActionCommand("From");
			rbFromUpdate.addKeyListener(kl);
			rbFromUpdate.addActionListener(al);
			grpFrom.add(rbFromUpdate);
			fieldPanel.add(rbFromUpdate);

			rbToUpdate.setActionCommand("To");
			grpTo.add(rbToUpdate);
			fieldPanel.add(rbToUpdate);

			rbFromCsv.setActionCommand("From");
			rbFromCsv.addKeyListener(kl);
			rbFromCsv.addActionListener(al);
			grpFrom.add(rbFromCsv);
			fieldPanel.add(rbFromCsv);

			rbToCsv.setActionCommand("To");
			grpTo.add(rbToCsv);
			fieldPanel.add(rbToCsv);

			rbToUpdate.setEnabled(false);
			rbFromUpdate.setSelected(true);
			rbToSelect.setSelected(true);

			content.add(fieldPanel, "North");

			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
			buttonPanel.setBorder(new EmptyBorder(12, 50, 0, 80));
			dialog.getRootPane().setDefaultButton(btnOk);
			buttonPanel.add(btnOk);
			buttonPanel.add(Box.createHorizontalStrut(6));
			buttonPanel.add(btnCancel);
			content.add(buttonPanel, "South");

			btnOk.addActionListener(al);
			btnCancel.addActionListener(al);

			dialog.pack();
			dialog.setLocationRelativeTo(view);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		}

		public String processText(){
			bfTmp = openTmpBuffer();
			SpreadSheet sp = new SpreadSheet();
			String fowardQuery = "";
			int lastSemiColon = replaceBuffer(";\\z", "", "ar");

			convertion = query1 + "_" + query2;
			queryType = query2.equals("SELECT JOIN") ? convertion : query1;

			startFormatQuery(query1);

			if(query1.equals("SELECT") || query1.equals("UPDATE")){
				replaceBuffer("ORDER[ \\t]BY.*(\\n.*)*", "", "r");

				if(findBuffer("WHERE.*", "air")){
					fowardQuery = textArea.getSelectedText();
					textArea.delete();
				}
			}

			setNameTable();

			switch(query1){
			case "INSERT":
				replaceBuffer(",", "\\t", "r");
				if(!sp.transposeMatrix()){
					return "";
				}
				break;
			case "SELECT":
				replaceBuffer("\\b(SELECT|DISTINCT) ", "", "r");
				replaceBuffer(",", "\\n", "r");
				replaceBuffer("(\\w+)(\\n.*).*", "_0.replaceAll(\"\\n\", \"	\")", "bir");
				break;
			case "UPDATE":
				replaceBuffer("\\A|,", "\\n", "r");
				replaceBuffer(TRIM_UP, "", "r");
				replaceBuffer(" = ", "\\t", "r");
				break;
			case "CSV":
				replaceBuffer("\"", "", "");
				break;
			}

			switch(query2){
			case "SELECT JOIN":
				if(!findBuffer("'", "a")) {
					textArea.goToBufferStart(false);
					textArea.goToNextLine(false);
					textArea.goToBufferEnd(true);
					replaceSelection("\\t", "'$0'", "r");
					replaceSelection(".*", "'$0'", "r");
				}

				textArea.goToBufferEnd(false);
				textArea.insertEnterAndIndent();
				textArea.goToBufferStart(false);
				transposeLines(textArea);
				textArea.goToBufferEnd(true);
				String tmpQuery = textArea.getSelectedText();
				textArea.delete();
				sp.transposeMatrix();
				replaceBuffer("\\n", ", ", "r");

				tmpQuery = tmpQuery.trim().replaceAll("\t", ", ");
				tmpQuery = tmpQuery.replaceAll("(?m)^", "UNION SELECT ");
				replaceBuffer("\\z", "\\n" + tmpQuery, "r");
				replaceBuffer("\\A", "SELECT ", "r");
				replaceBuffer("\\t", " ", "r");
				replaceBuffer("$", " FROM DUAL", "r");
				break;
			case "SELECT":
				replaceBuffer("\\t", ", ", "r");
				replaceBuffer("^", "\\t, ", "r");
				replaceBuffer("\\A\\t,", "SELECT", "r");
				replaceBuffer("\\z", "\nFROM " + nameTable.trim(), "r");
				break;
			case "INSERT":
				if(!sp.transposeMatrix()) return "";
				replaceBuffer("\\t", ", ", "r");
				textArea.goToBufferStart(false);
				textArea.selectLine();
				replaceSelection(".*", "INSERT INTO " + nameTable + "($0)", "r");
				textArea.goToBufferEnd(false);
				textArea.selectLine();
				replaceSelection(".*", "VALUES($0)", "r");
				break;
			case "UPDATE":
				replaceBuffer("\\t", " = ", "r");
				replaceBuffer("^", "\\t, ", "r");
				replaceBuffer(TRIM_UP + ",", "UPDATE " + nameTable + "SET", "r");
				break;
			case "CSV":
				csvPrefix = String.format(CSV_PREFIX, new Object[] { nameTable.trim() });
				replaceBuffer("\\A", csvPrefix, "r");
				break;
			}

			if(!query2.equals("CSV")) {
				if(!query2.equals("INSERT") && fowardQuery != ""){
					replaceBuffer("\\z", "\\n" + fowardQuery.trim(), "r");
				}
				if(!query2.equals("SELECT JOIN") && lastSemiColon > 0){
					replaceBuffer("\\z", ";", "r");
				}
			}

			endFormatQuery();

			String convertedQuery = bfTmp.getText();
			closeTmpBuffer(bfTmp);
			return convertedQuery;
		}
	}

	/**
	 * Convert fields tables to any Language
	 * @example
	 * <pre>
	 * NU_TO_INVOICE
	 * CD_INVOICE

	 * TO:
	 * private String nuToInvoice = "";
	 * private String cdInvoice = "";

	 * public String getNuToInvoice(){
	 * 	return nuToInvoice;
	 * }

	 * public void setNuToInvoice(String nuToInvoice){
	 * 	this.nuToInvoice = nuToInvoice;
	 * }

	 * public String getCdInvoice(){
	 * 	return cdInvoice;
	 * }

	 * public void setCdInvoice(String cdInvoice){
	 * 	this.cdInvoice = cdInvoice;
	 * }
	 */
	public String queryToLanguage(){
		bfTmp = openTmpBuffer();

		textArea.setText(firsUpperCase(textArea.getText(), '_'));
		replaceBuffer("_", "", "");
		replaceBuffer("(^)(\\w)(.*.)($)", "\"private String \" + _2.toLowerCase() + _3 + \" = \\\"\\\";\"", "br");
		String fields = textArea.getText() + "\n\n";
		new Java().genGetSet();
		replaceBuffer("\\A", fields, "r");
		closeTmpBuffer(bfTmp);

		return textArea.getText();
	}

	/**
	 * Format a list for where:
	 * <pre>
	 * 1
	 * 2
	 * 3
	 * 
	 * To:
	 * in(1,2,3)
	 * 
	 * or
	 * 123
	 * 456
	 * 456
	 * fdsa
	 * 789
	 * 789
	 * 
	 * To:
	 * in('one', 'two', '3')
	 */
	public void formatIn(){
		bfTmp = openTmpBuffer();
		deleteDuplicates(textArea);
		boolean isNotNumber = findBuffer("[\\p{Alpha}/\\*\\-\\+,\\(\\)\\\"#\\$&]", "air");

		replaceBuffer(BLANK_LINE, "", "r");
		replaceBuffer(BLANK_SPACE, "", "r");
		replaceBuffer(".+", ", '$0'", "r");
		replaceBuffer("\\A, ", "IN(", "r");
		replaceBuffer("\\z", ")", "r");

		if(!isNotNumber){
			replaceBuffer("'", "", "");
		}

		closeTmpBuffer(bfTmp);
	}

	/**
	 * Convert any Query to SQLite
	 */
	public void convertToSqLite(){
		String t = iniSelectedText();
		t=t.replaceAll("(?m)\"| (ENABLE|BYTE|NOVALIDATE)\\b", "");
		t=t.replace("(?m)NUMBER(*", "NUMBER(1");
		t=t.replaceAll("(?m)(\\w+\\.)(\\w+)", "$2");
		endSelectedText(t);
	}

	/**
	 * Settings declared variables into Stored Procedure
	 * @example
	 * <pre>
	 * {@code
	 * (@CODIGO VARCHAR(6)=NULL,
	 * @NOMBRE VARCHAR(10)=NULL)
	 * values(@CODIGO = '',
	 * {@literal @}NOMBRE = 'domiciliar')
	 * 
	 * To:
	 * DECLARE @CODIGO VARCHAR(6)
	 * , @NOMBRE VARCHAR(10)
	 * 
	 * SET @CODIGO = ''
	 * SET @NOMBRE = 'domiciliar'
	 *
	 * Or:
	 * (@LINKEO AS VARCHAR(50) ,
	 * {@literal @}strconvenio AS VARCHAR(5) ,
	 * {@literal @}strRegional AS VARCHAR(50) ,
	 * {@literal @}strCadenaPermisos as varchar (50),
	 * {@literal @}strFIni as varchar (10) = NULL ,
	 * {@literal @}PageSize as numeric (18,2)  ,
	 * {@literal @}i_RegInicial int,
	 * {@literal @}i_maxRegistros int
	 *  )
	 * values('PRESTAR.DBO.', '96' ,  '3, 4' ,  '(1,2)' , null,  function(1, 2) ,1 , 30)
	 * 
	 * POR:
	 * DECLARE @LINKEO AS VARCHAR(50)
	 * , @strconvenio AS VARCHAR(5)
	 * , @strRegional AS VARCHAR(50)
	 * , @strCadenaPermisos AS VARCHAR (50)
	 * , @strFIni AS VARCHAR (10)
	 * , @PageSize AS NUMERIC (18,2)
	 * , @i_RegInicial INT
	 * , @i_maxRegistros INT
	 * 
	 * SET @LINKEO = 'PRESTAR.DBO.'
	 * SET @strconvenio = '96'
	 * SET @strRegional = '3, 4'
	 * SET @strCadenaPermisos = '(1,2)'
	 * SET @strFIni = NULL
	 * SET @PageSize = function(1, 2)
	 * SET @i_RegInicial = 1
	 * SET @i_maxRegistros = 30
	 */
	public String sqlServerSetVariablesSp(){
		bfTmp = openTmpBuffer();
		queryType = "SP";
		startFormatQuery(queryType);
		boolean hasAt = findBuffer("VALUES(@", "a");
		replaceBuffer("^\\(|\\)$", "", "r");

		//remove assignments to first line
		textArea.goToBufferStart(false);
		textArea.selectLine();
		replaceSelection("=[ ]*([ ]{0,}\\w+|'')", "", "r");

		//case the second line has at char (@), only assign variables
		if(hasAt){
			textArea.goToBufferEnd(false);
			textArea.goToStartOfWhiteSpace(true);
			replaceSelection("\\A|, ", "\\nSET ", "r");
			replaceBuffer(", ", "\\n, ", "r");
		}
		//assign each values for each variable
		else{
			replaceBuffer(",", "\\t", "r");
			String firstLine = textArea.getSelectedText().replaceAll("\t", "\n, ") + "\n\n";
			textArea.selectAll();
			//TODO:capturar el error
			if(!new SpreadSheet().transposeMatrix()) {
				return "";
			}

			replaceBuffer(SQL_ALIAS, "$1", "ir");
			replaceBuffer("\t", " = ", "r");
			replaceBuffer("^", "SET ", "r");
			replaceBuffer("\\A", firstLine, "r");
		}

		replaceBuffer("\\A", "DECLARE ", "r");
		replaceBuffer("^SET.*DEFAULT[ ]*\\n*", "", "r");

		//remove unused reserved words
		replaceBuffer("OUTPUT", "", "");
		endFormatQuery();
		replaceBuffer("((\'|NUMERIC)\\()(.*)", "_1 + _3.replace(\".\", \",\")", "br");
		closeTmpBuffer(bfTmp);

		return textArea.getText();
	}

	/**
	 * Identify temporal variables into Stored Procedure
	 */
	public String sqlServerGetTmpTables(){
		if(!findBuffer("#", "a")){
			Macros.error(view, "Not found temp tables");
			return "";
		}

		bfTmp = openTmpBuffer();

		replaceBuffer(COMMENTS, "", "ir"); 

		//don't take primary keys from temp table like if this would a normal table, example: xll#SPDI
		replaceBuffer("\\w+#\\b\\w+\\b", "\\n$0\\n", "r");
		replaceBuffer("(\\w+)(#)(\\b\\w+\\b)", "$1__$3", "r");
		replaceBuffer("#{1,2}\\b[^ 0-9\\(]+\\w+\\b", "\\n$0\\n", "r");
		replaceBuffer(BLANK_LINE, "", "r");
		// replace all lines that doesn't begin for temp table (#temporal)
		replaceBuffer("^[^#].*(\\n|\\z)", "", "r");
		// remove possibles extrange chars
		replaceBuffer("[!\"$%&'\\(\\)*+,-./:;=>?@\\[\\\\\\]^`{|}~]", "", "r");
		replaceBuffer(TRIM, "", "r");

		textArea.selectAll();
		textArea.toUpperCase();
		deleteDuplicates(textArea);

		replaceBuffer("^", "DROP TABLE ", "r");
		replaceBuffer("^DROP TABLE ##", "--DROP TABLE ##", "r");
		sortLines(textArea);

		replaceBuffer("\\z", "\n\n" + previousText, "r");
		replaceBuffer(TRIM_UP, "", "r");
		closeTmpBuffer(bfTmp);

		return textArea.getText();
	}

	/**
	 * convert ldr file to rename images like as bat file
	 * @example
	 * Query:
	 * SELECT 
	 * FC.FOTO_CAFICULTOR, CAF.NRO_DOC_CAFICULTOR
	 * , CAF.ID_CAFICULTOR
	 *    FROM SICA.SC_FOTO_CAFICULTOR FC
	 *    INNER JOIN SICA.SC_CAFICULTOR CAF ON FC.ID_CAFICULTOR = CAF.ID_CAFICULTOR
	 *    WHERE CAF.NRO_DOC_CAFICULTOR IN('3454666')
	 *
	 * select the folder to extract Images:
	 * D:\testGenerateAll\Antioquia
	 * 	TABLE_EXPORT_DATA.ctl
	 * 	TABLE_EXPORT_DATA.ldr
	 * 	TABLE_EXPORT_DATA8350c797-0166-1000-8e39-a9fe7a170510.ldr
	 * 	TABLE_EXPORT_DATA8350c794-0166-1000-8e37-a9fe7a170510.ldr
	 * changed:
	 * D:\testGenerateAll\Antioquia
	 * 	TABLE_EXPORT_DATA.ctl
	 * 	TABLE_EXPORT_DATA.bat
	 * 	186423.jpg
	 * 	286306.jpg
	 */
	public void oracleLdrToBatRenameImages(){
		final String LDR_FILE = "\\TABLE_EXPORT_DATA.ldr";
		selectedText = textArea.getSelectedText();
		if(selectedText=="" || selectedText==null){
			Macros.error(view, ERR_SELECT_TEXT);
			return;
		}
		selectedText += LDR_FILE;
		String newFile = selectedText.replace(".ldr", ".bat");
		String dirNewFile = "\"" + new File(newFile).getParent() + "\"";
		Buffer ldrBuff = jEdit.openFile(view,selectedText);

		replaceBuffer("\\|\\{EOL\\}", "\\n", "r");
		replaceBuffer(TRIM_LEFT, "ren ", "r");
		replaceBuffer("|\"", " ", "");
		replaceBuffer("\"", ".jpg", "");

		ldrBuff.save(view,null,true);
		jEdit._closeBuffer(view,ldrBuff);
		new MrFile().moveFile(selectedText, newFile);

		runCommand("cd " + dirNewFile);
		runCommand("\"" + newFile + "\"");
		waitForConsole();
	}
}
