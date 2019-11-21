package masterraise;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.Registers;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.bufferset.BufferSetManager;
import org.gjt.sp.jedit.search.CurrentBufferSet;
import org.gjt.sp.jedit.search.DirectoryListSet;
import org.gjt.sp.jedit.search.SearchAndReplace;
import org.gjt.sp.jedit.textarea.JEditTextArea;
import org.gjt.sp.jedit.textarea.Selection;
import org.gjt.sp.jedit.textarea.Selection.Range;
import org.gjt.sp.jedit.textarea.Selection.Rect;
import org.gjt.sp.jedit.textarea.TextArea;

import console.Console;
import console.Shell;

/**
 * 
 * @author Richard Martinez
 *
 */
public class Text extends Constants{
	private View view = jEdit.getActiveView();
	private TextArea textArea = view.getTextArea();
	private String selectedText = textArea.getSelectedText() == null ? "" : textArea.getSelectedText();
	private Console console = (Console) view.getDockableWindowManager().getDockable("console");
	private Selection[] prevSelection = null;

	public Selection[] getPrevSelection() {
		return prevSelection;
	}

	/**
	 * Count number of character for String
	 * @param str String to count characters
	 * @param expression expression to Find number occurrences
	 * @param opts Options: i to setIgnoreCase, r to setRegexp, w to Whole word
	 * @return number of found characters
	 */
	//TODO: buscar todo lo que tenga Pattern
	public Integer countOccurrences(String str, String expression, String opts){
		int count = 0;
		opts = opts.toLowerCase().trim();
		
		switch(opts.trim()) {
		case "":
			count = StringUtils.countMatches(str, expression);
			break;
		case "i":
			count = StringUtils.countMatches(str.toUpperCase(), expression.toUpperCase());
			break;
		default:
			String prefix = "(?m";

			if(opts.indexOf('w') >= 0) {
				expression = "\\b" + expression + "\\b"; 
			}
			if(opts.indexOf('i') >= 0) {
				prefix += "i";
			}
			prefix += ")";
			
			expression = prefix + expression;

			Pattern pattern = Pattern.compile(expression);
			Matcher matcher = pattern.matcher(str);

			while (matcher.find()) {
				count++;
			}
			break;
		}

		return count;
	}

	/**
	 * Replace all phrases from directory
	 * @param search phrase to find
	 * @param replace phrase to replace
	 * @param opts Options: b to setBeanShellReplace, i to setIgnoreCase, r to setRegexp, w to Whole word
	 * @param directory String from directory
	 * @param filter filter to apply the files
	 * @param openFiles if is true open files in jEdit without save, for edit later
	 * @return Number of replaces
	 */
	public String replaceDirectory(String search, String replace, String opts, String directory, String filter, boolean openFiles){
		boolean ic =  SearchAndReplace.getIgnoreCase();
		boolean re =  SearchAndReplace.getRegexp();
		boolean ww = SearchAndReplace.getWholeWord();
		boolean bs = SearchAndReplace.getBeanShellReplace();
		String oldSearch = SearchAndReplace.getSearchString();

		SearchAndReplace.setSearchString(search);
		SearchAndReplace.setReplaceString(replace);
		SearchAndReplace.setWholeWord(opts.indexOf('w') >= 0);
		SearchAndReplace.setBeanShellReplace(opts.indexOf('b') >= 0);
		SearchAndReplace.setIgnoreCase(opts.indexOf('i') >= 0);
		SearchAndReplace.setRegexp(opts.indexOf('r') >= 0);
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

		return statusBar.replaceAll("(\\w+ )(\\d+)(.*in )(\\d+)(.*)", "$2,$4");
	}

