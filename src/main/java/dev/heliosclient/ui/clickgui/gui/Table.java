package dev.heliosclient.ui.clickgui.gui;

import dev.heliosclient.module.settings.buttonsetting.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Table {
    public final List<List<Button>> table;

    public Table() {
        table = new ArrayList<>();
    }

    public void addButton(int row, int column, Button button) {
        ensureCellExists(row, column);
        table.get(row).set(column, button);
    }

    public Button getButton(int row, int column) {
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

    public int adjustButtonLayout(int tableX, int tableY, int tableWidth, boolean equalDistribution) {
        int numRows = table.size();
        int numColumns = table.stream().mapToInt(List::size).max().orElse(0);
        int totalHeight = 0;

        for (int row = 0; row < numRows; row++) {
            if (row < table.size()) { // Check if the row exists in the table
                List<Button> buttonRow = table.get(row);
                int totalTextLength = buttonRow.stream().filter(Objects::nonNull).mapToInt(button -> button.getText().length()).sum();
                int y = tableY + totalHeight;
                int x = tableX;

                for (int column = 0; column < numColumns; column++) {
                    if (column < buttonRow.size()) { // Check if the column exists in the row
                        Button button = buttonRow.get(column);
                        if (button != null) {
                            int buttonWidth;
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
                }
                totalHeight += buttonRow.stream().filter(Objects::nonNull).findFirst().map(Button::getHeight).orElse(0);
            }
        }
        return totalHeight;
    }


}
