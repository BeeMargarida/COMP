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
			if (nodeToAnalyse.getType().equals(Utils.OP)) { // Is operation
				analyseOperation(nodeToAnalyse);

				// No need to continue checking children, already analized
				return;
			} else if (nodeToAnalyse.getType().equals(Utils.COND)) {
				analyseConditional(nodeToAnalyse);
				
				// No need to continue checking children, already analized
				return;
			}
			else {
				
				// Inside a new function, belongs to another scope
				if (nodeToAnalyse.getType() == Utils.FUNCTION) {
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
						newNode.setInitialization(Utils.DEFIN_INIT);
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

		// Check if there are any hidden calls
		if (Utils.checkFor(Utils.CALL, leftChild) || Utils.checkFor(Utils.CALL, rightChild)) {
			calls.add(node);
			return;
		}
		
		// In case RHS has some hidden operations or calls
		if (rightChild.getType() == Utils.OP || (rightChild.getType() == Utils.RHS &&
			rightChild.jjtGetNumChildren() > 1)) { 

			String rightType = recursiveOperationAnalysis(rightChild);

			if (rightType == Utils.WAS_CALLED) {
				calls.add(node);
				return;
			} // Error was detected in rhs, error was already reported
			else if (rightType == null) {
				return;
			} // Incompatibility between lhs and rhs
			else if (leftChild.getType() != rightType) {
				System.out.println("Semantical Error using variable " + leftChild.getValue() +".");
				System.out.println("Attempted operation with variables of type " + rightType +".");
			} // No problem was detected, adding to symbol table
			else {				
				push(leftChild);
			}

		} 
		// Direct check between two operatives
		else 
			analyseTwoNodesOperation(leftChild, rightChild, false);
			
	}


	private String recursiveOperationAnalysis(SimpleNode rightChild) {
		SimpleNode leftNode = (SimpleNode) rightChild.jjtGetChild(0);


		// Operation-ception, or rhs with terms within
		if (leftNode.getType() == Utils.OP) { 
			return recursiveOperationAnalysis(leftNode);
		} else { // Actually two different nodes, may be scalar or not
			// If type is term, inside it may contain scalar or array
			if (leftNode.getType() == Utils.TERM) 
			leftNode = (SimpleNode) leftNode.jjtGetChild(0);
				
			SimpleNode rightNode = (SimpleNode) rightChild.jjtGetChild(1);
			if (rightNode.getType() == Utils.TERM)
			rightNode = (SimpleNode) rightNode.jjtGetChild(0);

			// Are scalar or array, start analysing here
			return analyseTwoNodesOperation(leftNode, rightNode, true);	
		} 

	}

	// Needs to be initialized is when the two variables are on rhs, they both need to be initialized
	private String analyseTwoNodesOperation(SimpleNode leftChild, SimpleNode rightChild, boolean needToBeInitialized) {
		String leftType = leftChild.getType();
		String rightType = rightChild.getType();

		SimpleNode previousRightNode = lookup(rightChild);
		SimpleNode previousLeftNode = lookup(leftChild);
		
		// Is comparing against a number
		if (rightType == Utils.NUMBER) { 
			// Isn't array
			if (leftType != Utils.ARRAY) {
				leftChild.setInitialization(Utils.DEFIN_INIT);
				leftChild.setType(Utils.SCALAR);
				return Utils.SCALAR;
			} else {
				System.out.println("Semantic Error: Trying to do operation between number and " 
					+ leftChild.getValue() + ".");
				return null;
			}
		}
		
		// Right Hand Side variable was not initialized, semantic error
		if ((previousRightNode == null || previousRightNode.isInitialized() == Utils.NOT_INIT) 
			&& rightType != Utils.NUMBER) { 
			
			System.out.println("Variable " + rightChild.getValue() + " was not initialized.");
			return null;
		}

		// Left node needs to be initialized
		if ((previousLeftNode == null || previousLeftNode.isInitialized() == Utils.NOT_INIT) && needToBeInitialized) {
			System.out.println("Variable " + leftChild.getValue() + " was not initialized.");
			return null;
		} // Was already declared
		else if (previousLeftNode != null) { 
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
			leftChild.setInitialization(Utils.DEFIN_INIT);
			push(leftChild);
		}


		return leftChild.getType();
	}

	public void analyseConditional(SimpleNode nodeToAnalyse) {
		
	}

	public void analyseCalls() {
		// Only called after symbol table is complete

		// Calls include assigns and operations, to check if functions' return value is of the same type
		for (int i = 0; i < calls.size() ; i++) {

			// Extract the real call hidden within possible assigns and operations
			SimpleNode callToBeAnalysed = Utils.extractOfType(Utils.CALL, calls.get(i));
			SimpleNode function = Utils.containsValue(functions, callToBeAnalysed);

			if (function == null) {
				System.out.println("Semantic Error : There was no function associated named " + callToBeAnalysed.getValue());
			}
			else { 
				// Call is correct, checking types
				if (calls.get(i).getType().equals(Utils.OP)) {
					SimpleNode leftNode = Utils.extractOfType(Utils.SCALAR, (SimpleNode) calls.get(i).jjtGetChild(0));

					if (leftNode == null)
						leftNode = Utils.extractOfType(Utils.ARRAY, (SimpleNode) calls.get(i).jjtGetChild(0));
										
					if (!((ASTFunction) function).getReturnType().equals(leftNode.getType())) {
						System.out.println("Semantic error : Mismatching types between " + leftNode.getValue() + " and " + 
							function.getValue() + " -> " + leftNode.getType() + " opposed to " + ((ASTFunction) function).getReturnType());
					}
				}

				// Checking argslist to see if the size and types are correct
				if (callToBeAnalysed.jjtGetChild(0).jjtGetNumChildren() != function.jjtGetChild(0).jjtGetNumChildren()) {
					System.out.println("Semantic error : Mismatching number of arguments in call " + callToBeAnalysed.getValue() + 
						" -> " + callToBeAnalysed.jjtGetChild(0).jjtGetNumChildren() + " opposed to " +  function.jjtGetChild(1).jjtGetNumChildren());
				}
				
			}

			
		}

		for (int i = 0; i < functions.size() ; i++) {
			System.out.println("Function " + functions.get(i).getValue());
		}
	}
}
