/* Generated By:JJTree: Do not edit this line. ASTVarList.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTVarList extends SimpleNode {

  public String varList;
  public ASTVarList(int id) {
    super(id);
  }

  public ASTVarList(Parser p, int id) {
    super(p, id);
  }

  public String toString(String prefix) {
  	if (varList != null)
  		return prefix + "VarList (" + varList + " )";
  	else
  		return "";
  }

}
/* JavaCC - OriginalChecksum=df8e6a9b240926a2b045ef02c03e7142 (do not edit this line) */