package dev.klerkframework.mcp

import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions

//fun configureMcpServer(): Routing.() -> Unit = {
//    mcp {
//        getMcpServer()
//    }
//}

fun createMcpServer(): Server {
    println("Test1")
    return Server(
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
    ).apply {
        // Add a tool
        this.addTool(
            name = "kotlin-sdk-tool",
            description = "My test tool",
            inputSchema = Tool.Input()
        ) { request ->
            CallToolResult(
                content = listOf(TextContent("Hello, world!"))
            )
        }
    }
}
