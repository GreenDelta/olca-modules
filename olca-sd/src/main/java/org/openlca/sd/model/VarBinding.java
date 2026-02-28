package org.openlca.sd.model;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.ParameterRedef;

/// Describes the binding of a SD variable to an openLCA parameter
/// (redefinition).
///
/// @param varId     - The ID of the SD variable (a stock, flow, or auxiliary).
/// @param parameter - The name of the openLCA parameter.
/// @param context   - The parameter context, if `null` it is a global parameter
public record VarBinding(Id varId, String parameter, EntityRef context) {

	public VarBinding globalOf(Var variable, ParameterRedef redef) {
		return new VarBinding(variable.name(), redef.name, null);
	}

	public VarBinding of(Var variable, ParameterRedef redef, IDatabase db) {
		var context = redef.contextId != null && redef.contextType != null
			? db.getDescriptor(redef.contextType.getModelClass(), redef.contextId)
			: null;
		var ref = context != null
			? EntityRef.of(context)
			: null;
		return new VarBinding(variable.name(), redef.name, ref);
	}

	public boolean isGlobal() {
		return context == null;
	}

	@Override
	public String toString() {
		var s = varId + " :: " + parameter;
		if (context != null) {
			var id = context.refId() != null && context.refId().length() >= 5
				? context.refId().substring(0, 5)
				: context.refId();
			s += " :: " + context.type() + "@" + id;
		}
		return s;
	}
}
