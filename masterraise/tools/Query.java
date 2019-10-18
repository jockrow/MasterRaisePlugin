/************************************************/
/*      @author Richard Martínez 2015/04/21     */
/************************************************/

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

import masterraise.Edit;
import masterraise.files.MrFile;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EditBus;
import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.Macros;
import org.gjt.sp.jedit.Registers;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.msg.PositionChanging;
import org.gjt.sp.jedit.textarea.TextArea;

public class Query extends Edit{
	private final static String ROUND_BRACKET_LEFT  = "___";
	private final static String ROUND_BRACKET_RIGHT = "_____";
	private final static String COMA                = "__";
	private final static String SHARP               = "____";
	private final static String DOT                 = "_______";

	private final static String TRIM = "^[ \\t]+|[ \\t]+$";
	private final static String TRIM_COMA = "[ \\t]*,[ \\t]*";

	private final static String REGEXP_SQL_OBJECT = "(\\w+\\.){0,}+\\w+";
	private final static String REGEXP_SQL_FUNC_VALUES = "\\w+\\(([' \\t]*[\\w/]+[' \\t,.]*)+\\)";
	private final static String REGEXP_SQL_QUOTES_VALUES = "'([ \\t,]*\\w+)+'";
	private final static String REGEXP_SQL_ALIAS = "([a-z] )((AS ){0,1}\\w+)";
	private final static String REGEXP_SQL_RESERVED = "\\b(insert|into|values|update|set|as|not|like|in|inner|right|left|join|on|select|distinct|convert|case|when|then|else|end|sum|count|max|min|datetime|smallint|int|varchar|dateadd|isnull|null|from|where|and|or|with|nolock|union|group by|order by|having|desc|cast|concat|substr)\\b";
	private final static String REGEXP_SQL_COMMENT = "[ \\t]*--.*|/\\*([\\n\\t ]*([#\\w áéíóú]+\\n)+[\\n\\t ]*)+\\*/";
	private final static String REGEXP_SQL_DOUBLE_SPACES = "[ ]{2,}";
	private final static String REGEXP_SQL_RESERVED_LINE = "\\b(SET|FROM|WHERE|AND|OR|ORDER|INNER|RIGHT|LEFT)\\b";
	private final static String REGEXP_SQL_LAST_SEMICOLON = "[\\t ]*;+[\\t ]*$(\\n)*\\z";
	private final static String REGEXP_SQL_LAST_ROUND_BRACKET = "\\)" + TRIM_DOWN;
	private final static String REGEXP_SQL_FUNCTION = REGEXP_SQL_OBJECT + "[ \\t]*\\([ \\t]*[ \\(:\\d\\w',\\./-]+\\)+";
	private final static String REGEXP_SQL_RETURN_CARRIAGE = "[ \\t]*(WHERE|AND)[ \\t]*";
	private final static String REGEXP_SQL_RESERVED_VALUES = "\\b(SYSDATE)\\b";
	private final static String REGEXP_SQL_SET = "[ \\t]*SET[ \\t]*";
	private final static String REGEXP_SQL_NUMBER = "'\\d+,\\d+'";

	private final View view = jEdit.getActiveView();
	private final TextArea textArea = view.getTextArea();
	private final EditPane editPane = view.getEditPane();

	JEditBuffer bfTmp = null;
	private String queryType = "";
	private JDialog dialog = new JDialog(view, "Convert Query", true);
	private JPanel content = new JPanel(new BorderLayout());
	private JPanel buttonPanel = new JPanel();
	private JButton btnOk = new JButton("Convert");
	private JButton btnCancel = new JButton("Cancel");
	private String selectedText = "";

	public BeautyQuery getBeautyQuery(){
		return new BeautyQuery();
	}

	public ConvertQuery getConvertQuery(){
		return new ConvertQuery();
	}

	public String getQueryType(){
		return queryType;
	}

	public void setQueryType(String queryType){
		this.queryType = queryType;
	}

