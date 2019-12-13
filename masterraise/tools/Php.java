package masterraise.tools;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.Macros;

import masterraise.Text;

/**
 * Tools for php files
 * @author Richard Martínez 2011/05/13
 */
public class Php extends Text{
	/**
	 * get list for all fields from html file, to transform in php variables
	 */
	public String getPhpVarsFromHtml(){
		if(!findBuffer("<[ \\t]*" + HTML_FILTER_FIELDS, "air")){
			Macros.error(view, "Fields not found");
			return "";
		}
		Buffer bfTmp = openTmpBuffer();

		new Html().getFieldsList();
		replaceBuffer("(.*)(\\b(name|id)\\b[\"= ]+)(\\w+)(.*)", "\\$$4;", "ir");
		sortLines(textArea);
		deleteDuplicates(textArea);

		String fields = bfTmp.getText();
		closeTmpBuffer(bfTmp);
		return fields;
	}
}
