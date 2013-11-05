package com.lzq.trafficdirector.proxy;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.lzq.trafficdirector.global.Parameters;

public class AsynchronousProxyServer implements ProxyServer{
	private int servPort = 0;
	// nio class
	private Selector selector = null;
	private Charset charset = Charset.forName("UTF-8");
	private ServerSocketChannel server = null;
	
	private HashMap<SocketChannel, SocketChannel> App_Proxy_Map = new HashMap<SocketChannel, SocketChannel>();

	public AsynchronousProxyServer(int p) {
		servPort = p;
	}

	public void initial() {
		// try {
		// System.setOut(new PrintStream(new FileOutputStream(new
		// File("C:\\Users\\LZQ\\Desktop\\log.txt"))));
		// } catch (FileNotFoundException e1) {
		// // TODO Auto-generated catch block
		// e1.printStackTrace();
		// }
		try {
			// bind address and regist server socket to selector
			selector = Selector.open();
			server = ServerSocketChannel.open();
			server.socket().bind(new InetSocketAddress(servPort));
			server.configureBlocking(false);
			server.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			System.err.println("初始化错误");
			e.printStackTrace();
		}
	}

	public void start() {
		ForwardingFromAppToPrxoy ffap = new ForwardingFromAppToPrxoy();
		ffap.start();
	}

	/**
	 * 负责异步的将从应用程序处获得的数据直接转发给代理服务器
	 * 
	 * @author LZQ
	 * 
	 */
	class ForwardingFromAppToPrxoy extends Thread {
		public void run() {
			try {
				// check if there is some selected channel
				int select;
				while ((select = selector.select()) > 0) {
					System.out.println("loop in while : ForwardingFromAppToPrxoy   select=" + select + "  " + selector.selectedKeys().size());
					// Set<SelectionKey> deletSet = new HashSet<SelectionKey>();
					java.util.Iterator<SelectionKey> itr = selector.selectedKeys().iterator();
					while (itr.hasNext()) {
						// System.out
						// .println("loop in for : ForwardingFromAppToPrxoy");
						// selector_from_app.selectedKeys().remove(sk);
						// server socket: accept event
						SelectionKey sk = itr.next();
						itr.remove();
						if (sk.isAcceptable()) {
							/*
							 * 如果是一个新的连接连入了，那么要创建一条到代理服务器的Socket连接
							 */
							// 到远端代理的Socket连接//
							SocketChannel proxy_sc = SocketChannel.open(new InetSocketAddress(Parameters.RemoteProxyIP,Parameters.RemoteProxyPort));
							proxy_sc.configureBlocking(false);
							proxy_sc.register(selector, SelectionKey.OP_READ);
							System.out.println("注册新连接。");
							// 和应用程序之间的Socket连接
							SocketChannel sc = server.accept();
							sc.configureBlocking(false);
							sc.register(selector, SelectionKey.OP_READ);
							sk.interestOps(SelectionKey.OP_ACCEPT);
							// 将这两个Socket连接联系起来
							App_Proxy_Map.put(sc, proxy_sc);
						}
						// communication socket: read event
						else if (sk.isReadable()) {
							SocketChannel sc = (SocketChannel) sk.channel();
							if (App_Proxy_Map.containsKey(sc)) {
								// System.out.println("read data from app side.");
								SocketChannel proxy_socket = App_Proxy_Map.get(sc);
								ByteBuffer buff = ByteBuffer.allocate(40960);
								try {
									int cont = 0;
									while ((cont = sc.read(buff)) > 0) {
										buff.flip();
										// 把从远端代理中得到的数据转发给对应的应用程序连接到的Socket
										System.out.println(buff.toString());
										proxy_socket.write(buff);
									}
									// 如果Socket被关闭了，那么read一直会返回-1，同时产生READ事件
									if (cont == -1) {
										// sc.close();
										sk.cancel();
										System.out.println("SelectionKey close.");
										continue;
									}
									// sk.cancel();
								} catch (IOException e) {
									System.err.println("Error: " + "read data from app side.");
									e.printStackTrace();
									// deletSet.add(sk);
									sk.cancel();
									continue;
								}
							} else if (App_Proxy_Map.containsValue(sc)) {
								// System.out.println("read data from proxy side.");
								SocketChannel app_socket = null;
								Set<Entry<SocketChannel, SocketChannel>> entry = App_Proxy_Map.entrySet();
								for (Map.Entry e : entry) {
									if (e != null && e.getValue().equals(sc)) {
										app_socket = (SocketChannel) e.getKey();
									}
								}
								ByteBuffer buff = ByteBuffer.allocate(40960);
								try {
									int cont = 0;
									while ((cont = sc.read(buff)) > 0) {
										buff.flip();
										// 把从远端代理中得到的数据转发给对应的应用程序连接到的Socket
										System.out.println(buff.toString());

										app_socket.write(buff);// a write
																// operation
																// will return
																// only after
																// writing all
																// of the r
																// requested
																// bytes.
									}
									// 如果Socket被关闭了，那么read一直会返回-1，同时产生READ事件
									if (cont == -1) {
										// sc.close();   
										System.out.println("SelectionKey close.");
										sk.cancel();
										continue;
									}
									// sk.cancel();
								} catch (IOException e) {
									System.err.println("Error: " + "read data from proxy side.");
									e.printStackTrace();
									// deletSet.add(sk);
									sk.cancel();
									continue;
								}
							} else {
								System.err.println("^^^^^^^^^^^^^^^^^unexisted socket.");
							}
							if (sk.isValid())
								sk.interestOps(SelectionKey.OP_READ);
						}
						// deletSet.add(sk);
					}
					// System.out.println("After while, selectionKey size: "
					// + selector.selectedKeys().size());
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		//关闭服务
		
		try {
			selector.close();
			server.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
