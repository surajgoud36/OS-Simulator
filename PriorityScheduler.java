import java.util.ArrayList;
import java.util.HashMap;
import java.util.*;
import java.lang.*;
public class PriorityScheduler {
    public int processid=0; //For allocating process id's to the processes created
    public boolean flag1=false; // for running interactive and background processes with new probability values
    public ArrayList<KernelandProcess> RealtimeList; // queue for realtime processes
    public ArrayList<KernelandProcess> InteractiveList;  // queue for Interactive processes
    public ArrayList<KernelandProcess> BackgroundList; // queue for Background processes
    public HashMap<KernelandProcess, Integer> waitingList;// a hash map with key as userland process and value as time left to sleep
    public PriorityScheduler(){
        processid=0;
        flag1=false;
        RealtimeList = new ArrayList<KernelandProcess>();// queue for realtime processes
        InteractiveList = new ArrayList<KernelandProcess>();// queue for Interactive processes
        BackgroundList = new ArrayList<KernelandProcess>(); // queue for Background processes
        waitingList= new LinkedHashMap<KernelandProcess, Integer>(); // a hash map with key as userland process and value as time left to sleep
    }
    public int CreateProcess(UserlandProcess myNewProcess, PriorityEnum priority) {
        processid=processid+1;
        KernelandProcess obj = new KernelandProcess(myNewProcess,processid,priority); // kernaland object created for the new process
        if(priority == PriorityEnum.RealTime) // Adding processes to their respective queues based on their priorities
            RealtimeList.add(obj);
        else if(priority == PriorityEnum.Interactive)
            InteractiveList.add(obj);
        else
            BackgroundList.add(obj);

        return processid;
    }

