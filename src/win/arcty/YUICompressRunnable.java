package win.arcty;

import com.intellij.openapi.vfs.VirtualFile;
import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import javax.swing.*;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import java.io.*;

public class YUICompressRunnable implements Runnable {

	VirtualFile vPath;
	String outputFile;
	String inputFile;
	String ext;
	JTextPane log;

	public void run() {
		this.appendLog("Compressing " + inputFile + "\nPlease wait ...");
		try {
			Reader in = new InputStreamReader(new FileInputStream(inputFile), "UTF-8");
			Writer out = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
			if (ext.equalsIgnoreCase("js")) {
				this.CompressJS(in, out);
			} else if (ext.equalsIgnoreCase("css")) {
				this.CompressCSS(in, out);
			}
		} catch (Exception e) {
			this.appendLog("Compress Failed");
			return;
		}
		this.appendLog("Save to " + outputFile + "\nDone");
		//vPath.refresh(false, true);
	}

	private void CompressCSS(Reader in, Writer out) throws IOException {
		CssCompressor compressor = new CssCompressor(in);
		compressor.compress(out, -1);
		in.close();
	}

	private void CompressJS(Reader in, Writer out) throws IOException {
		JavaScriptCompressor compressor = new JavaScriptCompressor(in, new ErrorReporter() {
			public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
				if (line < 0) {
					YUICompressRunnable.this.appendLog("[WARNING] " + message);
				} else {
					YUICompressRunnable.this.appendLog("[WARNING] " + line + ':' + lineOffset + ':' + message);
				}
			}

			public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
				if (line < 0) {
					YUICompressRunnable.this.appendLog("[ERROR] " + message);
				} else {
					YUICompressRunnable.this.appendLog("[ERROR] " + line + ':' + lineOffset + ':' + message);
				}
			}

			public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
				this.error(message, sourceName, line, lineSource, lineOffset);
				return new EvaluatorException(message);
			}
		});
		compressor.compress(out, -1, false, true, true, false);
		in.close();
	}

	private void appendLog(String text) {
		Document doc = this.log.getDocument();
		SimpleAttributeSet attributes = new SimpleAttributeSet();
		try {
			doc.insertString(doc.getLength(), text + "\n", attributes);
		} catch (Exception e) {
		}
	}

}
