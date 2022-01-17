package org.openlca.ilcd.epd.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import org.openlca.ilcd.commons.ExchangeDirection;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.epd.conversion.RefExtension;
import org.openlca.ilcd.epd.util.Strings;
import org.openlca.ilcd.processes.Exchange;
import org.openlca.ilcd.processes.LCIAResult;

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

	public Indicator indicatorOf(Exchange exchange) {
		if (exchange == null || exchange.flow == null)
			return null;
		return indicator(exchange.flow, exchange.other, newIndicator -> {
			newIndicator.type = Indicator.Type.LCI;
			newIndicator.isInput = exchange.direction == ExchangeDirection.INPUT;
		});
	}

	public Indicator indicatorOf(LCIAResult result) {
		if (result == null || result.method == null)
			return null;
		return indicator(result.method, result.other,
			newIndicator -> newIndicator.type = Indicator.Type.LCIA);
	}

	private Indicator indicator(Ref ref, Other ext, Consumer<Indicator> ifNew) {
		for (var i : indicators) {
			if (Objects.equals(i.uuid, ref.uuid))
				return i;
		}
		var indicator = new Indicator();
		ifNew.accept(indicator);
		indicator.uuid = ref.uuid;
		indicator.name = LangString.getFirst(ref.name, "en");
		RefExtension.readFrom(ext, "referenceToUnitGroupDataSet")
			.ifPresent(groupRef -> {
				indicator.group = groupRef.uuid;
				indicator.unit = LangString.getFirst(groupRef.name, "en");
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
		return id == null
			? super.hashCode()
			: id.hashCode();
	}
}
