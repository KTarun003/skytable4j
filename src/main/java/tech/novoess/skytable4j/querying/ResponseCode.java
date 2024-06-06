package tech.novoess.skytable4j.querying;

/// <summary>A Skytable query response code.</summary>
public class ResponseCode
{
    /// <summary>The actual response code.</summary>
    public RespCode Code;

    /// <summary>A message from the server if the RespCode was <see cref="RespCode.OtherError" />.</summary>
    public String Error;

    private ResponseCode(RespCode code)
    {
        Code = code;
    }

    private ResponseCode(String error)
    {
        Code = RespCode.OtherError;
        Error = error;
    }

    static ResponseCode From(String code)
    {
        return new ResponseCode(Enum.valueOf(RespCode.class, code));
    }

    @Override
    public String toString() {
        if(Error == null)
            return Code.toString();
        return Code.toString() + "(" + Error + ")";
    }
}
