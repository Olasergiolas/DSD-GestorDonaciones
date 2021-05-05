package Servidor;

import Cliente.ClienteDonacionesI;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface GestorDonacionesI extends Remote {
    void registrarCliente(ClienteDonacionesI cliente) throws RemoteException;
    void receiveMSG(String msg) throws RemoteException;
    void broadcastMSG(String msg) throws RemoteException;
    void actualizarListadoReplicas() throws RemoteException, MalformedURLException,
            NotBoundException;
}
