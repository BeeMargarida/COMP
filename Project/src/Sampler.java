import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.util.HashMap;

public class Sampler {

    private HashMap<String,String> arith;
    private HashMap<String,String> compare;
    private PrintWriter pw;
    private int lineN;

    public Sampler(String filename) {
        arith = new HashMap<String, String>();
        compare = new HashMap<String, String>();
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
        arith.put(">>", "ishr");
        arith.put("<<", "ishl");
        //missing some

        compare.put("==", "icmpne");
        compare.put("!=", "icmpeq");
        compare.put("<", "icmpge");
        compare.put("<=", "icmpgt");
        compare.put(">", "icmple");
        compare.put(">=", "icmplt");


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

    public void printStaticField(String name, String type){
        if(type.equals(Utils.ARRAY)){
            println(".field static " + name + " [I");
        }
        else if(type.equals(Utils.SCALAR)){
            println(".field static " + name + " I");
        }
    }

    public void functionBegin(String functionName, String returnType, String[] params) {

        String returnT = "I";
        if(returnType.equals(Utils.VOID)){
            returnT = "V";
        }

        if(functionName.equals("main")){
            println(".method public static main([Ljava/lang/String;)" + returnT);
        }
        else if(params == null){
            println(".method public static " + functionName + "()" + returnT);
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
            println(")" + returnT);
            
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
        if(Integer.parseInt(arg) >= 10){
            return "bipush " + arg;
        } else {
            return "iconst_" + arg;
        }
    }

    public void printLoad(int arg){
        if(arg <= 3)
            println("iload_" + arg);
        else 
            println("iload " + arg);
    }

    public String getLoad(int arg, String type){
        if(type.equals(Utils.ARRAY)){

            if(arg <= 3)
                return "aload_" + arg;
            else 
                return "aload " + arg;
        }
        else {

            if(arg <= 3)
                return "iload_"+arg;
            else 
                return "iload "+arg;
        }
    }

    public String getStore(int arg, String type){
        if(type.equals(Utils.ARRAY)){
            return "astore_" + arg;
        }
        return "istore_"+arg+"\n";
    }

    public String getOperator(String op){
        return "" + arith.get(op);
    }

    public String getNewArray(){
        return "newarray int\n";
    }

    public String getIfStart(String comp, int loopN){
        return "if_" + compare.get(comp) + " loop" + loopN + "_end\n\n";
    }

    public String getIfGotoElse(int loopN){
        return "goto loop" + loopN + "_next\n";
    }

    public String getIfElse(int loopN){
        return "loop" + loopN + "_end:\n";
    }

    public String getIfEnd(int loopN, boolean hasElse){
        if(hasElse){
            return "loop" + loopN + "_next:\n";
        }
        return  "loop" + loopN + "_end:\n";
    }

    public String getWhileBegin(int loopN){
        return "loop" + loopN + ":\n\n";
    }

    public String getWhileLoop(int loopN){
        return "goto loop" + loopN + "\n\n";
    }

    public String getWhileEnd(int loopN){
        return "loop" + loopN + "_end:\n\n";
    }

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