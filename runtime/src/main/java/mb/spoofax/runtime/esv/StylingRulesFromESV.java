package mb.spoofax.runtime.esv;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import mb.log.api.Logger;
import mb.spoofax.api.parse.TokenConstants;
import mb.spoofax.api.parse.TokenType;
import mb.spoofax.api.style.*;
import mb.spoofax.runtime.style.StylingRules;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;

import java.util.Map;

public class StylingRulesFromESV {
    private final Logger logger;


    @Inject
    public StylingRulesFromESV(Logger logger) {
        this.logger = logger.forContext(getClass());
    }


    public StylingRules create(IStrategoAppl esv) {
        final StylingRules styler = new StylingRules();

        final Iterable<IStrategoAppl> styleDefs = ESVReader.collectTerms(esv, "ColorDef");
        final Map<String, Style> namedStyles = Maps.newHashMap();
        for(IStrategoAppl styleDef : styleDefs) {
            final IStrategoAppl styleTerm = (IStrategoAppl) styleDef.getSubterm(1);
            final IStrategoConstructor styleCons = styleTerm.getConstructor();
            final Style style;
            if(styleCons.getName().equals("Attribute")) {
                style = style(styleTerm);
            } else if(styleCons.getName().equals("AttributeRef")) {
                final String name = Tools.asJavaString(styleTerm.getSubterm(0));
                style = namedStyles.get(name);
                if(style == null) {
                    logger.error("Cannot resolve style definition " + name + " in style definition " + styleDef);
                    continue;
                }
            } else {
                logger.error("Unhandled style " + styleCons + " in style definition " + styleDef);
                continue;
            }

            namedStyles.put(Tools.asJavaString(styleDef.getSubterm(0)), style);
        }

        final Iterable<IStrategoAppl> styleRules = ESVReader.collectTerms(esv, "ColorRule");
        for(IStrategoAppl styleRule : styleRules) {
            final IStrategoAppl styleTerm = (IStrategoAppl) styleRule.getSubterm(1);
            final IStrategoConstructor styleCons = styleTerm.getConstructor();
            final Style style;
            if(styleCons.getName().equals("Attribute")) {
                style = style(styleTerm);
            } else if(styleCons.getName().equals("AttributeRef")) {
                final String name = Tools.asJavaString(styleTerm.getSubterm(0));
                style = namedStyles.get(name);
                if(style == null) {
                    logger.error("Cannot resolve style definition " + name + " in style rule " + styleRule);
                    continue;
                }
            } else {
                logger.error("Unhandled style " + styleCons + " in style rule " + styleRule);
                continue;
            }

            final IStrategoAppl node = (IStrategoAppl) styleRule.getSubterm(0);
            final IStrategoConstructor nodeCons = node.getConstructor();
            if(nodeCons.getName().equals("SortAndConstructor")) {
                final String sort = Tools.asJavaString(node.getSubterm(0).getSubterm(0));
                final String cons = Tools.asJavaString(node.getSubterm(1).getSubterm(0));
                styler.mapSortConsToStyle(sort, cons, style);
            } else if(nodeCons.getName().equals("ConstructorOnly")) {
                final String cons = Tools.asJavaString(node.getSubterm(0).getSubterm(0));
                styler.mapConsToStyle(cons, style);
            } else if(nodeCons.getName().equals("Sort")) {
                final String sort = Tools.asJavaString(node.getSubterm(0));
                styler.mapSortToStyle(sort, style);
            } else if(nodeCons.getName().equals("Token")) {
                final IStrategoAppl tokenAppl = (IStrategoAppl) node.getSubterm(0);
                final String tokenTypeStr = tokenAppl.getConstructor().getName();
                final TokenType tokenType = tokenType(tokenTypeStr);
                styler.mapTokenTypeToStyle(tokenType, style);
            } else {
                logger.error("Unhandled node " + nodeCons + " in style rule " + styleRule);
                continue;
            }
        }

        return styler;
    }

    private static Style style(IStrategoAppl attribute) {
        final Color color = color((IStrategoAppl) attribute.getSubterm(0));
        final Color backgroundColor = color((IStrategoAppl) attribute.getSubterm(1));
        final boolean bold;
        final boolean italic;
        final boolean underline = false;
        final boolean strikeout = false;
        final IStrategoAppl fontSetting = (IStrategoAppl) attribute.getSubterm(2);
        final String fontSettingCons = fontSetting.getConstructor().getName();
        switch(fontSettingCons) {
            case "BOLD":
                bold = true;
                italic = false;
                break;
            case "ITALIC":
                bold = false;
                italic = true;
                break;
            case "BOLD_ITALIC":
                bold = true;
                italic = true;
                break;
            default:
                bold = false;
                italic = false;
                break;
        }
        return new StyleImpl(color, backgroundColor, bold, italic, underline, strikeout);
    }

    private static Color color(IStrategoAppl color) {
        final String colorCons = color.getConstructor().getName();
        switch(colorCons) {
            case "ColorRGB":
                final int r = Integer.parseInt(Tools.asJavaString(color.getSubterm(0)));
                final int g = Integer.parseInt(Tools.asJavaString(color.getSubterm(1)));
                final int b = Integer.parseInt(Tools.asJavaString(color.getSubterm(2)));
                return new Color(r, g, b);
            case "ColorDefault":
                return Color.black;
            default:
                return null;
        }
    }

    private TokenType tokenType(String str) {
        switch(str) {
            case "TK_IDENTIFIER":
                return TokenConstants.identifierType;
            case "TK_STRING":
                return TokenConstants.stringType;
            case "TK_NUMBER":
                return TokenConstants.numberType;
            case "TK_KEYWORD":
                return TokenConstants.keywordType;
            case "TK_OPERATOR":
                return TokenConstants.operatorType;
            case "TK_LAYOUT":
                return TokenConstants.layoutType;
            default:
                return TokenConstants.unknownType;
        }
    }
}
