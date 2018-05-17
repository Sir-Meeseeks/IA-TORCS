package scr;
import java.io.IOException;


/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Mar 4, 2008
 * Time: 4:59:21 PM

 */
public class DeadSimpleSoloController extends Controller {

    Boolean flagClean = true;

    public Action control(SensorModel sensorModel) {
        Action action = new Action ();
  
        double[] dada = new double[2];
        dada[0] = sensorModel.getDistanceFromStartLine();
        dada[1] = sensorModel.getAngleToTrackAxis();
        if (flagClean && dada[0] < 1) {
            vectorDades.clear();
            flagClean = false;
        }
        vectorDades.add(dada);
        return action;
    }

    public void reset() {
		System.out.println("Restarting the race!");
		
	}

    public void shutdown() {
            System.out.println("Bye bye!");		
    }
}
