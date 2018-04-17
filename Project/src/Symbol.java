public class Symbol {
    private int scope;
    private SimpleNode value;
    
    public Symbol(SimpleNode value, int scope) {
        this.value = value; 
        this.scope = scope;
    }

    public int getScope() {
        return scope;
    }

    public SimpleNode getValue() {
        return value;
    }
}