
package org.openlca.proto.io.input;

import org.openlca.commons.Strings;
import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.Project;
import org.openlca.core.model.ProjectVariant;
import org.openlca.proto.ProtoProject;

public record ProjectReader(EntityResolver resolver)
	implements EntityReader<Project, ProtoProject> {

	@Override
	public Project read(ProtoProject proto) {
		var project = new Project();
		update(project, proto);
		return project;
	}

	@Override
	public void update(Project project, ProtoProject proto) {
		Util.mapBase(project, ProtoBox.of(proto), resolver);
		mapAttrs(project, proto);
		mapVariants(project, proto);
	}

	private void mapAttrs(Project p, ProtoProject proto) {
		p.isWithCosts = proto.getIsWithCosts();
		p.isWithRegionalization = proto.getIsWithRegionalization();
		p.impactMethod = Util.getImpactMethod(resolver, proto.getImpactMethod());

		// nw-set
		p.nwSet = null;
		if (p.impactMethod == null)
			return;
		var nwSetId = proto.getNwSet().getId();
		if (Strings.isBlank(nwSetId))
			return;
		p.nwSet = p.impactMethod.nwSets.stream()
			.filter(nwSet -> nwSetId.equals(nwSet.refId))
			.findAny()
			.orElse(null);
	}

	private void mapVariants(Project p, ProtoProject proto) {
		p.variants.clear();

		for (int i = 0; i < proto.getVariantsCount(); i++) {
			var protoVar = proto.getVariants(i);
			var v = new ProjectVariant();
			v.productSystem = Util.getProductSystem(
				resolver, protoVar.getProductSystem());
			if (v.productSystem == null)
				continue;

			// flow, property, and unit
			var flow = v.productSystem.referenceExchange != null
				? v.productSystem.referenceExchange.flow
				: null;
			if (flow == null)
				continue;
			var quantity = Quantity.of(flow)
				// TODO: .withProperty(protoVar.)
				.withUnit(protoVar.getUnit())
				.get();
			v.flowPropertyFactor = quantity.factor();
			v.unit = quantity.unit();

			// attributes
			v.name = protoVar.getName();
			v.amount = protoVar.getAmount();
			v.allocationMethod = Util.allocationMethodOf(
				protoVar.getAllocationMethod());
			v.description = protoVar.getDescription();
			v.isDisabled = protoVar.getIsDisabled();

			// parameter redefinitions
			var redefs = Util.parameterRedefsOf(
				protoVar.getParameterRedefsList(), resolver);
			v.parameterRedefs.addAll(redefs);

			p.variants.add(v);
		}
	}
}
