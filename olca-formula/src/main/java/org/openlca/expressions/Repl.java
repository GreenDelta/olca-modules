package org.openlca.expressions;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * A read-eval-print-loop for formula evaluation.
 */
public class Repl {

	private final BufferedReader in;
	private final PrintStream out;
	private final PrintStream err;
	private final FormulaInterpreter evaluator = new FormulaInterpreter();
	private Runnable exitHook;

	public Repl(InputStream in, PrintStream out, PrintStream err) {
		this.in = new BufferedReader(new InputStreamReader(in));
		this.out = out;
		this.err = err;
	}

	/**
	 * Registers an exit-hook for this Repl. The given runnable is called when
	 * the user quits this Repl instance.
	 *
	 * @param fn the exit-hook
	 */
	public Repl onExit(Runnable fn) {
		this.exitHook = fn;
		return this;
	}

	public void start() {
		printWelcome();
		String line;
		try {
			while ((line = in.readLine()) != null) {
				String expression = line.trim();
				if (expression.equalsIgnoreCase("exit")
					|| expression.equalsIgnoreCase("quit"))
					break;
				eval(expression);
				out.print(">>> ");
			}
		} catch (Exception e) {
			err.println("An unexpected error occurred: " + e.getMessage());
		}
		out.flush();
		err.flush();
		if (exitHook != null) {
			exitHook.run();
		}
	}

	private void eval(String expression) {
		if (expression.isEmpty()) {
			out.println();
		} else if (expression.equalsIgnoreCase("help")) {
			printHelp();
		} else if (expression.startsWith("var ")) {
			evalVariable(expression);
		} else {
			evalFormula(expression);
		}
	}

	private void printWelcome() {
		out.println("openLCA Formula Interpreter");
		out.println("Type \"help\" for more information.");
		out.println();
		out.print(">>> ");
	}

	private void printHelp() {
		out.println(" evaluate an expression: " +
			"\t type in the expression and press enter, e.g. sin(42)");
		out.println(" define a variable: " +
			"\t\t type var <variable name> = <expression>, e.g. var a = sin(42)");
		out.println(" exit the interpreter: " +
			"\t\t type 'exit' or 'quit' and press enter");
	}

	private void evalVariable(String expression) {
		StringBuffer variableNameBuf = new StringBuffer();
		StringBuffer formulaBuf = new StringBuffer();
		parseVariableExpression(expression.substring(4), variableNameBuf,
			formulaBuf);
		String variableName = variableNameBuf.toString().trim();
		String formula = formulaBuf.toString().trim();
		if (variableName.isEmpty() || formula.isEmpty()) {
			err.println("Error: wrong variable declaration (syntax is "
				+ "'var <identifier> = <expression>'). ");
		} else {
			evalVariable(variableName, formula);
		}
	}

	private void parseVariableExpression(String expression,
		StringBuffer variableNameBuf, StringBuffer formulaBuf) {
		boolean after = false;
		for (char c : expression.toCharArray()) {
			if (c != '=' && !after) {
				variableNameBuf.append(c);
			} else if (c == '=' && !after) {
				after = true;
			} else {
				formulaBuf.append(c);
			}
		}
	}

	private void evalVariable(String variableName, String formula) {
		try {
			evaluator.getGlobalScope().bind(variableName, formula);
			double val = evaluator.eval(variableName);
			out.println(variableName + " = " + val);
		} catch (Exception e) {
			err.println("Error: " + e.getMessage());
		}
	}

	private void evalFormula(String expression) {
		try {
			Object result = evaluator.eval(expression);
			out.println(result);
		} catch (Exception e) {
			err.println("Error: " + e.getMessage());
		}
	}

	public static void main(String[] args) {
		new Repl(System.in, System.out, System.err)
			.onExit(() -> System.out.println("Bye!"))
			.start();
	}

}
