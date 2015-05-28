package black.door.json.tokens;

/**
 * Created by nfischer on 5/25/15.
 */
public enum SyntaxToken implements Token{
    COLON(":"), OPEN_CURL("{"), CLOSE_CURL("}"), COMMA(","), OPEN_SQUARE("["), CLOSE_SQUARE("]"), QUOTE("\"");

    String content;

    SyntaxToken(String val){
        content = val;
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public void setContent(String content) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SyntaxToken evalute() {
        return this;
    }

    @Override
    public String toString() {
        return "syntax " + content;
    }
}
