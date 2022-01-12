package org.openlca.ilcd.epd.model;

public final class MaterialPropertyValue implements Cloneable {

	public MaterialProperty property;
	public double value;

	@Override
	public MaterialPropertyValue clone() {
		try {
			return (MaterialPropertyValue) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e);
		}
	}
}
