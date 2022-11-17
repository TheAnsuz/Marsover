

package mars.assembler;

import mars.util.Binary;
import mars.Globals;
import mars.ErrorMessage;
import mars.ErrorList;
import java.util.ArrayList;

public class SymbolTable
{
    private static String startLabel;
    private String filename;
    private ArrayList<Symbol> table;
    public static final int NOT_FOUND = -1;
    
    public SymbolTable(final String filename) {
        this.filename = filename;
        this.table = new ArrayList();
    }
    
    public void addSymbol(final Token token, final int address, final boolean b, final ErrorList errors) {
        final String label = token.getValue();
        if (this.getSymbol(label) != null) {
            errors.add(new ErrorMessage(token.getSourceMIPSprogram(), token.getSourceLine(), token.getStartPos(), "label \"" + label + "\" already defined"));
        }
        else {
            final Symbol s = new Symbol(label, address, b);
            this.table.add(s);
            if (Globals.debug) {
                System.out.println("The symbol " + label + " with address " + address + " has been added to the " + this.filename + " symbol table.");
            }
        }
    }
    
    public void removeSymbol(final Token token) {
        final String label = token.getValue();
        int i = 0;
        while (i < this.table.size()) {
            if (this.table.get(i).getName().equals(label)) {
                this.table.remove(i);
                if (Globals.debug) {
                    System.out.println("The symbol " + label + " has been removed from the " + this.filename + " symbol table.");
                    break;
                }
                break;
            }
            else {
                ++i;
            }
        }
    }
    
    public int getAddress(final String s) {
        for (int i = 0; i < this.table.size(); ++i) {
            if (this.table.get(i).getName().equals(s)) {
                return this.table.get(i).getAddress();
            }
        }
        return -1;
    }
    
    public int getAddressLocalOrGlobal(final String s) {
        final int address = this.getAddress(s);
        return (address == -1) ? Globals.symbolTable.getAddress(s) : address;
    }
    
    public Symbol getSymbol(final String s) {
        for (int i = 0; i < this.table.size(); ++i) {
            if (this.table.get(i).getName().equals(s)) {
                return this.table.get(i);
            }
        }
        return null;
    }
    
    public Symbol getSymbolGivenAddress(final String s) {
        int address = 0;
        try {
            address = Binary.stringToInt(s);
        }
        catch (NumberFormatException e) {
            return null;
        }
        for (int i = 0; i < this.table.size(); ++i) {
            if (this.table.get(i).getAddress() == address) {
                return this.table.get(i);
            }
        }
        return null;
    }
    
    public Symbol getSymbolGivenAddressLocalOrGlobal(final String s) {
        final Symbol sym = this.getSymbolGivenAddress(s);
        return (sym == null) ? Globals.symbolTable.getSymbolGivenAddress(s) : sym;
    }
    
    public ArrayList getDataSymbols() {
        final ArrayList list = new ArrayList();
        for (int i = 0; i < this.table.size(); ++i) {
            if (this.table.get(i).getType()) {
                list.add(this.table.get(i));
            }
        }
        return list;
    }
    
    public ArrayList getTextSymbols() {
        final ArrayList list = new ArrayList();
        for (int i = 0; i < this.table.size(); ++i) {
            if (!this.table.get(i).getType()) {
                list.add(this.table.get(i));
            }
        }
        return list;
    }
    
    public ArrayList getAllSymbols() {
        final ArrayList list = new ArrayList();
        for (int i = 0; i < this.table.size(); ++i) {
            list.add(this.table.get(i));
        }
        return list;
    }
    
    public int getSize() {
        return this.table.size();
    }
    
    public void clear() {
        this.table = new ArrayList();
    }
    
    public void fixSymbolTableAddress(final int originalAddress, final int replacementAddress) {
        for (Symbol label = this.getSymbolGivenAddress(Integer.toString(originalAddress)); label != null; label = this.getSymbolGivenAddress(Integer.toString(originalAddress))) {
            label.setAddress(replacementAddress);
        }
    }
    
    public static String getStartLabel() {
        return SymbolTable.startLabel;
    }
    
    static {
        SymbolTable.startLabel = "main";
    }
}
