

package mars.venus.editors.jeditsyntax.tokenmarker;

import mars.mips.hardware.Register;
import mars.mips.hardware.Coprocessor1;
import mars.mips.hardware.RegisterFile;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.HashMap;
import mars.assembler.Directives;
import mars.venus.editors.jeditsyntax.PopupHelpItem;
import mars.mips.instructions.BasicInstruction;
import mars.mips.instructions.Instruction;
import mars.Globals;
import java.util.ArrayList;
import mars.assembler.TokenTypes;
import javax.swing.text.Segment;
import mars.venus.editors.jeditsyntax.KeywordMap;

public class MIPSTokenMarker extends TokenMarker
{
    private static KeywordMap cKeywords;
    private static String[] tokenLabels;
    private static String[] tokenExamples;
    private KeywordMap keywords;
    private int lastOffset;
    private int lastKeyword;
    
    public MIPSTokenMarker() {
        this(getKeywords());
    }
    
    public MIPSTokenMarker(final KeywordMap keywords) {
        this.keywords = keywords;
    }
    
    public static String[] getMIPSTokenLabels() {
        if (MIPSTokenMarker.tokenLabels == null) {
            (MIPSTokenMarker.tokenLabels = new String[12])[1] = "Comment";
            MIPSTokenMarker.tokenLabels[3] = "String literal";
            MIPSTokenMarker.tokenLabels[4] = "Character literal";
            MIPSTokenMarker.tokenLabels[5] = "Label";
            MIPSTokenMarker.tokenLabels[6] = "MIPS instruction";
            MIPSTokenMarker.tokenLabels[7] = "Assembler directive";
            MIPSTokenMarker.tokenLabels[8] = "Register";
            MIPSTokenMarker.tokenLabels[10] = "In-progress, invalid";
            MIPSTokenMarker.tokenLabels[11] = "Macro parameter";
        }
        return MIPSTokenMarker.tokenLabels;
    }
    
    public static String[] getMIPSTokenExamples() {
        if (MIPSTokenMarker.tokenExamples == null) {
            (MIPSTokenMarker.tokenExamples = new String[12])[1] = "# Load";
            MIPSTokenMarker.tokenExamples[3] = "\"First\"";
            MIPSTokenMarker.tokenExamples[4] = "'\\n'";
            MIPSTokenMarker.tokenExamples[5] = "main:";
            MIPSTokenMarker.tokenExamples[6] = "lui";
            MIPSTokenMarker.tokenExamples[7] = ".text";
            MIPSTokenMarker.tokenExamples[8] = "$zero";
            MIPSTokenMarker.tokenExamples[10] = "\"Regi";
            MIPSTokenMarker.tokenExamples[11] = "%arg";
        }
        return MIPSTokenMarker.tokenExamples;
    }
    
    public byte markTokensImpl(byte token, final Segment line, final int lineIndex) {
        final char[] array = line.array;
        final int offset = line.offset;
        this.lastOffset = offset;
        this.lastKeyword = offset;
        final int length = line.count + offset;
        boolean backslash = false;
    Label_0568:
        for (int i = offset; i < length; ++i) {
            final int i2 = i + 1;
            final char c = array[i];
            if (c == '\\') {
                backslash = !backslash;
            }
            else {
                switch (token) {
                    case 0: {
                        switch (c) {
                            case '\"': {
                                this.doKeyword(line, i, c);
                                if (backslash) {
                                    backslash = false;
                                    continue;
                                }
                                this.addToken(i - this.lastOffset, token);
                                token = 3;
                                final int n = i;
                                this.lastKeyword = n;
                                this.lastOffset = n;
                                continue;
                            }
                            case '\'': {
                                this.doKeyword(line, i, c);
                                if (backslash) {
                                    backslash = false;
                                    continue;
                                }
                                this.addToken(i - this.lastOffset, token);
                                token = 4;
                                final int n2 = i;
                                this.lastKeyword = n2;
                                this.lastOffset = n2;
                                continue;
                            }
                            case ':': {
                                backslash = false;
                                boolean validIdentifier = false;
                                try {
                                    validIdentifier = TokenTypes.isValidIdentifier(new String(array, this.lastOffset, i2 - this.lastOffset - 1).trim());
                                }
                                catch (StringIndexOutOfBoundsException e) {
                                    validIdentifier = false;
                                }
                                if (validIdentifier) {
                                    this.addToken(i2 - this.lastOffset, (byte)5);
                                    final int n3 = i2;
                                    this.lastKeyword = n3;
                                    this.lastOffset = n3;
                                    continue;
                                }
                                continue;
                            }
                            case '#': {
                                backslash = false;
                                this.doKeyword(line, i, c);
                                if (length - i >= 1) {
                                    this.addToken(i - this.lastOffset, token);
                                    this.addToken(length - i, (byte)1);
                                    final int n4 = length;
                                    this.lastKeyword = n4;
                                    this.lastOffset = n4;
                                    break Label_0568;
                                }
                                continue;
                            }
                            default: {
                                backslash = false;
                                if (!Character.isLetterOrDigit(c) && c != '_' && c != '.' && c != '$' && c != '%') {
                                    this.doKeyword(line, i, c);
                                    continue;
                                }
                                continue;
                            }
                        }
                        break;
                    }
                    case 3: {
                        if (backslash) {
                            backslash = false;
                            break;
                        }
                        if (c == '\"') {
                            this.addToken(i2 - this.lastOffset, token);
                            token = 0;
                            final int n5 = i2;
                            this.lastKeyword = n5;
                            this.lastOffset = n5;
                            break;
                        }
                        break;
                    }
                    case 4: {
                        if (backslash) {
                            backslash = false;
                            break;
                        }
                        if (c == '\'') {
                            this.addToken(i2 - this.lastOffset, (byte)3);
                            token = 0;
                            final int n6 = i2;
                            this.lastKeyword = n6;
                            this.lastOffset = n6;
                            break;
                        }
                        break;
                    }
                    default: {
                        throw new InternalError("Invalid state: " + token);
                    }
                }
            }
        }
        if (token == 0) {
            this.doKeyword(line, length, '\0');
        }
        switch (token) {
            case 3:
            case 4: {
                this.addToken(length - this.lastOffset, (byte)10);
                token = 0;
                return token;
            }
            case 7: {
                this.addToken(length - this.lastOffset, token);
                if (!backslash) {
                    token = 0;
                    break;
                }
                break;
            }
        }
        this.addToken(length - this.lastOffset, token);
        return token;
    }
    
