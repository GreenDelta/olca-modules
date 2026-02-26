package org.openlca.sd.xmile.img;

import java.util.List;
import java.util.function.Consumer;

import org.openlca.sd.xmile.view.XmiView;
import org.openlca.sd.xmile.view.XmiViewPoint;

record ImageSize(int width, int height) {

	static ImageSize estimateFrom(XmiView view) {
		if (view == null)
			return new ImageSize(800, 600);
		var width = new MaxDim(view.pageWidth(), 800);
		var height = new MaxDim(view.pageHeight(), 600);

		Consumer<List<? extends XmiViewPoint>> acc = list -> list.forEach(p -> {
			width.accept(p.x());
			height.accept(p.y());
		});

		acc.accept(view.stocks());
		acc.accept(view.auxiliaries());
		acc.accept(view.textBoxes());
		for (var f : view.flows()) {
			width.accept(f.x());
			height.accept(f.y());
			acc.accept(f.pts());
		}
		return new ImageSize(width.value(), height.value());
	}

	private static class MaxDim {
		double value;

		MaxDim(Integer initial, double fallback) {
			value = initial != null ? initial : fallback;
		}

		void accept(double v) {
			value = Math.max(value, v + 60);
		}

		int value() {
			return (int) Math.ceil(value);
		}
	}
}
