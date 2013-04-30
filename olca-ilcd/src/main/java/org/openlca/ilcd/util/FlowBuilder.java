package org.openlca.ilcd.util;

import java.math.BigInteger;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.openlca.ilcd.commons.FlowType;
import org.openlca.ilcd.flows.AdministrativeInformation;
import org.openlca.ilcd.flows.DataEntry;
import org.openlca.ilcd.flows.DataSetInformation;
import org.openlca.ilcd.flows.Flow;
import org.openlca.ilcd.flows.FlowInformation;
import org.openlca.ilcd.flows.FlowPropertyList;
import org.openlca.ilcd.flows.FlowPropertyReference;
import org.openlca.ilcd.flows.LCIMethod;
import org.openlca.ilcd.flows.ModellingAndValidation;
import org.openlca.ilcd.flows.Publication;
import org.openlca.ilcd.flows.QuantitativeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlowBuilder {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private Flow flow;
	private DataSetInformation dataSetInfo;
	private String baseUri;
	private Integer refPropertyId;
	private List<FlowPropertyReference> flowProperties;
	private FlowType flowType;

	private FlowBuilder() {
		flow = new Flow();
		flow.setVersion("1.1");
	}

	public static FlowBuilder makeFlow() {
		return new FlowBuilder();
	}

	public FlowBuilder withBaseUri(String baseUri) {
		this.baseUri = baseUri;
		return this;
	}

	public FlowBuilder withReferenceFlowPropertyId(Integer id) {
		this.refPropertyId = id;
		return this;
	}

	public FlowBuilder withDataSetInfo(DataSetInformation dataSetInfo) {
		this.dataSetInfo = dataSetInfo;
		return this;
	}

	public FlowBuilder withFlowProperties(
			List<FlowPropertyReference> flowProperties) {
		this.flowProperties = flowProperties;
		return this;
	}

	public FlowBuilder withFlowType(FlowType flowType) {
		this.flowType = flowType;
		return this;
	}

	public Flow getFlow() {
		fill();
		return flow;
	}

	private void fill() {
		fillFlowInfo();
		fillAdminInfo();
		fillProperties();
		fillLciMethod();
	}

	private void fillFlowInfo() {
		FlowInformation info = new FlowInformation();
		flow.setFlowInformation(info);
		if (dataSetInfo == null) {
			dataSetInfo = new DataSetInformation();
		}
		info.setDataSetInformation(dataSetInfo);
		if (dataSetInfo.getUUID() == null) {
			dataSetInfo.setUUID(UUID.randomUUID().toString());
		}
		QuantitativeReference qRef = new QuantitativeReference();
		info.setQuantitativeReference(qRef);
		if (refPropertyId != null)
			qRef.setReferenceFlowProperty(BigInteger.valueOf(refPropertyId));
	}

	private void fillAdminInfo() {
		AdministrativeInformation adminInfo = new AdministrativeInformation();
		flow.setAdministrativeInformation(adminInfo);
		DataEntry dataEntry = new DataEntry();
		adminInfo.setDataEntry(dataEntry);
		setTimeStamp(dataEntry);
		dataEntry.getReferenceToDataSetFormat().add(Reference.forIlcdFormat());
		fillPublication(adminInfo);
	}

	private void setTimeStamp(DataEntry dataEntry) {
		try {
			XMLGregorianCalendar calendar = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(new GregorianCalendar());
			dataEntry.setTimeStamp(calendar);
		} catch (Exception e) {
			log.error("Cannot set timestamp", e);
		}
	}

	private void fillPublication(AdministrativeInformation adminInfo) {
		Publication publication = new Publication();
		adminInfo.setPublication(publication);
		publication.setDataSetVersion("01.00.000");
		if (baseUri == null)
			baseUri = "http://openlca.org/ilcd/resource/";
		if (!baseUri.endsWith("/"))
			baseUri += "/";
		publication.setPermanentDataSetURI(baseUri + "flows/" + getId());
	}

	private String getId() {
		String id = null;
		if (dataSetInfo != null)
			id = dataSetInfo.getUUID();
		return id;
	}

	private void fillProperties() {
		FlowPropertyList propertyList = new FlowPropertyList();
		flow.setFlowProperties(propertyList);
		if (flowProperties != null)
			propertyList.getFlowProperty().addAll(flowProperties);
	}

	private void fillLciMethod() {
		ModellingAndValidation mav = new ModellingAndValidation();
		flow.setModellingAndValidation(mav);
		LCIMethod method = new LCIMethod();
		mav.setLCIMethod(method);
		if (flowType != null) {
			method.setFlowType(flowType);
		} else {
			method.setFlowType(FlowType.OTHER_FLOW);
		}
	}

}
