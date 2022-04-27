package examples;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

import org.openlca.core.DataDir;
import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.Exchange;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Process;
import org.openlca.core.model.ProcessLink;
import org.openlca.core.model.ProductSystem;
import org.openlca.core.model.Result;

import gnu.trove.set.hash.TLongHashSet;

public class SubResultTree {

	public static void main(String[] args) {

		// This example creates a product system that links sub-results
		// in a random tree. To run the example, import Oekobaudat as
		// ILCD+EPD results into a database.

		var db = DataDir.get().openDatabase("oekobaudat");

		// the Oekobaudat results have no methods; so first we tag the results with
		// the same indicator set with the same method.
		var method = tagWithMethods(db).entity;

		var massId = "93a60a56-a3c8-11da-a746-0800200b9a66";
		var mass = db.get(FlowProperty.class, massId);

		// var methodId = "eb4db92d-97ac-41a7-9573-05566a319b06";
		// var method = db.get(ImpactMethod.class, methodId);
		var results = db.getAll(Result.class)
			.stream()
			.filter(r -> method.equals(r.impactMethod))
			.toList();

		int depth = 5;

		int processCount = 1;
		var root = newProcess(db, mass, processCount);
		var system = ProductSystem.of(root);
		var previousLevel = new Process[]{root};
		var rand = ThreadLocalRandom.current();
		for (int i = 0; i < depth; i++) {
			int width = rand.nextInt(1, depth);
			var nextLevel = new Process[width];
			for (int j = 0; j < width; j++) {
				processCount++;
				var child = newProcess(db, mass, processCount);
				system.processes.add(child.id);

				// link to parent
				int parentIdx = rand.nextInt(0, previousLevel.length);
				var parent = previousLevel[parentIdx];
				var linkFlow = child.quantitativeReference.flow;
				parent.input(linkFlow, rand.nextInt(1, 5));
				parent = db.update(parent);
				previousLevel[parentIdx] =parent;
				var  parentLink= new ProcessLink();
				parentLink.setProviderType(ModelType.PROCESS);
				parentLink.providerId = child.id;
				parentLink.processId = parent.id;
				parentLink.exchangeId = findExchangeOf(parent, linkFlow).id;
				parentLink.flowId = linkFlow.id;
				system.processLinks.add(parentLink);

				// add results
				int resultCount = rand.nextInt(1, 5);
				var addedResults = new TLongHashSet();
				for (int k = 0; k < resultCount; k++) {
					int resultIdx = rand.nextInt(0, results.size());
					var result = results.get(resultIdx);
					if (addedResults.contains(result.id))
						continue;
					var resultFlow = result.referenceFlow.flow;
					child.input(resultFlow, rand.nextInt(1, 5));
					child = db.update(child);
					nextLevel[j] = child;
					system.processes.add(result.id);
					var link = new ProcessLink();
					link.setProviderType(ModelType.RESULT);
					link.providerId = result.id;
					link.processId = child.id;
					link.exchangeId = findExchangeOf(child, resultFlow).id;
					link.flowId = resultFlow.id;
					system.processLinks.add(link);
				}
			}
			previousLevel = nextLevel;
		}

		db.insert(system);
		db.close();

	}

	private static Process newProcess(IDatabase db, FlowProperty mass, int i) {
		var product = Flow.product("product " + i, mass);
		product.category = new CategoryDao(db).sync(ModelType.FLOW, "products");
		db.insert(product);
		var process = Process.of("process " + i, product);
		return db.insert(process);
	}

	private static Exchange findExchangeOf(Process process, Flow flow) {
		return process.exchanges.stream()
			.filter(e -> Objects.equals(e.flow, flow))
			.findAny()
			.orElse(null);
	}


	private static Method tagWithMethods(IDatabase db) {
		var allResults = db.getAll(Result.class);


		var methods = new ArrayList<Method>();
		Method selectedMethod = null;
		for (var result : allResults) {
			if (isInvalid(result))
				continue;
			Method appliedMethod = null;
			for (var method : methods) {
				if (method.matches(result)) {
					appliedMethod = method.apply(result, db);
					break;
				}
			}
			if (appliedMethod == null) {
				appliedMethod = Method.create(db, result, methods.size() + 1);
				methods.add(appliedMethod);
			}
			if (selectedMethod == null
				|| selectedMethod.resultCount() < appliedMethod.resultCount()) {
				selectedMethod = appliedMethod;
			}
		}
		return selectedMethod;
	}

	private static boolean isInvalid(Result result) {
		return result.referenceFlow == null
			|| result.referenceFlow.flow == null
			|| result.referenceFlow.amount <= 0
			|| result.referenceFlow.flow.flowType != FlowType.PRODUCT_FLOW
			|| result.impactResults.isEmpty();
	}

	private record Method(ImpactMethod entity, List<Result> results) {

		static Method create(IDatabase db, Result result, int i) {
			var m = ImpactMethod.of("method " + i);
			for (var impact : result.impactResults) {
				m.impactCategories.add(impact.indicator);
			}
			db.insert(m);
			result.impactMethod = m;
			var results = new ArrayList<Result>();
			results.add(db.update(result));
			return new Method(m, results);
		}

		boolean matches(Result result) {
			for (var i : result.impactResults) {
				if (!entity.impactCategories.contains(i.indicator))
					return false;
			}
			return true;
		}

		Method apply(Result result, IDatabase db) {
			result.impactMethod = entity;
			results.add(db.update(result));
			return this;
		}

		int resultCount() {
			return results().size();
		}

	}
}
