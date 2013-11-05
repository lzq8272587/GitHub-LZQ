package com.lzq.trafficdirector.gui;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.lzq.trafficdirector.global.Parameters;

public class StartWindows extends ApplicationWindow {
	static private Text textIP;
	static private Text textPort;
	
	ToolItem toolItemStart;// = new ToolItem(toolBar, SWT.NONE);
	ToolItem toolItemStop; //= new ToolItem(toolBar, SWT.NONE);

	/**
	 * Create the application window.
	 */
	public StartWindows() {
		super(null);
		createActions();
		addToolBar(SWT.FLAT | SWT.WRAP);
		addMenuBar();
		addStatusLine();
	}

	/**
	 * Create contents of the application window.
	 * @param parent
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		{
			ToolBar toolBar = new ToolBar(container, SWT.FLAT | SWT.RIGHT);
			toolBar.setBounds(0, 0, 432, 28);
			toolItemStart = new ToolItem(toolBar, SWT.NONE);
			toolItemStop = new ToolItem(toolBar, SWT.NONE);

			//new ChartBuilder().build();
				toolItemStart.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						System.out.println("Start Service.");
						if(!toolItemStop.isEnabled())
						{
							toolItemStop.setEnabled(true);
							toolItemStart.setEnabled(false);
							//启动服务
							Parameters.port=Integer.parseInt(textPort.getText());
							new ChartBuilder().build();
							StartWindows.this.close();
						}
					}
				});
				toolItemStop.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						System.out.println("Service Stop");
						if(!toolItemStart.isEnabled())
						{
							toolItemStart.setEnabled(true);
							toolItemStop.setEnabled(false);
						}
					}
				});
				
				toolItemStart.setText("启动服务");
				toolItemStop.setText("关闭服务");
				toolItemStop.setEnabled(false);
			
		}
		{
			Label lblProxyIpAddress = new Label(container, SWT.NONE);
			lblProxyIpAddress.setBounds(20, 63, 165, 28);
			lblProxyIpAddress.setText("Proxy IP address: ");
		}
		{
			Label lblProxyPortNumber = new Label(container, SWT.NONE);
			lblProxyPortNumber.setText("Proxy Port number: ");
			lblProxyPortNumber.setBounds(20, 101, 165, 28);
		}
		
		Button button = new Button(container, SWT.NONE);
		button.setBounds(149, 157, 102, 30);
		button.setText("重置地址信息");
		
		textIP = new Text(container, SWT.BORDER);
		textIP.setText("192.168.137.100");
		textIP.setBounds(228, 63, 165, 28);
		
		textPort = new Text(container, SWT.BORDER);
		textPort.setText("12345");
		textPort.setBounds(228, 101, 165, 28);

		return container;
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Create the menu manager.
	 * @return the menu manager
	 */
	@Override
	protected MenuManager createMenuManager() {
		return null;
	}

	/**
	 * Create the toolbar manager.
	 * @return the toolbar manager
	 */
	@Override
	protected ToolBarManager createToolBarManager(int style) {
		return null;
	}

	/**
	 * Create the status line manager.
	 * @return 
	 */
	@Override
	protected StatusLineManager createStatusLineManager() {
		return null;
	}

	/**
	 * Launch the application. 图形界面启动入口
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			StartWindows window = new StartWindows();
			window.setBlockOnOpen(true);
			window.open();
			Display.getCurrent().dispose();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Configure the shell.
	 * @param newShell
	 */
	@Override
	protected void configureShell(Shell newShell) {
		newShell.setImage(SWTResourceManager.getImage(StartWindows.class, "/com/sun/java/swing/plaf/windows/icons/Inform.gif"));
		super.configureShell(newShell);
		newShell.setText("Traffic Director PC");
	}

	/**
	 * Return the initial size of the window.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(450, 274);
	}
}
