package org.openlca.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_social_aspects")
public class SocialAspect extends AbstractEntity
	implements Copyable<SocialAspect> {

	@OneToOne
	@JoinColumn(name = "f_indicator")
	public SocialIndicator indicator;

	/** The value for the activity variable defined in the indicator. */
	@Column(name = "activity_value")
	public double activityValue;

	/**
	 * The raw amount of the indicator's unit of measurement (not required to be
	 * numeric)
	 */
	@Column(name = "raw_amount")
	public String rawAmount;

	@Column(name = "risk_level")
	@Enumerated(EnumType.STRING)
	public RiskLevel riskLevel;

	@Column(name = "comment")
	public String comment;

	@OneToOne
	@JoinColumn(name = "f_source")
	public Source source;

	/** Encoded data quality, e.g.: (3,1,2,4,1) */
	@Column(name = "quality")
	public String quality;

	@Override
	public SocialAspect copy() {
		var clone = new SocialAspect();
		clone.indicator = indicator;
		clone.activityValue = activityValue;
		clone.rawAmount = rawAmount;
		clone.riskLevel = riskLevel;
		clone.comment = comment;
		clone.source = source;
		clone.quality = quality;
		return clone;
	}

}
