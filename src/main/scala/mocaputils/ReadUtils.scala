package mocaputils

import collection.immutable.{ IndexedSeq }
import scalaz.{ Validation, Success, Failure }

/** Useful utility functions for reading files. */
object ReadUtils {

  /** Reads a string from an `Iterator[String]`, returning a `Failure` if
    * there are no more strings in the iterator.
    *
    * @param lines iterator of strings from which to read
    * @return successfully-read string, or an error message */
  def nextLine(lines: Iterator[String]): Validation[String, String] = {
    if (!lines.hasNext) {
      Failure("Expected another line in the source file.")
    } else {
      Success(lines.next)
    }
  }

  /** Fetch a tab-separated `String` from an `Iterator[String]`, and check it
    * against a list of required items.
    *
    * Example: A line in a file may look as follows:
    * {{{
    * Sample Rate:\t400\tName:\ttest
    * }}}
    * In this case, it may be desirable to make sure that the `"Sample Rate:"`
    * and `"Name:"` fields are present, and to know that their values exist.
    * To perform that check, this function could be called as follows:
    * {{{
    * val l = fetchLineAndCheck(lines,
    *                           Some("Sample Rate:"), None,
    *                           Some("Name:"), None)
    * }}}
    * This pattern requires that the two text fields exist, and assumes
    * place-holders for the numerical and text values.  The function then
    * returns either an error message or a sequence of `String`s representing
    * the fields.
    *
    * @param lines iterator of strings from which to read
    * @param items varargs list of items to find (either `Some("item")` or
    *   `None`)
    * @return successfully-read items, or an error message */
  def fetchLineAndCheck(lines: Iterator[String], items: Option[String]*): 
  Validation[String, IndexedSeq[String]] = {
    val line = nextLine(lines)
    val tokens = for (l <- line) yield l.split("\t")

    tokens fold (e => Failure(e), lineTokens =>
      if (lineTokens.length != items.length) {
	Failure("Wrong item count (found %d, expected %d): \"%s\"".format(
	  lineTokens.length, items.length, line | None))
      } else {
	val checks = (lineTokens zip items).filter(_._2.isDefined).map(t => t._1 == t._2.get)
	if (checks.exists(_ == false)) {
	  val i = checks.indexOf(false)
	  Failure("Token mismatch: expected \"%s\", found \"%s\"" format (items(i), lineTokens(i)))
	} else {
	  Success(lineTokens.toIndexedSeq)
	}
      }
    )
  }

  /** Fetches and parses a `Double` from a position within an `IndexedSeq[String]`.
    * 
    * @param s sequence of strings from which to fetch and parse the `Double`
    * @param index index within the sequence
    * @return parsed `Double` or an error message */
  def getDouble(s: IndexedSeq[String], index: Int): Validation[String, Double] = {
    try {
      Success(s(index).trim.toDouble)
    } catch {
      case e: IndexOutOfBoundsException => Failure("Item %d does not exist." format (index))
      case e: NumberFormatException => Failure("Could not parse \"%s\" as Double." format(
	s(index).trim))
    }
  }

  /** Fetches and parses an `Int` from a position within an `IndexedSeq[String]`.
    * 
    * @param s sequence of strings from which to fetch and parse the `Int`
    * @param index index within the sequence
    * @return parsed `Int` or an error message */
  def getInt(s: IndexedSeq[String], index: Int): Validation[String, Int] = {
    try {
      Success(s(index).trim.toInt)
    } catch {
      case e: IndexOutOfBoundsException => Failure("Item %d does not exist." format (index))
      case e: NumberFormatException => Failure("Could not parse \"%s\" as Int." format(
	s(index).trim))
    }
  }

  /** Skips a specified number of items from an `Iterator[String]`.
    *
    * In this method, it is an error if a line is not available to be skipped. 
    * 
    * @param lines iterator from which to skip items
    * @param nLines number of lines to skip
    * @return an error message, or `None` if the method returns successfully */ 
  def skipLines(lines: Iterator[String], nLines: Int = 1): Option[String] = {
    var i = 0
    while (i < nLines) {
      nextLine(lines) fold (e => return Some(e), s => None) // break out early if error
      i = i + 1
    }
    return None
  }

}
