package org.elevans.migration.parse

import org.elevans.migration.Transition

/**
 * Implementation of the `Transition` trait.
 *
 * @author Eric Evans
 */
case class TransitionMetaData(beforeState: String, afterState: String, isDestructive: Boolean) extends Transition


/**
 * Parse an optional `TransitionMetaData` instance from text.
 *
 * @author Eric Evans
 */
trait TransitionMetadataParser {

  /** Parse an optional `TransitionMetaData` instance from the given String. */
  def parse(s: String): Option[TransitionMetaData]
}

