public class Symbol {
    private int scope;
    private boolean isInitialized;

    private SimpleNode value;
    
    public Symbol(SimpleNode value, int scope) {
        this.value = value; 
        this.scope = scope;
        isInitialized = false;
    }

    public int getScope() {
        return scope;
    }

    public SimpleNode getValue() {
        return value;
    }

    public String getType() {
        return value.getType();
    }
}