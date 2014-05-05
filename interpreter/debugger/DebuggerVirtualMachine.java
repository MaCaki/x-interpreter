package interpreter.debugger;
import interpreter.*;
import interpreter.ByteCode.*;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

/**
 *When the debug flag is set, the interpreter will create an instance of a
 * DebuggerVM to execute the program.   
 * 
 * 
 * @author Raskolnikov
 */


/**
 * Milestone 3: 
 *     help, set/clear breakpoints, display the current function,
 *      continue execution, quit execution
 *      display local variable(s)
 * 
 *   - Done - 
 * 
 * Milestone 5: 
 * Step over a line     **
 * Step into a function  **
 * Step out of current activation of a function     **
 * Set breakpoints where allowable  **
 * List current breakpoint settings   **
 * Clear designated breakpoints   **
 * Set function tracing  
 * Print Call Stack     **
 * Display local variables    **
 * Display the source code of the current function  **
 * Continue execution    **
 * Halt execution    **
 * 
 */
public class DebuggerVirtualMachine extends VirtualMachine{
    private Vector<AnnotatedSourceLine> sourceCodeLines;
    private Stack<FunctionEnvironmentRecord> EnvironmentRecordStack;
    private Stack<Integer> validBreakPoints; 
    
    /* Step Out parameters:
     * if stepOutFlag is set, then when continueExecution
     *is called, the vm will continue running until it has returned from
     *function that was active when it started to run. 
     * 
     * While the vm is running we check to see if the the sizeOfFuntionCallStack()
     * has gone below originalEnvironmentSize which is set when stepOut is called. 
     */
    private boolean stepOutFlag; 
    private int originalEnvironmentSize;
    private boolean stepOverFlag;
    private int originalLineNumber;
    private boolean stepIntoFlag;
    boolean stackTraceFlag;
    
    

    public DebuggerVirtualMachine(Program prog, Vector<String> source){
        super(prog);
        sourceCodeLines = AnnotatedSourceLine.annotate(source);
        EnvironmentRecordStack = new Stack<FunctionEnvironmentRecord>();
    }
    
    /**
     * This must run before the VM can start executing the program. 
     */
    public void initialize(){
        pc = 0;
        runStack = new RunTimeStack();
        returnAddrs = new Stack();
        isRunning = true;
        stepOutFlag =false;
        stepOverFlag = false;
        originalEnvironmentSize = 0;
        originalLineNumber = 0;
        beginScope();
        validBreakPoints = new Stack<Integer>();
        inventoryValidBreakPoints();
        BinaryOpTable.init();
    }
    
    /**
     * This will launch the initialized VM to start running until it hits a 
     * break point, or if one of the user entered commands has been fulfilled. 
     */
    public void continueRunning(){
        isRunning = true;

            /* Executes until breakpoint is hit or until it has stepped out of 
             current function, when stepOut has been set.*/
        while (isRunning){
            ByteCode code = program.getCode(pc);
            code.execute(this);
            // only dump if the current code is not DUMP and dumping is set. 
            if (!code.getClass().getName().equals("interpreter.ByteCode.DumpByteCode")
                    && dumping){
                System.out.println(code.toString());
                runStack.dump();
            }
            pc++;

            /* If the ByteCode executed in this loop was a LineCode where
             the line is breakpoint, then the setCurrentLine() function
             will also turn off the VM.  */
            
            // If stepOutFlag is set, return if stack has gone below original size
            if (stepOutFlag && haveSteppedOut()) return;
            
            // If stepOverFlag is set, return if stack has gone below original size
            if (stepOverFlag && haveSteppedOver()) return;
            
            if (stepIntoFlag && haveSteppedInto()) return;
            
        }
    }
    
    /**
     * Call this function before continuing execution will cause execution
     * of the program to stop once a new line is reached, or if the current function
     * is stepped out of. 
     */
    public void setStepOverFlag() {
        stepOverFlag = true;
        originalEnvironmentSize = sizeOfFunctionCallStack();
        originalLineNumber = getCurrentLineNumber();
    }
    
