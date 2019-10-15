package masterraise;

import java.io.File;

import org.gjt.sp.jedit.BeanShell;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.search.CurrentBufferSet;
import org.gjt.sp.jedit.search.DirectoryListSet;
import org.gjt.sp.jedit.search.SearchAndReplace;


public class Edit {
	/**
	 * Get number of occurrences when replace
	 * @param identify if is a directory
	 * @return number of occurences replaced, if is directory return: "number occurences,number files"
	 */
	private String _getNumReplaces(String numReplaces, boolean isDirectory){
		if(!isDirectory){
			return numReplaces.replaceAll("(\\w+ )(\\d+)(.*in )(\\d+)(.*)", "$2");
		}

		return numReplaces.replaceAll("(\\w+ )(\\d+)(.*in )(\\d+)(.*)", "$2,$4");
	}

	/**
	 * Replace all phrases from directory
	 * @param search - phrase to find
	 * @param replace - phrase to replace
	 * @param flags - params b to setBeanShellReplace, i to setIgnoreCase, r to setRegexp, w to Whole word
	 * @param directory - String from directory
	 * @param filter - filter to apply the files
	 * @param openFiles - flag to open files in jEdit without save, for edit later
	 * @return numReplaces
	 */
	public String replaceDirectory(String search, String replace, String flags, String directory, String filter, boolean openFiles){
		View view = jEdit.getActiveView();
		boolean ic =  SearchAndReplace.getIgnoreCase();
		boolean re =  SearchAndReplace.getRegexp();
		boolean ww = SearchAndReplace.getWholeWord();
		boolean bs = SearchAndReplace.getBeanShellReplace();
		String oldSearch = SearchAndReplace.getSearchString();

		SearchAndReplace.setSearchString(search);
		SearchAndReplace.setReplaceString(replace);
		SearchAndReplace.setWholeWord(flags.indexOf('w') >= 0);
		SearchAndReplace.setBeanShellReplace(flags.indexOf('b') >= 0);
		SearchAndReplace.setIgnoreCase(flags.indexOf('i') >= 0);
		SearchAndReplace.setRegexp(flags.indexOf('r') >= 0);
		SearchAndReplace.setSearchFileSet(new DirectoryListSet(directory,filter,true));
		SearchAndReplace.hyperSearch(view,false);
		SearchAndReplace.replaceAll(view);
		String statusBar = view.getStatus().getMessage();

		SearchAndReplace.setIgnoreCase(ic);
		SearchAndReplace.setRegexp(re);
		SearchAndReplace.setWholeWord(ww);
		SearchAndReplace.setBeanShellReplace(bs);
		SearchAndReplace.setSearchString(oldSearch);

		String[] matchFiles = SearchAndReplace.getSearchFileSet().getFiles(view);

		if(!openFiles && statusBar.indexOf("Replaced 0") < 0){
			for(int i=0; i<matchFiles.length; i++){
				Buffer ldrBuff = jEdit.openFile(view,matchFiles[i]);
				ldrBuff.save(view,null,true);
				jEdit._closeBuffer(view,ldrBuff);
			}
		}

		return _getNumReplaces(statusBar, true);
	}

	/**
	 * Replace all phrases from buffer
	 * @param search - phrase to find
	 * @param replace - phrase to replace
	 * @param flags - params b to setBeanShellReplace, i to setIgnoreCase, r to setRegexp, w to Whole word
	 * @return numReplaces
	 */
	public int replaceBuffer(String search, String replace, String flags){
		View view = jEdit.getActiveView();
		boolean isRunning = BeanShell.isScriptRunning();
		if(isRunning){
			BeanShell.runScript(view, jEdit.getSettingsDirectory() + File.separator + "startup" + File.separator + "startBeanShell.bsh",null,false);
		}

		boolean ic =  SearchAndReplace.getIgnoreCase();
		boolean re =  SearchAndReplace.getRegexp();
		boolean ww = SearchAndReplace.getWholeWord();
		boolean bs = SearchAndReplace.getBeanShellReplace();
		String oldSearch = SearchAndReplace.getSearchString();

		SearchAndReplace.setSearchString(search);
		SearchAndReplace.setReplaceString(replace);
		SearchAndReplace.setBeanShellReplace(flags.indexOf('b') >= 0);
		SearchAndReplace.setWholeWord(flags.indexOf('w') >= 0);
		SearchAndReplace.setIgnoreCase(flags.indexOf('i') >= 0);
		SearchAndReplace.setRegexp(flags.indexOf('r') >= 0);
		SearchAndReplace.setSearchFileSet(new CurrentBufferSet());
		SearchAndReplace.replaceAll(view);

		SearchAndReplace.setIgnoreCase(ic);
		SearchAndReplace.setRegexp(re);
		SearchAndReplace.setWholeWord(ww);
		SearchAndReplace.setBeanShellReplace(bs);
		SearchAndReplace.setSearchString(oldSearch);

		try{
			return Integer.parseInt(_getNumReplaces(view.getStatus().getMessage(), false));
		}
		catch(Exception ex){
			return 0;
		}
	}

