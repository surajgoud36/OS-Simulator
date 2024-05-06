import java.util.ArrayList;

public class MutexObject implements Mutex {
   MutexInstance[] mutexes; // array of mutex instances
    public MutexObject(){
        mutexes=new MutexInstance[10];
        for(int i=0;i<10;i++){
            MutexInstance obj=new MutexInstance(false); // instantiaing mutex objects
            mutexes[i]=obj;
        }
    }

    public int AttachToMutex(String name){
        boolean exists=false; // to check if mutex name already exists
        int id=0; // to return the mutex's index value from the mutex object array
        for(int i=0;i<10;i++){
            if((mutexes[i].mutexName!=null)&&(mutexes[i].mutexName.equals(name))){
                mutexes[i].attachList.add(OS.getInstance().currentProcess); // if mutex name exists we add it to the mutex's attach list
                id=i;
                exists=true;
                break;
            }
        }
        if(exists==false){ // picking an unused mutex assigning it a name and then attaching
            for(int i=0;i<10;i++){
                if(mutexes[i].mutexName==null){
                    mutexes[i].mutexName=name;
                    mutexes[i].attachList.add(OS.getInstance().currentProcess);
                    id=i;
                    exists=true;
                    break;
                }
            }
        }
        return id; // returning the array index
    }
    public void ReleaseMutex(int mutexId){
        for(int i=0;i<mutexes[mutexId].attachList.size();i++){
            if(mutexes[mutexId].attachList.get(i)==OS.getInstance().freeMutex){ // OS.getInstance().freeMutex holds the current kerneland object which needs to be freed from the attach list
                mutexes[mutexId].attachList.remove(i); // removing the process from the attached list
                break;
            }
        }
        for(int i=0;i<mutexes[mutexId].waitList.size();i++){ // Removing if any from the wait list of that mutex
            if(mutexes[mutexId].waitList.get(i)==OS.getInstance().freeMutex){
                mutexes[mutexId].waitList.remove(i);
                break;
            }
        }
        if(mutexes[mutexId].attachList.size()==0){ // if no processes are attached to the mutex we clear the name information
            mutexes[mutexId].mutexName=null;
            mutexes[mutexId].state=false;
        }
    }
    public boolean Lock(int mutexId){
        KernelandProcess currentProcess=OS.getInstance().currentProcess; // current kerneland process which wants to lock the mutex
        for(int i: currentProcess.mutexList.keySet()){ // iterating through it's mutex list to check if it already has a lock on the current mutex
            if(mutexId==i && currentProcess.mutexList.get(i)) // If the processes has already locked the mutex we just simply return true
                return true;
        }
        if(mutexes[mutexId].state==true) // If its locked already we return false
            return false;
        else { // if the mutex is free we lock it and return true
            mutexes[mutexId].state=true;
            return true;
        }

    }

    public void Unlock(int mutexId){
        KernelandProcess currentProcess=OS.getInstance().unlockMutex; // current kerneland process which wants to unlock the mutex
        for(int i: currentProcess.mutexList.keySet()){ // iterating its mutex list to check if it holds the lock on mutex
            if((mutexId==i && currentProcess.mutexList.get(i))){ // we proceed only when it holds the lock on the mutex
                mutexes[mutexId].state=false; // unlock the mutex
                currentProcess.mutexList.put(i,false); // update the mutex list of the process
                if(mutexes[mutexId].waitList.size()>0){ // pick a new process which has been waiting to lock the mutex if any
                    KernelandProcess waitToRunnable=mutexes[mutexId].waitList.get(0);
                    mutexes[mutexId].waitList.remove(0);
                    waitToRunnable.mutexList.put(mutexId,true); // locking the mutex for the new process and accordingly updating its kernaland mutex list
                    mutexes[mutexId].state=true; // locking the mutex
                    if(waitToRunnable.priority==PriorityEnum.RealTime) // putting it back into its respective runnable queue based on its priority
                        OS.getInstance().scheduler.RealtimeList.add(waitToRunnable);
                    else if(waitToRunnable.priority==PriorityEnum.Interactive)
                        OS.getInstance().scheduler.InteractiveList.add(waitToRunnable);
                    else
                        OS.getInstance().scheduler.BackgroundList.add(waitToRunnable);
                }
                break;
            }
        }
    }
}
