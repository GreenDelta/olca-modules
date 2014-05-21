package org.openlca.io.ilcd.output;

import org.openlca.core.database.IDatabase;
import org.openlca.core.model.FlowPropertyFactor;
import org.openlca.core.model.ImpactCategory;
import org.openlca.core.model.ImpactFactor;
import org.openlca.core.model.ImpactMethod;
import org.openlca.core.model.Unit;
import org.openlca.ilcd.commons.DataSetReference;
import org.openlca.ilcd.io.DataStore;
import org.openlca.ilcd.io.DataStoreException;
import org.openlca.ilcd.methods.DataSetInformation;
import org.openlca.ilcd.methods.Factor;
import org.openlca.ilcd.methods.FactorList;
import org.openlca.ilcd.methods.LCIAMethod;
import org.openlca.ilcd.methods.LCIAMethodInformation;
import org.openlca.ilcd.util.LangString;

import javax.xml.namespace.QName;
import java.util.Map;

public class ImpactMethodExport {

	private IDatabase database;
	private DataStore dataStore;

	public ImpactMethodExport(IDatabase database, DataStore dataStore) {
		this.database = database;
		this.dataStore = dataStore;
	}

	public void run(ImpactMethod method) throws DataStoreException {
		if (method == null)
			return;
		for (ImpactCategory impact : method.getImpactCategories()) {
			LCIAMethod lciaMethod = new LCIAMethod();
			putAttribute("olca_method_uuid", method.getRefId(),
					lciaMethod.getOtherAttributes());
			addMethodInfo(method, impact, lciaMethod);
			addFactors(impact, lciaMethod);
			dataStore.put(lciaMethod, impact.getRefId());
		}
	}

	private void addMethodInfo(ImpactMethod method, ImpactCategory impact,
			LCIAMethod lciaMethod) {
		LCIAMethodInformation info = new LCIAMethodInformation();
		lciaMethod.setLCIAMethodInformation(info);
		DataSetInformation dataSetInfo = new DataSetInformation();
		info.setDataSetInformation(dataSetInfo);
		dataSetInfo.setUUID(impact.getRefId());
		dataSetInfo.getMethodology().add(method.getName());
		dataSetInfo.getImpactCategory().add(impact.getName());
		putAttribute("olca_category_unit", impact.getReferenceUnit(),
				dataSetInfo.getOtherAttributes());
		if (impact.getDescription() != null)
			LangString.addFreeText(dataSetInfo.getGeneralComment(),
					impact.getDescription());
	}

	private void putAttribute(String name, String value, Map<QName, String> map) {
		if (name == null || value == null || map == null)
			return;
		QName qName = new QName("http://openlca.org/ilcd-extensions", name);
		map.put(qName, value);
	}

	private void addFactors(ImpactCategory impact, LCIAMethod lciaMethod) {
		FactorList list = new FactorList();
		lciaMethod.setCharacterisationFactors(list);
		for(ImpactFactor olcaFactor : impact.getImpactFactors()) {
			Factor ilcdFactor = new Factor();
			list.getFactor().add(ilcdFactor);
			// TODO: uncertainty values + formulas
			ilcdFactor.setMeanValue(getRefAmount(olcaFactor));
			DataSetReference ref = ExportDispatch.forwardExportCheck(
					olcaFactor.getFlow(), database, dataStore);
			ilcdFactor.setReferenceToFlowDataSet(ref);
		}
	}

	private double getRefAmount(ImpactFactor factor) {
		double val = factor.getValue();
		Unit unit = factor.getUnit();
		if(unit != null)
			val = val / unit.getConversionFactor();
		FlowPropertyFactor propFactor = factor.getFlowPropertyFactor();
		if(propFactor != null)
			val = val * propFactor.getConversionFactor();
		return val;
	}
}
