/* Generated By:JJTree: Do not edit this line. ASTAssign.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTAssign extends SimpleNode {
  public ASTAssign(int id) {
    super(id);
    type = Utils.OP;

  }

  public ASTAssign(Parser p, int id) {
    super(p, id);
  }

  public String toString(String prefix) {
  	return prefix + "= " ;
  }

}
/* JavaCC - OriginalChecksum=de72bb95384955638f6c336dea14bd0d (do not edit this line) */
