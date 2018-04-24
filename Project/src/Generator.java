public class Generator {

    private Sampler sampler;
    private String currentFunctionName;

    public Generator(Sampler sampler) {
        this.sampler = sampler;
    }  

    public Object visit(SimpleNode node) {
        System.out.println("PREFIX: " + node.toString());

        if(node.toString().equals("Module")){
            visit((ASTModule) node);
        }
        else if(node.toString().equals("Function")){
            System.out.println("Function");
            visit((ASTFunction) node);
        }
        else if(node instanceof ASTVar) {

        }

        return null;
    
    }

    // Module Node
    public Object visit(ASTModule node) {

        // prints Module yasmin code
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
        if(node.functionName.equals("main")){
            // if the function is the main one
            sampler.functionBegin(node.functionName, null);
        }
        else {
            // If there is no Parameters (Var)
            if(node.jjtGetChild(childN).jjtGetNumChildren() == 0){
                System.out.println("No Params");
                sampler.functionBegin(node.functionName, null);
            }
            else {
                // Get types of vars
                String[] vars = (String[]) visit((ASTVarList) node.jjtGetChild(0)); 
                sampler.functionBegin(node.functionName, vars);
            }
            childN++; 
        }

       
        // prints yasmin Function endline
        sampler.functionEnd();

        System.out.println("Function: " + node.functionName);
        return null;
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

    
}