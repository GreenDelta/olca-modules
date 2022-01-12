package org.openlca.ilcd.epd.model.content;

import java.util.ArrayList;
import java.util.List;

import org.openlca.ilcd.epd.conversion.Dom;
import org.w3c.dom.Element;

/**
 * In the logic of the EPD editor, a material is the same as a substance but it
 * can contain other substances. In this sense it extends a substance.
 */
public class Material extends Substance {

	public final List<Substance> substances = new ArrayList<>();

	@Override
	Material read(Element e) {
		if (e == null)
			return this;
		super.read(e);
		Dom.eachChild(e, child -> {
			ContentElement ce = ContentDeclaration.readElement(child);
			if (ce instanceof Substance) {
				substances.add((Substance) ce);
			}
		});
		return this;
	}

	@Override
	void write(Element elem) {
		if (elem == null)
			return;
		super.write(elem);
		for (Substance substance : substances) {
			ContentDeclaration.writeElement(elem, substance);
		}
	}

	@Override
	public Material clone() {
		Material clone = new Material();
		copyTo(clone);
		for (Substance s : substances) {
			if (s != null) {
				clone.substances.add(s.clone());
			}
		}
		return clone;
	}
}
