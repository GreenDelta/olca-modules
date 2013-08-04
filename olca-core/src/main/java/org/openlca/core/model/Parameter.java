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

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_parameters")
public class Parameter extends AbstractEntity {

	@Lob
	@Column(name = "description")
	private String description;

	@Embedded
	@AttributeOverrides({
			@AttributeOverride(name = "value", column = @Column(name = "expression_value")),
			@AttributeOverride(name = "formula", column = @Column(name = "expression_formula")) })
	private Expression expression = new Expression("1", 1);

	@Column(name = "name")
	private String name;

	@Column(name = "type")
	private ParameterType type = ParameterType.UNSPECIFIED;

	public Parameter() {
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

	public String getDescription() {
		return description;
	}

	public Expression getExpression() {
		return expression;
	}

	public String getName() {
		return name;
	}

	public ParameterType getType() {
		return type != null ? type : ParameterType.UNSPECIFIED;
	}

	public void setType(ParameterType type) {
		this.type = type;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "Parameter [expression=" + expression + ", name=" + name
				+ ", type=" + type + "]";
	}

}
