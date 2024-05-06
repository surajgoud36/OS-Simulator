import java.util.HashMap;

public class PipeDevice implements Device{
    public static PipeInstance pipes[]= new PipeInstance[10];// Array of pipe instances
    public static HashMap<String,Integer> pipeList= new HashMap<String,Integer>();// Hashmap to map name of the pipe to the pipe instance
    public int Open(String s){
        if(pipeList.containsKey(s)){ // pipe with same name exists we just return the index
            return pipeList.get(s);
        }
        else{
            int index=0;
            while(pipes[index]!=null&& index<10){ // To find which index in the array is free to store the next object
                index++;
            }
            PipeInstance newPipe=new PipeInstance();
            pipes[index]=newPipe;
            pipeList.put(s,index);
            return index;
        }
    }
    public byte[] Read(int id,int size){
        byte[] Dataread=pipes[id].Read(id,size); // calling the read method using the pipe instance object referred by the index value id
        return Dataread;
    }
    public int Write(int id, byte[] data){
        int write=pipes[id].Write(id,data); // calling the Write method using the pipe instance object referred by the index value id
        return write;
    }
    public void Seek(int id,int to){ // calling the seek method using the pipe instance object referred by the index value id
        pipes[id].Seek(id,to);
    }
    public void Close(int id){
        for(String i: pipeList.keySet()){
            if(pipeList.get(i)==id){
                pipeList.remove(i); // removing the pipe instance
                break;
            }
        }
        pipes[id]=null;
    }
}
