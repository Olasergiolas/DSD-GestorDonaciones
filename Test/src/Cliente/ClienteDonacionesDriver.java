package Cliente;

import Servidor.GestorDonacionesI;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class ClienteDonacionesDriver {
    public static void main(String[] args){
        if (args.length != 2){
            System.out.println("Se necesitan dos parámetros: idGestor servidor");
            System.exit(-1);
        }

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            String nombre = "rmi://" + args[1] + ":9991/gestor" + args[0];
            System.out.println(nombre);
            /*GestorDonacionesI gestor = (GestorDonacionesI) Naming
                    .lookup(nombre);
            System.out.println("Encontrado el gestor" + args[0]);*/



            Registry registry = LocateRegistry.getRegistry(args[1], 9991);
            GestorDonacionesI gestor = (GestorDonacionesI) registry.lookup("gestor" + args[0]);
            new Thread(new ClienteDonaciones(gestor)).start();
        }catch (RemoteException | NotBoundException e){
            System.out.println("Error al interactuar con el servidor " + e.getMessage());
        }
    }
}
