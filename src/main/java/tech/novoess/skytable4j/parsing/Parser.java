package tech.novoess.skytable4j.parsing;

import javafx.util.Pair;
import tech.novoess.skytable4j.SkyResult;
import tech.novoess.skytable4j.querying.Element;

import java.util.List;

import static com.sun.org.apache.xml.internal.serialize.OutputFormat.Defaults.Encoding;
import static java.lang.System.in;
import static jdk.vm.ci.meta.JavaKind.Char;

/**
 * <summary>A parser that can parse the packets sent by Skytable.</summary>
 */
public class Parser {
    // SKYHASH protocol constants
    private final byte SKYHASH_HEADER       = 42;  // *
    private final byte SKYHASH_LINEFEED     = 10;  // \n
    private final byte SKYHASH_STRING       = 43;  // +
    private final byte SKYHASH_U64          = 58;  // :
    private final byte SKYHASH_ARRAY        = 38;  // &
    private final byte SKYHASH_RESPCODE     = 33;  // !
    private final byte SKYHASH_FLATARRAY    = 95;  // _
    private final byte SKYHASH_BINARYSTRING = 63;  // ?
    private final byte SKYHASH_TYPEDARRAY   = 64;  // @

    private int _cursor;
    private final List<Byte> _buffer;

    /**
     * <summary>Create a parser that can parse the packets sent by Skytable.</summary>
     */
    public Parser(List<Byte> buffer)
    {
        _cursor = 0;
        _buffer = buffer;
    }

    /**
     * <summary>Parse the response. Returns a tuple containing a <see cref="SkyResult&lt;Response&gt;"/> and an integer that contains the cursor's position in the buffer.</summary>
     */
    public Pair<SkyResult<Element>, Integer> Parse() throws ClassNotFoundException {
        var numberOfQueries = ParseMetaframeGetDatagroupCount();
        if (numberOfQueries.IsError)
            return new Pair<>(SkyResult.Err(numberOfQueries.Error), _cursor);
        if (numberOfQueries.Item == 0)
            return new Pair<>(SkyResult.Err(ParseError.BadPacket), _cursor);

        if (numberOfQueries.Item == 1)
        {
            var result = ParseNextElement();
            if (result.IsError)
                return new Pair<>(SkyResult.Err(result.Error), 0);

            // No need to check result since we pass in true here. Cannot be an Error.
            if (WillCursorGiveChar((char)SKYHASH_HEADER, true).Item)
            {
                return new Pair<>(result, _cursor);
            }

            return new Pair<>(SkyResult.Err(ParseError.UnexpectedByte), _cursor);
        }

        throw new ClassNotFoundException("Pipelined queries are not supported yet.");
    }

        private SkyResult<Element> ParseNextElement()
        {
            if (_buffer.size() < _cursor)
                return SkyResult.Err(ParseError.NotEnough);

            var tsymbol = _buffer.get(_cursor++);
            switch (tsymbol)
            {
                case SKYHASH_STRING:
                    var str = ParseNextString();
                    if (str.IsError)
                        return SkyResult.Err(str.Error);
                    return SkyResult.Ok(new Element(str.Item));
                case SKYHASH_U64:
                    var u64 = ParseNextU64();
                    if (u64.IsError)
                        return SkyResult.Err(u64.Error);
                    return SkyResult.Ok(new Element(u64.Item));
                case SKYHASH_ARRAY:
                    var array = ParseNextArray(); //TODO: Recursive
                    if (array.IsError)
                        return SkyResult<Element>.Err(array.Error);
                    return SkyResult<Element>.Ok(new Element(array.Item));
                case SKYHASH_RESPCODE:
                    var respCode = ParseNextRespCode();
                    if (respCode.IsError)
                        return SkyResult<Element>.Err(respCode.Error);
                    return SkyResult<Element>.Ok(new Element(respCode.Item));
                case SKYHASH_FLATARRAY:
                    var flatArray = ParseNextFlatArray();
                    if (flatArray.IsError)
                        return SkyResult<Element>.Err(flatArray.Error);
                    return SkyResult<Element>.Ok(new Element(flatArray.Item));
                case SKYHASH_BINARYSTRING:
                    var binaryString = ParseNextBinaryString();
                    if (binaryString.IsError)
                        return SkyResult<Element>.Err(binaryString.Error);
                    return SkyResult<Element>.Ok(new Element(binaryString.Item));
                case SKYHASH_TYPEDARRAY:
                    // hmmm, a typed array; let's check the tsymbol
                    if (_buffer.Count < _cursor)
                        return SkyResult<Element>.Err(ParseError.NotEnough);

                    // got tsymbol, let's skip it too
                    var typed_tsymbol = _buffer[_cursor++];
                    switch(typed_tsymbol)
                    {
                        case SKYHASH_STRING:
                            var typedArrayStr = ParseNextTypedArrayStr();
                            if (typedArrayStr.IsError)
                                return SkyResult<Element>.Err(typedArrayStr.Error);
                            return SkyResult<Element>.Ok(new Element(typedArrayStr.Item));
                        case SKYHASH_BINARYSTRING:
                            var typedArrayBin = ParseNextTypedArrayBin();
                            if (typedArrayBin.IsError)
                                return SkyResult<Element>.Err(typedArrayBin.Error);
                            return SkyResult<Element>.Ok(new Element(typedArrayBin.Item));
                        default:
                            return SkyResult<Element>.Err(ParseError.UnknownDataType);
                    }
                default:
                    throw new NotImplementedException($"The tsymbol '{tsymbol}' is not yet implemented.");
            }
        }

