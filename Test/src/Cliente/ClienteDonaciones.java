package Cliente;

import Servidor.GestorDonaciones;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

public class ClienteDonaciones  extends UnicastRemoteObject implements
    ClienteDonacionesI, Runnable{
    Servidor.GestorDonacionesI gestor;

    public ClienteDonaciones(Servidor.GestorDonacionesI g) throws RemoteException{
        super();
        gestor = g;
        //añadir el cliente al gestor
        g.registrarCliente(this);
    }

    @Override
    public void broadcastMSG(String msg) throws RemoteException{
        gestor.broadcastMSG(msg);
    }

    @Override
    public void run(){
        try {
            broadcastMSG("Prueba");
        }catch (RemoteException e){
            e.printStackTrace();
        }

    }
}
