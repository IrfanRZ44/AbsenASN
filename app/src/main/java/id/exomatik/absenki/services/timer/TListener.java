package id.exomatik.absenki.services.timer;

public interface TListener {
    String updateDataOnTick(long remainingTimeInMs);
    void onTimerFinished();
}
