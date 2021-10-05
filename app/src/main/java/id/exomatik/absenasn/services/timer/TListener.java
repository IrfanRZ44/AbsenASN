package id.exomatik.absenasn.services.timer;

public interface TListener {
    String updateDataOnTick(long remainingTimeInMs);
    void onTimerFinished();
}
