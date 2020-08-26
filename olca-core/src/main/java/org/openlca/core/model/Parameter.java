package org.openlca.core.model;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.openlca.expressions.FormulaInterpreter;
import org.openlca.expressions.InterpreterException;

/**
 * In openLCA, parameters can be defined in different scopes: global, process,
 * or LCIA method. The parameter name can be used in formulas and, thus, need to
 * conform to a specific syntax. Within a scope the parameter name should be
 * unique (otherwise the evaluation is not deterministic). There are two types
 * of parameters in openLCA: input parameters and dependent parameters. An input
 * parameter can have an optional uncertainty distribution but not a formula. A
 * dependent parameter can (should) have a formula (where also other parameters
 * can be used) but no uncertainty distribution.
 */
@Entity
@Table(name = "tbl_parameters")
public class Parameter extends CategorizedEntity {

	/**
	 * The scope of the parameter (global, process, LCIA method).
	 */
	@Column(name = "scope")
	@Enumerated(EnumType.STRING)
	public ParameterScope scope = ParameterScope.GLOBAL;

	/**
	 * Indicates whether the parameter is an input parameter.
	 */
	@Column(name = "is_input_param")
	public boolean isInputParameter;

	/**
	 * The value of the parameter.
	 */
	@Column(name = "value")
	public double value;

	/**
	 * The formula of the parameter (only valid for dependent parameters).
	 */
	@Column(name = "formula")
	public String formula;

	/**
	 * The uncertainty distribution of the parameter value (only valid for input
	 * parameters).
	 */
	@Embedded
	public Uncertainty uncertainty;

	/**
	 * A reference to an external source of the parameter (e.g. a shape file in
	 * a regionalized LCIA method).
	 */
	@Column(name = "external_source")
	public String externalSource;

	/**
	 * If the parameter has an external source the type of this source can be
	 * specified in this field.
	 */
	@Column(name = "source_type")
	public String sourceType;

	/**
	 * Returns true if the given name is a valid identifier for a parameter. We
	 * allow the same rules as for Java identifiers.
	 */
	public static boolean isValidName(String name) {
		if (name == null)
			return false;
		String id = name.trim();
		if (id.isEmpty())
			return false;
		for (int i = 0; i < id.length(); i++) {
			char c = id.charAt(i);
			if (i == 0 && !Character.isLetter(c))
				return false;
			if (i > 0 && !Character.isJavaIdentifierPart(c))
				return false;
		}
		FormulaInterpreter interpreter = new FormulaInterpreter();
		interpreter.bind(name, "1");
		try {
			interpreter.eval(name);
		} catch (InterpreterException e) {
			return false;
		}
		return true;
	}

	@Override
	public Parameter clone() {
		Parameter clone = new Parameter();
		Util.copyRootFields(this, clone);
		clone.formula = formula;
		clone.isInputParameter = isInputParameter;
		clone.scope = scope;
		if (uncertainty != null)
			clone.uncertainty = uncertainty.clone();
		clone.value = value;
		clone.externalSource = externalSource;
		clone.sourceType = sourceType;
		return clone;
	}

	@Override
	public String toString() {
		return "Parameter [formula=" + formula + ", name=" + name
				+ ", type=" + scope + "]";
	}

}
