public class Startup {
    public static void main(String[] args) {
        OS os= OS.getInstance(); //retrieving the OS singleton Object (starting the OS)
        int pid1= os.CreateProcess(new HelloWorldProcess(), PriorityEnum.RealTime);
        int pid2=os.CreateProcess(new GoodbyeWorldProcess(), PriorityEnum.Interactive);
        int pid3= os.CreateProcess(new HelloWorldProcess(), PriorityEnum.Background);

        os.run();

    }
}
