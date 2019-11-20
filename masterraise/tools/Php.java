/************************************************
*			@author Richard Martínez 2011/05/13	*
*************************************************/
package masterraise.tools;

import masterraise.Text;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.Macros;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.textarea.TextArea;

public class Php extends Text{
	private final View view = jEdit.getActiveView();
	private final TextArea textArea = view.getTextArea();
	
	/**
	 * Method getPhpVarsFields()
	 * Saca una lista de todos los campos de un archivo html, para transformarlos en variables php
	 */
	public void getVarsFromHtml(){
		if(!findBuffer("<[ \\t]*(select|input|textarea)[ \\t].*>", "air")){
			Macros.message(view, "Fields not found");
			return;
		}
		Buffer bfTmp = openTempBuffer();
		
		new Html().getFieldsList();
		replaceBuffer("(.*)(\\b(name|id)\\b[\"= ]+)(\\w+)(.*)", "\\$$4;", "ir");
		sortLines(textArea);
		closeTempBuffer(bfTmp);
	}
}
