package turtle;

/**
 * Action list types.
 */
enum ActionType {
    FORWARD, TURN, COLOR
}

public class Action {

    ActionType type;
    String displayString;
    LineSegment lineSeg;

    public Action(ActionType type, String displayString, LineSegment lineSeg) {
        this.type = type;
        this.displayString = displayString;
        this.lineSeg = lineSeg;
    }

    public String toString() {
        if (displayString == null) {
            return "";
        } else {
            return displayString;
        }
    }
}
