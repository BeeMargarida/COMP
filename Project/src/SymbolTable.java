import java.util.ArrayList;
import java.util.HashMap;


public class SymbolTable {

	private String currentScope;
	private boolean hasErrors;

	// HashMap<ScopeString, SymbolTree>
	private HashMap<String, ArrayList<SimpleNode>> symbolTrees;
	private ArrayList<SimpleNode> declarations;
	private ArrayList<SimpleNode> functions;
	
	public SymbolTable() {
		symbolTrees = new HashMap<>();
		declarations = new ArrayList<>();
		functions = new ArrayList<>();
	
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

		System.out.println("Node to Add " + nodeToAdd.getType() + " value " + nodeToAdd.getValue() + " scope " + currentScope);

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

	public void fillSymbols(SimpleNode node, String scope) {
		currentScope = scope;

		// First passage to check functions
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			SimpleNode nodeToAnalyse = (SimpleNode) node.jjtGetChild(i);

			
			if (nodeToAnalyse.getType() == Utils.FUNCTION) {
				scope = nodeToAnalyse.getValue();
				
				// Create new symbol tree for this scope
				ArrayList<SimpleNode> newNodesInScope = new ArrayList<>();
				symbolTrees.put(scope, newNodesInScope);
				
				// Function Name was already encountered before
				if (Utils.contains(functions, nodeToAnalyse) != null) {
					System.out.println("Semantic Error : " + "There are more than one function with name "
						+ nodeToAnalyse.getValue() + ".");
				}
				else
					functions.add(nodeToAnalyse);	
				
			}
			else if (nodeToAnalyse.getType().equals(Utils.DECLARATION)) {
				if (nodeToAnalyse.jjtGetNumChildren() > 0) { // Has Array Children

					SimpleNode nodeToAdd = ((SimpleNode) nodeToAnalyse.jjtGetChild(0));
					declarations.add(nodeToAdd);
				} else { // Is scalar
					nodeToAnalyse.setType(Utils.SCALAR);
					declarations.add(nodeToAnalyse);
				}
			}
			
			System.out.println(functions);
		}

		for (int i = 0; i < functions.size(); i++) {
			currentScope = functions.get(i).getValue();

			boolean needsToBeInitializedAfterAnalysis = false;
					
			if (((ASTFunction) functions.get(i)).getReturnValue() != null) {
				SimpleNode returnNode = new SimpleNode(0);
				returnNode.setType(((ASTFunction) functions.get(i)).getReturnType());
				returnNode.jjtSetValue(((ASTFunction) functions.get(i)).getReturnValue());

				push(returnNode);				

				needsToBeInitializedAfterAnalysis = true;
			}
			
			analyseFunctions(functions.get(i));

			if (needsToBeInitializedAfterAnalysis) {
				String valueToAnalyse = ((ASTFunction) functions.get(i)).getReturnValue();
				SimpleNode check = Utils.containsValueString(symbolTrees.get(currentScope), valueToAnalyse);

				if (check != null) {
					if (check.isInitialized() != Utils.DEFIN_INIT) {
						System.out.println("Semantic Error : Variable " + valueToAnalyse + " needed to be initialized and wasn't.");
						hasErrors = true;
					}
				}
				else  {
					System.out.println("Semantic Error : Variable " + valueToAnalyse + " needed to be initialized and wasn't.");
					hasErrors = true;
				}
			}
			
		}

	}

