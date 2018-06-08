package bm.cir.objects;

import java.util.HashMap;

import bm.context.HTMLTransformable;
import bm.context.devices.Device;
import bm.context.properties.Property;
import bm.tools.StringTools;

public class Rule implements HTMLTransformable {
	private int index;
	private String name;
//	private Conditional condition;
	private Argument[] arguments;
	private ExecutionBlock[] execBlocks;
	
	/**
	 * Creates an blank CIR rule.
	 */
	public Rule(String name) {
		this.name = name;
	}
	
	public Rule(int index, String name, Argument[] arguments, ExecutionBlock[] execBlocks) {
		this.index = index;
		this.name = name;
//		setCondition(condition);
		setArguments(arguments);
		setExecBlocks(execBlocks);
	}

//	public Rule(int index, String name, Conditional condition, Argument[] arguments, ExecutionBlock[] execBlocks) {
//		this.index = index;
//		this.name = name;
//		setCondition(condition);
//		setArguments(arguments);
//		setExecBlocks(execBlocks);
//	}
	
	/**
	 * Checks if the specified component, property, and property value satisfies an argument in this rule
	 * 
	 * @param d The component to be checked
	 * @param p The property paired with the component to be checked. The property value to be checked will be 
	 * 		retrieved from this
	 * @return <b>True</b> if the specified Component exists in the arguments section, 
	 * 		<b>false</b> otherwise
	 */
	public boolean containsArgument(Device d, Property p) {
		boolean b = false;
		for(int i = 0; i < arguments.length; i++) {
			Argument arg = arguments[i];
			if(arg.getComID().equals(d.getSSID())) {
				if(arg.getPropSSID().equals(p.getSSID())) {
					b = true;
					break;
				}
			}
		}
		return b;
	}
	
	/**
	 * Checks if the specified array of components satisfies this rule's arguments
	 * 
	 * @param devices The array of components to check
	 * @return <b>true</b> if rule is satisfied, <b>false</b> otherwise
	 */
	public boolean isSatisfiedWith(Device[] devices) {
//		boolean[] argSatisfaction = new boolean[arguments.length];
		HashMap<String, Device> coms = new HashMap<String, Device>(devices.length);
		for(int i = 0; i < devices.length; i++) {
			Device com = devices[i];
			coms.put(com.getSSID(), com);
		}
		
		for(int i = 0; i < arguments.length; i++) {
			Argument arg = arguments[i];
			Device dev = coms.get(arg.getComID());
			if(dev == null) {
				return false;
			}
			Property prop = dev.getProperty(arg.getPropSSID());
			//FIXME Rule: Compare raw data types, not just strings! To avoid having to specify decimal numbers in CIR!
			if(!prop.getValue().toString().equals(arg.getPropValue().toString())) {
				return false;
			}
		}
		
		return true;
	}

//	/**
//	 * @return the condition
//	 */
//	public Conditional getCondition() {
//		return condition;
//	}
//
//	/**
//	 * @param condition the condition to set
//	 */
//	public void setCondition(Conditional condition) {
//		this.condition = condition;
//	}

	/**
	 * @return the execBlocks
	 */
	public ExecutionBlock[] getExecBlocks() {
		return execBlocks;
	}

	/**
	 * @param execBlocks the execBlocks to set
	 */
	public void setExecBlocks(ExecutionBlock[] execBlocks) {
		this.execBlocks = execBlocks;
	}

	/**
	 * Returns the Arguments block in order based on groupings.
	 * 
	 * @return the arguments
	 */
	public Argument[] getArguments() {
		return arguments;
	}

	/**
	 * @param arguments the arguments to set
	 */
	public void setArguments(Argument[] arguments) {
		this.arguments = arguments;
	}
	
	public String convertToJavascript() {
		String script = "";
		String argsArray = "var args" + index + " = [ ";
		String execsArray = "var execs" + index + " = [ ";
		String rule = "var rule" + index + " = new CIRRule(\"" + name + "\", args" + index + ", execs" + index + ");";
		Argument[] args = getArguments();
		ExecutionBlock[] execs = getExecBlocks();
		
		for(int i = 0; i < args.length; i++) {
			Argument arg = args[i];
			script += "var rule_" + index + "_arg_" + arg.getComID() + "_" + arg.getPropSSID() + " = new Component('" + arg.getComID() + "', "
					+ "[{id:'" + arg.getPropSSID() + "', value:'" + arg.getPropValue() 
					+ "', operator:'" + arg.getOperator().getSymbol() + "'}]); \n";
			argsArray += "rule_" + index + "_arg_" + arg.getComID() + "_" + arg.getPropSSID() +  ",";
		}
		
		for(int i = 0; i < execs.length; i++) {
			ExecutionBlock exec = execs[i];
			script += "var rule_" + index + "_exec_" + exec.getComID() + "_" + exec.getPropSSID() + " = new Component('" + exec.getComID() + "', "
					+ "[{id:'" + exec.getPropSSID() + "', value:'" + exec.getPropValue() 
					+ "', operator:'='}]); \n";
			execsArray += "rule_" + index + "_exec_" + exec.getComID() + "_" + exec.getPropSSID() +  ",";
		}
		
		argsArray = argsArray.substring(0, argsArray.length() - 1) + "];";
		execsArray = execsArray.substring(0, execsArray.length() - 1) + "];";
		
		return script + "\n" + argsArray + "\n" + execsArray + "\n" + rule;
	}
	
	/**
	 * Returns a String representation of this CIR Statement.
	 */
	public String toString() {
		String args = "";
		String execs = "";
		String str = null;
		
		for(int i = 0; i < arguments.length; i++) {
			args += arguments[i] + " ";
		}
		for(int i = 0; i < execBlocks.length; i++) {
			execs += execBlocks[i] + " AND ";
		}
		execs = execs.substring(0, execs.length() - 4);
		
		str = StringTools.injectStrings("IF %s THEN %s", new String[]{/*getCondition().toString(), */args, execs}, "%s");
		
		return str;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	public int getIndex() {
		return index;
	}
}
