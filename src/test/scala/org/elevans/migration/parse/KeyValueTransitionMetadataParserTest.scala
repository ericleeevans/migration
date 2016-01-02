package org.elevans.migration.parse

import org.scalatest.{Matchers, FunSpec}

class KeyValueTransitionMetadataParserTest extends FunSpec with Matchers {

  describe("KeyValueTransitionMetadataParser with no keyValueSeparator") {
    val parser = new KeyValueTransitionMetadataParser("from", "to", "destructive")
    val expectedTransition = Some(TransitionMetaData("1.0.4", "1.0.5", true))

    Iterable(
      "from1.0.4 to1.0.5 destructiveTrue ",
      "from 1.0.4 to 1.0.5 destructive TRUE ",
      "FROM 1.0.4  TO 1.0.5  DESTRUCTIVE true",
      "From 1.0.4 ; To1.0.5 ; Destructive True",
      "From1.0.4 ; To1.0.5 ; DestructiveTrue",

      """from 1.0.4
         to 1.0.5
         destructive True"""
    ) foreach { string =>
      it(s"extracts Transition metadata from matching single-line and multi-line String: '$string'") {
        parser.parse(string) shouldBe expectedTransition
      }
    }

    Iterable(
      "From 1.0.4, To 1.0.5, Destructive True",

      """from 1.0.4;
         to 1.0.5;
         destructive True"""
    ) foreach { string =>
      it(s"finds unexpected Transition metadata in String without required whitespace after a 'state' value: '$string'") {
        parser.parse(string) should not be None
        parser.parse(string) should not be expectedTransition
      }
    }

    Iterable(
      "from1.0.4to1.0.5destructiveTrue",
      "from1.0.4-to1.0.5-destructiveTrue"
    ) foreach { string =>
      it(s"finds no Transition metadata in String without required whitespace after a 'state' value and the following key: '$string'") {
        parser.parse(string) shouldBe None
      }
    }
  }

  describe("KeyValueTransitionMetadataParser with no keyValueSeparator and caseSensitive = true") {
    val parser = new KeyValueTransitionMetadataParser("from", "to", "destructive", caseSensitiveKeys = true)
    val expectedTransition = Some(TransitionMetaData("1.0.4", "1.0.5", true))

    Iterable(
      "from 1.0.4  to 1.0.5  destructive true",
      "from 1.0.4  to 1.0.5  destructive TRUE",
      "from 1.0.4  to 1.0.5  destructive True"
    ) foreach { string =>
      it(s"extracts Transition metadata from String with keys that have the expected case: '$string'") {
        parser.parse(string) shouldBe expectedTransition
      }
    }

    Iterable(
      "FROM 1.0.4  TO 1.0.5  DESTRUCTIVE true",
      "From 1.0.4  To 1.0.5  Destructive True"
    ) foreach { string =>
      it(s"finds no Transition metadata in String with keys that do not have the expected case: '$string'") {
        parser.parse(string) shouldBe None
      }
    }
  }

  describe("KeyValueTransitionMetadataParser with a keyValueSeparator") {
    val parser = new KeyValueTransitionMetadataParser("transition-before", "transition-after", "transition-destructive", "=")
    val expectedTransition = Some(TransitionMetaData("1.0.4", "1.0.5", true))

    Iterable(
      "transition-before=1.0.4 transition-after=1.0.5 transition-destructive=true",
      "transition-before = 1.0.4 transition-after = 1.0.5 transition-destructive = true",
      "transition-before = 1.0.4 transition-after = 1.0.5 transition-destructive = TrUe",
      "transition-before = 1.0.4 transition-after = 1.0.5 transition-destructive = TRUE",
      "Transition-Before = 1.0.4 Transition-After = 1.0.5 Transition-Destructive = true",
      "TRANSITION-BEFORE = 1.0.4 TRANSITION-AFTER = 1.0.5 TRANSITION-DESTRUCTIVE = true",
      "transition-before = 1.0.4 ; transition-after = 1.0.5 ; transition-destructive = true",
      "transition-before= 1.0.4 ; transition-after =1.0.5 ; transition-destructive=true",
      "   transition-before = 1.0.4 transition-after = 1.0.5 transition-destructive = true   ",
      "Other stuff . . . transition-before = 1.0.4 transition-after = 1.0.5 transition-destructive = true. . .other stuff",

      """Other stuff . . .

          transition-before = 1.0.4
          transition-after = 1.0.5
          transition-destructive = true

          . . .other stuff""",

      """Other stuff . . .

          -- transition-before = 1.0.4
          -- transition-after = 1.0.5
          --
          -- transition-destructive = true

          . . .other stuff""",

      """Other stuff . . .

           transition-before =
           1.0.4
           transition-after
           = 1.0.5

           transition-destructive
           =

           true

          . . .other stuff""",

      """Other
          // This is the 'before' state:
          // TRANSITION-BEFORE = 1.0.4
          //
          // This is the 'after' state:
          // TRANSITION-AFTER=1.0.5
          //
          // This transition destroys data: TRANSITION-DESTRUCTIVE = TRUE
          //
          // That's all, folks!""",

      """Other
           TRANSITION-BEFORE = 1.0.4 (old stuff)
           TRANSITION-AFTER=1.0.5 (new stuff!)
           TRANSITION-DESTRUCTIVE = TRUE"""


    ) foreach { string =>
      it(s"extracts Transition metadata from matching single-line and multi-line String: '$string'") {
        parser.parse(string) shouldBe expectedTransition
      }
    }

    Iterable(
      "transition-before = 1.0.4, transition-after = 1.0.5, transition-destructive = true",

      """Other
           TRANSITION-BEFORE = 1.0.4 (old stuff)
           TRANSITION-AFTER=1.0.5(new stuff!)
           TRANSITION-DESTRUCTIVE = TRUE"""
    ) foreach { string =>
      it(s"finds unexpected Transition metadata in String without required whitespace after a 'state' value: '$string'") {
        parser.parse(string) should not be None
        parser.parse(string) should not be expectedTransition
      }
    }

    Iterable(
      "transition-before=1.0.4transition-after=1.0.5transition-destructive=true"
    ) foreach { string =>
      it(s"finds no Transition metadata in String without required whitespace after a 'state' value and the following key: '$string'") {
        parser.parse(string) shouldBe None
      }
    }
  }
}
