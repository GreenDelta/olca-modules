package org.openlca.sd.xmile.svg;

import org.openlca.sd.model.Id;
import org.openlca.sd.xmile.view.XmiAuxView;
import org.openlca.sd.xmile.view.XmiConnectorView;
import org.openlca.sd.xmile.view.XmiFlowView;
import org.openlca.sd.xmile.view.XmiStockView;
import org.openlca.sd.xmile.view.XmiTextBoxView;
import org.openlca.sd.xmile.view.XmiView;

class Converter {

	private final XmiView view;
	private final SvgDoc doc;

	private Converter(XmiView view) {
		this.view = view;
		this.doc = new SvgDoc();
	}

	static SvgDoc convert(XmiView view) {
		return view != null
			? new Converter(view).convert()
			: new SvgDoc();
	}

	private SvgDoc convert() {
		for (var stock : view.stocks()) {
			stock(stock);
		}
		for (var aux : view.auxiliaries()) {
			aux(aux);
		}
		for (var textBox : view.textBoxes()) {
			textBox(textBox);
		}
		for (var flow : view.flows()) {
			flow(flow);
		}
		for (var connector : view.connectors()) {
			commector(connector);
		}
		return doc;
	}

	private void stock(XmiStockView stock) {
		if (stock == null || Id.isNil(stock.name()))
			return;
		var box = TextBox.create(stock.x(), stock.y(), stock.name());
		var text = box.svgText();
		var rect = box.svgRect();

		if (stock.width() != null && stock.height() != null) {
			double w = stock.width();
			double h = stock.height();
			double cx = stock.x() + w / 2;
			double cy = stock.y() + h / 2;
			text.x = cx;
			text.y = cy;
			for (var span : text.spans) {
				span.x = cx;
			}

			rect.x = stock.x();
			rect.y = stock.y();
			rect.width = w;
			rect.height = h;
		}

		doc.addText(text);
		doc.addRect(rect);
	}

	private void aux(XmiAuxView aux) {
		if (aux == null || Id.isNil(aux.name()))
			return;
		var box = TextBox.create(aux.x(), aux.y(), aux.name());
		doc.addText(box.svgText());
	}

	private void textBox(XmiTextBoxView text) {
		if (text == null || Id.isNil(text.text()))
			return;
		var box = TextBox.create(text.x(), text.y(), text.text());
		var svgText = box.svgText();
		svgText.fill = "black";
		doc.addText(svgText);
	}

	private void flow(XmiFlowView flow) {
		if (flow == null || Id.isNil(flow.name()))
			return;
		var box = TextBox.create(flow.x(), flow.y(), flow.name());
		doc.addText(box.svgText);

		var pts = flow.pts();
		if (pts.size() >= 2) {
			// Draw lines between points
			for (int i = 0; i < pts.size() - 1; i++) {
				var p1 = pts.get(i);
				var p2 = pts.get(i + 1);
				var line = new SvgLine(p1.x(), p1.y(), p2.x(), p2.y(), "blue");
				doc.addLine(line);
			}

			// Add label at flow position
			// var text = new SvgText(flow.x(), flow.y() - 10, flow.name(), "black");

			// text.fontSize = 10.0;
			// doc.addText(text);
		}
	}

	private void commector(XmiConnectorView connector) {
		// For now, create a simple curved path (this could be enhanced)
		// This is a placeholder - you might want to implement proper curve logic
		// based on the from/to elements and angle
		var pathData = String.format("M 100,100 Q 150,50 200,100");
		var path = new SvgPath(pathData, "gray");
		doc.addPath(path);
	}

	private record TextBox(
		double x,
		double y,
		double width,
		double height,
		SvgText svgText
	) {

		static TextBox create(double x, double y, String value) {

			double fontSize = 10;

			var parts = value.split("\\\\n");


			double height = 1.4 * fontSize * parts.length;
			double width = 0;
			for (String part : parts) {
				width = Math.max(width, part.length() * (fontSize / 2 + 1));
			}

			var text = new SvgText(x, y);
			text.fill = "blue";
			text.fontFamily = "Arial";
			text.fontSize = fontSize;
			text.textAnchor = "middle";
			for (int i = 0; i < parts.length; i++) {
				var span = new SvgText.Span(x, i == 0 ? 0 : fontSize, parts[i]);
				text.addSpan(span);
			}

			return new TextBox(
				x, y, width, height, text
			);
		}

		SvgRect svgRect() {
			var rect = new SvgRect(x - width / 2, y - height / 2, width, height);
			rect.stroke = "blue";
			rect.fill = "white";
			return rect;
		}
	}

}
