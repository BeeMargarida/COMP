public class Symbol {
    private int scope;
    private Token value;
    private boolean isScalar;

    public Symbol(Token value, boolean isScalar, int scope) {
        this.value = value; 
        this.isScalar = isScalar;
        this.scope = scope;
    }

    public int getScope() {
        return scope;
    }

    public Token getValue() {
        return value;
    }

    public boolean getIsScalar() {
        return isScalar;
    }
}