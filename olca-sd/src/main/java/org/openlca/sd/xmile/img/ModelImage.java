package org.openlca.sd.xmile.img;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.QuadCurve2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import org.openlca.commons.Res;
import org.openlca.sd.model.Id;
import org.openlca.sd.xmile.Xmile;
import org.openlca.sd.xmile.view.XmiView;
import org.openlca.sd.xmile.view.XmiViewPoint;

public class ModelImage {

	private final XmiView view;
	private final BufferedImage image;
	private final Graphics2D g;
	private final RenderContext ctx;
	private final HashMap<Id, Point> positions = new HashMap<>();

	private ModelImage(Xmile xmile, XmiView view, BufferedImage image) {
		this.view = view;
		this.image = image;
		this.g = image.createGraphics();
		g.setRenderingHint(
			RenderingHints.KEY_ANTIALIASING,
			RenderingHints.VALUE_ANTIALIAS_ON);
		g.setBackground(Color.WHITE);
		g.clearRect(0, 0, image.getWidth(), image.getHeight());
		this.ctx = RenderContext.create(g, xmile);
	}

	public static Res<BufferedImage> createFrom(Xmile xmile) {
		if (xmile == null || xmile.model() == null)
			return Res.error("XMILE object does not contain a model");
		var view = xmile.model().views().isEmpty()
			? null
			: xmile.model().views().getFirst();
		if (view == null)
			return Res.error("XMILE model does not contain a view");

		try {
			var size = ImageSize.estimateFrom(view);
			var image = new BufferedImage(
				size.width(), size.height(), BufferedImage.TYPE_INT_ARGB);
			return new ModelImage(xmile, view, image).render();
		} catch (Exception e) {
			return Res.error("Failed to render model image", e);
		}
	}

	public Res<BufferedImage> render() {

		var font = new Font("Arial", Font.PLAIN, 12);
		g.setFont(font);

		for (var f : view.flows()) {

			var p = pointOf(f);
			positions.put(Id.of(f.name()), p);
			g.setColor(Color.BLUE);
			g.fillOval(p.x - 3, p.y - 3, 6, 6);

			g.setColor(Color.GRAY);
			renderText(f.name(), new Point(p.x, (p.y - font.getSize() - 3)));

			if (f.pts().size() < 2)
				continue;
			for (int i = 1; i < f.pts().size(); i++) {
				var start = pointOf(f.pts().get(i - 1));
				var end = pointOf(f.pts().get(i));
				g.setColor(Color.BLUE);
				g.drawLine(start.x, start.y, end.x, end.y);
			}
		}

		for (var s : view.stocks()) {
			var pos = pointOf(s);
			var size = ctx.boxSizeOf(s);
			if (s.width() == null || s.height() == null) {
				pos = new Point(pos.x - size.x / 2, pos.y - size.y / 2);
			}
			var center = new Point(pos.x + size.x / 2, pos.y + size.y / 2);
			positions.put(Id.of(s.name()), center);
			g.setColor(Color.BLUE);
			g.drawRect(pos.x, pos.y, size.x, size.y);

			g.setColor(Color.GRAY);
			renderText(s.name(), new Point(center.x, center.y + font.getSize()));
		}


		for (var a : view.auxiliaries()) {
			var p = pointOf(a);
			positions.put(Id.of(a.name()), p);
			g.setColor(Color.GRAY);
			g.fillRect(p.x - 2, p.y - 2, 4, 4);
			g.setColor(Color.GRAY);
			renderText(a.name(), new Point(p.x, p.y - font.getSize()));
		}

		for (var con : view.connectors()) {
			var from = positions.get(Id.of(con.from()));
			var to = positions.get(Id.of(con.to()));
			if (from == null || to == null)
				continue;

			int mx = (from.x + to.x) / 2;
			int my = (from.y + to.y) / 2;

			int px = to.x - from.x;
			int py = to.y - from.y;

			int cx = mx + (int) (0.5 * py);
			int cy = my + (int) (0.5 * px);

			var curve = new QuadCurve2D.Double(
				from.x, from.y,
				cx, cy,
				to.x, to.y
			);

			g.setColor(Color.MAGENTA);
			g.draw(curve);
		}

		g.dispose();
		return Res.ok(image);
	}


	private Point pointOf(XmiViewPoint p) {
		return new Point((int) Math.round(p.x()), (int) Math.round(p.y()));
	}

	private void renderText(String label, Point center) {
		var lines = label.strip().split("\\\\n");
		var ms = g.getFontMetrics();
		int height = ms.getHeight() * lines.length;
		int y = center.y - (1 + height / 2);
		for (var line : lines) {
			int width = ms.stringWidth(line);
			int x = center.x - (1 + width / 2);
			g.drawString(line, x, y);
			y += ms.getHeight() + 1;
		}
	}

}
