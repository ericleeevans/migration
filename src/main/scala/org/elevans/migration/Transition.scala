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
