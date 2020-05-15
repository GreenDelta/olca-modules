package org.openlca.core.database.usage;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.openlca.core.database.EntityCache;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.NativeSql;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.descriptors.BaseDescriptor;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ImpactCategoryDescriptor;
import org.openlca.core.model.descriptors.ImpactMethodDescriptor;
import org.openlca.core.model.descriptors.ParameterDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.model.descriptors.ProductSystemDescriptor;
import org.openlca.core.model.descriptors.ProjectDescriptor;
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

		public BaseDescriptor context;
		public UsageType type;
		public String formula;
		public Node parent;
		public final List<Node> childs = new ArrayList<>();

		private void add(Node child) {
			if (child == null)
				return;
			child.parent = this;
			childs.add(child);
		}

		public Node root() {
			if (parent == null)
				return this;
			return parent.root();
		}

		@Override
		public int compareTo(Node o) {
			if (o == null)
				return 1;
			if (context == null || o.context == null)
				return 0;
			int o1 = typeOrder(context.type);
			int o2 = typeOrder(o.context.type);
			if (o1 == o2)
				return Strings.compare(
						context.name,
						o.context.name);
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
				default:
					return 99;
			}
		}
	}

	private static class Search {

		private final String param;
		private final IDatabase db;
		private final EntityCache cache;

		private final Map<Class<?>, Map<Long, Node>> contexts = new HashMap<>();

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
			List<Node> roots = new ArrayList<>();
			contexts.forEach(
					(clazz, map) -> roots.addAll(map.values()));
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
				var parent = context(int64(r, 1), ProcessDescriptor.class);
				var child = new Node();
				child.context = cache.get(FlowDescriptor.class, int64(r, 2));
				child.type = UsageType.FORMULA;
				child.formula = formula;
				parent.add(child);
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
				var parent = context(int64(r, 1),
						ImpactCategoryDescriptor.class);
				var child = new Node();
				child.context = cache.get(FlowDescriptor.class, int64(r, 2));
				child.type = UsageType.FORMULA;
				child.formula = formula;
				parent.add(child);
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
					paramNode = context(int64(r, 1),
							ParameterDescriptor.class);
				} else if (scope == ParameterScope.PROCESS) {
					Node root = context(int64(r, 5),
							ProcessDescriptor.class);
					paramNode = child(root, int64(r, 1),
							ParameterDescriptor.class);
				} else if (scope == ParameterScope.IMPACT_CATEGORY) {
					Node root = context(int64(r, 5),
							ImpactCategoryDescriptor.class);
					paramNode = child(root, int64(r, 1),
							ParameterDescriptor.class);
				}

				if (paramNode == null)
					return;

				if (nameMatch) {
					paramNode.type = UsageType.DEFINITION;
				} else {
					paramNode.type = UsageType.FORMULA;
					paramNode.formula = formula;
				}
			});
		}

		private void systemRedefs() {
			String sql = "SELECT redef.name, redef.f_owner, redef.f_context, "
					+ "redef.context_type FROM tbl_parameter_redefs redef "
					+ "INNER JOIN tbl_product_systems owner ON "
					+ "redef.f_owner = owner.id";
			query(sql, r -> {
				String name = string(r, 1);
				if (!matches(name))
					return;
				Node root = context(int64(r, 2), ProductSystemDescriptor.class);

				Class<? extends BaseDescriptor> redefContext = null;
				String ctxt = string(r, 4);
				if (ctxt != null) {
					redefContext = toDescriptorType(
							ModelType.valueOf(ctxt));
				}

				if (redefContext == null) {
					Node child = new Node();
					child.context = new ParameterDescriptor();
					child.context.name = name;
					child.type = UsageType.REDEFINITION;
					child.formula = name;
					root.add(child);
					return;
				}

				Node child = child(root, int64(r, 3), redefContext);
				child.type = UsageType.REDEFINITION;
				child.formula = name;
			});
		}

		private void projectRedefs() {
			String sql = "SELECT DISTINCT redef.name, proj.id" +
					" FROM tbl_parameter_redefs redef" +
					" INNER JOIN tbl_project_variants var ON" +
					" redef.f_owner = var.id" +
					" INNER JOIN tbl_projects proj" +
					" ON var.f_project = proj.id";
			query(sql, r -> {
				String name = string(r, 1);
				if (!matches(name))
					return;
				Node root = context(int64(r, 2), ProjectDescriptor.class);
				Node child = new Node();
				child.context = new ParameterDescriptor();
				child.context.name = name;
				child.type = UsageType.REDEFINITION;
				child.formula = name;
				root.add(child);
			});
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

		private Node context(long id, Class<? extends BaseDescriptor> clazz) {
			Map<Long, Node> nodes = contexts.computeIfAbsent(
					clazz, k -> new HashMap<>());
			Node node = nodes.get(id);
			if (node != null)
				return node;
			node = new Node();
			node.context = cache.get(clazz, id);
			nodes.put(id, node);
			return node;
		}

		private Node child(Node root, long id,
						   Class<? extends BaseDescriptor> clazz) {
			for (Node c : root.childs) {
				if (c.context != null && c.context.id == id)
					return c;
			}
			Node c = new Node();
			c.context = cache.get(clazz, id);
			root.add(c);
			return c;
		}

		/**
		 * We only return types that can have local parameters here.
		 */
		private Class<? extends BaseDescriptor> toDescriptorType(ModelType t) {
			if (t == null)
				return null;
			switch (t) {
				case IMPACT_METHOD:
					return ImpactMethodDescriptor.class;
				case PROCESS:
					return ProcessDescriptor.class;
				default:
					return null;
			}
		}
	}


}
