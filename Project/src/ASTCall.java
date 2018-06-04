/* Generated By:JJTree: Do not edit this line. ASTCall.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public class ASTCall extends SimpleNode {
  public String calledFunction;

  public String packageName;
  
  public ASTCall(int id) {
    super(id);
    setType(Utils.CALL);
  }

  public ASTCall(Parser p, int id) {
    super(p, id);
  }

  public String toString(String prefix) {
    value = calledFunction;
    if (packageName != null) {
      return prefix + packageName + "." + calledFunction;
    }
  	else {
      return prefix + calledFunction;
    }
  }

  public String getPackage() { 
    if (packageName != null)
      return packageName.toLowerCase(); 
     
    return packageName;
  }

  public String getCalledFunction() {
    if (calledFunction != null)
      return calledFunction.toLowerCase();
    
    return calledFunction;
  }

}
/*
 * JavaCC - OriginalChecksum=5206d28e366ee8ca4b52058e60679d46 (do not edit this
 * line)
 */
