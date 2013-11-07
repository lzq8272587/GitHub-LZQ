/*  Copyright 2010 princehaku
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  Created on : 2011-8-21, 下午12:21:26
 *  Author     : princehaku
 */

package net.techest.socksv5.socks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

public class SocksSocket {

	private Socket sck;

	private InputStream in;

	private OutputStream out;

	/**
	 * 连接到远程主机
	 * 
	 * @param d_ADDR
	 * @param d_PORT
	 * @throws IOException
	 */
	public SocksSocket(InetAddress d_ADDR, int d_PORT) throws IOException {
		this.sck = new Socket(d_ADDR, d_PORT);
		init();
	}

	public SocksSocket(Socket clientsck) throws IOException {
		this.sck = clientsck;
		init();
	}

	public void close() throws IOException {
		this.sck.close();
	}

	public Socket getSocket() {
		return this.sck;
	}

	private void init() throws IOException {
		in = this.sck.getInputStream();
		out = this.sck.getOutputStream();
	}

	/**
	 * 获取下一个数据
	 * 
	 * @return int
	 * @throws IOException
	 */
	public int pull() throws IOException {

		int n = -1;
		// 继续读
		try {
			n = in.read();
		} catch (SocketTimeoutException e) {
			// 这里是服务器读取超时了
			throw new IOException("Read timed out");
		} catch (SocketException e) {
			//连接出错
			throw e;
		}
		return n;
	}

	/**
	 * 获取数据
	 * 
	 * @return byte[]
	 * @throws IOException
	 */
	public byte[] pull(int length) throws IOException {

		byte[] s = new byte[length];
		// 继续读
		try {
			in.read(s);
		} catch (SocketTimeoutException e) {
			// 这里是服务器读取超时了
			throw new IOException("Read timed out");
		} catch (IOException e) {
			throw e;
		}
		return s;
	}

	/**
	 * 获取所有数据 依据available()的长度 如果available()无长度 则返回null
	 * 
	 * @return
	 * @throws IOException
	 */
	public byte[] pullAll() throws IOException {
		int length;
		byte[] con = null;
		try {
			length = this.sck.getInputStream().available();
			con = new byte[length];
			in.read(con);
		} catch (SocketTimeoutException e) {
			// 这里是服务器读取超时了
			throw new IOException("Read timed out");
		} catch (SocketException e) {
			//连接出错
			throw e;
		}
		return con;
	}

	/**
	 * 推送字节数组数据
	 * 
	 * @param context
	 * @throws IOException 
	 */
	void push(byte[] context) throws IOException {
		try {
			out.write(context);
		} catch (IOException e) {
			System.out.println("写入失败" + e.getMessage());
			throw e;
		}
	}

	/**
	 * 推送int数据
	 * 
	 * @param context
	 * @throws IOException
	 */
	public void push(int onebyte) throws IOException {
		try {
			out.write(onebyte);
		} catch (IOException e) {
			System.out.println("写入失败" + e.getMessage());
			throw e;
		}
	}

	public void pushAll(byte[] content) {
		try {
			out.write(content);
		} catch (IOException e) {
			System.out.print("写入失败");
			e.printStackTrace();
		}
	}

	/**
	 * 设置tcp的读超时
	 * 
	 * @param time
	 */
	public void setReadTimeOut(int time) {
		try {
			this.sck.setSoTimeout(time);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

}
