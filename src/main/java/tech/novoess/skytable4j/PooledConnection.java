package tech.novoess.skytable4j;

import javafx.concurrent.Task;
import tech.novoess.skytable4j.querying.Element;

/**
 * <summary>A pooled database connection over Skyhash/TCP.</summary>
 */
public class PooledConnection implements IConnection
{
    private ConnectionPool _pool;
    private Connection _connection;
    private ConnectionType _connectionType;

    PooledConnection(ConnectionPool pool, Connection connection, ConnectionType connectionType)
    {
        _pool = pool;
        _connection = connection;
        _connectionType = connectionType;
    }

    /// <summary>Dispose of the pooled connection, returning it to the pool or closing it if it was a temporary connection.</summary>
    public void Dispose()
    {
        if (_connectionType == ConnectionType.Pooled)
            _pool.Return(_connection);
        else
            _connection.Close();
    }

    /// <summary>
    /// This function will create a GET <see cref="Query"/> and write it to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and try to return type T if successful.
    /// </summary>
    public <T> SkyResult<T> Get(String key)
    {
        return _connection.Get<T>(key);
    }

    /// <summary>
    /// This function will create a GET <see cref="Query"/> and write it to the stream and read the response asynchronously from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and try to return type T if successful.
    /// </summary>
    public <T> SkyResult<T> GetAsync(String key)
    {
        return _connection.GetAsync(key);
    }

    /// <summary>
    /// This function will create a SET <see cref="Query"/> and write it to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public SkyResult<Element> Set(String key, String value)
    {
        return _connection.Set(key, value);
    }

    /// <summary>
    /// This function will create a SET <see cref="Query"/> and write it to the stream and read the response asynchronously from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public Task<SkyResult<Element>> SetAsync(String key, String value)
    {
        return _connection.SetAsync(key, value);
    }

    /// <summary>
    /// This function will create a SET <see cref="Query"/> and write it to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public SkyResult<Element> Set(String key, Skyhash value)
    {
        return _connection.Set(key, value);
    }

    /// <summary>
    /// This function will create a SET <see cref="Query"/> and write it to the stream and read the response asynchronously from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public Task<SkyResult<Element>> SetAsync(String key, Skyhash value)
    {
        return _connection.SetAsync(key, value);
    }

    /// <summary>
    /// This function will create a DEL <see cref="Query"/> and write it to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public SkyResult<Element> Delete(String key)
    {
        return _connection.Delete(key);
    }

    /// <summary>
    /// This function will create a DEL <see cref="Query"/> and write it to the stream and read the response asynchronously from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public Task<SkyResult<Element>> DeleteAsync(String key)
    {
        return _connection.DeleteAsync(key);
    }

    /// <summary>
    /// This function will create an USET <see cref="Query"/> and write it to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public SkyResult<Element> USet(String key, String value)
    {
        return _connection.USet(key, value);
    }

    /// <summary>
    /// This function will create an USET <see cref="Query"/> and write it to the stream and read the response asynchronously from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public Task<SkyResult<Element>> USetAsync(String key, String value)
    {
        return _connection.USetAsync(key, value);
    }

    /// <summary>
    /// This function will create an USET <see cref="Query"/> and write it to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public SkyResult<Element> USet(String key, Skyhash value)
    {
        return _connection.USet(key, value);
    }

    /// <summary>
    /// This function will create an USET <see cref="Query"/> and write it to the stream and read the response asynchronously from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public Task<SkyResult<Element>> USetAsync(String key, Skyhash value)
    {
        return _connection.USetAsync(key, value);
    }

    /// <summary>
    /// This function will create a POP <see cref="Query"/> and write it to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public SkyResult<Element> Pop(String key)
    {
        return _connection.Pop(key);
    }

    /// <summary>
    /// This function will create a POP <see cref="Query"/> and write it to the stream and read the response asynchronously from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    public Task<SkyResult<Element>> PopAsync(String key)
    {
        return _connection.PopAsync(key);
    }

    /// <summary>
    /// This function will create a POP <see cref="Query"/> and write it to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and try to return type T if successful.
    /// </summary>
    public <T> SkyResult<T> Pop(String key)
    {
        return _connection.Pop(key);
    }

    /// <summary>
    /// This function will create a POP <see cref="Query"/> and write it to the stream and read the response asynchronously from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and try to return type T if successful.
    /// </summary>
    public <T> Task<SkyResult<T>> PopAsync(String key)
    {
        return  _connection.PopAsync(key);
    }
}
