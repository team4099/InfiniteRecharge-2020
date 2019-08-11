package org.usfirst.frc.team4099.lib.util

import java.io.FileNotFoundException
import java.io.PrintWriter
import java.lang.reflect.Field
import java.util.concurrent.ConcurrentLinkedDeque

class ReflectingCSVWriter<T>(file: String, typeClass: Class<T>) {

    var linesToWrite = ConcurrentLinkedDeque<String>()
    var output: PrintWriter? = null
    var fields: Array<Field> = typeClass.fields

    fun add(value: T) {
        val line = StringBuffer()
        for (field: Field in fields) {
            if (line.isNotEmpty()) {
                line.append(", ")
            }
            try {
                line.append(field.get(value).toString())
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
            }
        }
        linesToWrite.add(line.toString())
    }

    @Synchronized fun writeLine(line: String) {
        if (output != null) {
            output!!.println(line)
        }
    }

    fun write() {
        var value: String
        while (true) {
            try {
                value = linesToWrite.pollFirst()
                print(value)
                if (value == null) {
                    break
                }
                writeLine(value)
            } catch (e : IllegalStateException) {
                break
            }
        }
    }

    @Synchronized fun flush() {
        if (output != null) {
            write()
            output!!.flush()
        }
    }

    init {
        try {
            output = PrintWriter(file)
//            output?.write("")
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        val line = StringBuffer()
        for (field: Field in fields) {
            if (line.isNotEmpty()) {
                line.append(", ")
            }
            line.append(field.name)
        }
        writeLine(line.toString())
    }
}