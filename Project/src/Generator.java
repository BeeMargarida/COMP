public class Generator {

    private Sampler sampler;
    private String moduleName;

    public Generator(Sampler sampler) {
        this.sampler = sampler;
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
        System.out.println("Function: " + node.functionName);
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
        SimpleNode varList = (SimpleNode) node.jjtGetChild(0);

        for(int i = 0; i < node.jjtGetNumChildren(); i++){
            System.out.println("CHILDREN   AH: " + node.toString());
        }

        for(int i = 0; i < varList.jjtGetNumChildren(); i++){
            ASTArgument arg = (ASTArgument) varList.jjtGetChild(i);
            System.out.println("CHILDREN: " + arg.content);
            for(int a = 0; a < arg.jjtGetNumChildren(); a++){
                System.out.println("CHILDREN 2: " + arg.jjtGetChild(a).toString());
            }
        }
        //sampler.print("invokestatic " + this.moduleName+ "/" + node.calledFunction); //missing type of arguments and return

        return null;
    }

    public Object visit(ASTAssign node) {
        System.out.println("ASSIGN");
        return null;
    }



    
}