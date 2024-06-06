package tech.novoess.skytable4j.querying;

/**
 * <summary>This enum represents the data types supported by the Skyhash Protocol.</summary>
 */
public enum ElementType
{
    /**
     * <summary>A unicode string value; `&lt;tsymbol&gt;` is `+`.</summary>
     */
    String,
    /**
     * <summary>An unsigned integer value; `&lt;tsymbol&gt;` is `:`.</summary>
     */
    UnsignedInt,
    /**
     * <summary>Arrays can be nested! Their `&lt;tsymbol&gt;` is `&amp;`.</summary>
     */
    Array,
    /**
     * <summary>A response code.</summary>
     */
    RespCode,
    /**
     * <summary>A non-recursive String array; `&lt;tsymbol&gt;` is `_`.</summary>
     */
    FlatArray,
    /**
     * <summary>A binary string value; `&lt;tsymbol&gt;` is `?`.</summary>
     */
    BinaryString
}