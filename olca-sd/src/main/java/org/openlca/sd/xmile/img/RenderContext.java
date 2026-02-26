package org.openlca.sd.xmile.img;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import org.openlca.sd.model.Id;
import org.openlca.sd.xmile.Xmile;
import org.openlca.sd.xmile.view.XmiAuxView;
import org.openlca.sd.xmile.view.XmiElementStyle;
import org.openlca.sd.xmile.view.XmiFlowView;
import org.openlca.sd.xmile.view.XmiStockView;
import org.openlca.sd.xmile.view.XmiStyleInfo;
import org.openlca.sd.xmile.view.XmiViewStyle;

record RenderContext(
	Graphics2D g,
	Xmile xmile,
	List<XmiViewStyle> styles,
	HashMap<String, Font> fontCache
) {

	static RenderContext create(Graphics2D g, Xmile xmile) {
		var styles = new ArrayList<XmiViewStyle>();

		// more specific styles first
		if (xmile.model() != null) {
			var model = xmile.model();
			if (!model.views().isEmpty()) {
				var view = model.views().getFirst();
				if (view.style() != null) {
					styles.add(view.style());
				}
			}
			if (model.style() != null) {
				styles.add(model.style());
			}
		}

		return new RenderContext(g, xmile, styles, new HashMap<>());
	}

	Font fontOf(XmiStyleInfo info) {
		var f = getFont(info.fontFamily(), info.fontSize());
		if (f != null)
			return f;
		var style = findElementStyle(info);
		if (style != null) {
			f = getFont(style.fontFamily(), style.fontSize());
			if (f != null)
				return f;
		}
		for (var s : styles) {
			f = getFont(s.fontFamily(), s.fontSize());
			if (f != null)
				return f;
		}
		return getFont("Arial", "12");
	}

	private Font getFont(String family, String size) {
		if (family == null || size == null)
			return null;
		var key = family + "-" + size;
		var cached = fontCache.get(key);
		if (cached != null)
			return cached;
		try {
			var sizeBuff = new StringBuilder();
			for (var c : size.toCharArray()) {
				if (Character.isDigit(c))
					sizeBuff.append(c);
			}
			int s = Integer.parseInt(sizeBuff.toString());
			var font = new Font(family, Font.PLAIN, s);
			fontCache.put(key, font);
			return font;
		} catch (Exception e) {
			return null;
		}
	}

	private XmiElementStyle findElementStyle(XmiStyleInfo info) {
		Function<XmiViewStyle, XmiElementStyle> match = switch (info) {
			case XmiStockView ignore -> XmiViewStyle::stockStyle;
			case XmiAuxView ignore -> XmiViewStyle::auxStyle;
			case XmiFlowView ignore -> XmiViewStyle::flowStyle;
			default -> s -> null;
		};
		return styles.stream()
			.map(match)
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
	}

	Point boxSizeOf(XmiStockView view) {
		if (view == null)
			return new Point(80, 40);
		if (view.width() != null && view.height() != null)
			return new Point(view.width().intValue(), view.height().intValue());

		var style = findElementStyle(view);
		if (style != null
			&& style.shape() != null
			&& style.shape().width() != null
			&& style.shape().height() != null) {
			return new Point(
				style.shape().width().intValue(),
				style.shape().height().intValue()
			);
		}

		if (Id.isNil(view.name()))
			return new Point(80, 40);

		// calculate based on text size
		var font = fontOf(view);
		if (font == null)
			return new Point(80, 40);

		g.setFont(font);
		var parts = view.name().split("\\\\n");
		int height = parts.length * (g.getFontMetrics().getHeight() + 2);
		int width = 0;
		for (var p : parts) {
			width = Math.max(width, g.getFontMetrics().stringWidth(p) + 4);
		}
		return new Point(width, height);
	}
}
