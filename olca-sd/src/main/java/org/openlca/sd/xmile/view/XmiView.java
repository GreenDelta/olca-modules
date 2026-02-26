package org.openlca.sd.xmile.view;

import java.util.List;

import org.openlca.sd.xmile.Xmile;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmiView {

	@XmlAttribute(name = "page_width")
	Integer pageWidth;

	@XmlAttribute(name = "page_height")
	Integer pageHeight;

	@XmlAttribute(name = "zoom")
	Integer zoom;

	@XmlElement(name = "stock", namespace = Xmile.NS)
	List<XmiStockView> stocks;

	@XmlElement(name = "aux", namespace = Xmile.NS)
	List<XmiAuxView> auxiliaries;

	@XmlElement(name = "text_box", namespace = Xmile.NS)
	List<XmiTextBoxView> textBoxes;

	@XmlElement(name = "flow", namespace = Xmile.NS)
	List<XmiFlowView> flows;

	@XmlElement(name = "connector", namespace = Xmile.NS)
	List<XmiConnectorView> connectors;

	@XmlElement(name = "style", namespace = Xmile.NS)
	XmiViewStyle style;

	public List<XmiStockView> stocks() {
		return stocks == null ? List.of() : stocks;
	}

	public void setStocks(List<XmiStockView> stocks) {
		this.stocks = stocks;
	}

	public List<XmiAuxView> auxiliaries() {
		return auxiliaries == null ? List.of() : auxiliaries;
	}

	public void setAuxiliaries(List<XmiAuxView> auxiliaries) {
		this.auxiliaries = auxiliaries;
	}

	public List<XmiTextBoxView> textBoxes() {
		return textBoxes == null ? List.of() : textBoxes;
	}

	public void setTextBoxes(List<XmiTextBoxView> textBoxes) {
		this.textBoxes = textBoxes;
	}

	public List<XmiFlowView> flows() {
		return flows == null ? List.of() : flows;
	}

	public void setFlows(List<XmiFlowView> flows) {
		this.flows = flows;
	}

	public List<XmiConnectorView> connectors() {
		return connectors == null ? List.of() : connectors;
	}

	public void setConnectors(List<XmiConnectorView> connectors) {
		this.connectors = connectors;
	}

	public Integer pageWidth() {
		return pageWidth;
	}

	public void setPageWidth(Integer pageWidth) {
		this.pageWidth = pageWidth;
	}

	public Integer pageHeight() {
		return pageHeight;
	}

	public void setPageHeight(Integer pageHeight) {
		this.pageHeight = pageHeight;
	}

	public Integer zoom() {
		return zoom;
	}

	public void setZoom(Integer zoom) {
		this.zoom = zoom;
	}

	public XmiViewStyle style() {
		return style;
	}

	public void setStyle(XmiViewStyle style) {
		this.style = style;
	}

}
