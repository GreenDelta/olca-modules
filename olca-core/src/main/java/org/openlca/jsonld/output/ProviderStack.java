package org.openlca.jsonld.output;

import java.util.ArrayDeque;

import org.openlca.core.model.Callback;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.descriptors.Descriptor;

class ProviderStack {

	private final JsonExport exp;
	private final ArrayDeque<Descriptor> stack = new ArrayDeque<>();

	ProviderStack(JsonExport exp) {
		this.exp = exp;
	}

	void push(Descriptor d) {
		if (!stack.contains(d)) {
			stack.push(d);
		}
	}

	void pop(Callback cb) {
		if (stack.isEmpty())
			return;
		var d = stack.pop();
		if (exp.hasVisited(ModelType.PROCESS, d.refId))
			return;
		var process = exp.db.get(Process.class, d.id);
		if (process != null) {
			exp.write(process, cb);
		}
	}
}
