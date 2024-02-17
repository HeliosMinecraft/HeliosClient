package dev.heliosclient.ui.clickgui.gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static dev.heliosclient.HeliosClient.MC;

/**
 * Scheduled for removal
 */
@Deprecated
public class Quadtree {
    private final int MAX_OBJECTS = 10;
    private final int MAX_LEVELS = 5;

    private final int level;
    private final List<HudBox> objects;
    private final Rectangle bounds;
    private final Quadtree[] nodes;

    public Quadtree(int level) {
        this.level = level;
        this.objects = new ArrayList<HudBox>();
        this.bounds = new Rectangle(0, 0, MC.getWindow().getWidth(), MC.getWindow().getHeight());
        this.nodes = new Quadtree[4];
    }

    public Quadtree(int level, Rectangle bounds) {
        this.level = level;
        this.objects = new ArrayList<HudBox>();
        this.bounds = bounds;
        this.nodes = new Quadtree[4];
    }

    public void clear() {
        objects.clear();

        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i] != null) {
                nodes[i].clear();
                nodes[i] = null;
            }
        }
    }

    private void split() {
        int subWidth = (int) (bounds.getWidth() / 2);
        int subHeight = (int) (bounds.getHeight() / 2);
        int x = (int) bounds.getX();
        int y = (int) bounds.getY();

        nodes[0] = new Quadtree(level + 1, new Rectangle(x + subWidth, y, subWidth, subHeight));
        nodes[1] = new Quadtree(level + 1, new Rectangle(x, y, subWidth, subHeight));
        nodes[2] = new Quadtree(level + 1, new Rectangle(x, y + subHeight, subWidth, subHeight));
        nodes[3] = new Quadtree(level + 1, new Rectangle(x + subWidth, y + subHeight, subWidth, subHeight));
    }

    private int getIndex(HudBox hudBox) {
        int index = -1;
        double verticalMidpoint = bounds.getX() + (bounds.getWidth() / 2);
        double horizontalMidpoint = bounds.getY() + (bounds.getHeight() / 2);

        boolean topQuadrant = (hudBox.getY() < horizontalMidpoint && hudBox.getY() + hudBox.getHeight() < horizontalMidpoint);
        boolean bottomQuadrant = (hudBox.getY() > horizontalMidpoint);

        if (hudBox.getX() < verticalMidpoint && hudBox.getX() + hudBox.getWidth() < verticalMidpoint) {
            if (topQuadrant) {
                index = 1;
            } else if (bottomQuadrant) {
                index = 2;
            }
        } else if (hudBox.getX() > verticalMidpoint) {
            if (topQuadrant) {
                index = 0;
            } else if (bottomQuadrant) {
                index = 3;
            }
        }

        return index;
    }

    public void insert(HudBox hudBox) {
        if (nodes[0] != null) {
            int index = getIndex(hudBox);

            if (index != -1) {
                nodes[index].insert(hudBox);

                return;
            }
        }

        objects.add(hudBox);

        if (objects.size() > MAX_OBJECTS && level < MAX_LEVELS) {
            if (nodes[0] == null) {
                split();
            }

            int i = 0;
            while (i < objects.size()) {
                int index = getIndex(objects.get(i));
                if (index == -1) {
                    i++;
                } else {
                    nodes[index].insert(objects.remove(i));
                }
            }
        }
    }

    public List<HudBox> retrieve(List<HudBox> returnObjects, HudBox hudBox) {
        int index = getIndex(hudBox);
        if (index != -1 && nodes[0] != null) {
            nodes[index].retrieve(returnObjects, hudBox);
        }

        returnObjects.addAll(objects);

        return returnObjects;
    }

    public void remove(HudBox hudBox) {
        int index = getIndex(hudBox);
        if (index != -1 && nodes[0] != null) {
            nodes[index].remove(hudBox);
            return;
        }

        objects.remove(hudBox);

        // If this node has no objects left and no subnodes, clear it
        if (objects.isEmpty() && nodes[0] == null) {
            clear();
        }
    }
}
