/* Generated By:JJTree: Do not edit this line. ASTArrayInstantion.java Version 7.0 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
public
class ASTArrayInstantion extends SimpleNode {
  public String size;
  
  public ASTArrayInstantion(int id) {
    super(id);
    type = Utils.ARRAY_INST;
  }

  public ASTArrayInstantion(Parser p, int id) {
    super(p, id);
  }
 
  public String toString(String prefix) {
    try  {
        Integer.parseInt(size);
    } catch (NumberFormatException ex)  {
    
    }
    value = size;
    return prefix + "[" + value + "]";
  }

}
/* JavaCC - OriginalChecksum=77d06f8b0f0c686c54f728b1a3175266 (do not edit this line) */
