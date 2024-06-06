package tech.novoess.skytable4j.querying;


import java.util.List;

/// <summary>Represents the data types supported by the Skyhash protocol.</summary>
public class Element
{
    /// <summary>Gets the object that this element represents. It is of type <see cref="ElementType"/>.</summary>
    public Object Object;

    /// <summary>Gets the <see cref="ElementType"/> of the Object that this element represents.</summary>
    public ElementType Type;

    Element(String s)
    {
        Object = s;
        Type = ElementType.String;
    }

    Element(long n)
    {
        Object = n;
        Type = ElementType.UnsignedInt;
    }

    Element(ResponseCode r)
    {
        Object = r;
        Type = ElementType.RespCode;
    }

    Element(List<Element> elements)
    {
        Object = elements;
        Type = ElementType.Array;
    }

    Element(List<String> strings)
    {
        Object = strings;
        Type = ElementType.FlatArray;
    }

    Element(List<Byte> binaryString)
    {
        Object = binaryString;
        Type = ElementType.BinaryString;
    }

    Element(List<List<Byte>> binaryStrings)
    {
        Object = binaryStrings;
        Type = ElementType.FlatArray;
    }

    @Override
    public String toString() {
        return "Element{" +
                "Object=" + Object +
                ", Type=" + Type +
                '}';
    }
}