	/**
	 * Verify if query haf syntaxError
	 * @return boolean - if true has Syntax error
	 */
	private boolean hasSyntaxError(){
		String msgSyntaxError = "Syntax Error in %s Query";
		boolean syntaxError = false;
		int iniLine = 0;

		if(queryType.equals("SELECT") && !findBuffer("\\bSELECT\\b.*\\bFROM\\b[ ]+\\w", "air")){
			syntaxError = true;
		}
		else if(queryType.equals("INSERT") && !findBuffer("\\bINSERT\\b[ \\t]+\\bINTO\\b[ \\t]+" + REGEXP_SQL_OBJECT + "[ \\t]+\\(.*\\)[ \\t]+VALUES[ \\t]*\\(.*\\)", "air")){
			syntaxError = true;
		}
		else if(queryType.equals("UPDATE") && !findBuffer("\\bUPDATE\\b[ \\t]+" + REGEXP_SQL_OBJECT + "[ \\t]+\\bSET[ \\t]+" + REGEXP_SQL_OBJECT + ".*=\\p{Print}", "air")){
			syntaxError = true;
		}
		else if(queryType.equals("SP") && !findBuffer("\\A[\\n \\t]*\\((.*(\\n)*^[ \\t]*)*(\\n)*[\\t ]*values[\\t ]*\\((.*(\\n)*^[ \\t]*)*.*" + REGEXP_SQL_LAST_ROUND_BRACKET, "air")){
			syntaxError = true;
		}
		if(syntaxError){
			Macros.message(view, String.format(msgSyntaxError, new Object[] {queryType}));
			return true;
		}

		if(!queryType.equals("CSV_SELECT") && queryType.indexOf("CSV") >=0 && !findBuffer("\\t", "ar")){
			Macros.message(view, "Text must have Tabs");
			return true;
		}

		if(queryType.equals("CSV_SELECT")){
			int numLinesWithTabs = replaceBuffer("[^\\t]\\t.*", "$0", "r");

			if(numLinesWithTabs != 0 && findBuffer("\\A" + REGEXP_SQL_OBJECT + "[ \\t]*$", "ar")){
				Macros.message(view, "Please Quit the table name, only must have data");
				return true;
			}
		}
		else if(queryType.equals("CSV_")){
			iniLine = 2;
		}
		if(iniLine > 0 && !new SpreadSheet().isMatchColumns(iniLine)){
			Macros.message(view, "The number columns is not Match");
			return true;
		}
		if(queryType.equals("CSV_") && !findBuffer("\\A" + REGEXP_SQL_OBJECT + "[ \\t]*$", "ar")){
			Macros.message(view, String.format(msgSyntaxError, new Object[] {"CSV"}) + ", \nMust have table Name in first line");
			return true;
		}

		return syntaxError;
	}

	/**
	 * Return name table from query
	 * @param String regExQuery: Regular Expression to find query
	 * @param String deleteRegExpToReplace: Regular Expression to delete for query
	 * @return String - name for table
	 */
	private String getNameTable(String regExQuery, String deleteRegExpToReplace){
		findBuffer(regExQuery, "air");
		String tableName = textArea.getSelectedText().replaceAll(deleteRegExpToReplace, "");
		textArea.delete();
		return tableName;
	}

