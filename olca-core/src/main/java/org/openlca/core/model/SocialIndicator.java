package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_social_indicators")
public class SocialIndicator extends CategorizedEntity {

	/** Name of the activity variable. */
	@Column(name = "activity_variable")
	public String activityVariable;

	/** Quantity of the activity variable. */
	@OneToOne
	@JoinColumn(name = "f_activity_quantity")
	public FlowProperty activityQuantity;

	/** Unit of the activity variable. */
	@OneToOne
	@JoinColumn(name = "f_activity_unit")
	public Unit activityUnit;

	/** (Raw) unit of measurement of the indicator. */
	@Column(name = "unit_of_measurement")
	public String unitOfMeasurement;

	@Column(name = "evaluation_scheme")
	public String evaluationScheme;

	@Override
	public SocialIndicator clone() {
		var clone = new SocialIndicator();
		Util.copyFields(this, clone);
		clone.activityVariable = activityVariable;
		clone.activityQuantity = activityQuantity;
		clone.activityUnit = activityUnit;
		clone.unitOfMeasurement = unitOfMeasurement;
		clone.evaluationScheme = evaluationScheme;
		return clone;
	}
}
