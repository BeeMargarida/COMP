public class Generator {

    private Sampler sampler;
    private SymbolTable table;
    private String moduleName;

    public Generator(Sampler sampler, SymbolTable table) {
        this.sampler = sampler;
        this.table = table;
    }  

    public Object visit(SimpleNode node) {
        System.out.println("PREFIX: " + node.toString());

        if(node.toString().equals("Module")){
            visit((ASTModule) node);
        }
        else if(node.toString().equals("Function")){
            visit((ASTFunction) node);
        }

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

        sampler.close();
        return null;
    }

    // Function Node
    public Object visit(ASTFunction node) {
        int childN = 0;
        System.out.println("Function: " + node.functionName + " RETURN: " + node.getReturnType());
        if(node.functionName.equals("main")){
            // if the function is the main one
            sampler.functionBegin(node.functionName, null);
        }
        else {
            // If there is no Parameters (Var)
            if(node.jjtGetChild(childN).jjtGetNumChildren() == 0){
                sampler.functionBegin(node.functionName, null);
            }
            else {
                // Get types of vars
                String[] vars = (String[]) visit((ASTVarList) node.jjtGetChild(0)); 
                sampler.functionBegin(node.functionName, vars);
            }
        }
        childN++; 

        // Print stack and locals values
        // TODO

        // Checks the other children of the function
        for(int i = childN; i < node.jjtGetNumChildren(); i++){
            checkFunctionChildren((SimpleNode) node.jjtGetChild(i));
        }


        //prints return types
        if(node.getReturnType().equals("Void")){
            sampler.printVoidReturn();
        }
        else
            sampler.printIntReturn();
        // prints yasmin Function endline
        sampler.functionEnd();

        return null;
    }

    public void checkFunctionChildren(SimpleNode node){
        if(node.toString().equals("Call")){
            visit((ASTCall) node);
        }
        else if(node.toString().equals("Assign")){
            visit((ASTAssign) node);
        }
    }

    // gets all the types of vars
    public Object visit(ASTVarList node) {
        int numChildren = node.jjtGetNumChildren();
        String[] vars = new String[numChildren];

        for(int i = 0; i < numChildren; i++){
            vars[i] = visit((ASTVar) node.jjtGetChild(i));
        }
        return vars;
    }

    // returns type of Var
    public String visit(ASTVar node) {
        System.out.println("Node: " + node.getValue() + " TYPE: " + node.getType());
        return node.getType();
    }

    public Object visit(ASTCall node) {
        System.out.println("CALL " + node.getValue());

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
                sampler.println("iconst_" + arg.content);
            }
            else {
                // go check the stack and do iload_<number> and its type
                
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
            
            if(function.getReturnType().equals("Void")){
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

    public Object visit(ASTAssign node) {
        System.out.println("ASSIGN");
        
        //Remove later
        for(int i = 0; i < node.jjtGetNumChildren(); i++) {
            System.out.println("ASSIGN NODE CHILDREN: " + node.jjtGetChild(i).toString());
        }

        // RHS
        ASTRhs rhs = (ASTRhs) node.jjtGetChild(1);
        
        for(int i = 0; i < rhs.jjtGetNumChildren(); i++) {

            System.out.println("RHS CHILDREN: " + rhs.jjtGetChild(i).toString());

            SimpleNode chil = (SimpleNode) rhs.jjtGetChild(i);

            for(int a = 0; a < chil.jjtGetNumChildren(); a++) {

                if(chil.jjtGetChild(a).toString().equals("Call")){
                    visit((ASTCall) chil.jjtGetChild(a));
                }
                else {
                    // Scalar or Array Access
                }
            }
        }

        // LHS
        //if(node.jjtGetChild(0).toString().equals())

        return null;
    }



    
}