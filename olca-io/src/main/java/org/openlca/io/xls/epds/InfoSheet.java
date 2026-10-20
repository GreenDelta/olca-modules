package org.openlca.io.xls.epds;

import java.util.OptionalDouble;

import org.openlca.commons.Strings;
import org.openlca.core.model.Actor;
import org.openlca.core.model.Epd;
import org.openlca.core.model.Location;
import org.openlca.core.model.Source;
import org.openlca.io.openepd.EpdConverter;

/// Writes the "EPD - Information" sheet.
class InfoSheet {

	private final WriterContext ctx;
	private final Epd epd;

	InfoSheet(WriterContext ctx) {
		this.ctx = ctx;
		this.epd = ctx.epd();
	}

	void write() {
		ctx.addContact(epd.manufacturer);
		ctx.addContact(epd.programOperator);
		ctx.addContact(epd.dataGenerator);
		ctx.addContact(epd.verifier);
		ctx.addSource(epd.pcr);
		ctx.addSource(epd.originalEpd);

		var sheet = ctx.writer(Tab.EPD_INFORMATION);
		writeGeneralInformation(sheet);
		writeDeclaredProduct(sheet);
		writeTimeAndLocation(sheet);
		writeTechnology(sheet);
		writePublicationAndOwnership(sheet);
		writeModelingAndValidation(sheet);
	}

	private void writeGeneralInformation(SheetWriter sheet) {
		sheet.section(Section.GENERAL_INFORMATION)
			.field(Field.NAME, epd.name)
			.metaDataFieldsOf(epd)
			.blank();
	}

	private void writeDeclaredProduct(SheetWriter sheet) {
		var product = epd.product;
		var flow = product != null && product.flow != null
			? product.flow.name
			: null;
		Double amount = product != null
			? product.amount
			: null;
		var unit = product != null && product.unit != null
			? product.unit.name
			: null;

		sheet.section(Section.DECLARED_PRODUCT)
			.field(Field.FLOW, flow)
			.field(Field.AMOUNT, amount, unit);

		var mass = product != null
			? EpdConverter.massInKgOf(product)
			: OptionalDouble.empty();
		if (mass.isPresent() && !isKg(unit)) {
			sheet.field(Field.MASS, mass.getAsDouble(), "kg");
		}
		sheet.blank();
	}

	private void writeTimeAndLocation(SheetWriter sheet) {
		sheet.section(Section.TIME_AND_LOCATION)
			.field(Field.PUBLICATION_DATE, epd.validFrom)
			.field(Field.VALID_UNTIL, epd.validUntil)
			.field(Field.LOCATION, locationName(epd.location))
			.blank();
	}

	private void writeTechnology(SheetWriter sheet) {
		sheet.section(Section.TECHNOLOGY)
			.field(Field.MANUFACTURING_DESCRIPTION, epd.manufacturing)
			.field(Field.PRODUCT_USAGE_DESCRIPTION, epd.productUsage)
			.blank();
	}

	private void writePublicationAndOwnership(SheetWriter sheet) {
		sheet.section(Section.PUBLICATION_AND_OWNERSHIP)
			.field(Field.DECLARATION_OWNER_MANUFACTURER, actorName(epd.manufacturer))
			.field(Field.PROGRAM_OPERATOR, actorName(epd.programOperator))
			.field(Field.REGISTRATION_NUMBER, epd.registrationId)
			.field(Field.ORIGINAL_EPD, sourceName(epd.originalEpd))
			.field(Field.URN, epd.urn)
			.blank();
	}

	private void writeModelingAndValidation(SheetWriter sheet) {
		sheet.section(Section.MODELING_AND_VALIDATION)
			.field(Field.EPD_TYPE, epd.epdType != null
				? epd.epdType.toString()
				: null)
			.field(Field.PCR, sourceName(epd.pcr))
			.field(Field.DATA_GENERATOR, actorName(epd.dataGenerator))
			.field(Field.VERIFIER, actorName(epd.verifier))
			.field(Field.USE_ADVICE, epd.useAdvice);
	}

	private static String actorName(Actor actor) {
		return actor != null
			? actor.name
			: null;
	}

	private static String sourceName(Source source) {
		return source != null
			? source.name
			: null;
	}

	private static boolean isKg(String unit) {
		return unit != null && "kg".equalsIgnoreCase(unit.strip());
	}

	private static String locationName(Location location) {
		if (location == null)
			return null;
		var code = Strings.isBlank(location.code)
			? null
			: location.code.strip();
		var name = Strings.isBlank(location.name)
			? null
			: location.name.strip();
		if (code == null)
			return name;
		return name == null
			? code
			: code + " - " + name;
	}
}
