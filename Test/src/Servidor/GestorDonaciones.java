package Servidor;

import Cliente.ClienteDonacionesI;

import java.net.MalformedURLException;
import java.rmi.ConnectException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

// TODO Uso de archivos para los usuarios y la cantidad de donaciones que han realizado
// TODO Evitar que un usuario consulte el total de donaciones si no ha donado todavía
// TODO Implementar algoritmo de exlusión mutua

public class GestorDonaciones extends UnicastRemoteObject implements
    GestorDonacionesI{
    int id = -1;
    long subtotal;
    long total;
    String server;
    volatile boolean token;
    volatile Estados estado;
    Queue<Long> donaciones_pendientes;
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
        donaciones_pendientes = new LinkedList<>();
        this.token = token;
    }

    public AbstractMap.SimpleEntry<GestorDonacionesI, Integer> registrarCliente(String username)
        throws RemoteException, MalformedURLException, NotBoundException{
        actualizarListadoReplicas();

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
        Thread.sleep(100);
        System.out.println("Recibida una donación de " + cantidad + " euros");

        try {
            actualizarListadoReplicas();
        } catch(MalformedURLException | NotBoundException e){
            System.out.println("Error en la comunicación con las réplicas");
            System.out.println(e.getMessage());
        }

        if (token) {
            actualizarTotales(cantidad, username);
        }
        else{
            estado = Estados.INTENTANDO;
            System.out.println("Encolando donación de " + cantidad + "...");
            donaciones_pendientes.add(cantidad);
            clientes.put(username, true);
        }
    }

    public void actualizarTotales(long cantidad, String username) throws RemoteException {
        if (!username.isEmpty())
            clientes.put(username, true);

        subtotal += cantidad;
        total += cantidad;
        for (int i = 0; i < replicas.size(); ++i)
            replicas.get(i).incrementarTotalDonado(cantidad);
    }

    @Override
    public long getTotalDonado() throws RemoteException {
        return total;
    }

    @Override
    public long getTotalDonado(String username) throws RemoteException, InterruptedException {
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

    @Override
    public synchronized void setToken(boolean t){
        token = t;
    }

    @Override
    public synchronized boolean getToken() throws RemoteException {
        return token;
    }

    @Override
    public synchronized Estados getEstado() throws RemoteException {
        return estado;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void gestionarToken() throws RemoteException, MalformedURLException, NotBoundException, InterruptedException {
        GestorDonacionesI g;

        while(true){
            Thread.sleep(100);
            if (getToken()) {
                //System.out.println("Recibido token");
                if (getEstado() == Estados.INTENTANDO){
                    System.out.println("Usando token");
                    despacharDonacionesPendientes();
                    estado = Estados.LIBRE;
                }

                Thread.sleep(100);
                g = getSiguienteReplica();
                setToken(false);
                Thread.sleep(100);

                if (g != null) {
                    try {
                        System.out.println("Enviando token a " + g.getId());
                        g.setToken(true);
                    }catch (ConnectException e){
                        System.out.println("Error al coordinar con uno de los gestores del anillo, abortando...");
                        System.exit(-1);
                    }

                    Thread.sleep(100);
                }

                else {
                    setToken(true);
                }
            }
            else
                System.out.println("NO TENGO TOKEN");
        }
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

    public void despacharDonacionesPendientes() throws RemoteException {
        Iterator it = donaciones_pendientes.iterator();
        long cantidad;

        while(it.hasNext()){
            cantidad = (long)it.next();
            System.out.println("Despachando donación encolada de " + cantidad);
            actualizarTotales(cantidad, "");
        }
        donaciones_pendientes.clear();
    }
}
