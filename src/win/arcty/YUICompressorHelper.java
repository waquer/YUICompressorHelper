package win.arcty;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import javax.swing.JTextPane;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class YUICompressorHelper extends AnAction {

	private Project project;
	private JTextPane log;
	private ToolWindow toolWindow;

	public void update(AnActionEvent e) {
		final VirtualFile vFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
		if (vFile != null) {
			String ext = vFile.getExtension();
			if (ext != null && (ext.equalsIgnoreCase("js") || ext.equalsIgnoreCase("css"))) {
				e.getPresentation().setEnabled(true);
				return;
			}
		}
		e.getPresentation().setEnabled(false);
	}

	public void actionPerformed(AnActionEvent e) {
		if (this.project == null) {
			this.project = e.getProject();
		}
		final VirtualFile vFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
		final VirtualFile vPath = e.getData(PlatformDataKeys.PROJECT_FILE_DIRECTORY);
		if (vFile == null || vPath == null) {
			return;
		}
		final String file = vFile.getPath();
		final String outPath = file.substring(0, file.lastIndexOf(vFile.getName()));
		final String outFile = vFile.getNameWithoutExtension() + ".min." + vFile.getExtension();

		if (this.toolWindow == null) {
			this.log = new JTextPane();
			JBScrollPane scrollPane = new JBScrollPane(this.log);
			Content content = ContentFactory.SERVICE.getInstance().createContent(scrollPane, "Output", false);
			this.toolWindow = ToolWindowManager.getInstance(this.project).registerToolWindow("Assets", true, ToolWindowAnchor.BOTTOM);
			this.toolWindow.getContentManager().addContent(content);
		}

		this.toolWindow.activate(new Runnable() {
			public void run() {
				YUICompressorHelper.this.appendLog("Compressing " + file + "\nPlease wait ...");
				String ext = vFile.getExtension();
				if (ext == null) {
					return;
				} else if (ext.equalsIgnoreCase("js")) {
					YUICompressorHelper.this.CompressJS(file, outPath + outFile);
				} else if (ext.equalsIgnoreCase("css")) {
					YUICompressorHelper.this.CompressCSS(file, outPath + outFile);
				}
				YUICompressorHelper.this.appendLog("Save to " + outPath + outFile + "\nDone");
				vPath.refresh(false, true);
			}
		});
	}

	private void CompressCSS(String inputFile, String outputFile) {
		try {
			InputStreamReader in = new InputStreamReader(new FileInputStream(inputFile), "UTF-8");
			CssCompressor compressor = new CssCompressor(in);
			in.close();
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
			compressor.compress(out, -1);
		} catch (Exception e) {
			YUICompressorHelper.this.appendLog("Compress failed");
		}
	}

	private void CompressJS(String inputFile, String outputFile) {
		try {
			InputStreamReader in = new InputStreamReader(new FileInputStream(inputFile), "UTF-8");
			JavaScriptCompressor compressor = new JavaScriptCompressor(in, new ErrorReporter() {
				public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
					if (line < 0) {
						YUICompressorHelper.this.appendLog("[WARNING] " + message);
					} else {
						YUICompressorHelper.this.appendLog("[WARNING] " + line + ':' + lineOffset + ':' + message);
					}
				}
				public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
					if (line < 0) {
						YUICompressorHelper.this.appendLog("[ERROR] " + message);
					} else {
						YUICompressorHelper.this.appendLog("[ERROR] " + line + ':' + lineOffset + ':' + message);
					}
				}
				public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
					this.error(message, sourceName, line, lineSource, lineOffset);
					return new EvaluatorException(message);
				}
			});
			in.close();
			OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(outputFile), "UTF-8");
			compressor.compress(out, -1, false, true, true, false);
		} catch (Exception e) {
			YUICompressorHelper.this.appendLog("Compress failed");
		}
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
