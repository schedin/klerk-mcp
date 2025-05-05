package dev.klerkframework.mcp

import dev.klerkframework.klerk.Klerk
import dev.klerkframework.klerk.KlerkContext
import dev.klerkframework.klerk.misc.extractNameFromFunction
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import kotlinx.serialization.json.JsonObject
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
            val event = klerk.config.getEvent(eventReference)
            val parameters = klerk.config.getParameters(eventReference)
            if (parameters != null) {
                val properties: JsonObject
                val required = klerk.config.getParameters(eventReference)?.requiredParameters?.map { it.name }
                    .takeUnless { it.isNullOrEmpty() }
                val optionalParameters = parameters.optionalParameters
                for (parameter in optionalParameters) {
                    println("Optional parameter = ${parameter.name}")
                }
//                println("requiredParameters = $requiredParameters")
//                println("optionalParameters = $optionalParameters")
            }

            server.addTool(
                name = toToolName(eventReference.eventName, model.kClass.simpleName!!),
                description = "Executes the ${eventReference.eventName} command on the data ${model.kClass.simpleName}",
//                inputSchema = Tool.Input(properties, required),
            ) { request ->
                println("Request = $request")
                CallToolResult(
                    content = listOf(TextContent("Hello, world!"))
                )
            }

        }
        break

//        stateMachine.instanceStates.forEach { state ->
//            println(state.name)
//            state.onEventBlocks.forEach { eventBlock ->
//                println(eventBlock.first.name)
//                eventBlock.second.executables
//                    .filterIsInstance<InstanceEventTransitionWhen<*, *, *, *, *>>()
//                    .forEach { transition ->
//                        transition.branches.forEach { branch ->
//                            result += "${toVariable(state.name)} --> ${toVariable(branch.value.name)}: ${
//                                extractNameFromFunction(
//                                    branch.key
//                                )
//                            }\n"
//                        }
//                    }
//            }
//        }

    }


    return server
//        .apply {
//        // Add a tool
//        this.addTool(
//            name = "kotlin-sdk-tool",
//            description = "My test tool",
//            inputSchema = Tool.Input()
//        ) { request ->
//            CallToolResult(
//                content = listOf(TextContent("Hello, world!"))
//            )
//        }
//    }
}

private fun toToolName(eventName: String, modelName: String): String {
     fun toSnakeCase(camelCase: String): String {
        return camelCase.replace(Regex("([a-z])([A-Z])"), "$1_$2")
            .replace(Regex("([A-Z])([A-Z][a-z])"), "$1_$2")
            .lowercase()
    }
    return "${toSnakeCase(modelName)}_${toSnakeCase(eventName)}"
}
