import java.util.HashMap;
public class KernelandProcess {
    UserlandProcess userprocess; // creating a reference to the userland process object instance
    int pid; // storing the process id for the userland process
    PriorityEnum priority;
    int ranToTimeoutCounter;
    int[] deviceList; // to store VFS indices associated with current process
    //int[] pageArray; // Page array to store virtualPage to physicalPage mapping
    VirtualToPhysicalMapping[] pageArray;
    HashMap<Integer,Boolean> mutexList;
    public KernelandProcess(UserlandProcess userprocess,int pid, PriorityEnum priority){ // storing userland object reference and associated pid
        this.userprocess=userprocess;
        this.pid=pid;
        this.priority=priority;
        ranToTimeoutCounter=0;
        deviceList=new int[10];
        pageArray=new VirtualToPhysicalMapping[1024];
        mutexList=new HashMap<Integer,Boolean>();
        for(int i=0;i<10;i++){
            deviceList[i]=-1;// initially the kernel storage for the process is empty and indicated using -1 value
        }
        for(int i=0;i<1024;i++){
            pageArray[i]=null;// initially the page array is empty
        }
    }
}
