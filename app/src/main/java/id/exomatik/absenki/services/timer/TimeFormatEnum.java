package id.exomatik.absenki.services.timer;

public enum TimeFormatEnum {
    MILLIS,
    SECONDS,
    MINUTES,
    HOUR,
    DAY;

    public String canonicalForm() {
        return this.name();
    }

    public static id.exomatik.absenki.services.timer.TimeFormatEnum fromCanonicalForm(String canonical) {
        return valueOf(id.exomatik.absenki.services.timer.TimeFormatEnum.class, canonical);
    }
}