    private boolean haveSteppedOver(){
        // If execution has stepped out, then we return
        if( sizeOfFunctionCallStack() <originalEnvironmentSize){
            stepOverFlag = false;
            return true;
        }

        if ((sizeOfFunctionCallStack() == originalEnvironmentSize)
                && (getCurrentLineNumber() > originalLineNumber)){
            stepOverFlag = false;
            return true;
        }
        return false;
    }

    
    /**
     * Call this function before continuing execution will cause execution
     * of the program to continue until the current FunctionEnvironmentRecord
     * is popped, 
     */
    public void setStepOutFlag(){
        stepOutFlag = true;
        originalEnvironmentSize = sizeOfFunctionCallStack();
    }
    
    private boolean haveSteppedOut(){
        if (sizeOfFunctionCallStack() <originalEnvironmentSize){
                stepOutFlag = false;
                return true;
        }else {
            return false;
        }
        
    }
    
    
    
    public void setStepIntoFlag(){
        stepIntoFlag = true;
        originalEnvironmentSize = sizeOfFunctionCallStack();
    }
    
    private boolean haveSteppedInto(){
        if (originalEnvironmentSize < sizeOfFunctionCallStack() 
                || haveSteppedOver()) {
            stepIntoFlag = false;
            return true;
        } 
        return false;
    }
    
    
    public void turnOnStackTrace() {
        stackTraceFlag = true;
    }
     
    public boolean isTracing(){
        return stackTraceFlag;
    }
    
    
        /*  ---------   Source Code Getters and Setters -----*/
    
    
    public String getSourceCodeLine(int n){
        AnnotatedSourceLine AnnotatedLine = sourceCodeLines.get(n-1);
        String line = AnnotatedLine.getLine();
        return line;
    }
    
     public boolean setBreakPoint(int n){
        if (validBreakPoints.contains(n)){
            sourceCodeLines.get(n-1).setBreakPoint();
            return true;
        } 
        return false;
    }
     
    public void clearBreakPoint(int n){
        sourceCodeLines.get(n-1).clearBreakPoint();
    }
    
    public boolean isBreakPointSet(int n){
        if (n>1) return sourceCodeLines.get(n-1).isBreakPointSet();
        return false;
    }
    
    public int sourceCodeSize(){
        return sourceCodeLines.size();
    }
    
    public Integer[] getBreakPoints() {
        Vector<Integer> breakPoints = new Vector<Integer>();
        
        for ( int line =1; line<=sourceCodeSize(); line++){
            if (isBreakPointSet(line)) breakPoints.add(line);
        }
        
        Integer[] a = new Integer[0];
        //if (breakPoints.isEmpty()) return new Integer[0];
        return (Integer[])breakPoints.toArray(a);
        
    }
    
    
    /*  ---------  FunctionEnvironStack   Getters and Setters -----*/
    
    public String stringifyFunctionEnvironmentRecord(int n){
        return EnvironmentRecordStack.get(n).stringifyRecord();
    }
    
    public int sizeOfFunctionCallStack(){
        return EnvironmentRecordStack.size();
    }
    
    private FunctionEnvironmentRecord currentRecord(){
        return EnvironmentRecordStack.peek();
    }
    

    public boolean isCurrentRecordEmpty(){
        return currentRecord().isEmpty();
    }
    
    public void setCurrentLineNumber(int n){
        currentRecord().setCurrentLine(n);
        // if this line corresponds to a breakoint, stop the VM.
        if (isBreakPointSet(n)) isRunning = false;
    }
    
    public int getCurrentLineNumber(){
        return currentRecord().getCurrentLine();
    }
    
    public int getCurrentFunctionStartLine(){
        return currentRecord().getStartLine();
    }
    
    public int getCurrentFunctionEndLine(){
        return currentRecord().getEndLine();
    }
    
    public String getCurrentFunctionName(){
        return currentRecord().getFunctionName();
    }
    