    @Override
    public ArrayList getTokenExactMatchHelp(final Token token, final String tokenText) {
        ArrayList matches = null;
        if (token != null && token.id == 6) {
            final ArrayList instrMatches = Globals.instructionSet.matchOperator(tokenText);
            if (instrMatches.size() > 0) {
                int realMatches = 0;
                matches = new ArrayList();
                for (int i = 0; i < instrMatches.size(); ++i) {
                    final Instruction inst = instrMatches.get(i);
                    if (Globals.getSettings().getExtendedAssemblerEnabled() || inst instanceof BasicInstruction) {
                        matches.add(new PopupHelpItem(tokenText, inst.getExampleFormat(), inst.getDescription()));
                        ++realMatches;
                    }
                }
                if (realMatches == 0) {
                    matches.add(new PopupHelpItem(tokenText, tokenText, "(is not a basic instruction)"));
                }
            }
        }
        if (token != null && token.id == 7) {
            final Directives dir = Directives.matchDirective(tokenText);
            if (dir != null) {
                matches = new ArrayList();
                matches.add(new PopupHelpItem(tokenText, dir.getName(), dir.getDescription()));
            }
        }
        return matches;
    }
    
    @Override
    public ArrayList getTokenPrefixMatchHelp(final String line, final Token tokenList, final Token token, final String tokenText) {
        final ArrayList matches = null;
        if (tokenList == null || tokenList.id == 127) {
            return null;
        }
        if (token != null && token.id == 1) {
            return null;
        }
        Token tokens = tokenList;
        String keywordTokenText = null;
        byte keywordType = -1;
        int offset = 0;
        boolean moreThanOneKeyword = false;
        while (tokens.id != 127) {
            if (tokens.id == 6 || tokens.id == 7) {
                if (keywordTokenText != null) {
                    moreThanOneKeyword = true;
                    break;
                }
                keywordTokenText = line.substring(offset, offset + tokens.length);
                keywordType = tokens.id;
            }
            offset += tokens.length;
            tokens = tokens.next;
        }
        if (token != null && token.id == 6) {
            if (moreThanOneKeyword) {
                return (keywordType == 6) ? this.getTextFromInstructionMatch(keywordTokenText, true) : this.getTextFromDirectiveMatch(keywordTokenText, true);
            }
            return this.getTextFromInstructionMatch(tokenText, false);
        }
        else {
            if (token == null || token.id != 7) {
                if (keywordTokenText != null) {
                    if (keywordType == 6) {
                        return this.getTextFromInstructionMatch(keywordTokenText, true);
                    }
                    if (keywordType == 7) {
                        return this.getTextFromDirectiveMatch(keywordTokenText, true);
                    }
                }
                if (token != null && token.id == 0) {
                    final String trimmedTokenText = tokenText.trim();
                    if (keywordTokenText == null && trimmedTokenText.length() == 0) {
                        return null;
                    }
                    if (keywordTokenText == null && trimmedTokenText.length() > 0) {
                        if (trimmedTokenText.charAt(0) == '.') {
                            return this.getTextFromDirectiveMatch(trimmedTokenText, false);
                        }
                        if (trimmedTokenText.length() >= Globals.getSettings().getEditorPopupPrefixLength()) {
                            return this.getTextFromInstructionMatch(trimmedTokenText, false);
                        }
                    }
                }
                return null;
            }
            if (moreThanOneKeyword) {
                return (keywordType == 6) ? this.getTextFromInstructionMatch(keywordTokenText, true) : this.getTextFromDirectiveMatch(keywordTokenText, true);
            }
            return this.getTextFromDirectiveMatch(tokenText, false);
        }
    }
    
