import java.util.ArrayList;

public class Utils {
    public static String ARGSLIST = "ArgumentList";
    public static String FUNCTION = "Function";
    public static String ARRAY = "Array";
    public static String SCALAR = "Scalar";
    public static String CALL = "Call";
    public static String ARG = "Argument";
    public static String OP = "Operation";
    public static String RIGHT_TERM = "Right_Term";

    // Checks if there is a node on array with the same value
    public static boolean contains(ArrayList<SimpleNode> array, SimpleNode node) {
        for (int i = 0 ; i < array.size() ; i++) {
            if (array.get(i).getValue() == node.getValue())
                return true;
        }
        
        return false;
    }
}