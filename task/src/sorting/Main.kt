package sorting

import java.io.File
import java.util.*

const val OPTION_DATATYPE = "-dataType"
const val OPTION_SORTING_TYPE = "-sortingType"
const val OPTION_INPUT_FILE = "-inputFile"
const val OPTION_OUTPUT_FILE = "-outputFile"

enum class SortingType() {
    NATURAL,
    BYCOUNT
}

enum class DataType(val type: String) {
    LONG("number"),
    LINE("line"),
    WORD("word")
}

fun <T>getOption(args: Array<String>, usedArguments:MutableList<Int>, optionName: String, valueOf: (String) -> T, default: T): T {
    return if (args.contains(optionName)) {
        val index = args.indexOf(optionName)
        usedArguments.add(index)
        usedArguments.add(index + 1)
        valueOf(args[index + 1])
    } else {
        default
    }
}

fun main(args: Array<String>) {
    val usedArguments = mutableListOf<Int>()
    val sortingType = try {
        getOption(args, usedArguments, OPTION_SORTING_TYPE, { SortingType.valueOf(it.uppercase()) }, SortingType.NATURAL)
    } catch (e: RuntimeException) {
        println("No sorting type defined!")
        return
    }

    val dataType = try {
        getOption(args, usedArguments, OPTION_DATATYPE, { DataType.valueOf(it.uppercase()) }, DataType.WORD)
    } catch (e: RuntimeException) {
        println("No data type defined!")
        return
    }

    val inputFile = getOption(args, usedArguments, OPTION_INPUT_FILE, { File(it) }, null)
    val outputFile = getOption(args, usedArguments, OPTION_OUTPUT_FILE, { File(it) }, null)
    val printLine: (String) -> Unit = if (outputFile == null)
        { it: String -> println(it)}
    else
        outputFile::appendText

    for (i in args.indices) {
        if (i in usedArguments)
            continue
        println("\"-${args[i]}\" is not a valid parameter. It will be skipped.")
    }

    val analysis = when (dataType) {
        DataType.LONG -> Analysis(inputFile, DataType.LONG, 0L, Scanner::nextLong) { it }
        DataType.WORD -> Analysis(inputFile, DataType.WORD, "", Scanner::next) { it.length.toLong() }
        DataType.LINE -> Analysis(inputFile, DataType.LINE, "", Scanner::nextLine) { it.length.toLong() }
    }
    println("Total ${dataType.type}s: ${analysis.size()}.")

    val sorted = analysis.sort(sortingType)

    if (sortingType == SortingType.NATURAL) {
        if (dataType == DataType.LINE) {
            printLine("Sorted data:")
            sorted.forEach(::println)
        } else {
            printLine("Sorted data: ${sorted.joinToString(" ")}")
        }
    } else {
        sorted.forEachIndexed() { i, a ->
            if (!sorted.subList(0, i).contains(a)) {
                printLine("$a: ${analysis.inputs[a]} time(s) ${(analysis.inputs[a]!! * 100) / analysis.size()}%")
            }
        }
    }
}

class Analysis<T: Comparable<T>>(inputFile: File?, dataType: DataType, initial :T, getNext: (Scanner) -> T, getLength: (T) -> Long) {
    private val scanner = if (inputFile == null)
        Scanner(System.`in`)
    else
        Scanner(inputFile!!)

    private val input = mutableListOf<T>()
    val inputs = mutableMapOf<T, Int>()

    init {
        while(scanner.hasNext()) {
            try {
                val item = getNext(scanner)
                if (inputs.containsKey(item)) {
                    inputs[item] = inputs[item]!! + 1
                } else {
                    inputs[item] = 1
                }
                input.add(item)
            } catch (e: NumberFormatException) {
                println("\"${e.message}\" is not a long. It will be skipped.")
            }
        }
        scanner.close()
    }

    fun sort(sortingType: SortingType) = when (sortingType) {
        SortingType.NATURAL -> sort(input) { a, b -> a.compareTo(b) }
        SortingType.BYCOUNT -> sort(inputs.keys.toList()) { a, b ->
            val countA = input.count{ it == a }
            val countB = input.count{ it == b }
            val comparisonByCount = countA.compareTo(countB)

            if (comparisonByCount != 0) {
                comparisonByCount
            } else {
                a.compareTo(b)
            }
        }
    }

    fun size() = input.size
}

fun <T: Comparable<T>> sort(list: List<T>, compare: (T, T) -> Int): List<T> {
    if (list.isEmpty())
        return list
    val size = list.size
    if (size == 1) {
        return list
    } else {
        val middle = size / 2
        val left = sort(list.subList(0, middle), compare)
        val right = sort(list.subList(middle, size), compare)

        val merged = mutableListOf<T>()

        var i = 0
        var j = 0
        while (i < left.size && j < right.size) {
            if (compare(left[i], right[j]) <= 0) {
                merged.add(left[i++])
            } else {
                merged.add(right[j++])
            }
        }
        while (i < left.size) {
            merged.add(left[i++])
        }
        while (j < right.size) {
            merged.add(right[j++])
        }
        return merged.toList()
    }
}