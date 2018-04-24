public class Generator {

    private Sampler sampler;

    public Generator(Sampler sampler) {
        this.sampler = sampler;
    }

    public void visit(SimpleNode node) {
        //System.out.println("PREFIX: " + prefix);
        if(node.toString().equals("Module")){
            System.out.println("Fack" + node.toString());
            visit((ASTModule) node);
        }
        else if(node.toString().equals("Function")){

        }

        
    }


    public void visit(ASTModule node) {

        System.out.println("VAlue: " + node.name);
    }

    public void visitFunction(SimpleNode node) {

    }

    
}