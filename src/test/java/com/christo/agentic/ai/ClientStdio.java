package com.christo.agentic.ai;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.modelcontextprotocol.client.transport.StdioClientTransport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class ClientStdio {

    public static void main(String[] args) {
        ServerParameters stdioParams = ServerParameters.builder("java")
                .args("-jar", "target/gmail-mcp-java-server-0.0.1-SNAPSHOT.jar")
                .build();

        StdioClientTransport stdioTransport = new StdioClientTransport(stdioParams);

        McpSyncClient mcpClient = McpClient.sync(stdioTransport).build();

        mcpClient.initialize();

        McpSchema.ListToolsResult toolsList = mcpClient.listTools();

        log.info("Available tools:" + toolsList.tools());

        McpSchema.CallToolResult unreadMessages = mcpClient.callTool(
                new McpSchema.CallToolRequest("getUnreadMessages",
                        Map.of()));

        log.info("Unread messages: " + unreadMessages.content());

        McpSchema.CallToolResult message = mcpClient.callTool(
                new McpSchema.CallToolRequest("readEmail",
                        Map.of("messageId", "195e6a5b6e893d77")));

        log.info("Message: " + message.content());

        McpSchema.CallToolResult markEmailAsRead = mcpClient.callTool(
                new McpSchema.CallToolRequest("markEmailAsRead",
                        Map.of("messageId", "195eba17fd90b67c")));

        log.info("Mark email as read: " + markEmailAsRead.content());

        McpSchema.CallToolResult sendMessage = mcpClient.callTool(
                new McpSchema.CallToolRequest("sendEmail",
                        Map.of("recipientId", "christopherrozario7@gmail.com",
                                "subject", "Test Email",
                                "body", "This is a test email")));

        log.info("Send message: " + sendMessage.content());

        mcpClient.closeGracefully();
    }




}
