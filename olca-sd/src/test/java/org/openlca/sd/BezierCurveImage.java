package org.openlca.sd;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.CubicCurve2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class BezierCurveImage {

	public static void main(String[] args) {
		// Define the image dimensions
		int width = 800;
		int height = 600;

		// Create a new BufferedImage (the canvas)
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		// Get the Graphics2D object from the image
		Graphics2D g2d = image.createGraphics();

		// Optional: Fill the background with a color
		g2d.setBackground(Color.WHITE);
		g2d.clearRect(0, 0, width, height);

		// Enable anti-aliasing for smooth lines
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// Define the points for a cubic Bezier curve
		double startX = 100, startY = 300;
		double endX = 700, endY = 500;
		// double ctrl1X = 250, ctrl1Y = 100;
		// double ctrl2X = 550, ctrl2Y = 500;
		double ctrlX = (startX + endX) / 2;
		double ctrlY = (startY + endY) / 2;

		// Create the CubicCurve2D object
		CubicCurve2D cubicCurve = new CubicCurve2D.Double(
			startX, startY,
			ctrlX, ctrlY,
			ctrlX, ctrlY,
			endX, endY
		);


		// Set the color and stroke for the curve
		g2d.setColor(Color.BLUE);
		g2d.setStroke(new BasicStroke(3));

		// Draw the curve
		g2d.draw(cubicCurve);

		// You can also draw the points for visualization if needed
		g2d.setColor(Color.RED);
		g2d.fillOval((int) startX - 5, (int) startY - 5, 10, 10);
		g2d.fillOval((int) endX - 5, (int) endY - 5, 10, 10);
		g2d.setColor(Color.MAGENTA);
		g2d.fillOval((int) ctrlX - 5, (int) ctrlY - 5, 10, 10);
		//g2d.fillOval((int) ctrl2X - 5, (int) ctrl2Y - 5, 10, 10);

		// Dispose of the graphics context to free up resources
		g2d.dispose();

		// Save the image to a file
		try {
			File outputfile = new File("bezier_curve.png");
			ImageIO.write(image, "png", outputfile);
			System.out.println("Image created successfully at: " + outputfile.getAbsolutePath());
		} catch (IOException e) {
			System.err.println("Error saving the image: " + e.getMessage());
		}
	}
}
