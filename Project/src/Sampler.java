import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.util.HashMap;

public class Sampler {

    private HashMap<String,String> arith;
    private PrintWriter pw;
    private int lineN;

    public Sampler(String filename) {
        arith = new HashMap<String, String>();
        lineN = 1;

        try {
            pw = new PrintWriter(filename);
        }
        catch(FileNotFoundException e) {
            e.printStackTrace();
        }

        arith.put("+", "iadd");
        arith.put("-", "isub");
        arith.put("*", "imul");
        arith.put("/", "idiv");
        arith.put("^", "ixor");
        arith.put("&", "iand");
        arith.put("|", "ior");
        //missing some

    }

    public void print(String s) {
        pw.print(s);
    }

    public void println(String s){
        pw.println(s);
        lineN++;
    }

    public void startModule(String moduleName) {
        print(".class public "); println(moduleName);
        println(".super java/lang/Object");
        println("");
        println("");
    }

    public void functionBegin(String functionName, String[] params) {
        if(functionName.equals("main")){
            println(".method public static main([Ljava/lang/String;)V");
        }
        else if(params == null){
            println(".method public static " + functionName + "()I");
            //missing stack
        }
        else {
            print(".method public static " + functionName + "(");
            for(int i = 0; i < params.length; i++){
                if(params[i].equals("Scalar"))
                    print("I");
                else if(params[i].equals("Array"))
                    print("[I");
            }
            println(")I");
            
        }
    }

    public void functionEnd() {
        println(".end method");
        println("");
        println("");
    }

    public void close() {
        pw.flush();
    }
}