    public boolean DeleteProcess(int processId) {
        boolean flag=false;
        for(int i=0;i<RealtimeList.size();i++) { // iterating the list to find the process by process id
            if(RealtimeList.get(i).pid==processId) {
                if(RealtimeList.size()==1)
                    flag1=true; // if all realtime processes are deleted
                KernelandProcess userProcess=RealtimeList.get(i);// getting the current kernel land process to be deleted
                OS.getInstance().delProcess=RealtimeList.get(i);// kerneland object whose pagearray and associated memory to be deleted
                OS.getInstance().freeMemory();// freeing any allocated memory of the current process to be deleted.
                OS.getInstance().freeMutex=RealtimeList.get(i); // freeing the deleted process mutex list
                OS.getInstance().unlockMutex=RealtimeList.get(i); // unlocking if any from the processes mutex list
                for(int j=0;j<userProcess.deviceList.length;j++){// iterating the device list for each userland process to close any open devices
                    if(userProcess.deviceList[j]!=-1){
                        OS.getInstance().vfs.Close(userProcess.deviceList[j]);
                        userProcess.deviceList[j]=-1;
                    }
                }
                for(int k: userProcess.mutexList.keySet()){ // iterating its mutex list and unlocking and releasing the mutexes if any
                    if(userProcess.mutexList.get(k)==true){
                        OS.getInstance().mutex.Unlock(k);
                    }
                    OS.getInstance().mutex.ReleaseMutex(k);

                }
                userProcess.mutexList.clear(); // deleting the mutex list
                RealtimeList.remove(i);
                flag=true;
                return flag;
            }
        }
        for(int i=0;i<InteractiveList.size();i++) { // iterating to find the process by process id
            if(InteractiveList.get(i).pid==processId) {
                KernelandProcess userProcess=InteractiveList.get(i);
                OS.getInstance().delProcess=InteractiveList.get(i);// kerneland object whose memory is to be deleted
                OS.getInstance().freeMemory(); // freeing the memory
                OS.getInstance().freeMutex=InteractiveList.get(i);
                OS.getInstance().unlockMutex=InteractiveList.get(i);
                for(int j=0;j<userProcess.deviceList.length;j++){// iterating the device list for each userland process to cloe any open devices
                    if(userProcess.deviceList[j]!=-1){
                        OS.getInstance().vfs.Close(userProcess.deviceList[j]);
                        userProcess.deviceList[j]=-1;
                    }
                }
                for(int k: userProcess.mutexList.keySet()){
                    if(userProcess.mutexList.get(k)==true){
                        OS.getInstance().mutex.Unlock(k);
                    }
                    OS.getInstance().mutex.ReleaseMutex(k);

                }
                userProcess.mutexList.clear();

                InteractiveList.remove(i);
                flag=true;
                return flag;
            }
        }
        for(int i=0;i<BackgroundList.size();i++) { // iterating to find the process by process id
            if(BackgroundList.get(i).pid==processId) {
                KernelandProcess userProcess=BackgroundList.get(i);
                OS.getInstance().delProcess=BackgroundList.get(i);// kerneland object whose memory is to be deleted
                OS.getInstance().freeMemory(); // freeing the memory
                OS.getInstance().freeMutex=BackgroundList.get(i);
                OS.getInstance().unlockMutex=BackgroundList.get(i);
                for(int j=0;j<userProcess.deviceList.length;j++){// iterating the device list for each userland process to cloe any open devices
                    if(userProcess.deviceList[j]!=-1){
                        OS.getInstance().vfs.Close(userProcess.deviceList[j]);
                        userProcess.deviceList[j]=-1;
                    }
                }
                for(int k: userProcess.mutexList.keySet()){
                    if(userProcess.mutexList.get(k)==true){
                        OS.getInstance().mutex.Unlock(k);
                    }
                    OS.getInstance().mutex.ReleaseMutex(k);

                }
                userProcess.mutexList.clear();
                BackgroundList.remove(i);
                flag=true;
                return flag;
            }
        }

        for(KernelandProcess i: waitingList.keySet()){ // deleting the process from sleep list if it is not present in any of the above lists
            if(i.pid==processId){
                if(i.priority==PriorityEnum.RealTime&& RealtimeList.size()==0) // if all realtime processes are deleted
                    flag1=true;
                KernelandProcess userProcess=i;
                OS.getInstance().delProcess=i; // kerneland object whose memory is to be deleted
                OS.getInstance().freeMemory(); // free the memory
                OS.getInstance().freeMutex=i;
                OS.getInstance().unlockMutex=i;
                for(int j=0;j<userProcess.deviceList.length;j++){// iterating the device list for each userland process to cloe any open devices
                    if(userProcess.deviceList[j]!=-1){
                        OS.getInstance().vfs.Close(userProcess.deviceList[j]);
                        userProcess.deviceList[j]=-1;
                    }
                }
                for(int k: userProcess.mutexList.keySet()){
                    if(userProcess.mutexList.get(k)==true){
                        OS.getInstance().mutex.Unlock(k);
                    }
                    OS.getInstance().mutex.ReleaseMutex(k);

                }
                userProcess.mutexList.clear();
                waitingList.remove(i);
                flag=true;
                return flag;
            }
        }
        return flag; // returns false when process id is not found
    }
    public void Sleep(int milliseconds){
        int size=waitingList.size();
        ArrayList<KernelandProcess> readylist = new ArrayList<KernelandProcess>();// list to store processes which needs to be moved to active list when it completes the sleep time
        if(size>0){
            for(KernelandProcess i: waitingList.keySet()){ // Updating the list of sleeping process
                int k=waitingList.get(i); // retrieving the sleep time for each process in sleep queue
                k-=milliseconds; // reducing the sleep time of each process by execution time of recently executed process
                if(k<=0){
                    readylist.add(i);// adding the process to the ready queue once it completed sleep time
                }
                else{
                    waitingList.put(i,k); // updating the sleep value of sleeping processes
                }
            }
            if(readylist.size()!=0){
                for(int i=0;i<readylist.size();i++){ // adding the process back to its original queue by using the priority value
                    if(readylist.get(i).priority==PriorityEnum.RealTime)
                        RealtimeList.add(readylist.get(i));
                    else if(readylist.get(i).priority==PriorityEnum.Interactive)
                        InteractiveList.add(readylist.get(i));
                    else
                        BackgroundList.add(readylist.get(i));
                    waitingList.remove(readylist.get(i)); // removing the process which completed the sleep time  from sleep list

                }
            }
        }
    }
    public void run() { // method to run the tasks
        RunResult runresult;
        Random randomNumber=new Random();
        double probability;
        if(RealtimeList.size()==0) // if no real time process exists then we use flag to use modified probability values for running processes in other queues
            flag1=true;
        while(true) { //infinite loop to run the tasks
            probability=randomNumber.nextDouble(); // implementing probabilistic model using random class

            if(probability>0.4 && RealtimeList.size()>0){ //realtime processes executes for 60% of the time

                ArrayList<Integer> Downgradelist = new ArrayList<Integer>(); // list for storing the processes to be downgraded

                for(int i=0;i<RealtimeList.size();i++){

                    OS.getInstance().currentProcess=RealtimeList.get(i); // helper variable which stores the executing kerneland process
                    runresult=RealtimeList.get(i).userprocess.run();

                    if(OS.getInstance().checkException==1){ // checking if exception has occured and deleting the process accordingly

                        boolean res=OS.getInstance().DeleteProcess(RealtimeList.get(i).pid);
                        OS.getInstance().checkException=0; // resetting the value of checkException to false
                        i--;
                        continue;
                    }
                    OS.getInstance().invalidateTLB(); // clearing the TLB for the current process after it finished it's execution
                    if(OS.getInstance().checkLock==1){ // making the process go into wait queue of the mutex
                        RealtimeList.remove(i);
                        i--;
                        OS.getInstance().checkLock=0;
                        OS.getInstance().mutex.mutexes[OS.getInstance().checkMutexId].waitList.add(OS.getInstance().currentProcess);
                    }
                    if(runresult.ranToTimeout==true){ // checking the process for downgrade
                        RealtimeList.get(i).ranToTimeoutCounter++;
                        if(RealtimeList.get(i).ranToTimeoutCounter>=5){
                            RealtimeList.get(i).priority= PriorityEnum.Interactive;
                            Downgradelist.add(i);
                        }
                    }
                    if(runresult.ranToTimeout==false){ //checking whether the process must be made to sleep
                        OS.getInstance().Sleep(runresult.millisecondsUsed);
                        waitingList.put(RealtimeList.get(i),50);
                        RealtimeList.remove(i);
                        i--;
                    }
                }
                for(int i=0;i<Downgradelist.size();i++){ // degrading the process from realtime list to Interactive list
                    InteractiveList.add(RealtimeList.get(Downgradelist.get(i)));
                    RealtimeList.remove(Downgradelist.get(i));
                    if(RealtimeList.size()==0)
                        flag1=true;
                }

            }
            else if((flag1)? ((probability<0.75)&& InteractiveList.size()>0): ((probability>=0.1 && probability<0.4)&& InteractiveList.size()>0)){ // using the probability values accordingly depending on the value of flag1

                ArrayList<Integer> Downgradelist = new ArrayList<Integer>(); // list to store the processes to be downgraded

                for(int i=0;i<InteractiveList.size();i++){

                    OS.getInstance().currentProcess=InteractiveList.get(i); // helper variable which stores the executing kerneland process
                    runresult=InteractiveList.get(i).userprocess.run();
                    if(OS.getInstance().checkException==1){ // checking if exception has occured and deleting the process accordingly

                        boolean res=OS.getInstance().DeleteProcess(InteractiveList.get(i).pid);
                        OS.getInstance().checkException=0;
                        i--;
                        continue;
                    }
                    OS.getInstance().invalidateTLB(); // clearing the TLB for the current process after it finished it's execution

                    if(OS.getInstance().checkLock==1){ // making the process go into wait queue
                        InteractiveList.remove(i);
                        i--;
                        OS.getInstance().checkLock=0;
                        OS.getInstance().mutex.mutexes[OS.getInstance().checkMutexId].waitList.add(OS.getInstance().currentProcess);
                    }

                    if(runresult.ranToTimeout==true){ // checking the process for downgrade
                        InteractiveList.get(i).ranToTimeoutCounter++;
                        if(InteractiveList.get(i).ranToTimeoutCounter>=5){
                            InteractiveList.get(i).priority= PriorityEnum.Background;
                            Downgradelist.add(i);
                        }
                    }
                    if(runresult.ranToTimeout==false){ //checking whether the process must be made to sleep
                        OS.getInstance().Sleep(runresult.millisecondsUsed);
                        waitingList.put(InteractiveList.get(i),50); // adding to sleep queue
                        InteractiveList.remove(i); // removing the process from list
                        i--;
                    }
                }
                for(int i=0;i<Downgradelist.size();i++){ // degrading the process from Interactive list to Background list
                    BackgroundList.add(InteractiveList.get(Downgradelist.get(i)));
                    InteractiveList.remove(Downgradelist.get(i));
                }
            }
            else if((flag1)? ((probability>=0.75)&& BackgroundList.size()>0): ((probability>=0.0)&& BackgroundList.size()>0)) { // running the processess from background list for 10% of the time

                for(int i=0;i<BackgroundList.size();i++){

                    OS.getInstance().currentProcess=BackgroundList.get(i); // helper variable which stores the executing kerneland process
                    runresult=BackgroundList.get(i).userprocess.run();
                    if(OS.getInstance().checkException==1){

                        boolean res=OS.getInstance().DeleteProcess(BackgroundList.get(i).pid);
                        OS.getInstance().checkException=0;
                        i--;
                        continue;
                    }
                    OS.getInstance().invalidateTLB(); // clearing the TLB for the current process after it finished it's execution

                    if(OS.getInstance().checkLock==1){ // making the process go into wait queue
                        BackgroundList.remove(i);
                        i--;
                        OS.getInstance().checkLock=0;
                        OS.getInstance().mutex.mutexes[OS.getInstance().checkMutexId].waitList.add(OS.getInstance().currentProcess);
                    }

                    if(runresult.ranToTimeout==false){ // checking whether to sleep the process
                        OS.getInstance().Sleep(runresult.millisecondsUsed);
                        waitingList.put(BackgroundList.get(i),50); // adding to sleep queue
                        BackgroundList.remove(i); // removing from the current list
                        i--;
                    }
                }
            }

        }

    }
    public KernelandProcess getRandomProcess(){
        Random r=new Random();
        ArrayList<KernelandProcess> waitList = new ArrayList<KernelandProcess>();
        if(waitingList.size()>0){

            for(KernelandProcess i: waitingList.keySet()){
                waitList.add(i);
            }
        }
        while(true){
            int check=r.nextInt(100);
            if(check>=0&&check<10){ // checking realTime process 10% of the time
                if(RealtimeList.size()>0){
                    int check1=r.nextInt(RealtimeList.size());
                    if(RealtimeList.get(check1)!=OS.getInstance().currentProcess){
                        for(int i=0;i<1024;i++){
                            if(RealtimeList.get(check1).pageArray[i]!=null){
                                if(RealtimeList.get(check1).pageArray[i].physicalPageNumber!=-1){
                                    return RealtimeList.get(check1);// returning a realtime process
                                }
                            }
                            else {
                                break;
                            }
                        }
                    }
                }
            }
            else if(check>=10&&check<30){ // checking a interactive process 20% of the time
                if(InteractiveList.size()>0){
                    int check1=r.nextInt(InteractiveList.size());
                    if(InteractiveList.get(check1)!=OS.getInstance().currentProcess){
                        for(int i=0;i<1024;i++){
                            if(InteractiveList.get(check1).pageArray[i]!=null){
                                if(InteractiveList.get(check1).pageArray[i].physicalPageNumber!=-1){
                                    return InteractiveList.get(check1); // returning an interactive process
                                }
                            }
                            else {
                                break;
                            }
                        }
                    }
                }
            }
            else if(check>=30&&check<60){//checking for a background process 30% of the time
                if(BackgroundList.size()>0){
                    int check1=r.nextInt(BackgroundList.size());
                    if(BackgroundList.get(check1)!=OS.getInstance().currentProcess){
                        for(int i=0;i<1024;i++){
                            if(BackgroundList.get(check1).pageArray[i]!=null){
                                if(BackgroundList.get(check1).pageArray[i].physicalPageNumber!=-1){
                                    return BackgroundList.get(check1);
                                }
                            }
                            else {
                                break;
                            }
                        }
                    }
                }
            }
            else { // checking for a sleeping process 40% of the time
                if(waitList.size()>0){

                    int check1=r.nextInt(waitList.size());
                    if(waitList.get(check1)!=OS.getInstance().currentProcess){
                        for(int i=0;i<1024;i++){
                            if(waitList.get(check1).pageArray[i]!=null){
                                if(waitList.get(check1).pageArray[i].physicalPageNumber!=-1){
                                    return waitList.get(check1);
                                }
                            }
                            else {
                                break;
                            }
                        }
                    }

                }
            }
        }


    }
}
