/* Generated By:JJTree: Do not edit this line. ASTCall.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTCall extends SimpleNode {

  public String value;
  public String calledFunction;
  
  public ASTCall(int id) {
    super(id);
  }

  public ASTCall(Parser p, int id) {
    super(p, id);
  }

  public String toString(String prefix) {
  	if (value != null && calledFunction != null) 
  		return prefix + value + " calls " + calledFunction;
  	else if (value != null) 
  		return prefix +"Called " + value;
  	else 
  		return "";
  }

}
/* JavaCC - OriginalChecksum=5206d28e366ee8ca4b52058e60679d46 (do not edit this line) */