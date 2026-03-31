# Azure CLI vs Azure MCP (Model Context Protocol)

This document compares the **Azure CLI** (Command-Line Interface) with the **Azure MCP Server** (Model Context Protocol), two tools for interacting with Azure services — each suited to different audiences and workflows.

---

## Overview

| | Azure CLI | Azure MCP Server |
|---|---|---|
| **Primary audience** | Developers, DevOps engineers, system admins | AI agents, copilots, LLM-powered applications |
| **Interaction style** | Human types commands | AI agent discovers and invokes tools automatically |
| **Protocol** | Shell commands (POSIX / PowerShell) | Model Context Protocol (MCP) over stdio or HTTP/SSE |
| **Output format** | Plain text, table, or JSON | Structured JSON (schema-validated) |
| **Tool discovery** | User must know commands upfront | Agent discovers available tools and their schemas at runtime |
| **Official package** | `azure-cli` (pip) | `@azure/mcp` (npm) |

---

## What Is Azure CLI?

[Azure CLI](https://learn.microsoft.com/en-us/cli/azure/) is Microsoft's cross-platform command-line tool for managing Azure resources. It is designed for direct human use (or scripted automation) and covers virtually every Azure service through a hierarchy of command groups.

### Key characteristics

- **Imperative**: You specify exactly what to do and in what order.
- **Scriptable**: Works in Bash, PowerShell, and CI/CD pipelines (GitHub Actions, Azure DevOps).
- **Comprehensive coverage**: Thousands of commands covering every Azure resource type.
- **Mature ecosystem**: Available since 2017; well-documented with widespread community adoption.

### Installation

```bash
# macOS (Homebrew)
brew update && brew install azure-cli

# Windows (WinGet)
winget install -e --id Microsoft.AzureCLI

# Linux (Debian/Ubuntu)
curl -sL https://aka.ms/InstallAzureCLIDeb | sudo bash

# Python (pip)
pip install azure-cli
```

### Authentication

```bash
az login                     # Interactive browser login
az login --use-device-code   # Device-code flow (headless)
az login --service-principal -u <appId> -p <password> --tenant <tenant>
```

### Example usage

```bash
# List all resource groups
az group list --output table

# Create a storage account
az storage account create \
  --name mystorageaccount \
  --resource-group myResourceGroup \
  --location eastus \
  --sku Standard_LRS

# Deploy an app to Azure Container Apps
az containerapp up \
  --name my-app \
  --resource-group myResourceGroup \
  --image mcr.microsoft.com/azuredocs/containerapps-helloworld
```

---

## What Is Azure MCP Server?

The [Azure MCP Server](https://github.com/microsoft/mcp) is a server implementation of the [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) — an open standard that lets AI agents (such as GitHub Copilot, Claude, or custom LLM applications) discover and invoke tools backed by Azure services using natural language and structured JSON.

### Key characteristics

- **Agent-native**: Designed to be consumed by AI agents, not humans directly.
- **Schema-driven**: Every tool exposes a JSON schema so agents understand inputs/outputs without prior knowledge.
- **Dynamic discovery**: Agents query the server at runtime to learn what operations are available, enabling zero-code integration.
- **Pluggable**: The same MCP protocol works with Azure, AWS, SaaS APIs, databases, etc., giving agents a unified interface.
- **Secure by design**: Leverages `DefaultAzureCredential` (Azure Entra ID) and supports fine-grained RBAC.

### Installation

```bash
# Run without installing (recommended for quick start)
npx -y @azure/mcp@latest server start

# Global install
npm install -g @azure/mcp
azmcp server start
```

### Configure with VS Code (GitHub Copilot Agent Mode)

Create `.vscode/mcp.json` in your workspace:

```json
{
  "servers": {
    "Azure MCP Server": {
      "command": "npx",
      "args": ["-y", "@azure/mcp@latest", "server", "start"]
    }
  }
}
```

Once configured, GitHub Copilot in Agent Mode will list **Azure MCP Server** tools automatically and can perform Azure operations on your behalf using natural language prompts.

### Authentication

Azure MCP Server uses [`DefaultAzureCredential`](https://learn.microsoft.com/en-us/azure/developer/intro/passwordless-overview), which automatically tries the following credential chain:

1. Environment variables (`AZURE_CLIENT_ID`, `AZURE_CLIENT_SECRET`, `AZURE_TENANT_ID`)
2. Workload identity (in Kubernetes / Azure Container Apps)
3. Managed identity
4. Azure CLI logged-in user (`az login`)
5. VS Code Azure extension credentials

For local development, simply run `az login` before starting the MCP server.

### Example interaction (natural language → MCP → Azure)

```
User prompt:  "List all my storage containers in the 'myResourceGroup' resource group"

Copilot agent → Azure MCP Server → Azure Storage API
             ← structured JSON response
             → "You have 3 containers: logs, backups, assets"
```

---

## Feature Comparison

| Feature | Azure CLI | Azure MCP Server |
|---|---|---|
| **Human-operated** | ✅ Yes | ❌ No (agent-operated) |
| **AI / agent integration** | ❌ Requires prompt engineering | ✅ Native MCP protocol |
| **Tool / command discovery** | ❌ Must know commands upfront | ✅ Auto-discovery via JSON schema |
| **Schema validation** | ❌ No | ✅ Yes |
| **Output format** | Text, table, optional JSON | Always structured JSON |
| **Authentication options** | az login, service principal, managed identity | DefaultAzureCredential (full chain) |
| **CI/CD pipelines** | ✅ Excellent fit | ⚠️ Possible but uncommon |
| **Multi-cloud support** | ❌ Azure only | ✅ Yes (via MCP ecosystem) |
| **IDE integration** | Terminal / Azure Extensions | VS Code Agent Mode, Copilot, etc. |
| **Context management** | Stateless (each command independent) | Session-aware via MCP context |
| **Learning curve** | Moderate (must learn command syntax) | Low for end-users (natural language) |
| **Extensibility** | Scripts, aliases | Custom MCP servers / tools |
| **Maturity** | High (released 2017) | Emerging (2024–2025) |

---

## Use Cases

### When to use Azure CLI

- **Manual administration**: Quick, one-off Azure tasks (listing resources, debugging, exploring).
- **CI/CD automation**: Deploying infrastructure, managing secrets, updating app settings in GitHub Actions or Azure DevOps pipelines.
- **Infrastructure as Code (IaC) companion**: Wrapping complex resource creation in shell scripts alongside Bicep/Terraform.
- **Batch operations**: Processing many resources via `az … --query` + `jq` pipelines.

```bash
# CI/CD example: update a container image in Azure Container Apps
az containerapp update \
  --name my-service \
  --resource-group prod-rg \
  --image myacr.azurecr.io/myapp:${{ github.sha }}
```

### When to use Azure MCP Server

- **AI-powered developer tools**: Letting GitHub Copilot or other agents perform Azure tasks on your behalf without writing CLI scripts.
- **Conversational infrastructure management**: "What's the status of my deployments?" answered by an agent in natural language.
- **Agentic workflows**: Multi-step, multi-service automation orchestrated by an LLM (e.g., create a resource group, deploy a container app, configure DNS — all from one prompt).
- **Reducing context-switching**: Developers stay in their editor while an agent handles cloud resource management in the background.
- **Standardised AI tooling**: Building applications where multiple AI clients (different IDEs, chat apps) need to interact with Azure using a common protocol.

---

## Hybrid Approach

Azure CLI and Azure MCP are **complementary, not mutually exclusive**. A common pattern is to configure the MCP server to wrap Azure CLI commands, giving AI agents the ability to invoke CLI operations while the MCP layer handles discovery, schema validation, and security:

```json
{
  "servers": {
    "azure-cli-mcp": {
      "command": "npx",
      "args": ["-y", "@azure/mcp@latest", "server", "start"],
      "env": {
        "AZURE_SUBSCRIPTION_ID": "your-subscription-id"
      }
    }
  }
}
```

This means:
- Humans use **Azure CLI** directly for scripts and pipelines.
- AI agents use **Azure MCP Server** for intelligent, context-aware cloud operations.
- Both authenticate through the same Azure identity (`az login` / Entra ID).

---

## Summary

| | Azure CLI | Azure MCP Server |
|---|---|---|
| **Best for** | Human-driven scripts & pipelines | AI agents & copilots |
| **Interface** | Shell commands | JSON over MCP protocol |
| **Discoverability** | None (must know the API) | Full (schema-driven) |
| **Integration** | CI/CD, Bash, PowerShell | VS Code, Copilot, LLM apps |
| **Maturity** | Mature | Emerging / rapidly growing |

---

## Further Reading

- [Azure CLI documentation](https://learn.microsoft.com/en-us/cli/azure/)
- [Azure MCP Server – Get Started](https://learn.microsoft.com/en-us/azure/developer/azure-mcp-server/get-started)
- [Model Context Protocol specification](https://modelcontextprotocol.io/)
- [Azure MCP Server on GitHub (microsoft/mcp)](https://github.com/microsoft/mcp)
- [MCP vs MCP-CLI: Dynamic Tool Discovery for Token-Efficient AI Agents](https://techcommunity.microsoft.com/blog/azuredevcommunityblog/mcp-vs-mcp-cli-dynamic-tool-discovery-for-token-efficient-ai-agents/4494272)
