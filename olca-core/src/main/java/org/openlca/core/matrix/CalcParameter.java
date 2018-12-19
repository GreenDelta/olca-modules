package org.openlca.core.matrix;

import org.openlca.core.model.ParameterScope;
import org.openlca.core.model.UncertaintyType;

class CalcParameter {

	public String name;
	public boolean inputParameter;
	public long owner;
	public ParameterScope scope;
	public double value;
	public String formula;

	public UncertaintyType uncertaintyType;
	public double parameter1;
	public double parameter2;
	public double parameter3;
	public String parameter1Formula;
	public String parameter2Formula;
	public String parameter3Formula;

}
