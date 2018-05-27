import java.util.ArrayList;
import java.util.HashMap;

public class Generator {

    private Sampler sampler;
    private SymbolTable table;
    private String moduleName;

    private int stackLimit;
    private int stackMax;
    private String function;

    private int stackDeclaration;
    private int localDeclaration;

    private int loopCount;

    private HashMap<String, ArrayList<SimpleNode>> stack;

    public Generator(Sampler sampler, SymbolTable table) {
        this.sampler = sampler;
        this.table = table;
        this.function = "";
        stack = new HashMap<>();
        stackLimit = 0;
        stackMax = 0;
        loopCount = -1;

        stackDeclaration = 0;
        localDeclaration = 0;
    }

    public Object visit(SimpleNode node) {

        if (node.toString().equals("Module")) {
            visit((ASTModule) node);
        } else if (node.toString().equals("Function")) {
            visit((ASTFunction) node);
        } else if (node.toString().equals("Declaration")) {
            visit((ASTDeclaration) node);
        }
        
        return null;

    }

    // Module Node
    public Object visit(ASTModule node) {

        // prints Module yasmin code
        this.moduleName = node.name;
        sampler.startModule(node.name);

        // visits Module nodes
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            SimpleNode n = (SimpleNode) node.jjtGetChild(i);
            n.generatorVisit(this);
        }

