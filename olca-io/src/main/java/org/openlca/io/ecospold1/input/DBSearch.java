package org.openlca.io.ecospold1.input;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openlca.commons.Strings;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.Query;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Category;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Location;
import org.openlca.core.model.Source;
import org.openlca.ecospold.model.IExchange;
import org.openlca.ecospold.model.IPerson;
import org.openlca.ecospold.model.IReferenceFunction;
import org.openlca.ecospold.model.ISource;
import org.openlca.ecospold.model.DataSet;
import org.openlca.io.UnitMappingEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Search for EcoSpold entities in the database. */
class DBSearch {

	private final IDatabase database;
	private final Logger log = LoggerFactory.getLogger(getClass());

	public DBSearch(IDatabase database) {
		this.database = database;
	}

	public Actor findActor(IPerson person) {
		try {
			log.trace("Search for actor {} in database", person.getName());
			String jpql = "select a from Actor a where a.name = :name "
					+ "and a.address = :address";
			Map<String, Object> args = new HashMap<>();
			args.put("name", person.getName());
			args.put("address", person.getAddress());
			return Query.on(database).getFirst(Actor.class, jpql, args);
		} catch (Exception e) {
			log.error("Failed to search for Actor", e);
			return null;
		}
	}

	public Source findSource(ISource source) {
		try {
			log.trace("Search for source {} in database", source.getTitle());
			String jpql = "select s from Source s where s.name = :name";
			Map<String, Object> args = new HashMap<>();
			int year = source.getYear() != null ? source.getYear().getYear()
					: 0;
			args.put("name", source.getFirstAuthor() + " " + year);
			Source candidate = Query.on(database).getFirst(Source.class, jpql,
					args);
			if (candidate == null)
				return null;
			if (Objects.equals(candidate.textReference, source.getTitle()))
				return candidate;
			return null;
		} catch (Exception e) {
			log.error("Failed to search for Source", e);
			return null;
		}
	}

	public Location findLocation(String locationCode) {
		try {
			log.trace("Search for location {} in database", locationCode);
			String jpql = "select loc from Location loc "
					+ "where loc.code = :locationCode";
			return Query.on(database).getFirst(Location.class, jpql,
					Collections.singletonMap("locationCode", locationCode));
		} catch (Exception e) {
			log.error("Failed to search for Location", e);
			return null;
		}
	}

	public Flow findFlow(IExchange exchange, UnitMappingEntry mapping) {
		List<Flow> candidates = findFlows(exchange.getName());
		if (candidates == null || candidates.isEmpty())
			return null;
		for (Flow flow : candidates) {
			if (!sameFlowType(exchange, flow))
				continue;
			if (!hasUnit(flow, mapping))
				continue;
			if (!sameCategory(exchange.getCategory(),
					exchange.getSubCategory(), flow))
				continue;
			if (!sameLocation(exchange.getLocation(), flow))
				continue;
			return flow;
		}
		return null;
	}

	public Flow findFlow(DataSet dataSet, UnitMappingEntry mapping) {
		IReferenceFunction refFun = dataSet.getReferenceFunction();
		if (refFun == null)
			return null;
		List<Flow> candidates = findFlows(refFun.getName());
		if (candidates == null || candidates.isEmpty())
			return null;
		for (Flow flow : candidates) {
			if (!hasUnit(flow, mapping))
				continue;
			if (!sameCategory(refFun.getCategory(), refFun.getSubCategory(),
					flow))
				continue;
			String locationCode = dataSet.getGeography() == null ? null
					: dataSet.getGeography().getLocation();
			if (!sameLocation(locationCode, flow))
				continue;
			return flow;
		}
		return null;
	}

	private List<Flow> findFlows(String name) {
		try {
			log.trace("Search for flow {} in database", name);
			String jpql = "select f from Flow f where f.name = :name";
			return Query.on(database).getAll(Flow.class, jpql,
					Collections.singletonMap("name", name));
		} catch (Exception e) {
			log.error("Flow search failed", e);
			return Collections.emptyList();
		}
	}

	private boolean sameFlowType(IExchange exchange, Flow flow) {
		if (exchange.isElementaryFlow())
			return flow.flowType == FlowType.ELEMENTARY_FLOW;
		return flow.flowType != FlowType.ELEMENTARY_FLOW;
	}

	private boolean hasUnit(Flow flow, UnitMappingEntry mapping) {
		if (flow == null || mapping == null
				|| mapping.flowProperty == null)
			return false;
		return flow.getFactor(mapping.flowProperty) != null;
	}

	private boolean sameCategory(String categoryName, String subCategoryName,
			Flow flow) {
		try {
			Category category = flow.category;
			if (category == null)
				return Strings.isBlank(categoryName)
					&& Strings.isBlank(subCategoryName);
			Category parent = category.category;
			if (parent == null)
				return sameCategory(categoryName, category)
						|| sameCategory(subCategoryName, category);
			else
				return sameCategory(subCategoryName, category)
						&& sameCategory(categoryName, parent);
		} catch (Exception e) {
			log.error("Failed to check categories");
			return false;
		}
	}

	private boolean sameCategory(String name, Category category) {
		if (Strings.isBlank(name) && category == null)
			return true;
		if (Strings.isBlank(name) || category == null)
			return false;
		return Strings.equalsIgnoreCase(name, category.name);
	}

	private boolean sameLocation(String locationCode, Flow flow) {
		if (locationCode == null || locationCode.equals("GLO"))
			return flow.location == null
					|| "GLO".equalsIgnoreCase(flow.location.code);
		if (flow.location == null)
			return false;
		return Strings.equalsIgnoreCase(locationCode, flow.location.code);
	}
}
