package org.openlca.io.olca;

import java.util.Objects;

import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;

class ProjectImport {

	private final Config conf;
	private final RefSwitcher refs;

	private ProjectImport(Config conf) {
		this.conf = conf;
		this.refs = new RefSwitcher(conf);
	}

	static void run(Config conf) {
		new ProjectImport(conf).run();
	}

	private void run() {
		conf.syncAll(Project.class, project -> {
			var copy = project.copy();
			copy.impactMethod = conf.swap(project.impactMethod);
			if (copy.impactMethod != null && project.nwSet != null) {
				copy.nwSet = copy.impactMethod.nwSets.stream()
						.filter(nws -> Objects.equals(nws.refId, project.nwSet.refId))
						.findFirst()
						.orElse(null);
				if (copy.nwSet == null) {
					conf.log().error("could not map NW set "
							+ project.nwSet.refId + " in project " + project.refId);
				}
			}
			for (var variant : copy.variants) {
				swapRefsOf(variant);
			}
			return copy;
		});
	}

	private void swapRefsOf(ProjectVariant variant) {
		variant.productSystem = conf.swap(variant.productSystem);
		swapPropertyOf(variant);
		variant.unit = variant.flowPropertyFactor != null && variant.unit != null
				? Config.findUnit(variant.flowPropertyFactor, variant.unit.refId)
				: null;

		for (var param : variant.parameterRedefs) {
			if (param.contextId == null)
				continue;
			param.contextId = conf.seq().get(param.contextType, param.contextId);
		}
	}

	private void swapPropertyOf(ProjectVariant variant) {
		if (variant.flowPropertyFactor == null)
			return;
		var system = variant.productSystem;
		if (system == null || system.referenceExchange == null) {
			variant.flowPropertyFactor = null;
			return;
		}
		var flow = system.referenceExchange.flow;
		variant.flowPropertyFactor = refs.switchRef(
				variant.flowPropertyFactor, flow);
	}
}
