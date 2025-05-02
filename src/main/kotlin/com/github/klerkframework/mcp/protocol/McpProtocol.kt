package com.github.klerkframework.mcp.protocol

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Represents the MCP (Model Context Protocol) protocol.
 * This protocol allows clients to interact with a Klerk-based application.
 */
object McpProtocol {
    
    /**
     * Base interface for all MCP requests.
     */
    interface McpRequest {
        val requestId: String
    }
    
    /**
     * Base interface for all MCP responses.
     */
    interface McpResponse {
        val requestId: String
    }
    
    /**
     * Request to get a model by ID.
     */
    @Serializable
    data class GetModelRequest(
        override val requestId: String,
        val modelType: String,
        val modelId: String
    ) : McpRequest
    
    /**
     * Response containing a model.
     */
    @Serializable
    data class GetModelResponse(
        override val requestId: String,
        val model: JsonElement?,
        val error: String? = null
    ) : McpResponse
    
    /**
     * Request to execute a command on a model.
     */
    @Serializable
    data class ExecuteCommandRequest(
        override val requestId: String,
        val modelType: String,
        val modelId: String?,
        val command: String,
        val parameters: JsonElement
    ) : McpRequest
    
    /**
     * Response after executing a command.
     */
    @Serializable
    data class ExecuteCommandResponse(
        override val requestId: String,
        val result: JsonElement?,
        val error: String? = null
    ) : McpResponse
    
    /**
     * Request to query models.
     */
    @Serializable
    data class QueryModelsRequest(
        override val requestId: String,
        val modelType: String,
        val filter: JsonElement? = null,
        val pagination: PaginationParams? = null
    ) : McpRequest
    
    /**
     * Response containing query results.
     */
    @Serializable
    data class QueryModelsResponse(
        override val requestId: String,
        val models: List<JsonElement>,
        val totalCount: Int,
        val pagination: PaginationInfo?,
        val error: String? = null
    ) : McpResponse
    
    /**
     * Pagination parameters for queries.
     */
    @Serializable
    data class PaginationParams(
        val page: Int,
        val pageSize: Int
    )
    
    /**
     * Pagination information in responses.
     */
    @Serializable
    data class PaginationInfo(
        val page: Int,
        val pageSize: Int,
        val totalPages: Int
    )
}
