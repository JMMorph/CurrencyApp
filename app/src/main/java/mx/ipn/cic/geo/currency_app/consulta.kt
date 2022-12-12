package mx.ipn.cic.geo.currency_app


data class consulta(val hora: String = "", val cantidad: String = "") {
    // Null default values create a no-argument default constructor, which is needed
    // for deserialization from a DataSnapshot.
}
