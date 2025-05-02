package com.github.klerkframework.mcp.server

import com.github.klerkframework.mcp.protocol.McpProtocol

/**
 * Interface for handling MCP requests for a specific model type.
 */
interface McpRequestHandler {
    
    /**
     * Handles a request to get a model by ID.
     */
    suspend fun handleGetModel(request: McpProtocol.GetModelRequest): McpProtocol.GetModelResponse
    
    /**
     * Handles a request to execute a command on a model.
     */
    suspend fun handleExecuteCommand(request: McpProtocol.ExecuteCommandRequest): McpProtocol.ExecuteCommandResponse
    
    /**
     * Handles a request to query models.
     */
    suspend fun handleQueryModels(request: McpProtocol.QueryModelsRequest): McpProtocol.QueryModelsResponse
}
