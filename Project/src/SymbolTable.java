import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class SymbolTable {

	private String currentScope;

	// HashMap<ScopeString, SymbolTree>
	private HashMap<String, ArrayList<SimpleNode>> symbolTrees;
	private ArrayList<SimpleNode> declarations;
	private ArrayList<SimpleNode> functions;
	private ArrayList<SimpleNode> calls;

	public SymbolTable() {
		symbolTrees = new HashMap<>();
		declarations = new ArrayList<>();
		functions = new ArrayList<>();
		calls = new ArrayList<>();

		currentScope = "";
	}

	public boolean lookup(SimpleNode node) {
		if (currentScope == "") { // Is outside all functions
			return Utils.contains(declarations, node);
		}
		else if (symbolTrees.containsKey(currentScope)) {
			ArrayList<SimpleNode> scopeSymbols = symbolTrees.get(currentScope);
			return Utils.contains(scopeSymbols, node);
		}
		
		return false;
	}

	public void push(SimpleNode nodeToAdd) {
		// Is outside all functions, is a declaration
		if (currentScope == "") {
			declarations.add(nodeToAdd);
			return;
		}
		// Check if there is no element associated to current scope
		else if (!symbolTrees.containsKey(currentScope)) {
			ArrayList<SimpleNode> newSymbolTree = new ArrayList<>();

			newSymbolTree.add(nodeToAdd);

			symbolTrees.put(currentScope, newSymbolTree);
		} else { 
			// There were already elements in scope, check if element was aready declared
			ArrayList<SimpleNode> scopeTree = symbolTrees.get(currentScope);
			
			scopeTree.add(nodeToAdd);

			symbolTrees.put(currentScope, scopeTree);
		}

	}

	/**
	 * Adds all symbols to array.
	 */
	public void fillSymbols(SimpleNode node, String scope) {
		currentScope = scope;
		
		// Goes through all the children
		for (int i = 0; i < node.jjtGetNumChildren() ; i++) {
			SimpleNode nodeToAnalyse = (SimpleNode) node.jjtGetChild(i);

			// If it is an operation, do a semantic analysis
			if (nodeToAnalyse.getType() == Utils.OP) { // Is operation
				System.out.println("IS IN OPERATION " + nodeToAnalyse.getValue());
				analyseOperation(nodeToAnalyse);
			} else {
				
				// Inside a new function, belongs to another scope
				if (nodeToAnalyse.getType() == Utils.FUNCTION) {

					System.out.println("IS IN FUNCTION " + nodeToAnalyse.getValue());
					scope = nodeToAnalyse.getValue();

					// Function Name was already encountered before
					if (Utils.contains(functions, nodeToAnalyse)) {
						System.out.println("There are more than one function with name " + 
							nodeToAnalyse.getValue() + ".");
					} else 
						functions.add(nodeToAnalyse);

					
				} else if (nodeToAnalyse.getType() == Utils.CALL) {
					// Encountered a call, will analyse later
					calls.add(nodeToAnalyse);
				}
				else if (!lookup(nodeToAnalyse))
					push(nodeToAnalyse);
				
			}
			
			// Repeate process
			fillSymbols(nodeToAnalyse, scope);
		}
	}

	// Semantically analise operations
	public void analyseOperation(SimpleNode node) {
		System.out.println("Iteration " + node + " Type " + node.getType());

		SimpleNode leftChild = (SimpleNode) node.jjtGetChild(0);
		SimpleNode rightChild = (SimpleNode) node.jjtGetChild(1);
		
		// In case RHS has some hidden operations
		if (rightChild.getType() == Utils.RIGHT_TERM || rightChild.getType() == Utils.OP) { 
			String rightType = recursiveOperationAnalysis(rightChild);

			if (rightType == null)
				return;

		} else if (rightChild.getType() == Utils.CALL) {
			// Is a call, cannot analyse here
			calls.add(rightChild);
			return;
		} else {
			// Direct check between two operatives
			analyseTwoNodesOperation(leftChild, rightChild);
		}		
	}

	// TODO ACABAR ISTO
	private String recursiveOperationAnalysis(SimpleNode rightChild) {
		SimpleNode leftNode = (SimpleNode) rightChild.jjtGetChild(0);

		for (int i = 1 ; i < rightChild.jjtGetNumChildren() ; i++) {
			String rightType = recursiveOperationAnalysis((SimpleNode) rightChild.jjtGetChild(i));

			// Something went wrong further down, no need to analyse, error already reported
			if (rightType == null)
				return null;
			
		}

		return leftNode.getType();

	}

	private void analyseTwoNodesOperation(SimpleNode leftChild, SimpleNode rightChild) {
		String leftType = leftChild.getType();
		String rightType = rightChild.getType();
		
		if (!lookup(rightChild)) { // Right Hand Side variable was not initialized
			System.out.println("Variable " + rightChild.getValue() + " was not initialized.");
			return;
		}

		if (lookup(leftChild)) { // Was already declared
			// If they are scalars or arrays
			if ((leftType == "Scalar" || leftType == "Array") && 
			(rightType == "Scalar" || rightType == "Array")) {
				if (leftChild.getType() != rightChild.getType()) {
					System.out.println("Semantic Error: Left Hand Side Value " + leftChild.getValue()
							+ " and Right Hand Side Value " + rightChild.getValue());
					return;
				}
			}
		} else // Was not present, new initialization
			leftChild.setType(rightChild.getType());		

		leftChild.initialize();
	}

	public void analyseCalls() {
		/*
		System.out.println("Declarations " + declarations.toString());
		System.out.println("Functions " + functions.toString());	
		System.out.println("Calls " + calls.toString());
		System.out.println("Symbol Trees " + symbolTrees.toString());
		*/

		for (int i = 0; i < calls.size() ; i++) {

		}
	}
}
