/* Generated By:JJTree: Do not edit this line. ASTElse.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTElse extends SimpleNode {

  public ASTElse(int id) {
    super(id);
    type = Utils.COND_STRUCTS;
  }

  public ASTElse(Parser p, int id) {
    super(p, id);
  }

  public String toString(String prefix) {
  	return prefix + "Else Statement: "; 
  }

}
/* JavaCC - OriginalChecksum=1cb44ac3da0d20a7be9538244d92847c (do not edit this line) */
