package mocaputils.plotting

import java.awt.Graphics2D
import java.awt.geom.Rectangle2D
import java.io.{BufferedOutputStream, File, FileOutputStream, OutputStream}

import com.lowagie.text.{Document, DocumentException, Rectangle}
import com.lowagie.text.pdf.{DefaultFontMapper, FontMapper, PdfContentByte, 
  PdfTemplate, PdfWriter}

/** Saves a JFreeChart chart to a PDF file. */
object PlotToPDF {
 
  // converts a measurement in mm to iText point units, with an additional 
  //  scale factor to allow for nice-looking font sizes
  private def mmToiText(x: Int) = (x.toDouble / 0.3528).toInt
  
  // width and height are in millimetres
  def save(file: File, chart: { def draw(g2d: Graphics2D, r2d: Rectangle2D) }, 
           width: Int, height: Int) 
  {
    val mapper = new DefaultFontMapper()
    save(file, chart, width, height, mapper)
  }
  
  // width and height are in millimetres
  def save(file: File, chart: { def draw(g2d: Graphics2D, r2d: Rectangle2D) }, 
           width: Int, height: Int, mapper: FontMapper)
  {
    val out = new BufferedOutputStream(new FileOutputStream(file))
    write(out, chart, width, height, mapper)
    out.close()
  }
  
  // width and height are in millimetres
  def write(out: OutputStream, 
            chart: { def draw(g2d: Graphics2D, r2d: Rectangle2D) }, 
            width: Int, height: Int, mapper: FontMapper)
  {
     val w = mmToiText(width)
     val h = mmToiText(height)
     val pageSize = new Rectangle(w, h)
     val document = new Document(pageSize, 50, 50, 50, 50)
     try {
       val writer = PdfWriter.getInstance(document, out)
       document.open()
       val cb = writer.getDirectContent()
       val tp = cb.createTemplate(w, h)
       val g2 = tp.createGraphics(w, h, mapper)
       val r2d = new Rectangle2D.Double(0, 0, w, h)
       chart.draw(g2, r2d)
       g2.dispose()
       cb.addTemplate(tp, 0, 0)
     } catch {
       case de: DocumentException => System.err.println(de.getMessage)
     }
     document.close()
  }
  
}
