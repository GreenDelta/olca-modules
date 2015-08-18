package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_social_indicators")
public class SocialIndicator extends CategorizedEntity {

	/**
	 * The unit of measurement of the indicator.
	 */
	@Column(name = "unit")
	public String unit;

	@Column(name = "evaluation_scheme")
	public String evaluationScheme;

	@Override
	public Object clone() {
		SocialIndicator clone = new SocialIndicator();
		Util.cloneRootFields(this, clone);
		clone.setCategory(getCategory());
		clone.unit = unit;
		clone.evaluationScheme = evaluationScheme;
		return clone;
	}
}
