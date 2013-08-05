package org.openlca.simapro.csv.model;

import java.util.ArrayList;
import java.util.List;

/**
 * The class represents a SimaPro CSV file. It contains SimaPro processes which
 * will be written into a CSV file
 */
public class SPDataSet {

	/**
	 * The name of the project
	 */
	private String project;

	/**
	 * The global calculated parameters
	 */
	private List<SPCalculatedParameter> calculatedParameters = new ArrayList<SPCalculatedParameter>();

	/**
	 * The global input parameters
	 */
	private List<SPInputParameter> inputParameters = new ArrayList<SPInputParameter>();

	/**
	 * The processes the data set contains
	 */
	private List<SPProcess> processes = new ArrayList<SPProcess>();

	/**
	 * The quantities the data set contains
	 */
	private List<SPQuantity> quantities = new ArrayList<SPQuantity>();

	/**
	 * The waste treatments the data set contains
	 */
	private List<SPWasteTreatment> wasteTreatments = new ArrayList<SPWasteTreatment>();

	/**
	 * Creates a new data set
	 * 
	 * @param project
	 *            The name of the project
	 */
	public SPDataSet(String project) {
		this.project = project;
	}

	/**
	 * Adds a calculated parameter to the data set
	 * 
	 * @param parameter
	 *            The parameter to add
	 */
	public void add(SPCalculatedParameter parameter) {
		calculatedParameters.add(parameter);
	}

	/**
	 * Adds an input parameter to the data set
	 * 
	 * @param parameter
	 *            The parameter to add
	 */
	public void add(SPInputParameter parameter) {
		inputParameters.add(parameter);
	}

	/**
	 * Adds a process to the data set
	 * 
	 * @param process
	 *            The process to add
	 */
	public void add(SPProcess process) {
		processes.add(process);
	}

	/**
	 * Adds a quantity to the data set
	 * 
	 * @param quantity
	 *            The quantity to add
	 */
	public void add(SPQuantity quantity) {
		quantities.add(quantity);
	}

	/**
	 * Adds a waste treatment to the data set
	 * 
	 * @param wasteTreatment
	 *            The waste treatment to add
	 */
	public void add(SPWasteTreatment wasteTreatment) {
		wasteTreatments.add(wasteTreatment);
	}

	/**
	 * Getter of the global calculated parameters
	 * 
	 * @see SPCalculatedParameter
	 * @return The global calculated parameters of the data set, which can be
	 *         used by all data entries
	 */
	public SPCalculatedParameter[] getCalculatedParameters() {
		return calculatedParameters
				.toArray(new SPCalculatedParameter[calculatedParameters.size()]);
	}

	/**
	 * Getter of the global input parameters
	 * 
	 * @see SPInputParameter
	 * @return The global input parameters of the data set, which can be used by
	 *         all data entries
	 */
	public SPInputParameter[] getInputParameters() {
		return inputParameters.toArray(new SPInputParameter[inputParameters
				.size()]);
	}

	/**
	 * Getter of the processes
	 * 
	 * @see SPProcess
	 * @return The SimaPro processes which will be written into a CSV file
	 */
	public SPProcess[] getProcesses() {
		return processes.toArray(new SPProcess[processes.size()]);
	}

	/**
	 * Getter of the project
	 * 
	 * @return The name of the SimaPro project containing the processes
	 */
	public String getProject() {
		return project;
	}

	/**
	 * Getter of the quantities
	 * 
	 * @see SPQuantity
	 * @return The quantities of the data set
	 */
	public SPQuantity[] getQuantities() {
		return quantities.toArray(new SPQuantity[quantities.size()]);
	}

	/**
	 * Getter of the waste treatments
	 * 
	 * @see SPWasteTreatment
	 * @return The SimaPro waste treatments which will be written into a CSV
	 *         file
	 */
	public SPWasteTreatment[] getWasteTreatments() {
		return wasteTreatments.toArray(new SPWasteTreatment[wasteTreatments
				.size()]);
	}

	/**
	 * Setter of the project
	 * 
	 * @param project
	 *            The new project name
	 */
	public void setProject(String project) {
		this.project = project;
	}
	
}
