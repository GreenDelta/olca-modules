package org.openlca.ilcd.epd.conversion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.openlca.ilcd.commons.Other;
import org.openlca.ilcd.commons.Ref;
import org.openlca.ilcd.epd.util.Strings;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * Contains some utility methods for reading and writing Ref instances from and
 * to DOM elements.
 */
final class JaxbRefs {

	private JaxbRefs() {
	}

	static void copyFields(Ref from, Ref to) {
		if (from == null || to == null || from == to)
			return;
		to.type = from.type;
		to.uuid = from.uuid;
		to.name.addAll(from.name);
		to.uri = from.uri;
		to.version = from.version;
	}

	static <T extends Ref> void write(Class<T> type, List<T> refs, Other ext) {
		if (refs.isEmpty() || ext == null)
			return;
		ext.any.clear();
		try {
			var context = JAXBContext.newInstance(type);
			var marshaller = context.createMarshaller();
			for (var ref : refs) {
				var doc = Dom.createDocument();
				if (doc == null)
					continue;
				marshaller.marshal(ref, doc);
				var elem = doc.getDocumentElement();
				ext.any.add(elem);
			}
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(JaxbRefs.class);
			log.error("failed to marshal Ref elements of " + type, e);
		}
	}

	static <T extends Ref> List<Ref> read(Class<T> type, Other ext) {
		if (ext == null || type == null || ext.any.isEmpty())
			return Collections.emptyList();
		var rootDef = type.getAnnotation(XmlRootElement.class);
		if (rootDef == null)
			return Collections.emptyList();

		try {
			var list = new ArrayList<Ref>();
			Unmarshaller unmarshaller = null;
			for (var obj : ext.any) {
				if (!(obj instanceof Element elem))
					continue;
				if (!Strings.nullOrEqual(elem.getLocalName(), rootDef.name()))
					continue;
				if (unmarshaller == null) {
					var context = JAXBContext.newInstance(type);
					unmarshaller = context.createUnmarshaller();
				}
				var instance = unmarshaller.unmarshal(elem);
				if (type.isInstance(instance)) {
					list.add(unwrap(type.cast(instance)));
				}
			}
			return list;
		} catch (Exception e) {
			var log = LoggerFactory.getLogger(Dom.class);
			log.error("failed to unmarshal Ref type of " + type, e);
			return Collections.emptyList();
		}
	}

	private static Ref unwrap(Ref ref) {
		if (ref == null)
			return null;
		if (ref.getClass().equals(Ref.class))
			return ref;
		var raw = new Ref();
		raw.name.addAll(ref.name);
		raw.type = ref.type;
		raw.uri = ref.uri;
		raw.uuid = ref.uuid;
		raw.version = ref.version;
		return raw;
	}

}
