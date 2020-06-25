package org.openlca.core.database.usage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.openlca.core.database.EntityCache;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.ProjectDao;
import org.openlca.core.model.CategorizedEntity;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterRedef;
import org.openlca.core.model.descriptors.CategorizedDescriptor;
import org.openlca.core.model.descriptors.Descriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.util.Formula;
import org.openlca.util.Strings;

import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.set.hash.TLongHashSet;

/**
 * Calculates the usage tree for a parameter.
 */
public class ParameterUsageTree {

	public final String param;
	public final List<Node> nodes;

	private ParameterUsageTree(String param, List<Node> nodes) {
		this.param = param;
		this.nodes = nodes;
	}

	public static ParameterUsageTree empty() {
		return new ParameterUsageTree("", Collections.emptyList());
	}

	/**
	 * Calculates a usage tree for a parameter with the given name. This returns all
	 * places in the database where this parameter is used, including the global and
	 * local definitions of that parameter itself. Note that this is more a search
	 * of the parameter name than a real usage tree of a parameter as there can be
	 * multiple parameters with the same name defined in the database.
	 */
	public static ParameterUsageTree of(String param, IDatabase db) {
		if (Strings.nullOrEmpty(param) || db == null)
			return empty();
		return new Search(param.trim(), db).doIt();
	}

	/**
	 * Calculates the usage tree of the given global parameter.
	 */
	public static ParameterUsageTree of(Parameter globalParam, IDatabase db) {
		return of(globalParam, null, db);
	}

	public static ParameterUsageTree of(
			Parameter param, CategorizedDescriptor owner, IDatabase db) {
		if (param == null
				|| Strings.nullOrEmpty(param.name)
				|| db == null)
			return empty();
		return new Search(param, owner, db).doIt();
	}

	public boolean isEmpty() {
		return nodes.isEmpty();
	}

	public enum UsageType {
		DEFINITION,
		REDEFINITION,
		FORMULA,
	}

	public static class Node implements Comparable<Node> {

		public final long id;
		public final String name;
		public final CategorizedDescriptor model;

		public UsageType usageType;
		public String usage;

		public Node parent;
		public final List<Node> childs = new ArrayList<>();

		Node(long id, String name) {
			this.id = id;
			this.name = name;
			this.model = null;
		}

		Node(CategorizedEntity e) {
			this(Descriptor.of(e));
		}

		Node(CategorizedDescriptor model) {
			this.id = model.id;
			this.name = model.name;
			this.model = model;
		}

		Node of(UsageType type, String usage) {
			this.usageType = type;
			this.usage = usage;
			return this;
		}

		private Node add(Node child) {
			if (child == null)
				return null;
			child.parent = this;
			childs.add(child);
			return this;
		}

		private Node addIfAbsent(long id, Supplier<Node> fn) {
			for (var child : childs) {
				if (child.id == id)
					return child;
			}
			return add(fn.get());
		}

		public Node root() {
			if (parent == null)
				return this;
			return parent.root();
		}

		@Override
		public int compareTo(Node other) {
			if (other == null)
				return 1;
			if (this.model == null && other.model == null)
				return Strings.compare(this.name, other.name);
			if (this.model == null || other.model == null)
				return this.model == null ? 1 : -1;
			int o1 = typeOrder(this.model.type);
			int o2 = typeOrder(other.model.type);
			if (o1 == o2)
				return Strings.compare(
						this.name, other.name);
			return o1 - o2;
		}

		private static int typeOrder(ModelType type) {
			if (type == null)
				return -1;
			switch (type) {
				case PARAMETER:
					return 0;
				case PROJECT:
					return 1;
				case PRODUCT_SYSTEM:
					return 2;
				case PROCESS:
					return 3;
				case IMPACT_CATEGORY:
					return 4;
				case FLOW:
					return 5;
				default:
					return 99;
			}
		}
	}

	private static class Search {

		private final String name;
		private final IDatabase db;
		private final EntityCache cache;

		// optional parameter context
		private Parameter param;
		private CategorizedDescriptor owner;

		private final HashMap<Long, Node> roots = new HashMap<>();

