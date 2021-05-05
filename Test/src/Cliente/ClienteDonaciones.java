package Cliente;

import Servidor.GestorDonaciones;
import Servidor.GestorDonacionesI;
import javafx.util.Pair;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.AbstractMap;
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
        try {
            registrarme(gestor);
            broadcastMSG("Prueba");
        }catch (RemoteException | MalformedURLException | NotBoundException e){
            e.printStackTrace();
        }

    }
}
