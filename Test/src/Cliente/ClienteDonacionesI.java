package Cliente;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClienteDonacionesI extends Remote{
    void broadcastMSG(String msg) throws RemoteException;
}
