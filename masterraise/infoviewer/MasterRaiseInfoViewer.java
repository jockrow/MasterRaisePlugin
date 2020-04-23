package masterraise.infoviewer;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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
		
		for(int i=0; i<arrLines.length; i++){
			isUrl = p.matcher(arrLines[i]).find();
			goPage(arrLines[i]);
		}
	}

	/**
	 * open browser with page
	 * @param url with browser open
	 */
	private static void goPage(String url){
		// find a word or sentence at default search
		if(!isUrl){
			url = jEdit.getProperty("infoviewer.searchEngine") + url;
		}

		try {
			Desktop.getDesktop().browse(new URI(url));
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
	}
}