	/**
	 * Replace all phrases from buffer
	 * @param search phrase to find
	 * @param replace phrase to replace
	 * @param opts Options: b to setBeanShellReplace, i to setIgnoreCase, r to setRegexp, w to Whole word
	 * @return Number of replaces
	 */
	public int replaceBuffer(String search, String replace, String opts){
		boolean ic =  SearchAndReplace.getIgnoreCase();
		boolean re =  SearchAndReplace.getRegexp();
		boolean ww = SearchAndReplace.getWholeWord();
		boolean bs = SearchAndReplace.getBeanShellReplace();
		String oldSearch = SearchAndReplace.getSearchString();
		int numReplaces = countOccurrences(textArea.getText(), search, opts);

		SearchAndReplace.setSearchString(search);
		SearchAndReplace.setReplaceString(replace);
		SearchAndReplace.setBeanShellReplace(opts.indexOf('b') >= 0);
		SearchAndReplace.setWholeWord(opts.indexOf('w') >= 0);
		SearchAndReplace.setIgnoreCase(opts.indexOf('i') >= 0);
		SearchAndReplace.setRegexp(opts.indexOf('r') >= 0);
		SearchAndReplace.setSearchFileSet(new CurrentBufferSet());
		SearchAndReplace.replaceAll(view);

		SearchAndReplace.setIgnoreCase(ic);
		SearchAndReplace.setRegexp(re);
		SearchAndReplace.setWholeWord(ww);
		SearchAndReplace.setBeanShellReplace(bs);
		SearchAndReplace.setSearchString(oldSearch);

		return numReplaces;
	}

	/**
	 * Replace all phrases from selection
	 * @param search phrase to find
	 * @param replace phrase to replace
	 * @param opts Options: b to setBeanShellReplace, i to setIgnoreCase, r to setRegexp, w to Whole word
	 * @return Number of replaces
	 */
	public int replaceSelection(String search, String replace, String opts){
		//TODO:corregir la selección para una sóla línea
		boolean ic =  SearchAndReplace.getIgnoreCase();
		boolean re =  SearchAndReplace.getRegexp();
		boolean ww = SearchAndReplace.getWholeWord();
		boolean bs = SearchAndReplace.getBeanShellReplace();
		String oldSearch = SearchAndReplace.getSearchString();
		int numReplaces = countOccurrences(textArea.getSelectedText(), search, opts);

		SearchAndReplace.setSearchString(search);
		SearchAndReplace.setReplaceString(replace);
		SearchAndReplace.setBeanShellReplace(opts.indexOf('b') >= 0);
		SearchAndReplace.setWholeWord(opts.indexOf('w') >= 0);
		SearchAndReplace.setIgnoreCase(opts.indexOf('i') >= 0);
		SearchAndReplace.setRegexp(opts.indexOf('r') >= 0);
		SearchAndReplace.replace(view);

		SearchAndReplace.setIgnoreCase(ic);
		SearchAndReplace.setRegexp(re);
		SearchAndReplace.setWholeWord(ww);
		SearchAndReplace.setBeanShellReplace(bs);
		SearchAndReplace.setSearchString(oldSearch);
		
		return numReplaces;
	}

	/**
	 * Replace all phrases from selection
	 * @param search phrase to find
	 * @param opts Options: w to setAutoWrapAround, v to setReverseSearch, i to setIgnoreCase, r to setRegexp, w to Whole word
	 * @return true if expression found
	 */
	//TODO: EN LOS WHILE EVITAR QUE NO MUESTRE LOS MENSAJES while(findBuffer(REGEXP_SQL_IN_VALUES, "r")){
	public boolean findBuffer(String search, String opts){
		boolean ic =  SearchAndReplace.getIgnoreCase();
		boolean re =  SearchAndReplace.getRegexp();
		boolean ww = SearchAndReplace.getWholeWord();
		boolean bs = SearchAndReplace.getBeanShellReplace();
		boolean aw = SearchAndReplace.getAutoWrapAround();
		String oldSearch = SearchAndReplace.getSearchString();

		if(opts.indexOf('a') >= 0){
			textArea.goToBufferStart(false);
		}

		SearchAndReplace.setSearchString(search);
		SearchAndReplace.setAutoWrapAround(opts.indexOf('a') >= 0);
		SearchAndReplace.setReverseSearch(opts.indexOf('v') >= 0);
		SearchAndReplace.setWholeWord(opts.indexOf('w') >= 0);
		SearchAndReplace.setIgnoreCase(opts.indexOf('i') >= 0);
		SearchAndReplace.setRegexp(opts.indexOf('r') >= 0);
		SearchAndReplace.setSearchFileSet(new CurrentBufferSet());
		boolean result = SearchAndReplace.find(view);

		SearchAndReplace.setAutoWrapAround(aw);
		SearchAndReplace.setSearchString(oldSearch);
		SearchAndReplace.setIgnoreCase(ic);
		SearchAndReplace.setRegexp(re);
		SearchAndReplace.setWholeWord(ww);
		SearchAndReplace.setBeanShellReplace(bs);
		return result;
	}

