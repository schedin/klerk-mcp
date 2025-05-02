package com.github.klerkframework.mcp

import com.github.klerkframework.mcp.server.McpRequestHandler
import com.github.klerkframework.mcp.server.McpServer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import org.slf4j.LoggerFactory

/**
 * Main entry point for the Klerk MCP (Model Context Protocol) plugin.
 * This plugin allows exposing a Klerk-based application as an MCP API.
 */
class McpPlugin {
    private val logger = LoggerFactory.getLogger(McpPlugin::class.java)
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private var server: McpServer? = null

    /**
     * Initializes the MCP plugin.
     */
    fun initialize() {
        logger.info("Initializing Klerk MCP Plugin")
    }

    /**
     * Starts the MCP server.
     *
     * @param port The port to listen on
     * @param host The host to bind to
     */
    fun startServer(port: Int = 8080, host: String = "0.0.0.0") {
        logger.info("Starting MCP server on $host:$port")
        server = McpServer(port, host).also { it.start() }
    }

    /**
     * Stops the MCP server.
     */
    fun stopServer() {
        logger.info("Stopping MCP server")
        server?.stop()
        server = null
    }

    /**
     * Registers a handler for a specific model type.
     *
     * @param modelType The model type to register the handler for
     * @param handler The handler to register
     */
    fun registerHandler(modelType: String, handler: McpRequestHandler) {
        logger.info("Registering handler for model type: $modelType")
        server?.registerHandler(modelType, handler)
    }
}
