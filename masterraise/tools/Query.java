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
import org.gjt.sp.jedit.Registers;
import org.gjt.sp.jedit.jEdit;

import masterraise.Text;
import masterraise.files.MrFile;

/**
 * Tools for Query
 * @author Richard Martínez 2015/04/21
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
		return new BeautyQuery();
	}

	public ConvertQuery getConvertQuery(){
		return new ConvertQuery();
	}

	/**
	 * Verify if query haf syntaxError
	 * @return boolean - if true has Syntax error
	 */
	private boolean hasSyntaxError(){
		boolean syntaxError = false;
		if(queryType.indexOf("CSV") < 0) {
			if(queryType.equals("")){
				syntaxError = true;
			}
			else if(queryType.equals("SELECT") && countOccurrences(selectedText, "\\bSELECT\\b.*\\bFROM\\b[ ]+\\w", "ir") == 0){
				syntaxError = true;
			}
			else if(queryType.equals("INSERT") && countOccurrences(selectedText, "\\bINSERT\\b[ \\t]+\\bINTO\\b[ \\t]+" + SQL_OBJECT + "[ \\t]+\\(.*\\)[ \\t]+VALUES[ \\t]*\\(.*\\)", "ir") == 0){
				syntaxError = true;
			}
			else if(queryType.equals("UPDATE") && countOccurrences(selectedText, "\\bUPDATE\\b[ \\t]+" + SQL_OBJECT + "[ \\t]+\\bSET[ \\t]+" + SQL_OBJECT + ".*=\\p{Print}", "ir") == 0){
				syntaxError = true;
			}
			if(syntaxError){
				msgSyntaxError = String.format(msgSyntaxError, new Object[] {queryType});
				return true;
			}
		}
		else{
			if(countOccurrences(selectedText, "^\\t", "ir") > 0){
				msgSyntaxError = "Remove Left Tabs";
				return true;
			}
			if(!new SpreadSheet().isMatchColumns(selectedText.replaceAll("\\A.*\n", ""))){
				msgSyntaxError = ERR_NOT_MATCH_COLUMN;
				return true;
			}

			if(queryType.equals("CSV_SELECT JOIN")){
				int numLinesWithTabs = countOccurrences(selectedText, "[^\\t]\\t.*", "r");
				if(numLinesWithTabs != 0 && countOccurrences(selectedText, "\\A" + SQL_OBJECT + "[ \\t]*$", "ir") > 0){
					msgSyntaxError = "Please Quit the table name, only must have data";
					return true;
				}
			}
			else{
				if(countOccurrences(selectedText, "\\t", "r") == 0){
					msgSyntaxError = ERR_INVALID_CSV;
					return true;
				}
				if(countOccurrences(selectedText, "\\A" + SQL_OBJECT + "[ \\t]*\\n", "ir") == 0){
					msgSyntaxError = String.format(msgSyntaxError, new Object[] {"CSV"}) + ", \nMust have table Name in first line";
					return true;
				}
			}
		}

		return syntaxError;
	}

	/**
	 * to format the special assigns
	 * @param ini format first assignment
	 * @param end format second assignment
	 */
	private void formatSpecialValues(boolean ini, boolean end){
		replaceBuffer("([()])([()])", "$1 $2", "r");

		if(ini){
			replaceBuffer(SQL_FUNCTION, "_0.replaceAll(\",\", \"" + COMA + "\")", "br");
			replaceBuffer(SQL_QUOTES_VALUES, "_0.replaceAll(\",\", \"" + COMA + "\")", "br");
			replaceBuffer(".", DOT, "");
			replaceBuffer(")", ROUND_BRACKET_RIGHT, "");
			replaceBuffer("(", ROUND_BRACKET_LEFT, "");
			replaceBuffer("SELECT" + ROUND_BRACKET_LEFT, "SELECT (", "i");

			if(queryType.equals("SELECT")){
				textArea.goToBufferStart(false);
				textArea.goToNextWord(false,false);
				textArea.goToEndOfWhiteSpace(true);

				replaceBuffer(SQL_RESERVED.replaceAll("(as|select)\\|", ""), "-$1-", "ir");
				replaceSelection(SQL_ALIAS, "$1", "ir");
				replaceSelection("AS", "", "iw");
				replaceSelection(TRIM_COMA, "\\t", "r");
			}
			else if(queryType.equals("UPDATE")){
				replaceBuffer(TRIM_COMA, "\\n\\t, ", "r");
			}
			else{
				replaceBuffer(TRIM_COMA, "\\t", "r");
			}
		}
		if(end){
			//apply assignment directly from fields to values like = 1 or = 'one' o = FUNCION(PARAM1, 'PARAM2')
			replaceBuffer("\\t", "\\n", "r");
			replaceBuffer("(" + SQL_OBJECT + ")(\\n.*).*", "_0.replaceAll(\"\\n\", \"	\")", "bir");
			replaceBuffer(DOT, ".", "");
			replaceBuffer(ROUND_BRACKET_RIGHT, ")", "");
			replaceBuffer(ROUND_BRACKET_LEFT, "(", "");
			replaceBuffer(COMA, ",", "");
			replaceBuffer("-" + SQL_RESERVED + "-", "$1", "ir");
		}

		replaceBuffer("([()])( +)([()])", "$1$3", "r");
		replaceBuffer("^[ \\t]*\\n", "", "r");
	}

	/**
	 * Format a query for easy read
	 *
	 * @example
	select distinct Prestar.dbo.autAutorizacionASP.autIDAutorizacion as AUTORIZACON, commDivision_2.divNom AS REGIONAL_IPS_ORIGEN
	, dos , '1, 2'
	--comentario
	, funcion , funcion(par1, 'par2')
	from         dbo.commDivision AS commDivision_3 inner join
								 dbo.commDivision with (nolock) on commDivision_3.divIDDivision = dbo.commDivision.divIDDivisionPadre inner join
								 dbo.redIPS AS Ips_Transcriptor on dbo.commDivision.divIDDivision = Ips_Transcriptor.ipsIDDivision inner join
								 Prestar.dbo.autAutorizacionASP 	ON Prestar.dbo.autAutorizacionASP.autIDIPSOrigen <> Prestar.dbo.autDetalleAutorizacionASP.autIDIPS inner join
								 Prestar.dbo.autDetalleAutorizacionASP with (nolock) on
								 Prestar.dbo.autAutorizacionASP.autIDAutorizacion = Prestar.dbo.autDetalleAutorizacionASP.autIDAutorizacion AND
								 Prestar.dbo.autAutorizacionASP.autIDIPSOrigen <> Prestar.dbo.autDetalleAutorizacionASP.autIDIPS inner join
								 dbo.redIPS AS IPS_Origen with (nolock) ON Prestar.dbo.autAutorizacionASP.autIDIPSOrigen = IPS_Origen.ipsIDIPS inner join
								 dbo.commTablaTablas with (nolock) ON Prestar.dbo.autDetalleAutorizacionASP.autEstado = dbo.commTablaTablas.tblCodElemento inner join
								 dbo.commDivision commDivision_2 with (nolock) inner join
								 dbo.commDivision AS commDivision_1 with (nolock) ON commDivision_2.divIDDivision = commDivision_1.divIDDivisionPadre on and  
								 IPS_Origen.ipsIDDivision = commDivision_1.divIDDivision and  Ips_Transcriptor.ipsIDIPS = Prestar.dbo.autDetalleAutorizacionASP.autIDIPS 
								 WHERE Prestar.dbo.autAutorizacionASP.autIDAutorizacion = 1 AND Prestar.dbo.autAutorizacionASP.autIDAutorizacion = 123 OR Prestar.dbo.autAutorizacionASP.autIDAutorizacion = 456

	to:
	SELECT DISTINCT Prestar.dbo.autAutorizacionASP.autIDAutorizacion, commDivision_2.divNom
		, dos, '1, 2'
		, funcion, funcion(par1, 'par2')
	FROM dbo.commDivision AS commDivision_3
	INNER JOIN dbo.commDivision WITH(NOLOCK) ON commDivision_3.divIDDivision = dbo.commDivision.divIDDivisionPadre
	INNER JOIN dbo.redIPS AS Ips_Transcriptor ON dbo.commDivision.divIDDivision = Ips_Transcriptor.ipsIDDivision
	INNER JOIN Prestar.dbo.autAutorizacionASP ON Prestar.dbo.autAutorizacionASP.autIDIPSOrigen <> Prestar.dbo.autDetalleAutorizacionASP.autIDIPS
	INNER JOIN Prestar.dbo.autDetalleAutorizacionASP WITH(NOLOCK) ON Prestar.dbo.autAutorizacionASP.autIDAutorizacion = Prestar.dbo.autDetalleAutorizacionASP.autIDAutorizacion
		AND Prestar.dbo.autAutorizacionASP.autIDIPSOrigen <> Prestar.dbo.autDetalleAutorizacionASP.autIDIPS
	INNER JOIN dbo.redIPS AS IPS_Origen WITH(NOLOCK) ON Prestar.dbo.autAutorizacionASP.autIDIPSOrigen = IPS_Origen.ipsIDIPS
	INNER JOIN dbo.commTablaTablas WITH(NOLOCK) ON Prestar.dbo.autDetalleAutorizacionASP.autEstado = dbo.commTablaTablas.tblCodElemento
	INNER JOIN dbo.commDivision commDivision_2 WITH(NOLOCK)
	INNER JOIN dbo.commDivision AS commDivision_1 WITH(NOLOCK) ON commDivision_2.divIDDivision = commDivision_1.divIDDivisionPadre ON
		AND IPS_Origen.ipsIDDivision = commDivision_1.divIDDivision
		AND Ips_Transcriptor.ipsIDIPS = Prestar.dbo.autDetalleAutorizacionASP.autIDIPS
	WHERE Prestar.dbo.autAutorizacionASP.autIDAutorizacion = 1
		AND Prestar.dbo.autAutorizacionASP.autIDAutorizacion = 123
		OR Prestar.dbo.autAutorizacionASP.autIDAutorizacion = 456
	 */
	public class BeautyQuery{
		private String opts = "";
		private JCheckBox chkUcase;
		private JCheckBox chkWithNolock;
		private JCheckBox chkIndent;

		private BeautyQuery() {
			selectedText = iniSelectedText();
		}

		private void setResult(){
			opts = "";
			if(chkUcase.isSelected()) opts += "u";
			if(chkWithNolock.isSelected()) opts += "l";
			if(chkIndent.isSelected()) opts += "i";
		}

		public void setOpcs(String opcs) {
			this.opts = opcs;
		}

		public void showGui(){
			String bufferText = textArea.getBuffer().getText().toUpperCase();
			JPanel checkBoxPanel = new JPanel(new BorderLayout());
			chkUcase = new JCheckBox("UpperCase Reserved", true);
			chkWithNolock = new JCheckBox("set WithNolock", false);
			chkIndent = new JCheckBox("Indent", true);

			checkBoxPanel.add(chkUcase, BorderLayout.NORTH);
			if(bufferText.indexOf("SELECT") != -1 && bufferText.indexOf("FROM") != -1){
				checkBoxPanel.add(chkWithNolock, BorderLayout.CENTER);
			}
			checkBoxPanel.add(chkIndent, BorderLayout.SOUTH);

			KeyAdapter ka = new KeyAdapter(){
				public void keyReleased(KeyEvent evt){
					if(evt.getKeyCode() == KeyEvent.VK_ESCAPE){
						dialog.dispose();
					}
				}
			};

			chkUcase.addKeyListener(ka);
			chkWithNolock.addKeyListener(ka);
			chkIndent.addKeyListener(ka);

			btnOk = new JButton("Beautify");

			JPanel buttonPanel = new JPanel();
			buttonPanel.add(btnOk, BorderLayout.WEST);
			buttonPanel.add(btnCancel, BorderLayout.EAST);

			btnOk.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent evt){
					setResult();
					processText();
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
			content.setPreferredSize(new Dimension(200, 130));
			content.add(checkBoxPanel, BorderLayout.NORTH);
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
		 */
		public void beautyQuery(String opts){
			//quita todos los comentarios y tabulador
			replaceBuffer(COMMENTS, "", "ir");

			//format decimal
			replaceBuffer(SQL_NUMBER, "_0.replaceAll(\",\", \".\")", "br");

			textArea.selectAll();
			textArea.joinLines();
			replaceBuffer("\\t+", " ", "r");

			if(selectedText.toUpperCase().indexOf("FROM") >= 0 && selectedText.toUpperCase().indexOf("INSERT") >= 0){
				queryType = "INSERT SELECT";
			}
			//at case query SELECT, optimize for only set with segment for FROM and don't delay to checking syntax
			else if(selectedText.toUpperCase().indexOf("FROM") >= 0){
				queryType = "SELECT";
			}
			else if(selectedText.toUpperCase().indexOf("INSERT") >= 0){
				queryType = "INSERT";
			}
			else if(selectedText.toUpperCase().indexOf("UPDATE") >= 0){
				queryType = "UPDATE";
			}
			if(hasSyntaxError()){
				Macros.error(view, msgSyntaxError);
				return;
			}

			replaceBuffer(SQL_RESERVED_LINE, "\\n$1", "ir");
			replaceBuffer("([^\\(])('[ \\t]*)(\\d{2}/\\d{2}/\\d{4})('[ \\t]*)", "TO_DATE('$3', 'dd/MM/yyyy')", "r");
			replaceBuffer("([^\\(])('[ \\t]*)(\\d{4}/\\d{2}/\\d{2})('[ \\t]*)", "TO_DATE('$3', 'yyyy/MM/dd')", "r");

			//set reserved word to Uppercase
			if(opts.indexOf('u') >= 0){
				replaceBuffer(SQL_RESERVED, "_1.toUpperCase()", "br");
				if(opts.equals("u")){
					return;
				}
			}
			if(queryType.equals("UPDATE")){
				formatSpecialValues(true, true);
			}
			else{
				replaceBuffer("(\\w+)([ \\t]+)(\\()", "$1(", "r");
				replaceBuffer("(\\()([ \\t])|([ \\t])(\\))", "$1$4", "r");

				// format wright query at case is only one line
				if(!queryType.equals("CSV")){
					replaceBuffer("\\[|\\]", "", "r");

					//modify the temp tables, for recovery later
					replaceBuffer("#", SHARP, "");
					if(queryType.equals("INSERT")){
						replaceBuffer("(\\bINSERT\\b \\bINTO\\b " + SQL_OBJECT + ")(\\()", "$1\\n$3", "ir");
						replaceBuffer("\\bVALUES\\b", "\\n$0", "ir");
					}
					else{
						formatSpecialValues(true, true);
					}
					if(queryType.equals("SELECT")){
						textArea.goToEndOfWhiteSpace(false);
						textArea.goToBufferStart(true);
						replaceSelection("\\t", ", ", "r");
						replaceSelection("^", "\\t, ", "r");
						replaceSelection("\\t, SELECT", "SELECT", "ir");

						replaceBuffer("(\\bWHERE\\b.*)((.*\\n)+)(.*\\bORDER\\b)", "_1 + _2.replaceAll(\"(?m)^\",\"\\t\") + _4", "bir");

						if(opts.indexOf('l') >= 0){
							replaceBuffer("(with)*[ \\(]*nolock[ \\)]*", "", "ir");
							replaceBuffer("\\b(from|join)\\b[ \\t\\n]*(\\w+\\.*)+[ \\t]*(\\bas\\b[ \\t]+)*\\w+", "$0 WITH(NOLOCK) ", "ir");
						}
						replaceBuffer("\\bFROM\\b.*", "_0.replaceAll(\",\", \"\\n\\t,\")", "bir");
					}
					if(queryType.equals("INSERT SELECT")){
						replaceBuffer("(\\)[ \\t])(SELECT)", ")\\n$2", "ir");
						findBuffer("(SELECT.*\\n)(^.*\\n)+(FROM)", "air");
						replaceSelection("^", ", ", "r");
						replaceSelection("(, )(" + SQL_RESERVED + ")", "$2", "ir");
					}

					//recupera las tablas temporales
					replaceBuffer(SHARP, "#", "r");
				}
			}

			replaceBuffer("([ \\t]+$|^" + TRIM_COMA + "\\n)", "", "r");
			replaceBuffer("(\\w)( )(, \\w)", "$1$3", "r");
			replaceBuffer("\\b(AND|OR)\\b", "\\t$0", "ir");
			replaceBuffer(SQL_DOUBLE_SPACES, " ", "r");
			replaceBuffer("^,", "\\t,", "r");
			replaceBuffer("[ \\t]*;[ \\t]*", ";", "r");
		}

		public void processText(){
			if(opts == ""){
				Macros.error(view, "You have to Check at least one Option");
				return;
			}

			bfTmp = openTempBuffer();
			beautyQuery(opts);
			closeTempBuffer(bfTmp);
			dialog.dispose();
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
		 * Constructor only for jEdit use 
		 */
		public ConvertQuery() { }

		public String getResult(){
			return convertion;
		}

		/**
		 * Set name table from query
		 */
		private void setNameTable(){
			String regExQuery = "";
			String deleteRegExpToReplace = "";
			switch(query1){
			case "INSERT":
				regExQuery = "\\bINSERT\\b[ \\t]\\bINTO\\b " + SQL_OBJECT;
				deleteRegExpToReplace = "^\\w+ \\w+ ";
				break;
			case "SELECT":
				regExQuery = "\\bFROM\\b[ \\t]+" + SQL_OBJECT;
				deleteRegExpToReplace = "^\\w+[ \\t]+";
				break;
			case "UPDATE":
				regExQuery = "UPDATE " + SQL_OBJECT;
				deleteRegExpToReplace = "^\\w+ ";
				break;
			case "CSV":
				if(!query2.equals("SELECT JOIN")){
					regExQuery = "\\A" + SQL_OBJECT + "\\n";
					deleteRegExpToReplace = "";
				}
				break;
			}

			if(!query2.equals("SELECT JOIN")){
				findBuffer(regExQuery, "air");
				nameTable = textArea.getSelectedText().replaceAll(deleteRegExpToReplace, "");
				textArea.delete();
			}
		}

		private void startFormatQuery(){	
			//format decimal
			replaceBuffer(SQL_NUMBER, "_0.replaceAll(\",\", \".\")", "br");

			replaceBuffer(COMMENTS, "", "ir");

			if(!query1.equals("CSV")){
				textArea.selectAll();
				textArea.joinLines();
			}
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
			bfTmp = openTempBuffer();
			String fowardQuery = "";
			int lastSemiColon = replaceBuffer(SQL_LAST_SEMICOLON, "", "ar");

			convertion = query1 + "_" + query2;
			queryType = query2.equals("SELECT JOIN") ? convertion : query1;

			startFormatQuery();

			selectedText = iniSelectedText();
			if(hasSyntaxError()){
				jEdit._closeBuffer(view,(Buffer) bfTmp);
				Macros.error(view, msgSyntaxError);
				return msgSyntaxError;
			}

			if(query1.equals("SELECT") || query1.equals("UPDATE")){
				replaceBuffer("ORDER[ \\t]BY.*(\\n.*)*", "", "ir");

				if(findBuffer("WHERE.*", "air")){
					fowardQuery = textArea.getSelectedText();
					textArea.delete();
				}
			}

			setNameTable();

			switch(convertion){
			case "CSV_UPDATE":
				replaceBuffer("\\t", " = ", "r");
				replaceBuffer("^", "\\t, ", "r");
				replaceBuffer("\\A\\t,", "UPDATE " + nameTable + "SET", "r");
				break;
			case "INSERT_SELECT": case "UPDATE_SELECT":
				convertQuery("INSERT", "CSV");
				replaceBuffer(csvPrefix, "", "r");
				replaceBuffer("\t|^", ", ", "r");
				replaceBuffer("\\A,", "SELECT", "r");
				replaceBuffer("\\z", "\nFROM " + nameTable, "r");
				break;
			case "SELECT_INSERT": case "UPDATE_INSERT":
				convertQuery("UPDATE", "CSV");
				replaceBuffer(csvPrefix, "", "");
				nameTable += "\n";
				convertQuery("CSV", "INSERT");
				if(convertion.equals("SELECT_INSERT")){
					replaceBuffer("(SELECT", "(", "");
				}
				break;
				/*
				TODO:VERIFICAR QUE EN LA POSICION EN LA QUE SE ENCUENTRE EL CURSOR SE PUEDA SELECCIONAR SIN DAR VUELTA COMO
				textArea.goToBufferStart(false);
				textArea.goToEndOfWhiteSpace(false);
				textArea.goToBufferEnd(true);

				Y TOMARLO como para menos lineas textArea.goToBufferEnd(true);
				 */
			default:
				convertQuery(query1, query2);
				break;
			}

			if(!fowardQuery.equals("") && (query2.equals("UPDATE") || query2.equals("SELECT"))){
				replaceBuffer("\\z", "\\n" + fowardQuery, "r");
			}

			replaceBuffer("(^SET|SELECT.*\\n)((.*\\n.*)+)", "_1 + _2.replaceAll(\"(?m)^,\", \"\\t,\")", "br");
			replaceBuffer(TRIM_RIGHT, "", "r");
			//put semicolon to end of query if had it
			if(!query2.equals("CSV") && lastSemiColon > 0){
				replaceBuffer("\\z", ";", "r");
			}

			replaceBuffer("(\\p{Graph})(,)(\\p{Graph})", "$1, $3", "r");

			String convertedQuery = bfTmp.getText();
			closeTempBuffer(bfTmp);
			return convertedQuery;
		}

		/**
		 * Convert a query to another query
		 * @param query1 first query query to convert
		 * @param query2 final converted query
		 */
		public void convertQuery(String query1, String query2){
			SpreadSheet sp = new SpreadSheet();
			switch(query1){
			case "INSERT":
				if(!query2.equals("SELECT")){
					replaceBuffer("\\)[ \\t]*\\bVALUES\\b[ \\t]*\\(", "\\n", "ir");
					replaceBuffer(TRIM_UP + "\\(", "", "r");
					replaceBuffer(TRIM_COMA, ",", "r");
					replaceBuffer("\\)" + TRIM_DOWN, "", "r");
					formatSpecialValues(true, false);
					if(!convertion.equals("UPDATE_SELECT")){
						sp.transposeMatrix();
					}
					formatSpecialValues(false, true);
					replaceBuffer("^", ", ", "r");
				}
				break;
			case "SELECT":
				replaceBuffer("^[ \\t]*\\b(SELECT|DISTINCT)\\b[ \\t]*", "", "ir");
				formatSpecialValues(true, true);
				replaceBuffer("^", ", ", "r");
				break;
			case "UPDATE":
				findBuffer("\\bWHERE\\b.*", "air");
				Registers.cut(textArea,'$');
				formatSpecialValues(true, true);
				break;
			case "CSV":
				replaceBuffer("\"", "", "");
				break;
			}

			if(query2.equals("SELECT JOIN")){
				int numLines = countOccurrences(view.getTextArea().getText(), "\n", "r") + 1;
				boolean hasTwoCols = findBuffer("\\A.*\\t", "ar");

				// encloses quote for blank spaces
				replaceBuffer("^\\t", "''\\t", "ir");
				replaceBuffer("\\t$", "\\t''", "ir");
				replaceBuffer("\\t[ ]*\\t", "\\t''\\t", "ir");

				// encloses quote for values except functions
				textArea.goToBufferStart(false);
				textArea.goToEndOfWhiteSpace(false);
				textArea.goToBufferEnd(true);
				replaceSelection("\\p{Print}+", "'$0'", "r");
				replaceSelection("'{2,}", "'", "r");
				replaceBuffer("(')(" + SQL_FUNC_VALUES  + ")(')", "$2", "r");

				//format two first lines for "value AS alias"
				if(numLines == 2){
					textArea.goToBufferEnd(false);
					textArea.insertEnterAndIndent();
				}
				textArea.goToBufferStart(false);
				transposeLines(textArea);
				textArea.goToBufferEnd(true);
				String tmpQuery = textArea.getSelectedText();
				textArea.delete();
				if(hasTwoCols){
					sp.transposeMatrix();
				}
				else{
					replaceBuffer("\\n", "\\t", "r");
				}

				// format SELECT
				replaceBuffer("(\\p{Print}+)(\\t)(\\p{Print}+)", "$1 $3", "r");
				replaceBuffer("\\n", ", ", "r");
				replaceBuffer("\\A", "SELECT ", "r");
				tmpQuery = tmpQuery.replaceAll("\t", ", ");
				tmpQuery = tmpQuery.replaceAll("^\n", "");
				tmpQuery = tmpQuery.replaceAll("(?m)^", "UNION SELECT ");
				replaceBuffer("\\z", "\\n" + tmpQuery, "r");
				replaceBuffer("$", " FROM DUAL", "r");
				replaceBuffer("'+[ \\t]*(" + SQL_FUNC_VALUES + "|" + SQL_RESERVED_VALUES + ")+[ \\t]*'", "$1", "r");
				return;
			}

			switch(query2){
			case "INSERT":
				if(!sp.transposeMatrix()) return;

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
				replaceBuffer("\\A, ", "UPDATE " + nameTable + "\\nSET ", "r");
				break;
			case "CSV":
				csvPrefix = String.format(CSV_PREFIX, new Object[] { nameTable });
				replaceBuffer(SQL_SET, "", "ir");
				replaceBuffer("\\A", csvPrefix, "r");
				replaceBuffer(BLANK_LINE, "", "r");
				replaceBuffer("^" + TRIM_COMA, "", "r");
				replaceBuffer(TRIM_RIGHT, "", "r");
				replaceBuffer("[ \t]*=[ \t]*", "\t", "r");
				return;
			}
			this.query1 = query2;
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
	public void queryToLanguage(){
		Buffer bfTmp = openTempBuffer();

		//TODO: REEMPLAZAR todo textArea.getSelectedText() por selectedText
		textArea.setText(firsUpperCase(selectedText, '_'));
		replaceBuffer("_", "", "");
		replaceBuffer("(^)(\\w)(.*.)($)", "\"private String \" + _2.toLowerCase() + _3 + \" = \\\"\\\";\"", "br");
		selectedText = textArea.getText() + "\n\n";

		new Java().genGetSet();

		replaceBuffer("\\A", selectedText, "r");
		closeTempBuffer(bfTmp);
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
		Buffer bfTmp = openTempBuffer();
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

		closeTempBuffer(bfTmp);
	}

	/**
	 * Convert any Query to SqlLite
	 */
	public void convertToSqlLite(){
		String t = iniSelectedText();
		t=t.replaceAll("(?m)(\"| ENABLE| BYTE)", "");
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
		Buffer bfTmp = openTempBuffer();

		//format query
		replaceBuffer(COMMENTS, "", "ir");
		replaceBuffer("values[\\( ]+", "values(", "ir");

		textArea.selectAll();
		textArea.joinLines();

		if(!findBuffer("^\\(.*\\bvalues\\b[ \\t]*\\(.*\\)$", "air")){
			jEdit._closeBuffer(view,(Buffer) bfTmp);
			Macros.error(view, String.format(msgSyntaxError, new Object[] { "Sql Server Variables SP" }));
			return "";
		}

		//separate in two lines for variables and values
		replaceBuffer("\\)[  \\t]*values[  \\t]*\\(", ")\\nvalues(", "ir");

		//identify if first character is At char (@)
		findBuffer("values[ \\t]*\\(", "air");
		textArea.goToNextCharacter(false);
		textArea.goToNextCharacter(true);
		String strAt = textArea.getSelectedText();

		//remove assigments for first line
		textArea.goToBufferStart(false);
		textArea.selectLine();
		replaceSelection("=[ ]*([ ]{0,}\\w+|'')", "", "ir");

		replaceBuffer(SQL_IN_VALUES, "_1 + _2 + _3.replace(\",\", \"" + COMA + "\")", "abr");

		//check number assigments and values is match
		textArea.goToBufferStart(false);
		textArea.selectLine();
		int numComaAsig = countOccurrences(textArea.getSelectedText(), ",", "");

		textArea.goToBufferEnd(false);
		textArea.selectLine();
		int numComaValue = countOccurrences(textArea.getSelectedText(), ",", "");

		if(numComaAsig!=numComaValue){
			jEdit._closeBuffer(view,(Buffer) bfTmp);
			Macros.error(view, "The number rows from this column don't match with anothers columns");
			return "";
		}

		//in case the second line has at char (@), only assign variables
		if(strAt.equals("@")){
			textArea.goToBufferEnd(false);
			textArea.goToStartOfWhiteSpace(true);

			replaceSelection("values(", "SET ", "i");
			replaceSelection(", ", " SET ", "");
			replaceSelection("[)]$", "", "r");
			replaceBuffer("^\\(|\\)$", "", "r");
			replaceBuffer("(SET|,)", "\\n$1", "ir");
		}
		//assign each values for each variable
		else{
			textArea.goToBufferStart(false);
			textArea.selectLine();
			String firstLine = textArea.getSelectedText().replaceAll("(^\\(|\\)$)", "") + "\n\n";
			replaceSelection(SQL_ALIAS, "$1", "ir");
			replaceSelection("[ \\t]+\\(\\w+\\)", "", "r");

			replaceBuffer("\\A", "INSERT INTO garbage", "r");
			textArea.selectAll();
			textArea.joinLines();
			new ConvertQuery().convertQuery("INSERT", "CSV");

			replaceBuffer("Structure table:\\nFIELD	VALUE\\nINSERT INTO garbage\\(", "", "r");
			replaceBuffer("\t", " = ", "r");
			replaceBuffer("^", "SET ", "r");
			replaceBuffer("\\A", firstLine.replaceAll("[ \t]*,[ \\t]*", "\n, "), "r");
		}

		replaceBuffer("\\A", "DECLARE ", "r");

		//recovery the comas when value have in(v1,v2,v3,v4)
		replaceBuffer(COMA, ",", "r");

		//revove default variables
		replaceBuffer("^set.*default[ ]*\\n*", "", "ir");

		replaceBuffer(TRIM, "", "r");
		replaceBuffer(SQL_RESERVED, "_1.toUpperCase()", "br");

		//remove unused reserved words
		replaceBuffer("[ ]*\\bOUTPUT\\b[ ]*", "", "ir");

		closeTempBuffer(bfTmp);

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

		Buffer bfTmp = openTempBuffer();

		replaceBuffer(COMMENTS, "", "ir"); 

		//don't take primary keys from temp table like if this would a normal table, example: xll#SPDI
		replaceBuffer("\\w+#\\b\\w+\\b", "\\n$0\\n", "ir");
		replaceBuffer("(\\w+)(#)(\\b\\w+\\b)", "$1__$3", "ir");
		replaceBuffer("#{1,2}\\b[^ 0-9\\(]+\\w+\\b", "\\n$0\\n", "ir");
		replaceBuffer("^.*#[!\"$%&'\\(\\)*+,-./:;=>?@\\[\\\\\\]^_`{|}~].*\\n", "", "ir");
		// replace all lines that doesn't begin for temp table (#temporal)
		replaceBuffer("^[^#].*(\\n|\\z)", "", "ir");
		// remove possibles extrange chars
		replaceBuffer("[!\"$%&'\\(\\)*+,-./:;=>?@\\[\\\\\\]^`{|}~]", "", "r");
		replaceBuffer("(^[ \\t]+|[ \\t]+$)", "", "r");

		textArea.selectAll();
		textArea.toUpperCase();
		deleteDuplicates(textArea);

		replaceBuffer("^", "DROP TABLE ", "ir");
		replaceBuffer("^DROP TABLE ##", "--DROP TABLE ##", "r");
		sortLines(textArea);

		replaceBuffer("\\z", "\n\n" + previousText, "r");
		closeTempBuffer(bfTmp);

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
		replaceBuffer("^[ \\t]+", "ren ", "r");
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
