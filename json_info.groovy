#!/usr/bin/env groovy

@Grab(group='com.fasterxml.jackson.core', module='jackson-core', version='2.7.0-rc2')

import com.fasterxml.jackson.core.*

String nameOrIndex(JsonStreamContext parsingContext) {
	def currentName = parsingContext.currentName
	def currentInfo = ""

	if (currentName) {
		currentInfo = ".${currentName}"
	} else {
		def index = parsingContext.currentIndex
		if (index)
			currentInfo = "[${index -1}]"
	}

	return currentInfo
}


String curentParentInfo(JsonStreamContext parsingContext) {

	if (parsingContext) {
		def currentInfo = nameOrIndex(parsingContext)
		return curentParentInfo(parsingContext.parent) + currentInfo
	} else {
		return '$'
	}
}


def handleStream(def stream) {
	JsonFactory jsonFactory = new JsonFactory()
	JsonParser jp = jsonFactory.createJsonParser( stream )

	while ( true ) {
		def currentToken = jp.nextToken()
		if ( currentToken == null ) {
			// means we're at the end of our document
			break
		}

		// todo: support arrays ??
		if (currentToken == JsonToken.FIELD_NAME) {

			def parentInfo = curentParentInfo(jp.parsingContext.parent)

			println "${jp.getCurrentLocation().lineNr}\tPath:${parentInfo}${nameOrIndex(jp.parsingContext)}\tField Name:${jp.currentName}"

		}
	}
}

// ==================== Start the program!! ===================================

def stream = System.in
def cli = new CliBuilder(usage: 'json_info.groovy -[h] [file]')
cli.with {
	h longOpt: 'help', 'Show usage information'
}

def options = cli.parse(args)
if (!options) {
	return
}
// Show usage text when -h or --help option is used.
if (options.h) {
	cli.usage()
	return
}

if (options.arguments().size == 1)
	stream = new File( options.arguments()[0])

handleStream( stream )
