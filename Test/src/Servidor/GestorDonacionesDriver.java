package Servidor;
import sun.misc.Signal;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;

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

        //Consultamos si había otras réplicas anteriormente para iniciar el gestor con un valor
        //total actualizado
        ArrayList<String> nombre_replicas_actual;
        long total = 0;
        try {
            nombre_replicas_actual = new ArrayList<>(Arrays.asList(Naming.list("rmi://localhost:9991")));

            if (!nombre_replicas_actual.isEmpty()) {
                total = ((GestorDonacionesI) Naming.lookup("rmi:" + nombre_replicas_actual.get(0))).getTotalDonado();
            }
        }catch (MalformedURLException | NotBoundException e){
            System.out.println("Error consultando al registro");
        }

        String nombre = "gestor" + args[0];
        System.out.println("Registrando el " + nombre + "...");
        GestorDonacionesI gestor = new GestorDonaciones(Integer.parseInt(args[0]), total);
        try {
            registry.bind(nombre, gestor);
        }catch (AlreadyBoundException e){
            System.out.println("Id de gestor ya en uso, inténtelo con otro identificador");
            System.exit(-1);
        }
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
