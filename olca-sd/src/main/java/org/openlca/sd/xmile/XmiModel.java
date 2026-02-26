package org.openlca.sd.xmile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openlca.sd.xmile.view.XmiView;
import org.openlca.sd.xmile.view.XmiViewStyle;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlElements;

@XmlAccessorType(XmlAccessType.FIELD)
public class XmiModel {

	@XmlElementWrapper(name = "variables", namespace = Xmile.NS)
	@XmlElements({
		@XmlElement(name = "stock", namespace = Xmile.NS, type = XmiStock.class),
		@XmlElement(name = "flow", namespace = Xmile.NS, type = XmiFlow.class),
		@XmlElement(name = "gf", namespace = Xmile.NS, type = XmiGf.class),
		@XmlElement(name = "aux", namespace = Xmile.NS, type = XmiAux.class)
	})
	List<XmiVariable> variables;

	@XmlElementWrapper(name = "views", namespace = Xmile.NS)
	@XmlElements({
		@XmlElement(name = "view", namespace = Xmile.NS, type = XmiView.class),
		@XmlElement(name = "style", namespace = Xmile.NS, type = XmiViewStyle.class)
	})
	List<Object> views;

	public List<XmiVariable> variables() {
		return variables == null ? Collections.emptyList() : variables;
	}

	public void setVariables(List<XmiVariable> variables) {
		this.variables = variables;
	}

	public List<XmiStock> stocks() {
		return filter(XmiStock.class);
	}

	public List<XmiFlow> flows() {
		return filter(XmiFlow.class);
	}

	public List<XmiAux> auxs() {
		return filter(XmiAux.class);
	}

	public List<XmiGf> gfs() {
		return filter(XmiGf.class);
	}

	public List<XmiView> views() {
		return views == null
			? Collections.emptyList()
			: views.stream().filter(XmiView.class::isInstance)
			.map(XmiView.class::cast)
			.toList();
	}

	public void setViews(List<Object> views) {
		this.views = views;
	}

	public XmiViewStyle style() {
		if (views == null)
			return null;
		for (var v : views) {
			if (v instanceof XmiViewStyle vs)
				return vs;
		}
		return null;
	}

	private <T> List<T> filter(Class<T> type) {
		if (variables == null)
			return Collections.emptyList();
		var result = new ArrayList<T>();
		for (var v : variables) {
			if (type.isInstance(v))
				result.add(type.cast(v));
		}
		return result;
	}
}
