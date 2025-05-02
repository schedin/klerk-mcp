package com.github.klerkframework.mcp

import com.github.klerkframework.mcp.protocol.McpProtocol
import com.github.klerkframework.mcp.server.McpRequestHandler
import kotlinx.serialization.json.JsonNull
import kotlin.test.Test
import kotlin.test.assertNotNull

class McpPluginTest {

    @Test
    fun `test plugin initialization`() {
        val plugin = McpPlugin()
        assertNotNull(plugin, "Plugin should be created successfully")

        // This just verifies that initialization doesn't throw an exception
        plugin.initialize()
    }

    @Test
    fun `test server start and stop`() {
        val plugin = McpPlugin()
        plugin.initialize()

        // Start server on a non-standard port for testing
        plugin.startServer(port = 8081)

        // Stop server
        plugin.stopServer()
    }

    @Test
    fun `test handler registration`() {
        val plugin = McpPlugin()
        plugin.initialize()
        plugin.startServer(port = 8082)

        // Create a dummy handler
        val handler = object : McpRequestHandler {
            override suspend fun handleGetModel(request: McpProtocol.GetModelRequest): McpProtocol.GetModelResponse {
                return McpProtocol.GetModelResponse(requestId = request.requestId, model = JsonNull)
            }

            override suspend fun handleExecuteCommand(request: McpProtocol.ExecuteCommandRequest): McpProtocol.ExecuteCommandResponse {
                return McpProtocol.ExecuteCommandResponse(requestId = request.requestId, result = JsonNull)
            }

            override suspend fun handleQueryModels(request: McpProtocol.QueryModelsRequest): McpProtocol.QueryModelsResponse {
                return McpProtocol.QueryModelsResponse(
                    requestId = request.requestId,
                    models = emptyList(),
                    totalCount = 0,
                    pagination = null
                )
            }
        }

        // Register handler
        plugin.registerHandler("TestModel", handler)

        // Clean up
        plugin.stopServer()
    }
}
