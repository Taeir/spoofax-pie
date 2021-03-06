package mb.spoofax.api.parse;

public class StringTokenKind implements TokenType {
    private static final long serialVersionUID = 1L;


    @Override public void accept(TokenKindVisitor visitor, Token token) {
        visitor.string(token);
    }


    @Override public int hashCode() {
        return 0;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        return getClass() == obj.getClass();
    }

    @Override public String toString() {
        return "string";
    }
}
