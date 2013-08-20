package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Represents the redefinition of parameters in calculation setups and product
 * systems.
 */
@Entity
@Table(name = "tbl_parameter_redefs")
public class ParameterRedef extends AbstractEntity {

	@Column(name = "name")
	private String name;

	@Column(name = "f_process")
	private Long processId;

	@Column(name = "value")
	private double value;

	@Override
	public ParameterRedef clone() {
		ParameterRedef clone = new ParameterRedef();
		clone.setName(getName());
		clone.setProcessId(getProcessId());
		clone.setValue(getValue());
		return clone;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getProcessId() {
		return processId;
	}

	public void setProcessId(Long processId) {
		this.processId = processId;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

}
