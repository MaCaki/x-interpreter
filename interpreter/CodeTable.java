package interpreter;
import interpreter.ByteCode.*;
/**
 * Static class that maps the string names of bytecodes to their corresponding
 * class objects.  
 * 
 * 
 * @author admin
 */
public class CodeTable {
    protected static java.util.HashMap<String,Class> codeTable = new java.util.HashMap<String,Class>();
            
    public static Class get(String byteCodeName){
        return codeTable.get(byteCodeName);
    }        
    
    public static void init(){
        // for each type of ByteCode, create a hash {"type" => ClassOfByteCode}.
        codeTable.put("ARGS", ArgsByteCode.class);
        codeTable.put("BOP", BopByteCode.class);
        codeTable.put("CALL", CallByteCode.class);
        codeTable.put("DUMP", DumpByteCode.class);
        codeTable.put("FALSEBRANCH", FalseBranchByteCode.class);
        codeTable.put("GOTO", GoToByteCode.class);
        codeTable.put("HALT", HaltByteCode.class);
        codeTable.put("LOAD", LoadByteCode.class);
        codeTable.put("LABEL", LabelByteCode.class);
        codeTable.put("LIT", LitByteCode.class);
        codeTable.put("POP", PopByteCode.class);
        codeTable.put("READ", ReadByteCode.class);
        codeTable.put("RETURN", ReturnByteCode.class);
        codeTable.put("STORE", StoreByteCode.class);
        codeTable.put("WRITE", WriteByteCode.class);        
    }
    
    public static void addDebugCode(){
        codeTable.put("LINE", LineByteCode.class);
        codeTable.put("FUNCTION", FunctionByteCode.class);
        codeTable.put("FORMAL", FormalByteCode.class);
        codeTable.put("CALL", DebugCallCode.class);
        codeTable.put("LIT", DebugLitCode.class);
        codeTable.put("POP", DebugPopCode.class);
        codeTable.put("RETURN", DebugReturnCode.class);
    }
    
}
