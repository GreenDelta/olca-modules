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

import org.openlca.util.Strings;

/**
 * A data quality system (DQS) in openLCA describes a pedigree matrix of $m$
 * data quality indicators (DQIs) and $n$ data quality scores (DQ scores). Such
 * a system can then be used to assess the data quality of processes and
 * exchanges by tagging them with an instance of the system $D$ where $D$ is a
 * $m * n$ matrix with an entry $d_{ij}$ containing the value of the data
 * quality score $j$ for indicator $i$.
 *
 * As each indicator in $D$ can only have a single score value, $D$ can be
 * stored in a vector $d$ where $d_i$ contains the data quality score for
 * indicator $i$. The possible values of the data quality scores are defined as
 * a linear order $1 \dots n$. In openLCA, the data quality entry $d$ of a
 * process or exchange is stored as a string like `(3;2;4;n.a.;2)` which means
 * the data quality score for the first indicator is `3`, for the second `2`
 * etc. A specific value is `n.a.` which stands for _not applicable_.
 *
 * In calculations, these data quality entries can be aggregated in different
 * ways. For example, the data quality entry of a flow $f$ with a contribution
 * of `0.5 kg` and a data quality entry of `(3;2;4;n.a.;2)` in a process $p$ and
 * a contribution of `1.5 kg` and a data quality entry of `(2;3;1;n.a.;5)` in a
 * process $q$ could be aggregated to `(2;3;2;n.a.;4)` by applying an weighted
 * average and rounding.
 *
 * Finally, custom labels like `A, B, C, ...` or `Very good, Good, Fair, ...`
 * for the DQ scores can be assigned by the user. These labels are then
 * displayed instead of `1, 2, 3 ...` in the user interface or result exports.
 * However, internally the numeric values are used in the data model and
 * calculations.
 */
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
		var clone = new DQSystem();
		Util.copyFields(this, clone);
		clone.hasUncertainties = hasUncertainties;
		for (DQIndicator indicator : indicators) {
			clone.indicators.add(indicator.clone());
		}
		clone.source = source;
		return clone;
	}

	/**
	 * Get the number of scores $n$ of the data quality system.
	 */
	public int getScoreCount() {
		if (indicators.isEmpty())
			return 0;
		return indicators.get(0).scores.size();
	}

	/**
	 * Get the (possible custom) label of the score $j$ which is $j$ by default.
	 * Note that the indices for indicators and scores are 1-based. Also, the
	 * returned value is null if the given score is not defined in this system.
	 */
	public String getScoreLabel(int j) {
		if (indicators.isEmpty())
			return null;
		for (DQScore score : indicators.get(0).scores) {
			if (score.position == j)
				return score.label == null
						? Integer.toString(j)
						: score.label;
		}
		return null;
	}

	/**
	 * Set a (custom) label of the score $j$. Note that the indices for indicators
	 * and scores are 1-based.
	 */
	public void setScoreLabel(int pos, String label) {
		if (indicators.isEmpty())
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

	/**
	 * Get the indicator $i$ of this data quality system. Note that the indices for
	 * indicators and scores are 1-based.
	 */
	public DQIndicator getIndicator(int i) {
		for (DQIndicator indicator : indicators) {
			if (indicator.position == i)
				return indicator;
		}
		return null;
	}

	/**
	 * Convert the give data quality entry $d$, e.g. `[2, 3, 4, 0, 1]`, to the
	 * string serialization format, e.g. `"(2;3;4;n.a.;1)"`.
	 */
	public String toString(int... d) {
		if (d == null || d.length == 0)
			return null;
		String s = "(";
		int n = getScoreCount();
		for (int i = 0; i < indicators.size(); i++) {
			if (i > 0) {
				s += ";";
			}
			if (d.length <= i || d[i] <= 0 || d[i] > n) {
				s += "n.a.";
				continue;
			}
			s += d[i];
		}
		return s + ")";
	}

	/**
	 * Convert a DQ entry in serialized format, e.g. `"(2;3;4;n.a.;1)"` to an
	 * integer array with score values, e.g. `[2, 3, 4, 0, 1]`.
	 */
	public int[] toValues(String s) {
		int[] values = new int[indicators.size()];
		if (s == null)
			return values;
		String raw = s.trim();
		if (raw.length() <= 2)
			return values;
		int n = getScoreCount();
		String[] nums = raw.substring(1, raw.length() - 1).split(";");
		for (int i = 0; i < indicators.size(); i++) {
			if (nums.length <= i)
				break;
			String num = nums[i].trim();
			if ("n.a.".equals(num)) {
				continue;
			}
			try {
				int val = Integer.parseInt(num);
				if (val > 0 && val <= n) {
					values[i] = val;
				}
			} catch (NumberFormatException ignored) {
			}
		}
		return values;
	}

	/**
	 * Applies the defined score labels of this data quality system to the given
	 * serialized form of a data quality entry.
	 */
	public String applyScoreLabels(String entry) {
		if (Strings.nullOrEmpty(entry))
			return "";
		int[] values = toValues(entry);
		if (values == null || values.length == 0)
			return "";
		String s = "(";
		for (int i = 0; i < values.length; i++) {
			if (i > 0) {
				s += "; ";
			}
			if (values[i] <= 0) {
				s += "n.a.";
				continue;
			}
			s += getScoreLabel(values[i]);
		}
		return s + ")";
	}
}
