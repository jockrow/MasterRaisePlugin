/**********************************************/
/*      Develop by Richard Martinez 2012      */
/**********************************************/
/**
 * Show the infoViewer under word from caret if search by searcher or api
 */
package masterraise.infoviewer;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.util.Log;

public class MasterRaiseInfoViewer {
	/***
	* Open all valid urls from a selected text
	* if select unique one line you can browser url
	* if the unique one line is not valid url this find the selected prhase or word
	***/
	public static void showBrowser(String url) {
		View view = jEdit.getActiveView();
		String[] arrLines = url.split("\\n");
		Pattern p = Pattern.compile("((https?|ftp)://)?(\\w+\\.\\w+)+(\\p{Graph})*");
		
		if(arrLines.length > 1){
			for(int i=0; i<arrLines.length; i++){
				if(!arrLines[i].trim().equals("")){
					try {
						Matcher m = p.matcher(arrLines[i]);
						m.find();
						url = m.group(0);
						
						if(!url.equals("")){
							goPage(url);
						}
					}catch(IllegalStateException ex){
						Log.log(Log.WARNING, view, "Doesn't Match at " + arrLines[i]);
					}
				}
			}
		}
		else{
			goPage(url);
		}
	}
	
	private static void goPage(String url){
		View view = jEdit.getActiveView();
		String cmd = jEdit.getProperty("infoviewer.otherBrowser");
		String[] args = convertCommandString(cmd, url);
		
		try {
			Runtime.getRuntime().exec(args);
		} catch (Exception ex) {
			Log.log(Log.ERROR, view, ex.getMessage());
		}
	}

	private static String[] convertCommandString(String command, String url) {
		Vector<String> args = new Vector<String>();
		Pattern p = Pattern.compile("^(http.{0,1}://){0,1}(\\w+\\.)+\\w+(\\S+)*$");
		StringBuffer arg = new StringBuffer();
		boolean foundDollarU = false;
		boolean inQuotes = false;
		int end = command.length() - 1;

		for (int i = 0; i <= end; i++) {
			char c = command.charAt(i);
			switch (c ) {
				case '$':
					if (i == end) {
						arg.append(c);
					} else {
						char c2 = command.charAt(++i);
						if (c2 == 'u') {
							arg.append(url);
							foundDollarU = true;
						} else {
							arg.append(c);
							arg.append(c2);
						}
					}
					break;

				case '"':
					inQuotes = !inQuotes;
					break;

				case ' ':
					if (inQuotes) {
						arg.append(c);
					} else {
						String newArg = arg.toString().trim();
						if (newArg.length() > 0) {
							args.addElement(newArg);
						}
						arg = new StringBuffer();
					}
					break;

				case '\\':                    // quote char, only for backwards
					// compatibility
					if (i == end) {
						arg.append(c);
					} else {
						char c2 = command.charAt(++i);
						if (c2 != '\\') {
							arg.append(c);
						}
						arg.append(c2);
					}
					break;

				default:
					arg.append(c);
					break;
			}
		}

		String newArg = arg.toString().trim();

		if (newArg.length() > 0) {
			// Si busca alguna api la mostrará directamente desde la ruta local
			if (url.substring(0,5).equals("file:")) {
				args.addElement(url);
			}
			// de lo contrario buscará en internet
			else {
				// abrirá una ruta url
				if(p.matcher(url).find()){
					args.addElement(url);
				}
				// buscará una palabra o frase en el buscador predeterminado
				else{
					args.addElement(newArg);
				}
			}
		}

		if (! foundDollarU && url.length() > 0) {
			args.addElement(url);
		}

		String[] result = new String[args.size()];
		args.copyInto(result);

		return result;
	}
}
