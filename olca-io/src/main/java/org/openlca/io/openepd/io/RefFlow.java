package org.openlca.io.openepd.io;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import org.openlca.core.database.CategoryDao;
import org.openlca.core.database.IDatabase;
import org.openlca.core.model.EpdProduct;
import org.openlca.core.model.Flow;
import org.openlca.core.model.FlowProperty;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.FlowResult;
import org.openlca.core.model.FlowType;
import org.openlca.core.model.ModelType;
import org.openlca.core.model.Unit;
import org.openlca.core.model.Version;
import org.openlca.io.UnitMapping;
import org.openlca.io.openepd.EpdDoc;
import org.openlca.io.openepd.EpdQuantity;

record RefFlow(EpdProduct product) {

	static RefFlow of(IDatabase db, EpdDoc doc) {

		var flow = new Flow();
		flow.name = doc.productName;
		flow.refId = UUID.randomUUID().toString();
		flow.lastChange = System.currentTimeMillis();
		flow.flowType = FlowType.PRODUCT_FLOW;
		flow.description = doc.productDescription;
		flow.version = Version.of(doc.version).getValue();
		EpdImport.categoryOf(doc).ifPresent(
			path -> flow.category = CategoryDao.sync(db, ModelType.FLOW, path));

		var product = new EpdProduct();
		Consumer<Quantity> refQ = q -> {
			product.property = q.property();
			product.unit = q.unit();
			product.amount = q.amount();
			flow.referenceFlowProperty = q.property();
			var factor = FlowPropertyFactor.of(q.property());
			flow.flowPropertyFactors.add(factor);
		};

		var uMap = UnitMapping.createDefault(db);
		var qDecl = Quantity.select(uMap, doc.declaredUnit);
		var qMass = Quantity.select(uMap, doc.kgPerDeclaredUnit);

		if (qDecl.isPresent()) {
			refQ.accept(qDecl);
			if (qMass.isPresent()
				&& !Objects.equals(qDecl.property(), qMass.property())
				&& qDecl.amount() != 0) {
				// add mass as secondary flow property
				var f = qMass.refAmount() / qDecl.refAmount();
				var massFactor = FlowPropertyFactor.of(qMass.property(), f);
				flow.flowPropertyFactors.add(massFactor);
			}

		} else if (qMass.isPresent()) {
			// add mass as reference flow property
			refQ.accept(qMass);
		}

		product.flow = db.insert(flow);
		return new RefFlow(product);
	}


	FlowResult create() {
		var f = FlowResult.outputOf(product.flow, product.amount);
		f.unit = product.unit;
		return f;
	}

	String name() {
		return product.flow.name;
	}

	private record Quantity(double amount, Unit unit, FlowProperty property) {

		static Quantity empty() {
			return new Quantity(0, null, null);
		}

		static Quantity select(UnitMapping mapping, EpdQuantity qEpd) {
			if (mapping == null || qEpd == null)
				return empty();
			var e = mapping.getEntry(qEpd.unit());
			return e != null
				? new Quantity(qEpd.amount(), e.unit, e.flowProperty)
				: Quantity.empty();
		}

		boolean isPresent() {
			return unit != null || property != null;
		}

		double refAmount() {
			return amount * unit.conversionFactor;
		}

		@Override
		public String toString() {
			var p = property != null && property.name != null
				? property.name
				: "- none -";
			var u = unit != null && unit.name != null
				? unit.name
				: "- none -";
			return "Quantity { property: " + p + "; unit: " + u + "}";
		}
	}
}
