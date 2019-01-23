package engine;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import rec.robotino.api2.DistanceSensor;

/**
 * This widget provides basic controls to drive Robotino.
 */
@SuppressWarnings("serial")
public class DriveWidget extends JComponent {
	protected static final float speed = 0.09f;
	protected static final float rotSpeed = 2f;

	static boolean checkDistance = true;

	protected final Robot robot;

	protected static double inicialXParam = 0;
	protected static double inicialYParam = 0;

	static final List<DistanceSensor> _distanceSensors = new ArrayList<DistanceSensor>();

	Timer _timer;
	private float vx;
	private float vy;
	private float omega;

	public DriveWidget(Robot robot) {
		this.robot = robot;
		for (int i = 0; i < 9; ++i) {
			DistanceSensor s = new DistanceSensor();
			s.setSensorNumber(i);
			s.setComId(robot._com.id());
			_distanceSensors.add(s);
		}

		setLayout(new GridLayout(3, 5));

		JButton buttonUp = new JButton(getIcon("n"));
		robot.addListener(new RobotListenerImpl(this));
		JButton buttonDown = new JButton(getIcon("s"));
		JButton buttonLeft = new JButton(getIcon("o"));
		JButton buttonRight = new JButton(getIcon("w"));
		JButton buttonCL = new JButton(getIcon("cl"));
		JButton buttonCCL = new JButton(getIcon("ccl"));
		JButton buttonStop = new JButton(getIcon("stop"));

		buttonUp.addActionListener(new ButtonListener(speed, 0.0f, 0.0f, this));
		buttonDown.addActionListener(new ButtonListener(-speed, 0.0f, 0.0f, this));
		buttonLeft.addActionListener(new ButtonListener(0.0f, -speed, 0.0f, this));
		robot.addListener(new RobotListenerImpl(this));
		buttonRight.addActionListener(new ButtonListener(0.0f, speed, 0.0f, this));
		buttonCL.addActionListener(new ButtonListener(0.0f, 0.0f, -rotSpeed, this));
		buttonCCL.addActionListener(new ButtonListener(0.0f, 0.0f, rotSpeed, this));
		buttonStop.addActionListener(new ButtonListener(0.0f, 0.0f, 0.0f, this));

		add(new JLabel());
		add(new JLabel());
		add(buttonUp);
		add(new JLabel());
		add(new JLabel());
		add(new JLabel());
		add(new JLabel());
		add(buttonCCL);
		add(buttonRight);
		add(buttonStop);
		add(buttonLeft);
		add(buttonCL);
		add(buttonDown);

		setMinimumSize(new Dimension(60, 30));
		setPreferredSize(new Dimension(200, 120));
		setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

		_timer = new Timer();
		_timer.scheduleAtFixedRate(new OnTimeOut(), 0, 20);
		robot._odometry.set(0.0, 0.0, 0.0);
	}

	public void setVelocity(float vx, float vy, float omega) {
		this.vx = vx;
		this.vy = vy;
		this.omega = omega;
		robot.setVelocity(vx, vy, omega);
	}

	public void setVelocity_i() {
		robot.setVelocity(this.vx, this.vy, this.omega);
	}

	class OnTimeOut extends TimerTask {
		public void run() {
			setVelocity_i();
		}
	}

	private Icon getIcon(String name) {
		return new ImageIcon(getClass().getResource("../icons/" + name + ".png"));
	}

	public class ButtonListener implements ActionListener {
		private final float vx;
		private final float vy;
		private final float omega;

		private final DriveWidget driveWidget;

		public ButtonListener(float vx, float vy, float omega, DriveWidget p) {
			this.vx = vx;
			this.vy = vy;
			this.omega = omega;
			this.driveWidget = p;
		}

		public void actionPerformed(ActionEvent e) {
			this.driveWidget.setVelocity(vx, vy, omega);
		}
	}

	private class RobotListenerImpl implements RobotListener {

		private final DriveWidget driveWidget;

		public RobotListenerImpl(DriveWidget driveWidget) {
			this.driveWidget = driveWidget;
		}

		public void onImageReceived(Image img) {
			repaint();
		}

		public void onConnected() {
		}

		public void onDisconnected() {
		}

		public void onError(String error) {
		}

		public void onOdometryReceived(double x, double y, double phi) {
			checkDistance(x, y);
		}

		private void checkDistance(double x, double y){
			boolean distanceSensorFind = false;

			for (int i = 0; i < 9; i++) {
				if (DriveWidget._distanceSensors.get(i).distance() < 0.2) {
					System.out.println("parametros " + x + " " + y);
					switch (i) {
					case 0:
						virarDireita0(1.0f);
						break;
					case 1:
						virarDireita1Diagonal(1.0f);
						break;
					case 2:
						virarDireita0(1.0f);
						break;
					case 6:
						virarEsquerda0(1.0f);
						break;
					case 7:
						virarEsquerdaDiagonal(-1.0f);
						break;
					case 8:
						virarEsquerda0(-1.0f);
						break;
					default:
						break;
					}
					distanceSensorFind = true;
					break;
				}
			}

			if (!distanceSensorFind) {
				System.out.println("Sensor nao encontrado");
				this.driveWidget.setVelocity(speed, 0.0f, 0.0f);
			}
		}

		private void virarDireita0(float rot) {
			this.driveWidget.setVelocity(0.0f, -speed / 2, -speed / 4);
		}

		private void virarDireita1Diagonal(float rot) {
			this.driveWidget.setVelocity(-speed / 2, -speed / 2, 0.0f);
		}

		private void virarEsquerda0(float rot) {
			this.driveWidget.setVelocity(0.0f, speed / 2, speed / 4);
		}

		private void virarEsquerdaDiagonal(float rot) {
			this.driveWidget.setVelocity(speed / 2, speed / 2, 0.0f);
		}
	}
}