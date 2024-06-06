package tech.novoess.skytable4j;

import tech.novoess.skytable4j.querying.Element;
import tech.novoess.skytable4j.querying.ElementType;

/// <summary>
    /// A type inheriting from Skyhash can be used in queries.
    /// </summary>
    public abstract class Skyhash
    {
        /// <summary>
        /// This function will by default try to deserialize a <see cref="Element"/> into type T using the <see cref="JsonSerializer"/>.
        /// </summary>
        public <T> T From(Element element)
        {
            switch(element.Type)
            {
                case ElementType.String:
                    return JsonSerializer.Deserialize(element.Object);
                case ElementType.BinaryString:
                    var bytes = (element.Object as List<Byte>).ToArray();
                    return JsonSerializer.Deserialize(bytes);
                default:
                    return new T();
            }
        }

        /// <summary>
        /// This function will by default try to serialize the object into JSON using the <see cref="JsonSerializer"/>.
        /// </summary>
        public String Into()
        {
            return JsonSerializer.Serialize(this, this.GetType());
        }
    }