package scr;
import java.io.IOException;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Variable;


/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Mar 4, 2008
 * Time: 4:59:21 PM

 */
public class DeadSimpleSoloController extends Controller {

    Boolean flagClean = true;
    String fileName = "src\\scr\\driver.fcl";
    FIS fis = FIS.load(fileName, true);
    double angle = 0; 
    int lap = 0;
    boolean newlap = false;
    public Action control(SensorModel sensorModel) {
        Action action = new Action ();
        if(sensorModel.getDistanceFromStartLine() < 200 && newlap)
        {
            lap++;
            newlap=false; 
            System.out.println("NEW LAP");
        }
        else if(sensorModel.getDistanceFromStartLine() > 1000)
            newlap = true;
         //VOLTA DE RECONEIXAMENT
        if (lap <= 0)
        {
            // --------------------------------------------
            //  METODE PER AGAFAR LES DADES: 
            //  Utilitzem el getAngleToTrackAxis(); per saber 
            // si hem de girar o no. 
            // Quan girem, anem incrementant el valor de l'angle 
            // per saber quina quantitat girem. 
            //
            //  Quan deixem de girar, posem l'angle a 0. 
            // --------------------------------------------
            // --------------------------------------------
            // Obtenim les dades sense tractar del cicuit
            // --------------------------------------------
            double[] dada = new double[2];
            dada[0] = sensorModel.getDistanceFromStartLine();
            dada[1] = sensorModel.getAngleToTrackAxis();
            if (flagClean && dada[0] < 1) {
                vectorDades.clear();
                flagClean = false;
            }
        
            //--------------------------------------------
            //  SI FEM ZIGA-ZAGA AL VOLTANT DE LA RECTA 
            // FILTREM AQUESTS GIRS
            //--------------------------------------------
            if (dada[1] > -0.005 && dada[1] < 0.005) 
                dada[1] = 0;
        
             action.gear = 2;
        
            if (sensorModel.getSpeed() < 65) {
                action.accelerate = 1;
            }
            else action.accelerate = 0;

            if (dada[1] <0) {
                //GIR A L'ESQUERRA
                action.steering = -0.2;
                angle+= -0.2;
            }
            else if (dada[1] > 0 ) {
                //GIR A LA DRETA
                action.steering = 0.2;
                angle+=0.2;
            }
            else{
                //RESET DEL GIR
                 action.steering = 0;
                 angle = 0;
            }

            dada[1]=angle;
            vectorDades.add(dada);
            //vectorDades.add(dada);
        }
        else //NO ESTEM EN LA VOLTA DE RECONEIXAMENT
        {
            // --------------------------------------------
            // Controlador FUZZY
            // --------------------------------------------
            fis.setVariable("posicio",sensorModel.getTrackPosition());
            fis.setVariable("velocitat",sensorModel.getSpeed());
            fis.setVariable("angleEix",sensorModel.getAngleToTrackAxis());
            //fis.evaluate();

            Variable direccio = fis.getVariable("direccio");
            Variable acceleracio = fis.getVariable("acceleracio");
            Variable frens = fis.getVariable("frens");
            
            action.steering = direccio.getValue();
            System.out.println(fis.getVariable("direccio").getValue());
            action.accelerate = acceleracio.getValue();
            action.brake = frens.getValue();

            // --------------------------------------------
            // Controlem el cotxe
            // --------------------------------------------
            double revolucions = sensorModel.getRPM();
            int marxa = sensorModel.getGear();

            // ---> Canviar marxa
            if (marxa < 1) marxa = 1;
            else if (action.accelerate > 0) {
                if (revolucions > 5000 && marxa < 6) marxa++;
            }
            else if (action.brake > 0) {
                if (revolucions < 2500 && marxa > 1) marxa--;
            }
        }
     

        return action;
    }

    public void reset() {
		System.out.println("Restarting the race!");
		
	}

    public void shutdown() {
            System.out.println("Bye bye!");		
    }
}
