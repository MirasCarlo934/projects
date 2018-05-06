package bm.context.properties;

public enum PropertyMode {
	I, O, IO, Null;
	
//	private String string;
//	
//	private PropertyMode(String string) {
//		this.string = string;
//	}
	
	public static PropertyMode parseModeFromString(String str) {
		if(str.equals("I"))
			return I;
		else if(str.equals("O"))
			return O;
		else if(str.equals("IO"))
			return IO;
		else
			return Null;
	}
}
