import java.math.RoundingMode;
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

	public void fillSymbols(SimpleNode node) {
		currentScope = "";

		// First passage to check functions
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			SimpleNode nodeToAnalyse = (SimpleNode) node.jjtGetChild(i);

			// System.out.println("Node to analyse " + nodeToAnalyse.getType());

			if (nodeToAnalyse.getType().equals(Utils.FUNCTION)) {
				currentScope = nodeToAnalyse.getValue();

				// Create new symbol tree for this scope
				ArrayList<SimpleNode> newNodesInScope = new ArrayList<>();
				symbolTrees.put(currentScope, newNodesInScope);

				// Function Name was already encountered before
				if (Utils.contains(functions, nodeToAnalyse) != null) {
					System.out.println("Semantic Error : " + "There are more than one function with name "
							+ nodeToAnalyse.getValue() + ".");
				} else
					functions.add(nodeToAnalyse);

				SimpleNode testNode = (SimpleNode) nodeToAnalyse.jjtGetChild(0);
				if (testNode.getType().equals(Utils.VARLIST))
					nodeToAnalyse = testNode;
				else
					nodeToAnalyse = (SimpleNode) nodeToAnalyse.jjtGetChild(1);

				// If it is the function argument list, need to store that for check
				if (nodeToAnalyse.getType().equals(Utils.VARLIST)) {
					for (int j = 0; j < nodeToAnalyse.jjtGetNumChildren(); j++) {
						SimpleNode newNode = (SimpleNode) nodeToAnalyse.jjtGetChild(j);

						if (newNode.getValue() == null)
							newNode = (SimpleNode) newNode.jjtGetChild(0);

						newNode.setInitialization(Utils.DEFIN_INIT);
						push(newNode);
					}
				}
			} else if (nodeToAnalyse.getType().equals(Utils.DECLARATION_SCALAR)
					|| nodeToAnalyse.getType().equals(Utils.DECLARATION_ARRAY)) {
				SimpleNode test = Utils.containsValue(declarations, nodeToAnalyse);
				if (test == null) {
					if (nodeToAnalyse.getType().equals(Utils.DECLARATION_SCALAR))
						nodeToAnalyse.setType(Utils.SCALAR);
					else
						nodeToAnalyse.setType(Utils.ARRAY);

					nodeToAnalyse.setInitialization(Utils.DEFIN_INIT);
					declarations.add(nodeToAnalyse);
				} else {
					if (test.getType().equals(Utils.SCALAR)
							&& nodeToAnalyse.getType().equals(Utils.DECLARATION_ARRAY)) {
						hasErrors = true;
						System.out.println("Semantic Error : Declaration of array " + test.getValue()
								+ " does not match previous declaration of scalar with the same value.");
					} else {
						nodeToAnalyse.setType(test.getType());
						nodeToAnalyse.setInitialization(test.isInitialized());
						declarations.add(nodeToAnalyse);
					}
				}
			}

			// System.out.println("functions " + functions);
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
				if (check == null)
					check = Utils.containsValueString(declarations, valueToAnalyse);

				if (check != null) {
					if (check.isInitialized() != Utils.DEFIN_INIT) {
						System.out.println("Semantic Error : Variable " + valueToAnalyse
								+ " needed to be initialized and wasn't.");
						hasErrors = true;
					}
				} else {
					System.out.println(
							"Semantic Error : Variable " + valueToAnalyse + " needed to be initialized and wasn't.");
					hasErrors = true;
				}
			}

		}

	}

	private void analyseFunctions(SimpleNode node) {
		for (int i = 0; i < node.jjtGetNumChildren(); i++) {
			SimpleNode nodeToAnalyse = (SimpleNode) node.jjtGetChild(i);

			// System.out.println("Going through node " + nodeToAnalyse + " value " +
			// nodeToAnalyse.getValue() + " type " + nodeToAnalyse.getType());

			// If it is a conditional, analyse everything inside
			if (nodeToAnalyse.getType() == Utils.COND) {
				analyseConditional(nodeToAnalyse);
			} else {
				if (nodeToAnalyse.getType() == Utils.CALL) {
					SimpleNode newNode = analyseCalls(nodeToAnalyse, false);
					if (newNode != null) {
						symbolTrees.get(currentScope).add(newNode);
					}
				}
				if (nodeToAnalyse.getType() == Utils.EXTERNAL_CALL) {
					SimpleNode newNode = analyseCalls(nodeToAnalyse, true);
					if (newNode != null) {
						symbolTrees.get(currentScope).add(newNode);
					}
				} else if (nodeToAnalyse.getType() == Utils.OP) {
					// Is operation
					SimpleNode newNode = analyseOperation(nodeToAnalyse);

					// If it is something worth adding, add it
					if (newNode != null) {
						if (newNode.getType().equals(Utils.ARRAY) || newNode.getType().equals(Utils.SCALAR)) {
							push(newNode);
						}
					}
				}

				analyseFunctions(nodeToAnalyse);
			}
		}
	}

	// Semantically analyse operations
	public SimpleNode analyseOperation(SimpleNode node) {
		SimpleNode leftChild = (SimpleNode) node.jjtGetChild(0);
		SimpleNode rightChild = (SimpleNode) node.jjtGetChild(1);

		// .size semantic check
		if (leftChild.getType().equals(Utils.SIZE)) {
			hasErrors = true;
			System.out.println("Semantic Error : Improper use of '.size' with variable " + leftChild.getValue());
			return null;
		}
		
		// Check if there are any hidden calls
		if (Utils.checkFor(Utils.CALL, leftChild) || Utils.checkFor(Utils.CALL, rightChild)) {
			SimpleNode callNode = analyseCalls(node, false);
			return callNode;
		}
		
		else if (Utils.checkFor(Utils.EXTERNAL_CALL, leftChild) || Utils.checkFor(Utils.EXTERNAL_CALL, rightChild)) {
			SimpleNode callNode = analyseCalls(node, true);
			return callNode;
		}
		
		// Check for array instantiations
		if (rightChild.jjtGetNumChildren() > 0) {
			if (rightChild.getType().equals(Utils.RHS) && rightChild.jjtGetChild(0).jjtGetNumChildren() > 0)
				rightChild = (SimpleNode) rightChild.jjtGetChild(0);
						
			
			if (((SimpleNode) rightChild.jjtGetChild(0)).getType().equals(Utils.ARRAY_INST)
			|| ((SimpleNode) rightChild.jjtGetChild(0)).getType().equals(Utils.ARRAY_INST_SCALAR)) {
				SimpleNode previousLeftNode = Utils.containsValue(symbolTrees.get(currentScope), leftChild);
				if (previousLeftNode == null)
					previousLeftNode = Utils.containsValue(declarations, leftChild);
				
				if (previousLeftNode != null)
					leftChild = previousLeftNode;

					if (leftChild.getType().equals(Utils.ARRAY)) {
					leftChild.setInitialization(Utils.DEFIN_INIT);
					push(leftChild);
					return leftChild;
				} else if (leftChild.isInitialized() == Utils.NOT_INIT) {
					leftChild.setType(Utils.ARRAY);
					leftChild.setInitialization(Utils.DEFIN_INIT);
					push(leftChild);
					return leftChild;
				} else {
					System.out.println(
							"Semantic Error : Attempted to instantiate scalar variable " + leftChild.getValue());
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
		return analyseTwoNodesOperation(leftChild, rightChild, false, node);

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
			// return null;

			SimpleNode rightNode = (SimpleNode) rightChild.jjtGetChild(1);

			if (rightNode.getType() == Utils.TERM)
				rightNode = (SimpleNode) rightNode.jjtGetChild(0);

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

		/*System.out.println("Teste");
		for (int i = 0; i < symbolTrees.get(currentScope).size(); i++) {
			SimpleNode yo = symbolTrees.get(currentScope).get(i);
			System.out.println("Yo " + yo.getValue() + " type " + yo.getType() + " init " + yo.isInitialized());
		}*/

		if (rightType == Utils.RHS) {
			rightChild = (SimpleNode) rightChild.jjtGetChild(0);
			rightType = rightChild.getType();
		}
		if (rightType == Utils.TERM) {
			rightChild = (SimpleNode) rightChild.jjtGetChild(0);
			rightType = rightChild.getType();
		}

		if (rightType.equals(Utils.ARRAY_INST) || rightType.equals(Utils.ARRAY_INST)) {
			rightChild = (SimpleNode) rightChild.jjtGetChild(0);
			rightType = rightChild.getType();
		}

		if (leftType == Utils.TERM) {
			leftChild = (SimpleNode) leftChild.jjtGetChild(0);
			leftType = leftChild.getType();
		}

		if (leftType.equals(Utils.ARRAY_INST) || leftType.equals(Utils.ARRAY_INST)) {
			leftChild = (SimpleNode) leftChild.jjtGetChild(0);
			leftType = leftChild.getType();
		}

		// If RightType is RHS or term

		// Check possible previous instantiations in symbol table
		SimpleNode previousLeftNode = Utils.containsValue(symbolTrees.get(currentScope), leftChild);
		if (previousLeftNode == null)
			previousLeftNode = Utils.containsValue(declarations, leftChild);

		if (leftChild.getType().equals(Utils.SIZE)) {
			if (previousLeftNode == null) {
				System.out.println("Semantic Error : Attempt to utilize '.size' without from unitialized variable "
						+ leftChild.getValue());
				hasErrors = true;
				return null;
			}
			if (!previousLeftNode.getType().equals(Utils.ARRAY)) {
				System.out.println("Semantic Error : Attempt to utilize '.size' without being array, with variable "
						+ leftChild.getValue());
				hasErrors = true;
				return null;
			}
		}

		if (previousLeftNode != null) {
			leftType = previousLeftNode.getType();

			if (leftChild.getType().equals(Utils.ARRAY_ACCESS) || leftChild.getType().equals(Utils.SIZE)) {
				leftType = Utils.SCALAR;
			}

			leftChild = previousLeftNode;
		}

		SimpleNode previousRightNode = Utils.containsValue(symbolTrees.get(currentScope), rightChild);
		if (previousRightNode == null)
			previousRightNode = Utils.containsValue(declarations, rightChild);

		if (rightChild.getType().equals(Utils.SIZE)) {
			if (previousRightNode == null) {
				System.out.println("Semantic Error : Attempt to utilize '.size' without from unitialized variable "
						+ rightChild.getValue());
				hasErrors = true;
				return null;
			}
			if (!previousRightNode.getType().equals(Utils.ARRAY)) {
				System.out.println("Semantic Error : Attempt to utilize '.size' without being array, with variable "
						+ rightChild.getValue());
				hasErrors = true;
				return null;
			}
		}

		if (previousRightNode != null) {
			rightType = previousRightNode.getType();

			if (rightChild.getType().equals(Utils.ARRAY_ACCESS) || rightChild.getType().equals(Utils.SIZE)) {
				rightType = Utils.SCALAR;
			}
			rightChild = previousRightNode;
		}

		//System.out.println("Chego aqui com " + leftChild.getValue() + " " + leftType + " e " + rightChild.getValue()
				//+ " " + rightType + " and isInit left " + leftChild.isInitialized() + " isInit right "
				//+ rightChild.isInitialized());

		if (operation != null) {
			// In case of '<' or '>' comparison between arrays
			if ((leftType == Utils.ARRAY || rightType == Utils.ARRAY) && (operation.getValue() != null
					&& (operation.getValue().equals("<") || operation.getValue().equals(">")))) {
				hasErrors = true;
				System.out.println("Semantic Error : Imcompatible operation ('" + operation.getValue()
						+ "') between two arrays, " + leftChild.getValue() + " and " + rightChild.getValue() + ".");
				return null;
			}
			if (leftType.equals(Utils.ARRAY) && rightType.equals(Utils.NUMBER) && !needToBeInitialized) {
				leftChild.setInitialization(Utils.DEFIN_INIT);
				return leftChild;
			}
		}

		// Utils.printNode(rightChild);
		// Right Hand Side variable was not initialized, semantic error
		if (rightType != Utils.NUMBER) {
			if (rightChild.isInitialized() == Utils.NOT_INIT) {
				hasErrors = true;
				System.out.println("Semantic Error : Variable " + rightChild.getValue() + " was not initialized.");
				return null;
			} // Right Hand Side may not be initialized, semantic warning
			else if (rightChild.isInitialized() == Utils.MAYBE_INIT) {
				System.out.println("Semantic Warning : Variable " + rightChild.getValue() + " may not be initialized.");
			} // Right Hand Side has had incompatible initializations, and is trying to be
				// accessed
			else if (rightChild.isInitialized() == Utils.INCOMPAT_INIT) {
				System.out.println("Semantic Error : Variable " + rightChild.getValue()
						+ " has incompatible previous declaration" + " in previous 'if' structure.");
				hasErrors = true;
				return null;
			}
		} else {
			if (leftType.equals(Utils.ARRAY)) {
				hasErrors = true;
				System.out
						.println("Semantic Error : Variable " + leftChild.getValue() + " is in operation with number.");
				return null;
			}
		}

		// Left node needs to be initialized (in case of it being on the rhs of some op)
		if (!leftType.equals(Utils.NUMBER)) {
			if (leftChild.isInitialized() == Utils.NOT_INIT && needToBeInitialized) {
				hasErrors = true;
				System.out.println("Semantic Error : Variable " + leftChild.getValue() + " was not initialized.");
				return null;
			} else if (leftChild.isInitialized() == Utils.MAYBE_INIT) {
				System.out
						.println("Semantic Warning : Variable " + leftChild.getValue() + " was maybe not initialized.");
				return leftChild;
			} else if (leftChild.isInitialized() == Utils.INCOMPAT_INIT) {
				System.out.println(
						"Semantic Warning : Variable " + leftChild.getValue() + " could be either scalar or array.");
				return leftChild;
			}
		} else {
			if (rightType.equals(Utils.ARRAY)) {
				hasErrors = true;
				System.out.println(
						"Semantic Error : Variable " + rightChild.getValue() + " is in operation with number.");
				return null;
			}
		}

		// If they are scalars or arrays
		if ((leftType.equals(Utils.SCALAR) || leftType.equals(Utils.ARRAY))
				&& (rightType.equals(Utils.SCALAR) || rightType.equals(Utils.ARRAY))) {
			if (leftType.equals(Utils.SCALAR) && rightType.equals(Utils.ARRAY)) {
				hasErrors = true;
				System.out.println("Semantic Error: Incompatible operation between " + "left Hand Side Value "
						+ leftChild.getValue() + " type " + leftType + " and Right Hand Side Value "
						+ rightChild.getValue() + " type " + rightType);
				return null;
			}
		}
		if (leftType.equals(Utils.SCALAR) && !rightType.equals(Utils.ARRAY) && !needToBeInitialized) {
			leftChild.setInitialization(Utils.DEFIN_INIT);
			leftChild.setType(Utils.SCALAR);
			return leftChild;
		}

		// System.out.println("End analysing\n");

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

		if (!isValid) {
			return;
		}

		// Analyse all the nodes inside the if
		for (int i = 1; i < nodeToAnalyse.jjtGetNumChildren(); i++) {
			SimpleNode child = (SimpleNode) nodeToAnalyse.jjtGetChild(i);

			// If there are operations inside 'if'
			if (child.getType().equals(Utils.OP)) {
				SimpleNode resultNode = analyseOperation(child);

				if (resultNode == null)
					return;

				// Check previous instanciations of resultNode
				SimpleNode previousNode = Utils.containsValue(nodesScope, resultNode);
				if (previousNode == null)
					previousNode = Utils.containsValue(declarations, resultNode);

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
						push(resultNode);
					}

				} else { // Had no node in scope, can add safely
					push(resultNode);
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
					System.out.println("2");
					SimpleNode resultNode = analyseOperation(child);

					SimpleNode previousNode = Utils.containsValue(nodesScope, resultNode);
					if (previousNode == null)
						previousNode = Utils.containsValue(declarations, resultNode);

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

		if (!leftChildExpr.getType().equals(Utils.NUMBER)) {
			// Check for previous declaration
			SimpleNode previousLeftNode = Utils.containsValue(symbolTrees.get(currentScope), leftChildExpr);
			if (previousLeftNode == null)
				previousLeftNode = Utils.containsValue(declarations, leftChildExpr);

			if (previousLeftNode == null) {
				previousLeftNode = lookup(leftChildExpr);
			}
			if (previousLeftNode != null)
				leftChildExpr = previousLeftNode;
		}

		if (rightChildExpr.getType().equals(Utils.RHS))
			rightChildExpr = (SimpleNode) rightChildExpr.jjtGetChild(0);

		if (!rightChildExpr.getType().equals(Utils.NUMBER)) {
			SimpleNode previousRightNode = Utils.containsValue(symbolTrees.get(currentScope), rightChildExpr);
			if (previousRightNode == null)
				previousRightNode = Utils.containsValue(declarations, rightChildExpr);

			if (previousRightNode == null) {
				previousRightNode = lookup(leftChildExpr);
			}
			if (previousRightNode != null)
				rightChildExpr = previousRightNode;
		}

		// Comparison between array and scalar or number
		if (leftChildExpr.getType().equals(Utils.ARRAY)) {
			System.out.println("Semantic Error : Array cannot compare with " + rightChildExpr.getValue());
			hasErrors = true;
			return false;
		}

		else if (rightChildExpr.getType().equals(Utils.ARRAY)) {
			System.out.println("Semantic Error : Array cannot compare with " + leftChildExpr.getValue());
			hasErrors = true;
			return false;
		}

		return true;

	}

	/**
	 * Analyse calls, do all semantic checks after all the function in the module
	 * were read
	 */
	public SimpleNode analyseCalls(SimpleNode nodeToAnalyse, boolean isExternal) {
		SimpleNode callToBeAnalysed;
		// Extract the real call hidden within possible assigns and operations
		if (isExternal) {
			callToBeAnalysed = new SimpleNode(0);
			callToBeAnalysed.setInitialization(Utils.DEFIN_INIT);
			callToBeAnalysed.setType(Utils.SCALAR);
		} else
			callToBeAnalysed = Utils.extractOfType(Utils.CALL, nodeToAnalyse);

		SimpleNode function = Utils.containsValue(functions, callToBeAnalysed);

		// Has no function and isn't external call
		if (function == null && !isExternal) {
			System.out.println("Semantic Error : There was no function associated named " + callToBeAnalysed.getValue());
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

				if (previousLeftNode == null)
					previousLeftNode = Utils.containsValue(declarations, leftNode);

				if (previousLeftNode != null)
					leftNode = previousLeftNode;
				// Semantic check of return type
				if (isExternal) {
					if (!leftNode.getType().equals(Utils.SCALAR)) {
						hasErrors = true;
						System.out.println("Semantic Error : External package, by default "
								+ " return scalar, variable " + leftNode.getValue()
								+ " was already initialized with type " + leftNode.getType() + ".");
						return null;
					} else {
						leftNode.setInitialization(Utils.DEFIN_INIT);
						return leftNode;
					}
				} else {
					if (leftNode != null && !((ASTFunction) function).getReturnType().equals(leftNode.getType())) {
						hasErrors = true;
						System.out.println("Semantic Error : Mismatching types between " + leftNode.getType() + " "
								+ leftNode.getValue() + " and " + function.getValue() + " -> " + leftNode.getType()
								+ " opposed to " + ((ASTFunction) function).getReturnType());
						return null;
					}

					SimpleNode varlistCall = (SimpleNode) callToBeAnalysed.jjtGetChild(0);

					if (!(varlistCall instanceof ASTArgumentList))
						varlistCall = (SimpleNode) callToBeAnalysed.jjtGetChild(1);

					SimpleNode varlistFunction = (SimpleNode) function.jjtGetChild(0);

					if (!varlistFunction.getType().equals(Utils.VARLIST))
						varlistFunction = (SimpleNode) function.jjtGetChild(1);

					

					// Checking argslist to see if the size and types are correct
					if (callToBeAnalysed.jjtGetNumChildren() > 0
							&& varlistCall.jjtGetNumChildren() != varlistFunction.jjtGetNumChildren()) {
						System.out.println("Semantic Error : Mismatching number of arguments in call "
								+ callToBeAnalysed.getValue() + " -> " + varlistCall.jjtGetNumChildren()
								+ " opposed to " + varlistFunction.jjtGetNumChildren());
						hasErrors = true;
						return null;
					} else {
						// Check to see types of arguments ()
						String typeCall, typeFunction;
						for (int i = 0; i < varlistCall.jjtGetNumChildren() ; i++ ) {
							typeCall = ((SimpleNode) varlistCall.jjtGetChild(i)).getType();
							typeFunction = ((SimpleNode) varlistFunction.jjtGetChild(i)).getType();

							System.out.println("DAMN IT " + varlistCall.jjtGetChild(i));
							
							if (typeCall != null) {
								if (typeCall.equals(Utils.NUMBER))
									typeCall = Utils.SCALAR;
							} else {
								SimpleNode previousNode = Utils.containsValue(symbolTrees.get(currentScope),
									 ((SimpleNode) varlistCall.jjtGetChild(i)));
								
								if (previousNode == null)
									previousNode = Utils.containsValue(declarations,
										((SimpleNode) varlistCall.jjtGetChild(i)));

								if (previousNode == null) {
									hasErrors = true;
									System.out.println("Semantic Error : Variable " + ((SimpleNode) varlistCall.jjtGetChild(i)).getValue() 
									 + " was not initialized before being passed as argument.");
									return null;
								}				
								
								typeCall = previousNode.getType();
							}
							if (!typeCall.equals(typeFunction)) {
								hasErrors = true;
								System.out.println("Semantic Error : Invalid type passed as argument. Passed " +typeCall + ", expected "
									+ typeFunction);
								return null;
							}
						}

						return leftNode;
					}
				}
			}

			return null;
		}
	}

	public ASTFunction getFunction(String functionName) {

		for (int i = 0; i < functions.size(); i++) {
			ASTFunction function = (ASTFunction) functions.get(i);
			if (function.functionName.toLowerCase().equals(functionName.toLowerCase())) {
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
