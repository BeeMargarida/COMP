import java.util.ArrayList;

public class SymbolTable {

    public ArrayList<Integer> scalars;
    public ArrayList<int[]> arrays;

    public SymbolTable() {
        scalars = new ArrayList<>();
        arrays = new ArrayList<>();
    }

    public boolean lookup(Token t, boolean isScalar) {
        if (isScalar)
            return scalars.contains(t.image);
        else
            return arrays.contains(t.image);
    }
}