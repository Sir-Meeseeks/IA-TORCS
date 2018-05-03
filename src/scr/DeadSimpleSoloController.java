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
        //System.out.print(dada[0]+" "+dada[1]+"\n");
        
        if (sensorModel.getAngleToTrackAxis() > 0.1) action.steering = 0.2;
        else if (sensorModel.getAngleToTrackAxis() > 0.2) action.steering = 0.3;
        else if (sensorModel.getAngleToTrackAxis() > 0.3) action.steering = 0.4;
        else if (sensorModel.getAngleToTrackAxis() > 0.4) action.steering = 0.5;
        
        if (sensorModel.getAngleToTrackAxis() < -0.1) action.steering = -0.2;
        else if (sensorModel.getAngleToTrackAxis() < -0.2) action.steering = -0.3;
        else if (sensorModel.getAngleToTrackAxis() < -0.3) action.steering = -0.4;
        else if (sensorModel.getAngleToTrackAxis() < -0.4) action.steering = -0.5;
        
        
        if (action.gear == 0) action.gear = 1;
        else if (sensorModel.getSpeed() > 75 && action.gear == 1) {
            action.gear = 2;
        }
        else if (sensorModel.getSpeed() > 130 && action.gear == 2) {
            action.gear = 3;
        }
        else if (sensorModel.getSpeed() > 150 && action.gear == 3) {
            action.gear = 4;
        }
        
        else if (sensorModel.getAngleToTrackAxis() > 0.1 || sensorModel.getAngleToTrackAxis() < -0.1) {
            action.brake = 1;
        }
        if (sensorModel.getAngleToTrackAxis() < 0.1 && sensorModel.getAngleToTrackAxis() > -0.1) action.accelerate = 1;
        
        return action;
    }

    public void reset() {
		System.out.println("Restarting the race!");
		
	}

    public void shutdown() {
            System.out.println("Bye bye!");		
    }
}
