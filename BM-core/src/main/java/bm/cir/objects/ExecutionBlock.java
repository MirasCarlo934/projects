package bm.cir.objects;

import org.apache.log4j.Logger;

import bm.tools.StringTools;
import bm.tools.StringTools.StringInjectionException;

public class ExecutionBlock extends CodeBlock {
	private static final Logger logger = Logger.getLogger(ExecutionBlock.class);
	
	public ExecutionBlock(String comID, String comProperty, Object comValue) {
		super(comID, comProperty, comValue);
	}

	@Override
	public String toString() {
		return StringTools.injectStrings("%s:%s = %s", new String[]{getComID(), getPropSSID(), getPropValue().toString()}, 
				"%s");
	}

}
