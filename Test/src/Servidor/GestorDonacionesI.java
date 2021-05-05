package Servidor;

import Cliente.ClienteDonacionesI;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.AbstractMap;

public interface GestorDonacionesI extends Remote {
    boolean estaRegistrado(ClienteDonacionesI cliente) throws RemoteException;
    int getNumeroClientesRegistrados() throws RemoteException;
    AbstractMap.SimpleEntry<GestorDonacionesI, Integer> registrarCliente(ClienteDonacionesI cliente) throws RemoteException, MalformedURLException, NotBoundException;
    void addCliente(ClienteDonacionesI cliente) throws RemoteException;
    void receiveMSG(String msg) throws RemoteException;
    void broadcastMSG(String msg) throws RemoteException;
    void actualizarListadoReplicas() throws RemoteException, MalformedURLException,
            NotBoundException;
}
