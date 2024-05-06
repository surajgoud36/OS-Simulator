public class VirtualToPhysicalMapping {
    boolean isDirty;
    int physicalPageNumber;
    int diskBlockNumber;
    public VirtualToPhysicalMapping(boolean isDirty,int physicalPageNumber,int diskBlockNumber){
        this.isDirty=isDirty;
        this.physicalPageNumber=physicalPageNumber;
        this.diskBlockNumber=diskBlockNumber;
    }
}
