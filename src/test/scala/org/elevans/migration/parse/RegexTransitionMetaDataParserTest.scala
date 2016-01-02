package org.elevans.migration.parse

import org.scalatest.{Matchers, FunSpec}

class RegexTransitionMetaDataParserTest extends FunSpec with Matchers {

  describe("RegexTransitionMetadataParser with simple Regex that matches single-line text without internal whitespace") {

    val parser = new RegexTransitionMetadataParser("""(?i).*from([\S^-]+)\-to([\S^-]+)\-destructive(true|false).*""".r)
    val transition = Some(TransitionMetaData("1.0.4", "1.0.5", true))

    Iterable(
      "from1.0.4-to1.0.5-destructiveTrue.txt",
      "from1.0.4-to1.0.5-destructiveTRUE.txt",
      "FROM1.0.4-TO1.0.5-DESTRUCTIVEtrue.txt",
      "From1.0.4-To1.0.5-DestructiveTrue.txt",
      "From1.0.4-To1.0.5-DestructiveTrue",
      "/foo/bar/from1.0.4-to1.0.5-destructiveTrue.txt",
      "moveFrom1.0.4-to1.0.5-destructiveTrueScript.txt"
    ) foreach { string =>
      it(s"extracts TransitionMetaData from String that matches the Regex: '$string'") {
        parser.parse(string) shouldBe transition
      }
    }

    Iterable(
      "from1.0.4 -to1.0.5 -destructiveTrue.txt",
      "from 1.0.4-to 1.0.5- destructiveTrue.txt",
      """from1.0.4
          .to1.0.5
          .destructiveTrue"""
    ) foreach { string =>
      it(s"finds no TransitionMetaData in String that does not match the Regex: '$string'") {
        parser.parse(string) shouldBe None
      }
    }
  }
}
