package dev.heliosclient.ui.clickgui.gui.tables;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Table {
    public final List<List<TableEntry>> table;

    public Table() {
        table = new ArrayList<>();
    }

    /**
     * Adds a new entry at the given cell index.
     * Note: Index start with 0th position. Eg: 0th row and 0th column
     *
     * @param row    The row index of the cell
     * @param column The column index of the cell
     * @param entry The entry to be added
     */
    public void addEntry(int row, int column, TableEntry entry) {
        ensureCellExists(row, column);
        table.get(row).set(column, entry);
    }
    public void addEntry(TableEntry entry, double maxWidth) {
        int currentRow = table.size() - 1;
        if (currentRow < 0) {
            currentRow = 0;
            table.add(new ArrayList<>());
        }

        List<TableEntry> currentRowEntries = table.get(currentRow);
        double currentRowWidth = currentRowEntries.stream().filter(Objects::nonNull).mapToDouble(TableEntry::getWidth).sum();

        if (currentRowWidth + entry.getWidth() > maxWidth) {
            currentRow++;
            table.add(new ArrayList<>());
            currentRowEntries = table.get(currentRow);
        }

        ensureCellExists(currentRow, currentRowEntries.size());
        currentRowEntries.add(entry);
    }



    public TableEntry getEntry(int row, int column) {
        return table.get(row).get(column);
    }

    private void ensureCellExists(int row, int column) {
        while (table.size() <= row) {
            table.add(new ArrayList<>());
        }
        while (table.get(row).size() <= column) {
            table.get(row).add(null);
        }
    }

    /**
     * Adjust the layout of the entries to be displayed as per the given coordinates and width. (Sets the entry X and Y).
     * Can be called when the buttons are added.
     *
     * @param tableX            The starting X - coordinates of the table.
     * @param tableY            The starting Y - coordinates of the table. No height is required as it automatically adjusts the entries with the given column and row index
     * @param tableWidth        The maximum width of the table which the entry should occupy
     * @param equalDistribution Whether to distribute the width of the entries equally within the width (i.e. entries / no. of columns) or on the basis of their width.
     * @return The total height of the table.
     */
    public double adjustTableLayout(double tableX, double tableY, double tableWidth, boolean equalDistribution) {
        int numRows = table.size();
        int numColumns = table.stream().mapToInt(List::size).max().orElse(0);
        int totalHeight = 0;

        for (int row = 0; row < numRows && row < table.size(); row++) {
            List<TableEntry> entryRow = table.get(row);
            double totalWidth = entryRow.stream().filter(Objects::nonNull).mapToDouble(TableEntry::getWidth).sum();
            double y = tableY + totalHeight;
            double x = tableX;


            for (int column = 0; column < numColumns && column < entryRow.size(); column++) {
                TableEntry entry = entryRow.get(column);
                if (entry != null) {
                    double entryWidth;

                    if (equalDistribution) {
                        entryWidth = tableWidth / entryRow.size();
                    } else if(totalWidth >= tableWidth){
                        entryWidth = (entry.getWidth() * tableWidth) / totalWidth;
                    } else {
                        entryWidth = entry.getWidth();
                    }
                    entry.setPosition(x, y);
                    entry.setWidth(entryWidth);
                    x += (int) entryWidth;
                }
            }
            totalHeight += entryRow.stream().filter(Objects::nonNull).findFirst().map(TableEntry::getHeight).orElse(0d);
        }
        return totalHeight;
    }
}