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

import javax.swing.JTextPane;

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

		VirtualFile vFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
		VirtualFile vPath = e.getData(PlatformDataKeys.PROJECT_FILE_DIRECTORY);
		if (vFile == null || vPath == null) {
			return;
		}
		String ext = vFile.getExtension();
		if (ext == null || (!ext.equalsIgnoreCase("js") && !ext.equalsIgnoreCase("css"))) {
			return;
		}

		String file = vFile.getPath();
		String path = file.substring(0, file.lastIndexOf(vFile.getName()));
		String outFile = path + vFile.getNameWithoutExtension() + ".min." + vFile.getExtension();

		if (this.project == null) {
			this.project = e.getProject();
		}
		if (this.log == null) {
			this.log = new JTextPane();
		}
		if (this.toolWindow == null) {
			JBScrollPane scrollPane = new JBScrollPane(this.log);
			Content content = ContentFactory.SERVICE.getInstance().createContent(scrollPane, "Output", false);
			this.toolWindow = ToolWindowManager.getInstance(this.project).registerToolWindow("Assets", true, ToolWindowAnchor.BOTTOM);
			this.toolWindow.getContentManager().addContent(content);
		}

		YUICompressRunnable doCompress = new YUICompressRunnable();
		doCompress.vPath = vPath;
		doCompress.inputFile = file;
		doCompress.outputFile = outFile;
		doCompress.ext = ext;
		doCompress.log = this.log;

		this.toolWindow.activate(doCompress);
	}

}
