import java.util.ArrayList;
import java.util.HashMap;

public class Generator {

    private Sampler sampler;
    private SymbolTable table;
    private String moduleName;

    private int stackLimit;
    private int stackMax;
    private int localLimit;
    private String function;

    private int stackDeclaration;
    private int localDeclaration;

    private String clinitCode;

    private int loopCount;

    private HashMap<String, ArrayList<SimpleNode>> stack;
    private HashMap<String, String> globalVariables;

    public Generator(Sampler sampler, SymbolTable table) {
        this.sampler = sampler;
        this.table = table;
        this.function = "";
        stack = new HashMap<>();
        globalVariables = new HashMap<String,String>();
        stackLimit = 0;
        stackMax = 0;
        localLimit = 0;
        loopCount = -1;

        stackDeclaration = 0;
        localDeclaration = 0;
        clinitCode = "";
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

        sampler.printClinit(clinitCode, stackDeclaration, localDeclaration);
        sampler.close();
        return null;
    }

    public Object visit(ASTDeclaration node) {
        System.out.println("DECLARATION");

        if(node.jjtGetNumChildren() == 0){

            sampler.printStaticField(node.getValue(), Utils.SCALAR, node.getAssignedValue());
            if(globalVariables.get(node.getValue()) == null){
                globalVariables.put(node.getValue(), Utils.SCALAR);
            }
        }
        else {
            // array initialization
            sampler.printStaticField(node.getValue(), Utils.ARRAY, null);
            localDeclaration++;

            if(globalVariables.get(node.getValue()) == null){
                globalVariables.put(node.getValue(), Utils.ARRAY);
            }
            SimpleNode chil = (SimpleNode) node.jjtGetChild(0);
            processDeclaration(node.getValue(), chil);
        }
        return null;
    }

    public void processDeclaration(String variableName, SimpleNode node){

        if (node.getType().equals(Utils.ARRAY_INST)) { 
            stackDeclaration++;
            clinitCode += sampler.getConst(node.getValue(), false) + "\n";
            clinitCode += sampler.getNewArray();
            clinitCode += sampler.getStoreStatic(this.moduleName, variableName, Utils.ARRAY) + "\n";
        }
    }

