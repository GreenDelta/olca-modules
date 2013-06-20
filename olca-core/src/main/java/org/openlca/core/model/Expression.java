/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.core.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Transient;

/**
 * An expression is an object which holds the combination of a formula and its
 * calculated value
 */
@Entity
@Embeddable
public class Expression implements Cloneable {

	@Column(name = "formula")
	private String formula;

	@Transient
	private final transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	@Column(name = "value")
	private double value;

	public Expression() {
	}

	public Expression(String formula, double value) {
		this.formula = formula;
		this.value = value;
	}

	public static boolean isDouble(final String value) {
		boolean isDouble = true;
		boolean dotFound = false;
		Integer eFoundAt = null;
		int i = 0;
		while (isDouble && !(value.length() == i)) {
			if (value.charAt(i) == '.') {
				if (dotFound) {
					isDouble = false;
				} else {
					dotFound = true;
				}
			} else if (!Character.isDigit(value.charAt(i))) {
				if (value.charAt(i) == 'E' || value.charAt(i) == 'e') {
					if (eFoundAt != null) {
						isDouble = false;
					} else {
						eFoundAt = i;
					}
				} else if (value.charAt(i) == '-' || value.charAt(i) == '+') {
					if (i != 0 && (eFoundAt == null || eFoundAt != i - 1)) {
						isDouble = false;
					}
				} else if (i != value.length() - 1 || value.charAt(i) != 'd') {
					isDouble = false;
				}
			}
			i++;
		}
		return isDouble;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	@Override
	public Expression clone() {
		return new Expression(getFormula(), getValue());
	}

	public String getFormula() {
		return formula;
	}

	public double getValue() {
		return value;
	}

	public void removePropertyChangeListener(
			final PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	public void setFormula(String formula) {
		support.firePropertyChange("formula", this.formula,
				this.formula = formula);
	}

	public void setValue(double value) {
		support.firePropertyChange("value", this.value, this.value = value);
	}

	@Override
	public String toString() {
		return "Expression [formula=" + formula + ", value=" + value + "]";
	}

}
