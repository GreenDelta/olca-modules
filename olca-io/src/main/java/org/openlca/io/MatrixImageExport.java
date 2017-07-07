package org.openlca.io;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.openlca.core.matrix.format.IMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exports a matrix as an image file. Cells with values are displayed in another
 * column than blank cells. This can be helpful to analyze the structure of a
 * matrix.
 */
public class MatrixImageExport {

	private int width = -1;
	private int height = -1;

	private Logger log = LoggerFactory.getLogger(getClass());
	private IMatrix matrix;
	private File exportFile;

	public MatrixImageExport(IMatrix matrix, File exportFile) {
		this.matrix = matrix;
		this.exportFile = exportFile;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void run() {
		log.trace("write matrix to image {}", exportFile);
		int colWidth = colWidth();
		int rowHeight = rowHeight();
		int realWidth = matrix.columns() * colWidth;
		int realHeight = matrix.rows() * rowHeight;
		BufferedImage image = new BufferedImage(realWidth, realHeight,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		graphics.setColor(Color.WHITE);
		graphics.fillRect(0, 0, realWidth, realHeight);
		graphics.setColor(Color.BLUE);
		try {
			drawCells(graphics, colWidth, rowHeight);
			ImageIO.write(image, "png", exportFile);
		} catch (Exception e) {
			log.error("Failed to write matrix to image", e);
		}
	}

	private void drawCells(Graphics2D graphics, int colWidth, int rowHeight) {
		for (int row = 0; row < matrix.rows(); row++) {
			for (int col = 0; col < matrix.columns(); col++) {
				double val = matrix.get(row, col);
				if (val == 0)
					continue;
				if (val < 0)
					graphics.setColor(Color.RED);
				else
					graphics.setColor(Color.BLUE);
				graphics.fillRect(col * colWidth, row * rowHeight, colWidth,
						rowHeight);
			}
		}
	}

	private int colWidth() {
		int cols = matrix.columns();
		if (width != -1) {
			int w = width / cols;
			return w == 0 ? 1 : w;
		}
		if (cols > 1000)
			return 1;
		if (cols > 500)
			return 2;
		int w = 500 / cols;
		return w < 2 ? 2 : w;
	}

	private int rowHeight() {
		int rows = matrix.rows();
		if (height != -1) {
			int h = height / rows;
			return h == 0 ? 1 : h;
		}
		if (rows > 1000)
			return 1;
		if (rows > 500)
			return 2;
		int h = 500 / rows;
		return h < 2 ? 2 : h;
	}
}
