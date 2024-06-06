package tech.novoess.skytable4j.querying;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

/**
 * <summary>This type represents a single simple query as defined by the Skyhash protocol.</summary>
 */
public class Query implements IQueryWriter
{
    private short _sizeCount;
    private byte[] _holdingBuffer;

    /// <summary>Returns the argument count of the query.</summary>
    public short getArgumentCount() {
        return _sizeCount;
    }

    /// <summary>Creates an empty query with a no arguments.</summary>
    public Query()
    {
        _sizeCount = 0;
    }

    /// <summary>Pushes an argument into the query.</summary>
    public void Push(String argument)
    {
        var unicode_argument = argument.getBytes(StandardCharsets.UTF_8);
        var header = unicode_argument.length +"\n";
        var unicode_header = header.getBytes(StandardCharsets.UTF_8);
        _holdingBuffer = new byte[unicode_header.length+unicode_argument.length+10];
        _holdingBuffer.AddRange(unicode_header);
        _holdingBuffer.AddRange(unicode_argument);
        _holdingBuffer.Add(10);
        _sizeCount++;
    }

    /// <summary>Writes the query to the specified stream.</summary>
    public void WriteTo(Stream stream)
    {
        // TODO: Write everything at once?
        var header = System.Text.Encoding.UTF8.GetBytes("*1\n");
        stream.Write(header, 0, header.Length);
        var numberOfItemsInDatagroup = System.Text.Encoding.UTF8.GetBytes($"~{_sizeCount}\n");
        stream.Write(numberOfItemsInDatagroup, 0, numberOfItemsInDatagroup.Length);
        stream.Write(_holdingBuffer.ToArray(), 0, _holdingBuffer.Count);
    }

    /// <summary>Writes the query to the specified list.</summary>
    public void WriteTo(byte[] list)
    {
        var numberOfItemsInDatagroup = System.Text.Encoding.UTF8.GetBytes($"~{_sizeCount}\n");
        list.AddRange(numberOfItemsInDatagroup);
        list.AddRange(_holdingBuffer);
    }

    /// <summary>Writes the query to the specified stream asynchronously.</summary>
    public async Task WriteToAsync(Stream stream)
    {
        // TODO: Write everything at once?
        var header = System.Text.Encoding.UTF8.GetBytes("*1\n");
        await stream.WriteAsync(header, 0, header.Length);
        var numberOfItemsInDatagroup = System.Text.Encoding.UTF8.GetBytes($"~{_sizeCount}\n");
        await stream.WriteAsync(numberOfItemsInDatagroup, 0, numberOfItemsInDatagroup.Length);
        await stream.WriteAsync(_holdingBuffer.ToArray(), 0, _holdingBuffer.Count);
    }
}
