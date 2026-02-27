package org.openlca.sd.xmile.view;

import java.util.ArrayList;
import java.util.List;

import org.openlca.sd.xmile.Xmile;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmiView {

	@XmlAttribute(name = "page_width")
	private Integer pageWidth;

	@XmlAttribute(name = "page_height")
	private Integer pageHeight;

	@XmlAttribute(name = "zoom")
	private Integer zoom;

	@XmlElement(name = "stock", namespace = Xmile.NS)
	private List<XmiStockView> stocks;

	@XmlElement(name = "aux", namespace = Xmile.NS)
	private List<XmiAuxView> auxiliaries;

	@XmlElement(name = "text_box", namespace = Xmile.NS)
	private List<XmiTextBoxView> textBoxes;

	@XmlElement(name = "flow", namespace = Xmile.NS)
	private List<XmiFlowView> flows;

	@XmlElement(name = "connector", namespace = Xmile.NS)
	private List<XmiConnectorView> connectors;

	@XmlElement(name = "style", namespace = Xmile.NS)
	private XmiViewStyle style;

	public List<XmiStockView> stocks() {
		if (stocks == null) {
			stocks = new ArrayList<>();
		}
		return stocks;
	}

	public List<XmiAuxView> auxiliaries() {
		if (auxiliaries == null) {
			auxiliaries = new ArrayList<>();
		}
		return auxiliaries;
	}

	public List<XmiTextBoxView> textBoxes() {
		if (textBoxes == null) {
			textBoxes = new ArrayList<>();
		}
		return textBoxes;
	}

	public List<XmiFlowView> flows() {
		if (flows == null) {
			flows = new ArrayList<>();
		}
		return flows;
	}

	public List<XmiConnectorView> connectors() {
		if (connectors == null) {
			connectors = new ArrayList<>();
		}
		return connectors;
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
