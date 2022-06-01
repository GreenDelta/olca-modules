package org.openlca.io.openepd.io;

import org.openlca.core.model.ImpactMethod;

import java.util.ArrayList;
import java.util.List;

public class MethodMapping {

	private ImpactMethod method;
	private Vocab.Method epdMethod;

	private final List<String> scopes = new ArrayList<>();
	private final List<IndicatorMapping> entries = new ArrayList<>();

	public ImpactMethod method() {
		return method;
	}

	public MethodMapping method(ImpactMethod method) {
		this.method = method;
		return this;
	}

	public Vocab.Method epdMethod() {
		return epdMethod;
	}

	public MethodMapping epdMethod(Vocab.Method epdMethod) {
		this.epdMethod = epdMethod;
		return this;
	}

	public List<String> scopes() {
		return scopes;
	}

	public List<IndicatorMapping> entries() {
		return entries;
	}

}
