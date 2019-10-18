/********************************************
 *			@author Richard Martínez 2010	*
 *********************************************/
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

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.textarea.TextArea;

import masterraise.Text;

/**
 * language-print-debug-variables
 * prepara unas variables para imprimir en pantalla
 * @example
 * pasa de:
	uno dos tres
 *
 * a (en javascript):
	alert("uno:" + uno + "\n dos:" + dos + "\n tres:" + tres);
 *---------------------
 * language-generate-url-string
 * Genera una url string
 *
 * @example (javascript):
uno
dos
tres
 * a:
var url = "?uno=" + uno + "&dos=" + dos + "&tres=" + tres;
 *---------------------
 *language-code-to-string
 ** prepara unas variables para imprimir en pantalla
 * @example
 * pasa de:
		View view = jEdit.getActiveView();
		JMenuBar menuBar = view.getJMenuBar();
		//un comentario
		JMenu menu = menuBar.getMenu(8);
		menu.init();
		//Imprime el mensaje
		System.out.println("Hello World");

 * 	a (java):
	String strCode = "View view = jEdit.getActiveView();"
		+ "\nJMenuBar menuBar = view.getJMenuBar();"
		//un comentario
		+ "\nJMenu menu = menuBar.getMenu(8);"
		+ "\nmenu.init();"
		//Imprime el mensaje
		+ "\nSystem.out.println(\"Hello World\");";
 */
