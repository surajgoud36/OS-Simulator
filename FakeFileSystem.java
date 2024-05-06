import java.io.*;
import java.nio.charset.StandardCharsets;
public class FakeFileSystem implements Device{
    //public  RandomAccessFile FileSystem[]=new RandomAccessFile[10]; // Array of Random access file objects
    public RandomAccessFile FileSystem[];
    public FakeFileSystem(){
        FileSystem=new RandomAccessFile[10];
    }
    public int Open(String s){
        int index=0;
        while(FileSystem[index]!=null&& index<10){// To find which index in the array is free to store the next object
            index++;
        }
        try{
            RandomAccessFile newFile= new RandomAccessFile(s,"rw"); // creating a File object in read write mode
            FileSystem[index]=newFile;
        }
        catch (IOException ex) {
            System.out.println("Something went Wrong");
            ex.printStackTrace();
        }

        return index;
    }
    public byte[] Read(int id,int size){
        long pos=0;
        byte[] DataRead=new byte[size];// creating a byte array of size "size" into which the data should be read
        try{
            FileSystem[id].seek(pos);

            int p=FileSystem[id].read(DataRead);

        }
        catch (IOException ex) {
            System.out.println("Something went Wrong");
            ex.printStackTrace();
        }
        return DataRead;// return the byte array containing the data

    }
    public int Write(int id, byte[] data){
        String s = new String(data, StandardCharsets.UTF_8);// converting the byte[] data to string to write into the file
        try{

            FileSystem[id].writeUTF(s);

        }
        catch (IOException ex) {
            System.out.println("Something went Wrong");
            ex.printStackTrace();
        }
        return s.length();// returning the number of characters returned
    }
    public void Seek(int id,int to){
        long position=to;
        try{
            FileSystem[id].seek(position); // setting the file descriptor to the position specified by the input parameter
        }
        catch (IOException ex) {
            System.out.println("Something went Wrong");
            ex.printStackTrace();
        }
    }
    public void Close(int id){
        try{
            FileSystem[id].close(); // closing using file descriptor
            FileSystem[id]=null; // removing from the array
        }catch (IOException ex) {
            System.out.println("Something went Wrong");
            ex.printStackTrace();
        }

    }
}
