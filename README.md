# skytable4j <img align="right" src="assets/logo.jpg" height="128" width="128" alt="Skytable Logo"/>
Java client for Skytable.<br/>

## Usage/Examples

```java
class Person extends Skyhash
{
    public string Name;
    public int Age;
}

public static class Main
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
        var getResult = connection.Get<Person>("P");
    }
}
```


## Installation

Install skytable4j with Maven

```xml
<dependency>
    <groupId>tech.novoess</groupId>
    <artifactId>skytable4j</artifactId>
    <version>0.8.3</version>
</dependency>
```

## Roadmap

- Additional browser support

- Add more integrations


## FAQ

#### Question 1

Answer 1

#### Question 2

Answer 2


## Documentation

[Documentation](https://linktodocumentation)


## License

[MIT](LICENSE)

