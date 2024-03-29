/* Generated By:JJTree: Do not edit this line. ASTTerm.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTTerm extends SimpleNode {
  public String termContent;
  private boolean isNegative = false;

  public ASTTerm(int id) {
    super(id); 
    type = Utils.TERM;
  }

  public ASTTerm(yal2jvm p, int id) {
    super(p, id);
    type = Utils.TERM;
  }

  public String toString(String prefix) {
    if (isNegative && value != null) {
      return prefix + "-" + value;
    }
    else if (value != null)
      return prefix + value;
  
    return null;   
  }

  public void setOperator(String operator) {
    if (operator.equals("-")) {
      isNegative = true;
    }
    else 
      isNegative = false;
  }

  public boolean getNegative(){
    return isNegative;
  }
}
/* JavaCC - OriginalChecksum=83d1bbf70069cf6a66041edc517e41db (do not edit this line) */
