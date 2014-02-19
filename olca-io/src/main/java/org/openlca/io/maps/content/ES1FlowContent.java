package org.openlca.io.maps.content;

import org.openlca.ecospold.IExchange;
import org.openlca.io.ecospold1.importer.ES1KeyGen;

public class ES1FlowContent implements IMappingContent {

	private String name;
	private String localName;
	private String unit;
	private String category;
	private String subCategory;
	private String localCategory;
	private String localSubCategory;
	private String location;
	private String casNumber;
	private double factor;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocalName() {
		return localName;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSubCategory() {
		return subCategory;
	}

	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}

	public String getLocalCategory() {
		return localCategory;
	}

	public void setLocalCategory(String localCategory) {
		this.localCategory = localCategory;
	}

	public String getLocalSubCategory() {
		return localSubCategory;
	}

	public void setLocalSubCategory(String localSubCategory) {
		this.localSubCategory = localSubCategory;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getCasNumber() {
		return casNumber;
	}

	public void setCasNumber(String casNumber) {
		this.casNumber = casNumber;
	}

	public double getFactor() {
		return factor;
	}

	public void setFactor(double factor) {
		this.factor = factor;
	}

	@Override
	public String getKey() {
		ES1FlowContent exchange = new ES1FlowContent();
		exchange.setCategory(category);
		exchange.setSubCategory(subCategory);
		exchange.setName(name);
		exchange.setUnit(unit);
		return ES1KeyGen.forElementaryFlow((IExchange) exchange);
	}

}
