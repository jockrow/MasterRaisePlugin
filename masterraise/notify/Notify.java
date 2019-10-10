/**
* @author Richard Martinez
* class Inspect
* Aditional functions in Status Bar
*/
package masterraise.notify;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.swing.Timer;

import org.gjt.sp.jedit.Buffer;
import org.gjt.sp.jedit.Macros;
import org.gjt.sp.jedit.MiscUtilities;
import org.gjt.sp.jedit.View;
import org.gjt.sp.jedit.jEdit;
import org.gjt.sp.jedit.textarea.TextArea;

public class Notify{
	public static ArrayList<Detail> list = new ArrayList<Detail>();
	private static Buffer logBuffer = null;
	public final static DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd h:mm:ss a");

	public static class Detail{
		private String inspect = "";
		private String location = "";
		private Timer timer;
		private String type = "";
		private Date dateHour = new Date();
		private int minutes = 0;
		private int seconds = 60;

		Detail(String inspect, String location, Timer timer, String type){
			this.inspect = inspect;
			this.location = location;
			this.timer = timer;
			this.type = type;
		}

		public String getInspect() {
			return inspect;
		}

		public String getLocation() {
			return location;
		}

		public Timer getTimer() {
			return timer;
		}

		public String getType() {
			return type;
		}

		public Date getDateHour() {
			return dateHour;
		}
		
		public int getMinutes() {
			return minutes;
		}

		public void setMinutes(int minutes) {
			this.minutes = minutes;
		}

		public int getSeconds() {
			return seconds;
		}

		public void setSeconds(int seconds) {
			this.seconds = seconds;
		}

		public void setInspect(String inspect) {
			this.inspect = inspect;
		}
	}
	
	public void registerInspect(String inspect, String location, Timer timer, String type){
		list.add(new Detail(inspect, location, timer, type));
	}
	
	public static int unregisterInspect(String location){
		int i = 0;
		
		for(Iterator<Detail> det = list.iterator(); det.hasNext();){
			Detail row = det.next();
			if(row.getLocation().equals(location)){
				row.getTimer().stop();
				det.remove();
				break;
			}
			i++;
		}
		return i;
	}

	public static Detail getDetail(String location){
		for(Iterator<Detail> det = list.iterator(); det.hasNext();){
			Detail inspect = det.next();
			if(location.equals(inspect.getLocation())){
				return inspect;
			}
		}
		return null;
	}
	
	public static void displayMessage(String location){
		View view = jEdit.getActiveView();
		TextArea textArea = view.getTextArea();
		Detail di = getDetail(location);
		Date startHour = di.getDateHour();
		Date finishHour = new Date();
		String msgElapsedTime = "";

		long diff = finishHour.getTime() - startHour.getTime();
		long diffDays = diff / (24 * 60 * 60 * 1000);
		long diffHours = diff / (60 * 60 * 1000) % 24;
		long diffMinutes = diff / (60 * 1000) % 60;
		long diffSeconds = diff / 1000 % 60;
		
		if(diffDays > 0){
			msgElapsedTime += " " + diffDays + " days";
		}
		if(diffHours > 0){
			msgElapsedTime += " " + diffHours + " hours";
		}
		if(diffMinutes > 0){
			msgElapsedTime += " " + diffMinutes + " minutes";
		}
		if(diffSeconds > 0){
			msgElapsedTime += " " + diffSeconds + " seconds";
		}

		String message = di.getType() + ": " + location
				+ "\nStart Hour: " + dateFormat.format(di.getDateHour())
				+ "\nFinish Hour: " + dateFormat.format(finishHour)
				+ "\nElapsed Time:" + msgElapsedTime;

		if(logBuffer == null){
			logBuffer = jEdit.openFile((View) null,null,
						MiscUtilities.constructPath(jEdit.getSettingsDirectory(),"macros",
						"logInspector.log"),true,null);
			view.goToBuffer(logBuffer);
		}
		else{
			view.goToBuffer(logBuffer);
			textArea.goToBufferEnd(false);
		}
		
		textArea.setSelectedText(message + "\n\n");
		view.getEditPane().recentBuffer();

		unregisterInspect(location);
		Macros.message(view, message);
	}
}
