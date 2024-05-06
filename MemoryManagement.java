import java.util.*;
public class MemoryManagement implements MemoryInterface{
    byte[][] memory; //Memory Storage for the processes
    BitSet freeList; // BitSet object to keep track of the free pages
    int tlbVirtual;
    int tlbPhysical;
    FakeFileSystem swapFile; // Fake file system object to access the random access file object for the disk
    int diskPageNumber; // To keep track of the next free memory address in the disk
    int swapFileIndex; // index of random access file array where the disk's random access file object is stored
    public MemoryManagement(){
        memory=new byte[1024][1024];
        freeList=new BitSet(1024);
        tlbPhysical=-1;
        tlbVirtual=-1;
       // ffs=OS.getInstance().getFileSystem();
        //swapFile=ffs.Open("swapFile");
        diskPageNumber=0;
    }
    public int sbrk(int amount){
        KernelandProcess CurrentProcess=OS.getInstance().currentProcess; // kerneland object of current executing process
        int allocatedPages=0;
        for(int i=0;i<1024;i++){// finding previously allocated pages
            if(CurrentProcess.pageArray[i]==null)
                break;
            allocatedPages++;
        }
        int allocatedMemory=allocatedPages*1024;// finding previously allocated memory
        int reqPages=amount/1024; // finding required no of memory pages
        int reqOffset=amount%1024;
        if(reqOffset>0)
            reqPages++;

        for(int i=0;i<reqPages;i++){
            int pageArrayIndex=0;
            while(CurrentProcess.pageArray[pageArrayIndex]!=null)
                pageArrayIndex++;
            VirtualToPhysicalMapping obj=new VirtualToPhysicalMapping(true,-1,-1); // promising the memory by creating virtualToPhysical Mappiing objects
            CurrentProcess.pageArray[pageArrayIndex]=obj; // allocating the object in the page array of the kerneland process
        }
        return allocatedMemory; // returns 0 bytes for first time and the begining of newly allocated address from 2nd time onwards
    }
    public void WriteMemory(int address,byte value) throws RescheduleException {
        int page=address/1024; // finding the virtual page number
        int offset=address%1024; // finding the offset
        int memoryPage; // to get the physical page number
        swapFile=OS.getInstance().getFileSystem(); // retrieves the fake file system object
        swapFileIndex=OS.getInstance().getSwapFileIndex(); // retrieves the index of the filesystem array where disk's Random access file is stored
        KernelandProcess CurrentProcess=OS.getInstance().currentProcess;
        RescheduleException rexception=new RescheduleException(); // creating exception object
        if(tlbVirtual!=page){
            memoryPage=virtualToPhysicalMapping(page); // getting the physical page number
          //  if(memoryPage==-1)
            //    throw rexception;
            tlbVirtual=page;
            tlbPhysical=memoryPage;
        }
        else{
            memoryPage=tlbPhysical; // getting the physical page number
        }
        if(memoryPage!=-1){ // If it's physical page exists in the memory
            memory[memoryPage][offset]=value;
            CurrentProcess.pageArray[page].isDirty=true; // is dirty set to true when write happens
        }
        else { // when physical page is not allocated in the memory for the current process
            int nextFreePage=freeList.nextClearBit(0); // getting physcial page number using the bit-set class
            if(nextFreePage<1024){ // if memory management has any space
                int diskNum=virtualToDiskMapping(page); // getting the disk block number for the current page
                if(diskNum==-1){ // page not been written to disk before
                    CurrentProcess.pageArray[page].physicalPageNumber=nextFreePage;
                    freeList.set(nextFreePage);
                    tlbVirtual=page;
                    tlbPhysical=nextFreePage;
                    memory[tlbPhysical][offset]=value;
                    CurrentProcess.pageArray[page].isDirty=true;
                }
                else{ // if the current page has been written to disk before we need to retrieve the contents from the disk
                    long pos=diskNum*1024; // getting it's position in the random access file
                    try{
                        swapFile.FileSystem[swapFileIndex].seek(pos); // seeking to the respective memory bytes
                        for(int i=0;i<1024;i++){
                            memory[nextFreePage][i]=swapFile.FileSystem[swapFileIndex].readByte(); // loading data from disk to memory
                        }
                        CurrentProcess.pageArray[page].physicalPageNumber=nextFreePage; // setting the physical page number
                        freeList.set(nextFreePage); // marking it in the bitset class
                        tlbVirtual=page;
                        tlbPhysical=nextFreePage;
                        memory[tlbPhysical][offset]=value;
                        CurrentProcess.pageArray[page].isDirty=true;
                    } catch (Exception e){
                        e.printStackTrace();
                    }

                }
            }
            else{ // when there is no space in the memory
                KernelandProcess randomProcess=OS.getInstance().getRandomProcess(); // getting random process with some physical pages in the memory
                ArrayList<Integer> track=new ArrayList<Integer>(); // to gather pages having physical pages in the memory
                for(int i=0;i<1024;i++){
                    if(randomProcess.pageArray[i]!=null){
                        if(randomProcess.pageArray[i].physicalPageNumber!=-1)
                            track.add(i);
                    }
                    else
                        break;
                }
                int pageIndex=getRandomPageIndex(track); // getting an index for picking a random page
                VirtualToPhysicalMapping randomPage=randomProcess.pageArray[pageIndex]; // storing the random page's Virtual to physical mapping reference in random Page
                int physicalPageNumber=randomPage.physicalPageNumber;
                if(randomPage.diskBlockNumber==-1 || (randomPage.diskBlockNumber!=-1 && randomPage.isDirty==true) ){ // Checking if to write the page to disk
                    int position=diskPageNumber*1024; // using this seek to the next free address to write the contents

                    try{
                        swapFile.FileSystem[swapFileIndex].seek(position);
                        for(int i=0;i<1024;i++){
                            swapFile.FileSystem[swapFileIndex].write(memory[physicalPageNumber][i]); // writing the contents to disk
                        }
                        diskPageNumber++;
                        randomPage.physicalPageNumber=-1;
                        randomPage.diskBlockNumber=diskPageNumber-1; // updating the disk block number after being written to disk
                        randomPage.isDirty=false; // setting it false
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
               else if(randomPage.diskBlockNumber!=-1 && randomPage.isDirty==false){ // as is dirty is false we need to write that to disk again
                   randomPage.physicalPageNumber=-1; // unmapping the physical page number
                }
               int diskNum=virtualToDiskMapping(page); // getting the disk block number for the current process to be written
               if(diskNum==-1){ // if it has not been written to disk before
                   for(int i=0;i<1024;i++){
                       memory[physicalPageNumber][i]=0; // zeroing out the physical page
                   }
                   CurrentProcess.pageArray[page].physicalPageNumber=physicalPageNumber; // allocating the physical page
                   tlbVirtual=page;
                   tlbPhysical=physicalPageNumber;
                   memory[physicalPageNumber][offset]=value;
                   CurrentProcess.pageArray[page].isDirty=true;
               }
               else{
                   long pos=diskNum*1024; // If it has been written to disk we need to load the disk contents before we proceed to write
                   try{
                       swapFile.FileSystem[swapFileIndex].seek(pos);
                       for(int i=0;i<1024;i++){
                           memory[physicalPageNumber][i]=swapFile.FileSystem[swapFileIndex].readByte(); // loading the disk contents into the page
                       }
                       CurrentProcess.pageArray[page].physicalPageNumber=physicalPageNumber;  // allocating the physical page
                       tlbVirtual=page;
                       tlbPhysical=physicalPageNumber;
                       memory[physicalPageNumber][offset]=value;
                       CurrentProcess.pageArray[page].isDirty=true;
                   } catch (Exception e){
                       e.printStackTrace();
                   }
               }
            }
        }


    }
    public byte ReadMemory(int address) throws RescheduleException {
        int page = address / 1024; // finding the virtual page number
        int offset = address % 1024; // finding the offset
        int memoryPage;
        swapFile=OS.getInstance().getFileSystem();
        swapFileIndex=OS.getInstance().getSwapFileIndex();
        KernelandProcess CurrentProcess = OS.getInstance().currentProcess;
        RescheduleException rexception = new RescheduleException(); // creating exception object
        if (tlbVirtual != page) {
            memoryPage = virtualToPhysicalMapping(page); //getting the physical page number

            tlbVirtual = page;
            tlbPhysical = memoryPage;
        } else {
            memoryPage = tlbPhysical; // getting the physical page number
        }
        if (memoryPage != -1) { // if it's physical page exists in the memory
            return memory[memoryPage][offset];

        } else {
            int nextFreePage = freeList.nextClearBit(0);
            if (nextFreePage < 1024) {
                int diskNum = virtualToDiskMapping(page);
                if (diskNum == -1) {
                    CurrentProcess.pageArray[page].physicalPageNumber = nextFreePage;
                    freeList.set(nextFreePage);
                    tlbVirtual = page;
                    tlbPhysical = nextFreePage;
                    memoryPage=tlbPhysical;
                    return memory[tlbPhysical][offset];
                    // CurrentProcess.pageArray[page].isDirty=true;
                } else {
                    long pos = diskNum * 1024; //getting the position in the random access file
                    try {
                        swapFile.FileSystem[swapFileIndex].seek(pos); // setting the file pointer appropriately
                        for (int i = 0; i < 1024; i++) {
                            memory[nextFreePage][i] = swapFile.FileSystem[swapFileIndex].readByte(); // loading the contents from the disk

                        }
                        CurrentProcess.pageArray[page].physicalPageNumber = nextFreePage;// allocating the physical page
                        freeList.set(nextFreePage); // locking the physical page number index
                        tlbVirtual = page;
                        tlbPhysical = nextFreePage;
                        memoryPage=tlbPhysical;
                        return memory[tlbPhysical][offset];
                        // CurrentProcess.pageArray[page].isDirty=true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            } else {  // when there is no space in the memory
                KernelandProcess randomProcess = OS.getInstance().getRandomProcess();// get random process
                ArrayList<Integer> track = new ArrayList<Integer>(); // track pages with physical memory
                for (int i = 0; i < 1024; i++) {
                    if (randomProcess.pageArray[i] != null) {
                        if (randomProcess.pageArray[i].physicalPageNumber != -1)
                            track.add(i);
                    } else
                        break;
                }
                int pageIndex = getRandomPageIndex(track);// get random page
                VirtualToPhysicalMapping randomPage = randomProcess.pageArray[pageIndex];// storing the random physical to virtual mapping object
                int physicalPageNumber = randomPage.physicalPageNumber; // storing the required physical page number
                if (randomPage.diskBlockNumber == -1
                        || (randomPage.diskBlockNumber != -1 && randomPage.isDirty == true)) { // condition necessary to write that random page to disk
                    int position = diskPageNumber * 1024; // getting the position of next free memory in the disk

                    try {
                        swapFile.FileSystem[swapFileIndex].seek(position);
                        for (int i = 0; i < 1024; i++) {
                            swapFile.FileSystem[swapFileIndex].write(memory[physicalPageNumber][i]); // writing it to disk
                        }
                        diskPageNumber++; // incrementing the disk page number
                        randomPage.physicalPageNumber = -1;
                        randomPage.diskBlockNumber = diskPageNumber - 1;
                        randomPage.isDirty = false; // dirty flag is reset
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (randomPage.diskBlockNumber != -1 && randomPage.isDirty == false) { // random page need not be written
                    randomPage.physicalPageNumber = -1;
                }
                int diskNum = virtualToDiskMapping(page);
                if (diskNum == -1) {
                    for (int i = 0; i < 1024; i++) {
                        memory[physicalPageNumber][i] = 0; // zeroing out the previous data
                    }
                    CurrentProcess.pageArray[page].physicalPageNumber = physicalPageNumber;
                    tlbVirtual = page;
                    tlbPhysical = physicalPageNumber;
                    memoryPage=physicalPageNumber;
                    return memory[physicalPageNumber][offset];
                    // CurrentProcess.pageArray[page].isDirty=true;
                } else { // loading the content of the disk if the page has been written to disk before
                    long pos = diskNum * 1024;
                    try {
                        swapFile.FileSystem[swapFileIndex].seek(pos);
                        for (int i = 0; i < 1024; i++) {
                            memory[physicalPageNumber][i] = swapFile.FileSystem[swapFileIndex].readByte();
                        }
                        CurrentProcess.pageArray[page].physicalPageNumber = physicalPageNumber;
                        tlbVirtual = page;
                        tlbPhysical = physicalPageNumber;
                        memoryPage=physicalPageNumber;
                        return memory[physicalPageNumber][offset];
                        // CurrentProcess.pageArray[page].isDirty=true;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return memory[memoryPage][offset];


    }
    public void invalidateTLB(){

        tlbPhysical=-1;
        tlbVirtual=-1;
    }
    public void freeMemory(){
        KernelandProcess CurrentProcess=OS.getInstance().delProcess; // getting the kerneland object whose memory is to be freed
        invalidateTLB(); // invalidating it's TLB
        for(int i=0;i<CurrentProcess.pageArray.length;i++){ // getting the physical pages from it's page array to be freed.
            if(CurrentProcess.pageArray[i]!=null){
                int Physicalpage=CurrentProcess.pageArray[i].physicalPageNumber;
                if(Physicalpage!=-1)
                    freeList.clear(Physicalpage); // deallocating the page
               // freeList.clear(Physicalpage); // deallocating the page
                CurrentProcess.pageArray[i]=null; // deleting the virtualToPhyscialMapping object
            }
        }
    }
    public int virtualToPhysicalMapping(int virtual) throws RescheduleException{ // to get physical page number
        VirtualToPhysicalMapping obj=OS.getInstance().currentProcess.pageArray[virtual];
        RescheduleException rexception=new RescheduleException();
        if(obj==null)
            throw rexception;
        return obj.physicalPageNumber;

    }
    public int virtualToDiskMapping(int virtual) throws RescheduleException{ // to get disk block number
        VirtualToPhysicalMapping obj=OS.getInstance().currentProcess.pageArray[virtual];
        RescheduleException rexception=new RescheduleException();
        if(obj==null)
            throw rexception;
        return obj.diskBlockNumber;

    }
    public int getRandomPageIndex(ArrayList<Integer> randomPages){ // function that returns a random index for selecting random page
        Random r =new Random();
        return r.nextInt(randomPages.size());
    }
}
