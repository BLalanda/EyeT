package controllers;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.theeyetribe.client.data.GazeData;

public class ImageEditor {

	private GazeController gc;

	private int circleStrokeWidth;
	private int pathStrokeWidth;
	private int baseDiameter;
	private int maxDiameter;

	private BufferedImage cursor;

	private Graphics2D g2d;

	public ImageEditor(GazeController gazeController) {
		this.gc = gazeController;
		loadResources();
		initializeVariables();
	}

	private void loadResources() {
		try {
			cursor = ImageIO.read(getClass().getResource(
					"/resources/cursor.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void initializeVariables() {
		// TODO: make it customizable in gui
		circleStrokeWidth = 3;
		pathStrokeWidth = 2;
		baseDiameter = 10;
		maxDiameter = 80;
	}

	public void addCursor(BufferedImage img) {

		int x = MouseInfo.getPointerInfo().getLocation().x;
		int y = MouseInfo.getPointerInfo().getLocation().y;

		g2d.drawImage(cursor, x, y, 16, 16, null);
	}

	public void addCurrentEyePosition(BufferedImage img) {
		g2d = img.createGraphics();

		addCursor(img);

		if (gc.hasAnyGazesInRange()) {
			System.out.println(gc.getGazeHistory().size());
			markLatestGazes(img);

			if (gc.atLeastTwoGazes())
				markSaccadesPaths();

		} else {
			setBorderOn(img);
		}

		g2d.dispose();
	}

	private void markLatestGazes(BufferedImage img) {
		g2d.setStroke(new BasicStroke(circleStrokeWidth));

		for (GazeData gaze : gc.getGazeHistory()) {
			double x = gaze.smoothedCoordinates.x;
			double y = gaze.smoothedCoordinates.y;

			int size = 0;
			if (gc.isLast(gaze)) {
				g2d.setColor(Color.GREEN);
				size = getFixationCircleGrowth();
			} else {
				size = baseDiameter;
				g2d.setColor(Color.RED);
			}

			int shift = size / 2;
			x = calcualteX(x) - shift;
			y = calcualteY(y) - shift;

			if (size <= maxDiameter)
				g2d.drawOval((int) x, (int) y, size, size);
			else
				g2d.fillOval((int) x, (int) y, maxDiameter, maxDiameter);
		}
	}

	private void markSaccadesPaths() {
		g2d.setColor(Color.BLUE);
		g2d.setStroke(new BasicStroke(pathStrokeWidth));

		for (int i = 0; i < gc.getGazeHistory().size() - 1; i++) {
			GazeData startGaze = gc.getGazeHistory().get(i);
			GazeData endGaze = gc.getGazeHistory().get(i + 1);

			int startX = (int) calcualteX(startGaze.smoothedCoordinates.x);
			int startY = (int) calcualteY(startGaze.smoothedCoordinates.y);
			int endX = (int) calcualteX(endGaze.smoothedCoordinates.x);
			int endY = (int) calcualteY(endGaze.smoothedCoordinates.y);

			g2d.drawLine(startX, startY, endX, endY);
		}
	}

	private double calcualteX(double x) {
		if (gc.isInWidthRange(x))
			return x;

		return x < 0 ? 0 : gc.getScreenSize().getWidth();
	}

	private double calcualteY(double y) {
		if (gc.isInHeightRange(y))
			return y;

		return y < 0 ? 0 : gc.getScreenSize().getHeight();
	}

	private int getFixationCircleGrowth() {
		return Math.min(maxDiameter,
				baseDiameter + (int) (gc.lastFixationLength() / 100));
	}

	private void setBorderOn(BufferedImage img) {
		g2d.setColor(Color.RED);
		g2d.drawRect(0, 0, img.getWidth() - 3, img.getHeight() - 3);
	}
}