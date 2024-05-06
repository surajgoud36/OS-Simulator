import java.util.*;
public class GoodbyeWorldProcess extends UserlandProcess {
    int stateVariable; // Especially used when working with mutexes, to resume execution from the point where it requires a lock on a particular mutex
    int mutexId;
    public GoodbyeWorldProcess(){
        stateVariable=0;
        mutexId=-1;
    }
    public RunResult run() {
        if(stateVariable==0){
            System.out.println("Goodbye World");
            mutexId=OS.getInstance().mutex.AttachToMutex("memory1");// attaching to mutex
            OS.getInstance().currentProcess.mutexList.put(mutexId,false); // updating kernal list of mutexes
            boolean lockres=OS.getInstance().mutex.Lock(mutexId); // storing the result of lock function
            if(lockres){
                OS.getInstance().currentProcess.mutexList.put(mutexId,true); // updating the  mutex state to true in kernaland tracking of mutex object state
                byte[] temp=new byte[10];
                int id1=OS.getInstance().Open("pipe joe"); // opeing a pipe
                int res=OS.getInstance().Write(id1,temp);
                OS.getInstance().Close(id1); // closing the pipe
                OS.getInstance().sbrk(500);

                byte b= Byte.parseByte("1");
                try{
                    OS.getInstance().WriteMemory(100,b);
                    byte readValue=OS.getInstance().ReadMemory(100);
                    //System.out.println(readValue);
                } catch (RescheduleException e){
                    OS.getInstance().checkException=1; // using this variable to indicate exception has occured in the scheduler

                }
                OS.getInstance().unlockMutex=OS.getInstance().currentProcess; // variable to store kernaLand process whose associated mutex needs to be unlocked
                OS.getInstance().mutex.Unlock(mutexId);
                OS.getInstance().freeMutex=OS.getInstance().currentProcess;  // variable to store kernaLand process whose associated mutex needs to be released
                OS.getInstance().mutex.ReleaseMutex(mutexId);
                OS.getInstance().currentProcess.mutexList.remove(mutexId); // updating the process's kerneland storage with regard to the mutex
            }
            else { // if locking is unsuccessful we go into a wait queue
                stateVariable=1; // changing the value of state varible to resume execution after obtaining the lock on the mutex
                OS.getInstance().checkLock=1; // using this variable to let priority scheduler know that it has to remove the current kernaland process from the running queue and put it in a waitlist of the mutex
                OS.getInstance().checkMutexId=mutexId; // storing the mutex's id whose wait queue we require to put the process
            }
        }
        else { // after it obtains the lock the process can happily resume it's execution from where it had left
            byte[] temp=new byte[10];
            int id1=OS.getInstance().Open("pipe joe"); // opeing a pipe
            int res=OS.getInstance().Write(id1,temp);
            OS.getInstance().Close(id1); // closing the pipe
            OS.getInstance().sbrk(500);

            byte b= Byte.parseByte("1");
            try{
                OS.getInstance().WriteMemory(100,b);
                byte readValue=OS.getInstance().ReadMemory(100);
                //System.out.println(readValue);
            } catch (RescheduleException e){
                OS.getInstance().checkException=1; // using this variable to indicate exception has occured in the scheduler

            }
            OS.getInstance().unlockMutex=OS.getInstance().currentProcess; // variable to store kernaLand process whose associated mutex needs to be unlocked
            OS.getInstance().mutex.Unlock(mutexId);
            OS.getInstance().freeMutex=OS.getInstance().currentProcess;  // variable to store kernaLand process whose associated mutex needs to be released
            OS.getInstance().mutex.ReleaseMutex(mutexId);
            OS.getInstance().currentProcess.mutexList.remove(mutexId); // updating the process's kerneland storage with regard to the mutex
            stateVariable=0; // resetting the state variable
        }


        try{
            Thread.sleep(500);
        }
        catch (Exception e){
            System.out.println(e);
        }
        RunResult obj=new RunResult(false,50);
        //OS.getInstance().invalidateTLB(); // clearing the TLB for the current process after it finished its execution
        return obj;
    }
}
