package mb.spoofax.runtime.sdf;

import java.util.ArrayList;

import javax.annotation.Nullable;

import org.spoofax.interpreter.terms.IStrategoTerm;

import mb.spoofax.api.message.Msg;
import mb.spoofax.api.parse.Token;

public class ParseOutput {
    public final boolean recovered;
    public final @Nullable IStrategoTerm ast;
    public final @Nullable ArrayList<Token> tokenStream;
    public final ArrayList<Msg> messages;


    public ParseOutput(boolean recovered, @Nullable IStrategoTerm ast, @Nullable ArrayList<Token> tokenStream,
        ArrayList<Msg> messages) {
        this.recovered = recovered;
        this.ast = ast;
        this.tokenStream = tokenStream;
        this.messages = messages;
    }
}