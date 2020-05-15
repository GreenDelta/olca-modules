package org.openlca.core.database.usage;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.openlca.core.database.EntityCache;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.database.ProductSystemDao;
import org.openlca.core.database.ProjectDao;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.RootEntity;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.Descriptors;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ParameterDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.util.Formula;
import org.openlca.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates the usage tree for a parameter.
 */
public class ParameterUsageTree {

	public final String param;
	public final List<Node> nodes = new ArrayList<>();

	public ParameterUsageTree(String param) {
		this.param = param;
	}

	public static ParameterUsageTree build(String param, IDatabase db) {
		ParameterUsageTree tree = new ParameterUsageTree(param);
		if (Strings.nullOrEmpty(param))
			return tree;
		Search search = new Search(param.trim(), db);
		tree.nodes.addAll(search.doIt());
		return tree;
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
		public final BaseDescriptor model;

		public UsageType usageType;
		public String usage;

		public Node parent;
		public final List<Node> childs = new ArrayList<>();

		Node(long id, String name) {
			this.id = id;
			this.name = name;
			this.model = null;
		}

		Node(RootEntity e) {
			this(Descriptors.toDescriptor(e));
		}

		Node(BaseDescriptor model) {
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

		private final String param;
		private final IDatabase db;
		private final EntityCache cache;

		private final HashMap<Long, Node> roots = new HashMap<>();

		Search(String param, IDatabase db) {
			this.param = param;
			this.db = db;
			this.cache = EntityCache.create(db);
		}

		List<Node> doIt() {
			exchanges();
			impacts();
			parameters();
			systemRedefs();
			projectRedefs();
			var roots = new ArrayList<>(this.roots.values());
			sortRec(roots);
			return roots;
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
			query(sql, r -> {
				String formula = string(r, 3);
				if (!matches(formula))
					return;
				var root = root(int64(r, 1), ProcessDescriptor.class);
				var flow = cache.get(FlowDescriptor.class, int64(r, 2));
				if (root == null || flow == null)
					return;
				root.add(new Node(flow).of(UsageType.FORMULA, formula));
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
			query(sql, r -> {
				String formula = string(r, 3);
				if (!matches(formula))
					return;
				var root = root(int64(r, 1), ImpactCategoryDescriptor.class);
				var flow = cache.get(FlowDescriptor.class, int64(r, 2));
				if (root == null || flow == null)
					return;
				root.add(new Node(flow).of(UsageType.FORMULA, formula));
			});
		}

		private void parameters() {
			String sql = "SELECT id, name, is_input_param, scope, "
					+ "f_owner, formula FROM tbl_parameters";
			query(sql, r -> {
				String scopeStr = string(r, 4);
				if (scopeStr == null)
					return;
				ParameterScope scope = ParameterScope.valueOf(scopeStr);
				String name = string(r, 2);
				boolean nameMatch = matches(name);
				String formula = string(r, 6);
				boolean formulaMatch = matches(formula);
				if (!nameMatch && !formulaMatch)
					return;

				Node paramNode = null;

				if (scope == ParameterScope.GLOBAL) {
					paramNode = root(int64(r, 1), ParameterDescriptor.class);
				} else if (scope == ParameterScope.PROCESS) {
					var root = root(int64(r, 5), ProcessDescriptor.class);
					paramNode = child(root, int64(r, 1),
							ParameterDescriptor.class);
				} else if (scope == ParameterScope.IMPACT_CATEGORY) {
					var root = root(int64(r, 5), ImpactCategoryDescriptor.class);
					paramNode = child(root, int64(r, 1),
							ParameterDescriptor.class);
				}

				if (paramNode == null)
					return;

				if (nameMatch) {
					paramNode.of(UsageType.DEFINITION, name);
				} else {
					paramNode.of(UsageType.FORMULA, formula);
				}
			});
		}

		private void systemRedefs() {
			for (var system : new ProductSystemDao(db).getAll()) {
				for (var paramset : system.parameterSets) {
					for (var redef : paramset.parameters) {
						if (!matches(redef.name))
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
						if (!matches(redef.name))
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

		private void query(String sql, Consumer<ResultSet> fn) {
			try {
				NativeSql.on(db).query(sql, r -> {
					fn.accept(r);
					return true;
				});
			} catch (Exception e) {
				Logger log = LoggerFactory.getLogger(Search.class);
				log.error("error in query " + sql, e);
			}
		}

		private long int64(ResultSet r, int column) {
			try {
				return r.getLong(column);
			} catch (Exception e) {
				return 0L;
			}
		}

		private String string(ResultSet r, int column) {
			try {
				return r.getString(column);
			} catch (Exception e) {
				return null;
			}
		}

		private boolean matches(String formula) {
			if (formula == null)
				return false;
			String f = formula.trim();
			if (f.equalsIgnoreCase(param))
				return true;
			try {
				Set<String> vars = Formula.getVariables(f);
				for (String var : vars) {
					if (var.equalsIgnoreCase(param))
						return true;
				}
			} catch (Error e) {
				return false;
			}
			return false;
		}

		private Node root(long id, Class<? extends BaseDescriptor> clazz) {
			return roots.computeIfAbsent(id, _id -> {
				var model = cache.get(clazz, id);
				if (model == null)
					return null;
				return new Node(model);
			});
		}

		private Node child(Node parent, long id, Class<? extends BaseDescriptor> clazz) {
			return parent.addIfAbsent(id, () -> {
				var model = cache.get(clazz, id);
				if (model == null)
					return null;
				return new Node(model);
			});
		}
	}
}
