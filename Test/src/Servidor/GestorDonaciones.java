package Servidor;

import Cliente.ClienteDonacionesI;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;

public class GestorDonaciones extends UnicastRemoteObject implements
    GestorDonacionesI{
    int id = -1;
    ArrayList<GestorDonacionesI> replicas;
    ArrayList<ClienteDonacionesI> clientes;

    public GestorDonaciones(int id) throws RemoteException {
        super();
        this.id = id;
        replicas = new ArrayList<GestorDonacionesI>();
        clientes = new ArrayList<ClienteDonacionesI>();
    }

    public void registrarCliente(ClienteDonacionesI cliente)
        throws RemoteException{
        clientes.add(cliente);
    }

    public void broadcastMSG(String msg) throws RemoteException{
        System.out.println("Haciendo broadcast de: " + msg);

        try {
            actualizarListadoReplicas();
        } catch(MalformedURLException | NotBoundException e){
            e.printStackTrace();
        }

        for (int i = 0; i < replicas.size(); ++i)
            replicas.get(i).receiveMSG(msg);
    }

    public void receiveMSG(String msg){
        System.out.println(msg);
    }

    public void actualizarListadoReplicas() throws RemoteException,
            MalformedURLException, NotBoundException {
        String replica_n = "";
        ArrayList<String> nombre_replicas = new ArrayList<>(Arrays.asList(Naming.list("rmi://localhost:9991")));

        for (int i = 0; i < nombre_replicas.size(); ++i){
            replica_n = "rmi:" + nombre_replicas.get(i);
            GestorDonacionesI gestor = (GestorDonacionesI) Naming.lookup(replica_n);
            if (!replicas.contains(gestor) && !replica_n.equals("rmi://localhost:9991/gestor" + id))
                replicas.add(gestor);

            //System.out.println(nombre_replicas.get(i));

            // TODO Gestionar réplicas que desaparecen
            // TODO Parametrizar el servidor a utilizar
        }
    }
}
