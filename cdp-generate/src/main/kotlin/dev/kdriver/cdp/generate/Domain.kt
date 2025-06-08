package dev.kdriver.cdp.generate

class Domain(
    val domain: String,
    val description: String?,
    val dependencies: List<String>,
    val types: List<Type>,
    val commands: List<Command>,
    val events: List<Event>,
) {

    class Type(
        val id: String,
        override val type: String,
        val enum: List<String>,
        val description: String?,
        val properties: List<Property>,
    ) : CanBeTypeAlias {
        class Property(
            val name: String,
            override val type: String?,
            val description: String?,
            override val ref: String?,
            override val optional: Boolean,
            override val items: Map<String, String>,
        ) : TypeOrReference
    }

    class Command(
        val name: String,
        val description: String?,
        val handlers: List<String>,
        val parameters: List<Parameter>,
        val returns: List<Return>,
        val redirect: String?,
        val deprecated: Boolean,
    ) {
        class Parameter(
            val name: String,
            override val type: String?,
            override val ref: String?,
            override val items: Map<String, String>,
            override val optional: Boolean,
            val description: String?,
        ) : TypeOrReference

        class Return(
            val name: String,
            override val ref: String?,
            override val type: String?,
            override val items: Map<String, String>,
            override val optional: Boolean,
            val experimental: Boolean?,
            val description: String?,
        ) : TypeOrReference
    }

    class Event(
        val name: String,
        val parameters: List<Parameter>,
        val description: String?,
        val handlers: List<String>,
    ) {
        class Parameter(
            val name: String,
            override val type: String?,
            override val ref: String?,
            override val items: Map<String, String>,
            override val optional: Boolean,
            val description: String?,
        ) : TypeOrReference
    }

    interface CanBeTypeAlias {
        val type: String?
    }

    interface TypeOrReference : CanBeTypeAlias {
        val items: Map<String, String>
        val ref: String?
        val optional: Boolean
    }

}
