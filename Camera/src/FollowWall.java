import java.util.ArrayList;
import java.util.List;

import rec.robotino.api2.DistanceSensor;

public class FollowWall {
	protected final float SLOW_VELOCITY = 0.08f;
	protected final float MEDIUM_VELOCITY = 0.16f;
	protected final float VELOCITY = 0.24f;
	protected final float FAST_VELOCITY = 0.32f;
	protected final float ANGULARVELOCITY = 0.02f;
	
	private Robot robot = new Robot();
	private List<DistanceSensor> _distanceSensors = new ArrayList<DistanceSensor>();
	
	public void init(Robot robot) {
		this.robot = robot;
		robot._omniDrive.setComId(robot._com.id());
		robot._bumper.setComId(robot._com.id());
	
		for (int i = 0; i < 9; ++i) {
			DistanceSensor s = new DistanceSensor();
			s.setSensorNumber(i);
			s.setComId(robot._com.id());
			_distanceSensors.add(s);
		}
	}

	public void followWalls() throws InterruptedException {
		float[] ev = new float[] { 1.0f, 0.0f };
		// escape vector for distance sensor
		float[][] escapeVector = new float[][] { new float[] { 0.0f, 0.0f }, new float[] { 0.0f, 0.0f },
				new float[] { 0.0f, 0.0f }, new float[] { 0.0f, 0.0f }, new float[] { 0.0f, 0.0f },
				new float[] { 0.0f, 0.0f }, new float[] { 0.0f, 0.0f }, new float[] { 0.0f, 0.0f },
				new float[] { 0.0f, 0.0f } };

		for (int i = 0; i < 9; i++) {
			robot.rotate(ev, escapeVector[i], 40.0f * i);
		}
		final float ESCAPE_DISTANCE = 0.10f;
		final float WALL_LOST_DISTANCE = 0.35f;
		final float WALL_FOUND_DISTANCE = 0.30f;
		final float WALL_FOLLOW_DISTANCE = 0.15f;
		final float NEW_WALL_FOUND_DISTANCE = 0.12f;
		float[] escape = new float[] { 0.0f, 0.0f };
		int curWallSensor = -1;
		float[] dir = new float[] { 1.0f, 0.0f };
		float velocity = VELOCITY;
		float rotVelocity = ANGULARVELOCITY;

		while (robot.isConnected() && false == robot._bumper.value()) {
			velocity = VELOCITY;
			int minIndex = 0;
			float minDistance = 0.40f;
			int numEscape = 0;
			escape[0] = escape[1] = 0.0f;

			StringBuilder values = new StringBuilder();
			for (int i = 0; i < _distanceSensors.size(); ++i) {
				float v = (float) _distanceSensors.get(i).distance();
				values.append(v + " ");
				if (v < minDistance) {
					minDistance = v;
					minIndex = i;
				}
				if (v < ESCAPE_DISTANCE) {
					++numEscape;
					robot.addScaledVector(escape, escapeVector[i], v);
				}
			}
			System.out.println(values.toString());

			if (numEscape >= 2) {
				// close to walls with more than one sensor, try to escape
				robot.normalizeVector(escape);
				robot.rotate(escape, dir, 180);
				velocity = SLOW_VELOCITY;
			} else {
				if (curWallSensor != -1 && _distanceSensors.get(curWallSensor).distance() > WALL_LOST_DISTANCE) {
					curWallSensor = -1;
					robot.rotateInPlace(dir, -20);
					rotVelocity = -0.20f;
				}
				if (curWallSensor == -1) {
					// wall not found yet
					if (minDistance < WALL_FOUND_DISTANCE) {
						curWallSensor = minIndex;
						velocity = SLOW_VELOCITY;
					}
				}
				if (curWallSensor != -1) {
					float wallDist = (float) _distanceSensors.get(curWallSensor).distance();

					// check for global new wall
					if (minIndex != curWallSensor && minDistance < NEW_WALL_FOUND_DISTANCE) {
						// new wall found, drive along this wall
						curWallSensor = minIndex;
						wallDist = (float) _distanceSensors.get(curWallSensor).distance();
						velocity = SLOW_VELOCITY;
					} else {
						if (_distanceSensors.get((curWallSensor + 1) % 9).distance() < wallDist) {
							// switch walls
							curWallSensor = (curWallSensor + 1) % 9;
							velocity = MEDIUM_VELOCITY;
						}
						// check for new wall in direction
						for (int i = 0; i < 2; ++i) {
							int tmpId = (curWallSensor + 2 + i) % 9;
							if (_distanceSensors.get(tmpId).distance() < WALL_FOUND_DISTANCE) {
								curWallSensor = tmpId;
								wallDist = (float) _distanceSensors.get(tmpId).distance();
								velocity = SLOW_VELOCITY;
								break;
							}
						}
					}
					// try to keep neighbor distance sensors in balance
					float vr = (float) _distanceSensors.get((curWallSensor + 1) % 9).distance();
					float vl = (float) _distanceSensors.get((curWallSensor + 8) % 9).distance();

					rotVelocity = (vr - vl);
					float followAngle = 95;
					if (Math.abs(rotVelocity) > 0.30) {
						velocity = SLOW_VELOCITY;
						followAngle = rotVelocity >= 0.0 ? 140 : 80;
					} else if (Math.abs(rotVelocity) > 0.40) {
						velocity = MEDIUM_VELOCITY;
						followAngle = rotVelocity >= 0.0 ? 120 : 85;
					}
					rotVelocity *= 8 * ANGULARVELOCITY;
					// follow the wall to the left
					robot.rotate(escapeVector[curWallSensor], dir, followAngle);
					// keep distance to wall steady
					float scale = wallDist - WALL_FOLLOW_DISTANCE;

					scale *= 10f;

					robot.addScaledVector(dir, escapeVector[curWallSensor], scale);
					robot.normalizeVector(dir);
				}
			}
			if (minDistance > 0.20f) {
				velocity = FAST_VELOCITY;
			}
			robot._omniDrive.setVelocity(velocity * (float) dir[0], velocity * (float) dir[1], rotVelocity);
			Thread.sleep(100);
		}
	}
}
