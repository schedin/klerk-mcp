package dev.klerkframework.mcp

import io.ktor.server.routing.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.mcp

fun configureMcpServer(): Routing.() -> Unit = {
    mcp {
        getMcpServer()
    }
}

fun getMcpServer(): Server {
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
