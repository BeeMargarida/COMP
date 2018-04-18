import java.util.ArrayList;

public class SymbolTable {

    // HashMap<Value, Scope>
    private ArrayList<Symbol> symbols;
    private int currentScope;

    public SymbolTable() {
        symbols = new ArrayList<>();
    }

    public boolean lookup(Symbol symbolToEvaluate) {
        for (Symbol symbol : symbols) {
            if (symbol.getValue() == symbolToEvaluate.getValue()  
                    && symbol.getScope() >= symbolToEvaluate.getScope())
                return true;
        }
        
        return false;
    }

    public boolean push(Symbol symbol) {

		// Maybe do check here? Dunno tbh
		symbols.add(symbol);
        
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
		for (int i = 0 ; i < symbols.size(); i++) {
			String type = symbols.get(i).getType();
			if (type != null)
				if (type == "Assign") { // Check children

				}

		}
        
    }
}

/*
package cps450;

import java.util.Iterator;
import java.util.Stack;

public class SymbolTable {
	
	Stack<Symbol> symbols;
	Integer level;
	
	public SymbolTable() {
		symbols = new Stack<Symbol>();
		level = 0;
	}
	
	Symbol push(String name, Declaration decl) {
		Symbol symbol = new Symbol(name, getScope(), decl);
		symbols.push(symbol);
		return symbol;
	}
	
	Symbol scopeContains(String name) {
		Symbol symbol = null;
		for (int i = symbols.size() - 1; i >= 0; i--) {
			symbol = symbols.get(i);
			if (symbol.getScope().equals(this.getScope())) {
				if (symbol.getName().equals(name)) {
					return symbol;
				}
			} else {
				break;
			}
		}
		return null;
	}
	
	Symbol lookup(String name) {
		Symbol symbol = null;
		for (int i = symbols.size() - 1; i >= 0; i--) {
			symbol = symbols.get(i);
			if (symbol.getName().equals(name)) {
				return symbol;
			}
		}
		return null;
	}
	
	void beginScope() {
		level += 1;
	}
	
	void endScope() {
		for (int i = symbols.size() - 1; i >= 0; i--) {
			Symbol symbol = symbols.get(i);
			if (symbol.getScope().equals(this.getScope())) {
				symbols.remove(i);
			} else {
				break;
			}
		}
		level -= 1;
	}
	
	Integer getScope() {
		return level;
	}
	
}

*/