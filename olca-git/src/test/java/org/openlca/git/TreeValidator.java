package org.openlca.git;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.junit.Assert;
import org.openlca.git.util.GitUtil;

public class TreeValidator {

	public static void assertEqualRecursive(Repository repo, AbstractTreeIterator iterator, String... entries) {
		var stack = new LinkedList<>(Arrays.asList(entries));
		try (var walk = new TreeWalk(repo)) {
			walk.addTree(iterator);
			walk.setRecursive(true);
			while (walk.next()) {
				var actual = GitUtil.decode(walk.getPathString());
				var expected = stack.pop();
				Assert.assertEquals(expected, actual);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void assertEqual(Repository repo, AbstractTreeIterator iterator, String... entries) {
		var stack = new LinkedList<>(Arrays.asList(entries));
		try (var walk = new TreeWalk(repo)) {
			walk.addTree(iterator);
			walk.setRecursive(false);
			while (walk.next()) {
				var actual = GitUtil.decode(walk.getPathString());
				var expected = stack.pop();
				Assert.assertEquals(expected, actual);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
