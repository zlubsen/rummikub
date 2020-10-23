package eu.lubsen.rummikub

import com.xenomachina.argparser.ArgParser

class CliArguments(parser:ArgParser) {
    val ssl by parser.flagging("--ssl", help = "enable SSL")
}