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
        super.execute(debugvm);
        debugvm.returnFromFunction();
        
        if(debugvm.isTracing()){
            String indent = "";
            for (int i=0; i<debugvm.sizeOfFunctionCallStack(); i++) indent += "-";
            
            String name = debugvm.getCurrentFunctionName();
            System.out.print(indent +  name +"(");
        }
    }
}
