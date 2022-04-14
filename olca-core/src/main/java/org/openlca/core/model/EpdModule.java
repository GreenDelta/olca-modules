package org.openlca.core.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tbl_epd_modules")
public class EpdModule extends AbstractEntity implements Copyable<EpdModule> {

	@Column(name = "name")
	public String name;

	@OneToOne
	@JoinColumn(name = "f_result")
	public Result result;

	/**
	 * The multiplier for the linked result. This scales the result to the
	 * required amount of this module.
	 */
	@Column(name = "multiplier")
	public double multiplier = 1;

	public static EpdModule of(String name, Result result) {
		var module = new EpdModule();
		module.name = name;
		module.result = result;
		module.multiplier = 1;
		return module;
	}

	@Override
	public EpdModule copy() {
		var copy = new EpdModule();
		copy.name = name;
		copy.result = result;
		copy.multiplier = multiplier;
		return copy;
	}
}
