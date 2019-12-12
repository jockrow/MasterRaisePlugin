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

import javax.swing.JOptionPane;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.Macros;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.browser.VFSBrowser;
import org.gjt.sp.jedit.browser.VFSFileChooserDialog;
import org.gjt.sp.jedit.gui.DockableWindowManager;
import org.gjt.sp.jedit.gui.StatusBar;

import masterraise.Text;
import masterraise.tools.Bulk;

/**
 * Tools for Files
 * @author Richard Martínez 2018
 *
 */
public class MrFile extends Text {
	/**
	 * move a file to another path
	 * @param currentPath current path for file
	 * @param newPath new path to move
	 * @return true if successfully
	 */
	public boolean moveFile(String currentPath, String newPath){
		File oldFile = new File(currentPath);
		File newFile = new File(newPath);

		try{
			org.apache.commons.io.FileUtils.moveFile(oldFile, newFile);
		}catch (IOException e){
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * create a file
	 * @param path Path to create the file
	 * @param body content for file
	 * @throws IOException
	 */
	public void createFile(String path, String body) throws IOException {
		BufferedWriter writer = null;
		File folder = new File(path.replaceAll("/\\*?\\w+(\\.\\w+)?$", ""));

		if(!folder.exists()) {
			folder.mkdir();
		}

		if(body.trim().equals("")) {
			new File(path).createNewFile();
			return;
		}
		writer = new BufferedWriter(new FileWriter(path));
		writer.write(body);
		writer.close();
	}

	/**
	 * Delete a file
	 * @param path current path for file 
	 * @param fileName File Name
	 */
	public void deleteFile(String path, String fileName){
		new File(path, fileName).delete();
	}

	/**
	 * List a complete content from directory
	 * @param path full path
	 * @param filter filter to apply
	 * @return array content directory, if doesn't set will show completed content
	 */
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

				if(countOccurrences(fileName, filter, "ir") > 0){
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

	/**
	 * read content for a file
	 * @param pathFile complete path location file
	 * @return content file
	 */
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

	/**
	 * Open Folder with Explorer
	 * @param openPath path to open
	 */
	public void openFolder(String openPath){
		String fileExplorer = "cmd /c start explorer";

		try {
			Runtime.getRuntime().exec(fileExplorer + " \"" + openPath + "\"");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
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

	/*
	 * delete current buffer in jEdit
	 */
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
	 * Create a directories and files structure, from a structure tabs in textBox
	 * @example
	 * <pre>
	 * {@code
	 * Note: * is file
	 *uno f
	 *	*unoF.txt
	 *	uno1
	 *	uno2
	 *	uno3
	 *dos
	 *	*dosF1.txt
	 *	*dosF2.txt
	 *	dosdos
	 *		*dosdosF
	 *		dosdos1
	 *		dosdos2
	 *	dos1
	 *		*dos1F.txt
	 *tres
	 *	*tresF.txt
	 */
	public void createFileStructure(){
		if(!findBuffer("\\p{Alnum}", "air")){
			Macros.error(view, "The textArea not must be empty");
			return;
		}
		else if(findBuffer("[\\\\/:\\?\"<>\\|]", "air")){
			Macros.error(view, "Don't must have this characters:\n \\ / : ? \" < >  |");
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
			textArea.shiftIndentLeft();
		}

		String optimizedText = textArea.getText();
		int maxTabs = 0;
		String tab = "\\t";

		// Check the max tabs number
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

			//TODO:siempre aparece el mensaje de confirmación cuando es verdadero
			if(!findBuffer(tab+"\\t", "ir")){
				maxTabs--;
				tab = tab.replaceAll("^..", "");
			}
		}

		String[] arrStructure = textArea.getText().split("\n");
		for(int i=0; i<arrStructure.length; i++){
			String path = (parentPath + "/" + arrStructure[i]);

			//Create Files
			if(path.indexOf("*") >= 0) {
				try {
					createFile(path.replace("*", ""), "");
				} catch (IOException e) {
					e.printStackTrace();
				}	
			}
			//Create Directories
			else {
				new File(path).mkdir();
			}
		}

		if(!isOpenConsole){
			mgr.toggleDockableWindow("console");
		}

		textArea.setText(optimizedText);
		VFSBrowser.browseDirectory(view, parentPath);

		openFolder(parentPath);
	}

	/**
	 * Copy all opened buffers in new buffer
	 */
	public void joinBuffers(){
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
		
		//TODO:PROBAR NUEVAMENTE
//		textArea.selectAll();
		new Bulk().insertFileSelection();
	}
}