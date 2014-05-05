package interpreter.ByteCode;
import interpreter.ByteCode.ByteCode;
import interpreter.VirtualMachine;
import interpreter.debugger.DebuggerVirtualMachine;

/**
 * This code is a debugging byte code that has as arguments
 * the name of the function and the starting and ending line numbers:
 * 
 * FUNCTION fib 2 11
 * 
 * @author Raskolnikov
 */
public class FunctionByteCode extends ByteCode{
    
    String name;
    int startLine, endLine;
    
    public void init(String args[]){
        name = args[1];
        startLine = Integer.parseInt(args[2]);
        endLine = Integer.parseInt(args[3]);
    }
    
    public void execute(VirtualMachine vm){
        DebuggerVirtualMachine debugvm = ((DebuggerVirtualMachine)vm);
        
        debugvm.setFunction(name, startLine, endLine);
        
        if ( debugvm.isTracing() ){
            String indent = "";
            for (int i=0; i<debugvm.sizeOfFunctionCallStack(); i++) indent += "-";
            
            System.out.print(indent +  name +"(");
            int[] args = debugvm.getCurrentFrame();
            
            for ( int i=0; i<args.length; i++) System.out.print(args[i]);
            
            System.out.println(")");
        }
    }
    
    
}
