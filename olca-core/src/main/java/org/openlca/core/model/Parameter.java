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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.PostLoad;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "tbl_parameters")
public class Parameter extends AbstractEntity implements PropertyChangeListener {

	@Lob
	@Column(name = "description")
	private String description;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "value", column = @Column(name = "expression_value")),
			@AttributeOverride(name = "formula", column = @Column(name = "expression_formula")) })
	private Expression expression = new Expression("1", 1);

	@Column(name = "f_owner")
	private String ownerId;

	@Column(name = "name")
	private String name;

	@Transient
	private final transient PropertyChangeSupport support = new PropertyChangeSupport(
			this);

	@Column(name = "type")
	private ParameterType type = ParameterType.UNSPECIFIED;

	public Parameter() {
		expression.addPropertyChangeListener(this);
	}

	public Parameter(Expression expression, ParameterType type, String ownerId) {
		if (type == null)
			this.type = ParameterType.UNSPECIFIED;
		else
			this.type = type;
		this.expression = expression;
		this.ownerId = ownerId;
		expression.addPropertyChangeListener(this);
	}

	// TODO allow upper case letters and underscores
	public static boolean checkName(String name) {
		if (name == null || name.equals(""))
			return false;
		boolean correct = true;
		int i = 0;
		while (correct && i < name.length()) {
			char c = name.charAt(i);
			if (!(c >= '0' && c <= '9' && i > 0 || c >= 'a' && c <= 'z'))
				correct = false;
			i++;
		}
		return correct;
	}

	@PostLoad
	protected void postLoad() {
		expression.addPropertyChangeListener(this);
	}

	public void addPropertyChangeListener(final PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}

	public String getDescription() {
		return description;
	}

	public Expression getExpression() {
		return expression;
	}

	public String getName() {
		return name;
	}

	public void setOwnerId(String ownerId) {
		this.ownerId = ownerId;
	}

	public String getOwnerId() {
		return ownerId;
	}

	public ParameterType getType() {
		return type != null ? type : ParameterType.UNSPECIFIED;
	}

	@Override
	public void propertyChange(PropertyChangeEvent arg0) {
		support.firePropertyChange(arg0);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}

	public void setDescription(String description) {
		support.firePropertyChange("description", this.description,
				this.description = description);
	}

	public void setName(String name) {
		support.firePropertyChange("name", this.name, this.name = name);
	}

	@Override
	public String toString() {
		return "Parameter [expression=" + expression + ", name=" + name
				+ ", type=" + type + "]";
	}

}
