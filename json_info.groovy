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

def main(def stream) {
	JsonFactory jsonFactory = new JsonFactory()
	JsonParser jp = jsonFactory.createJsonParser( stream )


	while ( true ) {
		def currentToken = jp.nextToken()
		if ( (currentToken == JsonToken.END_OBJECT) && (!jp.parsingContext.parent) ) {
			// if we're at the top level and the object ends, we're at the end of our object.
			// elsewise we're ending a sub-object.
			break
		}

		// todo: support arrays ??
		if (currentToken == JsonToken.FIELD_NAME) {

			def parentInfo = curentParentInfo(jp.parsingContext.parent)

			println ":${jp.getCurrentLocation().lineNr}\tPath:${parentInfo}${nameOrIndex(jp.parsingContext)}\tValue:${jp.currentName}"

		}
	}
}

main( new File("./test.json") )
