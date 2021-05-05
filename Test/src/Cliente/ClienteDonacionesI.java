package Cliente;

import Servidor.GestorDonacionesI;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClienteDonacionesI extends Remote{
    void broadcastMSG(String msg) throws RemoteException;
    void registrarme(GestorDonacionesI gestor) throws RemoteException, MalformedURLException, NotBoundException;
}
