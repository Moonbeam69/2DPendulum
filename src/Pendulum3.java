import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.text.*;

class Pendulum3 extends Canvas implements Runnable{
    private double      theta;
    private int         width=1000;
    private int         height=1000;
    private double      L = 30d;
    private double      g = 9.81d;
    private double      c = 0.02d;
    private double      driveAmpl = 0.0d;
    private double      driveFreq = 2d/3d;
    private Scrollbar   sb1;
    private Scrollbar   sb2;
    private Scrollbar   sb3;
    boolean             running = false;
    double              t;
    // initialise pendulum
    double              theta_next; // angle [rad].  positive counter-clockwise, down = 0rad
    double              omega_next; // angular velocity [rad/s]
    double              alpha_next; // angular acceleration [rad/s^2]
    double              t_next;

    Pendulum3() {

        // initialise
        setSize(width,height);
        JFrame pictureFrame = new JFrame("2D Pendulum");
        pictureFrame.setPreferredSize(new Dimension(width, height));
        Panel canvasPanel = new Panel();
        canvasPanel.add(this); // add the Canvas to the Panel
        pictureFrame.add(canvasPanel);

        Panel controlPanel = new Panel();
        controlPanel.setLayout(new GridLayout(4,2));

        sb1 = new Scrollbar(Adjustable.HORIZONTAL);
        sb1.setMaximum(150);
        sb1.setMinimum(0);
        sb1.setValue((int) (driveAmpl *100));
        sb1.setBackground(Color.DARK_GRAY);
        sb1.setUnitIncrement(1);

        Label driveAmplLbl = new Label("Torque drive amplitude: " + driveAmpl);
        driveAmplLbl.setPreferredSize(new Dimension(200,20));

        sb1.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                driveAmplLbl.setText("Torque drive amplitude: "+ driveAmpl);
            }
        });

        sb2 = new Scrollbar(Adjustable.HORIZONTAL);
        sb2.setMaximum(100);
        sb2.setMinimum(0);
        sb2.setValue((int)(driveFreq*100));
        sb2.setBackground(Color.DARK_GRAY);
        sb2.setUnitIncrement(1);

        Label driveFreqLbl = new Label("Torque drive freq: " + driveFreq);
        driveFreqLbl.setPreferredSize(new Dimension(200,20));

        sb2.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                driveFreqLbl.setText("Torque drive freq: " + driveFreq);
            }
        });

        sb3 = new Scrollbar(Adjustable.HORIZONTAL);
        sb3.setMaximum(100);
        sb3.setMinimum(0);
        sb3.setValue((int)(c*500d));
        sb3.setBackground(Color.DARK_GRAY);
        Label CLbl = new Label("Damping constant: " + c);
        CLbl.setPreferredSize(new Dimension(200,20));

        sb3.addAdjustmentListener(new AdjustmentListener() {
            @Override
            public void adjustmentValueChanged(AdjustmentEvent e) {
                CLbl.setText("Damping constant: " + c);
            }
        });

        Button StartStopBtn = new Button("Start");

        StartStopBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (running) {
                    running = false;
                    StartStopBtn.setLabel("Start");
                } else {
                    running = true;
                    StartStopBtn.setLabel("Stop");
                }
            }
        });

        controlPanel.add(StartStopBtn);
        controlPanel.add(new Label());
        controlPanel.add(sb1);
        controlPanel.add(driveAmplLbl);
        controlPanel.add(sb2);
        controlPanel.add(driveFreqLbl);
        controlPanel.add(sb3);
        controlPanel.add(CLbl);

        pictureFrame.add(controlPanel,BorderLayout.SOUTH);
        pictureFrame.pack();
        pictureFrame.setVisible(true);

        pictureFrame.addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });

        Thread myThread = new Thread(this);
        myThread.setPriority(Thread.MAX_PRIORITY);

        myThread.start();

    }


    public void run() {
        double dt = 0.01d; // time step. Less is more accurate, more computational

        double theta_mid;   // Runge-Kutta angle
        double omega_mid;   // Runge-Kutta angular velocity
        double alpha_mid;
        double t_mid;       // Runge-Kutta angular velocity

        double theta_prev = Math.toRadians(125d);;  // angle
        double omega_prev = 0d;  // angular velocity
        double alpha_prev = 0d;  // angular acceleration
        double t_prev     = 0d;  // time
        double initial_angle_in_degrees = 125d;
        theta = theta_prev;

        while (true) {
            for (int i = 0; i < 0.0051 / dt; i++) {

                if (running) {

                    // runge-kutta
                    alpha_mid = -g / L * Math.sin(theta_prev) - c * omega_prev + driveAmpl * Math.sin(driveFreq * t_prev);
                    omega_mid = omega_prev + alpha_prev * dt *.5d;
                    theta_mid = theta_prev + omega_prev * dt * .5d;
                    t_mid = t_prev + .5d * dt;

                    // Runge Kutta
                    alpha_next  = -g / L * Math.sin(theta_mid) - c * omega_mid + driveAmpl * Math.sin(driveFreq * t_mid);
                    omega_next  = omega_mid + alpha_mid * .5d * dt;
                    theta_next  = theta_mid + omega_mid * .5d * dt;
                    t_next      = t_mid + .5d * dt;

                    alpha_prev  = alpha_next;
                    omega_prev  = omega_next;
                    theta_prev  = theta_next;
                    t_prev      = t_next;

                    // update torque drive and damping constants from GUI
                    this.driveAmpl  = sb1.getValue() / 100d;
                    this.driveFreq  = sb2.getValue() / 100d;
                    this.c          = sb3.getValue() / 500d;

                    theta = theta_next; // assign theta for the paint method;
                    t = t_prev;

                    repaint();
               }


                // pause the run thread so the paint method has a chance to catchup
                try {Thread.sleep(20);} catch (InterruptedException e) {}
            }
        }
    }

    // Java calls this method when the Canvas needs to be drawn:
    public void paint(Graphics g) {
        DecimalFormat fmt = new DecimalFormat("###.###");

        int pivot_x = this.width/2;
        int pivot_y = this.height/2;
        int mass_x = (int)(pivot_x + 10d * L * Math.sin(theta));
        int mass_y = (int)(pivot_y + 10d * L * Math.cos(theta));
        int pivot_d = 10; // pixel diameter
        int mass_d = 30;  // pixel diameter

        g.setColor(Color.BLACK);
        g.fillOval(pivot_x, pivot_y, 10,10);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(3));
        g.drawLine((int)(pivot_x + pivot_d/2d), (int)(pivot_y + pivot_d/2d), (int)(mass_x + mass_d/2d), (int)(mass_y + mass_d/2d));
        g.setColor(Color.BLUE);
        g.fillOval(mass_x, mass_y, 30,30);

        g.drawString("t: " + fmt.format(t) + " s", (int) (width * .75), (int) (height * .25d));
        g.drawString("theta: " + fmt.format(theta) + " rad", (int) (width * .75), (int) (height * .25d+12d));
        g.drawString("omega: " + fmt.format(omega_next) + " rad/s", (int) (width * .75), (int) (height * .25d+24d));
        g.drawString("alpha: "+fmt.format(alpha_next) + " rad/s^2", (int) (width * .75), (int) (height * .25d+36d));

    }

    public void update(Graphics g) {
        Graphics offgc;
        Image offscreen = null;

        // create the offscreen buffer and associated Graphics
        offscreen = createImage(width, height);
        offgc = offscreen.getGraphics();
        // clear the exposed area
        offgc.setColor(getBackground());
        offgc.fillRect(0, 0, width, height);
        offgc.setColor(getForeground());
        // do normal redraw
        paint(offgc);
        // transfer offscreen to window
        g.drawImage(offscreen, 0, 0, this);
    }

    public static void main(String[] arg) {
        new Pendulum3();
    }
}