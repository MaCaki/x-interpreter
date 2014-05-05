package interpreter.ByteCode;

import interpreter.VirtualMachine;
import interpreter.debugger.DebuggerVirtualMachine;

/**
 * 
 * @author Raskolnikov
 */
public class DebugReturnCode extends ReturnByteCode{
    
    public void execute(VirtualMachine vm){
        DebuggerVirtualMachine debugvm = ((DebuggerVirtualMachine)vm);
        
        if(debugvm.isTracing()){
            String indent = "";
            for (int i=0; i<debugvm.sizeOfFunctionCallStack(); i++) indent += "-";
            
            String name = debugvm.getCurrentFunctionName();
            int returnValue = debugvm.peekRunStack();
            System.out.println(indent + "exit: " + name + ":" + returnValue);
        }
        
        // perform usual return execution after printing out tracing information. 
        super.execute(debugvm);
        debugvm.returnFromFunction();
        
        
    }
}
