public class OS implements OSInterface{  //singleton class
    // create a singleton OS object
    private static OS osInstance= new OS(); // creating a singleton object
    PriorityScheduler scheduler;
    KernelandProcess currentProcess; // helper variable to store the current kernel object whose userland process is executing
    VFS vfs;
    MemoryManagement memoryManagement;
    int checkException; // varible that tells if exception has been incured in any of the userland processes
    int checkLock;
    int checkMutexId;
    KernelandProcess delProcess;// helper variable to store the kerneland object whose process is going to be delted
    int swapFileIndex;
    KernelandProcess freeMutex;
    KernelandProcess unlockMutex;
    MutexObject mutex;
    private OS(){
        scheduler=new PriorityScheduler(); // creating instance of basic scheduler
        vfs=new VFS();
        memoryManagement=new MemoryManagement();
        checkException=0;
        checkLock=0;
        checkMutexId=0;
        FakeFileSystem objn=vfs.getFilesystem();
        swapFileIndex=objn.Open("swapFile");
        mutex=new MutexObject();
    }
    public static OS getInstance(){

        return osInstance; //returning singleton OS instance to startup
    }
    public int getSwapFileIndex(){
        return swapFileIndex;
    }
    public FakeFileSystem getFileSystem(){
        FakeFileSystem objn=vfs.getFilesystem();
        return objn;
    }
    public int CreateProcess(UserlandProcess myNewProcess, PriorityEnum priority){
        int pid=scheduler.CreateProcess(myNewProcess, priority);
        return pid;
    }
    public boolean DeleteProcess(int processId){

        return scheduler.DeleteProcess(processId);
    }
    public void run(){

        scheduler.run();
    }
    public void Sleep(int milliseconds){

        scheduler.Sleep(milliseconds);
    }
    public int Open(String s){
        int val=vfs.Open(s);
        int index=0;
        while(currentProcess.deviceList[index]!=-1&&index<9){
            index++;
        }
        currentProcess.deviceList[index]=val;// updating the kernel land storage of devices for each process
        return index;
    }
    public byte[] Read(int id,int size){
        return vfs.Read(currentProcess.deviceList[id],size);
    }
    public void Seek(int id,int to){
        vfs.Seek(currentProcess.deviceList[id],to);
    }
    public int Write(int id, byte[] data){
        return vfs.Write(currentProcess.deviceList[id],data);
    }
    public void Close(int id){
        vfs.Close(currentProcess.deviceList[id]);
        currentProcess.deviceList[id]=-1;// removing the device entry from kernel land storage
    }
    public  int sbrk(int amount){
        return memoryManagement.sbrk(amount);
    }
    public byte ReadMemory(int address) throws RescheduleException{
        return memoryManagement.ReadMemory(address);
    }
    public  void WriteMemory(int address, byte value) throws RescheduleException{
        memoryManagement.WriteMemory(address,value);
    }
    public void invalidateTLB(){
        memoryManagement.invalidateTLB();
    }
    public void freeMemory(){
        memoryManagement.freeMemory();
    }
    public KernelandProcess getRandomProcess(){
        return scheduler.getRandomProcess();
    }
    public int AttachToMutex(String name) {
        return mutex.AttachToMutex(name);
    }
    public boolean Lock(int mutexId){
        return mutex.Lock(mutexId);
    }
    public  void Unlock(int mutexId){
        mutex.Unlock(mutexId);
    }
    public void ReleaseMutex(int mutexId){
        mutex.ReleaseMutex(mutexId);
    }
}
