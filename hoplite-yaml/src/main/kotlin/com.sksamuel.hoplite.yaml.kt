package com.sksamuel.hoplite.yaml

import arrow.core.toOption
import arrow.data.invalidNel
import arrow.data.valid
import arrow.data.validNel
import com.sksamuel.hoplite.CannotParse
import com.sksamuel.hoplite.ConfigFailure
import com.sksamuel.hoplite.ConfigLocation
import com.sksamuel.hoplite.ConfigResult
import com.sksamuel.hoplite.Cursor2
import com.sksamuel.hoplite.preprocessor.EnvVarPreprocessor
import com.sksamuel.hoplite.MapCursor2
import com.sksamuel.hoplite.preprocessor.Preprocessor
import com.sksamuel.hoplite.arrow.flatMap
import com.sksamuel.hoplite.arrow.sequence
import com.sksamuel.hoplite.converter.DataClassConverter
import org.yaml.snakeyaml.Yaml
import org.yaml.snakeyaml.constructor.SafeConstructor
import org.yaml.snakeyaml.error.Mark
import org.yaml.snakeyaml.error.MarkedYAMLException
import java.io.InputStream
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.reflect.KClass

class ConfigLoader(private val preprocessors: List<Preprocessor> = listOf(EnvVarPreprocessor)) {

  fun withPreprocessor(preprocessor: Preprocessor) = ConfigLoader(preprocessors + preprocessor)

  /**
   * Attempts to load config from /application.yml on the resource path and returns
   * an instance of <A> if the values can be appropriately converted.
   */
  inline fun <reified A : Any> loadConfig(): ConfigResult<A> = loadConfig("/application.yml")

  inline fun <reified A : Any> loadConfigOrThrow(vararg resources: String): A =
      loadConfig<A>(*resources).fold(
          { errors -> throw RuntimeException("Error loading config\n" + errors.all.joinToString("\n") { it.description() }) },
          { it }
      )

  inline fun <reified A : Any> loadConfig(vararg resources: String): ConfigResult<A> = loadConfig(A::class, *resources)

  fun <A : Any> loadConfig(klass: KClass<A>, vararg resources: String): ConfigResult<A> {

    val streams = resources.map { resource ->
      this.javaClass.getResourceAsStream(resource).toOption().fold(
          { ConfigFailure("Could not find resource $resource").invalidNel() },
          { it.valid() }
      )
    }.sequence()

    val cursors = streams.flatMap {
      it.map { stream -> toCursor(stream) }.sequence()
    }.map { cs ->
      cs.map { c ->
        preprocessors.fold(c) { acc, p -> acc.transform(p::process) }
      }
    }

    return cursors.map {
      it.reduce { a, b -> a.withFallback(b) }
    }.flatMap {
      DataClassConverter(klass).apply(it)
    }
  }

  fun toCursor(stream: InputStream): ConfigResult<Cursor2> = handleYamlErrors(stream) {
    val yaml = Yaml(SafeConstructor())
    when (val result = yaml.load<Any>(it)) {
      is Map<*, *> -> MapCursor2(result).validNel()
      else -> ConfigFailure("Unsupported YAML return type ${result.javaClass.name}").invalidNel()
    }
  }

  fun <A> handleYamlErrors(stream: InputStream, f: (InputStream) -> ConfigResult<A>): ConfigResult<A> =
      try {
        f(stream)
      } catch (e: MarkedYAMLException) {
        CannotParse(e.message!!, locationFromMark(Paths.get("/todo"), e.problemMark)).invalidNel()
      } catch (t: Throwable) {
        ConfigFailure.throwable(t).invalidNel()
      }

  fun locationFromMark(path: Path, mark: Mark): ConfigLocation = ConfigLocation(path.toUri().toURL(), mark.line)

}