public class Language extends Text{
	private final View view = jEdit.getActiveView();
	private final TextArea textArea = view.getTextArea();
	private JDialog dialog  = new JDialog(view, "Print Debug Variables", true);
	private JPanel content = new JPanel(new BorderLayout());
	private JComboBox<?> cmbLanguages = null;
	private String language = "";
	private String[] lista = new String[]{"- Languages -"
			, "batchScript"
			, "beanShell"
			, "c# "
			, "java"
			, "javaScript"
			, "php"
			, "python"
			, "System"
			, "Oracle PRINT"
			, "sql SELECT"
			, "SqlServer PRINT"
			, "VB "
	};

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void showGui(String type){
		if(!type.equals("printDebugVariables")){
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
		case "codeToString":
			dialog  = new JDialog(view, "Code To String", true);			
			break;
		case "generateUrlString":
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

	public void processText(String type){
		JEditBuffer bfTmp = openTempBuffer();
		if(!type.equals("codeToString")){
			replaceBuffer("[!\"#$%&'()*+,-/:;=>?@\\[\\\\\\]^`{|}~]", " ", "ir");
			replaceBuffer("^(\\t|[ ])+|(\\t|[ ])+$.*", "", "ir");
			textArea.goToBufferStart(false);
			textArea.goToNextCharacter(true);
			if(language.toString().equals("php") && textArea.getSelectedText().equals("$")){
				replaceBuffer("$", "", "wi");
			}
		}

		switch(type){
		case "codeToString":
			replaceBuffer("\"", "\\\"", "");
			replaceBuffer("(^[ \\t]*)(.*)", "$1+ \"\\\\n$2\"", "r");
			replaceBuffer("(\\+ \"\\\\n//)(.*)(\")", "//$2", "r");
			replaceBuffer(TRIM_UP + "(\\+ \"\\\\n)", "String strCode = \"", "r");
			replaceBuffer(TRIM_DOWN, ";", "r");

			//Inicia cada línea seteando correctamente los comentarios
			switch(getResult()){
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
				replaceBuffer("//", "'", "");
				replaceBuffer("(String strCode)(.*)", "Dim strCode As String$2 _", "r");
				break;
			}
			break;

		case "generateUrlString":
			if(findBuffer("\\n^([ ]|\\t)*\\w+", "air")){
				replaceBuffer("\\n", " ", "ir");
			}

			textArea.selectAll();
			replaceBuffer("\\w+", "\"&$0=\" + $0 +", "r");
			smartJoin();
			replaceBuffer("^\"&", "var url = \"?", "r");
			replaceBuffer(" \\+$", ";", "r");

			switch(language.toString()){
			case "c#": case "java":
				replaceBuffer("var url = ", "String url = ", "i");
				break;
			case "php":
				replaceBuffer("var url = ", "$url = ", "i");
				replaceBuffer("(\\+ )(\\w)", ". \\$$2", "ir");
				replaceBuffer("+ \"", ". \"", "wi");
				break;
			case "vb":
				replaceBuffer("var url = ", "Dim url As String = ", "wi");
				replaceBuffer("=\" + ", "=\" & ", "i");
				replaceBuffer("+ \"", "& \"", "i");
				replaceBuffer(";$", "", "ir");
				break;
			}
			break;

		case "printDebugVariables":
			if(language.substring(0, 3).equals("sql") && textArea.getSelectedText().equals("@")){
				replaceBuffer("@", "", "wi");
			}

			if(findBuffer("\\n^[, \\t]*\\w+", "air")){
				replaceBuffer("\\n", " ", "ir");
			}
			replaceBuffer("[ \\t,]+", "\\n", "ir");

			textArea.selectAll();
			replaceBuffer("\\w+", "\", $0:\" + $0 +", "r");
			smartJoin();
			replaceBuffer("^\", ", "alert(\"", "r");
			replaceBuffer(" \\+$", ");", "r");

			switch(language.toString()){
			case "batchScript":
				replaceBuffer("alert(", "echo ", "");
				replaceBuffer(":\" + ", ":%", "");
				replaceBuffer(" + \", ", "%, ", "");
				replaceBuffer("\"", "", "");
				replaceBuffer(");", "%", "");
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
				replaceBuffer("alert", "echo ", "");
				replaceBuffer("\\(|\\)", "", "r");
				replaceBuffer("(\\+ )(\\w)", ". \\$$2", "ir");
				replaceBuffer("+ \", ", ". \", <br>", "");
				break;
			case "System":
				replaceBuffer("alert(", "echo ", "");
				replaceBuffer(":\" + ", ":\" + %", "");
				replaceBuffer(" + \", ", "% + \", ", "");
				replaceBuffer(");", "%", "");
				break;
			case "Oracle PRINT":
				replaceBuffer("alert", "dbms_output.put_line", "");
				replaceBuffer("\"", "'", "");
				replaceBuffer("+", "||", "");
				break;
			case "sql SELECT":
				replaceBuffer("alert(\"", "SELECT @", "");
				replaceBuffer(":\" + ", " AS ", "");
				replaceBuffer(" + \", ", ", @", "");
				replaceBuffer(");", "", "");
				break;
			case "SqlServer PRINT":
				replaceBuffer("alert(\"", "PRINT '", "");
				replaceBuffer(":\" + ", ":' + CAST(ISNULL(@", "");
				replaceBuffer(" + \", ", ", '') as varchar(255)) + ', ", "");
				replaceBuffer(");", ", '') as varchar(255))", "");
				break;
			case "VB ":
				replaceBuffer("alert(\"", "Response.Write \"<br>", "");
				replaceBuffer(":\" + ", ":\" & ", "");
				replaceBuffer(" + \", ", " & \"<br>", "");
				replaceBuffer(");", "", "");
				break;
			case "python":
				replaceBuffer("alert", "print", "");
				replaceBuffer(");", ")", "");
				replaceBuffer("(\\+ )(\\w+)", "+ str($2)", "r");
				break;
			}
			replaceBuffer("\\p{Cntrl}", "", "ir");
			if(!language.equals("beanShell")){
				replaceBuffer(" \", ", " \"\\\\n ", "ir");
			}
			break;
		}

		closeTempBuffer(bfTmp);
	}

	/**
	 * Convert String to Vars
	 * Example:
	 * CANT. CAFÉ PERGAMINO SECO
	 * UNIDAD DE MEDIDA CAFÉ PERGAMINO SECO
	 * to:
	 * CANT_CAFÉ_PERGAMINO_SECO
	 * UNIDAD_MEDIDA_CAFÉ_PERGAMINO_SECO
	 */
	public void stringToVars(){
		new Text().replaceAccent();
		String t = iniSelectedText();

		t = t.replaceAll("[\\.]|\\b(DE|DEL|OF)\\b", "");
		t = t.replaceAll("[ ]+", "_");

		endSelectedText(t);
	}
}