        sampler.printClinit();
        sampler.close();
        return null;
    }

    public Object visit(ASTDeclaration node) {
        System.out.println("DECLARATION");

        for(int i = 0; i < node.jjtGetNumChildren(); i++){
            System.out.println("DECLARATION CHIL: " + node.jjtGetChild(i).toString());

            
        }
        return null;
    }

    // Function Node
    public Object visit(ASTFunction node) {

        System.out.println("FUNCTION: " + node.getValue());

        if (node.functionName.equals("main")) {
            // if the function is the main one
            sampler.functionBegin(node.functionName, Utils.VOID, null);

            //ocupy first position of stack
            ArrayList<SimpleNode> arr = new ArrayList<SimpleNode>();
            arr.add(null);
            stack.put("main", arr);

        } else {
            // If there is no Parameters (Var)
            if (node.jjtGetChild(0).jjtGetNumChildren() == 0) {
                sampler.functionBegin(node.functionName, node.getReturnType(), null);
            } else {
                // Get types of vars
                String[] vars = (String[]) visit((ASTVarList) node.jjtGetChild(0), node.functionName);
                sampler.functionBegin(node.functionName, node.getReturnType(), vars);
            }
        }

        // Get locals values
        int locals = table.getSymbolTrees().get(node.functionName).size();
        sampler.printLocalsLimit(locals);
        //function += ".limit locals " + locals + "\n";

        // Checks the other children of the function
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            System.out.println("\nFUNCTION CHILDREN: " + node.jjtGetChild(i).toString());
            checkFunctionChildren((SimpleNode) node.jjtGetChild(i), node.functionName);
        }

        // update stack Limit
        if(stackMax < stackLimit){
            stackMax = stackLimit;
        }
        sampler.printStackLimit(stackMax);
        sampler.printString(function);

        stackLimit = 0;
        stackMax = 0;
        function = "";
        loopCount = -1;

        // prints return types
        if (node.getReturnType().equals(Utils.VOID)) {
            sampler.printVoidReturn();
        } else {
            int numStack = getFromStack(node.getReturnValue(), node.functionName);
            sampler.printLoad(numStack);
            sampler.printIntReturn();
        }

        // prints yasmin Function endline
        sampler.functionEnd();

        return null;
    }

    public void checkFunctionChildren(SimpleNode node, String functionName) {
        System.out.println("Function CHILDREN TYPES: " + node.toString());
        
        if (node.toString().equals("Call")) {
            visit((ASTCall) node, functionName);
        } 
        else if (node.toString().equals("Assign")) {
            visit((ASTAssign) node, functionName);
        } 
        else if(node.toString().equals("If")) {
            visit((ASTIf) node, functionName);
        }
        else if(node.toString().equals("While")){
            visit((ASTWhile) node, functionName);
        }
        
    }

    // gets all the types of vars
    public Object visit(ASTVarList node, String functionName) {
        int numChildren = node.jjtGetNumChildren();
        String[] vars = new String[numChildren];

        for (int i = 0; i < numChildren; i++) {
            vars[i] = visit((ASTVar) node.jjtGetChild(i), functionName);
        }
        return vars;
    }

    // returns type of Var
    public String visit(ASTVar node, String functionName) {

        if (stack.get(functionName) == null) {
            ArrayList<SimpleNode> arr = new ArrayList<SimpleNode>();
            arr.add(node);
            stack.put(functionName, arr);
        } else
            stack.get(functionName).add(node);

        return node.getType();
    }

    public Object visit(ASTCall node, String currentFunctionName) {
        if (node.jjtGetNumChildren() == 0)
            return null;
        // if there are parameters to the function call
        SimpleNode varList = (SimpleNode) node.jjtGetChild(0);

        String[] params = new String[varList.jjtGetNumChildren()];

        // Get parameters of the function called
        for (int i = 0; i < varList.jjtGetNumChildren(); i++) {
            ASTArgument arg = (ASTArgument) varList.jjtGetChild(i);

            // checks type of parameters passed
            boolean isInteger;
            try {
                Integer.parseInt(arg.content);
                isInteger = true;
            } catch (NumberFormatException e) {
                isInteger = false;
            }

            // check if parameter is integer or a variable
            if (isInteger) {
                // if parameter is a integer
                stackLimit++;

                params[i] = "I";
                function += sampler.getConst(arg.content) + "\n";

            } else {
                System.out.println("ARG CONTENT: " + arg.content + " : " + arg.content.contains("\""));
                
                if(arg.content.contains("\"")){

                    function += sampler.getLdc(arg.content);
                    params[i] = "Ljava/lang/String;";
                }
                else {

                    // go check the stack and do iload_<number> and its type
                    int numStack = getFromStack(arg.content, currentFunctionName);
                    if (numStack != -1) {
    
                        stackLimit++;
                        String type = stack.get(currentFunctionName).get(numStack).getType();
                        function += sampler.getLoad(numStack, type) + "\n";
    
                        // check type of parameters - TODO: Make this more readable
                        if (stack.get(currentFunctionName).get(numStack).getType().equals(Utils.SCALAR)) {
                            params[i] = "I";
                        } else if (stack.get(currentFunctionName).get(numStack).getType().equals(Utils.ARRAY))
                            params[i] = "[I";
    
                    } else {
                        System.out.println("Not in stack");
                    }
                }
            }
        }
        

        // TODO - do not hardcode this!!
        // go check the type of return of the function
        String returnType;
        if (!node.getValue().equals("println") && !node.getValue().equals("print")) {

            ASTFunction function = this.table.getFunction(node.getValue());

            if (function == null) {
                System.out.println("Function doesn't exist");
                return -1;
            }

            if (function.getReturnType().equals(Utils.VOID)) {
                returnType = "V";
            } else
                returnType = "I";
        } else {
            returnType = "V";
        }

        // prints function invocation
        // sampler.printFunctionInvocation(this.moduleName, node.getValue(), params,
        // returnType);

        System.out.println("FUNCTION CALL: " + node.getValue());
        function += sampler.getFunctionInvocation(this.moduleName, node.getValue(), params, returnType) + "\n";

        return null;
    }

    public Object visit(ASTAssign node, String functionName) {

        // RHS
        ASTRhs rhs = (ASTRhs) node.jjtGetChild(1);

        // LHS
        SimpleNode lhs = (SimpleNode) node.jjtGetChild(0);

        if(lhs.toString().equals("ArrayAccess")){
            ASTArrayAccess lhsArr = (ASTArrayAccess) lhs;
            int numStack = getFromStack(lhs.getValue(), functionName);
            // prints commands
            System.out.println("LHS ARRAY ACCESS: " + lhsArr.toString() + " : " + lhsArr.getIndex());
            function += sampler.getLoad(numStack, Utils.ARRAY) + "\n";

            numStack = getFromStack(lhsArr.getIndex(), functionName);
            function += sampler.getLoad(numStack, Utils.SCALAR) + "\n";
        }
        
        //Process RHS
        boolean[] answer = (boolean[]) visit(rhs, functionName);
        boolean isOp = answer[0];
        boolean wasArray = answer[1];

        // print operator
        if (isOp && rhs.getValue() != null){
            System.out.println("OP! " + rhs.getValue());
            function += sampler.getOperator(rhs.getValue()) + "\n";
            
        }

        // LHS
        System.out.println("LHS: " + lhs.toString());

        //Update stack Max
        if(stackMax < stackLimit){
            stackMax = stackLimit;
        }
        stackLimit = 0;

        // check if arrayAccess or not
        if(lhs.toString().equals("ArrayAccess")){

            function += sampler.getIStore();

        }
        else {

            System.out.println("LHS ScalarAccess : " + lhs.getValue());

            // add to stack
            if (stack.get(functionName) == null) {
                ArrayList<SimpleNode> arr = new ArrayList<SimpleNode>();
                arr.add(lhs);
                stack.put(functionName, arr);
            } else {
                stack.get(functionName).add(lhs);
            }

            int numStack = getFromStack(lhs.getValue(), functionName);

            if(wasArray){
                function += sampler.getStore(numStack, Utils.ARRAY) + "\n\n";
            }
            else{
                function += sampler.getStore(numStack, Utils.SCALAR) + "\n\n";
            }
        }

        return null;
    }

    public Object visit(ASTRhs rhs, String functionName){

        boolean isOp = false;
        boolean wasArray = false;

        System.out.println("RHS");

        for (int i = 0; i < rhs.jjtGetNumChildren(); i++) {
            
            SimpleNode chil = (SimpleNode) rhs.jjtGetChild(i);

            System.out.println("RHS CHILD " + chil.toString());

            if(chil.jjtGetNumChildren() == 0 && chil.toString().equals(Utils.TERM)){
                
                stackLimit++;

                function += sampler.getConst(chil.getValue()) + "\n";
            }
            else if(chil.toString().equals("ArrayInstantion")) {

                // array instatiation
                int numStack = getFromStack(chil.getValue(), functionName);
                function += sampler.getLoad(numStack, Utils.SCALAR) + "\n";

                //stack.get(functionName).add(chil);

                function += sampler.getNewArray();
                wasArray = true;
            }
            else {

                // TODO : POR ISTO MAIS BONITO
                for (int a = 0; a < chil.jjtGetNumChildren(); a++) {

                    SimpleNode term = (SimpleNode) chil.jjtGetChild(a);

                    System.out.println("Term: " + term.toString() + " : " + term.getValue());

                    if(term.toString().equals("ArrayAccess")){
                        // If RHS is a function call
                        ASTArrayAccess arrAcc = (ASTArrayAccess) term;

                        System.out.println("ARRAY ACCESS " + arrAcc.getValue());
                        
                        int numStack = getFromStack(arrAcc.getValue(), functionName);
                        function += sampler.getLoad(numStack, Utils.ARRAY) + "\n";

                        numStack = getFromStack(arrAcc.getIndex(), functionName);
                        function += sampler.getLoad(numStack, Utils.SCALAR)  + "\n";

                        function += sampler.getILoad();

                    }
                    else if (term.toString().equals(Utils.CALL)) {
                        // If RHS is a function call

                        isOp = false;
                        visit((ASTCall) chil.jjtGetChild(a), functionName);

                    } else if (term.toString().equals("ScalarAccess")) {
                        
                        if(term.getType().equals(Utils.SIZE)){

                            System.out.println("SCALARACCESS SIZE ");
                            int numStack = getFromStack(term.getValue(), functionName);

                            function += sampler.getLoad(numStack, Utils.ARRAY) + "\n";
                            function += sampler.getArraySize();
                            
                        }
                        else {
                            // Scalar 
                            isOp = true;
                            int numStack = getFromStack(term.getValue(), functionName);
    
                            // if numStack = -1, check the module variables -> TODO
    
                            // TODO: Check if operation like a = a + 1-> it will be iinc <stack_n> 1
    
                            stackLimit++;
                            function += sampler.getLoad(numStack, term.getType()) + "\n";
                        }
                    }
                }
            }
        }

        boolean[] answer = {isOp, wasArray};
        return answer;
    }


    public Object visit(ASTIf node, String functionName){
        System.out.println("IF NODE");

        loopCount++;
        int currentLoopCount = loopCount;

        boolean hasElse = false;

        for(int i = 0; i < node.jjtGetNumChildren(); i++){
            System.out.println("IF NODE CHILDREN: " + node.jjtGetChild(i).toString());

            if(node.jjtGetChild(i).toString().equals("Exprtest")){
                visit((ASTExprtest) node.jjtGetChild(i), functionName);
            }
            else if(node.jjtGetChild(i).toString().equals("Else")){
                hasElse = true;
                function += sampler.getIfGotoElse(currentLoopCount);
                visitElse((ASTElse) node.jjtGetChild(i), functionName, currentLoopCount);
            }
            else {
                checkFunctionChildren((SimpleNode) node.jjtGetChild(i), functionName);
            }
        }

        function += sampler.getIfEnd(currentLoopCount, hasElse);

        return null;
    }

    public Object visit(ASTExprtest node, String functionName){

        System.out.println("ASTEXPRTEST: " + node.getValue());

        SimpleNode lhs = (SimpleNode) node.jjtGetChild(0);
        int numStack = getFromStack(lhs.getValue(), functionName);
        function += sampler.getLoad(numStack, lhs.getType()) + "\n";

        ASTRhs rhs = (ASTRhs) node.jjtGetChild(1);
        visit(rhs, functionName);

        function += sampler.getIfStart(node.getValue(), loopCount);

        return null;
    }

    public void visitElse(ASTElse node, String functionName, int currentLoopCount){
        
        System.out.println("ELSE");

        function += sampler.getIfElse(currentLoopCount);

        for(int i = 0; i < node.jjtGetNumChildren(); i++){
            checkFunctionChildren((SimpleNode) node.jjtGetChild(i), functionName);
        }
        
    }

    public Object visit(ASTWhile node, String functionName){
        
        System.out.println("WHILE");

        loopCount++;

        int currentLoopCount = loopCount;

        function += sampler.getWhileBegin(currentLoopCount);

        for(int i = 0; i < node.jjtGetNumChildren(); i++){
            System.out.println("WHILE CHIL: " + node.jjtGetChild(i).toString());

            if(node.jjtGetChild(i).toString().equals("Exprtest")){
                visit((ASTExprtest) node.jjtGetChild(i), functionName);
            }
            else {
                checkFunctionChildren((SimpleNode) node.jjtGetChild(i), functionName);
            }
        }

        function += sampler.getWhileLoop(currentLoopCount);
        function += sampler.getWhileEnd(currentLoopCount);

        return null;
    }

    
    // Gets the position of the variable in the stack
    public int getFromStack(String arg, String functionName) {
        ArrayList<SimpleNode> arr = stack.get(functionName);

        for (int i = 0; i < arr.size(); i++) {

            if (arr.get(i) != null && arr.get(i).getValue().equals(arg)) {
                return i;
            }

        }
        return -1;
    }

}