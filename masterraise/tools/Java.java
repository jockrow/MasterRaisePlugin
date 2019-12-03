/************************************************/
/*      @author Richard Martínez 2016/02/18     */
/************************************************/
package masterraise.tools;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JDialog;
import javax.swing.JMenuBar;

import org.gjt.sp.jedit.Buffer;

import masterraise.Text;

public class Java extends Text{
	/**
	 * Create get and set Methods from Class fields
	 * @example
	 * private String variable = "";
	 * <pre>
	 * To:
	 * public String getVariable(){
	 * 	return variable;
	 * }
	 * 
	 * public void setVariable(String variable){
	 * 	this.variable = variable;
	 * }
	 */
	public String genGetSet(){
		Buffer bfTmp = openTempBuffer();

		replaceBuffer("^[ \\t]+", "", "r");
		replaceBuffer("([ \\t]*)(=.*|;)", "", "r");

		replaceBuffer("(\\p{Graph}+)([ \\t]+)(\\p{Graph}+)([ \\t]+)(\\w)(\\p{Graph}+)"
				, "\"public \" + _3 + \" \" + (_3.equals(\"boolean\") ? \"is\" : \"get\") + _5.toUpperCase() + _6 + \"(){"
						+ "\\n	return \" + _5 + _6 + \";"
						+ "\\n}\\n"
						+ "\\npublic void set\" + _5.toUpperCase() + _6 + \"(\" + _3 + \" \" + _5 + _6 + \"){"
						+ "\\n	this.\" + _5 + _6 + \" = \" + _5 + _6 + \";"
						+ "\\n}"
						+ "\\n\""
						, "bir");

		closeTempBuffer(bfTmp);
		
		return textArea.getText();
	}
	
	/**
	 * convert the fields from sql columns to java properties, get and set
	 * @example
	 * convert from:
	 * <pre>
	 * "ID_PERSONAL_APOYO" NUMBER,
	 * "PRIMER_NOMBRE" VARCHAR2(30 BYTE),
	 * "FECHA_CREACION" DATE,
	 *
	 * To:
	 * private Integer IdPersonalApoyo;
	 * private String PrimerNombre;
	 * private Date FechaCreacion;

	 * object.getIdPersonalApoyo(),
	 * object.getPrimerNombre(),
	 * object.getFechaCreacion(),

	 * object.setIdPersonalApoyo(resultSet.getInteger("ID_PERSONAL_APOYO"));
	 * object.setPrimerNombre(resultSet.getString("PRIMER_NOMBRE"));
	 * object.setFechaCreacion(resultSet.getDate("FECHA_CREACION"));
	 *
	 */
	public String fields2javaProperties(){
		String REPLACE = "(object)(\\.set)(\\w+)(.*)(get)(\\w+)(.*)";
		Buffer bfTmp = openTempBuffer();

		replaceBuffer(TRIM_LEFT + "|\"", "", "r");

		//set
		replaceBuffer("(\\w+)([ \\t]+)(\\w+)(.*)"
				, "\"object.set\" "
						+ "+ _1.toLowerCase()"
						+ "+ \"(resultSet.get\" "
						+ "+ _3 + \"(\\\"\" + _1.toUpperCase() + \"\\\"));\"", "br");

		replaceBuffer("getNumber", "getInteger", "ir");
		replaceBuffer("getDate", "getDate", "ir");
		replaceBuffer("(getVarchar.*)(\\()", "getString(", "ir");

		replaceBuffer("(\\.set)(\\w)", "_1 + _2.toUpperCase()", "br");
		replaceBuffer("(_)([a-z])", "_2.toUpperCase()", "br");

		//get
		textArea.selectAll();
		String text = textArea.getSelectedText();
		duplicate(text);
		replaceSelection(REPLACE, "$1.get$3(),", "r");

		//declare
		duplicate(text);
		replaceSelection(REPLACE, "private $6 $3;", "r");
		closeTempBuffer(bfTmp);

		return textArea.getText();
	}

	private void duplicate(String text){
		textArea.goToStartOfWhiteSpace(false);
		textArea.insertEnterAndIndent();
		textArea.goToBufferStart(false);
		textArea.setSelectedText(text);
		textArea.goToBufferStart(true);
	}
	
	/**
	 * Method javaDefaultIcons()
	 * get List Defaults Icons and Inteface for Java
	 */
	public void javaDefaultIcons(){
		JDialog ed = new JDialog(view);
		try {
			Class<?> UIManagerDefaults = Class.forName("UIManagerDefaults");
			Object umd = UIManagerDefaults.newInstance();
			Method getMenuBar = UIManagerDefaults.getDeclaredMethod("getMenuBar", new Class[0]);
			Method getContentPane = UIManagerDefaults.getDeclaredMethod("getContentPane", new Class[0]);
			JMenuBar menuBar = (JMenuBar) getMenuBar.invoke(umd, new Object[0]);
			
			menuBar.getMenu(0).setVisible(false);
			ed.setJMenuBar(menuBar);
			ed.getContentPane().add((Component) getContentPane.invoke(umd, new Object[0]));
			ed.setSize(718, 622);
			ed.setVisible(true);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
	}
}