        private SkyResult<List<Element>> ParseNextArray()
        {
            var (startedAt, stoppedAt) = ReadLine();
            var line = _buffer.GetRange(startedAt, stoppedAt - startedAt);
            if (line.Count == 0)
                return SkyResult<List<Element>>.Err(ParseError.NotEnough);

            var size = ParseSize(line);
            if (size.IsError)
                return SkyResult<List<Element>>.Err(size.Error);

            var elements = new List<Element>(size.Item);
            for (int i = 0; i < size.Item; i++)
            {
                var result = ParseNextElement();
                if (result.IsError)
                    return SkyResult<List<Element>>.Err(result.Error);

                elements.Add(result.Item);
            }
            return SkyResult<List<Element>>.Ok(elements);
        }

        private SkyResult<List<string>> ParseNextFlatArray()
        {
            var (startedAt, stoppedAt) = ReadLine();
            var line = _buffer.GetRange(startedAt, stoppedAt - startedAt);
            if (line.Count == 0)
                return SkyResult<List<string>>.Err(ParseError.NotEnough);

            var size = ParseSize(line);
            if (size.IsError)
                return SkyResult<List<string>>.Err(size.Error);

            var elements = new List<string>(size.Item);
            for (int i = 0; i < size.Item; i++)
            {
                if (_buffer.Count < _cursor)
                    return SkyResult<List<string>>.Err(ParseError.NotEnough);

                // TODO: +, ?, !, : should be supported here. Not just +.
                var tsymbol = _buffer[_cursor++];
                if (tsymbol != SKYHASH_STRING)
                    return SkyResult<List<string>>.Err(ParseError.UnknownDataType);

                var result = ParseNextString();
                if (result.IsError)
                    return SkyResult<List<string>>.Err(result.Error);

                elements.Add(result.Item);
            }

            return SkyResult<List<string>>.Ok(elements);
        }

        private SkyResult<List<string>> ParseNextTypedArrayStr()
        {
            var (startedAt, stoppedAt) = ReadLine();
            var line = _buffer.GetRange(startedAt, stoppedAt - startedAt);
            if (line.Count == 0)
                return SkyResult<List<string>>.Err(ParseError.NotEnough);

            // so we have a size chunk; let's get the size
            var size = ParseSize(line);
            if (size.IsError)
                return SkyResult<List<string>>.Err(size.Error);

            var elements = new List<string>(size.Item);
            for (int i = 0; i < size.Item; i++)
            {
                // no tsymbol, just elements and their sizes
                var result = ParseNextStringNullcheck();
                if (result.IsError)
                    return SkyResult<List<string>>.Err(result.Error);
                elements.Add(result.Item);
            }
            return SkyResult<List<string>>.Ok(elements);
        }

