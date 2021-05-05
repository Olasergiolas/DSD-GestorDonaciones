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
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            String nombre = "rmi://localhost:9991/gestor" + args[0];
            GestorDonacionesI gestor = (GestorDonacionesI) Naming
                    .lookup(nombre);
            System.out.println("Encontrado el gestor" + args[0]);
            new Thread(new ClienteDonaciones(gestor)).start();
        }catch (RemoteException | MalformedURLException | NotBoundException e){
            e.printStackTrace();
        }
    }
}
