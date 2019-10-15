/**
 * @author Richard Martinez 2019/03/18
 * class InspectionFile
 * Check each second when current file is changed
 */
package masterraise.notify;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;
import javax.swing.Timer;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.Macros;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.gui.FilesChangedDialog;

public abstract class File{
	static Timer timer = null;

	public static void start(View view){
		Buffer buffer = view.getBuffer();

		//TODO:VERIFICAR SI SE PUEDE MOVER ESTO A InspectionWidgetFactory
		timer = new Timer(1000, new ActionListener(){
			public void actionPerformed(ActionEvent e){
				int fileStatus = buffer.checkFileStatus(view);
				if(fileStatus == Buffer.FILE_CHANGED || fileStatus == Buffer.FILE_DELETED){
					if(!buffer.isDirty()){
						if(buffer.isClosed()){
							jEdit.openFile(view, buffer.getPath());
							view.getEditPane().recentBuffer();
						}

						Buffer[] buffers = view.getBuffers();
						int i = 0;
						for (i = 0; i < buffers.length; i++) {
							if(buffers[i].getPath().equals(buffer.getPath())){
								break;
							}
						}
						int[] states = new int[buffers.length];
						states[i] = Buffer.FILE_CHANGED;
						new FilesChangedDialog(view,states,true); 
					}
					Notify.displayMessage(buffer.getPath());
				}
			}
		});

		if(buffer.isUntitled()){
			Macros.error(view, "Cannot inspect this file, you must to saved to doit");
			return;
		}

		if(Notify.getDetail(buffer.getPath()) != null){
			Macros.error(view, "Inspection is running for this file");
			return;
		}
		
		if(Macros.confirm(view, "Do you want inspect if this current File will change?", JOptionPane.YES_NO_OPTION)==0){
			new Notify().registerInspect(buffer.getName()
					, buffer.getPath()
					, timer
					, "FILE");
			timer.start();
		}
	}
}