		/**
		 * This is only needed when we search for the usages of a global parameter. This
		 * set then contains the IDs of the entities that have a local parameter with
		 * the same name defined. Formulas that are local to these entities should be
		 * then excluded from the usage tree.
		 */
		private final TLongHashSet hasLocalDef = new TLongHashSet();

		Search(String param, IDatabase db) {
			this.name = param == null ? "" : param;
			this.db = db;
			this.param = null;
			this.owner = null;
			this.cache = EntityCache.create(db);
		}

		Search(Parameter param, CategorizedDescriptor owner, IDatabase db) {
			this(param.name, db);
			this.param = param;
			this.owner = owner;

			if (owner == null) {
				var sql = "select f_owner, name from tbl_parameters";
				NativeSql.on(db).query(sql, r -> {
					long ownerID = r.getLong(1);
					if (r.wasNull() || ownerID <= 0)
						return true;
					var name = r.getString(2);
					if (matches(name)) {
						hasLocalDef.add(ownerID);
					}
					return true;
				});
			}
		}

		ParameterUsageTree doIt() {
			exchanges();
			allocationFactors();
			impacts();
			parameters();
			systemRedefs();
			projectRedefs();
			var roots = new ArrayList<>(this.roots.values());
			sortRec(roots);
			return new ParameterUsageTree(name, roots);
		}

		private void sortRec(List<Node> nodes) {
			if (nodes == null)
				return;
			for (Node n : nodes) {
				sortRec(n.childs);
			}
			Collections.sort(nodes);
		}

		private void exchanges() {
			String sql = "SELECT f_owner, f_flow, "
					+ "resulting_amount_formula FROM tbl_exchanges "
					+ " WHERE resulting_amount_formula IS NOT NULL";
			NativeSql.on(db).query(sql, r -> {
				var formula = r.getString(3);
				if (!matches(formula))
					return true;
				long ownerID = r.getLong(1);
				if (skipOwner(ownerID))
					return true;
				var root = root(ownerID, ProcessDescriptor.class);
				var flow = cache.get(FlowDescriptor.class, r.getLong(2));
				if (root == null || flow == null)
					return true;
				root.add(new Node(flow).of(UsageType.FORMULA, formula));
				return true;
			});
		}

		private void allocationFactors() {
			var sql = "SELECT id, f_process, formula FROM " +
					"tbl_allocation_factors WHERE formula IS NOT NULL";
			NativeSql.on(db).query(sql, r -> {
				var formula = r.getString(3);
				if (!matches(formula))
					return true;
				long ownerID = r.getLong(2);
				if (skipOwner(ownerID))
					return true;
				var root = root(ownerID, ProcessDescriptor.class);
				if (root == null)
					return true;
				var child = new Node(r.getLong(1), "allocation factor")
						.of(UsageType.FORMULA, formula);
				root.add(child);
				return true;
			});
		}

		private void impacts() {
			String sql = "SELECT cat.id AS category," +
					"  fac.f_flow AS flow," +
					"  fac.formula AS FORMULA" +
					"  FROM tbl_impact_factors fac" +
					"  INNER JOIN tbl_impact_categories cat" +
					"  ON fac.f_impact_category = cat.id" +
					"  WHERE fac.formula IS NOT NULL";
			NativeSql.on(db).query(sql, r -> {
				String formula = r.getString(3);
				if (!matches(formula))
					return true;
				long ownerID = r.getLong(1);
				if (skipOwner(ownerID))
					return true;
				var root = root(ownerID, ImpactCategoryDescriptor.class);
				var flow = cache.get(FlowDescriptor.class, r.getLong(2));
				if (root == null || flow == null)
					return true;
				root.add(new Node(flow).of(UsageType.FORMULA, formula));
				return true;
			});
		}

		private boolean skipOwner(long ownerID) {
			// if an owner is set, the ID must match, otherwise skip it
			if (owner != null)
				return ownerID != owner.id;
			// no owner but parameter means global parameter
			// skip formulas when the owner has a local parameter
			// with the same name
			if (param != null)
				return hasLocalDef.contains(ownerID);
			return false;
		}

