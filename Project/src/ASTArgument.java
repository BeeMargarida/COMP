/* Generated By:JJTree: Do not edit this line. ASTArgument.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTArgument extends SimpleNode {
  public String content; 

  public ASTArgument(int id) {
    super(id);
    type = Utils.ARG;
  }

  public ASTArgument(Parser p, int id) {
    super(p, id);
  }

  public String toString(String prefix) {
    value = content;
  	return prefix + content;
  }
}
/* JavaCC - OriginalChecksum=7ea2693101e6b88b1fa4c45533b9d64f (do not edit this line) */
