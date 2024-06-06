package tech.novoess.skytable4j;


import javafx.concurrent.Task;

/// <summary>
/// A builder to create a <see cref="Connection"/>.
/// </summary>
public class ConnectionBuilder
{
    private static String _host = "127.0.0.1";
    private static short _port = 2003;
    private static String _certPath = "";

    /// <summary>Sets the Host that the connection should be made to. Default is '127.0.0.1'.</summary>
    /// <param name="host">The host that the connection should be made to.</param>
    public static void setHost(String host)
    {
        _host = host;
    }

    /// <summary>Sets the Port that the connection should be made to. Default is '2003'.</summary>
    /// <param name="port">The port that the connection should be made to.</param>
    public static void setPort(short port)
    {
        _port = port;
    }

    /// <summary>Enables TLS on the connection using the provided certificate file.</summary>
    /// <param name="certPath">The path to the certificate file that should be used for the communication.</param>
    public static void UseTls(String certPath)
    {
        _certPath = certPath;
    }

    /// <summary>
    /// Creates the <see cref="Connection"/> and connects to the Skytable instance.
    /// If a certificate path has been provided through the <see cref="ConnectionBuilder.UseTls"/> function it will attempt to
    /// connect to the Skytable instance securely.
    ///</summary>
    public static Connection Build()
    {
        var connection = new Connection(_host, _port, _certPath);
        connection.Connect();
        return connection;
    }

    /// <summary>
    /// Creates the <see cref="Connection"/> and connects asynchronously to the Skytable instance.
    /// If a certificate path has been provided through the <see cref="ConnectionBuilder.UseTls"/> function it will attempt to
    /// connect to the Skytable instance securely.
    ///</summary>
    public static Connection BuildAsync()
    {
        var connection = new Connection(_host, _port, _certPath);
        connection.ConnectAsync();
        return connection;
    }
}