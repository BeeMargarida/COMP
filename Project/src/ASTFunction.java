/* Generated By:JJTree: Do not edit this line. ASTFunction.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public class ASTFunction extends SimpleNode {
	public String functionName;
	public String returnType;
	public String returnValue;

	public ASTFunction(int id) {
		super(id);
		type = Utils.FUNCTION;
	}

	public ASTFunction(Parser p, int id) {
		super(p, id);
	}

	public String toString(String prefix) {
		value = functionName;
		return prefix + "Function " + functionName;
	}
	
	public String getReturnType() {
		return returnType;
	}

	public String getReturnValue(){
		if (returnValue != null)
			return returnValue.toLowerCase();
		
		return returnValue;
	}

}
/* JavaCC - OriginalChecksum=008affcfdc665974ee133dc683ec5c7c (do not edit this line) */
