import java.util.HashMap;

public class SymbolTable {

	private int currentScope;
	
	// HashMap<ScopeString, SymbolTree>
	private HashMap<String, SymbolTree> symbolTrees;

    public SymbolTable() {
        symbolTrees = new HashMap<>();
    }

    public boolean lookup(Symbol symbolToEvaluate) {
		/*
		for (Symbol symbol : symbols) {
            if (symbol.getValue() == symbolToEvaluate.getValue()  
                    && symbol.getScope() >= symbolToEvaluate.getScope())
                return true;
        }
        */
        return false;
    }

    public boolean push(Symbol symbol) {
		// Maybe do check here? Dunno tbh
		//symbols.add(symbol);
        
        return true;
    }

	/**
	 * Adds all symbols to array.
	 */
    public void fillSymbols(SimpleNode node, int currentScope) {
		push(new Symbol(node, currentScope));

		if (node.jjtGetNumChildren() != 0) {
		  for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
			System.out.println("Type is " + node.getType());
			SimpleNode n = (SimpleNode)node.jjtGetChild(i);
			if (n != null) {
			  fillSymbols((SimpleNode)node.jjtGetChild(i), currentScope + 1);
			}
		  }
		}
		  
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
		if ((leftType == "Scalar" || leftType == "Array")
		  	&& (rightType == "Scalar" || rightType == "Array")) {
			if (leftChild.getType() != rightChild.getType()) {
				System.out.println("Semantic Error: Left Hand Side Value " + 
					leftChild.getValue() + " and Right Hand Side Value " + 
					rightChild.getValue());
			}
		}
	}
}
