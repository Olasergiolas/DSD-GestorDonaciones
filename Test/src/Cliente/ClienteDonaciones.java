package Cliente;

import Servidor.GestorDonaciones;
import Servidor.GestorDonacionesI;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.AbstractMap;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ClienteDonaciones  extends UnicastRemoteObject implements
    ClienteDonacionesI, Runnable{
    Servidor.GestorDonacionesI gestor;

    public ClienteDonaciones(Servidor.GestorDonacionesI g) throws RemoteException{
        super();
        gestor = g;
    }

    @Override
    public void registrarme(GestorDonacionesI gestor) throws RemoteException, MalformedURLException, NotBoundException {
        AbstractMap.SimpleEntry<GestorDonacionesI, Integer> res = gestor.registrarCliente(this);
        this.gestor = res.getKey();

        if (res.getValue() == -1)
            System.out.println("Cliente previamente registrado");
        else
            System.out.println("Cliente registrado con éxito!");
    }

    @Override
    public void broadcastMSG(String msg) throws RemoteException{
        gestor.broadcastMSG(msg);
    }

    @Override
    public void run(){
        int eleccion = -1;
        boolean continuar = true;
        Scanner input = new Scanner(System.in);

        while(continuar) {
            System.out.println("****************************************");
            System.out.println("Bienvenido!");
            System.out.println("[1] Registrarte");
            System.out.println("[2] Iniciar Sesión");
            System.out.println("****************************************");

            if (input.hasNextInt()) {
                eleccion = input.nextInt();
                if (eleccion == 1 || eleccion == 2)
                    continuar = false;

                else
                    System.out.println("\nOpción no disponible");
            }
            else{
                System.out.println("\nPor favor, introduzca un entero");
                input.next();
                continue;
            }
        }

        try {
            switch (eleccion){
                case 1:
                    registrarme(gestor);
                    break;
                case 2:
                    broadcastMSG("Prueba");
                    break;
            }
        }catch (RemoteException | MalformedURLException | NotBoundException e){
            e.printStackTrace();
        }

    }
}
