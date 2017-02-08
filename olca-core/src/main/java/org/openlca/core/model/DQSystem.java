package org.openlca.core.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tbl_dq_systems")
public class DQSystem extends CategorizedEntity {

	@Column(name = "has_uncertainties")
	public boolean hasUncertainties;

	@OneToOne
	@JoinColumn(name = "f_source")
	public Source source;

	@JoinColumn(name = "f_dq_system")
	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	public final List<DQIndicator> indicators = new ArrayList<>();

	@Override
	public DQSystem clone() {
		DQSystem clone = new DQSystem();
		Util.cloneRootFields(this, clone);
		clone.hasUncertainties = hasUncertainties;
		for (DQIndicator indicator : indicators) {
			clone.indicators.add(indicator.clone());
		}
		clone.source = source;
		return clone;
	}

	public int getScoreCount() {
		if (indicators == null || indicators.isEmpty())
			return 0;
		return indicators.get(0).scores.size();
	}

	public String getScoreLabel(int pos) {
		if (indicators == null || indicators.isEmpty())
			return null;
		for (DQScore score : indicators.get(0).scores) {
			if (score.position == pos)
				return score.label;
		}
		return null;
	}

	public void setScoreLabel(int pos, String label) {
		if (indicators == null || indicators.isEmpty())
			return;
		if (indicators.get(0).scores.size() < pos)
			return;
		for (DQIndicator indicator : indicators) {
			for (DQScore score : indicator.scores) {
				if (score.position == pos)
					score.label = label;
			}
		}
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
		int[] values = new int[indicators.size()];
		if (s == null)
			return values;
		String raw = s.trim();
		if (raw.length() <= 2)
			return values;
		String[] strings = raw.substring(1, raw.length() - 1).split(";");
		for (int pos = 1; pos <= strings.length; pos++) {
			DQIndicator indicator = getIndicator(pos);
			String string = strings[pos - 1].trim();
			if (indicator == null || "n.a.".equals(string))
				continue;
			try {
				int val = Integer.parseInt(string);
				DQScore score = indicator.getScore(val);
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
