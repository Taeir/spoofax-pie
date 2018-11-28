package mb.spoofax.api.module.payload;

import mb.spoofax.api.module.SModule;
import mb.spoofax.api.module.SingleFileModule;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class TextPayload extends BasePayload {
    private String text;

    public TextPayload(SingleFileModule module) {
        super(module);
    }

    public TextPayload(SModule module, String text) {
        super(module);
        this.text = text;
    }

    @Override
    public String getType() {
        return PayloadTypes.TEXT;
    }

    /**
     * Parses this text payload to an AST payload.
     *
     * @return
     *      the ASTPayload
     */
    //TODO Throws exception if parsing fails?
    public ASTPayload parse() {
        //TODO The payload must be already depended upon in PIE in order to get it?
        //TODO Execute pie task
        return null;
    }

    //TODO The text is currently read directly from the file by the parser (tokenizer), so do we even need a method for this?
    public String getText() throws IOException {
        //TODO
        if (text != null) return text;
        SModule module = getModule();
        if (module instanceof SingleFileModule) {
            byte[] bytes = ((SingleFileModule) module).getFile().readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        }
        //TODO Implement something for a file.
        throw new IllegalStateException("Module type should have a file.");
    }

    /**
     * Called whenever the text is updated.
     */
    public void updateText() {
        //TODO
    }
}
