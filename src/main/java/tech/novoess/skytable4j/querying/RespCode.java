package tech.novoess.skytable4j.querying;
    /// <summary>Response codes returned by the server.</summary>
    public enum RespCode
    {
        /// <summary>Returned when a query is successful but contains no response data.</summary>
        Okay(0),
        /// <summary>Returned when an element is not found using the specified key.</summary>
        NotFound(1),
        /// <summary>Returned when trying to SET a key that already exists. Use UPDATE instead.</summary>
        OverwriteError(2),
        /// <summary>The action did not expect the arguments sent.</summary>
        ActionError( 3),
        /// <summary>The packet contains invalid data.</summary>
        PacketError(4),
        /// <summary>An error occurred on the server side.</summary>
        ServerError(5),
        /// <summary>Some other error occurred and the server returned a description of this error. See this <see href="https://docs.skytable.io/protocol/errors">document</see>.</summary>
        OtherError(6),
        /// <summary>WrongType Error.</summary>
        WrongType(7);

        public final int value;

        private RespCode(int value) {
            this.value = value;
        }
    }