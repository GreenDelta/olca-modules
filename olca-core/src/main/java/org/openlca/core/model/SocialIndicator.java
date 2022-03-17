package org.openlca.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_social_indicators")
public class SocialIndicator extends RootEntity {

	/**
	 * Name of the activity variable.
	 */
	@Column(name = "activity_variable")
	public String activityVariable;

	/**
	 * Quantity of the activity variable.
	 */
	@OneToOne
	@JoinColumn(name = "f_activity_quantity")
	public FlowProperty activityQuantity;

	/**
	 * Unit of the activity variable.
	 */
	@OneToOne
	@JoinColumn(name = "f_activity_unit")
	public Unit activityUnit;

	/**
	 * (Raw) unit of measurement of the indicator.
	 */
	@Column(name = "unit_of_measurement")
	public String unitOfMeasurement;

	@Column(name = "evaluation_scheme")
	public String evaluationScheme;

	public static SocialIndicator of(String name, FlowProperty quantity) {
		var indicator = new SocialIndicator();
		Entities.init(indicator, name);
		indicator.activityQuantity = quantity;
		if (quantity != null) {
			indicator.activityUnit = quantity.getReferenceUnit();
		}
		return indicator;
	}

	@Override
	public SocialIndicator copy() {
		var clone = new SocialIndicator();
		Entities.copyFields(this, clone);
		clone.activityVariable = activityVariable;
		clone.activityQuantity = activityQuantity;
		clone.activityUnit = activityUnit;
		clone.unitOfMeasurement = unitOfMeasurement;
		clone.evaluationScheme = evaluationScheme;
		return clone;
	}
}
