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
 *  Created on : 2011-8-20, 下午10:27:20
 *  Author     : princehaku
 */

package net.techest.socksv5.socks;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import net.techest.socksv5.enu.EAddrType;
import net.techest.socksv5.enu.ECMDType;
import net.techest.socksv5.enu.EREPType;
import net.techest.socksv5.helper.Logger;

public class SocksMessage {
	int VER;
	/**
	 * 客户端请求
	 * 
	 */
	int CMD;
	/**
	 * 服务器回应消息
	 * 
	 * 　　 * X '00 ' succeeded 　　 * X '01 ' general SOCKS server failure 　　 * X
	 * '02 ' connection not allowed by ruleset 　　 * X '03 ' Network unreachable
	 * 　　 * X '04 ' Host unreachable 　　 * X '05 ' Connection refused 　　 * X '06
	 * ' TTL expired 　　 * X '07 ' Command not supported 　　 * X '08 ' Address
	 * type not supported 　　 * X '09 ' to X 'FF ' unassigned
	 */
	int REP = -1;

	int RSV;
	/**
	 * 地址类型
	 * 
	 */
	int ATYP;
	/**
	 * 地址
	 * 
	 */
	InetAddress D_ADDR;

	int D_PORT;

	SocksSocket s;

	int S_PORT;

	/**
	 * 绑定到目的主机的socket
	 * 
	 */
	private SocksSocket bindSocket;

	/**
	 * 通过握手请求创建 同时进行权限认真
	 * 
	 * @param hs
	 */
	public SocksMessage(SocksHandShake hs) {
		this.s = hs.getHandler();
	}

	/**
	 * 接收请求 绑定socket到目标服务器 返回信息给客户机
	 * 
	 * @throws SocksException
	 */
	public void accept() throws SocksException {
		try {
			this.getRequest();
			Logger.getInstance().log(this.toString()+"  from SocksException-accept");
		} catch (SocksException e) {
			Logger.getInstance().log("代理客户端失败 " + e.getMessage());
		} catch (IOException e) {
			Logger.getInstance().log("客户端连接失败 " + e.getMessage());
		}
		
		try {
			this.sendEcho();
			if (this.REP != 0) {
				throw new SocksException("连接错误  无法连接到服务器");
			}
		} catch (IOException e) {
			throw new SocksException("Socket建立错误 " + e.getMessage());
		}
	}

	public SocksSocket getBindedSocket() {
		return this.bindSocket;
	}

	public SocksSocket getHandler() {
		return s;
	}

	/**
	 * 解析从服务器发来的请求
	 * 
	 * @throws IOException
	 */
	public void getRequest() throws IOException,SocksException {
		byte[] content = this.s.pull(3);
		this.VER = content[0];
		this.CMD = content[1];
		// 不支持的方式
		if (ECMDType.valueOf(this.CMD).equals("")) {
			this.putRep(EREPType.R_CMD_NOT_SUPPORT);
			Logger.getInstance().log("不支持的端口处理方式 CMD:"+this.CMD);
			throw new SocksException("不支持的端口处理方式 CMD:"+this.CMD);
		}
		this.RSV = content[2];
		// 分析ADDR
		content = this.s.pull(1);
		this.ATYP = content[0];
		byte[] addr;
		byte[] host=null;
		try{
			switch (content[0]) {
			case EAddrType.SOCKS_ATYP_IPV4:
				host = this.s.pull(4);
				this.D_ADDR = InetAddress.getByAddress(host);
				break;
			case EAddrType.SOCKS_ATYP_IPV6:
				host = this.s.pull(6);
				this.D_ADDR = InetAddress.getByAddress(host);
				break;
			case EAddrType.SOCKS_ATYP_DOMAINNAME:
				content = this.s.pull(1);
				int length = content[0];
				host = this.s.pull(length);
				this.D_ADDR = InetAddress.getByName(new String(host));
				break;
			default:
				// 不支持的地址格式
				this.putRep(EREPType.R_ADDTYPE_NOT_SUPPORT);
				Logger.getInstance().log("不支持的地址格式");
				throw new SocksException("不支持的地址格式");
			}
			
			Logger.getInstance().log("Domain " + this.D_ADDR+"   from SocksMessage");
			
		}catch (IOException e) {
				Logger.getInstance().log("目标服务器地址解析错误" + e.getMessage());
				this.putRep(EREPType.R_SOCKS_GENERAL_FAILD);
				throw new SocksException("目标服务器地址解析错误" + e.getMessage());
			}
		
		// TODO:黑名单处理

		// 最后两位是端口
		content = this.s.pull(2);
		int port = content[0];
		if (content[0] < 0) {
			port = port + 256;
		}
		port = port << 8;
		if (content[1] < 0) {
			port += 256;
		}
		port += content[1];
		this.D_PORT = port;
		// 绑定到socket
		try {
			if(this.D_ADDR!=null){
				bindSocket = new SocksSocket(this.D_ADDR, this.D_PORT);
				this.S_PORT = bindSocket.getSocket().getLocalPort();
			}
		} catch (UnknownHostException e) {
			this.putRep(EREPType.R_HOST_UNREACHABLE);
			Logger.getInstance().log("目标服务器连接错误" + e.getMessage());
			throw new SocksException("目标服务器连接错误" + e.getMessage());
		} catch (IOException e) {
			this.putRep(EREPType.R_SOCKS_GENERAL_FAILD);
			throw new SocksException("目标服务器连接错误" + e.getMessage());
		}

	}

	/**
	 * 发送回应消息
	 * 
	 * @throws IOException
	 * 
	 */
	public void sendEcho() throws IOException {
		this.s.push(this.VER);
		// 如果上一步有失败信息 则直接发送回去
		if (getRep() != 0) {
			this.s.push(this.REP);
			return;
		}

		this.s.push(getRep());
		this.s.push(this.RSV);
		// 服务器返回自身端口
		this.s.push(0x01);
		this.s.push(InetAddress.getLocalHost().getAddress());
		this.s.push(this.S_PORT >> 8);
		this.s.push(this.S_PORT);
	}

	private int getRep() {
		if (this.REP == -1) {
			this.REP = 0;
		}
		return this.REP;
	}

	/**
	 * 放置REP字段 如果已经存在则忽略
	 * 
	 * @param rep
	 */
	private void putRep(int rep) {
		if (this.REP == -1) {
			this.REP = rep;
		}
	}

	public String toString() {
		if(this.D_ADDR!=null){
			return "CMD " + ECMDType.valueOf(this.CMD) + "   D_ADDR   "
					+ this.D_ADDR.getHostAddress() + " Port " + this.D_PORT;
		}
			return "CMD " + ECMDType.valueOf(this.CMD) + "   Failed ";
		
	}
}
