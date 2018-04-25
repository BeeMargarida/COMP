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
			for (int i = 0; i < array.size(); i++) {
				if (array.get(i).getValue().equals(node.getValue()) && array.get(i).getType().equals(node.getType())) {
					return array.get(i);
				}

			}
		}

		return null;
	}

	public static void dumpType(String prefix, SimpleNode node) {
		if (node.getType() != null)
			System.out.println(prefix + "node " + node + " type " + node.getType() + " value " + node.getValue());

		if (node.jjtGetNumChildren() != 0) {
			for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
				SimpleNode n = (SimpleNode) node.jjtGetChild(i);
				if (n != null)
					dumpType(prefix + " ", n);
			}
		}
	}

	public static boolean checkFor(String value, SimpleNode node) {
		boolean b = false;

		if (node.getType().equals(value))
			b = true;
		else if (node.jjtGetNumChildren() != 0) {
			for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
				SimpleNode n = (SimpleNode) node.jjtGetChild(i);
				if (n != null) {
					if (n.getType().equals(value))
						return true;
					else
						b = checkFor(value, n);
				}
			}
		}
		return b;
	}
}