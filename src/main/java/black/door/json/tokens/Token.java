package black.door.json.tokens;

/**
 * Created by nfischer on 5/25/15.
 */
public interface Token {

    public String getContent();

    public void setContent(String content);

    public abstract Object evalute();
}
