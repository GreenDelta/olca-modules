
package org.openlca.proto.io.input;

import org.openlca.core.io.EntityResolver;
import org.openlca.core.model.DQIndicator;
import org.openlca.core.model.DQScore;
import org.openlca.core.model.DQSystem;
import org.openlca.core.model.Source;
import org.openlca.proto.ProtoDQIndicator;
import org.openlca.proto.ProtoDQSystem;
import org.openlca.util.Strings;

public record DQSystemReader(EntityResolver resolver)
	implements EntityReader<DQSystem, ProtoDQSystem> {

	@Override
	public DQSystem read(ProtoDQSystem proto) {
		var system = new DQSystem();
		update(system, proto);
		return system;
	}

	@Override
	public void update(DQSystem system, ProtoDQSystem proto) {
		Util.mapBase(system, ProtoWrap.of(proto), resolver);
		system.hasUncertainties = proto.getHasUncertainties();
		var sourceRefId = proto.getSource().getId();
		if (Strings.notEmpty(sourceRefId)) {
			system.source = resolver.get(Source.class, sourceRefId);
		}
		mapIndicators(system, proto);
	}

	private void mapIndicators(DQSystem system, ProtoDQSystem proto) {
		system.indicators.clear();
		for (int i = 0; i < proto.getIndicatorsCount(); i++) {
			var indicatorProto = proto.getIndicators(i);
			var indicator = new DQIndicator();
			indicator.name = indicatorProto.getName();
			indicator.position = indicatorProto.getPosition();
			mapScores(indicator, indicatorProto);
			system.indicators.add(indicator);
		}
	}

	private void mapScores(DQIndicator indicator, ProtoDQIndicator proto) {
		for (var i = 0; i < proto.getScoresCount(); i++) {
			var scoreProto = proto.getScores(i);
			var score = new DQScore();
			score.position = scoreProto.getPosition();
			score.label = scoreProto.getLabel();
			score.description = scoreProto.getDescription();
			score.uncertainty = scoreProto.getUncertainty();
			indicator.scores.add(score);
		}
	}
}
