package org.elevans.migration.parse

import scala.util.matching.Regex

/**
 * `TransitionMetadataParser` implementation that can use any `Regex` that matches the three components of `TransitionMetaData`.
 *
 * @author Eric Evans
 */
class RegexTransitionMetadataParser(val regex: Regex) extends TransitionMetadataParser {

  def parse(s: String) = s match {
    case regex(before, after, isDestructive) if isBoolean(isDestructive) => Some(TransitionMetaData(before, after, isDestructive.toBoolean))
    case _ => None
  }

  private[this] def isBoolean(s:String) = scala.util.Try{s.toLowerCase.toBoolean; true}.getOrElse(false)
}
