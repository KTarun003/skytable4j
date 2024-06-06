package tech.novoess.skytable4j.querying;

/**
 * <summary>This enum represents the array types supported by the Skyhash Protocol.</summary>
 */
public enum ArrayType
{
    /**
     * <summary>A binary array; `typed array &lt;tsymbol&gt;` is `?`, `@` base &lt;tsymbol&gt;.</summary>
      */
    BinaryString,
    /**
     * <summary>A unicode string array; `typed array &lt;tsymbol&gt;` is `+`, `@` base &lt;tsymbol&gt;.</summary>
     */
    String,
    /**
     * <summary>A non-recursive flat array; `&lt;tsymbol&gt;` is `_`.</summary>
     */
    Flat,
    /**
     * <summary>A recursive array; `&lt;tsymbol&gt;` is `&amp;`.</summary>
     */
    Recursive
}