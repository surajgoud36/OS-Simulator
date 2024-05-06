import java.util.ArrayList;
public class MutexInstance {
    String mutexName;
    boolean state; //in use state
    ArrayList<KernelandProcess> attachList; // list of attached processes
    ArrayList<KernelandProcess> waitList; // waiting queue for this respective mutex
    public MutexInstance(boolean state){
        this.state=state; // initializing with false as the mutex is not locked
        attachList=new ArrayList<KernelandProcess>();
        waitList=new ArrayList<KernelandProcess>();
    }

}
