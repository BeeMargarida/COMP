import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;
/*
public class Main {
    static java.util.Stack argStack;
    public static void main(String[] args) {
        argStack = new java.util.Stack(); 
        
        File filename = new File(args[0]);
        try {
                Parser parser = new Parser(new FileInputStream(filename));
                SimpleNode root = parser.Module();
                root.dump("");
        } catch(FileNotFoundException e) {
                System.out.println("Exception found");
        }
    }
}*/