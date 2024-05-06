import java.nio.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.io.*;
public class PipeInstance {
    byte[] pipe; // Pipe instance which is a byte array
    public PipeInstance(){
        this.pipe="".getBytes(StandardCharsets.UTF_8);
    }
    public int Open(String s){
        try{
          //  this.pipe=s.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e){
            e.printStackTrace();
        }

        return 0;
    }
    public byte[] Read(int id,int size){
        byte[] DataRead = new byte[size]; // a byte array of specified size to store and return the data to be read
        ByteArrayInputStream byteArrayInputStr = new ByteArrayInputStream(this.pipe);// using ByteArrayInputStrean to get the required size of bytes from the pipe instance
        int total_bytes = byteArrayInputStr.read(DataRead, 1, size);
        return DataRead;
    }
    public void Seek(int id,int to){
        long To=to;

        InputStream RandomNumberStream = new ByteArrayInputStream(this.pipe); // using input stream to seek up to "to" size
        try {
            RandomNumberStream.reset();
            RandomNumberStream.skip(To); // seeking bytes upto "to"
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public int Write(int id, byte[] data){
        byte[] WriteToPipe = new byte[this.pipe.length + data.length];// new byte array to store the existing plus the new data to be written to the pipe instance
        System.arraycopy(this.pipe, 0, WriteToPipe, 0, this.pipe.length);// copying contents of existing pipe data
        System.arraycopy(data, 0, WriteToPipe, this.pipe.length, data.length);// writing new content to the pipe
        this.pipe=WriteToPipe;// modifying the pipe instance to store the new augmented pipe data value
        return 0;
    }
}
