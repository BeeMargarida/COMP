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
		SimpleNode previousNode;

		// Is outside all functions, is a declaration
		if (currentScope == "") {
			previousNode = Utils.containsValue(declarations, nodeToAdd);
			if (previousNode != null) {
				if (!previousNode.getType().equals(nodeToAdd.getType())) {
					System.out.println("Semantic Error : Previous declaration of the variable "
							+ previousNode.getValue() + " with a different type was found.");
				}

			} else {
				declarations.add(nodeToAdd);

			}
		}
		// Check if there is no element associated to current scope
		else {
			// There were already elements in scope, check if element was aready declared
			ArrayList<SimpleNode> scopeTree = symbolTrees.get(currentScope);
			previousNode = Utils.containsValue(scopeTree, nodeToAdd);
			if (previousNode != null) {
				if (!previousNode.getType().equals(nodeToAdd.getType())) {
					System.out.println("Semantic Error : Previous statement with the variable "
							+ previousNode.getValue() + " with a different type was found.");
				}
			} else {
				scopeTree.add(nodeToAdd);
				symbolTrees.put(currentScope, scopeTree);
			}
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

			// If it is a conditional, analyse everything inside
			if (nodeToAnalyse.getType() == Utils.COND) {
				analyseConditional(nodeToAnalyse);
			} else if (nodeToAnalyse.getType() == Utils.OP) { // Is operation
				SimpleNode newNode = analyseOperation(nodeToAnalyse);

				// If it is something worth adding, add it
				if (newNode != null)
					push(newNode);
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
						System.out.println("Semantic Error : " + "There are more than one function with name "
								+ nodeToAnalyse.getValue() + ".");
					} else
						functions.add(nodeToAnalyse);

				}

				// If it is an operation, do a semantic analysis
				

				// Encountered a call, will analyse later				
				else if (nodeToAnalyse.getType() == Utils.CALL) {
					addToCalls(nodeToAnalyse);
				}

				// If it is the function argument list, need to store that for check
				else if (nodeToAnalyse.getType() == Utils.VARLIST) {
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

		
		/*
				System.out.println("LeftNode is " + leftChild + " value " + leftChild.getValue());
				System.out.println("RightNode is " + rightChild + " value " + rightChild.getValue());
				System.out.println("Children are " + rightChild.jjtGetChild(0) + 
					" type " + ((SimpleNode) rightChild.jjtGetChild(0)).getType()); 
		*/

		// SIZE semantic check
		if (leftChild.getType().equals(Utils.SIZE)) {
			hasErrors = true;
			System.out.println("Semantic Error : Improper use of '.size' with variable " + leftChild.getValue());
		}

		// Check if there are any hidden calls
		if (Utils.checkFor(Utils.CALL, leftChild) || Utils.checkFor(Utils.CALL, rightChild)) {
			addToCalls(node);
			return null;
		}

		// Check for array instantiations
		if (((SimpleNode) rightChild.jjtGetChild(0)).getType().equals(Utils.ARRAY_INST)) {
			if (leftChild.isInitialized() == Utils.NOT_INIT) {
				leftChild.setType(Utils.ARRAY);
				leftChild.setInitialization(Utils.DEFIN_INIT);
				return leftChild;
			}
		}

		// Check for array instantiations, with a scalar for size
		else if (((SimpleNode) rightChild.jjtGetChild(0)).getType().equals(Utils.ARRAY_INST_SCALAR)) {

			// Do lookup of scalar value
			if (leftChild.isInitialized() == Utils.NOT_INIT
					&& Utils.containsValueString(symbolTrees.get(currentScope), leftChild.getValue()) != null) {
				leftChild.setType(Utils.ARRAY);
				leftChild.setInitialization(Utils.DEFIN_INIT);
				push(leftChild);
				return leftChild;
			}
		}

		// In case RHS has some hidden operations or calls
		if (rightChild.getType() == Utils.OP
				|| (rightChild.getType() == Utils.RHS && rightChild.jjtGetNumChildren() > 1)) {

			SimpleNode rightRecursive = recursiveOperationAnalysis(rightChild);

			// Error was detected in rhs, was already reported
			if (rightRecursive == null) {
				return null;
			} 
			
			else 
				return analyseTwoNodesOperation(leftChild, rightRecursive, false, rightChild);
		}
		// Direct check between two operatives
		else 
			return analyseTwoNodesOperation(leftChild, rightChild, false, null);

	}

	/**
	 * Recursive operations, in case of nested ops.
	 */
	private SimpleNode recursiveOperationAnalysis(SimpleNode rightChild) {
		SimpleNode leftNode = (SimpleNode) rightChild.jjtGetChild(0);

		// More operations in leftNode
		if (leftNode.getType() == Utils.OP) {
			return recursiveOperationAnalysis(leftNode);
		} else { 
			// Two different singular nodes, may be scalar or not
			// If type is term, inside it may contain scalar or array

			if (leftNode.getType() == Utils.TERM)
				leftNode = (SimpleNode) leftNode.jjtGetChild(0);

			SimpleNode rightNode = (SimpleNode) rightChild.jjtGetChild(1);

			if (rightNode.getType() == Utils.TERM)
				rightNode = (SimpleNode) rightNode.jjtGetChild(0);

			// Check with possible previous instantiantions in symbol table
			SimpleNode previousLeftNode = Utils.containsValue(symbolTrees.get(currentScope), leftNode);
			SimpleNode previousRightNode = Utils.containsValue(symbolTrees.get(currentScope), rightNode);

			if (previousLeftNode != null)
				leftNode = previousLeftNode;

			if (previousRightNode != null)
				rightNode = previousRightNode;

			
			// Are scalar or array, start analysing here
			return analyseTwoNodesOperation(leftNode, rightNode, true, rightChild);
		}

	}

	// Needs to be initialized is when the two variables are on rhs, they both need to be initialized
	private SimpleNode analyseTwoNodesOperation(SimpleNode leftChild, SimpleNode rightChild,
			boolean needToBeInitialized, SimpleNode operation) {

		System.out.println("Operation is " + operation.getValue());
		String leftType = leftChild.getType();
		String rightType = rightChild.getType();

		// If RightType is RHS or term
		if (rightType == Utils.RHS) {
			rightChild = (SimpleNode) rightChild.jjtGetChild(0);
			rightType = rightChild.getType();
		}
		else if (rightType == Utils.TERM) {
			rightChild = (SimpleNode) rightChild.jjtGetChild(0);
			rightType = rightChild.getType();
		}

		// Check possible previous instantiations in symbol table
		SimpleNode previousRightNode = Utils.containsValue(symbolTrees.get(currentScope), rightChild);
		SimpleNode previousLeftNode = Utils.containsValue(symbolTrees.get(currentScope), leftChild);

		if (previousRightNode != null) {
			rightChild = previousRightNode;
			rightType = previousRightNode.getType();
		}
		if (previousLeftNode != null) {
			leftChild = previousLeftNode;
			leftType = previousLeftNode.getType();
		}

		// Is comparing against a number
		if (rightType == Utils.NUMBER) {
			// Isn't array
			if (leftType != Utils.ARRAY) {
				leftChild.setInitialization(Utils.DEFIN_INIT);
				leftChild.setType(Utils.SCALAR);
				return leftChild;
			} // Is definitely Array
			else if (leftChild.isInitialized() == Utils.DEFIN_INIT && leftType == Utils.ARRAY) {
				System.out.println("Semantic Error : Invalid comparison between array and number " + 
					"with variable " + leftChild.getValue());
				hasErrors = true;
				return null;
			}
		}

		// Right Hand Side variable was not initialized, semantic error
		if (rightChild.isInitialized() == Utils.NOT_INIT && rightChild.getType() != Utils.NUMBER) {
			hasErrors = true;
			System.out.println("Semantic Error : Variable " + rightChild.getValue() + " was not initialized.");
			return null;
		} // Right Hand Side may not be initialized, semantic warning
		else if (rightChild.isInitialized() == Utils.MAYBE_INIT && rightType != Utils.NUMBER) {
			System.out.println("Semantic Warning : Variable " + rightChild.getValue() + " may not be initialized.");
		} // Right Hand Side has had incompatible initializations, and is trying to be accessed
		 else if (rightChild.isInitialized() == Utils.INCOMPAT_INIT && rightType != Utils.NUMBER) {
			System.out.println("Semantic Error : Variable " + rightChild.getValue()
					+ " has incompatible previous declaration" + " in previous 'if' structure.");
			hasErrors = true;
			return null;
		}

		// Left node needs to be initialized (in case of it being on the rhs of some op)
		if (leftChild.isInitialized() == Utils.NOT_INIT && needToBeInitialized) {
			hasErrors = true;
			System.out.println("Semantic Error : Variable " + leftChild.getValue() + " was not initialized.");
			return null;
		} else if (leftChild.isInitialized() == Utils.MAYBE_INIT && needToBeInitialized) {
			System.out.println("Semantic Warning : Variable " + leftChild.getValue() + " was maybe not initialized.");
			return leftChild;
		} else if (leftChild.isInitialized() == Utils.INCOMPAT_INIT && needToBeInitialized) {
			System.out.println(
					"Semantic Warning : Variable " + leftChild.getValue() + " could be either scalar or array.");
			return leftChild;
		} // Was already declared
		else if (leftChild != null) {
			// If they are scalars or arrays
			if ((leftType.equals(Utils.SCALAR) || leftType.equals(Utils.ARRAY))
					&& (rightType.equals(Utils.SCALAR) || rightType.equals(Utils.ARRAY))) {
				if (!leftType.equals(rightType)) {
					hasErrors = true;
					System.out.println("Semantic Error: Incompatible operation between "
					 + "left Hand Side Value " + leftChild.getValue() + " and Right Hand Side Value " + rightChild.getValue());
					return null;
				}
				else { // Was not present, new initialization
					leftChild.setType(rightChild.getType());
					leftChild.setInitialization(Utils.DEFIN_INIT);
					return leftChild;
				}
			}
		}

		return leftChild;
	}

	/**
	 * Analyse conditional structures
	 */
	public void analyseConditional(SimpleNode nodeToAnalyse) {
		// Entered in if statement
		ArrayList<SimpleNode> nodesScope = new ArrayList<>();

		SimpleNode elseNode = null;

		// Check ExprTest variables 

		// Analyse the rest of the if
		for (int i = 1; i < nodeToAnalyse.jjtGetNumChildren(); i++) {
			SimpleNode child = (SimpleNode) nodeToAnalyse.jjtGetChild(i);
			if (child.getType().equals(Utils.OP)) {
				System.out.println("analyse conditional if");
				SimpleNode resultNode = analyseOperation(child);

				resultNode.setInitialization(Utils.MAYBE_INIT);

				SimpleNode previousNode = Utils.contains(nodesScope, resultNode);

				if (previousNode == null) {
					previousNode = lookup(resultNode);
				}

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
					System.out.println("conditionals");
					SimpleNode resultNode = analyseOperation(child);

					SimpleNode previousNode = Utils.containsValue(nodesScope, resultNode);

					// Was nowhere to be found
					if (previousNode == null && lookup(resultNode) == null) {
						if (resultNode != null) {
							resultNode.setInitialization(Utils.MAYBE_INIT);
							nodesScope.add(resultNode);
						}
					}

					//System.out.println("Previous node in else is " + previousNode.getValue());

					// Was already the same node in scope
					if (previousNode != null) {

						// Was not the same type as previous declaration
						if (!previousNode.getType().equals(resultNode.getType())) {
							nodesScope.remove(previousNode);
							previousNode.setInitialization(Utils.INCOMPAT_INIT);
							nodesScope.add(previousNode);
						} else {
							nodesScope.remove(previousNode);
							previousNode.setInitialization(Utils.DEFIN_INIT);
							nodesScope.add(previousNode);
						}

					}

				} else {
					push(child);
				}
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
