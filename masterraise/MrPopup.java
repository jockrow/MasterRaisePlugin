package masterraise;

import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JWindow;
import javax.swing.Timer;

import org.gjt.sp.jedit.GUIUtilities;

public final class MrPopup extends JWindow implements PropertyChangeListener, HierarchyListener {
	private static final long serialVersionUID = -2041980900427732575L;
	public static final int DELAY = 5;
	public static final int STEP = 3;
	private final Timer animator = new Timer(DELAY, null);
	private transient ActionListener listener;
	private int dx;
	private int dy;
	private String message;
	
	javax.swing.Timer timerTip = new javax.swing.Timer (8000, new ActionListener (){
		public void actionPerformed(ActionEvent e){
			dispose();
		}
	});

	public MrPopup(String message) {
		this.message = message;
	}

	public void startSlideIn() {
		if (animator.isRunning()) {
			return;
		}
		if (isVisible()) {
			setVisible(false);
			getContentPane().removeAll();
		}

		setSize(390, 100);
		setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.gridx = 0;
		constraints.gridy = 0;
		constraints.weightx = 1.0f;
		constraints.weighty = 1.0f;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.fill = GridBagConstraints.BOTH;
		JLabel headingLabel = new JLabel("NOTE:");
		headingLabel.setIcon(GUIUtilities.loadIcon("22x22/apps/information.png"));
		headingLabel.setOpaque(false);
		add(headingLabel, constraints);
		constraints.gridx++;
		constraints.weightx = 0f;
		constraints.weighty = 0f;
		constraints.fill = GridBagConstraints.NONE;
		constraints.anchor = GridBagConstraints.NORTH;

		JButton closesButton = new JButton("X");
		closesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				dispose();
			}
		});

		closesButton.setMargin(new Insets(1, 4, 1, 4));
		closesButton.setFocusable(false);
		add(closesButton, constraints);

		constraints.gridx = 0;
		constraints.gridy++;
		constraints.weightx = 1.0f;
		constraints.weighty = 1.0f;
		constraints.insets = new Insets(5, 5, 5, 5);
		constraints.fill = GridBagConstraints.BOTH;
		
		JLabel messageLabel = new JLabel(this.message);
		add(messageLabel, constraints);

		Dimension d = getContentPane().getPreferredSize();
		GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
		Rectangle desktopBounds = env.getMaximumWindowBounds();
		dx = desktopBounds.width - getWidth();
		dy = desktopBounds.height;
		setLocation(new Point(dx, dy));

		setVisible(true);
		setAlwaysOnTop(true);
		animator.removeActionListener(listener);
		AtomicInteger count = new AtomicInteger();
		listener = e -> {
			double v = count.addAndGet(STEP) / (double) d.height;
			double a = AnimationUtil.easeOut(v);
			int visibleHeight = (int) (.5 + a * d.height);
			if (visibleHeight >= d.height) {
				visibleHeight = d.height;
				animator.stop();
			}
			setLocation(new Point(dx, dy - visibleHeight));
		};
		animator.addActionListener(listener);
		animator.start();
		timerTip.start();
	}

	@Override
	public void propertyChange(PropertyChangeEvent e) {
		if (isVisible() && Objects.nonNull(e.getNewValue())
				&& e.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
			setVisible(false);
			getContentPane().removeAll();
		}
	}

	@Override
	public void hierarchyChanged(HierarchyEvent e) {
		if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0
				&& !e.getComponent().isDisplayable()) {
			animator.stop();
		}
	}
}

final class AnimationUtil {
	private static final int N = 3;

	public static double easeIn(double t) {
		return Math.pow(t, N);
	}

	public static double easeOut(double t) {
		return Math.pow(t - 1d, N) + 1d;
	}

	public static double easeInOut(double t) {
		double ret;
		boolean isFirstHalf = t < .5;
		if (isFirstHalf) {
			ret = .5 * intpow(t * 2d, N);
		} else {
			ret = .5 * (intpow(t * 2d - 2d, N) + 2d);
		}
		return ret;
	}

	public static double intpow(double da, int ib) {
		int b = ib;
		if (b < 0) {
			throw new IllegalArgumentException(
					"B must be a positive integer or zero");
		}
		double a = da;
		double d = 1d;
		for (; b > 0; a *= a, b >>>= 1) {
			if ((b & 1) != 0) {
				d *= a;
			}
		}
		return d;
	}
}