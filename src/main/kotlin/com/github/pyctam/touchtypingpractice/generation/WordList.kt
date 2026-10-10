package com.github.pyctam.touchtypingpractice.generation

import java.io.IOException

/**
 * A single entry from the bundled English word list.
 *
 * @property word the normalized (lowercase) word.
 * @property index the 0-based position of the word in the normalized list. The source list is
 *   ordered by frequency (most common first) and normalization preserves that order, so a lower
 *   index means a more common word; the index is used as a frequency proxy for ranking.
 * @property letterMask a 26-bit mask where bit N is set if the word contains the letter `'a' + N`.
 *   Used for fast key-coverage checks.
 */
data class WordEntry(
  val word: String,
  val index: Int,
  val letterMask: Int,
)

/**
 * Loads and preprocesses the bundled English word list exactly once.
 *
 * The word list is the "Google 10,000 English Words" dataset, bundled as the classpath resource
 * [RESOURCE_PATH] and read from the packaged plugin (not from a machine-specific filesystem path),
 * one word per line. Blank lines are skipped, words are normalized to lowercase, duplicates are
 * removed (the first occurrence wins, preserving the source frequency order), and entries that are
 * not pure lowercase-letter words are rejected.
 *
 * The parsed result is cached in [instance] so the resource is read and parsed only once per
 * application, even across many exercise-generation requests.
 *
 * Source: https://github.com/first20hours/google-10000-english
 */
class WordList private constructor(val words: List<WordEntry>) {

  /** The number of words in the list. */
  val size: Int
    get() = words.size

  companion object {
    /** Classpath location of the bundled word list. */
    const val RESOURCE_PATH = "/google-10000-english.txt"

    @Volatile private var instance: WordList? = null

    /**
     * Returns the shared, lazily-loaded word list, loading and parsing the bundled resource on
     * first use.
     *
     * @throws WordListException if the resource is missing or unreadable.
     */
    fun load(): WordList {
      instance?.let {
        return it
      }
      synchronized(this) {
        instance?.let {
          return it
        }
        val loaded = fromLines(loadLines(RESOURCE_PATH))
        instance = loaded
        return loaded
      }
    }

    /**
     * Reads the word list from the classpath resource [path].
     *
     * @throws WordListException if the resource is missing or cannot be read.
     */
    fun loadLines(path: String = RESOURCE_PATH): List<String> {
      val stream =
        WordList::class.java.getResourceAsStream(path)
          ?: throw WordListException(
            "Bundled English word list not found at classpath resource '$path'. Ensure " +
              "src/main/resources/google-10000-english.txt is present and included in the build."
          )
      return try {
        stream.use { it.reader(Charsets.UTF_8).readLines() }
      } catch (e: IOException) {
        throw WordListException(
          "Failed to read the bundled English word list at '$path': ${e.message}",
          e,
        )
      }
    }

    /**
     * Builds a [WordList] from raw [lines], applying the normalization rules documented on the
     * class. Exposed for testing and for building custom lists.
     */
    fun fromLines(lines: List<String>): WordList {
      val seen = HashSet<String>()
      val entries = ArrayList<WordEntry>()
      for (line in lines) {
        val word = line.trim().lowercase()
        if (word.isEmpty()) continue
        if (!word.all { it in 'a'..'z' }) continue
        if (!seen.add(word)) continue
        entries.add(WordEntry(word, entries.size, letterMask(word)))
      }
      return WordList(entries)
    }

    /** Computes the 26-bit letter mask for [word]. */
    fun letterMask(word: String): Int {
      var mask = 0
      for (c in word) mask = mask or (1 shl (c - 'a'))
      return mask
    }
  }
}

/** Thrown when the bundled word list resource is missing or unreadable. */
class WordListException(message: String, cause: Throwable? = null) :
  RuntimeException(message, cause)
