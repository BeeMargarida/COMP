import java.util.ArrayList;
import java.util.HashMap;

public class SymbolTable {

	private String currentScope;

	// HashMap<ScopeString, SymbolTree>
	private HashMap<String, ArrayList<Symbol>> symbolTrees;

	public SymbolTable() {
		symbolTrees = new HashMap<>();
		currentScope = "";
	}

	public boolean lookup(String scope, Symbol symbol) {
		if (symbolTrees.containsKey(scope))
			return symbolTrees.get(scope).contains(symbol);

		return false;
	}

	public void push(Symbol symbol) {
		// Check if there is no element associated to current scope
		if (!symbolTrees.containsKey(currentScope)) {

			ArrayList<Symbol> newSymbolTree = new ArrayList<>();
			newSymbolTree.add(symbol);

			symbolTrees.put(currentScope, newSymbolTree);
		} else { // There were already elements in scope
			if (!symbolTrees.get(currentScope).contains(symbol))
				symbolTrees.get(currentScope).add(symbol);
		}

	}

	/**
	 * Adds all symbols to array.
	 */
	public void fillSymbols(SimpleNode node, String scope) {
		currentScope = scope;

		if (node.jjtGetNumChildren() != 0) {
			for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
				System.out.println("Type is " + node.getType());

				SimpleNode n = (SimpleNode) node.jjtGetChild(i);

				if (isInDifferentScope(node.getType()))
					currentScope += "-" + node.getValue();
				if (n != null) {					
					fillSymbols((SimpleNode) node.jjtGetChild(i), currentScope + 1);
				}
			}
		}

	}

	public boolean isInDifferentScope(String type) {
		return type == Utils.FUNCTION || type == Utils.ARGSLIST || type == Utils.COND_STRUCTS;
	}

	public void semanticAnalysis() {
		/*for (int i = 0 ; i < symbols.size(); i++) {
			String type = symbols.get(i).getType();
			if (type != null)
				if (type == "Assign") { // Check children
					analyseChildren(symbols.get(i).getValue());
				}
		}
		    */
	}

	public void analyseChildren(SimpleNode node) {
		System.out.println("Analysing Children");

		SimpleNode leftChild = (SimpleNode) node.jjtGetChild(0);
		SimpleNode rightChild = (SimpleNode) node.jjtGetChild(1);

		String leftType = leftChild.getType();
		String rightType = rightChild.getType();

		// If they are scalars or arrays
		if ((leftType == "Scalar" || leftType == "Array") && (rightType == "Scalar" || rightType == "Array")) {
			if (leftChild.getType() != rightChild.getType()) {
				System.out.println("Semantic Error: Left Hand Side Value " + leftChild.getValue()
						+ " and Right Hand Side Value " + rightChild.getValue());
			}
		}
	}
}
