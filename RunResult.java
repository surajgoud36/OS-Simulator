public class RunResult {
    public boolean ranToTimeout;
    public int millisecondsUsed;
    public RunResult(boolean ranToTimeout,int millisecondsUsed) {
        this.ranToTimeout=ranToTimeout;
        this.millisecondsUsed=millisecondsUsed;
    }
}
