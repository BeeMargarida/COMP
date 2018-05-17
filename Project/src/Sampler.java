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

    public void printLocalsLimit(int limit){
        println(".limit locals " + limit);
    }

    public void printStackLimit(int limit){
        println(".limit stack " + limit);
    }

    /*public void printConst(String arg){
        println("iconst_" + arg);
    }*/

    public String getConst(String arg){
        return "iconst_" + arg;
    }

    /*public void printLoad(int arg){
        println("iload_" + arg);
    }*/

    public String getLoad(int arg){
        return "iload_"+arg;
    }

    /*public void printStore(int arg){
        println("istore_" + arg);
        println("");
    }*/

    public String getStore(int arg){
        return "istore_"+arg+"\n";
    }

    /*public void printOperator(String op) {
        println(arith.get(op));
    }*/

    public String getOperator(String op){
        return "" + arith.get(op);
    }

    /*public void printFunctionInvocation(String moduleName, String functionName, String[] params, String returnType){
        print("invokestatic " + moduleName+ "/" + functionName + "(");
        for(int i = 0; i < params.length; i++){
            print(params[i]);
        }   
        println(")" + returnType);
        println("");
    }*/

    public String getFunctionInvocation(String moduleName, String functionName, String[] params, String returnType){
        String res = "";
        res += "invokestatic " + moduleName+ "/" + functionName + "(";
        for(int i = 0; i < params.length; i++){
            res += params[i];
        }   
        res += ")" + returnType + "\n";
        return res;
    }

    public void printVoidReturn() {
        println("return");
    }
    
    public void printIntReturn(){
        println("ireturn");
    }

    public void printString(String function){
        println(function);
    }

    public void functionEnd() {
        println(".end method");
        println("");
        println("");
    }

    public void printClinit() {
        println(".method static public <clinit>()V");
        println(".limit stack 0");
        println(".limit locals 0");
        println("return");
        println(".end method");
    }

    public void close() {
        pw.flush();
    }
}