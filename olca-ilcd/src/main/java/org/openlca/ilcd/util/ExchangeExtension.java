package org.openlca.ilcd.util;

import javax.xml.namespace.QName;

import org.openlca.ilcd.processes.Exchange;

public class ExchangeExtension {

	private Exchange exchange;
	private final String FORMULA = "formula";
	private final String UNIT_ID = "unitId";
	private final String PROPERTY_ID = "propertyId";
	private final String AMOUNT = "amount";
	private final String PEDIGREE_UNCERTAINTY = "pedigreeUncertainty";
	private final String BASE_UNCERTAINTY = "baseUncertainty";
	private final String MOST_LIKELY_VALUE = "mostLikelyValue";
	private final String AVOIDED_PRODUCT = "avoidedProduct";
	private final String DEFAULT_PROVIDER = "defaultProvider";

	public ExchangeExtension(Exchange exchange) {
		this.exchange = exchange;
	}

	public boolean isValid() {
		return exchange != null && getUnitId() != null
				&& getPropertyId() != null && getAmount() != null;
	}

	public void setFormula(String formula) {
		setStringValue(FORMULA, formula);
	}

	public String getFormula() {
		return getStringValue(FORMULA);
	}

	public void setUnitId(String unitId) {
		setStringValue(UNIT_ID, unitId);
	}

	public String getUnitId() {
		return getStringValue(UNIT_ID);
	}

	public void setPropertyId(String propertyId) {
		setStringValue(PROPERTY_ID, propertyId);
	}

	public String getPropertyId() {
		return getStringValue(PROPERTY_ID);
	}

	public void setAmount(double amount) {
		setDoubleValue(AMOUNT, amount);
	}

	public Double getAmount() {
		return getDoubleValue(AMOUNT);
	}

	public void setPedigreeUncertainty(String val) {
		setStringValue(PEDIGREE_UNCERTAINTY, val);
	}

	public String getPedigreeUncertainty() {
		return getStringValue(PEDIGREE_UNCERTAINTY);
	}

	public void setBaseUncertainty(Double val) {
		if (val == null)
			return;
		setDoubleValue(BASE_UNCERTAINTY, val);
	}

	public Double getBaseUncertainty() {
		return getDoubleValue(BASE_UNCERTAINTY);
	}

	public void setMostLikelyValue(double val) {
		setDoubleValue(MOST_LIKELY_VALUE, val);
	}

	public Double getMostLikelyValue() {
		return getDoubleValue(MOST_LIKELY_VALUE);
	}

	public String getDefaultProvider() {
		return getStringValue(DEFAULT_PROVIDER);
	}

	public void setDefaultProvider(String providerId) {
		setStringValue(DEFAULT_PROVIDER, providerId);
	}

	public boolean isAvoidedProduct() {
		return getBooleanValue(AVOIDED_PRODUCT);
	}

	public void setAvoidedProduct(boolean b) {
		setBooleanValue(AVOIDED_PRODUCT, b);
	}

	private void setDoubleValue(String attribute, double value) {
		String str = Double.toString(value);
		setStringValue(attribute, str);
	}

	private Double getDoubleValue(String attribute) {
		String str = getStringValue(attribute);
		if (str == null)
			return null;
		return Double.valueOf(str);
	}

	private void setStringValue(String attribute, String value) {
		if (exchange == null || value == null)
			return;
		QName qName = Extensions.getQName(attribute);
		exchange.otherAttributes.put(qName, value);
	}

	private String getStringValue(String attribute) {
		if (exchange == null)
			return null;
		QName qName = Extensions.getQName(attribute);
		return exchange.otherAttributes.get(qName);
	}

	private void setBooleanValue(String attribute, boolean value) {
		String str = Boolean.toString(value);
		setStringValue(attribute, str);
	}

	private boolean getBooleanValue(String attribute) {
		String str = getStringValue(attribute);
		if (str == null)
			return false;
		return Boolean.parseBoolean(str);
	}

}
