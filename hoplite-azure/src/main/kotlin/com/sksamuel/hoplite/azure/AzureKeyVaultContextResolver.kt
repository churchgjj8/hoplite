package com.sksamuel.hoplite.azure

import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.security.keyvault.secrets.SecretClient
import com.azure.security.keyvault.secrets.SecretClientBuilder
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.resolver.CompositeResolver
import com.sksamuel.hoplite.resolver.ContextResolver
import com.sksamuel.hoplite.resolver.Resolver

class AzureKeyVaultContextResolver(
  private val report: Boolean = false,
  private val createClient: () -> SecretClient
) : ContextResolver() {

  constructor(url: String) : this(url, false)
  constructor(url: String, report: Boolean) : this(report = report, {
    SecretClientBuilder()
      .vaultUrl(url)
      .credential(DefaultAzureCredentialBuilder().build())
      .buildClient()
  })

  private val client = lazy { createClient() }
  private val ops = lazy { AzureOps(client.value) }

  override val contextKey = "azure-key-vault"
  override val default: Boolean = true

  override fun lookup(path: String, node: StringNode, root: Node, context: DecoderContext): ConfigResult<String?> {
    return ops.value.fetchSecret(path, context, report)
  }
}

class LegacyAzureKeyVaultContextResolver(
  private val report: Boolean = false,
  private val createClient: () -> SecretClient
) : ContextResolver() {

  constructor(url: String) : this(url, false)
  constructor(url: String, report: Boolean) : this(report = report, {
    SecretClientBuilder()
      .vaultUrl(url)
      .credential(DefaultAzureCredentialBuilder().build())
      .buildClient()
  })

  private val client = lazy { createClient() }
  private val ops = lazy { AzureOps(client.value) }

  override val contextKey = "azurekeyvault"
  override val default: Boolean = true

  override fun lookup(path: String, node: StringNode, root: Node, context: DecoderContext): ConfigResult<String?> {
    return ops.value.fetchSecret(path, context, report)
  }
}

fun AzureKeyVaultContextResolvers(
  report: Boolean = false,
  createClient: () -> SecretClient
): Resolver = CompositeResolver(
  AzureKeyVaultContextResolver(report, createClient),
  LegacyAzureKeyVaultContextResolver(report, createClient),
)
