package dev.heliosclient.ui.clickgui.gui;

import dev.heliosclient.module.settings.buttonsetting.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

// Todo: Create a new interface so any object implementing that interface could be adjusted in the table instead of just buttons. Short: Just make it universal.
public class Table {
    public final List<List<Button>> table;

    public Table() {
        table = new ArrayList<>();
    }

    /**
     * Adds a new button at the given cell index.
     * Note: Index start with 0th position. Eg: 0th row and 0th column
     *
     * @param row    The row index of the cell
     * @param column The column index of the cell
     * @param button The button to be added
     */
    public void addButton(int row, int column, Button button) {
        ensureCellExists(row, column);
        table.get(row).set(column, button);
    }

    public Button getButton(int row, int column) {
        return table.get(row).get(column);
    }

    /**
     * Ensures that the cell exists before adding a new button/element to it. Adds a new row / column whenever a button is added outside the present length of the table automatically.
     *
     * @param row    The row index of the cell
     * @param column The column index of the cell
     */
    private void ensureCellExists(int row, int column) {
        while (table.size() <= row) {
            table.add(new ArrayList<>());
        }
        while (table.get(row).size() <= column) {
            table.get(row).add(null);
        }
    }

    /**
     * Adjust the layout of the button to be displayed as per the given coordinates and width. (Sets the button X and Y).
     * Can be called when the buttons are added.
     *
     * @param tableX            The starting X - coordinates of the table.
     * @param tableY            The starting Y - coordinates of the table. No height is required as it automatically adjusts the buttons with the given column and row index
     * @param tableWidth        The maximum width of the table which the button should occupy
     * @param equalDistribution Whether to distribute the width of the buttons equally within the width (i.e. buttons / no. of columns) or on the basis of their text width.
     * @return The total height of the table.
     */

    public int adjustButtonLayout(int tableX, int tableY, int tableWidth, boolean equalDistribution) {
        int numRows = table.size();
        int numColumns = table.stream().mapToInt(List::size).max().orElse(0);
        int totalHeight = 0;
        // Check if the row exists in the table and is less than the num of rows.
        for (int row = 0; row < numRows && row < table.size(); row++) {
            List<Button> buttonRow = table.get(row);
            int totalTextLength = buttonRow.stream().filter(Objects::nonNull).mapToInt(button -> button.getText().length()).sum();
            int y = tableY + totalHeight;
            int x = tableX;

            // Check if the column exists in the row
            for (int column = 0; column < numColumns && column < buttonRow.size(); column++) {
                Button button = buttonRow.get(column);
                if (button != null) {
                    int buttonWidth;

                    //Distribute the cells equally across the given width
                    if (equalDistribution) {
                        buttonWidth = tableWidth / numColumns;
                    } else {
                        buttonWidth = (button.getText().length() * tableWidth) / totalTextLength;
                    }
                    button.setX(x);
                    button.setY(y);
                    button.setWidth(buttonWidth);
                    button.setHeight(button.getHeight());

                    x += buttonWidth;
                }
            }
            totalHeight += buttonRow.stream().filter(Objects::nonNull).findFirst().map(Button::getHeight).orElse(0);
        }
        return totalHeight;
    }


}
