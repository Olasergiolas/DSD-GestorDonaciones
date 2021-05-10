package Cliente;

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

    public void procesarInput(){
        int eleccion = -1;
        Scanner input = new Scanner(System.in);

        eleccion = comprobarEleccion(new ArrayList<Integer>(Arrays.asList(1, 2, 3)), input);
        switch (eleccion){
            case 1:
                int cantidad = 0;
                System.out.println("\nIndique la cantidad a donar: ");
                if (input.hasNextInt()) {
                    cantidad = input.nextInt();
                    try {
                        gestor.donar(cantidad, username);
                        System.out.println("\nDonación de " + cantidad + " realizada con éxito");
                    }catch (RemoteException | InterruptedException e){
                        System.out.println("Error al realizar la donación, inténtelo de nuevo");
                    }
                }
                else
                    System.out.println("\nIntroduzca un número por favor.");
                break;
            case 2:
                long total = 0;

                try {
                    total = gestor.getTotalDonado(username);

                    if (total == -1)
                        System.out.println("Es necesario donar antes de consultar el total");
                    else
                        System.out.println("Se ha donado un total de " + total + " euros");
                }catch (RemoteException | InterruptedException e){
                    System.out.println("Error en la comunicación con el servidor, inténtelo de nuevo");
                }
                break;

            case 3:
                System.exit(0);
        }
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
    public void run(){
        System.out.println("\n********************  Bienvenido! ********************\n");
        try {
            registrarme(gestor);
        }catch (RemoteException | MalformedURLException | NotBoundException e){
            e.printStackTrace();
        }

        while(true){
            System.out.println("\n********************  Bienvenido " + username + " ********************");
            System.out.println("[1] Realizar donación");
            System.out.println("[2] Consultar total donado al sistema");
            System.out.println("[3] Salir");
            System.out.println("*************************************************************");

            procesarInput();
        }
    }
}
