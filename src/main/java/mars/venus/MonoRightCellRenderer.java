

package mars.venus;

import javax.swing.JLabel;
import java.awt.Component;
import javax.swing.JTable;
import java.awt.Font;
import javax.swing.table.DefaultTableCellRenderer;

class MonoRightCellRenderer extends DefaultTableCellRenderer
{
    public static final Font MONOSPACED_PLAIN_12POINT;
    
    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
        final JLabel cell = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        cell.setFont(MonoRightCellRenderer.MONOSPACED_PLAIN_12POINT);
        cell.setHorizontalAlignment(4);
        return cell;
    }
    
    static {
        MONOSPACED_PLAIN_12POINT = new Font("Monospaced", 0, 12);
    }
}
