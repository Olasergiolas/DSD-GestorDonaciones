package Cliente;

import Servidor.GestorDonaciones;
import Servidor.GestorDonacionesI;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class ClienteDonaciones  extends UnicastRemoteObject implements
    ClienteDonacionesI, Runnable{
    Servidor.GestorDonacionesI gestor;
    String username;

    public ClienteDonaciones(Servidor.GestorDonacionesI g) throws RemoteException{
        super();
        gestor = g;
        username = "";
    }

    public int comprobarEleccion(ArrayList<Integer> elecciones, Scanner input){
        int eleccion = -1;

        if (input.hasNextInt()) {
            eleccion = input.nextInt();

            if (!elecciones.contains(eleccion)) {
                System.out.println("\nOpción no disponible");
                eleccion = -1;
            }
        }
        else{
            System.out.println("\nPor favor, introduzca un entero");
            input.next();
        }

        return eleccion;
    }

    @Override
    public void registrarme(GestorDonacionesI gestor) throws RemoteException, MalformedURLException, NotBoundException {
        Scanner input = new Scanner(System.in);

        System.out.println("Introduzca un nombre de usuario: ");
        username = input.nextLine();

        AbstractMap.SimpleEntry<GestorDonacionesI, Integer> res = gestor.registrarCliente(username);
        this.gestor = res.getKey();

        if (res.getValue() == -1)
            System.out.println("Cliente previamente registrado, bienvenido de nuevo!");
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
            System.out.println("\n********************  Bienvenido! ********************");
            System.out.println("[1] Registrarte");
            System.out.println("[2] Iniciar Sesión");
            System.out.println("********************************************************");

            eleccion = comprobarEleccion(new ArrayList<Integer>(Arrays.asList(1, 2)), input);

            if (eleccion != -1)
                continuar = false;
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

        while(true){
            System.out.println("\n********************  Bienvenido " + username + "********************");
            System.out.println("[1] Realizar donación");
            System.out.println("[2] Consultar total donado al sistema");
            System.out.println("[3] Salir");
            System.out.println("***********************************************************************");

            eleccion = comprobarEleccion(new ArrayList<Integer>(Arrays.asList(1, 2, 3)), input);

            switch (eleccion){
                case 1:
                    int cantidad = 0;
                    System.out.println("Indique la cantidad a donar: ");
                    if (input.hasNextInt()) {
                        cantidad = input.nextInt();
                        try {
                            gestor.donar(cantidad);
                        }catch (RemoteException e){
                            System.out.println("Error al realizar la donación, inténtelo de nuevo");
                        }
                    }
                    else
                        System.out.println("Introduzca un número por favor.");
                    break;
                case 2:
                    long total = 0;

                    try {
                        total = gestor.getTotalDonado();
                        System.out.println("Se ha donado un total de " + total + " euros");
                    }catch (RemoteException e){
                        System.out.println("Error en la comunicación con el servidor, inténtelo de nuevo");
                    }
                    break;

                case 3:
                    System.exit(0);
            }
        }
    }
}
