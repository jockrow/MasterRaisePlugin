package masterraise.tools;

import java.util.ArrayList;
import java.util.Arrays;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.Macros;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.io.VFSManager;
import org.gjt.sp.jedit.textarea.TextArea;

public class Bulk {
	private View view = jEdit.getActiveView();
	private TextArea textArea = view.getTextArea();
	
	private ArrayList<String> filesNotFound = new ArrayList<String>();
	private Buffer[] bf = jEdit.getBuffers();

	public void insertFileSelection(){
		String selectedText = textArea.getSelectedText() == null ? "" : textArea.getSelectedText().replaceAll("(?m)^[\t ]*\n", "").trim();
		if(selectedText.length() == 0){
			Macros.message(view, "Must select a valid text");
			return;
		}

		String[] lines = selectedText.split("\\r?\\n");
		for (int l = 0; l < lines.length; l++){
			openSelected(lines[l].trim());
		}

		for(String file: filesNotFound){
			VFSManager.error(view,file,"no-read.title", null);
		}
	}

	private void openSelected(String line){
		Buffer b = jEdit.openTemporary(view,null,line,false);
		try{
			if(b == null){
				return;
			}
			while(!b.isLoaded()){
				VFSManager.waitForRequests();
			}
			if(b.getLength() > 0){
				view.getTextArea().setSelectedText("_______________" + line + " {{{ \n" 
						+ b.getText(0,b.getLength()) 
						+ "\n_______________" + line + " }}}\n\n");
			}
			else{
				filesNotFound.add(line);
			}
		}
		finally{
			if(b != null && !Arrays.asList(bf).contains(b)){
				jEdit._closeBuffer(null, b);
			}
		}
	}
}
