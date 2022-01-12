package org.openlca.ilcd.epd.conversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.epd.model.MaterialProperty;
import org.openlca.ilcd.epd.model.MaterialPropertyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class MatML {

	private static final String NS = "http://www.matml.org/";

	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Other extension;
	private Document doc;
	private Element bulkDetails;
	private Element metaData;

	MatML(Other extension) {
		this.extension = extension;
	}

	void clear() {
		if (extension == null)
			return;
		Element root = findRoot();
		if (root != null)
			extension.any.remove(root);
	}

	void createStructure(String materialName) {
		clear();
		doc = Dom.createDocument();
		if (doc == null || extension == null)
			return;
		Element root = doc.createElementNS(NS, "mat:MatML_Doc");
		Element materialElem = doc.createElementNS(NS, "mat:Material");
		root.appendChild(materialElem);
		bulkDetails = doc.createElementNS(NS, "mat:BulkDetails");
		Element nameElem = doc.createElementNS(NS, "mat:Name");
		bulkDetails.appendChild(nameElem);
		nameElem.setTextContent(materialName);
		materialElem.appendChild(bulkDetails);
		metaData = doc.createElementNS(NS, "mat:Metadata");
		root.appendChild(metaData);
		extension.any.add(root);
	}

	void append(MaterialPropertyValue value) {
		if (doc == null || bulkDetails == null || metaData == null) {
			log.warn("Cannot append material property value; structure not "
					+ "yet initialized -> call createStructure first");
			return;
		}
		appendPropertyData(value);
		appendPropertyDetails(value);
	}

	private void appendPropertyData(MaterialPropertyValue value) {
		Element propElement = doc.createElementNS(NS, "mat:PropertyData");
		bulkDetails.appendChild(propElement);
		MaterialProperty property = value.property;
		if (property != null)
			propElement.setAttribute("property", property.id);
		Element dataElement = doc.createElementNS(NS, "mat:Data");
		propElement.appendChild(dataElement);
		dataElement.setAttribute("format", "float");
		dataElement.setTextContent(Double.toString(value.value));
	}

	private void appendPropertyDetails(MaterialPropertyValue value) {
		MaterialProperty property = value.property;
		if (property == null)
			return;
		Element detailsElem = doc.createElementNS(NS, "mat:PropertyDetails");
		metaData.appendChild(detailsElem);
		detailsElem.setAttribute("id", property.id);
		Element nameElem = doc.createElementNS(NS, "mat:Name");
		detailsElem.appendChild(nameElem);
		nameElem.setTextContent(property.name);
		Element unitsElem = doc.createElementNS(NS, "mat:Units");
		detailsElem.appendChild(unitsElem);
		unitsElem.setAttribute("name", property.unit);
		unitsElem.setAttribute("description", property.unitDescription);
		Element unitElement = doc.createElementNS(NS, "mat:Unit");
		unitsElem.appendChild(unitElement);
		Element unitNameElement = doc.createElementNS(NS, "mat:Name");
		unitElement.appendChild(unitNameElement);
		unitNameElement.setTextContent(property.unit);
	}

	List<MaterialPropertyValue> readValues() {
		if (extension == null)
			return Collections.emptyList();
		Element root = findRoot();
		if (root == null)
			return Collections.emptyList();
		Map<String, Double> dataValues = readDataValues(root);
		List<MaterialProperty> properties = readProperties(root);
		List<MaterialPropertyValue> values = new ArrayList<>();
		for (MaterialProperty property : properties) {
			Double val = dataValues.get(property.id);
			if (val == null)
				continue;
			MaterialPropertyValue value = new MaterialPropertyValue();
			value.value = val;
			value.property = property;
			values.add(value);
		}
		return values;
	}

	private Map<String, Double> readDataValues(Element root) {
		Map<String, Double> map = new HashMap<>();
		Element bulkDetails = Dom.findChild(root, "Material", "BulkDetails");
		if (bulkDetails == null)
			return map;
		NodeList list = bulkDetails.getElementsByTagNameNS(NS, "PropertyData");
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if (!(node instanceof Element dataElement))
				continue;
			Double val = Dom.getDouble(Dom.getChild(dataElement, "Data", NS));
			String propertyId = dataElement.getAttribute("property");
			map.put(propertyId, val);
		}
		return map;
	}

	private List<MaterialProperty> readProperties(Element root) {
		List<MaterialProperty> properties = new ArrayList<>();
		Element metadata = Dom.findChild(root, "Metadata");
		NodeList list = metadata.getElementsByTagNameNS(NS, "PropertyDetails");
		for (int i = 0; i < list.getLength(); i++) {
			Node node = list.item(i);
			if (!(node instanceof Element e))
				continue;
			MaterialProperty p = new MaterialProperty();
			p.id = e.getAttribute("id");
			p.name = Dom.getText(Dom.getChild(e, "Name", NS));
			Element unit = Dom.findChild(e, "Units");
			if (unit == null)
				continue;
			p.unit = unit.getAttribute("name");
			p.unitDescription = unit.getAttribute("description");
			properties.add(p);
		}
		return properties;
	}

	private Element findRoot() {
		if (extension == null)
			return null;
		for (Object any : extension.any) {
			if (!(any instanceof Element element))
				continue;
			if (Objects.equals("MatML_Doc", element.getLocalName()))
				return element;
		}
		return null;
	}
}
