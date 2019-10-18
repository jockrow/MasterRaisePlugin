package masterraise;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.Registers;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.buffer.JEditBuffer;
import org.gjt.sp.jedit.bufferset.BufferSetManager;
import org.gjt.sp.jedit.search.CurrentBufferSet;
import org.gjt.sp.jedit.search.DirectoryListSet;
import org.gjt.sp.jedit.search.SearchAndReplace;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.textarea.Selection;
import org.gjt.sp.jedit.textarea.TextArea;

import console.Console;
import console.Shell;

//TODO:refactor Edit to Text
public class Edit extends Constants{
	private View view = jEdit.getActiveView();
	private TextArea textArea = view.getTextArea();
	private String selectedText = textArea.getSelectedText() == null ? "" : textArea.getSelectedText();
	Console console = (Console) view.getDockableWindowManager().getDockable("console");

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
		//TODO:corregir la selección para una sóla línea
		boolean ic =  SearchAndReplace.getIgnoreCase();
		boolean re =  SearchAndReplace.getRegexp();
		boolean ww = SearchAndReplace.getWholeWord();
		boolean bs = SearchAndReplace.getBeanShellReplace();
		String oldSearch = SearchAndReplace.getSearchString();
		//		System.out.println("...oldSearch:" + oldSearch);

		//		textArea.goToNextLine(true);
		//		textArea.goToStartOfWhiteSpace(true);

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
			//			String retorno = _getNumReplaces(view.getStatus().getMessage(), false);

			//			SearchAndReplace.setIgnoreCase(ic);
			//			SearchAndReplace.setRegexp(re);
			//			SearchAndReplace.setWholeWord(ww);
			//			SearchAndReplace.setBeanShellReplace(bs);
			//			SearchAndReplace.setSearchString(oldSearch);

			//			System.out.println("...retorno:" + retorno);
			return Integer.parseInt(_getNumReplaces(view.getStatus().getMessage(), false));
		}
		catch(Exception ex){
			ex.printStackTrace();
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

	public JEditBuffer openTempBuffer(){
		final EditPane editPane = view.getEditPane();

		//En el caso que no haya seleccionado nada tomará todo el buffer
		if(selectedText.trim().equals("")){
			textArea.selectAll();
			selectedText = textArea.getSelectedText();
		}
		Buffer bfTmp = BufferSetManager.createUntitledBuffer();
		bfTmp.insert(0, selectedText);
		editPane.setBuffer(bfTmp);

		return bfTmp;
	}

	public void closeTempBuffer(JEditBuffer bfTmp){
		textArea.selectAll();
		selectedText = textArea.getSelectedText();
		jEdit._closeBuffer(view,(Buffer) bfTmp);
		Registers.setRegister('$', selectedText);
		textArea.setSelectedText(selectedText);
	}

	public String iniSelectedText(){
		String t = textArea.getSelectedText();

		if(t=="" || t==null){
			textArea.selectAll();
			t=textArea.getSelectedText();
		}

		return t;
	}

	public void endSelectedText(String t){
		Selection tmpSelection = (Selection) textArea.getSelection(0).clone();

		textArea.setSelectedText(t);
		Registers.setRegister('$', t);

		if(textArea.getCaretPosition() < tmpSelection.getEnd()){
			textArea.extendSelection(tmpSelection.getStart(), textArea.getCaretPosition());
		}
		else{
			textArea.addToSelection(tmpSelection);
		}
	}

	protected void deleteDuplicates(TextArea textArea){
		invokeDefaultClass("TextToolsSorting", "deleteDuplicates", textArea);
	}

	protected void transposeLines(TextArea textArea){
		invokeDefaultClass("TextToolsPlugin", "transposeLines", textArea);
	}

	protected void sortLines(TextArea textArea){
		invokeDefaultClass("TextToolsSorting", "sortLines", textArea);
	}

	private void invokeDefaultClass(String className, String methodName, TextArea textArea){
		try{
			Class<?> mainClass = Class.forName(className);
			Method method = null;

			if(methodName.equals("sortLines")){
				method = mainClass.getMethod(methodName, JEditTextArea.class, boolean.class);
				method.invoke(mainClass, textArea, false);
			}
			else{
				method = mainClass.getMethod(methodName, View.class, JEditTextArea.class);
				method.invoke(mainClass, view, textArea);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	protected void runCommand(String command)
	{
		view.getDockableWindowManager().addDockableWindow("console");
		Shell _shell = Shell.getShell("System");
		console.run(_shell, command);
	}

	protected void waitForConsole()
	{
		view.getDockableWindowManager().addDockableWindow("console");
		console.getShell().waitFor(console);
	}

	/**
	 * Toggle the first Case to upperCase for each word
	 * Example:
	 * IGEC_GESTOR_PROYECTO or IGEC GESTOR PROYECTO
	 * for:
	 * Igec Gestor Proyecto
	 */
	public void firsUpperCase(){
		String selectedText = textArea.getSelectedText() == null ? "" : textArea.getSelectedText();
		if(selectedText.trim().equals("")){
			textArea.selectAll();
			selectedText = textArea.getSelectedText();
		}

		int numUnderscore = replaceSelection("_", " ", "");

		replaceSelection("([ _\\t]+)(\\p{L})(\\p{L}+)", "_1 + _2.toUpperCase() + _3.toLowerCase()", "bir");
		replaceSelection("(^\\p{L})(\\p{L}+)", "_1.toUpperCase() + _2.toLowerCase()", "bir");

		System.out.println("...numUnderscore:" + numUnderscore);
		if(numUnderscore > 0){
			replaceSelection(" ", "_", "");
		}
	}

	/**
	 * Join coherence lines
	 */
	public void smartJoin(){
		String t = iniSelectedText();

		t=t.replaceAll("(?m)^[ \t]+|[ \t]+$", "");
		t=t.replaceAll("(?m)\n+", " ");
		t=t.replaceAll("(?m)[ \t]+,[ \t]+", ", ");
		t=t.replaceAll("(?m)(\\p{Print})([ \t]+)([\\)\\};])", "$1$3");
		t=t.replaceAll("(?m)([\\(\\{])([ \t]+)(\\p{Print})", "$1$3");

		endSelectedText(t);
	}


	/**
	 * replace all accent
	 */
	public void replaceAccent(){
		String t = iniSelectedText();

		for(int i=0; i<ARR_CHARS.length; i++){
			t=t.replace(ARR_CHARS[i][0], ARR_CHARS[i][4]);
			if(ARR_CHARS[i][0].equals("ñ")){
				break;
			}
		}

		endSelectedText(t);
	}

	private void _searchBack(){
		SearchAndReplace.setSearchString(textArea.getSelectedText());
		SearchAndReplace.setAutoWrapAround(true);
		SearchAndReplace.setReverseSearch(true);
		SearchAndReplace.setIgnoreCase(true);
		SearchAndReplace.setSearchFileSet(new CurrentBufferSet());
		SearchAndReplace.find(view);
	}

	/**
	 * Find selection to back in document
	 */
	public void searchBack(){
		if(textArea.getSelectionCount() == 1){
			_searchBack();
		}
		else if(textArea.getSelectionCount() == 0){
			textArea.setSelection(textobjects.TextObjectsPlugin.word(textArea, textArea.getCaretPosition(), false));
			_searchBack();
		}
	}
}
