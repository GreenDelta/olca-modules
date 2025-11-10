package org.openlca.io.xls.process;

import java.util.HashSet;
import java.util.Set;

import org.openlca.core.model.Actor;
import org.openlca.core.model.RootEntity;

class OutActorSync implements OutEntitySync {

	private final OutConfig config;
	private final Set<Actor> actors = new HashSet<>();

	OutActorSync(OutConfig config) {
		this.config = config;
	}

	@Override
	public void visit(RootEntity entity) {
		if (entity instanceof Actor actor) {
			actors.add(actor);
		}
	}

	@Override
	public void flush() {
		var sheet = config.createSheet(Tab.ACTORS)
			.withColumnWidths(14, 25)
			.header(
				Field.UUID,
				Field.NAME,
				Field.DESCRIPTION,
				Field.CATEGORY,
				Field.VERSION,
				Field.LAST_CHANGE,
				Field.ADDRESS,
				Field.CITY,
				Field.ZIP_CODE,
				Field.COUNTRY,
				Field.E_MAIL,
				Field.TELEFAX,
				Field.TELEPHONE,
				Field.WEBSITE);

		for (var actor : Out.sort(actors)) {
			sheet.next(row -> {
				row.next(actor.refId);
				row.next(actor.name);
				row.next(actor.description);
				row.next(Out.pathOf(actor));
				row.nextAsVersion(actor.version);
				row.nextAsDate(actor.lastChange);
				row.next(actor.address);
				row.next(actor.city);
				row.next(actor.zipCode);
				row.next(actor.country);
				row.next(actor.email);
				row.next(actor.telefax);
				row.next(actor.telephone);
				row.next(actor.website);
			});
		}
	}

}
