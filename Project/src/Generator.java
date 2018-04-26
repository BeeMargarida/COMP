import java.util.ArrayList;
import java.util.HashMap;

public class Generator {

    private Sampler sampler;
    private SymbolTable table;
    private String moduleName;

    private HashMap<String, ArrayList<SimpleNode>> stack;

    public Generator(Sampler sampler, SymbolTable table) {
        this.sampler = sampler;
        this.table = table;
        stack = new HashMap<>();
    }  

    public Object visit(SimpleNode node) {

        if(node.toString().equals("Module")){
            visit((ASTModule) node);
        }
        else if(node.toString().equals("Function")){
            visit((ASTFunction) node);
        }
        // TODO: Make Declarations

        return null;
    
    }

    // Module Node
    public Object visit(ASTModule node) {

        // prints Module yasmin code
        this.moduleName = node.name;
        sampler.startModule(node.name);

        // visits Module nodes
        for(int i = 0; i < node.jjtGetNumChildren(); i++){
            SimpleNode n = (SimpleNode) node.jjtGetChild(i);
            n.generatorVisit(this);
        }

        sampler.printClinit();
        sampler.close();
        return null;
    }

    // Function Node
    public Object visit(ASTFunction node) {

        System.out.println("FUNCTION: " + node.getValue());

        for(int i = 0; i < node.jjtGetNumChildren(); i++){
            SimpleNode s = (SimpleNode) node.jjtGetChild(i);
        }

        if(node.functionName.equals("main")){
            // if the function is the main one
            sampler.functionBegin(node.functionName, null);
        }
        else {
            // If there is no Parameters (Var)
            if(node.jjtGetChild(0).jjtGetNumChildren() == 0){
                sampler.functionBegin(node.functionName, null);
            }
            else {
                // Get types of vars
                String[] vars = (String[]) visit((ASTVarList) node.jjtGetChild(0), node.functionName); 
                sampler.functionBegin(node.functionName, vars);
            }
        }

        // Print stack and locals values
        // TODO : GET STACK AND LOCAL LIMIT VALUES
        sampler.printLocalsLimit(0);
        sampler.printStackLimit(0);


        // Checks the other children of the function
        for(int i = 1; i < node.jjtGetNumChildren(); i++){
            checkFunctionChildren((SimpleNode) node.jjtGetChild(i), node.functionName);
        }


        //prints return types
        if(node.getReturnType().equals(Utils.VOID)){
            sampler.printVoidReturn();
        }
        else {
            int numStack = getFromStack(node.getReturnValue(), node.functionName);
            sampler.printLoad(numStack);
            sampler.printIntReturn();
        }
        // prints yasmin Function endline
        sampler.functionEnd();

        return null;
    }

    public void checkFunctionChildren(SimpleNode node, String functionName){
        if(node.toString().equals("Call")){
            visit((ASTCall) node, functionName);
        }
        else if(node.toString().equals("Assign")){
            visit((ASTAssign) node, functionName);
        }
    }

    // gets all the types of vars
    public Object visit(ASTVarList node, String functionName) {
        int numChildren = node.jjtGetNumChildren();
        String[] vars = new String[numChildren];

        for(int i = 0; i < numChildren; i++){
            vars[i] = visit((ASTVar) node.jjtGetChild(i), functionName);
        }
        return vars;
    }

    // returns type of Var
    public String visit(ASTVar node, String functionName) {

        if(stack.get(functionName) == null){
            ArrayList<SimpleNode> arr = new ArrayList<SimpleNode>();
            arr.add(node);
            stack.put(functionName, arr);
        }
        else
            stack.get(functionName).add(node);

        return node.getType();
    }

    public Object visit(ASTCall node, String currentFunctionName) {

        SimpleNode varList = (SimpleNode) node.jjtGetChild(0);

        String[] params = new String[varList.jjtGetNumChildren()];

        // Get parameters of the function called
        for(int i = 0; i < varList.jjtGetNumChildren(); i++){
            ASTArgument arg = (ASTArgument) varList.jjtGetChild(i);

            //checks type of parameters passed
            boolean isInteger;
            try {
                Integer.parseInt(arg.content);
                isInteger = true;
            }
            catch(NumberFormatException e) {
                isInteger = false;
            }

            // check if parameter is integer or a variable
            if(isInteger){
                // if parameter is a integer
                params[i] = "I";
                sampler.printConst(arg.content);
            }
            else {
                // go check the stack and do iload_<number> and its type
                int numStack = getFromStack(arg.content, currentFunctionName);
                if(numStack != -1){
                    sampler.printLoad(numStack);

                    // check type of parameters - TODO: Make this more readable
                    if(stack.get(currentFunctionName).get(numStack).getType().equals(Utils.SCALAR)){
                        params[i] = "I";
                    }
                    else if(stack.get(currentFunctionName).get(numStack).getType().equals(Utils.ARRAY))
                        params[i] = "[I";
                }
                else {
                    System.out.println("Not in stack");
                }
            }
        }

        // go check the type of return of the function
        String returnType;
        if(!node.getValue().equals("println")){
            ASTFunction function = this.table.getFunction(node.getValue());
            if(function == null){
                System.out.println("Function doesn't exist");
                return -1;
            }
            
            if(function.getReturnType().equals(Utils.VOID)){
                returnType = "V";
            }
            else
                returnType = "I";
        }
        else {
            returnType = "V";
        }
        
        // prints function invocation
        sampler.printFunctionInvocation(this.moduleName, node.getValue(), params, returnType);

        return null;
    }

    public Object visit(ASTAssign node, String functionName) {
        
        boolean isCall = false;

        // RHS
        ASTRhs rhs = (ASTRhs) node.jjtGetChild(1);
        
        for(int i = 0; i < rhs.jjtGetNumChildren(); i++) {

            SimpleNode chil = (SimpleNode) rhs.jjtGetChild(i);

            for(int a = 0; a < chil.jjtGetNumChildren(); a++) {

                SimpleNode term = (SimpleNode) chil.jjtGetChild(a); 

                // If RHS is a function call
                if(term.toString().equals(Utils.CALL)){
                    visit((ASTCall) chil.jjtGetChild(a), functionName);
                    isCall = true;
                }
                else if(term.toString().equals("ScalarAccess")){
                    // Scalar or Array Access                  
                    int numStack = getFromStack(term.getValue(), functionName);
                    sampler.printLoad(numStack);
                }
            }
        }

        // print operator
        if(!isCall)
            sampler.printOperator(rhs.getValue());

        // LHS
        SimpleNode lhs = (SimpleNode) node.jjtGetChild(0);

        // add to stack
        if(stack.get(functionName) == null){
            ArrayList<SimpleNode> arr = new ArrayList<SimpleNode>();
            arr.add(lhs);
            stack.put(functionName, arr);
        }
        else {
            stack.get(functionName).add(lhs);
        }

        //print store of Lhs
        int numStack = getFromStack(lhs.getValue(), functionName);        
        sampler.printStore(numStack);

        return null;
    }

    public int getFromStack(String arg, String functionName) {
        ArrayList<SimpleNode> arr = stack.get(functionName);
        for(int i = 0; i < arr.size(); i++){
            if(arr.get(i).getValue().equals(arg)){
                return i;
            }
        }
        return -1;
    }

    
}