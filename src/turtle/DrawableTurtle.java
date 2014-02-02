package turtle;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

/**
 * Turtle for drawing in a window on the screen.
 */
public class DrawableTurtle implements Turtle {

    List<Action> actionList;
    List<LineSegment> lines;

    Point currentPosition;
    double currentHeading;
    PenColor currentColor;

    private static final int canvasWidth = 512;
    private static final int canvasHeight = 512;

    public DrawableTurtle() {
        this.currentPosition = new Point(0, 0);
        this.currentHeading = 0.0;
        this.currentColor = PenColor.BLACK;
        this.lines = new ArrayList<LineSegment>();
        this.actionList = new ArrayList<Action>();
    }

    public void forward(int steps) {
        double newX = currentPosition.x + Math.cos(Math.toRadians(90.0 - currentHeading)) * (double)steps;
        double newY = currentPosition.y + Math.sin(Math.toRadians(90.0 - currentHeading)) * (double)steps;

        LineSegment lineSeg = new LineSegment(currentPosition.x, currentPosition.y, newX, newY, currentColor);
        this.lines.add(lineSeg);
        this.currentPosition = new Point(newX, newY);

        this.actionList.add(new Action(ActionType.FORWARD, "forward " + steps + " steps", lineSeg));
    }

    public void turn(double degrees) {
        degrees = (degrees % 360 + 360) % 360;
        this.currentHeading += degrees;
        if (this.currentHeading >= 360.0)
            this.currentHeading -= 360.0;
        this.actionList.add(new Action(ActionType.TURN, "turn " + degrees + " degrees", null));
    }

    public void color(PenColor color) {
        this.currentColor = color;
        this.actionList.add(new Action(ActionType.COLOR, "change to " + color.toString().toLowerCase(), null));
    }

    /**
     * Draw the image created by this turtle in a window on the screen.
     */
    public void draw() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                (new TurtleGUI(actionList, canvasWidth, canvasHeight)).setVisible(true);
            }
        });
        return;
    }
}
