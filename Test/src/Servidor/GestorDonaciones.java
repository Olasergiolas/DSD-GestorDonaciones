package Servidor;

import Cliente.ClienteDonacionesI;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;

public class GestorDonaciones extends UnicastRemoteObject implements
    GestorDonacionesI{
    int id = -1;
    ArrayList<GestorDonacionesI> replicas;
    ArrayList<String> nombre_replicas;
    ArrayList<ClienteDonacionesI> clientes;

    public GestorDonaciones(int id) throws RemoteException {
        super();
        this.id = id;
        replicas = new ArrayList<GestorDonacionesI>();
        clientes = new ArrayList<ClienteDonacionesI>();
        nombre_replicas = new ArrayList<String>();
    }

    public AbstractMap.SimpleEntry<GestorDonacionesI, Integer> registrarCliente(ClienteDonacionesI cliente)
        throws RemoteException, MalformedURLException, NotBoundException{
        actualizarListadoReplicas();

        // TODO Comprobamos si esta réplica es la más indicada
        // TODO Ver si está registrado el cliente está registrado en alguna réplica
        // TODO Si no, buscar la réplica con menos clientes y hacerle un .addCliente
        // TODO Si está registrado en alguna de las réplicas, devolver un error
        int min;
        int tam;
        GestorDonacionesI g;
        AbstractMap.SimpleEntry<GestorDonacionesI, Integer> candidato;
        boolean continuar = true;

        if (!estaRegistrado(cliente)){
            min = clientes.size();
            candidato = new AbstractMap.SimpleEntry<GestorDonacionesI, Integer>(this, min);

            for (int i = 0; i < replicas.size() && continuar; ++i){
                g = replicas.get(i);
                if (!g.estaRegistrado(cliente)){
                    tam = g.getNumeroClientesRegistrados();
                    if (tam < min){
                        min = tam;
                        candidato = new AbstractMap.SimpleEntry<GestorDonacionesI, Integer>(g, tam);
                    }
                }

                else {
                    continuar = false;
                    candidato = new AbstractMap.SimpleEntry<GestorDonacionesI, Integer>(g, -1);
                }
            }
        }

        else
            candidato = new AbstractMap.SimpleEntry<GestorDonacionesI, Integer>(this, -1);

        if (candidato.getKey() != null && candidato.getValue() != -1)
            candidato.getKey().addCliente(cliente);

        else
            System.out.println("Cliente ya registrado anteriormente");


        return candidato;
    }

    @Override
    public void addCliente(ClienteDonacionesI cliente) {
        clientes.add(cliente);
        System.out.println("Añadido un nuevo cliente!");
    }

    public boolean estaRegistrado(ClienteDonacionesI cliente) throws RemoteException{
        return clientes.contains(cliente);
    }

    public int getNumeroClientesRegistrados(){
        return clientes.size();
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
        ArrayList<String> nombre_replicas_actual = new ArrayList<>(Arrays.asList(Naming.list("rmi://localhost:9991")));

        if (!nombre_replicas.equals(nombre_replicas_actual)){
            nombre_replicas = nombre_replicas_actual;

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
}