	private void analyseFunctions(SimpleNode node) {
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			SimpleNode nodeToAnalyse = (SimpleNode) node.jjtGetChild(i);

			//System.out.println("Going through node " + nodeToAnalyse + " value " + nodeToAnalyse.getValue() + " type " + nodeToAnalyse.getType());
			
			// If it is a conditional, analyse everything inside
			if (nodeToAnalyse.getType() == Utils.COND) {
				analyseConditional(nodeToAnalyse);
			}
			else {
				if (nodeToAnalyse.getType() == Utils.CALL) {
					SimpleNode newNode = analyseCalls(nodeToAnalyse);
					if (newNode != null) {
						symbolTrees.get(currentScope).add(newNode);
					}
				}
				else if (nodeToAnalyse.getType() == Utils.OP) {
				   // Is operation
				   SimpleNode newNode = analyseOperation(nodeToAnalyse);
   
				   // If it is something worth adding, add it
				   if (newNode != null) {
					    if (newNode.getType().equals(Utils.ARRAY) || newNode.getType().equals(Utils.SCALAR))  {
					   		push(newNode);
					   }
				   }
			   } else {
				   // If it is the function argument list, need to store that for check
				   if (nodeToAnalyse.getType() == Utils.VARLIST) {
					     
					   for (int j = 0; j < nodeToAnalyse.jjtGetNumChildren(); j++) {
						   	SimpleNode newNode = (SimpleNode) nodeToAnalyse.jjtGetChild(j);

							
							newNode.setInitialization(Utils.DEFIN_INIT);
						   	push(newNode);
					   }
					   //symbolTrees.put(currentScope, nodesInScope);
				   }
   
				   // Repeate process
			   }
			   
			   analyseFunctions(nodeToAnalyse);
			}
		}
	}

	// Semantically analise operations
	public SimpleNode analyseOperation(SimpleNode node) {
		System.out.println("Analysing operation node " + node.getValue());
		SimpleNode leftChild = (SimpleNode) node.jjtGetChild(0);
		SimpleNode rightChild = (SimpleNode) node.jjtGetChild(1);

		SimpleNode previousLeftChild = Utils.containsValue(symbolTrees.get(currentScope), leftChild);
		SimpleNode previousRightChild = Utils.containsValue(symbolTrees.get(currentScope), rightChild);

		if (previousLeftChild != null)
			leftChild = previousLeftChild;
		if (previousRightChild != null)
			rightChild = previousRightChild;

		// .size semantic check
		if (leftChild.getType().equals(Utils.SIZE)) {
			hasErrors = true;
			System.out.println("Semantic Error : Improper use of '.size' with variable " + leftChild.getValue());
		}

		// Check if there are any hidden calls
		if (Utils.checkFor(Utils.CALL, leftChild) || Utils.checkFor(Utils.CALL, rightChild)) {
			SimpleNode callNode = analyseCalls(node);

			return callNode;
		}

		// Check for array instantiations
		if (((SimpleNode) rightChild.jjtGetChild(0)).getType().equals(Utils.ARRAY_INST)) {
			if (leftChild.getType().equals(Utils.ARRAY)){
				leftChild.setInitialization(Utils.DEFIN_INIT);
				return leftChild;
			} else if (leftChild.isInitialized() == Utils.NOT_INIT) {
				leftChild.setType(Utils.ARRAY);
				leftChild.setInitialization(Utils.DEFIN_INIT);
				return leftChild;
			} else {
				System.out.println("Semantic Error : Attempted to instantiate scalar variable " + 
					leftChild.getValue());
					hasErrors = true;
					return null;
			}
		}

		// Check for array instantiations, with a scalar for size
		else if (((SimpleNode) rightChild.jjtGetChild(0)).getType().equals(Utils.ARRAY_INST_SCALAR)) {
			// Do lookup of scalar value
			if (leftChild.isInitialized() == Utils.NOT_INIT
					&& Utils.containsValueString(symbolTrees.get(currentScope), leftChild.getValue()) != null) {
				leftChild.setType(Utils.ARRAY);
				leftChild.setInitialization(Utils.DEFIN_INIT);
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

			else {
				return analyseTwoNodesOperation(leftChild, rightRecursive, false, node);
			}
		}
		// Direct check between two operatives
		else {
			return analyseTwoNodesOperation(leftChild, rightChild, false, null);

		}

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

	/**
	 * Compares between two nodes. Needs to know the operation and whether the left
	 * needs to be initialized for semantic checks.
	 */
	private SimpleNode analyseTwoNodesOperation(SimpleNode leftChild, SimpleNode rightChild,
			boolean needToBeInitialized, SimpleNode operation) {

		String leftType = leftChild.getType();
		String rightType = rightChild.getType();

		// If RightType is RHS or term
		if (rightType == Utils.RHS) {
			rightChild = (SimpleNode) rightChild.jjtGetChild(0);
			rightType = rightChild.getType();
		} 
		if (rightType == Utils.TERM) {
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
		
		if (operation != null) {
			System.out.println("OPERATION " + operation.getValue());
			// In case of '<' or '>' comparison between arrays
			if ((leftType == Utils.ARRAY || rightType == Utils.ARRAY)
					&& (operation.getValue().equals("<") || operation.getValue().equals(">"))) {
				hasErrors = true;
				System.out.println("Semantic Error : Imcompatible operation ('" + operation.getValue()
						+ "') between two arrays, " + leftChild.getValue() + " and " + rightChild.getValue() + ".");
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
		} // Right Hand Side has had incompatible initializations, and is trying to be
			// accessed
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
		} else if (leftChild.isInitialized() == Utils.MAYBE_INIT) {
			System.out.println("Semantic Warning : Variable " + leftChild.getValue() + " was maybe not initialized.");
			return leftChild;
		} else if (leftChild.isInitialized() == Utils.INCOMPAT_INIT) {
			System.out.println(
					"Semantic Warning : Variable " + leftChild.getValue() + " could be either scalar or array.");
			return leftChild;
		} 
		// Was already declared
		else if (leftChild != null) {
			// If they are scalars or arrays
			if ((leftType.equals(Utils.SCALAR) || leftType.equals(Utils.ARRAY))
					&& (rightType.equals(Utils.SCALAR) || rightType.equals(Utils.ARRAY))) {
				if (!leftType.equals(rightType)) {
					hasErrors = true;
					System.out.println("Semantic Error: Incompatible operation between " + "left Hand Side Value "
							+ leftChild.getValue() + " and Right Hand Side Value " + rightChild.getValue());
					return null;
				} else { // Was not present, new initialization
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
		// Nodes in new scope
		ArrayList<SimpleNode> nodesScope = new ArrayList<>();
		
		SimpleNode elseNode = null;
		
		// Analyse ExprTest
		SimpleNode exprTest = ((SimpleNode) nodeToAnalyse.jjtGetChild(0));
		
		boolean isValid = analyseExprTest(exprTest);

		if (!isValid) 
			return;

		// Analyse all the nodes inside the if
		for (int i = 1; i < nodeToAnalyse.jjtGetNumChildren(); i++) {
			
			SimpleNode child = (SimpleNode) nodeToAnalyse.jjtGetChild(i);

			// If there are operations inside 'if'
			if (child.getType().equals(Utils.OP)) {
				SimpleNode resultNode = analyseOperation(child);
				
				System.out.println("ResultNode of if " + resultNode.getValue() + " type " + resultNode.getType());
				// Check previous instanciations of resultNode
				SimpleNode previousNode = Utils.containsValue(nodesScope, resultNode);
				
				if (previousNode == null) {
					previousNode = lookup(resultNode);
				}

				// Had a node in scope
				if (previousNode != null) {

					// Was not the same type as previous declaration
					if (!previousNode.getType().equals(resultNode.getType())) {
						System.out.println("Semantic Error: Incompatible previous declaration of variable "
								+ resultNode.getValue() + " was found.");
						hasErrors = true;
					} else {
						nodesScope.add(resultNode);
					}

				} else { // Had no node in scope, can add safely
					nodesScope.add(resultNode);
				}
			} // Had else, need to analyse that next
			else if (child.getType().equals(Utils.ELSE))
				elseNode = child;
			else if (child.getType().equals(Utils.CALL)) {
				// To maybe change in the future, does not check if initialization is correct
				// with calls within if
				child.setInitialization(Utils.MAYBE_INIT);
				nodesScope.add(child);
			}
		}

		// Has else, needs to compare to previous statements
		if (elseNode != null) {
			for (int i = 0; i < elseNode.jjtGetNumChildren(); i++) {
				SimpleNode child = (SimpleNode) elseNode.jjtGetChild(i);

				if (child.getType().equals(Utils.OP)) {
					SimpleNode resultNode = analyseOperation(child);
					
					System.out.println("ResultNode of else " + resultNode.getValue() + " type " + resultNode.getType());
					SimpleNode previousNode = Utils.containsValue(nodesScope, resultNode);
					
					// Was nowhere to be found
					if (previousNode == null && lookup(resultNode) == null) {
						if (resultNode != null) {
							resultNode.setInitialization(Utils.MAYBE_INIT);
							nodesScope.add(resultNode);
						}
					}

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
				} 
			}
		}
		
		// Merge the previous array with the new, replacing all the old instantiations
		ArrayList<SimpleNode> mergedNodesInScope = Utils.mergeArrays(symbolTrees.get(currentScope), nodesScope);
		symbolTrees.replace(currentScope, mergedNodesInScope);		
	}

	public boolean analyseExprTest(SimpleNode exprTest) {
		SimpleNode leftChildExpr = ((SimpleNode) exprTest.jjtGetChild(0));
		SimpleNode rightChildExpr = ((SimpleNode) exprTest.jjtGetChild(1));

		// Check for previous declaration
		SimpleNode previousLeftNode = Utils.containsValue(symbolTrees.get(currentScope), leftChildExpr);
		System.out.println("ANda la " + symbolTrees.get(currentScope));
				
		if (previousLeftNode == null) {
			previousLeftNode = lookup(leftChildExpr);
		}
		if (previousLeftNode != null)
			leftChildExpr = previousLeftNode;

		if (rightChildExpr.getType().equals(Utils.RHS))
			rightChildExpr = (SimpleNode) rightChildExpr.jjtGetChild(0);

		SimpleNode previousRightNode = Utils.containsValue(symbolTrees.get(currentScope), rightChildExpr);
				
		if (previousRightNode == null) {
			previousRightNode = lookup(leftChildExpr);
		}
		if (previousRightNode != null)
			rightChildExpr = previousRightNode;

		System.out.println("leftchildexpr " + leftChildExpr.getValue() + " type " + leftChildExpr.getType());
		System.out.println("rightchildexpr " + rightChildExpr.getValue() + " type " + rightChildExpr.getType());

		// Comparison between array and scalar or number
		if (leftChildExpr.getType().equals(Utils.ARRAY)) {
			System.out.println("Semantic Error : Array cannot compare with " + rightChildExpr.getType());
			hasErrors = true;
			return false;
		}

		if (rightChildExpr.getType().equals(Utils.ARRAY)) {
			 System.out.println("Semantic Error : Array cannot compare with " + leftChildExpr.getType());
			 hasErrors = true;
			 return false;
		 }	 
		
		 return true;

	}

	/**
	 * Analyse calls, do all semantic checks after all the function in the module
	 * were read
	 */
	public SimpleNode analyseCalls(SimpleNode nodeToAnalyse) {

		// Extract the real call hidden within possible assigns and operations
		SimpleNode callToBeAnalysed = Utils.extractOfType(Utils.CALL, nodeToAnalyse);
		SimpleNode function = Utils.containsValue(functions, callToBeAnalysed);

		//System.out.println("Analysing call " + nodeToAnalyse);

		if (function == null) {
			System.out
					.println("Semantic Error : There was no function associated named " + callToBeAnalysed.getValue());
			hasErrors = true;
			return null;
		} else {
			// Call is correct, checking types
			if (nodeToAnalyse.getType().equals(Utils.OP)) {
				// Had operation in call
				
				SimpleNode leftNode = Utils.extractOfType(Utils.SCALAR, (SimpleNode) nodeToAnalyse.jjtGetChild(0));
				
				if (leftNode == null)
					leftNode = Utils.extractOfType(Utils.ARRAY, (SimpleNode) nodeToAnalyse.jjtGetChild(0));
					
				// Check previous declarations of node
				SimpleNode previousLeftNode = Utils.containsValue(symbolTrees.get(currentScope), leftNode);

				if (previousLeftNode != null)
					leftNode = previousLeftNode;

				// Semantic check of return type
				if (!((ASTFunction) function).getReturnType().equals(leftNode.getType())) {
					hasErrors = true;
					System.out.println("Semantic Error : Mismatching types between " + leftNode.getValue() + " and "
							+ function.getValue() + " -> " + leftNode.getType() + " opposed to "
							+ ((ASTFunction) function).getReturnType());
					return null;
				} else {
					return leftNode;
				}
			}


			// Checking argslist to see if the size and types are correct
			if (callToBeAnalysed.jjtGetChild(0).jjtGetNumChildren() != function.jjtGetChild(0).jjtGetNumChildren()) {
				System.out.println("Semantic Error : Mismatching number of arguments in call "
					+ callToBeAnalysed.getValue() + " -> " + callToBeAnalysed.jjtGetChild(0).jjtGetNumChildren()
					+ " opposed to " + function.jjtGetChild(0).jjtGetNumChildren());
				hasErrors = true;
				return null;
			}
			return null;
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
