package org.openlca.ilcd.util;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.openlca.ilcd.commons.DataSetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ErrorHandler;

public class SchemaValidator {

	private ErrorHandler errorHandler;

	private Logger log = LoggerFactory.getLogger(getClass());
	private HashMap<DataSetType, Schema> schemas = new HashMap<>(10);

	public void setErrorHandler(ErrorHandler h) {
		this.errorHandler = h;
	}

	public boolean isValid(Path path, DataSetType type) {
		if (path == null)
			return false;
		Validator val = makeValidator(type);
		if (val == null)
			return false;
		try (BufferedInputStream is = new BufferedInputStream(
				Files.newInputStream(path))) {
			val.validate(new StreamSource(is));
			return true;
		} catch (Exception e) {
			log.error("validation of {} failed: {}", path, e.getMessage());
			return false;
		}
	}

	public boolean isValid(InputStream stream, DataSetType type) {
		if (stream == null)
			return false;
		Validator val = makeValidator(type);
		if (val == null)
			return false;
		try {
			val.validate(new StreamSource(stream));
			return true;
		} catch (Exception e) {
			log.error("validation failed: {}", e.getMessage());
			return false;
		}
	}

	private Validator makeValidator(DataSetType type) {
		Schema s = schema(type);
		if (s == null) {
			log.error("could not load a schema for type {}", type);
			return null;
		}
		Validator val = s.newValidator();
		if (errorHandler != null)
			val.setErrorHandler(errorHandler);
		return val;
	}

	private Schema schema(DataSetType type) {
		if (type == null)
			return null;
		Schema s = schemas.get(type);
		if (s != null)
			return s;
		s = loadSchema(schemaName(type));
		schemas.put(type, s);
		return s;
	}

	private String schemaName(DataSetType type) {
		if (type == null)
			return null;
		switch (type) {
		case CONTACT:
			return "ILCD_ContactDataSet.xsd";
		case FLOW:
			return "ILCD_FlowDataSet.xsd";
		case FLOW_PROPERTY:
			return "ILCD_FlowPropertyDataSet.xsd";
		case LCIA_METHOD:
			return "ILCD_LCIAMethodDataSet.xsd";
		case PROCESS:
			return "ILCD_ProcessDataSet.xsd";
		case SOURCE:
			return "ILCD_SourceDataSet.xsd";
		case UNIT_GROUP:
			return "ILCD_UnitGroupDataSet.xsd";
		default:
			log.error("no schema available for data set type {}", type);
			return null;
		}
	}

	private Schema loadSchema(String name) {
		if (name == null)
			return null;
		try {
			URL url = SchemaValidator.class.getResource("/ilcd/schemas/" + name);
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			return factory.newSchema(url);
		} catch (Exception e) {
			log.error("failed to load schema " + name, e);
			return null;
		}
	}

}
