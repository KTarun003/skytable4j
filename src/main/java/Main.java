import tech.novoess.skytable4j.*;

class Person extends Skyhash
{
    public String Name;
    public int Age;
}

public class Main
{
    public static void main(String[] args)
    {
        var setPerson = new Person();
        setPerson.Name = "John Doe";
        setPerson.Age = 30;

        var connection = new Connection("127.0.0.1", 2003);
        connection.Connect();

        // Serializes and stores the Person as a JSON string in Key `P`.
        // Result is a SkyResult<Element> containing a ResponseCode which indicates the result of the action.
        var setResult = connection.Set("P", setPerson);

        // Contains a SkyResult<Person> deserialized from the JSON string retrieved with the Key `P`.
        var getResult = connection.Get("P");
    }
}
