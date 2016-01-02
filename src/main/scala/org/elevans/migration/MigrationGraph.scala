package org.elevans.migration

import scalax.collection.Graph
import scalax.collection.edge.LDiEdge
import scala.language.postfixOps

/**
 * Comprises a `Set` of possible `Transitions`, and can find a sequence of those Transitions that
 * constitutes a migration "path" from one state to another.
 *
 * @author Eric Evans
 */
trait MigrationGraph[T <: Transition] {
  /**
   * All the Transitions that can be found and executed by this Migration.
   */
  val transitions: Set[T]

  /**
   * Get the path (if any exist) that is the shortest sequence of Transitions required to migrate from startState to endState.
   * NOTE: If there exists more than one shortest path, then one of them is arbitrarily returned.
   *
   * @param startState state for path to start from
   * @param endState   state for path to end at
   *
   * @return Some sequence of Transitions from startState to endState, or None if there is no such path;
   *         if startState == endState then returns an empty sequence.
   */
  def getPath(startState: String, endState: String): Option[Seq[T]] = path(startState, endState, fullGraph)

  /**
   * Get the non-destructive path (if any exist) that is the shortest sequence of Transitions required to migrate from
   * startState to endState.
   * NOTE: If there exists more than one shortest non-destructive path, then one of them is arbitrarily returned.
   *
   * @param startState state for path to start from
   * @param endState   state for path to end at
   *
   * @return Some non-destructive sequence of Transitions from startState to endState, or None if there is no such path;
   *         if startState == endState then returns an empty sequence.
   */
  def getNonDestructivePath(startState: String, endState: String): Option[Seq[T]] = path(startState, endState, nonDestructiveGraph)


  private[this] lazy val fullGraph = Graph( transitions.map { t => LDiEdge(t.beforeState, t.afterState)(t) }.toSeq :_* )
  private[this] lazy val nonDestructiveGraph = Graph( transitions.filterNot(_.isDestructive).map { t => LDiEdge(t.beforeState, t.afterState)(t) }.toSeq :_* )

  private[this] def node(state: String, g: Graph[String, LDiEdge]): g.NodeT = g get state

  private[this] def path(startState: String, endState: String, g: Graph[String, LDiEdge]) : Option[Seq[T]] =
    if (!g.contains(startState) || !g.contains(endState)) None
    else node(startState, g) shortestPathTo node(endState, g) map (_.edges map(_.label.asInstanceOf[T]) toSeq)
}
