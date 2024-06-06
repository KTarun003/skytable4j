package tech.novoess.skytable4j.parsing;

/**
 * <summary>Errors that can be returned by the Parser.</summary>
 */
public enum ParseError {
    /**
     * <summary>NotEnough occurs when there are not enough bytes left to complete a so far well-structured packet.
     * */
    NotEnough,
    /**
     * <summary>UnexpectedByte occurs when a packet is not constructed correctly. Unable to continue parsing.</summary>
     */
    UnexpectedByte,
     /**
      * <summary>
        The packet simply contains invalid data.
        This is rarely returned and only in the special cases where a bad client sends `0` as
        the query count.
        </summary>
      */
    BadPacket,
     /**
      * <summary>A data type was given but the parser failed to serialize it into this type.</summary>
      */
    DataTypeParseError,
     /**
      * <summary>
        A data type that the client doesn't know was passed into the query.
        This is a frequent problem that can arise between different server editions as more data types
        can be added with changing server versions.
        </summary>
      */
    UnknownDataType,
     /**
      * <summary>The query is empty.</summary>
      */
    Empty
}
