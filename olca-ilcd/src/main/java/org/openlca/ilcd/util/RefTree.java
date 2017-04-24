package org.openlca.ilcd.util;

import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.commons.IDataSet;
import org.openlca.ilcd.commons.LangString;
import org.openlca.ilcd.commons.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RefTree {

	public final Node root;

	public static class Node {
		public String field;
		public Ref ref;
		public List<Node> childs = new ArrayList<>();
	}

	public RefTree() {
		root = new Node();
	}

	public List<Ref> getRefs() {
		Deque<Node> next = new ArrayDeque<>();
		next.add(root);
		HashSet<String> handled = new HashSet<>();
		List<Ref> list = new ArrayList<>();
		while (!next.isEmpty()) {
			Node node = next.poll();
			if (node.ref != null) {
				boolean add = handled.add(key(node.ref));
				if (add)
					list.add(node.ref);
			}
			next.addAll(node.childs);
		}
		return list;
	}

	// we use this key function and not the equals-function of Ref because
	// we want also collect the same reference in different versions.
	private String key(Ref ref) {
		if (ref == null)
			return "/";
		return "/" + ref.type + "/" + ref.uuid + "/" + ref.version;
	}

	public static RefTree create(IDataSet ds) {
		RefTree tree = new RefTree();
		if (ds == null) {
			tree.root.field = "none";
			return tree;
		}
		tree.root.field = ds.getClass().getSimpleName();
		tree.root.childs.addAll(fetchChilds(ds));
		return tree;
	}

	private static List<Node> fetchChilds(Object obj) {
		if (obj == null)
			return Collections.emptyList();
		List<Node> nodes = new ArrayList<>();
		try {
			for (Field field : obj.getClass().getDeclaredFields()) {
				if (!follow(field.getType()))
					continue;
				field.setAccessible(true);
				Object val = field.get(obj);
				if (val == null)
					continue;
				if (val instanceof Ref) {
					addNode(field, (Ref) val, nodes);
					continue;
				}
				if (val instanceof Collection) {
					Collection<?> c = (Collection<?>) val;
					followCollection(field, c, nodes);
					continue;
				}
				if (val instanceof Map) {
					Map<?, ?> m = (Map<?, ?>) val;
					followCollection(field, m.values(), nodes);
					continue;
				}
				if (Object[].class.isAssignableFrom(val.getClass())) {
					Object[] array = (Object[]) val;
					followCollection(field, Arrays.asList(array), nodes);
					continue;
				}
				collectChilds(field, val, nodes);

			}
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(RefTree.class);
			log.error("failed to create RefTree", e);
		}
		return nodes;
	}

	private static void followCollection(Field field, Collection<?> c,
			List<Node> nodes) {
		for (Object elem : c) {
			if (!follow(elem.getClass()))
				break;
			if (elem instanceof Ref) {
				addNode(field, (Ref) elem, nodes);
				continue;
			}
			collectChilds(field, elem, nodes);
		}
	}

	private static void addNode(Field field, Ref ref, List<Node> nodes) {
		Node node = node(field);
		node.ref = ref;
		nodes.add(node);
	}

	private static void collectChilds(Field field, Object val,
			List<Node> nodes) {
		List<Node> childs = fetchChilds(val);
		if (childs.isEmpty())
			return;
		Node node = node(field);
		node.childs.addAll(childs);
		nodes.add(node);
	}

	private static Node node(Field field) {
		Node n = new Node();
		n.field = field.getName();
		if (!field.isAnnotationPresent(XmlElement.class))
			return n;
		XmlElement xe = field.getAnnotation(XmlElement.class);
		if (xe == null || xe.name().equals("##default"))
			return n;
		n.field = xe.name();
		return n;
	}

	private static boolean follow(Class<?> clazz) {
		if (clazz == null)
			return false;
		if (!Object.class.isAssignableFrom(clazz))
			return false;
		if (Boolean.class.isAssignableFrom(clazz))
			return false;
		if (Number.class.isAssignableFrom(clazz))
			return false;
		if (String.class.isAssignableFrom(clazz))
			return false;
		if (Character.class.isAssignableFrom(clazz))
			return false;
		if (LangString.class.isAssignableFrom(clazz))
			return false;
		if (Enum.class.isAssignableFrom(clazz))
			return false;
		if (XMLGregorianCalendar.class.isAssignableFrom(clazz))
			return false;
		else
			return true;
	}

}
