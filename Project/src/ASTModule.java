/* Generated By:JJTree: Do not edit this line. ASTModule.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTModule extends SimpleNode {

  public String name;

  public ASTModule(int id) {
    super(id);
  }

  public ASTModule(yal2jvm p, int id) {
    super(p, id);
  }

  public String toString(String prefix) {
  	return prefix + "Module: " + name;
  }


}
/* JavaCC - OriginalChecksum=f67b96868b932ccf0f0b0edfdb7a7ae5 (do not edit this line) */
