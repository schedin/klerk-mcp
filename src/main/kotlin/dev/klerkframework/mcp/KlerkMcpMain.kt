package dev.klerkframework.mcp

import dev.klerkframework.klerk.*
import dev.klerkframework.klerk.CommandResult.Failure
import dev.klerkframework.klerk.CommandResult.Success
import dev.klerkframework.klerk.command.Command
import dev.klerkframework.klerk.command.CommandToken
import dev.klerkframework.klerk.command.ProcessingOptions
import dev.klerkframework.klerk.misc.PropertyType
import dev.klerkframework.klerk.statemachine.StateMachine
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions

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

fun <C : KlerkContext, V> createMcpServer(
    klerk: Klerk<C, V>,
    contextProvider: suspend () -> C,
): Server {
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
                handleToolRequest(stateMachine, klerk, klerk.config.getEvent(eventReference), contextProvider, request)
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

private suspend fun <T : Any, ModelStates : Enum<*>, C : KlerkContext, V> handleToolRequest(
    stateMachine: StateMachine<T, ModelStates, C, V>,
    klerk: Klerk<C, V>,
    event: Event<Any, Any?>,
    contextProvider: suspend () -> C,
    request: CallToolRequest,
): CallToolResult {
    logger.debug("Handling tool request for event: {}", event)
    logger.debug("State machine: {}", stateMachine)

    val parametersClass = when(event) {
        is VoidEventWithParameters -> event.parametersClass
        is InstanceEventWithParameters -> event.parametersClass
        else -> null
    }

    if (parametersClass != null) {
        logger.debug("Parameters class: {}", parametersClass)

        try {
            // Get the constructor of the parameters class
            val constructor = parametersClass.constructors.firstOrNull()
                ?: throw IllegalStateException("No constructor found for $parametersClass")

            // Get constructor parameters
            val constructorParams = constructor.parameters

            // Create a map to hold the parameter values we'll pass to the constructor
            val paramValues = mutableMapOf<kotlin.reflect.KParameter, Any>()

            // Process each constructor parameter
            for (param in constructorParams) {
                val paramName = param.name ?: continue
                val paramType = param.type.classifier as? kotlin.reflect.KClass<*> ?: continue
                val requestParamValue = request.arguments[paramName]
                    ?: throw IllegalArgumentException("Missing parameter for tool call ${request}: $paramName")

                if (requestParamValue !is JsonPrimitive) {
                    throw IllegalArgumentException("Unknown JSON class ${requestParamValue.javaClass.simpleName}")
                }

                // Find the constructor of the parameter type (which should be a DataContainer subclass)
                val containerConstructor = paramType.constructors.firstOrNull()
                    ?: throw IllegalStateException("No constructor found for parameter type $paramType")

                // Get the first parameter of the constructor to determine what type it expects
                val constructorFirstParam = containerConstructor.parameters.firstOrNull()
                    ?: throw IllegalStateException("Constructor for $paramType has no parameters")

                // Create an instance of the DataContainer subclass with the value from the request
                val containerInstance = when (val parameterType = constructorFirstParam.type.classifier) {
                    String::class -> {
                        containerConstructor.call(requestParamValue.content)
                    }
                    Int::class -> {
                        val intValue = requestParamValue.content.toInt()
                        containerConstructor.call(intValue)
                    }
                    Boolean::class -> {
                        val boolValue = requestParamValue.content.toBoolean()
                        containerConstructor.call(boolValue)
                    }
                    else -> {
//                      logger.warn("Using fallback for parameter type: {}", parameterType)
                        throw IllegalArgumentException("Unsupported parameter type: $paramType with constructor parameter type: $parameterType")
                    }
                }
                paramValues[param] = containerInstance
            }

            // Create an instance of the parameters class with the constructed parameter values
            val paramsInstance = constructor.callBy(paramValues)

            // Create a context for the command
            val context = contextProvider()

            // Create and execute the command
            @Suppress("UNCHECKED_CAST")
            val command = Command(
                event = event as Event<T, Any?>,
                model = null,
                params = paramsInstance
            )

            // Handle the command
            when(val result = klerk.handle(command, context, ProcessingOptions(CommandToken.simple()))) {
                is Failure -> {
                    logger.error("Command execution failed: {}", result.problem)
                    return CallToolResult(
                        content = listOf(TextContent("Error: ${result.problem}"))
                    )
                }
                is Success -> {
                    logger.info("Command executed successfully")
                    val modelId = result.primaryModel
                    if (modelId != null) {
                        val model = klerk.read(context) { get(modelId) }
                        return CallToolResult(
                            content = listOf(TextContent("Successfully created: $model"))
                        )
                    } else {
                        return CallToolResult(
                            content = listOf(TextContent("Command executed successfully"))
                        )
                    }
                }
            }

        } catch (e: Exception) {
            logger.error("Error instantiating parameters class: {}", e.message, e)
            return CallToolResult(
                content = listOf(TextContent("Error: ${e.message}"))
            )
        }
    } else {
        logger.warn("No parameters class found for event: {}", event)
    }

    return CallToolResult(
        content = listOf(TextContent("Request processed but no action was taken"))
    )
}
