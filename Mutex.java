public interface Mutex {
    int AttachToMutex(String name);
    boolean Lock(int mutexId);
    void Unlock(int mutexId);
    void ReleaseMutex(int mutexId);
}
