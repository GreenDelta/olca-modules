package org.openlca.core;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ProcessDao;
import org.openlca.core.model.AllocationFactor;
import org.openlca.core.model.AllocationMethod;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.Process;
import org.openlca.util.Strings;

public class TestProcess {

	private IDatabase db = Tests.getDb();
	private Process process;

	private TestProcess() {
	}

	/**
	 * Creates a process with the given product output as quantitative
	 * reference.
	 */
	public static TestProcess refProduct(String flow, double amount,
			String unit) {
		TestProcess tp = new TestProcess();
		tp.process = new Process();
		tp.process.refId = UUID.randomUUID().toString();
		tp.process.name = flow;
		tp.prodOut(flow, amount, unit);
		tp.process.quantitativeReference = tp.process.exchanges.get(0);
		return tp;
	}

	/**
	 * Creates a process with the given waste input as quantitative reference.
	 */
	public static TestProcess refWaste(String flow, double amount,
			String unit) {
		TestProcess tp = new TestProcess();
		tp.process = new Process();
		tp.process.refId = UUID.randomUUID().toString();
		tp.process.name = flow;
		tp.wasteIn(flow, amount, unit);
		tp.process.quantitativeReference = tp.process.exchanges.get(0);
		return tp;
	}

	/**
	 * Finally, saves the process in the database and returns it.
	 */
	public Process get() {
		ProcessDao dao = new ProcessDao(db);
		return dao.insert(process);
	}

	public TestProcess with(Consumer<Process> fn) {
		fn.accept(process);
		return this;
	}

	public TestProcess addCosts(String flow, double amount, String currency) {
		for (Exchange e : process.exchanges) {
			Flow f = e.flow;
			if (f == null || !Strings.nullOrEqual(f.name, flow))
				continue;
			e.currency = TestData.currency(currency);
			e.costs = amount;
			break;
		}
		return this;
	}

	public TestProcess prodOut(String flow, double amount, String unit) {
		Exchange e = prepareExchange(FlowType.PRODUCT_FLOW, flow, amount, unit);
		e.isInput = false;
		return this;
	}

	public TestProcess prodIn(String flow, double amount, String unit) {
		Exchange e = prepareExchange(FlowType.PRODUCT_FLOW, flow, amount, unit);
		e.isInput = true;
		return this;
	}

	public TestProcess elemOut(String flow, double amount, String unit) {
		Exchange e = prepareExchange(FlowType.ELEMENTARY_FLOW, flow, amount,
				unit);
		e.isInput = false;
		return this;
	}

	public TestProcess elemIn(String flow, double amount, String unit) {
		Exchange e = prepareExchange(FlowType.ELEMENTARY_FLOW, flow, amount,
				unit);
		e.isInput = true;
		return this;
	}

	public TestProcess wasteOut(String flow, double amount, String unit) {
		Exchange e = prepareExchange(FlowType.WASTE_FLOW, flow, amount, unit);
		e.isInput = false;
		return this;
	}

	public TestProcess wasteIn(String flow, double amount, String unit) {
		Exchange e = prepareExchange(FlowType.WASTE_FLOW, flow, amount, unit);
		e.isInput = true;
		return this;
	}

	/**
	 * Adds an economic or physical allocation factor for the given flow and
	 * method to the process. Use this method *after* the exchanges are added.
	 */
	public TestProcess alloc(String flow, AllocationMethod method, double factor) {
		AllocationFactor f = new AllocationFactor();
		f.method = method;
		Exchange e = findExchange(process, flow);
		f.productId = e.flow.id;
		f.value = factor;
		process.allocationFactors.add(f);
		return this;
	}

	/**
	 * Adds a causal allocation factor for the given product and flow. Use this
	 * method *after* the exchanges are added.
	 */
	public TestProcess alloc(String product, String flow, double factor) {
		AllocationFactor f = new AllocationFactor();
		f.method = AllocationMethod.CAUSAL;
		Exchange e = findExchange(process, product);
		f.productId = e.flow.id;
		f.value = factor;
		f.exchange = findExchange(process, flow);
		process.allocationFactors.add(f);
		return this;
	}

	public static Exchange findExchange(Process p, String flow) {
		Exchange exchange = null;
		for (Exchange e : p.exchanges) {
			if (e.flow == null)
				continue;
			if (Objects.equals(e.flow.name, flow)) {
				exchange = e;
				break;
			}
		}
		return exchange;
	}

	private Exchange prepareExchange(FlowType flowType, String flow,
			double amount, String unit) {
		Flow f = TestData.flow(flow, unit, flowType);
		Exchange e = process.exchange(
				f, f.referenceFlowProperty, TestData.unit(unit));
		e.amount = amount;
		return e;
	}

}
