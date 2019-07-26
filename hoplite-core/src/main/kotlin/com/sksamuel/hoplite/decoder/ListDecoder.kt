package com.sksamuel.hoplite.decoder

import arrow.data.invalidNel
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.ListNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.sequence
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.starProjectedType

class ListDecoder : Decoder<List<*>> {

  override fun supports(type: KType): Boolean = type.isSubtypeOf(List::class.starProjectedType)

  private fun <T> decode(node: ListNode,
                         type: KType,
                         decoder: Decoder<T>,
                         registry: DecoderRegistry): ConfigResult<List<T>> {
    return node.elements.map { decoder.decode(it, type, registry) }.sequence()
  }

  private fun <T> decode(node: StringNode,
                         type: KType,
                         decoder: Decoder<T>,
                         registry: DecoderRegistry): ConfigResult<List<T>> {
    val tokens = node.value.split(",").map { it.trim() }
    return tokens.map { decoder.decode(StringNode(it, node.pos), type, registry) }.sequence()
  }

  override fun decode(node: Node, type: KType, registry: DecoderRegistry): ConfigResult<List<*>> {
    require(type.arguments.size == 1)
    val t = type.arguments[0].type!!
    return registry.decoder(t).flatMap { decoder ->
      when (node) {
        is ListNode -> decode(node, t, decoder, registry)
        is StringNode -> decode(node, t, decoder, registry)
        else -> ConfigFailure("Unsupported list type ${node.javaClass.name}").invalidNel()
      }
    }
  }
}