	public Buffer openTempBuffer(){
		final EditPane editPane = view.getEditPane();

		//if is not selection take all textArea
		if(selectedText.trim().equals("")){
			textArea.selectAll();
			selectedText = textArea.getSelectedText();
		}
		Buffer bfTmp = BufferSetManager.createUntitledBuffer();
		bfTmp.insert(0, selectedText);
		editPane.setBuffer(bfTmp);
		return bfTmp;
	}

	public void closeTempBuffer(Buffer bfTmp){
		if(!isJUnitTest()) {
			//TODO: se puede poner textArea.setBuffer(bfTmp) en vez de esto:
			textArea.selectAll();
			selectedText = textArea.getSelectedText();

			jEdit._closeBuffer(view, bfTmp);
			textArea.setSelectedText(selectedText);
			Registers.setRegister('$', selectedText);
		}
	}

	public String iniSelectedText(){
		String t = textArea.getSelectedText();

		if(t=="" || t==null){
			textArea.selectAll();
			t=textArea.getSelectedText();
		}
		prevSelection = textArea.getSelection();

		return t;
	}

	public void endSelectedText(String t){
		textArea.setSelection(prevSelection);
		textArea.setSelectedText(t);
		Registers.setRegister('$', t);
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

			switch(methodName) {
			case "sortLines":
				method = mainClass.getMethod(methodName, JEditTextArea.class, boolean.class);
				method.invoke(mainClass, textArea, false);
				break;
			case "transposeLines":
				method = mainClass.getMethod(methodName, JEditTextArea.class);
				method.invoke(mainClass, textArea);
				break;
			default:
				method = mainClass.getMethod(methodName, View.class, JEditTextArea.class);
				method.invoke(mainClass, view, textArea);
				break;
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
	 * @example
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
	public String replaceAccent(String t){
		for(int i=0; i<ARR_CHARS.length; i++){
			t=t.replace(ARR_CHARS[i][0], ARR_CHARS[i][4]);
			if(ARR_CHARS[i][0].equals(LOW_ENIE)){
				break;
			}
		}

		return t;
	}

	/**
	 * Search backward in text area with selected text expression
	 */
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

	/**
	 * Enclose selected text with prefix or suffix with prefix and suffix 
	 * @param prefix char before text
	 * @param suffix char after text
	 */
	public void encloseText(char prefix, char suffix) {
		String s = textArea.getSelectedText();
		if(s != null){
			Selection[] selection = textArea.getSelection();
			Selection select0 = selection[0];

			selection[0] = new Range(new Rect(select0.getStartLine(), select0.getStart() + 1, select0.getEndLine(), select0.getEnd() + 1));
			textArea.setSelectedText(prefix + s + suffix);
			textArea.setSelection(selection);
		}else{
			textArea.setSelectedText(prefix + "" + suffix);
			textArea.goToPrevCharacter(false);
		}
	}
	
	private boolean isJUnitTest() {
	    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
	    List<StackTraceElement> list = Arrays.asList(stackTrace);
	    for (StackTraceElement element : list) {
	        if (element.getClassName().startsWith("org.junit.")) {
	            return true;
	        }           
	    }
	    return false;
	}
}
