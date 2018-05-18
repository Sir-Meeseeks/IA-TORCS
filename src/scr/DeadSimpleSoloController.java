package scr;


import java.io.IOException;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.FunctionBlock;
import net.sourceforge.jFuzzyLogic.rule.Variable;
import net.sourceforge.jFuzzyLogic.plot.JFuzzyChart;


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
    
    //Entrar dades
    int counterSuau = 0; 
    double sumaAngles = 0; 
    double sumaSuau = 0; 
    int counterMapping = 0;
    //Ubicar el cotxe dins el vector
    int punterMapeig = 1; 
    //Per debuggar FUZZYLOGIC
    boolean showup = true; 
    int counter =0;
    public Action control(SensorModel sensorModel) {
        Action action = new Action ();
        //Comprovar quan fem una volta per tal de cambiar a fuzzy logic
        if(sensorModel.getDistanceFromStartLine() < 200 && newlap)
        {
            lap++;
            newlap=false; 
            System.out.println("NEW LAP");
        }
        else if(sensorModel.getDistanceFromStartLine() > 1000)
            newlap = true;
         //VOLTA DE RECONEIXAMENT
        if (lap <= 1)
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
            //vectorDades.add(dada);
            processarDades(dada);

        }
        else //NO ESTEM EN LA VOLTA DE RECONEIXAMENT
        {
            //--------------------------------------------
            //  BUSQUEM EL GIR DINS EL VECTOR
            //--------------------------------------------
            double pos = sensorModel.getTrackPosition();
            if(vectorMapejat.get(punterMapeig)[0] < pos + 150 )
            {
                punterMapeig++;
                punterMapeig = punterMapeig % vectorMapejat.size();//Per poder fer mes de una volta
            }
                
            // --------------------------------------------
            // Controlador FUZZY
            // --------------------------------------------
            fis.setVariable("posicio",sensorModel.getTrackPosition());
            fis.setVariable("velocitat",sensorModel.getSpeed());
            fis.setVariable("angleEix",sensorModel.getAngleToTrackAxis());
            double valorgir = vectorMapejat.get(punterMapeig)[1];
            if (valorgir < 0)
                    valorgir*=-1;
            fis.setVariable("gir", valorgir);
            fis.evaluate();

            Variable direccio = fis.getVariable("direccio");
            Variable acceleracio = fis.getVariable("acceleracio");
            Variable frens = fis.getVariable("frens");
            
            action.steering = direccio.getValue();
            //---------------------------------------
            //  DEBUGAR EL FUZZY LOGIC
            //---------------------------------------
            /*if(showup)
            {
                JFuzzyChart.get().chart(fis);
                JFuzzyChart.get().chart(direccio, direccio.getDefuzzifier(), true);
                showup = false;
            }
            else 
            {
                counter++;
                showup = counter%20000 == 0;
            }*/
                
            action.accelerate = acceleracio.getValue();
            action.brake = frens.getValue();

            // --------------------------------------------
            // Controlem el cotxe
            // --------------------------------------------
            double revolucions = sensorModel.getRPM();
            int marxa = sensorModel.getGear();
            System.out.println(revolucions +" -- "+marxa);
            

            // ---> Canviar marxa
            if (marxa < 1) marxa = 1;
            else if (action.accelerate > 0) {
                if (revolucions > 5000 && marxa < 6) marxa++;
                else if (revolucions < 1000 && marxa > 1)marxa--;
            }
            else if (action.brake > 0) {
                if (revolucions < 2500 && marxa > 1) marxa--;
            }
             action.gear = marxa;
        }
     

        return action;
    }

    public void reset() {
		System.out.println("Restarting the race!");
		
	}

    public void shutdown() {
            System.out.println("Bye bye!");		
    }
    
    private void processarDades(double[] dada)
    {
        System.out.println(dada[0]+"  "+dada[1]);
        sumaAngles+=dada[1];
        counterSuau++;
        vectorDades.add(dada);
        boolean mapping = false; 
        if(counterSuau%10 == 0)
        {
            double[] aux = new double[2];
            aux[0]= dada[0]; aux[1] = sumaAngles/10;
            //Filtre per evitar els girs durant una recte
            if(aux[1] < 0.02 && aux[1] > - 0.02)
                aux[1]=0;
            
            System.out.println("NOU SUAVITZAT: "+aux[0]+"  "+aux[1]);
            vectorSuavitzat.add(aux); 
            sumaSuau +=aux[1];
            counterMapping++;
            sumaAngles = 0;
            mapping = true;//Aixi nomes fa el mapejat quan entra un nou valor al suavitzat
        }
        //TENIM 30 VALORS DE SUAVITZAT, PODEM FER EL MAPPING 
        if(counterMapping > 29 && mapping)
        {
            double[]aux2 = new double[2];
            aux2[0] = dada[0];
            if(counterMapping > 30)//Hi ha més de un valor
                sumaSuau -= vectorSuavitzat.get(counterMapping-30)[1];//TREIEM EL PRIMER VALOR DE TOTS, AIXI MANTENIM LA SUMA DELS 30 

            aux2[1] = sumaSuau; 
            System.out.println("NOU MAPPING: "+aux2[0]+"  "+aux2[1]);
            vectorMapejat.add(aux2);
        }

        dada = new double[2];
    }
}
