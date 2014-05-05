package interpreter.DebuggerUI.DebuggerConsoleUI;
import interpreter.Program;
import interpreter.debugger.*;
import java.lang.reflect.Method;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;




/**
 * This is the default UI that allows the user to interact with the debugger
 * through the console. 
 * 
 * The DebuggerConsoleUI is expecting to receive all of the parameters of the
 * DebuggerVM so that it can create its own vm and control the execution of it. 
 * 
 * @author Raskolnikov
 */
public class DebuggerConsoleUI {
    java.util.HashMap<String,String> commandTable = new java.util.HashMap<String,String>();
    DebuggerVirtualMachine vm;
    StringTokenizer currentCommandTokens;
    
    public DebuggerConsoleUI(Program prog, Vector<String> source){
        vm = new DebuggerVirtualMachine(prog, source);
        initializeCommandTable();
    }
    
    public void run(){
  
        vm.initialize();
        System.out.println("-----Running in Debugger Mode-----");
        printSourceCode();
        
 //       ConsoleUIunitTests.test_1(this);
        while (!vm.doneExecuting()){
             waitForUserCommand();
        }
        
         System.out.println("Execution Finished");
       
    }
    
    
    /*
     * The user enters a command which is hashed to the corresponding UI function
     * which is executed within this function.  Once the vm finishes executing
     * (stops for either a break point or end of program) this function returns
     * to the run() function. 
     */
    private void waitForUserCommand(){
        help();
        String command;
        Scanner input = new Scanner(System.in);
        command = input.nextLine();
        currentCommandTokens = new StringTokenizer(command);
        
        String commandSymbol; 
        try{ // Return if the commandSymbol is empty. 
            commandSymbol= currentCommandTokens.nextToken().trim();
        } catch(Exception e){ return;}
        String functionName = commandTable.get(commandSymbol);
        
        if (functionName == null ) {
            System.out.println("Sorry, could not interpret command " + commandSymbol + 
                    ", please enter a new command");
            return;
        }
        
        Method UImethod;
        
        try{
            UImethod = DebuggerConsoleUI.class.getDeclaredMethod(functionName, (Class[])null );
            UImethod.invoke(this, (Object[])null);
        } catch (Exception e){
            System.out.println("Something went wrong, try again");
            e.printStackTrace();
        }
    }
    
    
    
    public void help(){
        System.out.print("Type ? for help \n >");
    }
    
    private void continueRunning(){
        vm.continueRunning();
        if (!vm.doneExecuting()) printCurrentFunction();
    }
    
    private void setBreakPoints(){
        while(currentCommandTokens.hasMoreTokens()){
            try {
                int line = Integer.parseInt(currentCommandTokens.nextToken().trim());
                if ( !vm.setBreakPoint(line)) {
                    System.out.println(line +" is not a valid break point");
                }
            } catch (Exception e){
                System.out.println("Something went wrong.");
            }
        }
        
        printCurrentBreakPoints();
        
    }
    
    private void clearBreakPoints(){
        while(currentCommandTokens.hasMoreTokens()){
            try {
                int line = Integer.parseInt(currentCommandTokens.nextToken().trim());
                vm.clearBreakPoint(line);
            } catch (Exception e){
                System.out.println("Something went wrong.");
            }
        }
    }
    
    private void printCurrentBreakPoints() {
        Integer[] breakPoints = vm.getBreakPoints();
        
        if (breakPoints.length>0){
            System.out.print("Breakpoints currently set at lines:  ");
            for (int i=0; i<breakPoints.length; i++) {
                System.out.print(breakPoints[i] + " ");
            }
            System.out.println();
        } else {
            System.out.println("There are no breakpoints set.");
        }
    }
        
    private void printSourceCode(){
        System.out.print("Source Code: \n ------------------- \n");
        int currentLineNumber = vm.getCurrentLineNumber();
        printSourceBetweenLines(1, vm.sourceCodeSize());
        System.out.print(" ------------------- \n");
        
    }
    
    private void printCurrentFunction(){
        if (vm.isCurrentRecordEmpty()){
            System.out.println("Not currently in a function");
            return;
        }
        System.out.println("--- Current Function:  " + vm.getCurrentFunctionName() +"\n");
        int beginLine = vm.getCurrentFunctionStartLine();
        int endLine = vm.getCurrentFunctionEndLine();
        printSourceBetweenLines(beginLine, endLine);
        System.out.print(" ------------------- \n");
    }
    
