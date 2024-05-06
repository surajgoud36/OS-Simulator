import java.util.*;
public class VFS implements Device{
    FakeFileSystem filesystem; // Fake File System Instance
    PipeDevice pipedevice; // Pipe device Instance
    RandomDevice randomdevice; // Random device Instance
    String[] devices; // String array to map a VFS id to a (Device/id) pair stored as a string value
    public VFS(){
        filesystem=new FakeFileSystem(); // initializing the device objects
        pipedevice=new PipeDevice();
        randomdevice=new RandomDevice();
        devices=new String[100];
    }
    public FakeFileSystem getFilesystem(){
        return filesystem;
    }
    public int Open(String s){
        StringTokenizer st=new StringTokenizer(s); // To split the incoming string
        String p1=st.nextToken();
        String p2=st.nextToken();
        int ind;
        if(p1.equals("random")){
            ind=randomdevice.Open(p2);
            int index=0;
            while(devices[index]!=null&&index<99){ // To find which index in the array is free to store the next object
                index++;
            }
            devices[index]=Integer.toString(ind); //storing the index returned
            devices[index]+=" random"; // storing device associated with the index
            return index;
        }
        else if(p1.equals("pipe")){
            ind=pipedevice.Open(p2);
            int index=0;
            while(devices[index]!=null&&index<99){// To find which index in the array is free to store the next object
                index++;
            }
            devices[index]=Integer.toString(ind);//storing the index returned
            devices[index]+=" pipe"; // storing device associated with the index
            return index;
        }
        else{
            ind=filesystem.Open(p2);
            int index=0;
            while(devices[index]!=null&&index<99){
                index++;
            }
            devices[index]=Integer.toString(ind);
            devices[index]+=" file";
            return index;
        }

    }
    public byte[] Read(int id,int size){
        StringTokenizer st=new StringTokenizer(devices[id]); // to split the device and id pair
        String t1=st.nextToken();
        int ID=Integer.parseInt(t1); // getting index integer value
        String t2=st.nextToken();
        byte[] readarray;
        if(t2.equals("random")){// calling the appropriate device based on device id pair
            readarray=randomdevice.Read(ID,size);
            return readarray;
        }
        else if(t2.equals("file")){
            readarray=filesystem.Read(ID,size);
            return readarray;
        }
        else{
            readarray=pipedevice.Read(ID,size);
            return readarray;
        }
    }
    public int Write(int id, byte[] data){
        StringTokenizer st=new StringTokenizer(devices[id]);// to split the device and id pair
        String t1=st.nextToken();
        int ID=Integer.parseInt(t1);// getting index integer value
        String t2=st.nextToken();
        int writevalue;
        if(t2.equals("random")){// calling the appropriate device based on device id pair
            writevalue=randomdevice.Write(ID,data);
            return writevalue;
        }
        else if(t2.equals("file")){
            writevalue=filesystem.Write(ID,data);
            return writevalue;
        }
        else{
            writevalue=pipedevice.Write(ID,data);
            return writevalue;
        }
    }
    public void Seek(int id,int to){
        StringTokenizer st=new StringTokenizer(devices[id]);
        String t1=st.nextToken();
        int ID=Integer.parseInt(t1);
        String t2=st.nextToken();
        if(t2.equals("random")) // seeking the appropriate device id pair
            randomdevice.Seek(ID,to);
        else if(t2.equals("file"))
            filesystem.Seek(ID,to);
        else
            pipedevice.Seek(ID,to);
    }
    public void Close(int id){
        StringTokenizer st=new StringTokenizer(devices[id]);
        String t1=st.nextToken();
        int ID=Integer.parseInt(t1);
        String t2=st.nextToken();
        if(t2.equals("random"))// closing the appropriate device id pair
            randomdevice.Close(ID);
        else if(t2.equals("file"))
            filesystem.Close(ID);
        else
            pipedevice.Close(ID);
        devices[id]=null;
    }
}
