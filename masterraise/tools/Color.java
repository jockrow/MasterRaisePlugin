/**********************************************
 *     @author Richard Martínez 2019/03/08     *
 ***********************************************/
package masterraise.tools;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JColorChooser;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.Timer;

import org.gjt.sp.jedit.Registers;
import org.gjt.sp.util.SyntaxUtilities;

import masterraise.MrPopup;
import masterraise.Text;

public class Color extends Text{
	java.awt.Color lastColor = null;
	java.awt.Color c = null;
	Robot robbie = null;
	String mode = "HEX";

	public void colorPalette() {
		if (selectedText != null) {
			c = SyntaxUtilities.parseColor(selectedText, c);
		}
		
		c = JColorChooser.showDialog(view, "Color Palette", c);

		if (c != null){
			textArea.setSelectedText(SyntaxUtilities.getColorHexString(c));
		}
	}
	
	/**
	 * Get RGB or Hexadecimal color from pixel
	 *
	 * USE:
	 *	1. Press UP or DOWN to toggle RGB/Hexadecimal values
	 *	2. Press ENTER to copy the current value to the clipboard and stop.
	 *	3. Press ESCAPE to stop without copying.
	 */
	public void colorPicker(){
		try {
			robbie = new Robot();
		} catch (AWTException e1) {
			e1.printStackTrace();
		}
		JTextField b = new JTextField("", 14);
		JWindow win = new JWindow(view);
		MrPopup mp = new MrPopup("<HtMl>Get Screen Color USE:<br>1.Press \u25B2 or \u25BC to toggle RGB/Hexadecimal values <br>2.Press ENTER to copy the current value to the clipboard and stop <br>3.Press ESCAPE to stop without copying");
		
		Timer timer = new Timer(100, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if(!win.isVisible()) win.setVisible(true);
				Point p = MouseInfo.getPointerInfo().getLocation();
				win.setLocation(p.x+4, p.y+22);
				c = robbie.getPixelColor(p.x, p.y);
				if(!c.equals(lastColor)){
					if(mode.equals("HEX")){
						b.setText(toHexString(c));
					}
					else{
						b.setText(c.getRed() + ", " + c.getGreen() + ", " + c.getBlue());
					}
					b.setBackground(new java.awt.Color(c.getRed(), c.getGreen(), c.getBlue()));
					b.setForeground(new java.awt.Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue()));
				}
				lastColor = c;
			}
		});
		
		KeyListener ka = new KeyListener(){
			@Override
			public void keyReleased(KeyEvent e){
				switch(e.getKeyCode()){
				case KeyEvent.VK_ESCAPE:
					timer.stop();
					win.dispose();
					mp.dispose();
					break;
				case KeyEvent.VK_ENTER:
					timer.stop();
					Registers.setRegister('$', b.getText());
					win.setVisible(false);
					mp.dispose();
					break;
				case KeyEvent.VK_UP: case KeyEvent.VK_DOWN:
					mode = mode.equals("HEX") ? "RGB" : "HEX";
					lastColor = null;
					break;
				}
			}

			@Override
			public void keyTyped(KeyEvent e) { }

			@Override
			public void keyPressed(KeyEvent e) { }
		};

		b.setHorizontalAlignment(JTextField.CENTER);
		b.setEditable(false);

		win.add(b, BorderLayout.WEST);
		win.setAlwaysOnTop(true);
		win.pack();
		mp.startSlideIn();
		
		b.addKeyListener(ka);
		
		timer.start();
		b.requestFocus();
	}

	private String toHexString(java.awt.Color c){
		StringBuffer sb = new StringBuffer("#");
		int[] rgb = new int[] { c.getRed(), c.getGreen(), c.getBlue() };
		for(int j=0; j < rgb.length; j++){
			if(rgb[j] <= 15) sb.append("0");
			sb.append(Integer.toHexString(rgb[j]).toUpperCase());
		}
		return sb.toString();
	}
}
