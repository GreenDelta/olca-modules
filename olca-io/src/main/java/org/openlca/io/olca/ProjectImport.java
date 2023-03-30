package org.openlca.io.olca;

import org.openlca.core.model.ModelType;
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
			copy.nwSet = refs.switchRef(project.nwSet);
			for (var variant : copy.variants) {
				swapRefsOf(variant);
			}
			return copy;
		});
	}

	private void swapRefsOf(ProjectVariant variant) {
		variant.productSystem = conf.swap(variant.productSystem);
		variant.unit = refs.switchRef(variant.unit);
		swapPropertyOf(variant);
		for (var param : variant.parameterRedefs) {
			if (param.contextId == null)
				continue;
			param.contextId = param.contextType == ModelType.IMPACT_CATEGORY
					? refs.getDestImpactId(param.contextId)
					: refs.getDestProcessId(param.contextId);
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
