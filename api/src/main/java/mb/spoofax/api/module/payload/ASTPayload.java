package mb.spoofax.api.module.payload;

import mb.spoofax.api.module.SModule;
import org.spoofax.interpreter.terms.IStrategoTerm;

public abstract class ASTPayload extends BasePayload {
    private IStrategoTerm ast;

    public ASTPayload(SModule module, IStrategoTerm ast) {
        super(module);
        this.ast = ast;
    }

    @Override
    public String getType() {
        return PayloadTypes.AST;
    }

    //TODO These methods should not recreate the payloads in the case it already exists.
    /**
     * Pretty prints this ASTPayload and returns the text.
     *
     * @return
     *      the pretty printed ASTPayload
     */
    public abstract PrettyPrintedPayload prettyPrint();

    public abstract TSGPayload analyzeScopeGraph();

    /**
     * @return
     *      the AST of the module this payload belongs to
     */
    public IStrategoTerm getAST() {
        return ast;
    }

    /**
     * Updates the AST to the given AST.
     *
     * @param ast
     *      the new AST
     */
    public void updateAST(IStrategoTerm ast) {
        this.ast = ast;
    }
}
