package Servidor;

import Cliente.ClienteDonacionesI;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.AbstractMap;

public interface GestorDonacionesI extends Remote {
    boolean estaRegistrado(String username) throws RemoteException;
    int getNumeroClientesRegistrados() throws RemoteException;
    AbstractMap.SimpleEntry<GestorDonacionesI, Integer> registrarCliente(String username) throws RemoteException, MalformedURLException, NotBoundException;
    void addCliente(String username) throws RemoteException;
    void receiveMSG(String msg) throws RemoteException;
    void donar(long cantidad, String username) throws RemoteException;
    long getTotalDonado() throws RemoteException;
    long getTotalDonado(String username) throws RemoteException;
    long getSubTotalDonado() throws RemoteException;
    void incrementarTotalDonado(long incremento) throws RemoteException;
    void broadcastMSG(String msg) throws RemoteException;
    void actualizarListadoReplicas() throws RemoteException, MalformedURLException,
            NotBoundException;
}
