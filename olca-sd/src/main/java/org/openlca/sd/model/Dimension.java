package org.openlca.sd.model;

import java.util.List;
import java.util.Objects;

import org.openlca.sd.model.Subscript.Empty;
import org.openlca.sd.model.Subscript.Identifier;
import org.openlca.sd.model.Subscript.Index;
import org.openlca.sd.model.Subscript.Wildcard;

public class Dimension {

	private final Id name;
	private final Id[] elements;

	public Dimension(Id name, Id... elements) {
		this.name = Objects.requireNonNull(name);
		this.elements = Objects.requireNonNull(elements);
	}

	public static Dimension of(String name, String... elements) {
		return new Dimension(Id.of(name), Id.ofAll(elements));
	}

	public Id name() {
		return name;
	}

	public boolean hasName(Id name) {
		return this.name.equals(name);
	}

	public int size() {
		return elements.length;
	}

	public List<Id> elements() {
		return List.of(elements);
	}

	/// Returns the index of the given element, or -1 if this dimension does not
	/// contain it.
	public int indexOf(Id elem) {
		if (elem == null)
			return -1;
		for (int i = 0; i < elements.length; i++) {
			if (elem.equals(elements[i])) {
				return i;
			}
		}
		return -1;
	}

	/// Returns the index of the element represented by the given subscript,
	/// or -1 if the subscript does not represent an element of this dimension
	/// (could be also a wildcard or empty subscript).
	public int indexOf(Subscript sub) {
		return switch (sub) {
			case Empty ignored -> -1;
			case Index(int i) -> i >= 0 && i < elements.length ? i : -1;
			case Identifier id -> indexOf(id.value());
			case Wildcard ignored -> -1;
			case null -> -1;
		};
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Dimension other))
			return false;
		if (!Objects.equals(name, other.name))
			return false;
		if (elements.length != other.elements.length)
			return false;
		for (int i = 0; i < elements.length; i++) {
			if (!Objects.equals(elements[i], other.elements[i]))
				return false;
		}
		return true;
	}
}
