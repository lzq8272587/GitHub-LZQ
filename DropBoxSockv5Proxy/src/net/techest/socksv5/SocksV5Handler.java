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
 *  Created on : 2011-8-20, 下午4:16:25
 *  Author     : princehaku
 */

package net.techest.socksv5;



import java.io.IOException;
import java.net.Socket;
import java.util.Date;

import net.techest.socksv5.auth.AuthException;
import net.techest.socksv5.helper.ConnectionInfo;
import net.techest.socksv5.helper.Logger;
import net.techest.socksv5.helper.Recorder;
import net.techest.socksv5.socks.DataPipe;
import net.techest.socksv5.socks.IDataPipe;
import net.techest.socksv5.socks.SocksException;
import net.techest.socksv5.socks.SocksHandShake;
import net.techest.socksv5.socks.SocksMessage;
import net.techest.socksv5.socks.SocksSocket;



public class SocksV5Handler extends Thread {

	SocksSocket sck;

	String start_Time;
	public SocksV5Handler(Socket clientsck) {
		try {
			this.sck = new SocksSocket(clientsck);
			start_Time=new Date().toString().replace(" ", "_");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		IDataPipe dp = null;
		SocksSocket sc=null;
		
		try {
			// 握手
			SocksHandShake hs = new SocksHandShake(this.sck);
			hs.accept();
			// 端口连接
			SocksMessage sm = new SocksMessage(hs);
			sm.accept();
			sc = sm.getBindedSocket();
			// 接收用户请求和转发消息
			dp = new DataPipe();
			dp.pipe(this.sck, sc);//在这里，sck为代理服务器接收到的来自应用的TCP连接，所以sck维护的是和应用之间的联系，sc是通过解析SOCKv5开始交互的那部份信息，创建的和服务器之间的联系
			
			while (dp.isKeepAlive()) {
				SocksV5Handler.sleep(300);
			}
			String end_Time=new Date().toString().replace(" ", "_");
			ConnectionInfo ci=new ConnectionInfo(sck.getSocket().getRemoteSocketAddress().toString(), sc.getSocket().getRemoteSocketAddress().toString(), ""+sck.getSocket().getPort(), ""+sc.getSocket().getPort(), start_Time, end_Time, ""+dp.getUploadTraffic(), ""+dp.getDownloadTraffic());
			Recorder.recordConnectionInfo(ci);
			dp.release();
		} catch (AuthException e) {
			// 验证失败
			//e.printStackTrace();
			Logger.getInstance().log("验证失败  "+e.getMessage());
		} catch (SocksException e) {
			// socks端口绑定失败
			Logger.getInstance().log("socks端口绑定失败  "+e.getMessage());
		} catch (IOException e) {
			// 转发内容失败
			Logger.getInstance().log("转发内容失败  "+e.getMessage());
		} catch (InterruptedException e) {
			// 等待失败 进程被其他终止
			Logger.getInstance().log("内部进程错误"+e.getMessage());
		}

		try {
			this.sck.close();	
			if(sc!=null)sc.close();
		}  catch (IOException e) {
			// 资源释放失败
			Logger.getInstance().log("资源释放失败"+e.getMessage());
		}

		Logger.getInstance().log("######thread ended " + this.toString()+"   from SocksV5Handler--run()######");

	}

}