    public Set<String> getCurrentVariables() {
        return currentRecord().getCurrentVariables();
    }
    
    public int getVariableValue(String name){
        int offset = currentRecord().getVariableOffset(name);
        int value;
        value = runStack.getValueAtOffset(offset);
        return value;
    }
    
    /**
     * 
     * @param n the index of the function in the stack (0...k)
     * @return  The name of the nth function in the stack. 
     */
    public String getNthFunctionName(int n) {
        try{
            return EnvironmentRecordStack.get(n).getFunctionName();
        } catch (Exception e){ return null;}
    }
    
    public int getNthFunctionStartLine(int n) {
        try{
            return EnvironmentRecordStack.get(n).getStartLine();
        }catch (Exception e){ return 0;}
    }

    
    /*
     * Sets the name, beginline and end line of the current record.  This
     * is executed when a FUNCTION byte code is called. 
     */
    public void setFunction(String name, int start, int end){
        FunctionEnvironmentRecord topRecord = EnvironmentRecordStack.peek();
        topRecord.setFunction(name, start, end);
    }
    
    /*
     * Creates a new, empty funct env. record and pushes it onto the record 
     * stack. This is done at initialization, and every time a CALL byte code
     * is encountered. 
     */
    public void beginScope(){
        FunctionEnvironmentRecord newRecord = new FunctionEnvironmentRecord();
        newRecord.beginScope();
        EnvironmentRecordStack.push(newRecord);
    }
    
    public void loadLiteral(String varname){
        FunctionEnvironmentRecord currentRecord = EnvironmentRecordStack.peek();
        currentRecord.enter(varname, getCurrentOffset());
    }
    
    public void enterIntoCurrentRecord(String name, int offset) {
        EnvironmentRecordStack.peek().enter(name, offset); //To change body of generated methods, choose Tools | Templates.
    }

    
    /*
     * Executed when a DebugPop code is called.  Pops the last n values pushed
     * onto the FER stack. 
     */
    public void popFunctionEnvironmentRecordStack(int n) {
        EnvironmentRecordStack.peek().pop(n);
    }
    
    public void returnFromFunction(){
        EnvironmentRecordStack.pop();
    }

    
    
    /*
     * Goes through the program object and adds all of the argmuents of the
     * LINE byte codes except for -1's. 
     */
    private void inventoryValidBreakPoints(){
        for(int i=0; i< program.getNumberOfByteCodes(); i++){
            ByteCode code = program.getCode(i);
            if ( code.getClass().getName().equals("interpreter.ByteCode.LineByteCode")){
                int line =((LineByteCode)code).lineNumber();
                if (line != -1)validBreakPoints.add(line);
            }
        }
    }

  
}





/**
 * A helper class to contain source code text as well as information about
 * the source code such as if a break point is set.  
 * 
 * @author Raskolnikov
 */
class AnnotatedSourceLine{

    static Vector<AnnotatedSourceLine> annotate(Vector<String> source) {
        Vector<AnnotatedSourceLine> annotatedVector = new Vector<AnnotatedSourceLine>();
        
        for (int i=0; i<source.size(); i++){
            AnnotatedSourceLine annotatedLine = new AnnotatedSourceLine(source.get(i));
            annotatedVector.add(annotatedLine);
        }
        return annotatedVector;
    }
    private boolean isBreakPointSet;
    private String sourceCodeLine;
    
    AnnotatedSourceLine(String line, boolean breakpoint){
        isBreakPointSet = breakpoint;
        sourceCodeLine = line;
    }
    
    AnnotatedSourceLine(String line){
        isBreakPointSet = false;
        sourceCodeLine = line;
    }
    
    public String getLine(){
        return sourceCodeLine;
    }
    
    public boolean isBreakPointSet(){
        return isBreakPointSet;
    }
    
    public void setBreakPoint(){
        isBreakPointSet = true;
    }
    
    public void clearBreakPoint(){
        isBreakPointSet = false;
    }
    
}