        private SkyResult<List<List<byte>>> ParseNextTypedArrayBin()
        {
            var (startedAt, stoppedAt) = ReadLine();
            var line = _buffer.GetRange(startedAt, stoppedAt - startedAt);
            if (line.Count == 0)
                return SkyResult<List<List<byte>>>.Err(ParseError.NotEnough);

            // so we have a size chunk; let's get the size
            var size = ParseSize(line);
            if (size.IsError)
                return SkyResult<List<List<byte>>>.Err(size.Error);

            var elements = new List<List<byte>>(size.Item);
            for (int i = 0; i < size.Item; i++)
            {
                // no tsymbol, just elements and their sizes
                var result = ParseNextBinaryStringNullcheck();
                if (result.IsError)
                    return SkyResult<List<List<byte>>>.Err(result.Error);
                elements.Add(result.Item);
            }
            return SkyResult<List<List<byte>>>.Ok(elements);
        }

        private SkyResult<Long> ParseNextU64()
        {
            var ourU64Chunk = GetNextElement();
            if (ourU64Chunk.IsError)
                return SkyResult.Err(ourU64Chunk.Error);

            var ourU64 = ParseU64(ourU64Chunk.Item);
            if (ourU64.IsError)
                return ourU64;

            var result = WillCursorGiveLineFeed();
            if (result.IsOk && result.Item)
            {
                _cursor++;
                return SkyResult<ulong>.Ok(ourU64.Item);
            }

            return SkyResult<ulong>.Err(ParseError.UnexpectedByte);
        }

        private SkyResult<List<byte>> ReadUntil(int until)
        {
            if (_buffer.Count < _cursor + until)
                return SkyResult<List<byte>>.Err(ParseError.NotEnough);

            var range = _buffer.GetRange(_cursor, until);
            _cursor += until;
            return SkyResult<List<byte>>.Ok(range);
        }

        private SkyResult<List<byte>> GetNextElement()
        {
            var (startedAt, stoppedAt) = ReadLine();
            var line = _buffer.GetRange(startedAt, stoppedAt - startedAt);
            var size = ParseSize(line);
            if (size.IsError)
                return SkyResult.Err(size.Error, new Byte[]);
            return ReadUntil(size.Item);
        }

        private SkyResult<Boolean> WillCursorGiveChar(char c, boolean thisIfNothingAhead)
        {
            if (_buffer.Count <= _cursor)
            {
                if (thisIfNothingAhead)
                    return SkyResult.Ok(true);
                return SkyResult.Err(ParseError.NotEnough, Boolean.FALSE);
            }

            return SkyResult.Ok(_buffer.get(_cursor) == c);
        }

        private SkyResult<Boolean> WillCursorGiveLineFeed()
        {
            return WillCursorGiveChar((char)SKYHASH_LINEFEED, false);
        }

        private SkyResult<String> ParseNextString()
        {
            var ourStringChunk = GetNextElement();
            if (ourStringChunk.IsError)
                return SkyResult.Err(ourStringChunk.Error);

            var ourString = ourStringChunk.Item;
            var result = WillCursorGiveLineFeed();
            if (result.IsOk && result.Item)
            {
                _cursor++;
                return SkyResult.Ok(ourString);
            }

            return SkyResult.Err(ParseError.UnexpectedByte);
        }

        private SkyResult<List<byte>> ParseNextBinaryString()
        {
            var ourStringChunk = GetNextElement();
            if (ourStringChunk.IsError)
                return ourStringChunk;

            var result = WillCursorGiveLineFeed();
            if (result.IsOk && result.Item)
            {
                // there is a lf after the end of the binary string; great!
                // let's skip that now
                _cursor++;
                // let's return our string
                return SkyResult<List<byte>>.Ok(ourStringChunk.Item);
            }

            return SkyResult<List<byte>>.Err(ParseError.UnexpectedByte);
        }

        /// Parse the next null checked element
        private SkyResult<List<byte>> ParseNextChunkNullcheck()
        {
            var (startedAt, stoppedAt) = ReadLine();
            var sizeLine = _buffer.GetRange(startedAt, stoppedAt - startedAt);
            if (sizeLine.Count == 0)
                return SkyResult<List<byte>>.Err(ParseError.NotEnough);

            var result = ParseSize(sizeLine);
            if (result.IsError)
                return SkyResult<List<byte>>.Err(result.Error);
            return ReadUntil(result.Item);
        }

        private SkyResult<List<byte>> ParseNextBinaryStringNullcheck()
        {
            var ourChunk = ParseNextChunkNullcheck();
            if (ourChunk.IsError)
                return ourChunk;

            var result = WillCursorGiveLineFeed();
            if (result.IsOk && result.Item)
            {
                _cursor++;
                return SkyResult<List<byte>>.Ok(ourChunk.Item);
            }
            return SkyResult<List<byte>>.Err(ParseError.UnexpectedByte);
        }

