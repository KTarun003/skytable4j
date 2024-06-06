package tech.novoess.skytable4j;

import java.net.Socket;
import java.util.List;
import java.util.stream.Stream;

/// <summary>A database connection over Skyhash/TCP.</summary>
public class Connection implements IConnection
{
    /// <summary>Gets the host that this connection is connected to.</summary>
    public String Host;

    /// <summary>Gets the port that this connection is connected to.</summary>
    public short Port;

    /// <summary>Gets the entity that this connection is connected to.</summary>
    public String Entity = "default:default";

    /// <summary>Gets the connection state.</summary>
    public boolean isConnected() {
        return  _client.isConnected();
    }

    private String _certPath;
    private final short BUF_CAP = 4096;
    private final int WSAENOTCONN = 10057;
    private Socket _client;
    private Stream _stream;
    private List<Byte> _buffer;

    /// <summary>
    /// Create a new connection to a Skytable instance hosted on the provided host and port with Tls disabled.
    /// Call <see cref="Connection.Connect"/> or <see cref="Connection.ConnectAsync"/> to connect after creating the connection.
    ///</summary>
    /// <Param name="host">The host which is running Skytable.</Param>
    /// <Param name="port">The port which the host is running Skytable.</Param>
    public Connection(String host, int port)
    {
        new Connection(host, (short)port, "");
    }

    /// <summary>
    /// Create a new connection to a Skytable instance hosted on the provided host and port with Tls enabled.
    /// Call <see cref="Connection.Connect"/> or <see cref="Connection.ConnectAsync"/> to connect after creating the connection.
    ///</summary>
    /// <Param name="host">The host which is running Skytable.</Param>
    /// <Param name="port">The port which the host is running Skytable. This has to be configured as a secure port in Skytable.</Param>
    /// <Param name="certPath">Path to the certificate file.</Param>
    public Connection(String host, short port, String certPath)
    {
        Host = host;
        Port = port;
        _client = new TcpClient();
        _buffer = new List<Byte>(BUF_CAP);
        _certPath = certPath;
    }

    /// <summary>
    /// Open a connection to the Host:Port specified in the constructor.
    /// If a Certificate path is specified an attempt will be made to set up a secure connection.
    /// </summary>
    public void Connect()
    {
        _client.Connect(Host, Port);
        _stream = _client.GetStream();

        if (!String.IsNullOrEmpty(_certPath))
            AuthenticateSsl();
    }

    /// <summary>
    /// Open a connection asynchronously to the Host:Port specified in the constructor.
    /// If a Certificate path is specified an attempt will be made to set up a secure connection.
    /// </summary>
    public void ConnectAsync()
    {
        _client.ConnectAsync(Host, Port);
        _stream = _client.GetStream();

        if (!String.IsNullOrEmpty(_certPath))
            await AuthenticateSslAsync();
    }

    /// <summary>Close the connection.</summary>
    public void Close()
    {
        _client.Close();
    }

    /// <summary>Dispose of the connection. This will close the connection.</summary>
    public void Dispose()
    {
        Close();
    }

    private void AuthenticateSsl()
    {
        var sslStream = new SslStream(
            _client.GetStream(),
            false,
            new RemoteCertificateValidationCallback(ValidateServerCertificate),
            new LocalCertificateSelectionCallback(SelectCertificate)
            );

        try
        {
            sslStream.AuthenticateAsClient(Host);
            _stream = sslStream;
        }
        catch (Exception ex)
        {
            _client.Close();
            throw ex;
        }
    }

    private void AuthenticateSslAsync()
    {
        var sslStream = new SslStream(
            _client.GetStream(),
            false,
            new RemoteCertificateValidationCallback(ValidateServerCertificate),
            new LocalCertificateSelectionCallback(SelectCertificate)
            );

        try
        {
            sslStream.AuthenticateAsClientAsync(Host);
            _stream = sslStream;
        }
        catch (Exception ex)
        {
            _client.Close();
            throw ex;
        }
    }

