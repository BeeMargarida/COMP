import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.util.HashMap;

import javax.lang.model.util.ElementScanner6;

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

    public void printStaticField(String name, String type, String assignedValue){
        if(type.equals(Utils.ARRAY)){
            println(".field static " + name + " [I");
        }
        else if(type.equals(Utils.SCALAR)){
            if(assignedValue != null){
                println(".field static " + name + " I = " + assignedValue);
            }
            else {
                println(".field static " + name + " I");
            }
        }
    }

    public void functionBegin(String functionName, String returnType, String[] params) {

        String returnT = "I";
        if(returnType.equals(Utils.VOID)){
            returnT = "V";
        }
        else if(returnType.equals("Array")){
            returnT = "[I";
        }

        if(functionName.equals("main")){
            println(".method public static main([Ljava/lang/String;)" + returnT);
        }
        else if(params == null){
            println(".method public static " + functionName + "()" + returnT);
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

    public String getConst(String arg, boolean isNegative){

        if(isNegative){
            
            if(Integer.parseInt(arg) > 32768){
                return "ldc " + "-" + arg;
            }
            else if(Integer.parseInt(arg) >= 129){               
                return "sipush " + "-" + arg;  
            }
            else if(Integer.parseInt(arg) > 1){                
                return "bipush " + "-" + arg;               
            }
            else{
                return "iconst_" + "m" + arg;
            }
        }
        else {
            if(Integer.parseInt(arg) > 32767){
                return "ldc " + arg;
            }
            else if(Integer.parseInt(arg) >= 128){
                return "sipush " + arg;
            }
            else if(Integer.parseInt(arg) > 5){
                return "bipush " + arg;
                
            }
            else {
                return "iconst_" + arg;
            }
        }
        
    }

    public void printLoad(int arg, String type){
        if(type.equals(Utils.ARRAY)){

            if(arg <= 3)
                println("aload_" + arg);
            else 
                println("aload " + arg);
        }
        else {

            if(arg <= 3)
                println("iload_"+arg);
            else 
                println("iload "+arg);
        }
    }

    public String getLdc(String content) {
        return "ldc " + content + "\n";
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
        if(arg <= 3){
            return "istore_"+arg+"\n";
        }
        else{
            return "istore "+arg+"\n";
        }
    }


    public String getOperator(String op){
        return "" + arith.get(op);
    }

    public String getInc(int stack, String num){
        return "iinc " + stack + " " + num;
    }

    public String getNewArray(){
        return "newarray int\n";
    }

    public String getArraySize() {
        return "arraylength\n";
    }

    public String getIStore(){
        return "iastore\n";
    }

    public String getILoad(){
        return "iaload\n";
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
        if(params != null){
            
            for(int i = 0; i < params.length; i++){
                if(params[i].equals("Scalar")){
                    res += "I";
                }
                else if(params[i].equals("Array")){
                    res += "[I";
                }
                else {
                    res += params[i];
                }
            }  
        }

        if(functionName.equals("main")){
            res += "[Ljava/lang/String;";
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

    public String getStoreStatic(String moduleName, String variableName, String type){
        if(type.equals(Utils.ARRAY)){
            return "putstatic " + moduleName + "/" + variableName + " [I ";
        }
        else {
            return "putstatic " + moduleName + "/" + variableName + " I ";
        }
    }

    public String getLoadStatic(String moduleName, String variableName, String type){
        if(type.equals(Utils.ARRAY)){
            return "getstatic " + moduleName + "/" + variableName + " [I ";
        }
        else {
            return "getstatic " + moduleName + "/" + variableName + " I ";
        }
    }

    public void printClinit(String clinitCode, int stackDeclaration, int localDeclaration) {
        println(".method static public <clinit>()V");
        println(".limit stack " + stackDeclaration);
        println(".limit locals " + localDeclaration);
        println(clinitCode);
        println("return");
        println(".end method");
    }

    public void close() {
        pw.flush();
    }
}