package org.openlca.core;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import javax.persistence.Embeddable;

import org.openlca.core.model.AbstractEntity;
import org.openlca.core.model.RootEntity;

/**
 * Calculates and prints a dependency graph of the openLCA core model. The graph
 * is written in the Graphviz syntax and can be visualized e.g. via
 * http://webgraphviz.com/.
 */
public class DependencyGraph {

	public static void print() throws Exception {
		Queue<Class<?>> queue = new ArrayDeque<>();
		queue.addAll(getSubTypes(AbstractEntity.class, "org.openlca.core.model"));
		Set<Class<?>> nodes = new HashSet<>();
		List<Link> links = new ArrayList<>();
		while (!queue.isEmpty()) {
			Class<?> next = queue.poll();
			nodes.add(next);
			for (Link link : getLinks(next)) {
				Class<?> ref = link.to;
				if (!nodes.contains(ref) && !queue.contains(ref))
					queue.add(ref);
				links.add(link);
			}
		}
		printGraph(nodes, links);
	}

	public static List<Class<?>> getSubTypes(Class<?> superType,
			String packageName) throws Exception {
		List<Class<?>> types = new ArrayList<>();
		Enumeration<URL> urls = Tests.class.getClassLoader().getResources(
				packageName.replace('.', '/'));
		while (urls.hasMoreElements()) {
			URL url = urls.nextElement();
			File dir = new File(url.toURI());
			for (String fileName : dir.list()) {
				if (!fileName.endsWith(".class"))
					continue;
				String shortName = fileName.substring(0, fileName.length() - 6);
				String fullName = packageName + "." + shortName;
				Class<?> type = Class.forName(fullName);
				if (!superType.isAssignableFrom(type)
						|| Modifier.isAbstract(type.getModifiers()))
					continue;
				types.add(type);
			}
		}
		return types;
	}

	private static List<Link> getLinks(Class<?> next) throws Exception {
		List<Link> links = new ArrayList<>();
		for (Field field : getFields(next)) {
			Class<?> refType = field.getType();
			if (isPrimitive(refType))
				continue;
			Link link = new Link();
			link.from = next;
			link.name = field.getName();
			if (List.class.isAssignableFrom(refType)) {
				ParameterizedType pType = (ParameterizedType) field.getGenericType();
				Type arg = pType.getActualTypeArguments()[0];
				if (arg instanceof Class) {
					Class<?> type = (Class<?>) arg;
					link.to = type;
					link.name += " (*)";
				} else {
					link.to = refType;
					link.name += " (?)";
				}
			} else {
				link.to = refType;
				link.name += " (0,1)";
			}
			links.add(link);
		}
		return links;
	}

	private static List<Field> getFields(Class<?> type) {
		if (type == null)
			return Collections.emptyList();
		List<Field> fields = new ArrayList<>();
		Class<?> superType = type.getSuperclass();
		if (superType != null && !superType.equals(Object.class))
			fields.addAll(getFields(type.getSuperclass()));
		for (Field field : type.getDeclaredFields()) {
			fields.add(field);
		}
		return fields;
	}

	private static boolean isPrimitive(Class<?> type) {
		if (type.isEnum())
			return true;
		Class<?>[] primitives = {
				int.class, double.class, boolean.class, double[].class,
				long.class, short.class, Double.class, String.class,
				Long.class, Date.class, Short.class
		};
		for (Class<?> primitive : primitives) {
			if (type.equals(primitive))
				return true;
		}
		return false;
	}

	private static void printGraph(Set<Class<?>> nodes, List<Link> links) {
		p("digraph model {");
		p("  node [style = filled];");
		for (Class<?> node : nodes) {
			printNode(node);
		}
		p("");
		for (Link link : links) {
			p("  \"" + link.from.getSimpleName()
					+ "\" -> \"" + link.to.getSimpleName()
					+ "\" [ label = \"" + link.name + "\", fontsize = 8 ] ;");
		}
		p("}");
	}

	private static void printNode(Class<?> node) {
		String color = "white";
		if (node.isAnnotationPresent(Embeddable.class))
			color = "grey";
		else if (RootEntity.class.isAssignableFrom(node))
			color = "plum";
		else if (AbstractEntity.class.isAssignableFrom(node))
			color = "wheat";
		p("  \"" + node.getSimpleName() + "\" [color=" + color + "];");
	}

	private static void p(String s) {
		System.out.println(s);
	}

	private static class Link {
		Class<?> from;
		Class<?> to;
		String name;
	}

	public static void main(String[] args) {
		try {
			DependencyGraph.print();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
