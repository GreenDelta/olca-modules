package org.openlca.io.ecospold2.input;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.openlca.core.model.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An ISIC category tree which can be build from an ISIC text file (see the ISIC
 * text file in this project resources which is available from
 * http://unstats.un.org/unsd/cr/registry/isic-4.asp).
 */
class IsicTree {

	private List<IsicNode> roots = new ArrayList<>();

	public List<IsicNode> getRoots() {
		return roots;
	}

	public IsicNode findNode(String code) {
		if (code == null)
			return null;
		for (IsicNode root : roots) {
			IsicNode node = findNode(code, root);
			if (node != null)
				return node;
		}
		return null;
	}

	private IsicNode findNode(String code, IsicNode root) {
		for (IsicNode child : root.childs) {
			if (code.equals(child.code))
				return child;
			if (code.startsWith(child.code))
				return findNode(code, child);
		}
		return null;
	}

	public static IsicTree fromFile(InputStream in) {
		IsicTree tree = new IsicTree();
		try (InputStreamReader reader = new InputStreamReader(in);
				BufferedReader buffer = new BufferedReader(reader)) {
			String line = null;
			while ((line = buffer.readLine()) != null) {
				IsicNode node = makeNode(line);
				if (node == null)
					continue;
				addNode(tree, node);
			}
		} catch (Exception e) {
			Logger log = LoggerFactory.getLogger(IsicTree.class);
			log.error("faile to build ISIC tree", e);
		}
		return tree;
	}

	private static void addNode(IsicTree tree, IsicNode node) {
		if (node.code.length() == 1) {
			tree.roots.add(node);
			return;
		}
		IsicNode root = tree.roots.get(tree.roots.size() - 1);
		IsicNode parent = findParent(node.code, root);
		parent.childs.add(node);
		node.parent = parent;
	}

	private static IsicNode findParent(String code, IsicNode root) {
		for (IsicNode candidate : root.childs) {
			if (code.startsWith(candidate.code))
				return findParent(code, candidate);
		}
		return root;
	}

	private static IsicNode makeNode(String line) {
		if (line == null || line.isEmpty() || line.length() < 1)
			return null;
		String[] parts = line.split("\",\"");
		if (parts.length != 2)
			return null;
		if (!parts[0].startsWith("\"") || !parts[1].endsWith("\""))
			return null;
		String code = parts[0].substring(1);
		String name = parts[1].substring(0, parts[1].length() - 1);
		IsicNode node = new IsicNode();
		node.code = code;
		node.name = name;
		return node;
	}

	static class IsicNode {

		private String code;
		private String name;
		private IsicNode parent;
		private List<IsicNode> childs = new ArrayList<>();
		private Category category;

		/** Set the corresponding openLCA category */
		void setCategory(Category category) {
			this.category = category;
		}

		/** Set the corresponding openLCA category */
		Category getCategory() {
			return category;
		}

		public IsicNode getParent() {
			return parent;
		}

		public List<IsicNode> getChilds() {
			return childs;
		}

		public String getCode() {
			return code;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return "IsicNode [code=" + code + "]";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((code == null) ? 0 : code.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			IsicNode other = (IsicNode) obj;
			if (code == null) {
				if (other.code != null)
					return false;
			} else if (!code.equals(other.code))
				return false;
			return true;
		}

	}
}
