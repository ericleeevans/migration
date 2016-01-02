package org.elevans.migration

/**
 * Metadata about a transition from one state to another.
 *
 * @author Eric Evans
 */
trait Transition {
  /** State which should hold before executing this transition. */
  val beforeState: String

  /** State which should hold after executing this transition successfully. */
  val afterState: String

  /** True if executing the transition irreversibly would destroy something; exact meaning is implementation-dependent. */
  val isDestructive: Boolean
}
