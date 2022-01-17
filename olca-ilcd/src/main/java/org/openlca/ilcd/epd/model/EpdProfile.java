package org.openlca.ilcd.epd.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.epd.conversion.RefExtension;
import org.openlca.ilcd.epd.util.Strings;
import org.openlca.ilcd.processes.Exchange;

public class EpdProfile {

	public String id;
	public String name;
	public String description;
	public String referenceDataUrl;
	public final List<Indicator> indicators = new ArrayList<>();
	public final List<Module> modules = new ArrayList<>();

	public static EpdProfile create() {
		var profile = new EpdProfile();
		profile.id = UUID.randomUUID().toString();
		return profile;
	}

	/**
	 * Get the indicator with the given ID from this profile.
	 */
	public Indicator indicator(Exchange exchange) {
		if (exchange == null)
			return null;
		var flowRef = exchange.flow;
		if (flowRef == null)
			return null;
		var uuid = flowRef.uuid;
		for (var i : indicators) {
			if (Objects.equals(i.uuid, uuid))
				return i;
		}

		var indicator = new Indicator();
		indicator.type = Indicator.Type.LCI;
		indicator.isInput = exchange.direction == ExchangeDirection.INPUT;
		indicator.uuid = uuid;
		indicator.name = LangString.getFirst(flowRef.name, "en");

		RefExtension.readFrom(exchange.other, "referenceToUnitGroupDataSet")
			.ifPresent(ref -> {
				indicator.group = ref.uuid;
				indicator.unit = LangString.getFirst(ref.name, "en");
			});

		indicators.add(indicator);
		return indicator;
	}

	/**
	 * Get the module for the given name from the profile. Adds a new module
	 * with the given name if there is no such module in this profile.
	 */
	public Module module(String name) {
		for (var module : modules) {
			if (Strings.nullOrEqual(name, module.name))
				return module;
		}
		var module = new Module();
		module.name = name;
		module.index = modules.size();
		modules.add(module);
		return module;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		if (obj == this)
			return true;
		if (!(obj instanceof EpdProfile other))
			return false;
		return Objects.equals(this.id, other.id);
	}

	@Override
	public int hashCode() {
		if (id == null)
			return super.hashCode();
		return id.hashCode();
	}
}
