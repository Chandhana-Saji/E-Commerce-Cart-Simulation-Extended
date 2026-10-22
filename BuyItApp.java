
import javax.swing.SwingUtilities;
import database.gui.HomeFrame;

public class BuyItApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new HomeFrame();
            }
        });
    }
}
