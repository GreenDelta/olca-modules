package org.openlca.simapro.csv.model;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a SimaPro quantity
<<<<<<< Updated upstream
=======
 * 
>>>>>>> Stashed changes
 */
public class SPQuantity {

	/**
	 * Indicates if the quantity is dimensional
	 */
	private boolean dimensional = true;

	/**
	 * The name of the quantity
	 */
	private String name;

	/**
	 * The reference unit of the quantity
	 */
	private SPUnit referenceUnit;

	/**
	 * The units of the quantity
	 */
	private List<SPUnit> units = new ArrayList<SPUnit>();

	/**
	 * Creates a new quantity
	 * 
	 * @param name
	 *            The name of the quantity
	 * @param referenceUnit
	 *            The reference unit of the quantity
	 */
	public SPQuantity(String name, SPUnit referenceUnit) {
		this.name = name;
		this.referenceUnit = referenceUnit;
		this.units.add(referenceUnit);
	}

	/**
	 * Creates a new quantity
	 * 
	 * @param name
	 *            The name of the quantity
	 * @param referenceUnit
	 *            The reference unit of the quantity
	 * @param dimensional
	 *            Indicates if the quantity is dimensional
	 */
	public SPQuantity(String name, SPUnit referenceUnit, boolean dimensional) {
		this.name = name;
		this.referenceUnit = referenceUnit;
		this.dimensional = dimensional;
		this.units.add(referenceUnit);
	}

	/**
	 * Adds a unit to the quantity
	 * 
	 * @param unit
	 *            The unit to add
	 */
	public void add(SPUnit unit) {
		this.units.add(unit);
	}

	/**
	 * Getter of the name
	 * 
	 * @return The name of the quantity
	 */
	public String getName() {
		return name;
	}

	/**
	 * Getter of the reference unit
	 * 
	 * @see SPUnit
	 * @return The reference unit of the quantity
	 */
	public SPUnit getReferenceUnit() {
		return referenceUnit;
	}

	/**
	 * Getter of the units
	 * 
	 * @see SPUnit
	 * @return The units of the quantity
	 */
	public SPUnit[] getUnits() {
		return units.toArray(new SPUnit[units.size()]);
	}

	/**
	 * Getter of the dimension
	 * 
	 * @return true if the quantity is dimensional, false otherwise
	 */
	public boolean isDimensional() {
		return dimensional;
	}

	/**
	 * Setter of the dimensional value
	 * 
	 * @param dimensional
	 *            The new dimensional value
	 */
	public void setDimensional(boolean dimensional) {
		this.dimensional = dimensional;
	}

	/**
	 * Setter of the name
	 * 
	 * @param name
	 *            The new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Setter of the reference unit
	 * 
	 * @param referenceUnit
	 *            The new reference unit
	 */
	public void setReferenceUnit(SPUnit referenceUnit) {
		this.referenceUnit = referenceUnit;
	}

}