        private SkyResult<string> ParseNextStringNullcheck()
        {
            var ourChunk = ParseNextBinaryStringNullcheck();
            if (ourChunk.IsError)
                return SkyResult<string>.Err(ourChunk.Error);

            return SkyResult<string>.Ok(Encoding.UTF8.GetString(ourChunk.Item.ToArray()));
        }

        private SkyResult<ResponseCode> ParseNextRespCode()
        {
            var ourRespcodeChunk = GetNextElement();
            if (ourRespcodeChunk.IsError)
                return SkyResult<ResponseCode>.Err(ourRespcodeChunk.Error);

            var ourRespCode = Encoding.UTF8.GetString(ourRespcodeChunk.Item.ToArray());
            var result = WillCursorGiveLineFeed();
            if (result.IsOk && result.Item)
            {
                _cursor++;
                return SkyResult<ResponseCode>.Ok(ResponseCode.From(ourRespCode));
            }

            return SkyResult<ResponseCode>.Err(ParseError.UnexpectedByte);
        }

        /// This will return the number of datagroups present in this query packet
        ///
        /// This **will forward the cursor itself**
        private SkyResult<int> ParseMetaframeGetDatagroupCount()
        {
            // The smallest query we can have is: `*1\n` or 3 chars
            if (_buffer.Count < 3)
                return SkyResult<int>.Err(ParseError.NotEnough);

            // Now we want to read `*<n>\n`
            var (startedAt, stoppedAt) = ReadLine();
            var ourChunk = _buffer.GetRange(startedAt, stoppedAt - startedAt);

            if (ourChunk[0] == SKYHASH_HEADER)
                return ParseSize(ourChunk.GetRange(1, ourChunk.Count - 1));

            return SkyResult<int>.Err(ParseError.UnexpectedByte);
        }

        private SkyResult<int> ParseSize(List<byte> bytes)
        {
            if (bytes.Count == 0)
                return SkyResult<int>.Err(ParseError.NotEnough);

            int itemSize = 0;
            foreach (byte digit in bytes)
            {
                var c = (char)digit;
                if (!char.IsDigit(c))
                    return SkyResult<int>.Err(ParseError.DataTypeParseError);

                // 48 is the ASCII code for 0, and 57 is the ascii code for 9
                // so if 0 is given, the subtraction should give 0; similarly
                // if 9 is given, the subtraction should give us 9!
                try
                {
                    checked
                    {
                        byte curDig = (byte)(digit - 48);
                        var product = itemSize * 10;
                        itemSize = product + curDig;
                    }
                }
                catch(OverflowException)
                {
                    return SkyResult<int>.Err(ParseError.DataTypeParseError);
                }
            }

            return SkyResult<int>.Ok(itemSize);
        }

        private SkyResult<Long> ParseU64(List<Byte> bytes)
        {
            if (bytes.isEmpty())
                return SkyResult.Err(ParseError.NotEnough);

            long itemU64 = 0;
            for (Byte digit : bytes)
            {
                var temp = digit.toString();
                Char c = Char.();
                if (!Char.isNumericInger())
                    return SkyResult.Err(ParseError.DataTypeParseError);

                // 48 is the ASCII code for 0, and 57 is the ascii code for 9
                // so if 0 is given, the subtraction should give 0; similarly
                // if 9 is given, the subtraction should give us 9!
                try
                {
                    checked
                    {
                        byte curDig = (byte)(digit - 48);
                        var product = itemU64 * 10;
                        itemU64 = product + curDig;
                    }
                }
                catch(OverflowException)
                {
                    return SkyResult<ulong>.Err(ParseError.DataTypeParseError);
                }
            }

            return SkyResult<ulong>.Ok(itemU64);
        }

    private (int, int) ReadLine()
    {
        var startedAt = _cursor;
        var stoppedAt = _cursor;

        while (_cursor < _buffer.Count)
        {
            if (_buffer[_cursor] == SKYHASH_LINEFEED)
            {
                _cursor++;
                break;
            }

            _cursor++;
            stoppedAt += 1;
        }

        return (startedAt, stoppedAt);
    }
}