    private ArrayList getTextFromDirectiveMatch(final String tokenText, final boolean exact) {
        ArrayList matches = null;
        ArrayList directiveMatches = null;
        if (exact) {
            final Object dir = Directives.matchDirective(tokenText);
            if (dir != null) {
                directiveMatches = new ArrayList();
                directiveMatches.add(dir);
            }
        }
        else {
            directiveMatches = Directives.prefixMatchDirectives(tokenText);
        }
        if (directiveMatches != null) {
            matches = new ArrayList();
            for (int i = 0; i < directiveMatches.size(); ++i) {
                final Directives direct = directiveMatches.get(i);
                matches.add(new PopupHelpItem(tokenText, direct.getName(), direct.getDescription(), exact));
            }
        }
        return matches;
    }
    
    private ArrayList getTextFromInstructionMatch(final String tokenText, final boolean exact) {
        final String text = null;
        ArrayList matches = null;
        final ArrayList results = new ArrayList();
        if (exact) {
            matches = Globals.instructionSet.matchOperator(tokenText);
        }
        else {
            matches = Globals.instructionSet.prefixMatchOperator(tokenText);
        }
        if (matches == null) {
            return null;
        }
        int realMatches = 0;
        final HashMap insts = new HashMap();
        final TreeSet mnemonics = new TreeSet();
        for (int i = 0; i < matches.size(); ++i) {
            final Instruction inst = matches.get(i);
            if (Globals.getSettings().getExtendedAssemblerEnabled() || inst instanceof BasicInstruction) {
                if (exact) {
                    results.add(new PopupHelpItem(tokenText, inst.getExampleFormat(), inst.getDescription(), exact));
                }
                else {
                    final String mnemonic = inst.getExampleFormat().split(" ")[0];
                    if (!insts.containsKey(mnemonic)) {
                        mnemonics.add(mnemonic);
                        insts.put(mnemonic, inst.getDescription());
                    }
                }
                ++realMatches;
            }
        }
        if (realMatches == 0) {
            if (!exact) {
                return null;
            }
            results.add(new PopupHelpItem(tokenText, tokenText, "(not a basic instruction)", exact));
        }
        else if (!exact) {
            for (final String mnemonic2 : mnemonics) {
                final String info = insts.get(mnemonic2);
                results.add(new PopupHelpItem(tokenText, mnemonic2, info, exact));
            }
        }
        return results;
    }
    
    public static KeywordMap getKeywords() {
        if (MIPSTokenMarker.cKeywords == null) {
            MIPSTokenMarker.cKeywords = new KeywordMap(false);
            final ArrayList instructionSet = Globals.instructionSet.getInstructionList();
            for (int i = 0; i < instructionSet.size(); ++i) {
                MIPSTokenMarker.cKeywords.add(instructionSet.get(i).getName(), (byte)6);
            }
            final ArrayList directiveSet = Directives.getDirectiveList();
            for (int j = 0; j < directiveSet.size(); ++j) {
                MIPSTokenMarker.cKeywords.add(directiveSet.get(j).getName(), (byte)7);
            }
            final Register[] registerFile = RegisterFile.getRegisters();
            for (int k = 0; k < registerFile.length; ++k) {
                MIPSTokenMarker.cKeywords.add(registerFile[k].getName(), (byte)8);
                MIPSTokenMarker.cKeywords.add("$" + k, (byte)8);
            }
            final Register[] coprocessor1RegisterFile = Coprocessor1.getRegisters();
            for (int l = 0; l < coprocessor1RegisterFile.length; ++l) {
                MIPSTokenMarker.cKeywords.add(coprocessor1RegisterFile[l].getName(), (byte)8);
            }
        }
        return MIPSTokenMarker.cKeywords;
    }
    
    private boolean doKeyword(final Segment line, final int i, final char c) {
        final int i2 = i + 1;
        final int len = i - this.lastKeyword;
        final byte id = this.keywords.lookup(line, this.lastKeyword, len);
        if (id != 0) {
            if (this.lastKeyword != this.lastOffset) {
                this.addToken(this.lastKeyword - this.lastOffset, (byte)0);
            }
            this.addToken(len, id);
            this.lastOffset = i;
        }
        this.lastKeyword = i2;
        return false;
    }
    
    private boolean tokenListContainsKeyword() {
        Token token = this.firstToken;
        boolean result = false;
        String str = "";
        while (token != null) {
            str = str + "" + token.id + "(" + token.length + ") ";
            if (token.id == 6 || token.id == 7 || token.id == 8) {
                result = true;
            }
            token = token.next;
        }
        System.out.println("" + result + " " + str);
        return result;
    }
}
