package org.openlca.ilcd.util;

import java.util.Map;

import javax.xml.namespace.QName;

import org.openlca.ilcd.processes.Exchange;

public class ExchangeExtension {

	private final Map<QName, String> atts;

	public ExchangeExtension(Exchange exchange) {
		this.atts = exchange.otherAttributes;
	}

	public boolean isValid() {
		return atts != null && getUnitId() != null
				&& getPropertyId() != null && getAmount() != null;
	}

	public void setFormula(String formula) {
		Extensions.setString(atts, "formula", formula);
	}

	public String getFormula() {
		return Extensions.getString(atts, "formula");
	}

	public void setUnitId(String unitId) {
		Extensions.setString(atts, "unitId", unitId);
	}

	public String getUnitId() {
		return Extensions.getString(atts, "unitId");
	}

	public void setPropertyId(String propertyId) {
		Extensions.setString(atts, "propertyId", propertyId);
	}

	public String getPropertyId() {
		return Extensions.getString(atts, "propertyId");
	}

	public void setAmount(double amount) {
		Extensions.setDouble(atts, "amount", amount);
	}

	public Double getAmount() {
		return Extensions.getDouble(atts, "amount");
	}

	public void setPedigreeUncertainty(String val) {
		Extensions.setString(atts, "pedigreeUncertainty", val);
	}

	public String getPedigreeUncertainty() {
		return Extensions.getString(atts, "pedigreeUncertainty");
	}

	public void setBaseUncertainty(Double val) {
		if (val == null)
			return;
		Extensions.setDouble(atts, "baseUncertainty", val);
	}

	public Double getBaseUncertainty() {
		return Extensions.getDouble(atts, "baseUncertainty");
	}

	public void setMostLikelyValue(double val) {
		Extensions.setDouble(atts, "mostLikelyValue", val);
	}

	public Double getMostLikelyValue() {
		return Extensions.getDouble(atts, "mostLikelyValue");
	}

	public String getDefaultProvider() {
		return Extensions.getString(atts, "defaultProvider");
	}

	public void setDefaultProvider(String providerId) {
		Extensions.setString(atts, "defaultProvider", providerId);
	}

	public boolean isAvoidedProduct() {
		return Extensions.getBoolean(atts, "avoidedProduct");
	}

	public void setAvoidedProduct(boolean b) {
		Extensions.setBoolean(atts, "avoidedProduct", b);
	}

}
