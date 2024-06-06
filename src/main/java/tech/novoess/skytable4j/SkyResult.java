package tech.novoess.skytable4j;

import tech.novoess.skytable4j.parsing.ParseError;

/**
 * <summary>The result of a Skytable Query.</summary>
 */
public class SkyResult<T>
{
    /**
     * <summary>The item of type {@link T} if the result is Ok.</summary>
     */
    public T Item;
    /**
     * <summary>The error in case the result is an Error.</summary>
     */
    public ParseError Error;
    /**
     * <summary>True if the result was ok. You can use the Item.</summary>
     */
    public boolean IsOk;
    /**
     * <summary>True if the query failed in any way. Check the Error to learn more.</summary>
     */
    public boolean IsError;

    private SkyResult(T item)
    {
        Item = item;
        IsOk = true;
        IsError = false;
    }

    private SkyResult(ParseError error)
    {
        Error = error;
        IsOk = false;
        IsError = true;
    }

    public static <U> SkyResult<U> Ok(U item)
    {
        return new SkyResult<>(item);
    }

    public static <U> SkyResult<U> Err(ParseError error, U item)
    {
        return new SkyResult<>(error);
    }
}
