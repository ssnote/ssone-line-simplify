import org.xml.sax.helpers.DefaultHandler
import org.xml.sax.Attributes

import javax.xml.parsers.SAXParser
import javax.xml.parsers.SAXParserFactory

import java.io.InputStream
import java.io.File


class MyHandler(
    private val callbackStrokeData: (String)->Unit,
    private val callbackDocumentEnd: ()->Unit) : DefaultHandler() {

    private var underStrokeElement = false

    override fun startElement(
        uri: String,
        localName: String,
        qName: String,
        attrs: Attributes) {

        if( qName=="ss:stroke" ){ underStrokeElement = true }
    }

    override fun endElement(uri: String, localName: String, qName: String){
        if( qName=="ss:stroke" ){ underStrokeElement = false }
    }

    override fun characters(ch: CharArray, start: Int, length: Int) {
        if( underStrokeElement ){
            val xyParams = 0.until(length).map { index->
                "${ch[start + index]}"
            }.joinToString("")
    
            callbackStrokeData(xyParams)
        }
    }

    override fun endDocument(){
        callbackDocumentEnd()
    }
}


typealias StrokeParser = (File)->Unit
typealias StrokeParseResult = (List<String>)->Unit

val toStrokeParser: (StrokeParseResult)->StrokeParser = { strokeParseResult->
    val xyParamsList = mutableListOf<String>()

    val callbackStrokeData: (String)->Unit = { xyParams->
        xyParamsList.add(xyParams) 
    }

    val callbackDocumentEnd: ()->Unit = {
        strokeParseResult(xyParamsList)
    }

    val handler = MyHandler(callbackStrokeData, callbackDocumentEnd)

    val strokeParser: StrokeParser = { inputSvgFile->
        inputSvgFile.inputStream().use { inputStream->
            val factory = SAXParserFactory.newInstance()
            val parser = factory.newSAXParser()
            parser.parse(inputStream, handler)
        }
    }

    strokeParser
}

typealias PtsConverter = (List<Float>)->List<Float>

val toSVG: (List<String>, PtsConverter, Pair<String, String>, Boolean)->String = { xyParamsList, ptsConverter, colorPair, fillBackground->
    val foregroundColor = colorPair.first
    val backgroundColor = colorPair.second

    val toPath: (String)->String = { xyParams->
        val xyList = ptsConverter( xyParams.split(",").map { it.toFloat() } )

        val xList  = xyList.mapIndexed { index, value-> if(index%2==0){ value } else { null } }.mapNotNull{ it }
        val yList  = xyList.mapIndexed { index, value-> if(index%2==1){ value } else { null } }.mapNotNull{ it }
    
        val ptList = xList.zip(yList)

        val head = ptList.first()
        val tail = ptList.drop(1)
        val data = listOf(
            listOf("M ${head.first} ${head.second}"),
            tail.map { "L ${it.first} ${it.second}" }
        ).flatten().joinToString(" ")

        "<path d=\"${data}\"/>"
    }

    val toXValueMinAndMax: (List<String>)->Pair<Float,Float> = { xyParamsList0->
        val xList: List<Float> = xyParamsList0.fold(listOf<Float>(), { acc, xyParams->
            val xyList = ptsConverter( xyParams.split(",").map { it.toFloat() } )
            val xList  = xyList.mapIndexed { index, value-> if(index%2==0){ value } else { null } }.mapNotNull{ it }
            acc + xList
        })
        Pair(xList.minOf{ it }, xList.maxOf{ it })
    }

    val toYValueMinAndMax: (List<String>)->Pair<Float,Float> = { xyParamsList0->
        val yList: List<Float> = xyParamsList0.fold(listOf<Float>(), { acc, xyParams->
            val xyList = ptsConverter( xyParams.split(",").map { it.toFloat() } )
            val yList  = xyList.mapIndexed { index, value-> if(index%2==1){ value } else { null } }.mapNotNull{ it }
            acc + yList
        })
        Pair(yList.minOf{ it }, yList.maxOf{ it })
    }

    val pathList = xyParamsList.map { toPath(it) }


    val (minX, maxX) = toXValueMinAndMax(xyParamsList)
    val (minY, maxY) = toYValueMinAndMax(xyParamsList)

    val strokeWidth = 0.254f
    val w = (maxX - minX) + strokeWidth
    val h = (maxY - minY) + strokeWidth

    val header = listOf(
        "<?xml version=\"1.0\" encoding=\"utf-8\"?>",
        "\n",
        "<!DOCTYPE svg PUBLIC ",
            "\"-//W3C//DTD SVG 1.1//EN\" ",
            "\"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">",
        "<svg ",
            "xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" ",
            "x=\"0mm\" y=\"0mm\" width=\"${w}mm\" height=\"${h}mm\" ",
            "viewBox=\"0.0 0.0 ${w} ${h}\">")

    val background = listOf(
        "<g fill=\"${backgroundColor}\">",
        "<path d=\"M 0 0 L ${w} 0 L ${w} ${h} L 0 ${h} z\"/>",
        "</g>")

    val body = listOf(
        "<g stroke=\"${foregroundColor}\" stroke-width=\"${strokeWidth}\" fill=\"none\">",
        pathList.joinToString(""),
        "</g>")
    val footer = listOf("</svg>")

    if( fillBackground ){
        (header + background + body + footer).joinToString("")
    } else {
        (header + body + footer).joinToString("")
    }
}


val fillBackground = true
val foregroundColor = "rgb(0, 0, 0)"// rgb(76, 96, 130)
val backgroundColor = "rgb(255, 255, 255)"

if( args.size>2 ){
    val inputSvgFilename = args[0]
    val outputSvgFilename = args[1]

    val epsilon = args[2].toDouble() 
    val ptsConverter: PtsConverter = { pts-> LineSimple.convertPts( pts, epsilon ) }

    val inputSvgFile = File(inputSvgFilename)
    val outputSvgFile = File(outputSvgFilename)

    val strokeParser = toStrokeParser {xyParamsList->
        val svg = toSVG(xyParamsList, ptsConverter, Pair(foregroundColor, backgroundColor), fillBackground)
        outputSvgFile.writer(Charsets.UTF_8).use { writer-> writer.write(svg) }
    } 

    strokeParser(inputSvgFile)
}
