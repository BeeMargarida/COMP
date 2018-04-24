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

	public SimpleNode lookup(SimpleNode node) {
		if (currentScope == "")  // Is outside all functions
			return Utils.contains(declarations, node);
		
		else 
			return Utils.contains(symbolTrees.get(currentScope), node);
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

			//System.out.println("Currently analysed node " + nodeToAnalyse + " value " + 
			//	nodeToAnalyse.getValue());

			// If it is an operation, do a semantic analysis
			if (nodeToAnalyse.getType() == Utils.OP) { // Is operation
				System.out.println("Is in operation");
				analyseOperation(nodeToAnalyse);

				// No need to continue checking children, already analized
				return;
			} else {
				
				// Inside a new function, belongs to another scope
				if (nodeToAnalyse.getType() == Utils.FUNCTION) {
					
					System.out.println("IS IN FUNCTION " + nodeToAnalyse.getValue());
					scope = nodeToAnalyse.getValue();
					
					// Create new symbol tree for this scope
					ArrayList<SimpleNode> newNodesInScope = new ArrayList<>();
					symbolTrees.put(scope, newNodesInScope);

					// Function Name was already encountered before
					if (Utils.contains(functions, nodeToAnalyse) != null) {
						System.out.println("There are more than one function with name " + 
							nodeToAnalyse.getValue() + ".");
					} else 
						functions.add(nodeToAnalyse);
					
				} else if (nodeToAnalyse.getType() == Utils.CALL) {
					// Encountered a call, will analyse later
					calls.add(nodeToAnalyse);

					// No need to analyse children
					return;

				} else if (nodeToAnalyse.getType() == Utils.ARGSLIST) {
					ArrayList<SimpleNode> nodesInScope = symbolTrees.get(currentScope);

					for (int j = 0 ; j < nodeToAnalyse.jjtGetNumChildren() ; j++) {
						SimpleNode newNode = (SimpleNode) nodeToAnalyse.jjtGetChild(j);
						newNode.initialize();
						nodesInScope.add(newNode);
					}
					symbolTrees.put(currentScope, nodesInScope);
				}
				else if (lookup(nodeToAnalyse) == null)
					push(nodeToAnalyse);
				
			}
			
			// Repeate process
			fillSymbols(nodeToAnalyse, scope);
		}
	}

	// Semantically analise operations
	public void analyseOperation(SimpleNode node) {
	
		SimpleNode leftChild = (SimpleNode) node.jjtGetChild(0);
		SimpleNode rightChild = (SimpleNode) node.jjtGetChild(1);
		
		// In case RHS has some hidden operations
		if (rightChild.getType() == Utils.OP || (rightChild.getType() == Utils.RHS &&
			rightChild.jjtGetNumChildren() > 1)) { 
			String rightType = recursiveOperationAnalysis(rightChild);

			if (rightType == Utils.WAS_CALLED) {
				calls.add(node);
				return;
			}
			else if (rightType == null) {
				// Error was detected in rhs, error was already reported
				return;
			} else if (leftChild.getType() != rightType) {
				// Incompatibility between lhs and rhs
				System.out.println("Semantical Error using variable " + leftChild.getValue() +".");
				System.out.println("Attempted operation with variables of type " + rightType +".");
			} else {
				// No problem was detected, adding to symbol table
				push(leftChild);
			}

		} else if (rightChild.getType() == Utils.CALL) {
			// Is a call, cannot analyse here
			calls.add(rightChild);
			
		} else {
			// Direct check between two operatives
			analyseTwoNodesOperation(leftChild, rightChild);
		}		
	}


	private String recursiveOperationAnalysis(SimpleNode rightChild) {
		SimpleNode leftNode = (SimpleNode) rightChild.jjtGetChild(0);

		System.out.println("LeftNode " + leftNode + " type " + leftNode.getType());

		// Operation-ception, or rhs with terms within
		if (leftNode.getType() == Utils.OP) { 
			return recursiveOperationAnalysis(leftNode);
		} else { // Actually two different nodes, may be scalar or not
			if (leftNode.getType() == Utils.CALL) {
				// Call in the middle of operations, cannot analyse here
				
				return Utils.WAS_CALLED;
			} else {
				// If type is term, inside it may contain scalar or array
				if (leftNode.getType() == Utils.TERM) 
					leftNode = (SimpleNode) leftNode.jjtGetChild(0);
				
				SimpleNode rightNode = (SimpleNode) rightChild.jjtGetChild(1);
				if (rightNode.getType() == Utils.TERM)
					rightNode = (SimpleNode) rightNode.jjtGetChild(0);

				// Are scalar or array, start analysing here
				return analyseTwoNodesOperation(leftNode, rightNode);
			}
		} 

	}

	private String analyseTwoNodesOperation(SimpleNode leftChild, SimpleNode rightChild) {
		String leftType = leftChild.getType();
		String rightType = rightChild.getType();

		SimpleNode previousRightNode = lookup(rightChild);
		SimpleNode previousLeftNode = lookup(leftChild);

		if (rightType == Utils.RHS)

		// Is comparing against a number
		if (rightType == Utils.NUMBER) { 
			// Isn't array
			if (leftType != Utils.ARRAY) {
				leftChild.initialize();
				leftChild.setType(Utils.SCALAR);
				return Utils.SCALAR;
			} else {
				System.out.println("Semantic Error: Trying to do operation between number and " 
					+ leftChild.getValue() + ".");
				return null;
			}
		}
		
		// Right Hand Side variable was not initialized, semantic error
		if ((previousRightNode == null || !previousRightNode.isInitialized()) 
			&& rightType != Utils.NUMBER) { 
			
			System.out.println("Variable " + rightChild.getValue() + " was not initialized. Dumping:");
			Utils.dumpType("", rightChild);
			return null;
		}

		if (previousLeftNode != null) { // Was already declared
			// If they are scalars or arrays
			if ((leftType.equals(Utils.SCALAR) || leftType.equals(Utils.ARRAY)) && 
				(rightType.equals(Utils.SCALAR) || rightType.equals(Utils.ARRAY))) {
				if (!leftType.equals(rightType)) {
					System.out.println("Semantic Error: Left Hand Side Value " + leftChild.getValue()
							+ " and Right Hand Side Value " + rightChild.getValue());
					return null;
				}
			}
		} else { // Was not present, new initialization
			leftChild.setType(rightChild.getType());		
			leftChild.initialize();
			push(leftChild);
		}


		return leftChild.getType();
	}

	public void analyseCalls() {
		// Only called after symbol table is complete

		System.out.println("Calls " + calls.toString());
		System.out.println("Declarations " + declarations.toString());
		System.out.println("Symbols " + symbolTrees.toString());

		for (int i = 0; i < calls.size() ; i++) {

		}
	}
}
