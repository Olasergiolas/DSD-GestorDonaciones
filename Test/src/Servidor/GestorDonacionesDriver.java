package Servidor;
import sun.misc.Signal;

import java.net.MalformedURLException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;

public class GestorDonacionesDriver {
    public static void main(String[] args) throws RemoteException, AlreadyBoundException {
        Registry registry;
        String server;

        if (args.length > 2){
            System.out.println("Modo de uso: idGestor [servidor]");
            System.exit(-1);
        }

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        if (args.length == 1){
            server = "localhost";
            try {
                registry = LocateRegistry.createRegistry(9991);
            }catch (RemoteException e){
                registry = LocateRegistry.getRegistry(9991);
                System.out.println("Puerto ocupado, utilizando el registro existente...");
            }
        }

        else{
            server = args[1];
            System.out.println("Obteniendo el registro en el servidor proporcionado...");
            registry = LocateRegistry.getRegistry(server, 9991);
        }

        //Consultamos si había otras réplicas anteriormente para iniciar el gestor con un valor
        //total actualizado
        ArrayList<String> nombre_replicas_actual = new ArrayList<>();
        long total = 0;
        try {
            nombre_replicas_actual = new ArrayList<>(Arrays.asList(Naming.list("rmi://" + server + ":9991")));

            if (!nombre_replicas_actual.isEmpty()) {
                total = ((GestorDonacionesI) Naming.lookup("rmi:" + nombre_replicas_actual.get(0))).getTotalDonado();
            }
        }catch (MalformedURLException | NotBoundException | UnknownHostException | ConnectException e){
            System.out.println("Error consultando al registro con la dirección: rmi:" + server + ":9991");
            System.exit(-1);
        }

        boolean token = (nombre_replicas_actual.isEmpty()) ? true : false;
        String nombre = "gestor" + args[0];
        System.out.println("Registrando el " + nombre + "...");
        GestorDonacionesI gestor = new GestorDonaciones(Integer.parseInt(args[0]), total, server, token);
        try {
            registry.bind(nombre, gestor);
            System.out.println(nombre + " registrado");
            gestor.enviarToken();
        }catch (AlreadyBoundException e){
            System.out.println("Id de gestor ya en uso, inténtelo con otro identificador");
            System.exit(-1);
        }catch (ServerException e){
            System.out.println("Por motivos de seguridad, no es posible registrar un gestor en un servidor remoto, " +
                    "mantenga los gestores en un mismo host");
            System.exit(-1);
        }catch (MalformedURLException | NotBoundException | InterruptedException e){
            System.out.println("Error al iniciar la EM basada en anillos");
        }

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
