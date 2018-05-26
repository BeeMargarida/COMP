/* Generated By:JJTree: Do not edit this line. ASTArrayAccess.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTArrayAccess extends SimpleNode {
  public String content, index;

  public ASTArrayAccess(int id) {
    super(id);
    type = Utils.ARRAY_ACCESS;
  }

  public ASTArrayAccess(Parser p, int id) {
    super(p, id);
  }

  public String toString(String prefix) {
    if (content != null) {
      value = content;
      return prefix + value + "[" + index + "]";
    }
    else {
      value = "[" + index + "]";
      return prefix + value;
    }
    
  }

}
/* JavaCC - OriginalChecksum=0e62b27ae5af6ae9e88f185eb64b929b (do not edit this line) */
