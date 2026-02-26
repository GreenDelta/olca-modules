package org.openlca.sd.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.openlca.sd.model.Subscript;
import org.openlca.sd.model.Tensor;
import org.openlca.sd.model.Var;

public class Tensors {

	private Tensors() {
	}

	public static boolean haveSameDimensions(Tensor a, Tensor b) {
		if (a == null || b == null)
			return false;
		var dimsA = a.dimensions();
		var dimsB = b.dimensions();
		if (dimsA.size() != dimsB.size())
			return false;
		for (int i = 0; i < dimsA.size(); i++) {
			if (!Objects.equals(dimsA.get(i), dimsB.get(i)))
				return false;
		}
		return true;
	}

	/// Returns the address key for a subscript sequence on a variable. For example,
	/// a variable `a` with the subscripts `b, c` gives a subscript key `a[b, c]`.
	public static String addressKeyOf(Var v, List<Subscript> subscripts) {
		var pref = v != null ? v.name().value() : "";
		if (subscripts == null || subscripts.isEmpty())
			return pref;
		var subs = subscripts.stream()
			.map(Subscript::toString)
			.toList();
		return pref + "[" + String.join(", ", subs) + "]";
	}

	/// Returns the subscript addresses of all possible elements of this tensor.
	/// The returned addresses have the correct dimension order, means the first
	/// element of an address is an element of the first dimension, the second
	/// element an element of the second dimension etc.
	public static List<List<Subscript>> addressesOf(Tensor t) {
		if (t == null)
			return List.of();
		List<List<Subscript>> addresses = List.of();
		var dims = t.dimensions();
		for (var dim : dims) {
			if (addresses.isEmpty()) {
				addresses = new ArrayList<>(dim.size());
				for (var elem : dim.elements()) {
					addresses.add(List.of(Subscript.of(elem)));
				}
				continue;
			}

			int n = dim.size() * addresses.size();
			var next = new ArrayList<List<Subscript>>(n);
			for (var elem : dim.elements()) {
				for (var address : addresses) {
					var a = new ArrayList<Subscript>(address.size() + 1);
					a.addAll(address);
					a.add(Subscript.of(elem));
					next.add(a);
				}
			}
			addresses = next;
		}
		return addresses;
	}
}