	/**
	 * Replace all phrases from selection
	 * @param search - phrase to find
	 * @param replace - phrase to replace
	 * @param flags - params b to setBeanShellReplace, i to setIgnoreCase, r to setRegexp, w to Whole word
	 * @return numReplaces
	 */
	public int replaceSelection(String search, String replace, String flags){
		View view = jEdit.getActiveView();

		boolean isRunning = BeanShell.isScriptRunning();
		if(isRunning){
			BeanShell.runScript(view, jEdit.getSettingsDirectory() + File.separator + "startup" + File.separator + "startBeanShell.bsh",null,false);
		}

		BeanShell.runScript(view, jEdit.getSettingsDirectory() + File.separator + "startup" + File.separator + "startBeanShell.bsh",null,false);
		boolean ic =  SearchAndReplace.getIgnoreCase();
		boolean re =  SearchAndReplace.getRegexp();
		boolean ww = SearchAndReplace.getWholeWord();
		boolean bs = SearchAndReplace.getBeanShellReplace();
		String oldSearch = SearchAndReplace.getSearchString();

		SearchAndReplace.setSearchString(search);
		SearchAndReplace.setReplaceString(replace);
		SearchAndReplace.setBeanShellReplace(flags.indexOf('b') >= 0);
		SearchAndReplace.setWholeWord(flags.indexOf('w') >= 0);
		SearchAndReplace.setIgnoreCase(flags.indexOf('i') >= 0);
		SearchAndReplace.setRegexp(flags.indexOf('r') >= 0);
		SearchAndReplace.replace(view);

		SearchAndReplace.setIgnoreCase(ic);
		SearchAndReplace.setRegexp(re);
		SearchAndReplace.setWholeWord(ww);
		SearchAndReplace.setBeanShellReplace(bs);
		SearchAndReplace.setSearchString(oldSearch);

		try{
			return Integer.parseInt(_getNumReplaces(view.getStatus().getMessage(), false));
		}
		catch(Exception ex){
			return 0;
		}
	}

	/**
	 * Replace all phrases from selection
	 * @param search - phrase to find
	 * @param flags - params w to setAutoWrapAround, v to setReverseSearch, i to setIgnoreCase, r to setRegexp, w to Whole word
	 * @return true if expression found
	 */
	public boolean findBuffer(String search, String flags){
		boolean ic =  SearchAndReplace.getIgnoreCase();
		boolean re =  SearchAndReplace.getRegexp();
		boolean ww = SearchAndReplace.getWholeWord();
		boolean bs = SearchAndReplace.getBeanShellReplace();
		boolean aw = SearchAndReplace.getAutoWrapAround();
		String oldSearch = SearchAndReplace.getSearchString();

		if(flags.indexOf('a') >= 0){
			jEdit.getActiveView().getTextArea().goToBufferStart(false);
		}

		SearchAndReplace.setSearchString(search);
		SearchAndReplace.setAutoWrapAround(flags.indexOf('a') >= 0);
		SearchAndReplace.setReverseSearch(flags.indexOf('v') >= 0);
		SearchAndReplace.setWholeWord(flags.indexOf('w') >= 0);
		SearchAndReplace.setIgnoreCase(flags.indexOf('i') >= 0);
		SearchAndReplace.setRegexp(flags.indexOf('r') >= 0);
		SearchAndReplace.setSearchFileSet(new CurrentBufferSet());
		boolean result = SearchAndReplace.find(jEdit.getActiveView());

		SearchAndReplace.setAutoWrapAround(aw);
		SearchAndReplace.setSearchString(oldSearch);
		SearchAndReplace.setIgnoreCase(ic);
		SearchAndReplace.setRegexp(re);
		SearchAndReplace.setWholeWord(ww);
		SearchAndReplace.setBeanShellReplace(bs);
		return result;
	}
}
