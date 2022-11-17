

package mars.tools;

import java.util.Vector;
import javax.swing.table.AbstractTableModel;

public class BHTableModel extends AbstractTableModel
{
    private Vector<BHTEntry> m_entries;
    private int m_entryCnt;
    private int m_historySize;
    private String[] m_columnNames;
    private Class[] m_columnClasses;
    
    public BHTableModel(final int numEntries, final int historySize, final boolean initVal) {
        this.m_columnNames = new String[] { "Index", "History", "Prediction", "Correct", "Incorrect", "Precision" };
        this.m_columnClasses = new Class[] { Integer.class, String.class, String.class, Integer.class, Integer.class, Double.class };
        this.initBHT(numEntries, historySize, initVal);
    }
    
    @Override
    public String getColumnName(final int i) {
        if (i < 0 || i > this.m_columnNames.length) {
            throw new IllegalArgumentException("Illegal column index " + i + " (must be in range 0.." + (this.m_columnNames.length - 1) + ")");
        }
        return this.m_columnNames[i];
    }
    
    @Override
    public Class getColumnClass(final int i) {
        if (i < 0 || i > this.m_columnClasses.length) {
            throw new IllegalArgumentException("Illegal column index " + i + " (must be in range 0.." + (this.m_columnClasses.length - 1) + ")");
        }
        return this.m_columnClasses[i];
    }
    
    @Override
    public int getColumnCount() {
        return 6;
    }
    
    @Override
    public int getRowCount() {
        return this.m_entryCnt;
    }
    
    @Override
    public Object getValueAt(final int row, final int col) {
        final BHTEntry e = this.m_entries.elementAt(row);
        if (e == null) {
            return "";
        }
        if (col == 0) {
            return new Integer(row);
        }
        if (col == 1) {
            return e.getHistoryAsStr();
        }
        if (col == 2) {
            return e.getPredictionAsStr();
        }
        if (col == 3) {
            return new Integer(e.getStatsPredCorrect());
        }
        if (col == 4) {
            return new Integer(e.getStatsPredIncorrect());
        }
        if (col == 5) {
            return new Double(e.getStatsPredPrecision());
        }
        return "";
    }
    
    public void initBHT(final int numEntries, final int historySize, final boolean initVal) {
        if (numEntries <= 0 || (numEntries & numEntries - 1) != 0x0) {
            throw new IllegalArgumentException("Number of entries must be a positive power of 2.");
        }
        if (historySize < 1 || historySize > 2) {
            throw new IllegalArgumentException("Only history sizes of 1 or 2 supported.");
        }
        this.m_entryCnt = numEntries;
        this.m_historySize = historySize;
        this.m_entries = new Vector();
        for (int i = 0; i < this.m_entryCnt; ++i) {
            this.m_entries.add(new BHTEntry(this.m_historySize, initVal));
        }
        this.fireTableStructureChanged();
    }
    
    public int getIdxForAddress(final int address) {
        if (address < 0) {
            throw new IllegalArgumentException("No negative addresses supported");
        }
        return (address >> 2) % this.m_entryCnt;
    }
    
    public boolean getPredictionAtIdx(final int index) {
        if (index < 0 || index > this.m_entryCnt) {
            throw new IllegalArgumentException("Only indexes in the range 0 to " + (this.m_entryCnt - 1) + " allowed");
        }
        return this.m_entries.elementAt(index).getPrediction();
    }
    
    public void updatePredictionAtIdx(final int index, final boolean branchTaken) {
        if (index < 0 || index > this.m_entryCnt) {
            throw new IllegalArgumentException("Only indexes in the range 0 to " + (this.m_entryCnt - 1) + " allowed");
        }
        this.m_entries.elementAt(index).updatePrediction(branchTaken);
        this.fireTableRowsUpdated(index, index);
    }
}
