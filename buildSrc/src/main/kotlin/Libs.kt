object Libs {

  const val kotlinVersion = "1.4.30"
  const val org = "com.sksamuel.hoplite"

  object Kotlin {
    const val reflect = "org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion"
    const val stdlib = "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"
    const val datetime = "org.jetbrains.kotlinx:kotlinx-datetime:0.1.1"
  }

  object Arrow {
    const val Validation = "io.arrow-kt:arrow-validation:0.11.0"
    const val Data = "io.arrow-kt:arrow-core-data:0.11.0"
  }

  object Aws {
    private const val version = "1.11.954"
    const val core = "com.amazonaws:aws-java-sdk-core:$version"
    const val ssm = "com.amazonaws:aws-java-sdk-ssm:$version"
  }

  object CronUtils {
    const val utils = "com.cronutils:cron-utils:9.1.3"
  }

  object Hadoop {
    const val common = "org.apache.hadoop:hadoop-common:2.10.1"
  }

  object Hikari {
    const val core = "com.zaxxer:HikariCP:4.0.2"
  }

  object Typesafe {
    const val config = "com.typesafe:config:1.4.1"
  }

  object Jackson {
    const val core = "com.fasterxml.jackson.core:jackson-core:2.12.1"
    const val databind = "com.fasterxml.jackson.core:jackson-databind:2.12.1"
  }

  object Kotest {
    private const val version = "4.4.1"
    const val assertions = "io.kotest:kotest-assertions-core-jvm:$version"
    const val junit5 = "io.kotest:kotest-runner-junit5-jvm:$version"
  }

  object Toml {
    const val toml = "org.tomlj:tomlj:1.0.0"
  }

  object Vavr {
    const val kotlin = "io.vavr:vavr-kotlin:0.10.2"
  }
}
