package Servidor;
import sun.misc.Signal;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class GestorDonacionesDriver {
    public static void main(String[] args) throws RemoteException, AlreadyBoundException {
        Registry registry;

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            registry = LocateRegistry.createRegistry(9991);
        }catch (RemoteException e){
            registry = LocateRegistry.getRegistry(9991);
            System.out.println("Puerto ocupado, utilizando el registro existente...");
        }

        String nombre = "gestor" + args[0];
        System.out.println("Registrando el " + nombre + "...");
        GestorDonacionesI gestor = new GestorDonaciones(Integer.parseInt(args[0]));
        registry.bind(nombre, gestor);
        System.out.println(nombre + " registrado");

        final Registry finalRegistry = registry;
        Signal.handle(new Signal("INT"),  // SIGINT
                signal -> {
                    System.out.println("\nApagando el servidor...");
                    try {
                        finalRegistry.unbind(nombre);
                        System.exit(0);
                    }catch (RemoteException | NotBoundException e){
                        System.out.println("Registro inalcanzable, apagando...");
                        System.exit(-1);
                    }
                });
    }
}
