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

public class jEditFile{
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
		try {
			writer = new BufferedWriter(new FileWriter(pathFile));
			writer.write(body);
		} catch(IOException ex) {
			ex.printStackTrace();
			return false;
		} finally {
			writer.close();
		}

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
}