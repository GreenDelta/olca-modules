package org.openlca.core.results;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.matrix.LongPair;
import org.openlca.core.matrix.TechIndex;
import org.openlca.core.matrix.format.IMatrix;
import org.openlca.core.model.ProcessLink;

public class LinkContributionsTest {

	/**
	 * A = [1, 0, 0, 0; -0.5, 1, 0, 0; -0.5, 0, 1, 0; 0, -0.5, -0.5, 1]
	 * 
	 * d = [1; 0; 0; 0]
	 * 
	 * s = inv(A) * d = [1; 0.5; 0.5; 0.5]
	 */
	@Test
	public void testDoubleLink() {

		IMatrix techMatrix = Tests.getDefaultSolver().matrix(4, 4);
		techMatrix.setValues(new double[][] {
				{ 1, 0, 0, 0 },
				{ -0.5, 1, 0, 0 },
				{ -0.5, 0, 1, 0 },
				{ 0, -0.5, -0.5, 1 } });

		TechIndex index = new TechIndex(LongPair.of(1, 1));
		index.put(LongPair.of(2, 2));
		index.put(LongPair.of(3, 3));
		index.put(LongPair.of(4, 4));
		index.putLink(LongPair.of(1, 2), LongPair.of(2, 2));
		index.putLink(LongPair.of(1, 3), LongPair.of(3, 3));
		index.putLink(LongPair.of(2, 4), LongPair.of(4, 4));
		index.putLink(LongPair.of(3, 4), LongPair.of(4, 4));
		double[] s = { 1, 0.5, 0.5, 0.5 };

		LinkContributions cons = LinkContributions.calculate(techMatrix, index,
				s);
		Assert.assertEquals(0, cons.getShare(link(4, 4, 1)), 1e-16);
		Assert.assertEquals(1, cons.getShare(link(2, 2, 1)), 1e-16);
		Assert.assertEquals(1, cons.getShare(link(3, 3, 1)), 1e-16);
		Assert.assertEquals(0.5, cons.getShare(link(4, 4, 2)), 1e-16);
		Assert.assertEquals(0.5, cons.getShare(link(4, 4, 3)), 1e-16);
	}

	/**
	 * each column has two entries: +1 for the reference product, -1 for the
	 * input of the direct neighbor
	 * 
	 * p_1 <- p_2 <- p_3 <- ... <- p_n
	 */
	@Test
	public void testBandMatrix() {
		int size = 4000;
		IMatrix techMatrix = Tests.getDefaultSolver().matrix(size, size);
		TechIndex index = new TechIndex(LongPair.of(1, 1));
		double[] s = new double[size];
		for (int i = 0; i < size; i++) {
			index.put(LongPair.of(i + 1, i + 1));
			techMatrix.set(i, i, 1.0);
			s[i] = 1;
			if (i < (size - 1)) {
				techMatrix.set(i + 1, i, -1);
				index.putLink(LongPair.of(i + 1, i + 2),
						LongPair.of(i + 2, i + 2));
			}
		}
		Assert.assertEquals(size - 1, index.getLinkedExchanges().size());
		LinkContributions cons = LinkContributions.calculate(techMatrix, index,
				s);
		for (int i = 0; i < size; i++) {
			if (i < (size - 1)) {
				Assert.assertEquals(1,
						cons.getShare(link(i + 2, i + 2, i + 1)), 1e-16);
				Assert.assertEquals(0,
						cons.getShare(link(i + 3, i + 3, i + 1)), 1e-16);
			}
		}
	}

	private ProcessLink link(long provider, long flow, long recipient) {
		ProcessLink link = new ProcessLink();
		link.flowId = flow;
		link.providerId = provider;
		link.processId = recipient;
		return link;
	}
}
