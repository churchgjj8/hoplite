package com.sksamuel.hoplite.decoder.arrow

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import com.sksamuel.hoplite.BooleanNode
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.DecoderContext
import com.sksamuel.hoplite.DoubleNode
import com.sksamuel.hoplite.LongNode
import com.sksamuel.hoplite.Node
import com.sksamuel.hoplite.NullNode
import com.sksamuel.hoplite.StringNode
import com.sksamuel.hoplite.Undefined
import com.sksamuel.hoplite.decoder.Decoder
import com.sksamuel.hoplite.fp.flatMap
import com.sksamuel.hoplite.fp.invalid
import com.sksamuel.hoplite.fp.valid
import kotlin.reflect.KType

class OptionDecoder : Decoder<Option<*>> {

  override fun supports(type: KType): Boolean = type.classifier == Option::class

  override fun decode(node: Node,
                      type: KType,
                      context: DecoderContext): ConfigResult<Option<*>> {
    require(type.arguments.size == 1)
    val t = type.arguments[0].type!!

    fun <T> decode(value: Node, decoder: Decoder<T>): ConfigResult<Option<T>> {
      return decoder.decode(value, t, context).map { Some(it) }
    }

    return context.decoder(t).flatMap { decoder ->
      when (node) {
        is Undefined -> None.valid()
        is NullNode -> None.valid()
        is StringNode, is LongNode, is DoubleNode, is BooleanNode -> decode(node, decoder)
        else -> ConfigFailure.DecodeError(node, type).invalid()
      }
    }
  }
}