    /**
     * Prints out the source code between lines given as parameters, 
     * and indicates where the current line is. 
     * 
     * @param start
     * @param end 
     */
    private void printSourceBetweenLines(int start, int end){
        int currentLineNumber = vm.getCurrentLineNumber();
        for (int lineNo = start ; lineNo <= end; lineNo++){
            String line =vm.getSourceCodeLine(lineNo);

            String currentLineFlag = "";
            if (lineNo == currentLineNumber) currentLineFlag= "\t <= EXECUTION STOPPED HERE";

            String breakFlag =" ";
            if (vm.isBreakPointSet(lineNo)){
                breakFlag = "**";
            }
            System.out.printf("%2s%2d: %-40s %s \n",breakFlag, lineNo, line, currentLineFlag);
        }
    }

    
    
    /**
     * Prints out the function call stack like
     * 
     * main()  line:1
     *  g(3)    line:9
     *   f(4)    line:3
     * 
     */
    private void printCallStack(){
        int depthOfStack = vm.sizeOfFunctionCallStack();
        for(int n=0; n < depthOfStack; n++){
            String functionName = vm.getNthFunctionName(n);
            int startLine = vm.getNthFunctionStartLine(n);
            
            String indent = "";
            for( int i=0; i<n; i++){
                indent += " ";
            }
            
            System.out.println(indent + functionName + "  line:" + startLine);
        }
    }
    
    private void printVariables(){
        Set<String> vars = vm.getCurrentVariables();
        System.out.println("Current Variables: ");
        for(String variable : vars){
            
            int value = vm.getVariableValue(variable);
            System.out.printf( "%10s : %d \n", variable, value);
        }
    }
    
    private void stepOutOfCurrentFunction(){
        vm.setStepOutFlag();
        continueRunning();
    }
    
    private void stepOverLine(){
        vm.setStepOverFlag();
        continueRunning();
    }
    
    private void stepIntoLine(){
        vm.setStepIntoFlag();
        continueRunning();
    }
    
    
    private void printFunctionEnvironmentRecordStack(){
        
        for ( int i = vm.sizeOfFunctionCallStack()-1; i>=0; i--){
            System.out.println(vm.stringifyFunctionEnvironmentRecord(i));
        }
        
    }
    
    
  
    private void helpMenu(){
        System.out.println("The following are valid commands: ");       
        System.out.printf("\t %-10s \t %s  \n", "c", "Continue execution of program until next break point.");
        System.out.printf("\t %-10s \t %s  \n", "stout", "Step out: continue execution until the current function returns.");
        System.out.printf("\t %-10s \t %s \n", "stover", "Step over current line."); 
        System.out.printf("\t %-10s \t %s \n", "stin", "Step into the current line."); 
        System.out.printf("\t %-10s \t %s \n", "setb <lines>", "Set break point on the lines corresponding to the numbers passed to it.");
        
        System.out.println();
        System.out.printf("\t %-10s \t %s \n", "clrb <lines>", "Clear break point on the lines corresponding to the numbers passed to it.");
        System.out.printf("\t %-10s \t %s \n", "print", "Print annotated source code.");
        System.out.printf("\t %-10s \t %s \n", "dfn", "Display the current function indicating current point of execution.");
        System.out.printf("\t %-10s \t %s \n", "calls", "Print out the function call stack.");
        System.out.printf("\t %-10s \t %s \n", "fest", "Prints the current state of the Function Environment Stack.");
        System.out.printf("\t %-10s \t %s \n", "vars", "Prints all the current variables and their values."); 
        
        System.out.println();
        System.out.printf("\t %-10s \t %s \n", "help/?", "Print out this help menu.");
        System.out.printf("\t %-10s \t %s \n", "quit", "Halt execution of the current program.");
       
          
        
    }
    
    
     private void initializeCommandTable(){
        commandTable.put("?", "helpMenu");
        commandTable.put("help", "helpMenu");
        commandTable.put("c", "continueRunning");
        commandTable.put("setb", "setBreakPoints");
        commandTable.put("clrb", "clearBreakPoints");
        commandTable.put("print", "printSourceCode");
        commandTable.put("quit", "quitExecution");
        commandTable.put("dfn", "printCurrentFunction");
        commandTable.put("fest", "printFunctionEnvironmentRecordStack");
        commandTable.put("vars", "printVariables");
        commandTable.put("stout", "stepOutOfCurrentFunction");
        commandTable.put("stover", "stepOverLine");
        commandTable.put("stin", "stepIntoLine");
        commandTable.put("calls", "printCallStack");
    }

    public void quitExecution(){
        vm.turnOffVm();
    }
    
}



class commandLineInterfaceFunction{
    
    String command;
    String functionName;
    String helpMenuDescription;
    
}





class ConsoleUIunitTests {
    
    public static void test_1(DebuggerConsoleUI ui){

        
    }
    
}