    private X509Certificate SelectCertificate(
        object sender,
        String targetHost,
        X509CertificateCollection localCertificates,
        X509Certificate remoteCertificate,
        String[] acceptableIssuers)
    {
        var cert = new X509Certificate(_certPath);
        return cert;
    }

    private static boolean ValidateServerCertificate(
          object sender,
          X509Certificate certificate,
          X509Chain chain,
          SslPolicyErrors sslPolicyErrors)
    {
        if (sslPolicyErrors == SslPolicyErrors.None)
            return true;

        // Allow self-signed certificates for now.
        if (chain.ChainStatus.Length == 1)
        {
            if (sslPolicyErrors == SslPolicyErrors.RemoteCertificateChainErrors || certificate.Subject == certificate.Issuer)
            {
                if (chain.ChainStatus[0].Status == X509ChainStatusFlags.UntrustedRoot)
                    return true;
            }
        }

        // Do not allow this client to communicate with unauthenticated servers.
        return false;
    }

    /// <summary>
    /// This function will write a <see cref="Query"/> to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public SkyResult<Element> RunSimpleQuery(Query query) throws Exception {
        if (query.ArgumentCount == 0)
            throw new Exception("A query cannot be empty!");

        return RunQuery(query);
    }

    /// <summary>
    /// This function will write a <see cref="Query"/> asynchronously to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public async Task<SkyResult<Element>> RunSimpleQueryAsync(Query query)
    {
        if (query.ArgumentCount == 0)
            throw new Exception("A query cannot be empty!");

        return await RunQueryAsync(query);
    }

    /// <summary>
    /// This function will write a <see cref="Pipeline"/> to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public SkyResult<Element> RunPipeline(Pipeline pipeline)
    {
        if (pipeline.Count == 0)
            throw new Exception("A Pipeline cannot be empty!");

        return RunQuery(pipeline);
    }

    /// <summary>
    /// This function will write a <see cref="Pipeline"/> asynchronously to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public async Task<SkyResult<Element>> RunPipelineAsync(Pipeline pipeline)
    {
        if (pipeline.Count == 0)
            throw new Exception("A Pipeline cannot be empty!");

        return await RunQueryAsync(pipeline);
    }

    private SkyResult<Element> RunQuery(IQueryWriter queryWriter)
    {
        if (!IsConnected)
            throw new NotConnectedException();

        queryWriter.WriteTo(_stream);

        while (true)
        {
            var buffer = new byte[1024];
            var read = _stream.Read(buffer, 0, 1024);
            if (read == 0)
                throw new Exception("ConnectionReset");

            _buffer.AddRange(buffer[..read]);

            var result = ParseResponse();
            if (result.IsOk)
                return result;

            switch (result.Error)
            {
                case ParseError.NotEnough:
                    continue; // We need to read again to get the complete response.
                case ParseError.UnexpectedByte:
                case ParseError.BadPacket:
                    _buffer.Clear();
                    return result;
                case ParseError.DataTypeParseError:
                case ParseError.UnknownDataType:
                case ParseError.Empty:
                    return result;
            }
        }
    }

    private async Task<SkyResult<Element>> RunQueryAsync(IQueryWriter queryWriter)
    {
        if (!IsConnected)
            throw new NotConnectedException();

        await queryWriter.WriteToAsync(_stream);

        while (true)
        {
            var buffer = new byte[1024];
            var read = await _stream.ReadAsync(buffer, 0, 1024);
            if (read == 0)
                throw new Exception("ConnectionReset");

            _buffer.AddRange(buffer[..read]);

            var result = ParseResponse();
            if (result.IsOk)
                return result;

            switch (result.Error)
            {
                case ParseError.NotEnough:
                    continue; // We need to read again to get the complete response.
                case ParseError.UnexpectedByte:
                case ParseError.BadPacket:
                    _buffer.Clear();
                    return result;
                case ParseError.DataTypeParseError:
                case ParseError.UnknownDataType:
                case ParseError.Empty:
                    return result;
            }
        }
    }

    private SkyResult<Element> ParseResponse()
    {
        // The connection was possibly reset
        if (_buffer.Count == 0)
            return SkyResult<Element>.Err(ParseError.Empty);

        var parser = new Parser(_buffer);
        var (result, forward_by) = parser.Parse();
        _buffer.RemoveRange(0, forward_by);
        return result;
    }

    /// <summary>
    /// This function will create a GET <see cref="Query"/> and write it to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public SkyResult<T> Get(String key)
    {
        var query = new Query();
        query.Push("get");
        query.Push(key);
        return RunSimpleQuery(query);
    }

    /// <summary>
    /// This function will create a GET <see cref="Query"/> and write it to the stream and read the response asynchronously from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public Task<SkyResult<T>> GetAsync(String key)
    {
        var query = new Query();
        query.Push("get");
        query.Push(key);
        return await RunSimpleQueryAsync(query);
    }

    /// <summary>
    /// This function will create a GET <see cref="Query"/> and write it to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and try to return type T if successful.
    /// </summary>
    public SkyResult<T> Get<T>(String key) where T: Skyhash, new()
    {
        var query = new Query();
        query.Push("get");
        query.Push(key);
        var result = RunSimpleQuery(query);
        if (result.IsOk)
            return SkyResult<T>.Ok(new T().From<T>(result.Item));
        return SkyResult<T>.Err(result.Error);
    }

    /// <summary>
    /// This function will create a GET <see cref="Query"/> and write it to the stream and read the response asynchronously from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and try to return type T if successful.
    /// </summary>
    public async Task<SkyResult<T>> GetAsync<T>(String key) where T: Skyhash, new()
    {
        var query = new Query();
        query.Push("get");
        query.Push(key);
        var response = await RunSimpleQueryAsync(query);
        if (response.IsOk)
            return SkyResult<T>.Ok(new T().From<T>(response.Item));
        return SkyResult<T>.Err(response.Error);
    }

    /// <summary>
    /// This function will create a SET <see cref="Query"/> and write it to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public SkyResult<Element> Set(String key, String value)
    {
        var query = new Query();
        query.Push("set");
        query.Push(key);
        query.Push(value);
        return RunSimpleQuery(query);
    }

    /// <summary>
    /// This function will create a SET <see cref="Query"/> and write it to the stream and read the response asynchronously from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public async Task<SkyResult<Element>> SetAsync(String key, String value)
    {
        var query = new Query();
        query.Push("set");
        query.Push(key);
        query.Push(value);
        return await RunSimpleQueryAsync(query);
    }

    /// <summary>
    /// This function will create a SET <see cref="Query"/> and write it to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public SkyResult<Element> Set(String key, Skyhash value)
    {
        var query = new Query();
        query.Push("set");
        query.Push(key);
        query.Push(value.Into());
        return RunSimpleQuery(query);
    }

    /// <summary>
    /// This function will create a SET <see cref="Query"/> and write it to the stream and read the response asynchronously from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public async Task<SkyResult<Element>> SetAsync(String key, Skyhash value)
    {
        var query = new Query();
        query.Push("set");
        query.Push(key);
        query.Push(value.Into());
        return await RunSimpleQueryAsync(query);
    }

    /// <summary>
    /// This function will create a DEL <see cref="Query"/> and write it to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public SkyResult<Element> Delete(String key)
    {
        var query = new Query();
        query.Push("del");
        query.Push(key);
        return RunSimpleQuery(query);
    }

    /// <summary>
    /// This function will create a DEL <see cref="Query"/> and write it to the stream and read the response asynchronously from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public async Task<SkyResult<Element>> DeleteAsync(String key)
    {
        var query = new Query();
        query.Push("del");
        query.Push(key);
        return await RunSimpleQueryAsync(query);
    }

    /// <summary>
    /// This function will create an USET <see cref="Query"/> and write it to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public SkyResult<Element> USet(String key, String value)
    {
        var query = new Query();
        query.Push("uset");
        query.Push(key);
        query.Push(value);
        return RunSimpleQuery(query);
    }

    /// <summary>
    /// This function will create an USET <see cref="Query"/> and write it to the stream and read the response asynchronously from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public async Task<SkyResult<Element>> USetAsync(String key, String value)
    {
        var query = new Query();
        query.Push("uset");
        query.Push(key);
        query.Push(value);
        return await RunSimpleQueryAsync(query);
    }

    /// <summary>
    /// This function will create an USET <see cref="Query"/> and write it to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public SkyResult<Element> USet(String key, Skyhash value)
    {
        var query = new Query();
        query.Push("uset");
        query.Push(key);
        query.Push(value.Into());
        return RunSimpleQuery(query);
    }

    /// <summary>
    /// This function will create an USET <see cref="Query"/> and write it to the stream and read the response asynchronously from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public async Task<SkyResult<Element>> USetAsync(String key, Skyhash value)
    {
        var query = new Query();
        query.Push("uset");
        query.Push(key);
        query.Push(value.Into());
        return await RunSimpleQueryAsync(query);
    }

    /// <summary>
    /// This function will create a USE <see cref="Query"/> and write it to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public SkyResult<Element> Use(String keyspace, String table)
    {
        var entity = String.Join(':', keyspace, table);
        var query = new Query();
        query.Push("use");
        query.Push(entity);
        var result = RunSimpleQuery(query);
        if (result.IsOk) // TODO: Check if the Response Element is Okay.
            Entity = entity;

        return result;
    }

    /// <summary>
    /// This function will create a USE <see cref="Query"/> and write it to the stream and read the response asynchronously from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public async Task<SkyResult<Element>> UseAsync(String keyspace, String table)
    {
        var entity = String.Join(':', keyspace, table);
        var query = new Query();
        query.Push("use");
        query.Push(entity);
        var result = await RunSimpleQueryAsync(query);
        if (result.IsOk) // TODO: Check if the Response Element is Okay.
            Entity = entity;

        return result;
    }

    /// <summary>
    /// This function will create a POP <see cref="Query"/> and write it to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public SkyResult<Element> Pop(String key)
    {
        var query = new Query();
        query.Push("pop");
        query.Push(key);
        return RunSimpleQuery(query);
    }

    /// <summary>
    /// This function will create a POP <see cref="Query"/> and write it to the stream and read the response asynchronously from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public async Task<SkyResult<Element>> PopAsync(String key)
    {
        var query = new Query();
        query.Push("pop");
        query.Push(key);
        return await RunSimpleQueryAsync(query);
    }

    /// <summary>
    /// This function will create a POP <see cref="Query"/> and write it to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and try to return type T if successful.
    /// </summary>
    public SkyResult<T> Pop<T>(String key) where T: Skyhash, new()
    {
        var query = new Query();
        query.Push("pop");
        query.Push(key);
        var result = RunSimpleQuery(query);
        if (result.IsOk)
            return SkyResult<T>.Ok(new T().From<T>(result.Item));
        return SkyResult<T>.Err(result.Error);
    }

    /// <summary>
    /// This function will create a POP <see cref="Query"/> and write it to the stream and read the response asynchronously from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and try to return type T if successful.
    /// </summary>
    public async Task<SkyResult<T>> PopAsync<T>(String key) where T: Skyhash, new()
    {
        var query = new Query();
        query.Push("pop");
        query.Push(key);
        var response = await RunSimpleQueryAsync(query);
        if (response.IsOk)
            return SkyResult<T>.Ok(new T().From<T>(response.Item));
        return SkyResult<T>.Err(response.Error);
    }
}
