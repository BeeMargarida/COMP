import java.util.ArrayList;

public class Utils {
    public static String ARGSLIST = "ArgumentList";
    public static String FUNCTION = "Function";
    public static String ARRAY = "Array";
    public static String SCALAR = "Scalar";
    public static String CALL = "Call";
    public static String ARG = "Argument";
    public static String OP = "Operation";
    public static String TERM = "Term";
    public static String NUMBER = "Number";
    public static String RHS = "Rhs";

    public static String WAS_CALLED = "WasCalled";

    // Checks if there is a node on array with the same value
    public static SimpleNode contains(ArrayList<SimpleNode> array, SimpleNode node) {
      if (array != null) {
        for (int i = 0 ; i < array.size() ; i++) {
          if (array.get(i).getValue().equals(node.getValue()) && 
            array.get(i).getType().equals(node.getType())) {
            return array.get(i);
          }
              
        }
      }
        
      return null;
    }

    public static void dumpType(String prefix, SimpleNode node) {
        if (node.getType() != null)
          System.out.println(prefix + node.getType() +" " + node.getValue());
          
        if (node.jjtGetNumChildren() != 0) {
          for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
            SimpleNode n = (SimpleNode)node.jjtGetChild(i);
            if (n != null) {
              dumpType(prefix + " ", n);
              //System.out.println(prefix + "Scope: " + (currentScope + 1));
            }
          }
        }
      }
}