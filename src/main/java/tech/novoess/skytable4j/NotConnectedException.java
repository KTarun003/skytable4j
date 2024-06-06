package tech.novoess.skytable4j;


/// <summary>Thrown if the connection is not connected to Skytable while trying to run a query.</summary>
public class NotConnectedException extends Exception
{
    NotConnectedException() {
        super("Call Connect before using the Connection.");
    }
}
