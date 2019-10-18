/**
 * @author Richard Martinez 2019/02/21     
 * class InspectionPixel
 * Check if a pixel is changed
 */
package masterraise.notify;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.Timer;

import org.gjt.sp.jedit.Macros;
import org.gjt.sp.jedit.Registers;
import org.gjt.sp.jedit.View;

public abstract class Pixel{
	static Timer timer = null;
	static Color c = null;
	static String currentDetail = "";
	
	public static void start(View view){
		try {
			new InspectionDialog(view);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
	
	private static class InspectionDialog extends JWindow{
		private static final long serialVersionUID = 1258379208114033718L;
		Color lastColor = null;
		Timer timerWaitChange = null;
		Point loc = null;

		private InspectionDialog(View view) throws AWTException{
			super(view);
			
			Robot robbie = new Robot();
			JTextField bOld = new JTextField("", 10);
			JTextField bNew = new JTextField("", 10);

			bOld.setHorizontalAlignment(JTextField.CENTER);
			bOld.setEditable(false);
			bNew.setHorizontalAlignment(JTextField.CENTER);

			add(bOld, BorderLayout.NORTH);
			setAlwaysOnTop(true);
			pack();

			Timer timer = new Timer(100, new ActionListener(){
				public void actionPerformed(ActionEvent e){
					if(!isVisible()) setVisible(true);
					Point p = MouseInfo.getPointerInfo().getLocation();
					setLocation(p.x+4, p.y+22);
					c = robbie.getPixelColor(p.x, p.y);
					if(!c.equals(lastColor)){
						setContentColor(bOld);
					}
					lastColor = c;
				}
			});

			// TODO:Invocar desde mi Color
			timerWaitChange = new Timer(1000, new ActionListener(){
				public void actionPerformed(ActionEvent e){
					setLocation(loc.x+4, loc.y+22);
					c = robbie.getPixelColor(loc.x, loc.y);
					if(!c.equals(lastColor)){
						setContentColor(bNew);
						add(bNew, BorderLayout.SOUTH);
						Registers.setRegister('$', bNew.getText());
						pack();
						setVisible(true);

						//TODO:if there is more than two pixels must to quit for inspects table
						Notify.displayMessage(currentDetail);
						dispose();
					}
					lastColor = c;
				}
			});
					
			bOld.addKeyListener(new KeyListener() {
				@Override
				public void keyReleased(KeyEvent e) {
					if(e.getKeyCode() == KeyEvent.VK_ESCAPE){
						timer.stop();
						dispose();
					}
					else if(e.getKeyCode() == KeyEvent.VK_ENTER){
						timer.stop();
						setVisible(false);
						loc = MouseInfo.getPointerInfo().getLocation();
						currentDetail = "Position x:" + loc.x + " y:" + loc.y;
						timerWaitChange.start();
						
						new Notify().registerInspect(bOld.getText()
								, currentDetail
								, timerWaitChange
								, "PIXEL");
						Macros.message(view, "Waiting for pixel changed");
					}
				}
				
				@Override
				public void keyPressed(KeyEvent e) {}
				
				@Override
				public void keyTyped(KeyEvent e) {}
			});

			bOld.addFocusListener(new FocusListener() {
				@Override
				public void focusLost(FocusEvent e) {
					timer.stop();
					dispose();
				}
				
				@Override
				public void focusGained(FocusEvent e) {}
			});
			
			timer.start();
			bOld.requestFocus();
		}
	}
	
	private static void setContentColor(JTextField tf){
		tf.setBackground(new Color(c.getRed(), c.getGreen(), c.getBlue()));
		tf.setForeground(new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue()));

		StringBuffer sb = new StringBuffer("#");
		int[] rgb = new int[] { c.getRed(), c.getGreen(), c.getBlue() };
		for(int j=0; j < rgb.length; j++){
			if(rgb[j] <= 15) sb.append("0");
			sb.append(Integer.toHexString(rgb[j]).toUpperCase());
		}
		tf.setText(sb.toString());
	}
}