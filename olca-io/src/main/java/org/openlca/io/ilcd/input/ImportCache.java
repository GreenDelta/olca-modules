package org.openlca.io.ilcd.input;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.openlca.commons.Strings;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Location;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.util.KeyGen;

class ImportCache {

	private final Import imp;

	private Map<String, ImpactMethodDescriptor> methods;
	private Map<String, Location> locations;

	ImportCache(Import imp) {
		this.imp = imp;
	}

	Location locationOf(String code) {
		if (Strings.isBlank(code))
			return null;

		// build the location index if not done yet
		if (locations == null) {
			locations = new HashMap<>();
			for (var loc : imp.db().getAll(Location.class)) {
				if (Strings.isNotBlank(loc.code)) {
					locations.put(norm(loc.code), loc);
				}
				if (Strings.isNotBlank(loc.name)) {
					locations.put(norm(loc.name), loc);
				}
				if (Strings.isNotBlank(loc.refId)) {
					locations.put(norm(loc.refId), loc);
				}
			}
		}

		var cached = locations.get(norm(code));
		if (cached != null)
			return cached;
		var refId =  KeyGen.get(code);
		cached = locations.get(refId);
		if (cached != null)
			return cached;

		var loc = new Location();
		loc.refId = refId;
		loc.code = code;
		loc.name = code;
		imp.db().insert(loc);
		imp.log().imported(loc);
		locations.put(norm(code), loc);
		locations.put(refId, loc);
		return loc;
	}

	private String norm(String s) {
		return s != null
			? s.trim().toLowerCase(Locale.ROOT)
			: "";
	}

	/**
	 * Returns the impact method of the given name that was created during the
	 * import. As impact methods do not have UUIDs in ILCD (in fact "ILCD LCIA
	 * methods" are impact categories or indicators) we need to identify them by
	 * name. If an impact category is newly created in an import, and it has one
	 * or more LCIA method references (by name) we also create the LCIA methods
	 * then (also if there is a method with the same name already in openLCA).
	 */
	ImpactMethodDescriptor impactMethodOf(String name) {
		if (Strings.isBlank(name))
			return null;
		if (methods == null) {
			methods = new HashMap<>();
		}
		var key = name.trim().toLowerCase();
		var descriptor = methods.get(key);
		if (descriptor != null) {
			return descriptor;
		}
		var method = imp.db().insert(ImpactMethod.of(name));
		imp.log().imported(method);
		descriptor = Descriptor.of(method);
		methods.put(key, descriptor);
		return descriptor;
	}
}
