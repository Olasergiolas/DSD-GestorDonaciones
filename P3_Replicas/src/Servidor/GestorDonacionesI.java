package Servidor;

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
    void donar(long cantidad, String username) throws RemoteException, InterruptedException;
    long getTotalDonado() throws RemoteException;
    long getTotalDonado(String username) throws RemoteException, InterruptedException;
    long getSubTotalDonado() throws RemoteException;
    void incrementarTotalDonado(long incremento) throws RemoteException;
    void actualizarListadoReplicas() throws RemoteException, MalformedURLException,
            NotBoundException;
    void gestionarToken() throws RemoteException, MalformedURLException, NotBoundException, InterruptedException;
    void setToken(boolean t) throws RemoteException;
    boolean getToken() throws RemoteException;
    Estados getEstado() throws RemoteException;
    int getId() throws RemoteException;
}
