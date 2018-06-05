import java.util.ArrayList;

public class Utils {
	public static String VARLIST = "ArgumentList";
	public static String FUNCTION = "Function";
	public static String ARRAY = "Array";
	public static String SCALAR = "Scalar";
	public static String VOID = "Void";
	public static String CALL = "Call";
	public static String EXTERNAL_CALL = "External Package Call";
	public static String OP = "Operation";
	public static String TERM = "Term";
	public static String NUMBER = "Number";
	public static String RHS = "Rhs";
	public static String COND = "Conditional";
	public static String ELSE = "Else";
	public static String ARRAY_INST = "Array Instantion"; // for [20]
	public static String ARRAY_INST_SCALAR = "Array Instantion Scalar"; // for [d]
	public static String SIZE = "Size";
	public static String DECLARATION = "Declaration";
	public static String DECLARATION_SCALAR = "Declaration Scalar";
	public static String DECLARATION_ARRAY = "Declaration Array";
	public static String ARRAY_ACCESS = "Array Access";
	public static String WAS_CALLED = "WasCalled";

	public static int NOT_INIT = 0;
	public static int MAYBE_INIT = 1;
	public static int DEFIN_INIT = 2;
	public static int INCOMPAT_INIT = 3;

	// Made to Extract node that contains a specific value and type
	public static SimpleNode contains(ArrayList<SimpleNode> array, SimpleNode node) {
		if (array != null && node != null) {
			for (int i = 0; i < array.size(); i++) {
				if (array.get(i).getValue() != null)
					if (array.get(i).getValue().equals(node.getValue())
							&& array.get(i).getType().equals(node.getType())) {
						return array.get(i);
					}
			}
		}

		return null;
	}

	public static void printNode(SimpleNode node) {
		System.out.println("Node is " + node + " type " + node.getType() 
			+ " value " + node.getValue() + " is Initialized " + node.isInitialized());
	}

	// Made to Extract node that contains a specific value
	public static SimpleNode containsValue(ArrayList<SimpleNode> array, SimpleNode node) {
		if (array != null && node != null) {
			for (int i = 0; i < array.size(); i++) {
				if (array.get(i).getValue() != null) {
					if (array.get(i).getValue().equals(node.getValue())) {
						return array.get(i);
					}
				}

			}
		}

		return null;
	}

	public static SimpleNode containsValueString(ArrayList<SimpleNode> array, String string) {
		if (array != null) {
			for (int i = 0; i < array.size(); i++) {
				if (array.get(i) != null && array.get(i).getValue() != null) {
					if (array.get(i).getValue().equals(string)) {
						return array.get(i);
					}
				}

			}
		}

		return null;
	}

	// Dumps to screen information about all nodes and subsequent children
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

	// Checks if there is node hidden in children with specific value 
	public static boolean checkFor(String value, SimpleNode node) {
		boolean b = false;

		if (node.getType().equals(value))
			b = true;
		else if (node.jjtGetNumChildren() != 0) {
			for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
				SimpleNode n = (SimpleNode) node.jjtGetChild(i);
				if (n != null && n.getType() != null) {
					if (n.getType().equals(value))
						return true;
					else
						b = checkFor(value, n);
				}
			}
		}
		return b;
	}

	// Extracts node above
	public static SimpleNode extractOfType(String value, SimpleNode node) {
		SimpleNode nodeExtracted = null;

		if (node.getType().equals(value))
			nodeExtracted = node;
		else if (node.jjtGetNumChildren() != 0) {
			for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
				SimpleNode n = (SimpleNode) node.jjtGetChild(i);
				if (n != null && n.getType() != null) {
					if (n.getType().equals(value))
						return n;
					else
						nodeExtracted = extractOfType(value, n);
				}
			}
		}
		return nodeExtracted;
	}

	public static ArrayList<SimpleNode> mergeArrays(ArrayList<SimpleNode> old, ArrayList<SimpleNode> newArray) {
		for (int i = 0; i < old.size(); i++) {
			if (Utils.contains(newArray, old.get(i)) == null) {
				newArray.add(old.get(i));
			}
		}

		return newArray;
	}
}