package mb.spoofax.api.parse;

import java.io.Serializable;

public interface TokenType extends Serializable {
    void accept(TokenKindVisitor visitor, Token token);
}
