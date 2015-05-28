package black.door.json.tokens;

/**
 * Created by nfischer on 5/25/15.
 */
public class StringToken implements Token {

    String content;

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String evalute() {
        return null;
    }

    public String toString(){
        return "string \""+content+"\"";
    }
}