	/**
	 * to format the special assigns
	 * @param boolean ini: si quiere que se formatee la primera asignación
	 * @param boolean end: si quiere que se formatee la segunda asignación
	 */
	private void formatSpecialValues(boolean ini, boolean end){
		replaceBuffer("([()])([()])", "$1 $2", "r");

		if(ini){
			replaceBuffer(REGEXP_SQL_FUNCTION, "_0.replaceAll(\",\", \"" + COMA + "\")", "br");
			replaceBuffer(REGEXP_SQL_QUOTES_VALUES, "_0.replaceAll(\",\", \"" + COMA + "\")", "br");
			replaceBuffer(".", DOT, "");
			replaceBuffer(")", ROUND_BRACKET_RIGHT, "");
			replaceBuffer("(", ROUND_BRACKET_LEFT, "");
			replaceBuffer("SELECT" + ROUND_BRACKET_LEFT, "SELECT (", "i");

			if(queryType.equals("SELECT")){
				textArea.goToBufferStart(false);
				textArea.goToNextWord(false,false);
				textArea.goToEndOfWhiteSpace(true);

				replaceBuffer(REGEXP_SQL_RESERVED.replaceAll("(as|select)\\|", ""), "-$1-", "ir");
				replaceSelection(REGEXP_SQL_ALIAS, "$1", "ir");
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
			//Se realiza la asignación directa de los campos para valores como = 1 o = 'uno' o = FUNCION(PARAM1, 'PARAM2')
			replaceBuffer("\\t", "\\n", "r");
			replaceBuffer("(" + REGEXP_SQL_OBJECT + ")(\\n.*).*", "_0.replaceAll(\"\\n\", \"	\")", "bir");

			replaceBuffer(DOT, ".", "");
			replaceBuffer(ROUND_BRACKET_RIGHT, ")", "");
			replaceBuffer(ROUND_BRACKET_LEFT, "(", "");
			replaceBuffer(COMA, ",", "");
			replaceBuffer("-" + REGEXP_SQL_RESERVED + "-", "$1", "ir");
		}

		replaceBuffer("([()])( +)([()])", "$1$3", "r");
		replaceBuffer("^[ \\t]*\\n", "", "r");
	}

	/**
	 * Formatea el query para que se vea presentable
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

	POR:
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
		private String opcs = "";
		private JCheckBox chkUcase;
		private JCheckBox chkWithNolock;
		private JCheckBox chkIndent;

		public String getResult(){
			return opcs;
		}

		private void setResult(){
			opcs = "";
			if(chkUcase.isSelected()) opcs += "u";
			if(chkWithNolock.isSelected()) opcs += "l";
			if(chkIndent.isSelected()) opcs += "i";
		}

		public void setOpcs(String opcs) {
			this.opcs = opcs;
		}

		public void showGui(){
			chkUcase = new JCheckBox("UpperCase Reserved", true);
			chkWithNolock = new JCheckBox("set WithNolock", false);
			chkIndent = new JCheckBox("Indent", true);
			String bufferText = textArea.getBuffer().getText().toUpperCase();

			JPanel checkBoxPanel = new JPanel(new BorderLayout());

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
		 * @param String opcs: u: set upper case reserved words, l: set WITH(NOLOCK) when query is SELECT, i: Indent query, f:if this function invoked from beauty Query Macro
		 * @return String - return a message
		 */
		public void beautyQuery(String opcs){
			//quita todos los comentarios y tabulador
			//TODO:Revisar cuando venga una sola línea
			replaceBuffer(REGEXP_SQL_COMMENT, "", "ir");

			textArea.selectAll();
			textArea.joinLines();
			replaceBuffer("\\t+", " ", "r");

			//		String selectedText = textArea == null ? "" : textArea.getSelectedText();
			selectedText = textArea.getSelectedText();

			if(selectedText.toUpperCase().indexOf("FROM") >= 0 && selectedText.toUpperCase().indexOf("INSERT") >= 0){
				queryType = "INSERT SELECT";
			}
			//En el caso de query SELECT Optimizo para que quede sólo con el segmento del FROM y no se demore al verificar la síntaxis
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
				return;
			}

			replaceBuffer(REGEXP_SQL_RESERVED_LINE, "\\n$1", "ir");
			replaceBuffer("([^\\(])('[ \\t]*)(\\d{2}/\\d{2}/\\d{4})('[ \\t]*)", "TO_DATE('$3', 'dd/MM/yyyy')", "r");
			replaceBuffer("([^\\(])('[ \\t]*)(\\d{4}/\\d{2}/\\d{2})('[ \\t]*)", "TO_DATE('$3', 'yyyy/MM/dd')", "r");

			// Pongo las palabras reservadas en mayúsculas
			if(opcs.indexOf('u') >= 0){
				replaceBuffer(REGEXP_SQL_RESERVED, "_1.toUpperCase()", "br");
				if(opcs.equals("u")){
					return;
				}
			}
			if(queryType.equals("UPDATE")){
				formatSpecialValues(true, true);
			}
			else{
				replaceBuffer("(\\w+)([ \\t]+)(\\()", "$1(", "r");
				replaceBuffer("(\\()([ \\t])|([ \\t])(\\))", "$1$4", "r");

				// formateo correctamente el query en el caso que esté en una sola línea
				if(!queryType.equals("CSV")){
					replaceBuffer("\\[|\\]", "", "r");

					// quita los espacios al principio y al final
					replaceBuffer(TRIM_UP, "", "r");
					replaceBuffer(TRIM_DOWN, "", "r");

					//modifica las tablas temporales, para recuperarlas después
					replaceBuffer("#", SHARP, "");
					if(queryType.equals("INSERT")){
						replaceBuffer("(\\bINSERT\\b \\bINTO\\b " + REGEXP_SQL_OBJECT + ")(\\()", "$1\\n$3", "ir");
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

						if(opcs.indexOf('l') >= 0){
							replaceBuffer("(with)*[ \\(]*nolock[ \\)]*", "", "ir");
							replaceBuffer("\\b(from|join)\\b[ \\t\\n]*(\\w+\\.*)+[ \\t]*(\\bas\\b[ \\t]+)*\\w+", "$0 WITH(NOLOCK) ", "ir");
						}
						replaceBuffer("\\bFROM\\b.*", "_0.replaceAll(\",\", \"\\n\\t,\")", "bir");
					}
					if(queryType.equals("INSERT SELECT")){
						replaceBuffer("(\\)[ \\t])(SELECT)", ")\\n$2", "ir");
						findBuffer("(SELECT.*\\n)(^.*\\n)+(FROM)", "air");
						replaceSelection("^", ", ", "r");
						replaceSelection("(, )(" + REGEXP_SQL_RESERVED + ")", "$2", "ir");
					}

					//recupera las tablas temporales
					replaceBuffer(SHARP, "#", "r");
				}
			}

			replaceBuffer("([ \\t]+$|^" + TRIM_COMA + "\\n)", "", "r");
			replaceBuffer("(\\w)( )(, \\w)", "$1$3", "r");
			replaceBuffer("\\b(AND|OR)\\b", "\\t$0", "ir");
			replaceBuffer(REGEXP_SQL_DOUBLE_SPACES, " ", "r");
			replaceBuffer("^,", "\\t,", "r");
			replaceBuffer("[ \\t]*;[ \\t]*", ";", "r");
		}

