#include <iostream>
#include <fstream>
#include <string>
using namespace std;

int main() {
	cout << "pene";
	ifstream fitxer;
	fitxer.open("fitxer.txt");
	ofstream fitxerOut;
	fitxerOut.open("dadesTractades.txt");
	if (fitxerOut.fail()) cout << "ha petat el open\n";
	int counter = 0;
	double distancia;
	double angle;
	double distanciaTotal = 0;
	double angleTotal = 0;
	while (!fitxer.eof()) {
		cout << "pene";
		fitxer >> distancia >> angle;
		distanciaTotal += distancia;
		angleTotal += angle;
		if (counter == 10) {
			counter = 0;
			fitxerOut << distanciaTotal/10 << " " << angleTotal/10 << endl;
			distanciaTotal = 0;
			angleTotal = 0;
		}
		else counter++;
	}
	if (counter != 0) {
		fitxerOut << distanciaTotal/counter << " " << angleTotal/counter << endl;
	}
	fitxerOut.close();
	fitxer.close();
}