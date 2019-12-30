package masterraise.tools;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.gjt.sp.jedit.Buffer;

import masterraise.Text;

/**
 * Tools for any Language
 * @author Richard Martinez 2010
 *
 */
public class Language extends Text{
	private final static String DEFAULT_LANGUAGE = "javaScript";
	private JDialog dialog  = new JDialog(view, "Print Debug Variables", true);
	private JPanel content = new JPanel(new BorderLayout());
	private JComboBox<?> cmbLanguages = null;
	private String language = DEFAULT_LANGUAGE;
	private String[] lista = new String[]{"- Languages -"
			, "batchScript"
			, "beanShell"
			, "c# "
			, "java"
			, "javaScript"
			, "php"
			, "python"
			, "Oracle PRINT"
			, "SqlServer SELECT"
			, "SqlServer PRINT"
			, "VB "
	};

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void showGui(String type){
		if(!type.equals("language-print-debug-variables")){
			lista = new String[]{"- Languages -"
					, "c#"
					, "java"
					, "javaScript"
					, "php"
					, "vb"
			};
		}
		cmbLanguages = new JComboBox(lista);

		switch(type){
		case "language-code-to-string":
			dialog  = new JDialog(view, "Code To String", true);			
			break;
		case "language-generate-url-string":
			dialog  = new JDialog(view, "Generate Url String", true);
			break;
		}

		content.setBorder(new EmptyBorder(8, 8, 8, 8));
		content.setPreferredSize(new Dimension(200, 40));
		content.add(cmbLanguages);

		cmbLanguages.addKeyListener(new KeyAdapter(){
			public void keyReleased(KeyEvent evt){
				if(evt.getKeyCode() == KeyEvent.VK_ESCAPE){
					dialog.dispose();
				}
			}
		});

		cmbLanguages.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(!cmbLanguages.getSelectedItem().toString().equals("- Languages -")){
					language = cmbLanguages.getSelectedItem().toString();
					dialog.dispose();
					processText(type);
				}
			}
		});

		dialog.setContentPane(content);
		dialog.pack();
		dialog.setLocationRelativeTo(view);
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
	}

	public String getResult(){
		return language;
	}

	public void setResult(){
		language = cmbLanguages.getSelectedItem().toString();
	}

	public String processText(String type){
		Buffer bfTmp = openTmpBuffer();
		replaceBuffer(BLANK_LINE, "", "r");

		if(!type.equals("language-code-to-string")){
			replaceBuffer("\\$|@", "", "r");
			replaceBuffer("[ \\t,]+", "\\n", "ir");
			deleteDuplicates(textArea);
		}

		switch(type){
		case "language-code-to-string":
			replaceBuffer("\"", "\\\"", "");
			replaceBuffer("(^)(.*)", "\t$1+ \"\\\\n$2\"", "r");
			replaceBuffer("(\\+ \"\\\\n)(" + COMMENTS + ")(\"$)", "$2", "ir");
			replaceBuffer("(//.*)(\")", "$1", "r");
			replaceBuffer(TRIM_UP + "\\+ \"\\\\n", "String strCode = \"", "r");
			replaceBuffer(TRIM_DOWN, ";", "r");

			//Settings comments for each line
			switch(language){
			case "javaScript":
				replaceBuffer("String strCode", "var strCode", "");
				break;
			case "php":
				replaceBuffer("String strCode", "$strCode", "");
				replaceBuffer("+ \"", ". \"", "");
				break;
			case "vb":
				replaceBuffer("(.*)(\\+ \"\\\\n)(.*)", "$1& Chr(10) & \\\"$3 _", "r");
				replaceBuffer("\"; _", "\"", "");
				replaceBuffer("\\\"", "\"\"", "");
				replaceBuffer(COMMENTS, "", "r");
				replaceBuffer("(String strCode)(.*)", "Dim strCode As String$2 _", "r");
				break;
			}
			break;

		case "language-generate-url-string":
			replaceBuffer("\\w+", "\\t+ \"&$0=\" + $0", "r");
			replaceBuffer("\\A\\t\\+ \"&", "var url = \"?", "r");
			replaceBuffer("\\z", ";", "r");

			switch(language){
			case "c#": case "java":
				replaceBuffer("var url = ", "String url = ", "i");
				break;
			case "php":
				replaceBuffer("var url = ", "$url = ", "i");
				replaceBuffer("\" + ", "\" . $", "");
				break;
			case "vb":
				replaceBuffer("var url = ", "Dim url As String = ", "");
				replaceBuffer("(\\+ )(\\w+)(.*)", "& $2 _", "r");
				replaceBuffer(" _\\z", "", "r");
				break;
			}
			break;

		case "language-print-debug-variables":
			replaceBuffer("\\w+", "\\t\\+ \", $0:\" + $0", "r");
			replaceBuffer(TRIM_UP +"\\+ \", ", "alert(\"", "r");
			replaceBuffer("\\z", ");", "r");

			switch(language){
			case "batchScript":
				replaceBuffer("^(alert\\(\"|\\t\\+ \", )", "echo ", "r");
				replaceBuffer("(\" \\+ )(\\w+)(.*)", "%$2%", "r");
				break;
			case "beanShell":
				replaceBuffer("alert(\"", "Log.log(Log.NOTICE,this,\"...", "");
				break;
			case "c# ":
				replaceBuffer("alert", "Response.Write", "");
				break;
			case "java":
				replaceBuffer("alert", "System.out.println", "");
				break;
			case "php":
				replaceBuffer("alert(", "echo ", "");
				replaceBuffer("+", ".", "");
				replaceBuffer(");", "", "");
				break;
			case "Oracle PRINT":
				replaceBuffer("alert", "dbms_output.put_line", "");
				replaceBuffer("\"", "'", "");
				replaceBuffer("+", "||", "");
				break;
			case "SqlServer SELECT":
				replaceBuffer("alert(\"", "SELECT @", "");
				replaceBuffer("+ \", ", ", @", "");
				replaceBuffer(":\" + ", " AS ", "");
				replaceBuffer(");", "", "");
				break;
			case "SqlServer PRINT":
				replaceBuffer("alert(\"", "PRINT '", "");
				replaceBuffer(");", "", "");
				replaceBuffer("\"", "'", "");
				replaceBuffer("(:' \\+ )(\\w+)", "$1CAST(ISNULL(@$2) as varchar(255))", "r");
				break;
			case "VB ":
				replaceBuffer("alert(", "MsgBox ", "");
				replaceBuffer("+", "&", "");
				replaceBuffer("$", " _", "r");
				replaceBuffer("); _", "", "");
				break;
			case "python":
				replaceBuffer("alert", "print", "");
				replaceBuffer(");", ")", "");
				replaceBuffer("(\\+ )(\\w+)", "+ str($2)", "r");
				break;
			}
			break;
		}

		String result = bfTmp.getText();
		closeTmpBuffer(bfTmp);
		//TODO:reemplazar result por selectedText
		return result;
	}

	/**
	 * Create 
	 * prepare variables to print with function according selected language
	 * @return function depend from choose language 
	 * @example
	 * <pre>
	 * {@code
	 * one two three
	 * 
	 * to (javascript):
	 * alert("one:" + one
	 *	+ ", two:" + two
	 *	+ ", three:" + three);
	 */
	public String printDebugVariables() {
		return processText("language-print-debug-variables");
	}

	/**
	 * Generate a url string from text
	 * @return url string
	 * @example
	 * <pre>
	 * {@code
	 * one
	 * two
	 * three
	 *
	 * to (javascript):
	 * var url = "?one=" + one + "&two=" + two + "&three=" + three;
	 * }
	 */
	public String generateUrlString() {
		return processText("language-generate-url-string");
	}

	/**
	 * convert a text to String
	 * @return string into variable
	 * @example
	 * <pre>
	 * {@code
	 * View view = jEdit.getActiveView();
	 * JMenuBar menuBar = view.getJMenuBar();
	 * //comment
	 * JMenu menu = menuBar.getMenu(8);
	 * menu.init();
	 * //print message
	 * System.out.println("Hello World");
	 *
	 * to (javascript):
	 * var strCode = "View view = jEdit.getActiveView();"
	 *	+ "\nJMenuBar menuBar = view.getJMenuBar();"
	 *	//comment
	 *	+ "\nJMenu menu = menuBar.getMenu(8);"
	 *	+ "\nmenu.init();"
	 *	//print message
	 *	+ "\nSystem.out.println(\"Hello World\");";
	 * }
	 */
	public String codeToString() {
		return processText("language-code-to-string");
	}

	/**
	 * Convert String to Vars
	 * @example
	 * <pre>
	 * CANT. CAFÉ PERGAMINO SECO
	 * UNIDAD DE MEDIDA CAFÉ PERGAMINO SECO
	 * to:
	 * CANT_CAFE_PERGAMINO_SECO
	 * UNIDAD_MEDIDA_CAFE_PERGAMINO_SECO
	 */
	public String stringToVars(){
		String t = iniSelectedText();
		t=replaceAccent(t);
		t=t.replaceAll("[\\.]|\\b(DE|DEL|OF)\\b", "");
		t=t.replaceAll("[ ]+", "_");

		endSelectedText(t);
		return t;
	}
}
