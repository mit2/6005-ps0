package turtle;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingWorker;

public class TurtleGUI extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Color canvasBGColor = Color.WHITE;

    private static final double LENGTH_OF_A_TURN = 20;
    private static final long MILLIS_PER_DRAWING = 5000;
    private static final double ROUGH_FPS = 60;

    private static final long MILLIS_PER_FRAME = (long) (1000.0 / ROUGH_FPS);

    private int canvasWidth;
    private int canvasHeight;
    private int actionListSize;

    private boolean isRunning;
    private AnimationThread currAnimationThread;

    private JButton runButton;
    private JLabel currentActionLabel;
    private JLabel currentAction;
    private BufferedImage canvas;
    private Graphics2D graphics;
    private JLabel drawLabel;

    private List<Action> actionList;

    private int originX;
    private int originY;

    public TurtleGUI(List<Action> actionList, int canvasWidth, int canvasHeight) {
        super("TurtleGUI");

        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.actionListSize = actionList.size();
        this.originX = (canvasWidth - 1) / 2;
        this.originY = (canvasHeight - 1) / 2;

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        Container cp = getContentPane();
        GroupLayout layout = new GroupLayout(cp);
        cp.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        runButton = new JButton();
        runButton.setName("runButton");
        runButton.setText("Run!");

        isRunning = false;

        currentActionLabel = new JLabel();
        currentActionLabel.setName("currentActionLabel");
        currentActionLabel.setText("Currently performing: ");

        currentAction = new JLabel();
        currentAction.setName("currentAction");
        currentAction.setText("STOPPED");

        canvas = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
        graphics = canvas.createGraphics();
        graphics.setBackground(canvasBGColor);
        graphics.clearRect(0, 0, canvasWidth, canvasHeight);
        graphics.setStroke(new BasicStroke(1.0f));

        drawLabel = new JLabel(new ImageIcon(canvas));
        drawLabel.setName("drawLabel");

        this.actionList = actionList;

        runButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                if (!isRunning) {
                    runButton.setText("Stop");
                    isRunning = true;
                    currAnimationThread = new AnimationThread();
                    currAnimationThread.execute();
                } else {
                    currAnimationThread.cancel(true);
                }
            }
        });

        layout.setHorizontalGroup(layout.createParallelGroup()
                .addComponent(drawLabel)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(runButton)
                        .addComponent(currentActionLabel)
                        .addComponent(currentAction)));
        layout.setVerticalGroup(layout.createSequentialGroup()
                .addComponent(drawLabel)
                .addGroup(layout.createParallelGroup(Alignment.CENTER)
                        .addComponent(runButton)
                        .addComponent(currentActionLabel)
                        .addComponent(currentAction)));

        pack();
    }

    public void stopAnimation() {
        currentAction.setText("STOPPED");
        isRunning = false;
        runButton.setText("Run!");
    }

    public List<Action> getActionList() {
        return actionList;
    }

    public BufferedImage getCanvas() {
        return canvas;
    }

    public JLabel getDrawLabel() {
        return drawLabel;
    }

    public void setCurrentAction(String s) {
        currentAction.setText(s);
    }

    private class AnimationThread extends SwingWorker<Void, Void> {

        AnimationThread() {
            super();
        }

        @Override
        protected Void doInBackground() throws Exception {
            animate();
            return null;
        }

        private void animate() {
            graphics.clearRect(0, 0, canvasWidth, canvasHeight);
            drawLabel.repaint();

            // first, calculate the total length of line segments and turns,
            // in order to allocate drawtime proportionally later

            double totalLength = 0;
            for (Action a : actionList) {
                if (a.type == ActionType.TURN) {
                    totalLength += LENGTH_OF_A_TURN;
                } else if (a.type == ActionType.FORWARD) {
                    totalLength += a.lineSeg.length();
                }
            }

            // now, draw the animation

            double cumulativeLength = 0;
            long initialTime = System.currentTimeMillis();
            for (int i = 0; i < actionListSize; i++) {
                if (isCancelled()) {
                    break;
                }
                Action action = actionList.get(i);
                setCurrentAction((i + 1) + ". " + action);
                if (action.lineSeg != null) {
                    long startTime = (long) (initialTime + cumulativeLength / totalLength * MILLIS_PER_DRAWING);
                    cumulativeLength += action.lineSeg.length();
                    long endTime = (long) (initialTime + cumulativeLength / totalLength * MILLIS_PER_DRAWING);
                    draw(action.lineSeg, startTime, endTime);
                } else {
                    cumulativeLength += LENGTH_OF_A_TURN;
                    long drawTime = (long) (initialTime + cumulativeLength / totalLength * MILLIS_PER_DRAWING - System.currentTimeMillis());
                    if (drawTime > 0) {
                        try {
                            Thread.sleep((long) drawTime);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
            stopAnimation();
        }

        private void draw(LineSegment lineSeg, long initialTime, long endTime) {
            long drawTime = endTime - initialTime;

            double initX = originX + lineSeg.start.x;
            double initY = originY - lineSeg.start.y;

            double finalX = originX + lineSeg.end.x;
            double finalY = originY - lineSeg.end.y;

            int fromX = (int) initX;
            int fromY = (int) initY;

            boolean abort = false;
            long elapsedTime = System.currentTimeMillis() - initialTime;

            graphics.setPaint(lineSeg.color.color);

            while (!abort && elapsedTime + MILLIS_PER_FRAME < drawTime) {
                // while we have time remaining for this action
                double fractionDone = Math.max(elapsedTime * 1.0 / drawTime, 0);
                int toX = (int) Math.round(initX * (1 - fractionDone) + finalX * fractionDone);
                int toY = (int) Math.round(initY * (1 - fractionDone) + finalY * fractionDone);
                graphics.drawLine(fromX, fromY, toX, toY);
                drawLabel.repaint();

                try {
                    Thread.sleep(MILLIS_PER_FRAME);
                } catch (InterruptedException e) {
                    abort = true;
                }

                // update
                fromX = toX;
                fromY = toY;

                elapsedTime = System.currentTimeMillis() - initialTime;
            }

            // finish the line if we're still not done
            if (!abort && (fromX != finalX || fromY != finalY)) {
                graphics.drawLine(fromX, fromY, (int) finalX, (int) finalY);
                drawLabel.repaint();
            }
        }
    }
}
