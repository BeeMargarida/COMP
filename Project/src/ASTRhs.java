/* Generated By:JJTree: Do not edit this line. ASTRhs.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTRhs extends SimpleNode {

  public ASTRhs(int id) {
    super(id);
    type = Utils.RHS;
  }

  public ASTRhs(Parser p, int id) {
    super(p, id);
  }
    
  public String toString(String prefix) {
    if(value != null){
      return prefix + value;
    }
    else
      return null;
  } 

}
/* JavaCC - OriginalChecksum=9e8e08102ca50579db5e2882e58e6298 (do not edit this line) */
