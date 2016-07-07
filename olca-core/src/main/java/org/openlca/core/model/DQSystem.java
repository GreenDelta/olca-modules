package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_dq_systems")
public class DQSystem extends CategorizedEntity {

	@Column(name = "has_uncertainties")
	public boolean hasUncertainties;

	@Column(name = "summable")
	public boolean summable;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "f_dq_system")
	public final List<DQIndicator> indicators = new ArrayList<>();

	@Override
	public DQSystem clone() {
		DQSystem clone = new DQSystem();
		Util.cloneRootFields(this, clone);
		clone.hasUncertainties = hasUncertainties;
		clone.summable = summable;
		for (DQIndicator indicator : indicators) {
			clone.indicators.add(indicator.clone());
		}
		return clone;
	}

	public int getScoreCount() {
		if (indicators == null || indicators.isEmpty())
			return 0;
		if (indicators.get(0).scores == null)
			return 0;
		return indicators.get(0).scores.size();
	}

	public String getScoreLabel(int pos) {
		if (indicators == null || indicators.isEmpty())
			return null;
		if (indicators.get(0).scores == null)
			return null;
		for (DQScore score : indicators.get(0).scores)
			if (score.position == pos)
				return score.label;
		return null;
	}

	public void setScoreLabel(int pos, String label) {
		if (indicators == null || indicators.isEmpty())
			return;
		if (indicators.get(0).scores == null)
			return;
		if (indicators.get(0).scores.size() < pos)
			return;
		for (DQIndicator indicator : indicators)
			for (DQScore score : indicator.scores)
				if (score.position == pos)
					score.label = label;
	}

	public DQIndicator getIndicator(int pos) {
		for (DQIndicator indicator : indicators)
			if (indicator.position == pos)
				return indicator;
		return null;
	}

}
