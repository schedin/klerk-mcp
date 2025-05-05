package dev.klerkframework.mcp

import dev.klerkframework.klerk.Klerk
import dev.klerkframework.klerk.KlerkContext
import dev.klerkframework.klerk.misc.PropertyType
import dev.klerkframework.klerk.misc.extractNameFromFunction
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.putJsonObject
import io.modelcontextprotocol.kotlin.sdk.Tool
import org.slf4j.LoggerFactory

//fun configureMcpServer(): Routing.() -> Unit = {
//    mcp {
//        getMcpServer()
//    }
//}

private val logger = LoggerFactory.getLogger("dev.klerkframework.mcp.KlerkMcpMain")

fun <C : KlerkContext, V> createMcpServer(klerk: Klerk<C, V>): Server {
    logger.info("Creating MCP server")

    val server = Server(
        serverInfo = Implementation(
            name = "example-sse-server",
            version = "1.0.0"
        ),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                prompts = ServerCapabilities.Prompts(listChanged = null),
                resources = ServerCapabilities.Resources(subscribe = null, listChanged = null),
                tools = ServerCapabilities.Tools(listChanged = null),
            )
        )
    )

    for (model in klerk.config.managedModels) {
//        println(model)
        val stateMachine = model.stateMachine

        stateMachine.getExternalEvents().forEach { eventReference ->
            if (eventReference.eventName != "CreateTodo") {
                return@forEach
            }
            println("${model.kClass.simpleName}: ${eventReference.eventName}")

//            val event = klerk.config.getEvent(eventReference)
//            val inputSchema
//            val parameters = klerk.config.getParameters(eventReference)

            val inputSchema: Tool.Input = klerk.config.getParameters(eventReference)?.let { parameters ->
                val required = parameters.requiredParameters.map { it.name }
                    .takeUnless { it.isEmpty() }
                val properties = buildJsonObject {
                    parameters.all.forEach { eventParameter ->
                        putJsonObject(eventParameter.name) {
                            put("type", JsonPrimitive(propertyTypeToJsonType(eventParameter.type)))
                            put("description", JsonPrimitive("Value for the ${eventParameter.valueClass.simpleName}"))
                        }
                    }
                }
                logger.debug("Tool input properties: {}", properties)
                Tool.Input(properties, required)
            } ?: Tool.Input()

            server.addTool(
                name = toToolName(eventReference.eventName, model.kClass.simpleName!!),
                description = "Executes the ${eventReference.eventName} command on the data ${model.kClass.simpleName}",
                inputSchema = inputSchema,
            ) { request ->
                val event = klerk.config.getEvent(eventReference)
                print(event)
                print(stateMachine)


                println("Request = $request")
                CallToolResult(
                    content = listOf(TextContent("Hello, world!"))
                )
            }
       }
        break
    }

    return server
}

fun propertyTypeToJsonType(propertyType: PropertyType?): String {
    return when (propertyType) {
        PropertyType.String ->  "string"
        PropertyType.Int ->     "number"
        PropertyType.Long ->    "number"
        PropertyType.Float ->   "number"
        PropertyType.Boolean -> "boolean"
        PropertyType.Ref ->     throw IllegalArgumentException("PropertyType.Ref not yet implemented")
        PropertyType.Enum ->    throw IllegalArgumentException("PropertyType.Enum not yet implemented")
        null -> throw IllegalArgumentException("PropertyType was null!?")
    }
}

fun toToolName(eventName: String, modelName: String): String {
     fun toSnakeCase(camelCase: String): String {
        return camelCase.replace(Regex("([a-z])([A-Z])"), "$1_$2")
            .replace(Regex("([A-Z])([A-Z][a-z])"), "$1_$2")
            .lowercase()
    }
    return "${toSnakeCase(modelName)}_${toSnakeCase(eventName)}"
}
