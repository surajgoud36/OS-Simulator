import java.util.*;
import java.net.*;
import java.nio.*;
import java.io.*;
public class RandomDevice implements Device {
    public static Random randomDeviceArray[] = new Random[10];// Array to store Java.util.Random objects
    public static HashMap<Integer, byte[]> RandomArrayList=new HashMap<Integer, byte[]>(); // Hashmap to store the byte array of random values generated by respective Random object
    public int Open(String s){
        int index=0;
        long l = Long.parseLong(s); // storing the Seed value as long Integer
        while(randomDeviceArray[index]!=null&& index<9){// To find which index in the array is free to store the next object
            index++;
        }
        Random randomDevice = new Random(l); // generating the random object using the seed
        randomDeviceArray[index]=randomDevice;
        return index;
    }

    public byte[] Read(int id,int size){
        long Size=size;
        int[] randomNumbersArray = randomDeviceArray[id].ints(Size, 10, 100).toArray();// generating random integer values where count is equal to size

        ByteBuffer byteBuffer = ByteBuffer.allocate(randomNumbersArray.length * 4);// using byteBuffer to convert Integer array to byte array
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(randomNumbersArray);
        byte[] RandomValues = byteBuffer.array();// integer array stored as byte array
        RandomArrayList.put(id,RandomValues);// storing the byte array of random values in hashmap which is associated to its random object by means of index value of the Random object array
        return RandomValues;
    }
    public int Write(int id, byte[] data){
        return 0;
    }
    public void Seek(int id,int to){
        long To=to;
        byte[] currentRandomNumberArray=RandomArrayList.get(id); // retrieving the generated list of random number
        InputStream RandomNumberStream = new ByteArrayInputStream(currentRandomNumberArray); // using input stream to seek up to "to" size
        try {
            RandomNumberStream.reset();
            RandomNumberStream.skip(To);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void Close(int id){
        randomDeviceArray[id]=null; // removing the object Random
        RandomArrayList.remove(id);
    }
}