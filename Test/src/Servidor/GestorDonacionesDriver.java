package Servidor;

import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

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
        registry.bind(nombre, new GestorDonaciones(Integer.parseInt(args[0])));
        System.out.println(nombre + " registrado");
    }
}
