package interpreter;
import interpreter.ByteCode.*;
import interpreter.debugger.DebuggerCodeTable;
import java.io.*;
import java.util.*;
/**
 *
 * @author admin
 */
public class ByteCodeLoader {
    
    private Scanner byteCodeSource;
    
   //initializer
   // opens up the file and puts in in a scanner to be read into a Program
   // object. 
   public ByteCodeLoader(String file_name) throws IOException{
       byteCodeSource = new Scanner(new FileReader(file_name));
   }
   
   public Program loadCodes(){
       Program theProgram = new Program();
       
       while(byteCodeSource.hasNextLine()){
           try{
                String line = byteCodeSource.nextLine(); 
                String lineTokens[]  = line.split(" ");
                Class byteCodeClass;

                byteCodeClass = CodeTable.get(lineTokens[0]);
                
                ByteCode theByteCode = (ByteCode)byteCodeClass.newInstance();
                theByteCode.init(lineTokens);

                theProgram.pushByteCode(theByteCode);
           } catch (Exception e){ }
       }
       theProgram.resolveAddresses();
       return theProgram;
   }
}
