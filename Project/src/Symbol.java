public class Symbol {
    private String type;

    private SimpleNode value;
    
    public Symbol(SimpleNode value) {
        this.value = value; 
        type = value.getType();
    }

    public SimpleNode getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

}