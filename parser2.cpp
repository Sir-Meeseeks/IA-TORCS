#include <iostream>
#include <fstream>
#include <string>
using namespace std;

int main() {
	ifstream fitxer;
	fitxer.open("dadesTractades.txt");
	ofstream fitxerOut;
	fitxerOut.open("dadesTractades2.txt");
	int counter = 0;
	double distancia;
	double angle;
	double distanciaTotal = 0;
	double angleTotal = 0;
	while (!fitxer.eof()) {
		fitxer >> distancia >> angle;
		distanciaTotal += distancia;
		angleTotal += angle;
		if (counter == 10) {
			counter = 0;
			fitxerOut << distancia << " " << angleTotal << endl;
			distanciaTotal = 0;
			angleTotal = 0;
		}
		else counter++;
	}
	fitxerOut << distancia << " " << angleTotal << endl;
	fitxerOut.close();
	fitxer.close();
}