    // Function Node
    public Object visit(ASTFunction node) {

        System.out.println("FUNCTION: " + node.getValue());
        String[] vars = null;

        if (node.functionName.equals("main")) {
            // if the function is the main one
            sampler.functionBegin(node.getValue(), Utils.VOID, null);

            //ocupy first position of stack
            ArrayList<SimpleNode> arr = new ArrayList<SimpleNode>();
            arr.add(null);
            stack.put("main", arr);

        } else {
            // TODO - CHECK THIS
            if(node.jjtGetChild(0).toString().equals("VarList")){
                if (node.jjtGetChild(0).jjtGetNumChildren() == 0) {
                    // If there is no Parameters (Var)
                    sampler.functionBegin(node.getValue(), node.getReturnType(), null);
                } else {
                    // Get types of vars
                    vars = (String[]) visit((ASTVarList) node.jjtGetChild(0), node.getValue());
                    sampler.functionBegin(node.getValue(), node.getReturnType(), vars);
                }

            } else {

                if (node.jjtGetChild(1).jjtGetNumChildren() == 0) {
                    // If there is no Parameters (Var)
                    sampler.functionBegin(node.getValue(), node.getReturnType(), null);
                } else {
                    System.out.println("HERE!!!");
                    // Get types of vars
                    vars = (String[]) visit((ASTVarList) node.jjtGetChild(1), node.getValue());
                    sampler.functionBegin(node.getValue(), node.getReturnType(), vars);
                }
            }
            
        }

        
        localLimit = table.getSymbolTrees().get(node.getValue()).size();
        
        if(node.getValue().equals("main")){
            localLimit++;
        }

        // Checks the other children of the function
        for (int i = 1; i < node.jjtGetNumChildren(); i++) {
            System.out.println("\nFUNCTION CHILDREN: " + node.jjtGetChild(i).toString());
            checkFunctionChildren((SimpleNode) node.jjtGetChild(i), node.getValue());
        }

        // update stack Limit
        if(stackMax < stackLimit){
            stackMax = stackLimit;
        }

        sampler.printLocalsLimit(localLimit);
        sampler.printStackLimit(stackMax);
        sampler.printString(function);

        stackLimit = 0;
        stackMax = 0;
        localLimit = 0;
        function = "";
        loopCount = -1;

        // prints return types
        if (node.getReturnType().equals(Utils.VOID)) {
            sampler.printVoidReturn();
        } else {
            int numStack = getFromStack(node.getReturnValue(), node.getValue());
            if(numStack != -1){
                sampler.printLoad(numStack);
                sampler.printIntReturn();
            }
            else {
                String type = globalVariables.get(node.getReturnValue());
                if(type != null){
                    function += sampler.getLoadStatic(this.moduleName, node.getReturnValue(), type) + "\n";
                }
            }
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

        if(node.jjtGetNumChildren() > 0){
            
            for (int i = 0; i < node.jjtGetNumChildren(); i++) {

                if (stack.get(functionName) == null) {
                    ArrayList<SimpleNode> arr = new ArrayList<SimpleNode>();
                    arr.add((SimpleNode) node.jjtGetChild(i));
                    stack.put(functionName, arr);
                } else{
                    stack.get(functionName).add((SimpleNode) node.jjtGetChild(i));
                }
            }

        }
        else {
            if (stack.get(functionName) == null) {
                ArrayList<SimpleNode> arr = new ArrayList<SimpleNode>();
                arr.add(node);
                stack.put(functionName, arr);
            } else
                stack.get(functionName).add(node);
    
            return node.getType();
        }

        return node.getType();
    }

    public Object visit(ASTCall node, String currentFunctionName) {

        String[] params = null;

        if (node.jjtGetNumChildren() > 0){

            // if there are parameters to the function call
            SimpleNode varList = (SimpleNode) node.jjtGetChild(0);

            params = new String[varList.jjtGetNumChildren()];

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
                    localLimit++;
                    params[i] = "I";
                    function += sampler.getConst(arg.content, false) + "\n";

                } else {

                    if(arg.content.contains("\"")){
                        // if the content is a string
                        stackLimit++;
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
        
                            // check type of parameters
                            if (type.equals(Utils.SCALAR)) {
                                params[i] = "I";
                            } else if (type.equals(Utils.ARRAY))
                                params[i] = "[I";
        
                        } else {

                            String type = globalVariables.get(arg.content);
                            if(type != null){
                                function += sampler.getLoadStatic(this.moduleName, arg.content, type) + "\n";
                                if(type.equals("Array")){
                                    params[i] = "[I";
                                }
                                else {
                                    params[i] = "I";
                                }
                            }
                        }
                    }
                }
            }
        }

        // go check the type of return of the function
        String returnType;
        String moduleString = this.moduleName;
        if (!node.getValue().equals("println") && !node.getValue().equals("print")) {

            ASTFunction function = this.table.getFunction(node.getValue()); 

            if (function == null) {
                // It's from another module
                moduleString = node.getPackage();
                returnType = "I";
            }
            else {
                //params = (String[]) visit((ASTVarList) function.jjtGetChild(0), currentFunctionName);
                if (function.getReturnType().equals(Utils.VOID)) {
                    returnType = "V";
                } else{
                    returnType = "I";
                }
            }

        } else {
            moduleString = node.getPackage();
            returnType = "V";
        }


        System.out.println("FUNCTION CALL: " + node.getValue());
        function += sampler.getFunctionInvocation(moduleString, node.getValue(), params, returnType) + "\n";

        return null;
    }

    public Object visit(ASTAssign node, String functionName) {
        // RHS
        ASTRhs rhs = (ASTRhs) node.jjtGetChild(1);
        
        SimpleNode lhs = (SimpleNode) node.jjtGetChild(0);
        // LHS
        SimpleNode previousNode = Utils.containsValue(table.getSymbolTrees().get(functionName), (SimpleNode) node.jjtGetChild(0));
        if (previousNode == null){
            previousNode = Utils.containsValue(table.getDeclarations(), (SimpleNode) node.jjtGetChild(0));
        }
        
        if (previousNode != null) {
            lhs.setType(previousNode.getType());
            lhs.setInitialization(previousNode.isInitialized());
        }

        System.out.println("LHS: string " + lhs.toString() + " value " + lhs.getValue() + " type " + lhs.getType() + " is Init "+ lhs.isInitialized());
        
        if(lhs.getType().equals(Utils.ARRAY)){
            if(checkArrayInstantiation(lhs, rhs, functionName)) {
                return null;
            }
        }

        if(lhs.toString().equals("ArrayAccess")){
            
            ASTArrayAccess lhsArr = (ASTArrayAccess) lhs;

            int numStack = getFromStack(lhs.getValue(), functionName);
            if(numStack != -1){
                // prints commands
                function += sampler.getLoad(numStack, Utils.ARRAY) + "\n";
    
                numStack = getFromStack(lhsArr.getIndex(), functionName);
                function += sampler.getLoad(numStack, Utils.SCALAR) + "\n";
            }
            else {
                String type = globalVariables.get(lhs.getValue());
                if(type != null){
                    function += sampler.getLoadStatic(this.moduleName, lhs.getValue(), type) + "\n";
                }
            }
        }
        
        //Process RHS
        boolean[] answer = (boolean[]) visit(rhs, functionName);
        boolean wasArray = answer[0];
        boolean isInc = answer[1];


        // print operator
        if (rhs.getValue() != null && !isInc){
            function += sampler.getOperator(rhs.getValue()) + "\n";
            
        }

        // LHS
        System.out.println("LHS: " + lhs.toString());

        // check if arrayAccess or not
        if(lhs.toString().equals("ArrayAccess")){

            function += sampler.getIStore();
            stackLimit++;

        }
        else if(!isInc){

            int numStack = getFromStack(lhs.getValue(), functionName);

            if(numStack != -1){
                if(wasArray){
                    function += sampler.getStore(numStack, Utils.ARRAY) + "\n\n";
                }
                else{
                    function += sampler.getStore(numStack, Utils.SCALAR) + "\n\n";
                }
            }
            else { // TODO: verify this
                // check if global variable

                String type = globalVariables.get(lhs.getValue());
                if(type != null){

                    function += sampler.getStoreStatic(this.moduleName, lhs.getValue(), type) + "\n";

                }
                else {
                    // not in stack, so add
                    if (stack.get(functionName) == null) {
                        ArrayList<SimpleNode> arr = new ArrayList<SimpleNode>();
                        arr.add(lhs);
                        stack.put(functionName, arr);
                    } else {

                        stack.get(functionName).add(lhs);
                    }

                    numStack = getFromStack(lhs.getValue(), functionName);
                    if(wasArray){
                        function += sampler.getStore(numStack, Utils.ARRAY) + "\n\n";
                    }
                    else{
                        function += sampler.getStore(numStack, Utils.SCALAR) + "\n\n";
                    }
                }
            }
        }

        //Update stack Max
        if(stackMax < stackLimit){
            stackMax = stackLimit;
        }
        stackLimit = 0;

        return null;
    }

    public Object visit(ASTRhs rhs, String functionName){

        boolean wasArray = false;
        boolean isInc = false;
        System.out.println("RHS");

        if(checkIfInc(rhs, functionName)){
            isInc = true;
            boolean[] answer = {wasArray, isInc};
            return answer;
        }

        for (int i = 0; i < rhs.jjtGetNumChildren(); i++) {
            
            SimpleNode chil = (SimpleNode) rhs.jjtGetChild(i);

            System.out.println("RHS CHILD " + chil.toString());

            if(chil.jjtGetNumChildren() == 0 && chil.toString().equals(Utils.TERM)){
                stackLimit++;
                boolean isNegative = ((ASTTerm) chil).getNegative();
                function += sampler.getConst(chil.getValue(), isNegative) + "\n";
            }
            else if(chil.toString().equals("ArrayInstantion")) {

                try {
                    Integer.parseInt(chil.getValue());
                    function += sampler.getConst(chil.getValue(), false) + "\n";

                } catch (NumberFormatException e) {
                    // array instatiation
                    int numStack = getFromStack(chil.getValue(), functionName);
                    if(numStack != -1){
                        function += sampler.getLoad(numStack, Utils.SCALAR) + "\n";
        
                        //stack.get(functionName).add(chil);                    
                    }
                    else {
                        String type = globalVariables.get(chil.getValue());
                        if(type != null){
                            function += sampler.getLoadStatic(this.moduleName, chil.getValue(), type) + "\n";
                        }
                    }
                }
                

                function += sampler.getNewArray();
                wasArray = true;
            }
            else {

                for (int a = 0; a < chil.jjtGetNumChildren(); a++) {

                    SimpleNode term = (SimpleNode) chil.jjtGetChild(a);

                    System.out.println("Term: " + term.toString() + " : " + term.getValue());

                    if(term.toString().equals("ArrayAccess")){
                        // If RHS is an array access

                        stackLimit += 2;

                        ASTArrayAccess arrAcc = (ASTArrayAccess) term;

                        System.out.println("ARRAY ACCESS " + arrAcc.getValue());
                        
                        int numStack = getFromStack(arrAcc.getValue(), functionName);
                        if(numStack != -1){
                            function += sampler.getLoad(numStack, Utils.ARRAY) + "\n";
                        }
                        else {
                            String type = globalVariables.get(arrAcc.getValue());
                            if(type != null){
                                function += sampler.getLoadStatic(this.moduleName, arrAcc.getValue(), type) + "\n";
                            }
                        }

                        try {
                            Integer.parseInt(arrAcc.getIndex());
                            function += sampler.getConst(arrAcc.getIndex(), false) + "\n";

                        } catch (NumberFormatException e) {
                            numStack = getFromStack(arrAcc.getIndex(), functionName);
                            if(numStack != -1){
                                function += sampler.getLoad(numStack, Utils.SCALAR)  + "\n";
                            }
                            else {
                                String type = globalVariables.get(arrAcc.getIndex());
                                if(type != null){
                                    function += sampler.getLoadStatic(this.moduleName, arrAcc.getIndex(), type) + "\n";
                                }
                            }
                        }

                        function += sampler.getILoad();

                    }
                    else if (term.toString().equals(Utils.CALL)) {
                        // If RHS is a function call

                        visit((ASTCall) chil.jjtGetChild(a), functionName);

                    } else if (term.toString().equals("ScalarAccess")) {
                        
                        if(term.getType().equals(Utils.SIZE)){

                            //if it is an access to an array size                            
                            int numStack = getFromStack(term.getValue(), functionName);
                            if(numStack != -1){
                                function += sampler.getLoad(numStack, Utils.ARRAY) + "\n";
                            } 
                            else {
                                String type = globalVariables.get(term.getValue());
                                if(type != null){
                                    function += sampler.getLoadStatic(this.moduleName, term.getValue(), type) + "\n";
                                }
                            }

                            function += sampler.getArraySize();
                            
                        }
                        else {
                            // Scalar 
                            int numStack = getFromStack(term.getValue(), functionName);
                            stackLimit++;
                            
                            if(numStack != -1){
                                function += sampler.getLoad(numStack, term.getType()) + "\n";
                            }
                            else {
                                String type = globalVariables.get(term.getValue());
                                if(type != null){
                                    function += sampler.getLoadStatic(this.moduleName, term.getValue(), type) + "\n";
                                }
                            }
                        }
                    }
                }
            }
        }

        boolean[] answer = {wasArray, isInc};
        return answer;
    }

    // Checks if it is an Inc command
    public boolean checkIfInc(ASTRhs rhs, String functionName){

        int numStack = -1;
        String constNum = "";

        SimpleNode parent = (SimpleNode) rhs.jjtGetParent();
        SimpleNode sibling = (SimpleNode) parent.jjtGetChild(0);
        
        if(rhs.jjtGetNumChildren() > 0 && rhs.getValue() != null && parent.toString().equals("Assign")
            && sibling.toString().equals("ScalarAccess")) {
            
            if(!rhs.getValue().equals("+")){
                return false;
            }

            for(int i = 0; i < rhs.jjtGetNumChildren(); i++){
                
                SimpleNode chil = (SimpleNode) rhs.jjtGetChild(i);

                // if there is more than one term
                if(chil.jjtGetNumChildren() > 0 && chil.toString().equals(Utils.TERM)){

                    for (int a = 0; a < chil.jjtGetNumChildren(); a++) {

                        SimpleNode term = (SimpleNode) chil.jjtGetChild(a);

                        //if it is a scalar access and not the size of an array
                        if(term.toString().equals("ScalarAccess") && !term.getType().equals(Utils.SIZE)){

                            // if it isn't equal to the lhs term
                            if(!term.getValue().equals(sibling.getValue())){
                                return false;
                            }

                            int num = getFromStack(term.getValue(), functionName);

                            if(num != -1){
                                numStack = num;
                            }
                        }
                    }
                }
                else {
                    //const value
                    constNum = chil.getValue();
                }
            }
        }

        if(constNum != "" && numStack != -1){
            function += sampler.getInc(numStack, constNum) + "\n";
            return true;
        }
        
        return false;

    }

    public boolean checkArrayInstantiation(SimpleNode lhs, ASTRhs rhs, String functionName){

        SimpleNode rhsChild = (SimpleNode) rhs.jjtGetChild(0);
        System.out.println("NODE: " + rhsChild + " : " + rhsChild.getValue());
        // its a constant
        if(rhsChild.toString().equals("ArrayInstantion")){
            return false;
        }
        if(rhsChild.getValue() != null){
            
            int numStack = stack.get(functionName).size();
            function += sampler.getConst("0", false) + "\n";
            function += sampler.getStore(numStack, Utils.SCALAR) + "\n";

            loopCount++;
            function += sampler.getWhileBegin(loopCount) + "\n";
            function += sampler.getLoad(numStack, Utils.SCALAR) + "\n";
            function += sampler.getLoadStatic(this.moduleName, lhs.getValue(), Utils.ARRAY) + "\n";
            function += sampler.getArraySize() + "\n";
            function += sampler.getIfStart("<", loopCount) + "\n";
            function += sampler.getLoadStatic(this.moduleName, lhs.getValue(), Utils.ARRAY) + "\n";
            function += sampler.getLoad(numStack, Utils.SCALAR) + "\n";
            function += sampler.getConst(rhsChild.getValue(), false); // verificar isto depois
            function += sampler.getIStore() + "\n";
            function += sampler.getInc(numStack, "1") + "\n";
            function += sampler.getWhileLoop(loopCount) + "\n";
            function += sampler.getWhileEnd(loopCount) + "\n";
            return true;
        }
        else {
            SimpleNode var = (SimpleNode) rhsChild.jjtGetChild(0);
            System.out.println("NOT A CONST: " +var.getValue());

            int numStack = stack.get(functionName).size();
            function += sampler.getConst("0", false) + "\n";
            function += sampler.getStore(numStack, Utils.SCALAR) + "\n";

            loopCount++;
            function += sampler.getWhileBegin(loopCount) + "\n";
            function += sampler.getLoad(numStack, Utils.SCALAR) + "\n";
            function += sampler.getLoadStatic(this.moduleName, lhs.getValue(), Utils.ARRAY) + "\n";
            function += sampler.getArraySize() + "\n";
            function += sampler.getIfStart("<", loopCount) + "\n";
            function += sampler.getLoadStatic(this.moduleName, lhs.getValue(), Utils.ARRAY) + "\n";
            function += sampler.getLoad(numStack, Utils.SCALAR) + "\n";

            //get variable
            int numStackVar = getFromStack(var.getValue(), functionName);
            function += sampler.getLoad(numStackVar, Utils.SCALAR) + "\n"; // verificar isto depois
            function += sampler.getIStore() + "\n";
            function += sampler.getInc(numStack, "1") + "\n";
            function += sampler.getWhileLoop(loopCount) + "\n";
            function += sampler.getWhileEnd(loopCount) + "\n";
            return true;
        }
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


        SimpleNode lhs = (SimpleNode) node.jjtGetChild(0);
        int numStack = getFromStack(lhs.getValue(), functionName);
        if(numStack != -1){
            stackLimit++;
            function += sampler.getLoad(numStack, lhs.getType()) + "\n";
        }
        else {
            String type = globalVariables.get(lhs.getValue());
            if(type != null){
                function += sampler.getLoadStatic(this.moduleName, lhs.getValue(), type) + "\n";
            }
        }

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

        if(arr == null)
            return -1;

        for (int i = 0; i < arr.size(); i++) {

            if (arr.get(i) != null) {
                if(arr.get(i).getValue().equals(arg)) {
                    return i;
                }
            }

        }
        return -1;
    }

}