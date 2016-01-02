package org.elevans.migration

import org.scalatest.{Matchers, FunSpec}

class MigrationGraphTest extends FunSpec with Matchers {

  case class TestTransition(label: String,
                            beforeState: String,
                            afterState: String,
                            isDestructive: Boolean = false) extends Transition

  class TestMigrationGraph(val transitions: Set[TestTransition]) extends MigrationGraph[TestTransition]

  def verify(path: Option[Seq[TestTransition]], labels: String*) (isDestructive: Boolean) = {
    path shouldNot be (None)
    path.get.map(_.label) shouldBe labels.toSeq
    path.get.exists(_.isDestructive) shouldBe isDestructive
  }

  describe("MigrationGraph with cycles") {

    //        C       -------> D <--------
    //        ^      |                    |
    //        |      v                    v
    // . <--> A <--> B <--> E <--> F <--> G <--> H
    //        ^                                  ^
    //        |                                  |
    //         ----------------------------------

    val migration = new TestMigrationGraph(Set(
      TestTransition(".->A", ".", "A"),
      TestTransition("A->.", "A", ".", isDestructive = true),
      TestTransition("A->B", "A", "B"),
      TestTransition("B->A", "B", "A", isDestructive = true),
      TestTransition("A->H", "A", "H"), // shortcut up
      TestTransition("H->A", "H", "A", isDestructive = true), // shortcut down
      TestTransition("A->C", "A", "C"), // dead end
      TestTransition("B->D", "B", "D"),
      TestTransition("D->B", "D", "B", isDestructive = true),
      TestTransition("D->G", "D", "G"),
      TestTransition("G->D", "G", "D", isDestructive = true),
      TestTransition("B->E", "B", "E"),
      TestTransition("E->B", "E", "B", isDestructive = true),
      TestTransition("E->F", "E", "F"),
      TestTransition("F->E", "F", "E", isDestructive = true),
      TestTransition("F->G", "F", "G"),
      TestTransition("G->F", "G", "F", isDestructive = true),
      TestTransition("G->H", "G", "H"),
      TestTransition("H->G", "H", "G", isDestructive = true)
    ))

    it("returns a path with an empty Seq of Transitions if the startState and endState are the same") {
      migration.getPath("B", "B") shouldBe Some(Seq.empty)
    }
    it("returns a non-destructive path with an empty Seq of Transitions if the startState and endState are the same") {
      migration.getNonDestructivePath("B", "B") shouldBe Some(Seq.empty)
    }

    Iterable(
      ("C", "A"),
      ("C", "H")
    ) foreach { case(start, end) =>
      it(s"returns None if no path can be found from startState '$start' to endState '$end'") {
        migration.getPath(start, end) shouldBe None
      }
    }

    Iterable(
      ("X", "C"),
      ("C", "X"),
      ("X", "Y"),
      ("X", "X")
    ) foreach { case(start, end) =>
      it(s"returns None if no path can be found because either startState '$start' or endState '$end' (or both) do not exist") {
        migration.getPath(start, end) shouldBe None
      }
    }

    it("returns the shortest path from '.' to 'H'") {
      verify(migration.getPath(".", "H"), ".->A", "A->H") (isDestructive = false)
    }
    it("returns the shortest path from 'A' to 'H'") {
      verify(migration.getPath("A", "H"), "A->H")         (isDestructive = false)
    }
    it("returns the shortest path from 'B' to 'H'") {
      verify(migration.getPath("B", "H"), "B->A", "A->H") (isDestructive = true)
    }
    it("returns the shortest path from 'D' to 'E'") {
      verify(migration.getPath("D", "E"), "D->B", "B->E") (isDestructive = true)
    }
    it("returns the shortest path from 'D' to 'F'") {
      verify(migration.getPath("D", "F"), "D->G", "G->F") (isDestructive = true)
    }

    it("returns any one of the shortest paths from startState to endState, when there is more than one") {
      val possibleSolutions = Set( Seq("A->B", "B->E", "E->F"), Seq("A->H", "H->G", "G->F") )
      val path = migration.getPath("A", "F")

      path shouldNot be (None)
      possibleSolutions.contains( path.get.map(_.label) ) shouldBe true
    }

    Iterable(
      ("C", "A"),
      ("B", "A"),
      ("D", "E"),
      ("G", "B"),
      ("H", "A")
    ) foreach { case(start, end) =>
      it(s"returns None if no non-destructive path can be found from startState '$start' to endState '$end'") {
        migration.getNonDestructivePath(start, end) shouldBe None
      }
    }

    Iterable(
      ("X", "C"),
      ("C", "X"),
      ("X", "Y"),
      ("X", "X")
    ) foreach { case(start, end) =>
      it(s"returns None if no non-destructive path can be found because either startState '$start' or endState '$end' (or both) do not exist") {
        migration.getNonDestructivePath(start, end) shouldBe None
      }
    }

    it("returns the shortest non-destructive path from '.' to 'H'") {
      verify(migration.getNonDestructivePath(".", "H"), ".->A", "A->H")         (isDestructive = false)
    }
    it("returns the shortest non-destructive path from 'A' to 'H'") {
      verify(migration.getNonDestructivePath("A", "H"), "A->H")                 (isDestructive = false)
    }
    it("returns the shortest non-destructive path from 'A' to 'F'") {
      verify(migration.getNonDestructivePath("A", "F"), "A->B", "B->E", "E->F") (isDestructive = false)
    }
    it("returns the shortest non-destructive path from 'B' to 'H'") {
      verify(migration.getNonDestructivePath("B", "H"), "B->D", "D->G", "G->H") (isDestructive = false)
    }
    it("returns the shortest non-destructive path from 'E' to 'H'") {
      verify(migration.getNonDestructivePath("E", "H"), "E->F", "F->G", "G->H") (isDestructive = false)
    }
  }
}
