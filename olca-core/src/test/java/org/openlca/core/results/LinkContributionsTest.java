package org.openlca.core.results;

import org.junit.Assert;
import org.junit.Test;
import org.openlca.core.Tests;
import org.openlca.core.matrix.Demand;
import org.openlca.core.matrix.MatrixData;
import org.openlca.core.matrix.index.LongPair;
import org.openlca.core.matrix.index.TechFlow;
import org.openlca.core.matrix.index.TechIndex;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.descriptors.FlowDescriptor;
import org.openlca.core.model.descriptors.ProcessDescriptor;
import org.openlca.core.results.providers.LazyResultProvider;
import org.openlca.core.results.providers.SolverContext;

public class LinkContributionsTest {

	/**
	 * A = [1, 0, 0, 0; -0.5, 1, 0, 0; -0.5, 0, 1, 0; 0, -0.5, -0.5, 1]
	 *
	 * f = [1; 0; 0; 0]
	 *
	 * s = inv(A) * d = [1; 0.5; 0.5; 0.5]
	 */
	@Test
	public void testDoubleLink() {

		var solver = Tests.getDefaultSolver();
		var techMatrix = solver.matrix(4, 4);
		techMatrix.setValues(new double[][] {
				{ 1, 0, 0, 0 },
				{ -0.5, 1, 0, 0 },
				{ -0.5, 0, 1, 0 },
				{ 0, -0.5, -0.5, 1 },
		});

		var index = new TechIndex(provider(1, 1));
		index.add(provider(2, 2));
		index.add(provider(3, 3));
		index.add(provider(4, 4));
		index.putLink(LongPair.of(1, 2), provider(2, 2));
		index.putLink(LongPair.of(1, 3), provider(3, 3));
		index.putLink(LongPair.of(2, 4), provider(4, 4));
		index.putLink(LongPair.of(3, 4), provider(4, 4));

		var data =new MatrixData();
		data.demand = Demand.of(index.at(0), 1.0);
		data.techMatrix = techMatrix;
		data.techIndex = index;

		var solution = LazyResultProvider.create(SolverContext.of(data));
		var r = new FullResult(solution);

		Assert.assertEquals(0, r.getLinkShare(link(4, 4, 1)), 1e-16);
		Assert.assertEquals(1, r.getLinkShare(link(2, 2, 1)), 1e-16);
		Assert.assertEquals(1, r.getLinkShare(link(3, 3, 1)), 1e-16);
		Assert.assertEquals(0.5, r.getLinkShare(link(4, 4, 2)), 1e-16);
		Assert.assertEquals(0.5, r.getLinkShare(link(4, 4, 3)), 1e-16);
	}

	/**
	 * each column has two entries: +1 for the reference product, -1 for the
	 * input of the direct neighbor
	 *
	 * p_1 <- p_2 <- p_3 <- ... <- p_n
	 */
	@Test
	public void testBandMatrix() {
		int size = 40;
		var solver = Tests.getDefaultSolver();
		var techMatrix = solver.matrix(size, size);
		var index = new TechIndex(provider(1, 1));
		for (int i = 0; i < size; i++) {
			index.add(provider(i + 1, i + 1));
			techMatrix.set(i, i, 1.0);
			if (i < (size - 1)) {
				techMatrix.set(i + 1, i, -1);
				index.putLink(LongPair.of(i + 1, i + 2),
						provider(i + 2, i + 2));
			}
		}
		Assert.assertEquals(size - 1, index.getLinkedExchanges().size());

		var data = new MatrixData();
		data.demand = Demand.of(index.at(0), 1.0);
		data.techIndex = index;
		data.techMatrix = techMatrix;
		var solutions = LazyResultProvider.create(SolverContext.of(data));
		var r = new FullResult(solutions);

		for (int i = 0; i < size; i++) {
			if (i < (size - 1)) {
				Assert.assertEquals(1,
						r.getLinkShare(link(i + 2, i + 2, i + 1)), 1e-16);
				Assert.assertEquals(0,
						r.getLinkShare(link(i + 3, i + 3, i + 1)), 1e-16);
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

	private TechFlow provider(long id, long flowId) {
		ProcessDescriptor process = new ProcessDescriptor();
		process.name = "Process " + id;
		process.id = id;
		FlowDescriptor flow = new FlowDescriptor();
		flow.name = "Flow " + flowId;
		flow.id = flowId;
		return TechFlow.of(process, flow);
	}
}
