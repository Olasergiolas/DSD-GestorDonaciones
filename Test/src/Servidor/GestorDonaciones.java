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
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

// TODO Uso de archivos para los usuarios y la cantidad de donaciones que han realizado
// TODO Evitar que un usuario consulte el total de donaciones si no ha donado todavía
// TODO Implementar algoritmo de exlusión mutua

public class GestorDonaciones extends UnicastRemoteObject implements
    GestorDonacionesI{
    int id = -1;
    long subtotal;
    long total;
    String server;
    boolean token;
    Estados estado;
    ArrayList<GestorDonacionesI> replicas;
    ArrayList<String> nombre_replicas;
    HashMap<String, Boolean> clientes;

    public GestorDonaciones(int id, long total, String server, boolean token) throws RemoteException {
        super();
        this.id = id;
        this.total = total;
        this.server = server;
        estado = Estados.LIBRE;
        subtotal = 0;
        replicas = new ArrayList<GestorDonacionesI>();
        clientes = new HashMap<>();
        nombre_replicas = new ArrayList<String>();
        this.token = token;

        //inicializarBD();
    }

    /*public void inicializarBD(){
        try{
            File archivo = new File("bd" + id + ".csv");
            if (archivo.createNewFile()) {
                System.out.println("BD" + id + " creada");

                FileWriter escritor = new FileWriter("bd" + id + ".csv");
                escritor.write("usuario, donante");
            }
            else
                System.out.println("BD" + id + " ya existente");
        }catch (IOException e){
            System.out.println("Error al crear la BD de usuarios");
            System.out.println(e.getMessage());
        }
    }*/

    public AbstractMap.SimpleEntry<GestorDonacionesI, Integer> registrarCliente(String username)
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

        if (!estaRegistrado(username)){
            min = clientes.size();
            candidato = new AbstractMap.SimpleEntry<>(this, min);

            for (int i = 0; i < replicas.size() && continuar; ++i){
                g = replicas.get(i);
                if (!g.estaRegistrado(username)){
                    tam = g.getNumeroClientesRegistrados();
                    if (tam < min){
                        min = tam;
                        candidato = new AbstractMap.SimpleEntry<>(g, tam);
                    }
                }

                else {
                    continuar = false;
                    candidato = new AbstractMap.SimpleEntry<>(g, -1);
                }
            }
        }

        else
            candidato = new AbstractMap.SimpleEntry<>(this, -1);

        if (candidato.getKey() != null && candidato.getValue() != -1)
            candidato.getKey().addCliente(username);

        else
            System.out.println("Cliente ya registrado anteriormente");


        return candidato;
    }

    @Override
    public synchronized void addCliente(String username) {
        clientes.put(username, false);
        System.out.println("Añadido un nuevo cliente!: " + username);
    }

    public boolean estaRegistrado(String username) throws RemoteException{
        return (clientes.get(username) == null) ? false : true;
    }

    public int getNumeroClientesRegistrados(){
        return clientes.size();
    }

    @Override
    public synchronized void donar(long cantidad, String username) throws RemoteException, InterruptedException {
        // TODO Entrar en sección crítica
        // TODO Actualizar el subtotal
        // TODO Comunicar el incremento al resto de réplicas
        // TODO Salir de sección crítica

        estado = Estados.INTENTANDO;
        while(!token){Thread.sleep(100);}
        estado = Estados.SC;

        System.out.println("Recibida una donación de " + cantidad + " euros");

        try {
            actualizarListadoReplicas();
        } catch(MalformedURLException | NotBoundException e){
            System.out.println("Error en la comunicación con las réplicas");
            System.out.println(e.getMessage());
        }

        clientes.put(username, true);
        subtotal += cantidad;
        total += cantidad;
        for (int i = 0; i < replicas.size(); ++i)
            replicas.get(i).incrementarTotalDonado(cantidad);

        estado = Estados.LIBRE;
    }

    @Override
    public long getTotalDonado() throws RemoteException {
        // TODO Entrar en sección crítica
        // TODO Devolver total
        // TODO Salir de sección crítica
        return total;
    }

    @Override
    public long getTotalDonado(String username) throws RemoteException {
        return (clientes.get(username) == true) ? getTotalDonado() : -1;
    }

    @Override
    public long getSubTotalDonado() throws RemoteException {
        return subtotal;
    }

    @Override
    public void incrementarTotalDonado(long incremento) throws RemoteException {
        total += incremento;
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

    @Override
    public void enviarToken() throws RemoteException, MalformedURLException, NotBoundException, InterruptedException {
        GestorDonacionesI g = getSiguienteReplica();
        token = false;
        Thread.sleep(100);
        if (g == null)
            this.recibirToken();

        else
            g.recibirToken();
    }

    @Override
    public void recibirToken() throws RemoteException, MalformedURLException, NotBoundException, InterruptedException {
        if (estado == Estados.INTENTANDO){
            token = true;
            //System.out.println("USANDO TOKEN");
            while (estado != Estados.LIBRE){Thread.sleep(100);}
        }
        enviarToken();
    }

    public GestorDonacionesI getSiguienteReplica() throws RemoteException, MalformedURLException, NotBoundException{
        actualizarListadoReplicas();
        GestorDonacionesI g = null;
        String[] regexpMatches;
        int siguienteReplica;
        boolean continuar = true;

        for (int i = 0; i < nombre_replicas.size() && continuar; ++i){
            regexpMatches = nombre_replicas.get(i).split("\\S+/gestor");
            siguienteReplica = Integer.parseInt(regexpMatches[1]);

            if (siguienteReplica > id){
                //System.out.println("Encontrada réplica siguiente: " + siguienteReplica);
                continuar = false;
                g = replicas.get(i-1);
            }
        }

        if (continuar && !replicas.isEmpty()) {
            //System.out.println("Reenviando el token al primer gestor");
            g = replicas.get(0);
        }

        return g;
    }

    public void actualizarListadoReplicas() throws RemoteException,
            MalformedURLException, NotBoundException {
        String replica_n = "";
        ArrayList<String> nombre_replicas_actual = new ArrayList<>(Arrays.asList(Naming.list("rmi://" + server + ":9991")));

        if (!nombre_replicas.equals(nombre_replicas_actual)){
            nombre_replicas = nombre_replicas_actual;
            replicas.clear();

            for (int i = 0; i < nombre_replicas.size(); ++i){
                replica_n = "rmi:" + nombre_replicas.get(i);
                GestorDonacionesI gestor = (GestorDonacionesI) Naming.lookup(replica_n);
                if (!replica_n.equals("rmi://" + server + ":9991/gestor" + id))
                    replicas.add(gestor);
            }
        }
    }
}
