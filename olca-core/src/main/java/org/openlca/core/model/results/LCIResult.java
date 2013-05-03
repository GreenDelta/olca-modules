/*******************************************************************************

 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.core.model.results;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.openlca.core.model.Exchange;

/**
 * Class represents an LCI result
 * 
 * @author Sebastian Greve
 * 
 */
@Entity
@Table(name = "tbl_lciresults")
public class LCIResult {

	@Id
	@Column(name = "id")
	private String productSystemId;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "f_owner")
	private final List<Exchange> inventory = new ArrayList<>();

	@Column(name = "product")
	private String productName;

	@Column(name = "productSystem")
	private String productSystemName;

	@Column(name = "targetamount")
	private double targetAmount;

	@Column(name = "unit")
	private String unitName;

	@Column(name = "calculationmethod")
	private String calculationMethod;

	public void setProductSystemId(String productSystemId) {
		this.productSystemId = productSystemId;
	}

	public String getProductSystemId() {
		return productSystemId;
	}

	public void setProductSystemName(String productSystemName) {
		this.productSystemName = productSystemName;
	}

	public String getProductSystemName() {
		return productSystemName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductName() {
		return productName;
	}

	public String getCalculationMethod() {
		return calculationMethod;
	}

	public void setCalculationMethod(String calculationMethod) {
		this.calculationMethod = calculationMethod;
	}

	/** Returns a live list of the result exchanges. */
	public List<Exchange> getInventory() {
		return inventory;
	}

	public double getTargetAmount() {
		return targetAmount;
	}

	public void setTargetAmount(double targetAmount) {
		this.targetAmount = targetAmount;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

}
