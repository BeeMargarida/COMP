/* Generated By:JJTree: Do not edit this line. ASTScalarAccess.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTScalarAccess extends SimpleNode {
  public String content, size;

  public ASTScalarAccess(int id) {
    super(id, "Scalar");
  }

  public ASTScalarAccess(Parser p, int id) {
    super(p, id);
  }

  public String toString(String prefix) {
    if (size != null)
      value = content + "." + size;
    else
      value = content;
    
    return prefix + value;
  }

}
/* JavaCC - OriginalChecksum=18593799f559e2f31668aff54fba25f1 (do not edit this line) */
