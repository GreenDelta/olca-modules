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

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "f_dq_system")
	public final List<DQIndicator> indicators = new ArrayList<>();

	@Override
	public DQSystem clone() {
		DQSystem clone = new DQSystem();
		Util.cloneRootFields(this, clone);
		clone.hasUncertainties = hasUncertainties;
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

	public String toString(int... values) {
		if (values == null || values.length == 0)
			return null;
		if (values.length < indicators.size()) {
			int[] newValues = new int[indicators.size()];
			for (int i = 0; i < values.length; i++) {
				newValues[i] = values[i];
			}
			values = newValues;
		}
		boolean atLeastOneValue = false;
		String s = null;
		for (int pos = 1; pos <= indicators.size(); pos++) {
			int value = values[pos - 1];
			DQIndicator indicator = getIndicator(pos);
			if (indicator == null || indicator.getScore(value) == null) {
				value = 0;
			}
			s = s == null ? "(" : s + ";";
			if (value == 0) {
				s += "n.a.";
			} else {
				s += value;
				atLeastOneValue = true;
			}
		}
		if (!atLeastOneValue)
			return null;
		return s + ")";
	}

	public int[] toValues(String s) {
		if (s == null || s.length() <= 2)
			return new int[indicators.size()];
		String[] sValues = s.substring(1, s.length() - 1).split(";");
		if (sValues == null || sValues.length == 0)
			return new int[indicators.size()];
		int[] values = new int[indicators.size()];
		for (int pos = 1; pos <= sValues.length; pos++) {
			DQIndicator indicator = getIndicator(pos);
			String sValue = sValues[pos - 1];
			if (indicator == null || "n.a.".equals(sValue))
				continue;
			try {
				DQScore score = indicator.getScore(Integer.parseInt(sValue));
				if (score == null)
					continue;
				values[pos - 1] = score.position;
			} catch (NumberFormatException e) {
				// ignore
			}
		}
		return values;
	}

}
