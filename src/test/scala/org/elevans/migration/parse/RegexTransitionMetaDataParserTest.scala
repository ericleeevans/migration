// Copyright 2015-2016 Eric Evans : Scala migration
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

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