		private void parameters() {

			// collect the parameter owners: id -> owner id
			var sql = "select id, f_owner from tbl_parameters";
			var owners = new TLongLongHashMap();
			NativeSql.on(db).query(sql, r -> {
				long id = r.getLong(1);
				long owner = r.getLong(2);
				if (!r.wasNull() && owner > 0L) {
					owners.put(id, owner);
				}
				return true;
			});

			if (owner != null) {
				// search only in formulas of local parameters
				for (var p : new ParameterDao(db).getAll()) {
					long ownerID = owners.get(p.id);
					if (ownerID != owner.id
							|| p.isInputParameter
							|| !matches(p.formula))
						continue;
					roots.computeIfAbsent(owner.id, id -> new Node(owner))
							.add(new Node(p).of(UsageType.FORMULA, p.formula));
				}
				return;
			}

			if (param != null) {
				// search only in global formulas and in local
				// formulas where there is no local definition
				// of a parameter with the same name
				for (var p : new ParameterDao(db).getAll()) {
					long ownerID = owners.get(p.id);
					if (hasLocalDef.contains(ownerID)
							|| !matches(p.formula))
						continue;
					var node = new Node(p).of(UsageType.FORMULA, p.formula);
					var root = parent(p, ownerID);
					if (root != null) {
						root.add(node);
					} else {
						roots.put(p.id, node);
					}
				}
				return;
			}

			// search via all text matches
			for (var p : new ParameterDao(db).getAll()) {
				var nameMatch = matches(p.name);
				var formulaMatch = !nameMatch
						&& !p.isInputParameter
						&& matches(p.formula);
				if (!nameMatch && !formulaMatch)
					continue;

				var node = nameMatch
						? new Node(p).of(UsageType.DEFINITION, p.name)
						: new Node(p).of(UsageType.FORMULA, p.formula);
				var root = parent(p, owners.get(p.id));
				if (root != null) {
					root.add(node);
				} else {
					roots.put(p.id, node);
				}
			}
		}

		private void systemRedefs() {
			for (var system : new ProductSystemDao(db).getAll()) {
				for (var paramset : system.parameterSets) {
					for (var redef : paramset.parameters) {
						if (!matches(redef))
							continue;
						var root = roots.computeIfAbsent(
								system.id, _i -> new Node(system));
						var inner = root.addIfAbsent(
								paramset.id, () -> new Node(paramset.id, paramset.name));
						var leaf = new Node(redef.id, redef.name)
								.of(UsageType.REDEFINITION, redef.name);
						inner.add(leaf);
					}
				}
			}
		}

		private void projectRedefs() {
			for (var project : new ProjectDao(db).getAll()) {
				for (var variant : project.variants) {
					for (var redef : variant.parameterRedefs) {
						if (!matches(redef))
							continue;
						var root = roots.computeIfAbsent(
								project.id, _i -> new Node(project));
						var inner = root.addIfAbsent(
								variant.id, () -> new Node(variant.id, variant.name));
						var leaf = new Node(redef.id, redef.name)
								.of(UsageType.REDEFINITION, redef.name);
						inner.add(leaf);
					}
				}
			}
		}

		private boolean matches(String formula) {
			if (formula == null)
				return false;
			String f = formula.trim();
			if (f.equalsIgnoreCase(name))
				return true;
			try {
				Set<String> vars = Formula.getVariables(f);
				for (String var : vars) {
					if (var.equalsIgnoreCase(name))
						return true;
				}
			} catch (Error e) {
				return false;
			}
			return false;
		}

		private boolean matches(ParameterRedef redef) {
			if (!matches(redef.name))
				return false;
			if (owner != null)
				return redef.contextId != null
						&& redef.contextId == owner.id;
			if (param != null)
				return redef.contextId == null;
			return true;
		}

		private Node root(long id, Class<? extends CategorizedDescriptor> clazz) {
			if (owner != null && owner.id == id)
				return roots.computeIfAbsent(id, _id -> new Node(owner));
			return roots.computeIfAbsent(id, _id -> {
				var model = cache.get(clazz, id);
				if (model == null)
					return null;
				return new Node(model);
			});
		}

		private Node parent(Parameter param, long ownerID) {
			if (param.scope == null)
				return null;
			switch (param.scope) {
				case PROCESS:
					return root(ownerID, ProcessDescriptor.class);
				case IMPACT_CATEGORY:
					return root(ownerID, ImpactCategoryDescriptor.class);
				default:
					return null;
			}
		}
	}
}
