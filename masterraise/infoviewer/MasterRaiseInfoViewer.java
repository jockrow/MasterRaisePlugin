package masterraise.infoviewer;

import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gjt.sp.jedit.jEdit;

import masterraise.Constants;

/**
 * Show the infoViewer under word from caret if search by searcher or api
 * @author Richard Martinez 2012
 */
public class MasterRaiseInfoViewer {
	private static boolean isUrl = false;

	/**
	 * Open browser with valid url from a selected text
	 * @param url
	 * if select unique one line you can browser url
	 * if the unique one line is not valid url this find the selected prhase or word
	 */
	public static void showBrowser(String url) {
		String[] arrLines = url.replaceAll("(?m)" + Constants.BLANK_LINE, "").split("\\n");
		Pattern p = Pattern.compile(Constants.URL);

		if(arrLines.length > 1){
			for(int i=0; i<arrLines.length; i++){
				try {
					isUrl = true;
					Matcher m = p.matcher(arrLines[i]);
					m.find();
					goPage(m.group(0));
				}catch(IllegalStateException ex){
					isUrl = false;
					goPage(arrLines[i]);
				}
			}
		}
		else{
			goPage(url);
		}
	}

	private static void goPage(String url){
		String cmd = jEdit.getProperty("infoviewer.otherBrowser");
		String[] args = convertCommandString(cmd, url);

		try {
			Runtime.getRuntime().exec(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String[] convertCommandString(String command, String url) {
		Vector<String> args = new Vector<String>();
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
			// if search any api will show directly since the local path
			if (url.substring(0,5).equals("file:")) {
				args.addElement(url);
			}
			// instead will search at Internet
			else {
				// Open a url
				if(isUrl){
					args.addElement(url);
				}
				// find a word or sentence at default search
				else{
					args.addElement(newArg);
				}
			}
		}

		if (!foundDollarU && url.length() > 0) {
			args.addElement(url);
		}

		String[] result = new String[args.size()];
		args.copyInto(result);

		return result;
	}
}
