package tech.novoess.skytable4j;

import javafx.concurrent.Task;
import tech.novoess.skytable4j.querying.*;

/**
 * <summary>A database connection interface for Skyhash.</summary>
 */
public interface IConnection
{
    /**
     * @param key The key of the value to be retrieved
     * @return {@link SkyResult<T>} The value encapsulated in SkyResult
     * @Description: This function will create a GET <see cref="Query"/> and write it to the stream and read the response from the
     * server. It will then determine if the returned response is complete, incomplete
     * or invalid and try to return type T if successful.
     */
    <T> SkyResult<T> Get(String key);

    /**
     * @param key The key of the value to be retrieved
     * @return {@link SkyResult<T>} The value encapsulated in SkyResult
     * @Description: This function will create a GET <see cref="Query"/> and write it to the stream and read the response from the
     * server. It will then determine if the returned response is complete, incomplete
     * or invalid and try to return type T if successful.
     */
    /// <summary>
    /// This function will create a SET <see cref="Query"/> and write it to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    <T> SkyResult<T> Set(String key, String value);

    /**
     * @param key The key of the value to be retrieved
     * @return {@link SkyResult<T>} The value encapsulated in SkyResult
     * @Description: This function will create a GET <see cref="Query"/> and write it to the stream and read the response from the
     * server. It will then determine if the returned response is complete, incomplete
     * or invalid and try to return type T if successful.
     */
    /// <summary>
    /// This function will create a SET <see cref="Query"/> and write it to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    <T> SkyResult<T> Set(String key, Skyhash value);

    /**
     * @param key The key of the value to be retrieved
     * @return {@link SkyResult<T>} The value encapsulated in SkyResult
     * @Description: This function will create a GET <see cref="Query"/> and write it to the stream and read the response from the
     * server. It will then determine if the returned response is complete, incomplete
     * or invalid and try to return type T if successful.
     */
    /// <summary>
    /// This function will create a DEL <see cref="Query"/> and write it to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    SkyResult<Element> Delete(String key);

    /**
     * @param key The key of the value to be retrieved
     * @return {@link SkyResult<T>} The value encapsulated in SkyResult
     * @Description: This function will create a GET <see cref="Query"/> and write it to the stream and read the response from the
     * server. It will then determine if the returned response is complete, incomplete
     * or invalid and try to return type T if successful.
     */
    /// <summary>
    /// This function will create an USET <see cref="Query"/> and write it to the stream and read the response from the
    /// server. It will then determine if the returned response is complete, incomplete
    /// or invalid and return an appropriate variant of <see cref="Element"/>.
    /// </summary>
    SkyResult<Element> USet(String key, String value);

    /**
     * @param key The key of the value to be retrieved
     * @return {@link SkyResult<T>} The value encapsulated in SkyResult
     * @Description: This function will create an USET {@link Query} and write it to the stream and read the response from the
     * server. It will then determine if the returned response is complete, incomplete
     * or invalid and return an appropriate variant of {@link Element}.
     */
    SkyResult<Element> USet(String key, Skyhash value);

    /**
     * @param key The key of the value to be retrieved
     * @return {@link SkyResult<T>} The value encapsulated in SkyResult
     * @Description: This function will create a POP {@link Query} and write it to the stream and read the response from the
     * server. It will then determine if the returned response is complete, incomplete
     * or invalid and try to return type {@link T} if successful.
     */
    <T> SkyResult<T> Pop(String key);
}