		//renombrar este y convertQueryProcess a queryProcess
		//o revisar cómo se puede resolver para Apply_Massive_Macro.bsh
		public void processText(){
			if(opcs == ""){
				Macros.message(view, "You have to Check at least one Option");
				return;
			}

			if(bfTmp == null){
				bfTmp = openTempBuffer();
				beautyQuery(opcs);
				closeTempBuffer(bfTmp);
			}
			dialog.dispose();
		}
	}

	/**
	Convert a querys an another query

		SELECT_UPDATE	SELECT_INSERT	INSERT_SELECT	INSERT_UPDATE	INSERT_CSV	UPDATE_INSERT	UPDATE_SELECT	UPDATE_CSV	CSV_INSERT	CSV_SELECT	CSV_UPDATE
	putWhere	ok	ok	-	-	-	-	ok		-	-	-
	whereIn	ok	ok	-	-	-				-	-	-
	beauty	ok	ok	ok		-
	return	-	-	ok	ok	ok
	convert	ok	ok	ok	ok	ok	ok	ok	ok	ok	ok	ok
	comments	ok	ok
	;

	SELECT ESTADO_FINCA, 'ACT'
	, CN_USUARIO_MODIFICACION, 'SICA_USER'
	, FECHA_MODIFICACION, SYSDATE
	, Fecha_Inactivacion, TO_DATE('31/01/2016', 'dd/mm/yyyy')
	, Area_Cultivo, 6,24
	FROM SICA.SC_FINCA
	WHERE CODIGO_SICA ='1700100378'
	ORDER BY ESTADO_FINCA
	;

	SC_FINCA
	FIELD	VALUE
	CODIGO_SICA	'1700100378'
	ESTADO_FINCA	'ACT'
	CN_USUARIO_MODIFICACION	'SICA_USER'
	FECHA_MODIFICACION	SYSDATE
	Fecha_Inactivacion	TO_DATE('31/01/2016','dd/mm/yyyy')
	Area_Cultivo	6,24

	INSERT INTO SICA.SC_FINCA (CODIGO_SICA, ESTADO_FINCA, CN_USUARIO_MODIFICACION, FECHA_MODIFICACION, Fecha_Inactivacion, Area_Cultivo)
	VALUES ('1700100378', 'ACT', 'SICA_USER', SYSDATE, TO_DATE('31/01/2016', 'dd/mm/yyyy'), 6,24);

	UPDATE SICA.SC_FINCA
	SET CODIGO_SICA = '1700100378'
	, ESTADO_FINCA = 'ACT'
	, CN_USUARIO_MODIFICACION = 'SICA_USER'
	, FECHA_MODIFICACION = SYSDATE
	, Fecha_Inactivacion = TO_DATE('31/01/2016','dd/mm/yyyy')
	, Area_Cultivo = 6,24
	WHERE CODIGO_SICA = '2529300114';
	 */
	public class ConvertQuery{
		private String query1 = "";
		private String query2 = "";
		private String conversion = "";
		private JRadioButton rbFromInsert = new JRadioButton("INSERT");
		private JRadioButton rbToInsert = new JRadioButton("INSERT");
		private JRadioButton rbFromSelect = new JRadioButton("SELECT");
		private JRadioButton rbToSelect = new JRadioButton("SELECT");
		private JRadioButton rbFromUpdate = new JRadioButton("UPDATE");
		private JRadioButton rbToUpdate = new JRadioButton("UPDATE");
		private JRadioButton rbFromCsv = new JRadioButton("CSV");
		private JRadioButton rbToCsv = new JRadioButton("CSV");

		public String getResult(){
			return conversion;
		}

		private void validation(){
			rbToInsert.setEnabled(true);
			rbToSelect.setEnabled(true);
			rbToUpdate.setEnabled(true);
			rbToCsv.setEnabled(true);

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

						conversion = query1 + "_" + query2;

						dialog.dispose();

						if(conversion.equals("CSV_SELECT")){
							queryType = conversion;
						}
						else if(query1.equals("CSV") && !query2.equals("CSV_SELECT")){
							queryType = "CSV_";
						}

						setQueryType(queryType);
						if(hasSyntaxError()){
							return;
						}

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

		/**
		 * Convert a query to another query
		 * @param String query1: first query query to convert
		 * @param String query2: final converted query
		 */
		public void convertQuery(String query1, String query2){
			String tableName = "";
			String fowardQuery = "";
			SpreadSheet sp = new SpreadSheet();
			String conversion = query1 + "_" + query2;
			int semiColon = replaceBuffer(REGEXP_SQL_LAST_SEMICOLON, "", "ar");

			//format decimal
			replaceBuffer(REGEXP_SQL_NUMBER, "_0.replaceAll(\",\", \".\")", "br");

			//quita todos los posibles comentarios que puedan quedar
			replaceBuffer(REGEXP_SQL_COMMENT, "", "ir");
			if(!query1.equals("CSV")){
				textArea.selectAll();
				textArea.joinLines();
			}

			if(conversion.equals("SELECT_UPDATE") || conversion.equals("UPDATE_SELECT")){
				replaceBuffer("ORDER[ \\t]BY.*(\\n.*)*", "", "ir");

				if(findBuffer("WHERE.*", "air")){
					fowardQuery = textArea.getSelectedText();
					textArea.delete();
				}
			}

			switch(query1){
			case "INSERT":
				tableName = getNameTable("\\bINSERT\\b[ \\t]\\bINTO\\b " + REGEXP_SQL_OBJECT, "^\\w+ \\w+ ");

				if(!query2.equals("SELECT")){
					replaceBuffer("\\)[ \\t]*\\bVALUES\\b[ \\t]*\\(", "\\n", "ir");
					replaceBuffer(TRIM_UP + "\\(", "", "r");
					replaceBuffer(TRIM_COMA, ",", "r");
					replaceBuffer(REGEXP_SQL_LAST_ROUND_BRACKET, "", "r");

					formatSpecialValues(true, false);
					sp.transposeMatrix();
					formatSpecialValues(false, true);
					replaceBuffer("^", ", ", "r");
				}
				break;
			case "SELECT":
				replaceBuffer("^[ \\t]*\\b(SELECT|DISTINCT)\\b[ \\t]*", "", "ir");
				tableName = getNameTable("\\bFROM\\b[ \\t]+" + REGEXP_SQL_OBJECT, "^\\w+[ \\t]+");
				formatSpecialValues(true, true);
				replaceBuffer("^", ", ", "r");
				break;
			case "UPDATE":
				tableName = getNameTable("UPDATE " + REGEXP_SQL_OBJECT, "^\\w+ ");

				findBuffer("\\bWHERE\\b.*", "air");
				Registers.cut(textArea,'$');

				replaceBuffer("=", ",", "r");
				formatSpecialValues(true, true);
				break;
			case "CSV":
				replaceBuffer("\"", "", "");
				if(!query2.equals("SELECT")){
					tableName = getNameTable("\\A" + REGEXP_SQL_OBJECT + "\\n", "");
				}
				//remove spaces like trim
				replaceBuffer(TRIM_UP, "", "r");
				replaceBuffer(TRIM_DOWN, "", "r");
				break;
			}

			switch(conversion){
			case "UPDATE_SELECT":
				replaceBuffer(REGEXP_SQL_SET, "", "ir");
				replaceBuffer("(^|\\t)", ", ", "r");

				replaceBuffer(REGEXP_SQL_RETURN_CARRIAGE, "\\n, ", "ir");
				replaceBuffer(TRIM, "", "r");
				deleteDuplicates(textArea);

				replaceBuffer("\\A, ", "SELECT ", "r");
				replaceBuffer("\\z", "\\nFROM " + tableName, "r");
				break;
			case "CSV_SELECT":
				int numLines = view.getTextArea().getText().split("\n").length;
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
				replaceBuffer("(')(" + REGEXP_SQL_FUNC_VALUES  + ")(')", "$2", "r");

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
				textArea.goToBufferEnd(false);
				textArea.insertEnterAndIndent();
				textArea.setSelectedText(tmpQuery);
				replaceBuffer("$", " FROM DUAL", "r");

				replaceBuffer("'+[ \\t]*(" + REGEXP_SQL_FUNC_VALUES + "|" + REGEXP_SQL_RESERVED_VALUES + ")+[ \\t]*'", "$1", "r");
				return;
			}

			switch(query2){
			case "INSERT":
				sp.transposeMatrix();
				replaceBuffer("\\t", ", ", "r");
				textArea.goToBufferStart(false);
				textArea.goToEndOfWhiteSpace(true);
				replaceSelection(".*", "INSERT INTO " + tableName + "($0)", "r");
				textArea.goToBufferEnd(false);
				textArea.goToStartOfWhiteSpace(true);
				replaceSelection(".*", "VALUES($0)", "r");
				break;
			case "UPDATE":
				replaceBuffer("\\t", " = ", "r");
				replaceBuffer("\\A, ", "UPDATE " + tableName + "\\nSET ", "r");
				break;
			case "CSV":
				replaceBuffer(REGEXP_SQL_SET, "", "ir");
				replaceBuffer("\\A", "Structure table: " + tableName + "\nFIELD	VALUE\n", "r");
				replaceBuffer(BLANK_LINE, "", "r");
				replaceBuffer("^" + TRIM_COMA, "", "r");
				return;
			}

			if(!fowardQuery.equals("")){
				textArea.goToBufferEnd(false);
				textArea.insertEnterAndIndent();
				textArea.setSelectedText(fowardQuery);
			}
			// Vuelve a poner el punto y coma al final de la sentencia en el caso de que haya exisitido
			if(!query2.equals("CSV") && semiColon > 0){
				replaceBuffer("\\z", ";", "r");
			}
		}

		public void processText(){
			if(query1 == null){
				query1 = conversion.split("_")[0];
			}
			if(query2 == null){
				query2 = conversion.split("_")[1];
			}
			bfTmp = openTempBuffer();

			if(!query1.equals("CSV")){
				replaceBuffer("[ \\t]+", " ", "r");
			}
			switch(conversion){
			case "CSV_UPDATE":
				convertQuery("CSV", "INSERT");
				convertQuery("INSERT", "UPDATE");
				break;
			case "INSERT_SELECT":
				convertQuery("INSERT", "CSV");
				replaceBuffer("(Structure table: )(" + REGEXP_SQL_OBJECT + ")(\\nFIELD\\tVALUE)((\\n.*)+)", "\"SELECT\" + _4.replaceAll(\"(?m)(^|\\t)\", \", \") + \"\\nFROM \" + _2", "br");
				replaceBuffer("SELECT, \\n, ", "SELECT ", "r");
				break;
			case "SELECT_INSERT":
				convertQuery("SELECT", "UPDATE");
				replaceBuffer("(\\bSET\\b)((.*\\n)+)(\\bWHERE\\b[ \\t]*)((.*\\n*)+)", "$1 $5, $2", "ir");
			case "UPDATE_INSERT":
				convertQuery("UPDATE", "CSV");
				//TODO:VERIFICAR SI FUNCIONA
				replaceBuffer("Structure table: (" + REGEXP_SQL_OBJECT + ")\\nFIELD\\tVALUE.*\\n", "$1\\n", "ir");
				convertQuery("CSV", "INSERT");
				break;
			default:
				convertQuery(query1, query2);
				break;
			}

			if(!query1.equals("CSV") && !query2.equals("CSV")){
				textArea.selectAll();
				new BeautyQuery().beautyQuery("ui");
			}
			closeTempBuffer(bfTmp);
		}
	}

	/**
	 * Method convertSqlToLanguage()
	 * Convert fields tables to any Language
	 * Sample:

	NU_TO_INVOICE
	CD_INVOICE

	TO:

	private String nuToInvoice = "";
	private String cdInvoice = "";


	public String getNuToInvoice(){
		return nuToInvoice;
	}

	public void setNuToInvoice(String nuToInvoice){
		this.nuToInvoice = nuToInvoice;
	}

	public String getCdInvoice(){
		return cdInvoice;
	}

	public void setCdInvoice(String cdInvoice){
		this.cdInvoice = cdInvoice;
	}
	 */
	public void convertSqlToLanguage(){
		/*		
	Macros.getMacro("Editing/First_To_UpperCase").invoke(view);

import masterraise.Edit;
//bfTmp = new Edit().openTempBuffer();
edit = new Edit();
edit.firsUpperCase();
edit.replaceSelection("_", " ", "");

search="_";
replace=" ";
SearchAndReplace.setSearchString(search);
SearchAndReplace.setReplaceString(replace);
SearchAndReplace.setWholeWord(false);
//SearchAndReplace.replace(view);
//SearchAndReplace.replace(view, buffer, 0, 13);
SearchAndReplace.replace(view, buffer, textArea.getSelection(0).getStart(), textArea.getSelection(0).getEnd());


startLine = textArea.getCaretLine();
endLine = startLine;
selection = new Selection.Range(textArea.getLineStartOffset(startLine), textArea.getLineEndOffset(endLine));
selections = textArea.getSelection();

replaceSelection("_", " ", "");
		 */	
		JEditBuffer bfTmp = openTempBuffer();
		replaceBuffer("^[ \t]+", "", "r");
		firsUpperCase();

		replaceSelection("_", "", "");
		replaceSelection("(^)(\\w)(.*.)($)", "\"private String \" + _2.toLowerCase() + _3 + \" = \\\"\\\";\"", "br");

		String selectedText = textArea.getSelectedText() + "\n\n";

		new Java().genGetSet();

		textArea.goToBufferStart(false);
		textArea.setSelectedText(selectedText);

		closeTempBuffer(bfTmp);
	}

	/**
	 * Method formatIn()
	 * Format a list for where:
	 * 1
	 * 2
	 * 3
	 * to:
	 * in(1,2,3)
	 * 
	 * 123
	 * 456
	 * 456
	 * fdsa
	 * 789
	 * 789
	 * to:
	 * in('one', 'two', '3')
	 */
	public void formatIn(){
		JEditBuffer bfTmp = openTempBuffer();
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
	 * Method convertToSqlLite()
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
	 *
	 * @example
	(@CODIGO VARCHAR(6)=NULL,
	@NOMBRE VARCHAR(10)=NULL)
	values(@CODIGO = '',
	@NOMBRE = 'domiciliar')

	POR:
	DECLARE @CODIGO VARCHAR(6)
	,@NOMBRE VARCHAR(10)

	SET @CODIGO = ''
	SET @NOMBRE = 'domiciliar'

	 o

	 (@LINKEO AS VARCHAR(50) ,
	 @strconvenio AS VARCHAR(5) ,
	 @strRegional AS VARCHAR(50) ,
	 @strCadenaPermisos as varchar (50),
	 @strFIni as varchar (10) = NULL ,
	 @PageSize as numeric (18,2)  ,
	 @i_RegInicial int,
	 @i_maxRegistros int
	 )
	 values('PRESTAR.DBO.', '96' ,  '3, 4' ,  '(1,2)' , null,  function(1, 2) ,1 , 30)

	 POR:
	 DECLARE @LINKEO AS VARCHAR(50)
	 ,@strconvenio AS VARCHAR(5)
	 ,@strRegional AS VARCHAR(50)
	 ,@strCadenaPermisos AS VARCHAR (50)
	 ,@strFIni AS VARCHAR (10)
	 ,@PageSize AS NUMERIC (18,2)
	 ,@i_RegInicial INT
	 ,@i_maxRegistros INT

	 TODO: Se puede usar el transposeMatrix, Corregir para que quede así:
	 SET @LINKEO = 'PRESTAR.DBO.'
	 SET @strconvenio = '96'
	 SET @strRegional = '3, 4'
	 SET @strCadenaPermisos = '(1,2)'
	 SET @strFIni = NULL
	 SET @PageSize = function(1, 2)
	 SET @i_RegInicial = 1
	 SET @i_maxRegistros = 30
	 */
	public boolean sqlServerSetVariablesSp(){
		String strArroba = "";
		String strMatchError = "The number rows from this column don't match with anothers columns";
		String selectedText = textArea.getSelectedText();

		//En el caso que no haya seleccionado nada tomará todo el buffer
		if(selectedText=="" || selectedText==null){
			textArea.selectAll();
			selectedText = textArea.getSelectedText();
		}

		JEditBuffer bfTmp = openTempBuffer();

		// quita los espacios al principio y al final
		replaceBuffer("\\A$[\\n \\t]+", "", "r");
		replaceBuffer("[\\n$ \\t]+\\z", "", "r");

		//formateo el texto
		replaceBuffer("\\)[  \\t]*values[  \\t]*\\(", ")\\nvalues(", "ir");

		setQueryType("SP");
		if(hasSyntaxError()){
			return false;
		}

		//Quita los comentarios
		replaceBuffer("[ \\t]*--.*|/\\*([\\n\\t ]*([#\\w áéíóú]+\\n)+[\\n\\t ]*)+\\*/", "", "ir");

		//Identifico si el primer caracter es una variable (contiene arroba)
		findBuffer("values[ \\t]*\\(", "air");

		textArea.goToNextCharacter(false);
		textArea.goToNextCharacter(true);

		strArroba = textArea.getSelectedText();

		//quito los espacios que puedan haber despues de values(
		replaceBuffer("values[(] +", "values(", "ir");
		replaceBuffer("^[ |\\t]+", "", "ir");

		//pongo en un sólo renglón la segunda línea desde la palabra values
		textArea.goToBufferStart(false);
		findBuffer("values[ \\t]*\\(", "air");

		textArea.goToStartOfWhiteSpace(false);
		textArea.goToBufferEnd(true);

		replaceSelection("\n", "", "ir");

		//pongo en un sólo renglón la primera línea desde la palabra values
		textArea.goToBufferStart(false);
		findBuffer("values[ \\t]*\\(", "air");
		textArea.goToStartOfWhiteSpace(false);
		textArea.goToBufferStart(true);
		replaceSelection("\n", "", "ir");

		//quito las asignaciones de null y números
		replaceSelection("=[ ]*([ ]{0,}\\w+|'')", "", "ir");

		textArea.goToNextCharacter(false);
		textArea.insertEnterAndIndent();

		textArea.goToBufferStart(false);
		textArea.goToNextLine(false);
		textArea.goToNextCharacter(true);

		//controlo los valores cuando tienen in(v1,v2,v3,v4)
		textArea.goToBufferStart(false);
		textArea.goToEndOfWhiteSpace(true);
		Registers.cut(textArea,'i');
		textArea.deleteLine();

		textArea.goToBufferStart(false);
		textArea.goToNextWord(true,false);
		textArea.goToNextWord(true,false);
		Registers.cut(textArea,'s');
		textArea.goToEndOfWhiteSpace(false);
		textArea.goToPrevWord(true,false);
		Registers.cut(textArea,'e');

		textArea.goToBufferStart(false);
		Registers.paste(textArea,'s',false);
		Registers.clearRegister('s');
		textArea.goToEndOfWhiteSpace(false);
		Registers.paste(textArea,'e',false);
		Registers.clearRegister('e');


		textArea.goToBufferStart(false);
		Registers.paste(textArea,'i',false);
		Registers.clearRegister('i');
		textArea.insertEnterAndIndent();

		//Controlo las posibles comas que se encuentren en las asignaciones cuando tienen in(v1,v2,v3,v4)
		textArea.goToBufferStart(false);
		while(findBuffer("[\\(']([ ]*\\w+[, ]+)+", "ir")){
			replaceSelection(",", "__", "");
		}

		//Verifico que el número de asignaciones coincida con el número de valores
		textArea.goToBufferStart(false);
		int numComaAsig = getNumComa();

		textArea.goToNextLine(false);
		textArea.goToStartOfWhiteSpace(false);
		int numComaValue = getNumComa();
		if(numComaAsig!=numComaValue){
			closeTempBuffer(bfTmp);
			Macros.message(view, strMatchError);
			return false;
		}

		//Para la segunda línea
		//En el caso que sea una variable sólo asigno valores a las variables
		if(strArroba.equals("@")){
			findBuffer("values[ \\t]*\\(", "air");
			textArea.goToStartOfWhiteSpace(false);
			textArea.goToEndOfWhiteSpace(true);

			replaceSelection("values(", "SET ", "i");
			replaceSelection(",", " SET ", "i");
			replaceSelection("[)]$", "", "ir");
		}
		//En el caso que NO sea una variable, pongo la variable y asigno valores a las variables
		else{
			String tmpPrimeraLinea = "";

			textArea.goToBufferStart(false);
			textArea.goToStartOfWhiteSpace(false);
			textArea.goToEndOfWhiteSpace(true);

			tmpPrimeraLinea = textArea.getSelectedText();

			//Quito las definiciones de las variables que se encuentran antes de la última definición
			replaceSelection("[ ]+,", ",", "ir");

			replaceSelection("[ ]+(\\w+)[ ]{0,},|[ ]+(\\w+)[ ]{0,}[(][0-9]+[)][ ]{0,},|[ ]+(\\w+)[ ]{0,}[(][0-9]+,[0-9]+[)][ ]{0,},", ",", "ir");

			//Quito los posibles as que se encuentran antes de la última definición
			replaceSelection("\\bas[ ]{0,},", ",", "ir");

			//quito la última definición
			replaceSelection("[ ]+(\\w+)[ ]{0,}[)]|[ ]+(\\w+)[ ]{0,}[(][0-9]+[)][ ]{0,}[)]|[ ]+(\\w+)[ ]{0,}[(][0-9]+,[0-9]+[)][ ]{0,}[)]", ")", "ir");

			//Quito el posible as que se encuentre en la última definición
			replaceSelection("\\bas[ ]{0,}\\)", ")", "ir");

			textArea.goToBufferStart(false);
			textArea.setSelectedText("INSERT INTO garbage ");

			new ConvertQuery().convertQuery("INSERT", "CSV");
			replaceBuffer("[ \\t]+\\w+.*\\t", "\\t", "r");
			replaceBuffer("Structure table: \\w+\\nFIELD	VALUE\\n", "", "r");
			replaceBuffer("\t", " = ", "ir");
			replaceBuffer("^, ", "SET ", "ir");
			replaceBuffer("\n", " ", "ir");

			textArea.goToBufferStart(false);
			textArea.setSelectedText(tmpPrimeraLinea);
			textArea.insertEnterAndIndent();
			textArea.goToBufferStart(false);
		}

		//recupero las comas de los valores cuando tienen in(v1,v2,v3,v4)
		replaceBuffer("__", ",", "r");

		//quito los paréntesis que sobran de la primera línea
		textArea.goToBufferStart(false);
		textArea.goToStartOfWhiteSpace(false);
		textArea.goToEndOfWhiteSpace(true);
		replaceSelection("(^[ |\\t]*\\()|([ |\\t]*\\))$", "", "ir");

		//quito los paréntesis que sobran de la segunda línea
		textArea.goToNextLine(false);
		textArea.goToStartOfWhiteSpace(false);
		textArea.goToEndOfWhiteSpace(true);
		textArea.goToEndOfLine(true);
		replaceSelection("[ ]*\\)[ ]*$", "", "ir");

		//pongo nuevas líneas a todo el documento
		textArea.goToBufferStart(false);
		textArea.setSelectedText("DECLARE ");

		textArea.goToNextWord(false,false);
		textArea.goToEndOfWhiteSpace(true);

		replaceSelection("[ ]{0,},[ ]{0,}@", "\n,@", "ir");

		textArea.goToNextLine(false);
		textArea.goToStartOfWhiteSpace(false);
		textArea.goToEndOfWhiteSpace(true);

		replaceSelection("SET", "\\nSET", "ir");

		//quito las variables que están seteadas como default
		replaceBuffer("^set.*default[ ]*\\n*", "", "ir");

		replaceBuffer("[ \\t]+$", "", "ir");

		// Pongo las palabras reservadas en mayusculas
		replaceBuffer(REGEXP_SQL_RESERVED, "_1.toUpperCase()", "br");

		// Quito las palabras reservadas que no sirven
		replaceBuffer("[ ]*\\bOUTPUT\\b[ ]*", "", "ir");

		closeTempBuffer(bfTmp);
		return true;
	}

	private int getNumComa(){
		int numComa = 0;
		int currentLine = textArea.getCaretLine();
		while(findBuffer(",", "")){
			numComa++;

			if(currentLine != textArea.getCaretLine()){
				numComa--;
				break;
			}
		}

		return numComa;
	}

	/**
	 * Method identifyTmpTables()
	 * Identify tempora variables into Stored Procedure
	 */
	public void sqlServerIdentifyTmpTables(){
		if(findBuffer("#", "a")){
			textArea.selectAll();
			String selectedText = textArea.getSelectedText();
			jEdit.newFile(view);
			textArea.setSelectedText(selectedText);

			//Quito todos los comentarios para no usar las tablas que se encuentran en ellos
			replaceBuffer("[ \\t]*--.*|/\\*([\\n\\t ]*([#\\w áéíóú]+\\n)+[\\n\\t ]*)+\\*/", "", "ir");

			//no toma las llaves primarias de la tabla temporal como si fuera tabla, ejemplo: xll#SPDI
			replaceBuffer("\\w+#\\b\\w+\\b", "\\n$0\\n", "ir");
			replaceBuffer("(\\w+)(#)(\\b\\w+\\b)", "$1__$3", "ir");
			replaceBuffer("#{1,2}\\b[^ 0-9\\(]+\\w+\\b", "\\n$0\\n", "ir");
			replaceBuffer("^.*#[!\"$%&'\\(\\)*+,-./:;=>?@\\[\\\\\\]^_`{|}~].*\\n", "", "ir");
			// reemplazo todas las líneas que no comienzan por la tabla tempora (#temporal)
			replaceBuffer("^[^#].*(\\n|\\z)", "", "ir");
			// quito los posibles caracteres extraños
			replaceBuffer("[!\"$%&'\\(\\)*+,-./:;=>?@\\[\\\\\\]^`{|}~]", "", "r");
			replaceBuffer("(^[ \\t]+|[ \\t]+$)", "", "r");

			textArea.selectAll();
			selectedText = textArea.getSelectedText();

			textArea.selectAll();
			textArea.toUpperCase();
			deleteDuplicates(textArea);

			replaceBuffer("^", "DROP TABLE ", "ir");
			replaceBuffer("^DROP TABLE ##", "--DROP TABLE ##", "r");
			//			TextToolsSorting.sortLines(textArea,false);
			sortLines(textArea);
			textArea.goToBufferEnd(false);
			textArea.insertEnterAndIndent();
			textArea.insertEnterAndIndent();
			replaceBuffer("\\A^\\n", "", "r");

			closeTempBuffer(bfTmp);

			textArea.goToBufferStart(false);
			EditBus.send(new PositionChanging(editPane));
			Registers.paste(textArea,'$',false);
			textArea.goToBufferStart(true);
			replaceSelection("DROP TABLE", "SELECT * FROM", "i");
		}
		else{
			Macros.message(view, "Not found temp tables");
		}
	}

	/**
	 * Method oracleLdrToBatRenameImages()
	 * convert ldr file to rename images like as bat file
	 * Example:
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
		String selectedText = textArea.getSelectedText();
		if(selectedText=="" || selectedText==null){
			Macros.message(view, "Must Select a Text");
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
