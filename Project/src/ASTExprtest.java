/* Generated By:JJTree: Do not edit this line. ASTExprtest.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTExprtest extends SimpleNode {
  public String relOp;

  public ASTExprtest(int id) {
    super(id);
  }

  public ASTExprtest(Parser p, int id) {
    super(p, id);
  }

  public String toString(String prefix) {
    String toReturn = "  " + prefix + "" + relOp + " \n" + this.jjtGetChild(0).toString() + " " +
      this.jjtGetChild(1).toString();
    
    return toReturn;
  }

}
/* JavaCC - OriginalChecksum=67c911a4aaa1156435924bee7f590e88 (do not edit this line) */
