package org.openlca.ilcd.commons;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.w3c.dom.Element;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "any" })
@XmlRootElement(name = "other")
public class Other implements Serializable {

	private final static long serialVersionUID = 1L;

	/**
	 * Objects of the following type(s) are allowed in the list {@link Element }
	 * {@link Object }
	 */
	@XmlAnyElement(lax = true)
	public final List<Object> any = new ArrayList<>();

	@Override
	public Other clone() {
		Other clone = new Other();
		for (Object o : any) {
			if (o instanceof Element) {
				Element e = (Element) o;
				clone.any.add(e.cloneNode(true));
			}
		}
		return clone;
	}

}
