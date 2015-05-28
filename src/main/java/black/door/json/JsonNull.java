package black.door.json;

/**
 * Created by nfischer on 5/25/15.
 */
public enum JsonNull {
    /**
     * to avoid confusion between the null java primitive and the null JSON type, we explicitly make a null here
     */
    NULL;

    @Override
    public String toString() {
        return this.name().toLowerCase();
    }

}
