package org.openlca.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.openlca.core.model.Exchange;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.Process;

public final class AllocationUtils {

	private AllocationUtils() {
	}

	/**
	 * Removes invalid allocation factors from the given process. New allocation
	 * factors are initialized with a default value if required. Values of
	 * existing allocation factors are not changed.
	 */
	public static void cleanup(Process process) {
		if (process == null)
			return;
		new AllocationCleanup(process).run();
	}

	/**
	 * Returns the list of flow properties that could be used to calculate
	 * allocation factors. These are flow properties that are present for each
	 * product output or waste input of the given process.
	 */
	public static Set<FlowProperty> allocationPropertiesOf(Process process) {
		if (process == null)
			return Collections.emptySet();

		var techFlows = process.exchanges.stream()
			.filter(Exchanges::isProviderFlow)
			.map(e -> e.flow)
			.toList();
		if (techFlows.isEmpty())
			return Collections.emptySet();

		var first = techFlows.get(0);
		var props = new HashSet<FlowProperty>();
		for (var propFac : first.flowPropertyFactors) {
			var next = propFac.flowProperty;
			if (next == null)
				continue;
			boolean matches = true;
			for (int i = 1; i < techFlows.size(); i++) {
				for (var otherFac : techFlows.get(i).flowPropertyFactors) {
					if (!Objects.equals(next, otherFac.flowProperty)) {
						matches = false;
						break;
					}
				}
				if (!matches)
					break;
			}
			if (matches) {
				props.add(next);
			}
		}

		return props;
	}

	/**
	 * Provider flows are product outputs are waste inputs. For each of such a
	 * flow a mono-functional process can be created when applying allocation
	 * factors.
	 */
	public static List<Exchange> getProviderFlows(Process p) {
		if (p == null)
			return Collections.emptyList();
		return p.exchanges.stream()
			.filter(Exchanges::isProviderFlow)
			.collect(Collectors.toList());
	}

	/**
	 * Non-provider flows are product inputs, waste outputs and all elementary
	 * flows that are partitioned when applying allocation factors to create
	 * mono-functional processes from a multi-functional process.
	 */
	public static List<Exchange> getNonProviderFlows(Process p) {
		if (p == null)
			return Collections.emptyList();
		return p.exchanges.stream()
			.filter(e -> !Exchanges.isProviderFlow(e))
			.collect(Collectors.toList());
	}

}
