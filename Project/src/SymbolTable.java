import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

	private String currentScope;
	private boolean hasErrors;

	// HashMap<ScopeString, SymbolTree>
	private HashMap<String, ArrayList<SimpleNode>> symbolTrees;
	private ArrayList<SimpleNode> declarations;
	private ArrayList<SimpleNode> functions;
	private HashMap<String, ArrayList<SimpleNode>> calls;

	public SymbolTable() {
		symbolTrees = new HashMap<>();
		declarations = new ArrayList<>();
		functions = new ArrayList<>();
		calls = new HashMap<>();

		currentScope = "";
		hasErrors = false;
	}

	public SimpleNode lookup(SimpleNode node) {
		if (currentScope == "") // Is outside all functions
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

	private void addToCalls(SimpleNode nodeToAdd) {
		ArrayList<SimpleNode> nodesInScope = calls.get(currentScope);

		// Not initialized
		if (nodesInScope == null) {
			nodesInScope = new ArrayList<>();
		}

		nodesInScope.add(nodeToAdd);

		calls.put(currentScope, nodesInScope);
	}

	/**
	 * Adds all symbols to array.
	 */
	public void fillSymbols(SimpleNode node, String scope) {
		currentScope = scope;

		// Goes through all the children
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {

			SimpleNode nodeToAnalyse = (SimpleNode) node.jjtGetChild(i);

			if (nodeToAnalyse.getType() == Utils.COND) {
				analyseConditional(nodeToAnalyse);
			} else {
				// Inside a new function, belongs to another scope
				if (nodeToAnalyse.getType() == Utils.FUNCTION) {
					scope = nodeToAnalyse.getValue();

					// Create new symbol tree for this scope
					ArrayList<SimpleNode> newNodesInScope = new ArrayList<>();
					symbolTrees.put(scope, newNodesInScope);

					// Function Name was already encountered before
					if (Utils.contains(functions, nodeToAnalyse) != null) {
						System.out.println(
								"There are more than one function with name " + nodeToAnalyse.getValue() + ".");
					} else
						functions.add(nodeToAnalyse);

				}
				// If it is an operation, do a semantic analysis
				else if (nodeToAnalyse.getType() == Utils.OP) { // Is operation
					SimpleNode newNode = analyseOperation(nodeToAnalyse);

					if (newNode != null)
						push(newNode);

				}

				else if (nodeToAnalyse.getType() == Utils.CALL) {
					// Encountered a call, will analyse later
					addToCalls(nodeToAnalyse);
				} else if (nodeToAnalyse.getType() == Utils.VARLIST) {
					ArrayList<SimpleNode> nodesInScope = symbolTrees.get(currentScope);

					for (int j = 0; j < nodeToAnalyse.jjtGetNumChildren(); j++) {
						SimpleNode newNode = (SimpleNode) nodeToAnalyse.jjtGetChild(j);
						newNode.setInitialization(Utils.DEFIN_INIT);
						nodesInScope.add(newNode);
					}
					symbolTrees.put(currentScope, nodesInScope);
				}

				// Repeate process
				fillSymbols(nodeToAnalyse, scope);
			}
		}
	}

	// Semantically analise operations
	public SimpleNode analyseOperation(SimpleNode node) {
		SimpleNode leftChild = (SimpleNode) node.jjtGetChild(0);
		SimpleNode rightChild = (SimpleNode) node.jjtGetChild(1);

		/*System.out.println("MainNode is " + node + " value " + node.getValue());
		System.out.println("LeftNode is " + leftChild + " value " + leftChild.getValue());
		System.out.println("RightNode is " + rightChild + " value " + rightChild.getValue());
		System.out.println("Children are " + rightChild.jjtGetChild(0)); */

		// Check if there are any hidden calls
		if (Utils.checkFor(Utils.CALL, leftChild) || Utils.checkFor(Utils.CALL, rightChild)) {
			addToCalls(node);
			return null;
		}

		// In case RHS has some hidden operations or calls
		if (rightChild.getType() == Utils.OP
				|| (rightChild.getType() == Utils.RHS && rightChild.jjtGetNumChildren() > 1)) {

			SimpleNode rightRecursive = recursiveOperationAnalysis(rightChild);

			// Error was detected in rhs, error was already reported
			if (rightRecursive == null) {
				return null;
			} // Incompatibility between lhs and rhs
			else if (leftChild.getType() != rightRecursive.getType()) {
				System.out.println("Semantical Error using variable " + leftChild.getValue() + ".");
				System.out.println("Attempted operation with variables of type " + rightRecursive.getType() + ".");
				hasErrors = true;
				return null;
			} // No problem was detected, adding to symbol table
			else {
				return leftChild;
			}

		}
		// Direct check between two operatives
		else {
			return analyseTwoNodesOperation(leftChild, rightChild, false);

		}

	}

	private SimpleNode recursiveOperationAnalysis(SimpleNode rightChild) {
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
	private SimpleNode analyseTwoNodesOperation(SimpleNode leftChild, SimpleNode rightChild,
			boolean needToBeInitialized) {

		String leftType = leftChild.getType();
		String rightType = rightChild.getType();

		// If RightType is RHS
		if (rightType == Utils.RHS) {
			rightChild = (SimpleNode) rightChild.jjtGetChild(0);
			rightType = rightChild.getType();
		}

		if (rightType == Utils.TERM) {
			rightChild = (SimpleNode) rightChild.jjtGetChild(0);
			rightType = rightChild.getType();
		}

		SimpleNode previousRightNode = lookup(rightChild);
		SimpleNode previousLeftNode = lookup(leftChild);

		// Is comparing against a number
		if (rightType == Utils.NUMBER) {
			// Isn't array
			if (leftType != Utils.ARRAY) {
				leftChild.setInitialization(Utils.DEFIN_INIT);
				leftChild.setType(Utils.SCALAR);
				return leftChild;
			} else {
				System.out.println(
						"Semantic Error : Trying to do operation between number and " + leftChild.getValue() + ".");
				hasErrors = true;
				return null;
			}
		}

		/*
		if (previousLeftNode != null)
			System.out.println("Previous left " + previousLeftNode.getValue() + " init " + previousLeftNode.isInitialized());
		if (previousRightNode != null)
			System.out.println(" previous right " + previousRightNode.getValue() + " init " + previousRightNode.isInitialized());
		*/

		// Right Hand Side variable was not initialized, semantic error
		if ((previousRightNode == null || previousRightNode.isInitialized() == Utils.NOT_INIT)
				&& rightType != Utils.NUMBER) {
			hasErrors = true;
			System.out.println("Semantic Error : Variable " + rightChild.getValue() + " was not initialized.");
			return null;
		}
		if ((previousRightNode == null || previousRightNode.isInitialized() == Utils.MAYBE_INIT)
				&& rightType != Utils.NUMBER) {
			System.out.println("Semantic Warning : Variable " + rightChild.getValue() + " may not not initialized.");
			return null;
		}

		// Left node needs to be initialized
		if ((previousLeftNode == null || previousLeftNode.isInitialized() == Utils.NOT_INIT) && needToBeInitialized) {

			hasErrors = true;
			System.out.println("Semantic Error : Variable " + leftChild.getValue() + " was not initialized.");
			return null;
		} else if ((previousLeftNode == null || previousLeftNode.isInitialized() == Utils.MAYBE_INIT)
				&& needToBeInitialized) {
			System.out.println("Semantic Warning : Variable " + leftChild.getValue() + " was maybe not initialized.");
			return leftChild;
		} else if ((previousLeftNode == null || previousLeftNode.isInitialized() == Utils.INCOMPAT_INIT)
				&& needToBeInitialized) {
			System.out.println(
					"Semantic Warning : Variable " + leftChild.getValue() + " could be either scalar or array.");
			return leftChild;
		} // Was already declared
		else if (previousLeftNode != null) {
			// If they are scalars or arrays
			if ((leftType.equals(Utils.SCALAR) || leftType.equals(Utils.ARRAY))
					&& (rightType.equals(Utils.SCALAR) || rightType.equals(Utils.ARRAY))) {
				if (!leftType.equals(rightType)) {
					hasErrors = true;
					System.out.println("Semantic Error: Left Hand Side Value " + leftChild.getValue()
							+ " and Right Hand Side Value " + rightChild.getValue());
					return null;
				}
			}
		} else { // Was not present, new initialization
			leftChild.setType(rightChild.getType());
			leftChild.setInitialization(Utils.DEFIN_INIT);
			return leftChild;
		}

		return leftChild;
	}

	public void analyseConditional(SimpleNode nodeToAnalyse) {
		// Entered in if statement
		ArrayList<SimpleNode> nodesScope = new ArrayList<>();

		SimpleNode elseNode = null;

		// Check ExprTest variables 

		// Analyse the rest of the if
		for (int i = 1; i < nodeToAnalyse.jjtGetNumChildren(); i++) {
			SimpleNode child = (SimpleNode) nodeToAnalyse.jjtGetChild(i);
			if (child.getType().equals(Utils.OP)) {
				SimpleNode resultNode = analyseOperation(child);

				resultNode.setInitialization(Utils.MAYBE_INIT);

				SimpleNode previousNode = Utils.contains(nodesScope, resultNode);
				// Was already the same node in scope
				if (previousNode != null) {
					// Was not the same type as previous declaration
					if (!previousNode.getType().equals(resultNode.getType())) {
						System.out.println("Semantic Error: Incompatible previous declaration of variable "
								+ resultNode.getValue() + " was found.");
						hasErrors = true;
					} else {
						nodesScope.add(resultNode);
					}

				} else {
					nodesScope.add(resultNode);
				}
			} else if (child.getType().equals(Utils.ELSE))
				elseNode = child;
			else if (child.getType().equals(Utils.CALL)) {
				child.setInitialization(Utils.MAYBE_INIT);
				nodesScope.add(child);
			}
		}

		// Has else, needs to compare to previous statements
		if (elseNode != null) {

			// Check if children have previous
			for (int i = 0; i < elseNode.jjtGetNumChildren(); i++) {
				SimpleNode child = (SimpleNode) elseNode.jjtGetChild(i);
				
				if (child.getType().equals(Utils.OP)) {
					SimpleNode resultNode = analyseOperation(child);
					
					SimpleNode previousNode = Utils.containsValue(nodesScope, resultNode);

					// Was already the same node in scope
					if (previousNode != null) {
						// Was not the same type as previous declaration
						if (!previousNode.getType().equals(resultNode.getType())) {
							System.out.println("Semantic Error: Incompatible previous declaration of variable "
									+ resultNode.getValue() + " was found.");
							hasErrors = true;
						} else {
							nodesScope.remove(previousNode);
							previousNode.setInitialization(Utils.DEFIN_INIT);
							nodesScope.add(previousNode);
						}

					} else {
						resultNode.setInitialization(Utils.MAYBE_INIT);
						nodesScope.add(resultNode);
					}
				} else if (child.getType().equals(Utils.ELSE))
					elseNode = child;
				else
					push(child);
			}
		}

		ArrayList<SimpleNode> mergedNodesInScope = Utils.mergeArrays(symbolTrees.get(currentScope), nodesScope);

		/*for (int i = 0 ; i < mergedNodesInScope.size() ; i++) {
			System.out.println("Final node " + mergedNodesInScope.get(i) + " is init " + mergedNodesInScope.get(i).isInitialized());
		} */

		symbolTrees.replace(currentScope, mergedNodesInScope);
	}

	public void analyseCalls() {
		//System.out.println("All symbols, check number " + symbolTrees);

		// Only called after symbol table is complete
		Map<String, ArrayList<SimpleNode>> map = calls;

		for (Map.Entry<String, ArrayList<SimpleNode>> entry : map.entrySet()) {
			System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());

			// Calls include assigns and operations, to check if functions' return value 
			//is of the same type
			for (int i = 0; i < entry.getValue().size(); i++) {

				// Extract the real call hidden within possible assigns and operations
				SimpleNode callToBeAnalysed = Utils.extractOfType(Utils.CALL, entry.getValue().get(i));
				SimpleNode function = Utils.containsValue(functions, callToBeAnalysed);

				if (function == null) {
					System.out.println(
							"Semantic Error : There was no function associated named " + callToBeAnalysed.getValue());
					hasErrors = true;
				} else {
					// Call is correct, checking types
					if (entry.getValue().get(i).getType().equals(Utils.OP)) {
						SimpleNode leftNode = Utils.extractOfType(Utils.SCALAR,
								(SimpleNode) entry.getValue().get(i).jjtGetChild(0));

						if (leftNode == null)
							leftNode = Utils.extractOfType(Utils.ARRAY,
									(SimpleNode) entry.getValue().get(i).jjtGetChild(0));

						if (!((ASTFunction) function).getReturnType().equals(leftNode.getType())) {
							hasErrors = true;
							System.out.println("Semantic Error : Mismatching types between " + leftNode.getValue()
									+ " and " + function.getValue() + " -> " + leftNode.getType() + " opposed to "
									+ ((ASTFunction) function).getReturnType());
						} else {
							ArrayList<SimpleNode> nodesInScope = symbolTrees.get(entry.getKey());
							nodesInScope.add(leftNode);
							symbolTrees.put(entry.getKey(), nodesInScope);
						}
					}

					// Checking argslist to see if the size and types are correct
					if (callToBeAnalysed.jjtGetChild(0).jjtGetNumChildren() != function.jjtGetChild(0)
							.jjtGetNumChildren()) {
						hasErrors = true;
						System.out.println("Semantic Error : Mismatching number of arguments in call "
								+ callToBeAnalysed.getValue() + " -> "
								+ callToBeAnalysed.jjtGetChild(0).jjtGetNumChildren() + " opposed to "
								+ function.jjtGetChild(0).jjtGetNumChildren());
					}

				}

			}

			for (int i = 0; i < functions.size(); i++) {
				System.out.println("Function " + functions.get(i).getValue());
			}
		}
	}

	public ASTFunction getFunction(String functionName) {
		for (int i = 0; i < functions.size(); i++) {
			ASTFunction function = (ASTFunction) functions.get(i);
			if (function.functionName.equals(functionName)) {
				return function;
			}
		}
		return null;
	}

	public HashMap<String, ArrayList<SimpleNode>> getSymbolTrees() {
		return symbolTrees;
	}

	public boolean hasErrors() {
		return hasErrors;
	}
}
