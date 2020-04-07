package com.backwards.config

import scala.language.experimental.macros
import scala.reflect.ClassTag
import cats.effect.IO
import pureconfig.generic.ExportMacros
import pureconfig.{ConfigReader, ConfigSource, ConfigWriter, Exported}

/**
 * Copy of pureconfig.generic.auto.
 * Unfortunately, upon importing said object, IntelliJ thinks it is unused so import optimisation will incorrectly remove it.
 * As an IntelliJ workaround, instead mix in this trait.
 */
trait PureConfig {
  implicit def exportReader[A]: Exported[ConfigReader[A]] = macro ExportMacros.exportDerivedReader[A]

  implicit def exportWriter[A]: Exported[ConfigWriter[A]] = macro ExportMacros.exportDerivedWriter[A]
}

object PureConfig {
  def config[C: ConfigReader: ClassTag](namespace: String): IO[C] =
    IO(ConfigSource.default.at(namespace).loadOrThrow[C])
}