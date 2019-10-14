package com.sksamuel.hoplite.json

import com.sksamuel.hoplite.ConfigException
import com.sksamuel.hoplite.ConfigLoader
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class Wibble(val a: LocalDateTime, val b: LocalDate, val c: Instant, val d: LocalTime)
enum class Season { Fall }

data class Foo(val wrongType: Boolean,
               val whereAmI: String,
               val notnull: String,
               val season: Season,
               val notalist: List<String>,
               val notamap: Map<String, Boolean>,
               val notaset: Set<Long>,
               val duration: Duration,
               val nested: Wibble)

class ErrorTests : StringSpec({

  "error handling for basic errors" {
    shouldThrow<ConfigException> {
      ConfigLoader().loadConfigOrThrow<Foo>("/error1.json")
    }.message shouldBe """Error loading config because:

    - Could not instantiate 'com.sksamuel.hoplite.json.Foo' because:
    
        - 'wrongType': Required type Boolean could not be decoded from a Long value: 123 (/error1.json:2:19)
    
        - 'whereAmI': Missing from config
    
        - 'notnull': Type defined as not-null but null was loaded from config (/error1.json:6:18)
    
        - 'season': Required a value for the Enum type com.sksamuel.hoplite.json.Season but given value was Fun (/error1.json:8:18)
    
        - 'notalist': Required a List but a Boolean cannot be converted to a collection (/error1.json:3:19)
    
        - 'notamap': Required a Map but a Double cannot be converted to a collection (/error1.json:5:22)
    
        - 'notaset': Required a Set but a Long cannot be converted to a collection (/error1.json:4:17)
    
        - 'duration': Required type java.time.Duration could not be decoded from a String value: 10 grams (/error1.json:7:26)
    
        - 'nested': - Could not instantiate 'com.sksamuel.hoplite.json.Wibble' because:
    
            - 'a': Required type java.time.LocalDateTime could not be decoded from a String value: qwqwe (/error1.json:10:17)
    
            - 'b': Required type java.time.LocalDate could not be decoded from a String value: qwqwe (/error1.json:11:17)
    
            - 'c': Required type java.time.Instant could not be decoded from a String value: qwqwe (/error1.json:12:17)
    
            - 'd': Unable to locate a decoder for class java.time.LocalTime: 47 are registered"""
  }

  "error handling for resource file failures" {
    shouldThrow<ConfigException> {
      ConfigLoader().loadConfigOrThrow<Foo>("/weqweqweqw.json", "ewrwerwerwer.yaml")
    }.message shouldBe """Error loading config because:

    Could not find config file /weqweqweqw.json
    
    Could not find config file ewrwerwerwer.yaml"""
  }
})
