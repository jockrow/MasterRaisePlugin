/**********************************************/
/*      Develop by Richard Martinez 2018      */
/**********************************************/
package masterraise.files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.EditPane;
import org.gjt.sp.jedit.Macros;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.browser.VFSFileChooserDialog;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.gui.StatusBar;
import org.gjt.sp.jedit.textarea.TextArea;

import masterraise.Edit;
import masterraise.tools.Bulk;

public class MrFile extends Edit{
	private View view = jEdit.getActiveView();
	private Buffer buffer = view.getBuffer();
	private TextArea textArea = view.getTextArea();
	private String selectedText = textArea.getSelectedText() == null ? "" : textArea.getSelectedText();

	public boolean moveFile(String oldPathFile, String newPathFile){
		File oldFile = new File(oldPathFile);
		File newFile = new File(newPathFile);

		try{
			org.apache.commons.io.FileUtils.moveFile(oldFile, newFile);
		}catch (IOException e){
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public boolean createFile(String pathFile, String body) throws IOException{
		BufferedWriter writer = null;
		//		try {
		writer = new BufferedWriter(new FileWriter(pathFile));
		writer.write(body);
		//		} catch(IOException ex) {
		//			ex.printStackTrace();
		//			return false;
		//		} finally {
		writer.close();
		//		}

		return true;
	}

	public void deleteFile(String directory, String fileName){
		new File(directory, fileName).delete();
	}

	public File[] dir(String path, String filter){
		List<File> listDir = new ArrayList<File>();
		FileVisitor<Path> simpleFileVisitor = new SimpleFileVisitor<Path>() {
			@Override
			public FileVisitResult preVisitDirectory(Path dir,BasicFileAttributes attrs)
					throws IOException {
				if(!dir.toFile().getPath().replaceAll("[\\\\/]", "/").equals(path.replaceAll("[\\\\/]", "/"))){
					listDir.add(new File(dir.toFile().getPath()));
				}

				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path visitedFile,BasicFileAttributes fileAttributes)
					throws IOException {
				String fileName = visitedFile.getFileName().toString();

				if(Pattern.compile("(?i)" + filter.replace(".", "\\.").replace("*", ".*")).matcher(fileName).find()){
					listDir.add(visitedFile.toFile());
				}

				return FileVisitResult.CONTINUE;
			}
		};
		FileSystem fileSystem = FileSystems.getDefault();
		Path rootPath = fileSystem.getPath(path);

		try {
			Files.walkFileTree(rootPath, simpleFileVisitor);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return (File[])listDir.toArray(new File[listDir.size()]);
	}

	public String readFile(String pathFile){
		String body = "";
		try (BufferedReader br = new BufferedReader(new FileReader(pathFile))) {
			String sCurrentLine;

			while ((sCurrentLine = br.readLine()) != null) {
				body += sCurrentLine + "\n";
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return body;
	}

	public void openFolder(String openPath){
		boolean isWindows = System.getProperty("os.name").indexOf("Linux") < 0;
		String fileExplorer = "cmd /c start \"\"";

		if(!isWindows){
			fileExplorer = "nemo";
		}

		try {
			Runtime.getRuntime().exec(fileExplorer + " \"" + openPath + "\"");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Method openSelection()
	 * Open the correct selection for path file
	 */
	public void openSelection(){
		selectedText = selectedText.trim();
		StatusBar sb = view.getStatus();
		if(selectedText.equals("")){
			sb.setMessageAndClear("Must to select something");
			return;
		}

		File f = new File(selectedText);
		if(!f.exists()){
			sb.setMessageAndClear("File or Folder not Found");
			return;
		}

		if(f.isDirectory()){
			openFolder(selectedText);
		}
		else{
			jEdit.openFile(view,selectedText);
		}
	}

	public void deleteCurrentBuffer(){
		if(buffer.isNewFile()){
			Macros.error(view, "Buffer doesn't exist on disk." );
			return;
		}

		if(Macros.confirm(view, "Are you sure delete this file?", JOptionPane.YES_NO_OPTION)==0){
			deleteFile(buffer.getDirectory(), buffer.getName());
			jEdit.checkBufferStatus(view);
			view.getStatus().setMessageAndClear("Deleted: " + buffer.getDirectory() + SEP + buffer.getName());
		}
	}

	/**
	 * Method createFileStructure()
	 * Crea una estructura de directorios y archivos, de un buffer con una estructura de tabulaciones

	uno f
	 *unoF.txt
		uno1
		uno2
		uno3
	dos
	 *dosF1.txt
	 *dosF2.txt
		dosdos
	 *dosdosF
			dosdos1
			dosdos2
		dos1
	 *dos1F.txt
	tres
	 *tresF.txt

		NOTE: * is file
	 */
	public void createFileStructure(){
		if(!findBuffer("\\p{Alnum}", "air")){
			Macros.message(view, "The textArea not must be empty");
			return;
		}
		else if(findBuffer("[\\\\/:\\?\"<>\\|]", "air")){
			Macros.message(view, "Don't must have this characters:\n \\ / : ? \" < >  |");
			return;
		}

		DockableWindowManager mgr = jEdit.getActiveView().getDockableWindowManager();
		boolean isOpenConsole = mgr.isDockableWindowVisible("console");
		VFSFileChooserDialog fileChooser = new VFSFileChooserDialog(view, null, VFSBrowser.CHOOSE_DIRECTORY_DIALOG, true);
		String[] arrDirectory = fileChooser.getSelectedFiles();

		if(arrDirectory == null){
			return;
		}

		String parentPath = arrDirectory[0];
		String parent = "";

		replaceBuffer("(" + TRIM_UP + ")(^\\t+\\w)", "$2", "r");
		replaceBuffer(TRIM_RIGHT + "|" + TRIM_DOWN, "", "r");

		//trim left with indent
		while(findBuffer("\\A^\\t+\\w", "ar")){
			textArea.selectAll();
			textArea.shiftIndentLeft();
		}

		textArea.selectAll();
		String optimizedText = textArea.getSelectedText();

		int maxTabs = 0;
		String tab = "\\t";

		// Mira el número máximo de tabulaciones
		while(findBuffer(tab, "ar")){
			tab += "\\t";
			maxTabs++;
		}
		tab = tab.replaceAll("^....", "");

		//create directory structure
		while(maxTabs > 0){
			textArea.goToBufferStart(false);
			String getSons = "(^\\t{" + (maxTabs-1) + "," + (maxTabs-1) + "}(\\w+[ ]*)+)(\\n(^\\t{" + maxTabs + "," + maxTabs + "}(\\**\\w+[ \\./]*)+))+";
			findBuffer(getSons, "r");
			textArea.goToStartOfWhiteSpace(false);
			textArea.goToEndOfWhiteSpace(true);
			parent = textArea.getSelectedText();
			textArea.goToPrevLine(false);
			findBuffer(getSons, "ir");
			replaceSelection("(^\\t{" + maxTabs + "})((\\p{Print}+[ ]*)+)", tab + parent + "/$2", "ir");
			textArea.goToStartOfWhiteSpace(false);
			textArea.deleteLine();

			if(!findBuffer(tab+"\\t", "ir")){
				maxTabs--;
				tab = tab.replaceAll("^..", "");
			}
		}

		//Find Files
		String[] arrFiles = null;
		//TODO:siempre aparece el mensaje de confirmación
		if(findBuffer("*", "a")){
			textArea.selectAll();
			String tree = textArea.getSelectedText();
			replaceBuffer("^.*/\\w+$\\n", "", "r");
			replaceBuffer("*", "", "wi");
			arrFiles = textArea.getSelectedText().split("\n");
			textArea.setSelectedText(tree);
			replaceBuffer("\\*.*", "", "r");
		}

		//Create Directories
		textArea.selectAll();
		String[] arrStructure = textArea.getSelectedText().split("\n");
		for(int i=0; i<arrStructure.length; i++){
			new File(parentPath + "/" + arrStructure[i]).mkdirs();
		}

		//Create Files
		for(int i=0; i<arrFiles.length; i++){
			try {
				createFile(parentPath + "/" + arrFiles[i], "");
			} catch (IOException e) {
				System.out.println("[error] " + e.getMessage());
			}
		}

		if(!isOpenConsole){
			mgr.toggleDockableWindow("console");
		}

		textArea.selectAll();
		textArea.setSelectedText(optimizedText);
		VFSBrowser.browseDirectory(view, parentPath);

		openFolder(parentPath);
	}

	/**
	 * Method joinBuffers()
	 * Copy all opened buffers in new buffer
	 */
	public void joinBuffers(){
		EditPane editPane = view.getEditPane(); 
		Buffer[] bf = jEdit.getBuffers();
		String strCurrentBuffer = "";
		int current = buffer.getIndex();

		if(Macros.confirm(view, "Do you want put each file name like a titles at New file? \nWarning, the Untitled doesn't will show in result", JOptionPane.YES_NO_OPTION) != 0){
			return;
		}

		for(int i=0; i<bf.length; i++){
			if(!bf[current].isNewFile() && bf[current].toString().indexOf("archive:") < 0 ){
				strCurrentBuffer += bf[current].getPath() + "\n";
			}
			current++;
			if(current>=bf.length){
				current = 0;
			}
			editPane.nextBuffer();
		}

		// Open new buffer and paste the content from opened Buffers
		jEdit.newFile(view);
		textArea.setText(strCurrentBuffer);
		sortLines(textArea);
		textArea.selectAll();
		new Bulk().insertFileSelection();
	}
}